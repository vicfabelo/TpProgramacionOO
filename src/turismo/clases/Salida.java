package turismo.clases;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import turismo.excepciones.CupoExcedidoExcepcion;
import turismo.excepciones.EstadoInvalidoExcepcion;
import turismo.excepciones.GuiaNoDisponibleExcepcion;
import turismo.excepciones.ReglaNegocioExcepcion;
import turismo.interfaces.PoliticaCancelacion;
import turismo.interfaces.Promocion;

/**
 * Realización concreta de un tour en una fecha.
 * Creador (GRASP): la salida contiene sus reservas, por eso es quien las crea.
 * Experto (GRASP): conoce su cupo y sus reservas, por eso calcula lugares y su propio estado.
 * Comparable: el orden natural de las salidas es por fecha de inicio.
 */
public class Salida implements Comparable<Salida>, Serializable {
    private static final long serialVersionUID = 1L;

    /** Días antes de la salida en que se controla el cupo mínimo. */
    public static final int DIAS_CONTROL_CUPO_MINIMO = 7;

    private final String codigo;
    private final Tour tour;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;
    private final int cupoMaximo;
    private EstadoSalida estado = EstadoSalida.PROGRAMADA;
    private boolean alcanzoCupoMinimo;
    private Guia guia;
    private Vehiculo vehiculo;
    private boolean listaPasajerosEmitida;
    private Liquidacion liquidacion;
    private final List<Reserva> reservas = new ArrayList<>();

    public Salida(String codigo, Tour tour, LocalDate fechaInicio, int cupoMaximo) throws ReglaNegocioExcepcion {
        if (tour == null || fechaInicio == null) {
            throw new IllegalArgumentException("El tour y la fecha son obligatorios");
        }
        if (cupoMaximo < tour.getCupoMinimo() || cupoMaximo > tour.getCupoMaximo()) {
            throw new ReglaNegocioExcepcion("El cupo de la salida debe estar entre " + tour.getCupoMinimo()
                    + " y " + tour.getCupoMaximo() + " (límites del tour)");
        }
        tour.validarServiciosObligatorios();
        this.codigo = codigo;
        this.tour = tour;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaInicio.plusDays(tour.getDuracionDias() - 1L);
        this.cupoMaximo = cupoMaximo;
    }

    // ---------------------------------------------------------------- reservas

    /**
     * Crea una reserva para esta salida (Creador).
     * Valida estado, cupo y condiciones del tour antes de crearla.
     */
    public Reserva crearReserva(int numero, Cliente titular, Vendedor vendedor, List<Pasajero> pasajeros,
                                LocalDate fechaReserva, List<Promocion> promociones) throws ReglaNegocioExcepcion {
        if (!estaAbiertaParaReservas()) {
            throw new EstadoInvalidoExcepcion("La salida " + codigo + " está " + estado + " y no admite reservas");
        }
        if (pasajeros == null || pasajeros.isEmpty()) {
            throw new ReglaNegocioExcepcion("La reserva debe tener al menos un pasajero");
        }
        if (!fechaReserva.isBefore(fechaInicio)) {
            throw new ReglaNegocioExcepcion("No se puede reservar el mismo día de la salida o después");
        }
        if (pasajeros.size() > getLugaresDisponibles()) {
            throw new CupoExcedidoExcepcion("Se piden " + pasajeros.size() + " lugares y quedan "
                    + getLugaresDisponibles() + " en la salida " + codigo);
        }
        for (Pasajero pasajero : pasajeros) {
            tour.validarPasajero(pasajero, fechaInicio);
        }
        Reserva reserva = new Reserva(numero, fechaReserva, titular, this, vendedor);
        for (Pasajero pasajero : pasajeros) {
            reserva.agregarPasajero(pasajero);
        }
        reserva.calcularTotal(promociones);
        reservas.add(reserva);
        actualizarEstado();
        return reserva;
    }

    public boolean estaAbiertaParaReservas() {
        return estado == EstadoSalida.PROGRAMADA || estado == EstadoSalida.CONFIRMADA;
    }

    /** Lugares ocupados: pasajeros de todas las reservas que no están anuladas. */
    public int getLugaresOcupados() {
        int ocupados = 0;
        for (Reserva reserva : reservas) {
            if (reserva.getEstado() != EstadoReserva.ANULADA) {
                ocupados += reserva.getCantidadPasajeros();
            }
        }
        return ocupados;
    }

    public int getLugaresDisponibles() {
        return Math.max(0, cupoMaximo - getLugaresOcupados());
    }

    /**
     * Recalcula el estado según el cupo. La reserva la llama cuando se confirma o se anula.
     * Supuesto: una salida que ya llegó al cupo mínimo no vuelve a PROGRAMADA si después se cancela una reserva.
     */
    void actualizarEstado() {
        if (estado != EstadoSalida.PROGRAMADA && estado != EstadoSalida.CONFIRMADA && estado != EstadoSalida.COMPLETA) {
            return;
        }
        int pasajerosConfirmados = 0;
        for (Reserva reserva : reservas) {
            if (reserva.getEstado() == EstadoReserva.CONFIRMADA) {
                pasajerosConfirmados += reserva.getCantidadPasajeros();
            }
        }
        if (pasajerosConfirmados >= tour.getCupoMinimo()) {
            alcanzoCupoMinimo = true;
        }
        if (getLugaresOcupados() >= cupoMaximo) {
            estado = EstadoSalida.COMPLETA;
        } else if (alcanzoCupoMinimo) {
            estado = EstadoSalida.CONFIRMADA;
        } else {
            estado = EstadoSalida.PROGRAMADA;
        }
    }

    /** Si faltan 7 días o menos y no se llegó al cupo mínimo, la salida se debe cancelar. */
    public boolean debeCancelarsePorCupoMinimo(LocalDate hoy) {
        return estado == EstadoSalida.PROGRAMADA && !hoy.isBefore(fechaInicio.minusDays(DIAS_CONTROL_CUPO_MINIMO));
    }

    /** Cancelación provocada por la agencia: se devuelve el 100 % de todas las reservas. */
    public void cancelarPorAgencia(LocalDate fecha, String motivo) throws EstadoInvalidoExcepcion {
        if (estado == EstadoSalida.EN_CURSO || estado == EstadoSalida.FINALIZADA || estado == EstadoSalida.CANCELADA) {
            throw new EstadoInvalidoExcepcion("La salida " + codigo + " está " + estado + " y no se puede cancelar");
        }
        PoliticaCancelacion politica = new PoliticaCancelacionAgencia();
        for (Reserva reserva : reservas) {
            if (reserva.getEstado() != EstadoReserva.ANULADA) {
                reserva.cancelar(fecha, motivo, politica);
            }
        }
        estado = EstadoSalida.CANCELADA;
        if (guia != null) {
            guia.quitarSalida(this);
        }
    }

    // ---------------------------------------------------------------- operación

    public void asignarGuia(Guia nuevoGuia) throws ReglaNegocioExcepcion {
        verificarEnPreparacion();
        if (!nuevoGuia.estaHabilitadoPara(tour.getCategoria())) {
            throw new GuiaNoDisponibleExcepcion(nuevoGuia.getNombreCompleto() + " no está habilitado para tours de "
                    + tour.getCategoria());
        }
        if (!nuevoGuia.estaDisponible(fechaInicio, fechaFin, this)) {
            throw new GuiaNoDisponibleExcepcion(nuevoGuia.getNombreCompleto() + " ya tiene otra salida entre "
                    + fechaInicio + " y " + fechaFin);
        }
        if (guia != null) {
            guia.quitarSalida(this);
        }
        guia = nuevoGuia;
        nuevoGuia.agregarSalida(this);
    }

    public void asignarVehiculo(Vehiculo nuevoVehiculo) throws ReglaNegocioExcepcion {
        verificarEnPreparacion();
        if (nuevoVehiculo.getCapacidad() < cupoMaximo) {
            throw new ReglaNegocioExcepcion("El vehículo " + nuevoVehiculo.getPatente() + " tiene " + nuevoVehiculo.getCapacidad()
                    + " lugares y la salida tiene cupo " + cupoMaximo);
        }
        vehiculo = nuevoVehiculo;
    }

    /** Lista de pasajeros de reservas confirmadas, ordenada por apellido y nombre (Comparator). */
    public List<Pasajero> emitirListaPasajeros() throws EstadoInvalidoExcepcion {
        if (estado != EstadoSalida.CONFIRMADA && estado != EstadoSalida.COMPLETA) {
            throw new EstadoInvalidoExcepcion("Solo se emite la lista de salidas confirmadas o completas (estado actual: "
                    + estado + ")");
        }
        List<Pasajero> lista = new ArrayList<>();
        for (Reserva reserva : reservas) {
            if (reserva.getEstado() == EstadoReserva.CONFIRMADA) {
                lista.addAll(reserva.getPasajeros());
            }
        }
        lista.sort(Pasajero.POR_APELLIDO_Y_NOMBRE);
        listaPasajerosEmitida = true;
        return lista;
    }

    /** No se puede iniciar una salida sin lista de pasajeros ni sin guía asignado. */
    public void iniciar() throws EstadoInvalidoExcepcion {
        if (estado != EstadoSalida.CONFIRMADA && estado != EstadoSalida.COMPLETA) {
            throw new EstadoInvalidoExcepcion("Solo se inicia una salida confirmada o completa (estado actual: " + estado + ")");
        }
        if (guia == null) {
            throw new EstadoInvalidoExcepcion("No se puede iniciar la salida " + codigo + " sin guía asignado");
        }
        if (!listaPasajerosEmitida) {
            throw new EstadoInvalidoExcepcion("No se puede iniciar la salida " + codigo + " sin emitir la lista de pasajeros");
        }
        estado = EstadoSalida.EN_CURSO;
    }

    /** Cierre de la salida por el coordinador. */
    public void finalizar() throws EstadoInvalidoExcepcion {
        if (estado != EstadoSalida.EN_CURSO) {
            throw new EstadoInvalidoExcepcion("Solo se finaliza una salida en curso (estado actual: " + estado + ")");
        }
        estado = EstadoSalida.FINALIZADA;
        for (Reserva reserva : reservas) {
            reserva.finalizar();
        }
    }

    /** Total vendido en servicios adicionales (base del 5 % del guía). */
    public double calcularVentasAdicionales() {
        double total = 0;
        for (Reserva reserva : reservas) {
            if (reserva.getEstado() != EstadoReserva.ANULADA) {
                total += reserva.getTotalAdicionales();
            }
        }
        return total;
    }

    /** Creador: la salida finalizada genera la liquidación de su guía. */
    public Liquidacion liquidarHonorariosGuia(LocalDate fecha) throws EstadoInvalidoExcepcion {
        if (estado != EstadoSalida.FINALIZADA) {
            throw new EstadoInvalidoExcepcion("Solo se liquidan salidas finalizadas (estado actual: " + estado + ")");
        }
        if (liquidacion != null) {
            throw new EstadoInvalidoExcepcion("La salida " + codigo + " ya fue liquidada");
        }
        liquidacion = new Liquidacion(guia, this, tour.getDuracionDias(), guia.getHonorarioPorDia(),
                calcularVentasAdicionales(), fecha);
        return liquidacion;
    }

    private void verificarEnPreparacion() throws EstadoInvalidoExcepcion {
        if (estado != EstadoSalida.PROGRAMADA && estado != EstadoSalida.CONFIRMADA && estado != EstadoSalida.COMPLETA) {
            throw new EstadoInvalidoExcepcion("La salida " + codigo + " está " + estado + " y ya no se puede modificar");
        }
    }

    public boolean seSuperponeCon(Salida otra) {
        return !fechaInicio.isAfter(otra.fechaFin) && !fechaFin.isBefore(otra.fechaInicio);
    }

    @Override
    public int compareTo(Salida otra) {
        int porFecha = fechaInicio.compareTo(otra.fechaInicio);
        return porFecha != 0 ? porFecha : codigo.compareTo(otra.codigo);
    }

    // ---------------------------------------------------------------- getters

    public String getCodigo() {
        return codigo;
    }

    public Tour getTour() {
        return tour;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public EstadoSalida getEstado() {
        return estado;
    }

    public Guia getGuia() {
        return guia;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public boolean isListaPasajerosEmitida() {
        return listaPasajerosEmitida;
    }

    public Liquidacion getLiquidacion() {
        return liquidacion;
    }

    public List<Reserva> getReservas() {
        return Collections.unmodifiableList(reservas);
    }

    @Override
    public String toString() {
        return codigo + " - " + tour.getNombre() + " (" + fechaInicio + ")";
    }
}