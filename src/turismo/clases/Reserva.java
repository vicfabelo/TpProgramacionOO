package turismo.clases;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import turismo.excepciones.EstadoInvalidoExcepcion;
import turismo.excepciones.ReglaNegocioExcepcion;
import turismo.interfaces.PoliticaCancelacion;
import turismo.interfaces.Promocion;

/**
 * Reserva de un cliente para una salida.
 * Experto (GRASP): tiene los pasajeros y los pagos, por eso calcula total, seña y saldo.
 * Creador (GRASP): contiene sus pagos, ajustes y su cancelación, por eso los crea.
 * Una reserva nunca se elimina: las correcciones se hacen con notas de ajuste.
 */
public class Reserva implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final double PORCENTAJE_SENA = 0.30;
    public static final int DIAS_LIMITE_SALDO = 10;
    public static final int DIAS_LIMITE_REEMPLAZO = 3;
    /** Margen para comparar importes con decimales. */
    private static final double TOLERANCIA = 0.009;

    private final int numero;
    private final LocalDate fechaReserva;
    private final Cliente titular;
    private final Salida salida;
    private final Vendedor vendedor;
    private EstadoReserva estado = EstadoReserva.PENDIENTE;
    private final List<Pasajero> pasajeros = new ArrayList<>();
    private final List<Pago> pagos = new ArrayList<>();
    private final List<NotaAjuste> ajustes = new ArrayList<>();
    private Cancelacion cancelacion;
    private double totalTarifas;
    private double porcentajePromocion;
    private String promocionAplicada = "";

    /** Constructor de paquete: las reservas solo las crea Salida.crearReserva() (Creador). */
    Reserva(int numero, LocalDate fechaReserva, Cliente titular, Salida salida, Vendedor vendedor) {
        if (titular == null || vendedor == null) {
            throw new IllegalArgumentException("La reserva necesita un titular y un vendedor");
        }
        this.numero = numero;
        this.fechaReserva = fechaReserva;
        this.titular = titular;
        this.salida = salida;
        this.vendedor = vendedor;
    }

    /** Agrega el pasajero y le carga los servicios que el tour declara obligatorios. */
    void agregarPasajero(Pasajero pasajero) {
        pasajeros.add(pasajero);
        for (ServicioAdicional obligatorio : salida.getTour().getServiciosObligatorios()) {
            pasajero.agregarServicio(obligatorio);
        }
    }

    /**
     * Calcula la parte tarifaria: precio base de la temporada por el factor de cada pasajero,
     * y aplica la mejor promoción disponible (no se acumulan).
     */
    void calcularTotal(List<Promocion> promociones) throws ReglaNegocioExcepcion {
        double precioBase = salida.getTour().obtenerPrecioBase(salida.getFechaInicio(), fechaReserva);
        totalTarifas = 0;
        for (Pasajero pasajero : pasajeros) {
            totalTarifas += precioBase * pasajero.calcularFactorTarifa(salida.getFechaInicio());
        }
        Promocion mejor = null;
        for (Promocion promocion : promociones) {
            if (promocion.aplicaA(this)
                    && (mejor == null || promocion.getPorcentajeDescuento() > mejor.getPorcentajeDescuento())) {
                mejor = promocion;
            }
        }
        porcentajePromocion = mejor == null ? 0 : mejor.getPorcentajeDescuento();
        promocionAplicada = mejor == null ? "" : mejor.getNombre();
    }

    /** Contrata un servicio adicional para un pasajero de la reserva. */
    public void contratarServicio(Pasajero pasajero, ServicioAdicional servicio) throws ReglaNegocioExcepcion {
        verificarModificable();
        if (!pasajeros.contains(pasajero)) {
            throw new ReglaNegocioExcepcion("El pasajero no pertenece a la reserva " + numero);
        }
        if (!salida.getTour().ofreceServicio(servicio)) {
            throw new ReglaNegocioExcepcion("El tour no ofrece el servicio " + servicio.getNombre());
        }
        if (pasajero.getServicios().contains(servicio)) {
            throw new ReglaNegocioExcepcion(pasajero.getNombreCompleto() + " ya tiene contratado " + servicio.getNombre());
        }
        pasajero.agregarServicio(servicio);
    }

    /**
     * Registra un pago (Creador de Pago). Con el 30 % pagado la reserva queda confirmada.
     * Desde la fecha límite (10 días antes) solo se acepta el pago del saldo completo.
     */
    public Pago registrarPago(LocalDate fecha, double importe, MedioPago medio, int cuotas) throws ReglaNegocioExcepcion {
        verificarModificable();
        if (importe <= 0) {
            throw new ReglaNegocioExcepcion("El importe debe ser mayor a cero");
        }
        double saldo = getSaldo();
        if (importe > saldo + TOLERANCIA) {
            throw new ReglaNegocioExcepcion("El importe supera el saldo pendiente ($" + redondear(saldo) + ")");
        }
        if (!fecha.isBefore(getFechaLimiteSaldo()) && importe < saldo - TOLERANCIA) {
            throw new ReglaNegocioExcepcion("Desde el " + getFechaLimiteSaldo()
                    + " solo se acepta el pago del saldo completo ($" + redondear(saldo) + ")");
        }
        if (cuotas < 1 || (cuotas > 1 && medio != MedioPago.TARJETA)) {
            throw new ReglaNegocioExcepcion("Solo con tarjeta se puede pagar en más de una cuota");
        }
        Pago pago = new Pago(fecha, importe, medio, cuotas);
        pagos.add(pago);
        if (estado == EstadoReserva.PENDIENTE && getTotalPagado() + TOLERANCIA >= getMontoSena()) {
            estado = EstadoReserva.CONFIRMADA;
            salida.actualizarEstado();
        }
        return pago;
    }

    /**
     * Cancela la reserva aplicando la política recibida (Polimorfismo / OCP).
     * La devolución se hace por el mismo medio de cada pago, en la proporción que indica la política.
     */
    public Cancelacion cancelar(LocalDate fecha, String motivo, PoliticaCancelacion politica) throws EstadoInvalidoExcepcion {
        if (estado == EstadoReserva.ANULADA || estado == EstadoReserva.FINALIZADA) {
            throw new EstadoInvalidoExcepcion("La reserva " + numero + " ya está " + estado);
        }
        if (salida.getEstado() == EstadoSalida.EN_CURSO || salida.getEstado() == EstadoSalida.FINALIZADA) {
            throw new EstadoInvalidoExcepcion("La salida ya comenzó: la reserva " + numero + " no se puede cancelar");
        }
        long diasDeAnticipacion = ChronoUnit.DAYS.between(fecha, salida.getFechaInicio());
        double porcentaje = politica.calcularPorcentajeDevolucion(diasDeAnticipacion);
        Map<MedioPago, Double> devoluciones = new EnumMap<>(MedioPago.class);
        for (Pago pago : pagos) {
            devoluciones.merge(pago.getMedio(), pago.getImporte() * porcentaje, Double::sum);
        }
        cancelacion = new Cancelacion(fecha, motivo, politica.getDescripcion(), porcentaje, devoluciones);
        estado = EstadoReserva.ANULADA;
        salida.actualizarEstado();
        return cancelacion;
    }

    /** True si pasó la fecha límite y todavía hay saldo (la reserva se anula y libera el cupo). */
    public boolean tieneSaldoVencido(LocalDate hoy) {
        boolean activa = estado == EstadoReserva.PENDIENTE || estado == EstadoReserva.CONFIRMADA;
        return activa && hoy.isAfter(getFechaLimiteSaldo()) && getSaldo() > TOLERANCIA;
    }

    public Cancelacion anularPorFaltaDePago(LocalDate hoy) throws EstadoInvalidoExcepcion {
        return cancelar(hoy, "Saldo impago al vencimiento (" + getFechaLimiteSaldo() + ")",
                new PoliticaCancelacionFaltaDePago());
    }

    /** Un pasajero puede reemplazarse sin cargo hasta 3 días antes, si cumple las condiciones del tour. */
    public void reemplazarPasajero(Pasajero actual, Pasajero nuevo, LocalDate fecha) throws ReglaNegocioExcepcion {
        verificarModificable();
        long diasDeAnticipacion = ChronoUnit.DAYS.between(fecha, salida.getFechaInicio());
        if (diasDeAnticipacion < DIAS_LIMITE_REEMPLAZO) {
            throw new ReglaNegocioExcepcion("Los reemplazos se aceptan hasta " + DIAS_LIMITE_REEMPLAZO
                    + " días antes de la salida (faltan " + diasDeAnticipacion + ")");
        }
        int indice = pasajeros.indexOf(actual);
        if (indice < 0) {
            throw new ReglaNegocioExcepcion("El pasajero no pertenece a la reserva " + numero);
        }
        salida.getTour().validarPasajero(nuevo, salida.getFechaInicio());
        for (ServicioAdicional servicio : actual.getServicios()) {
            nuevo.agregarServicio(servicio);
        }
        pasajeros.set(indice, nuevo);
    }

    /** Las correcciones de importe se registran como notas de ajuste (nunca se borra nada). */
    public NotaAjuste registrarAjuste(LocalDate fecha, String motivo, double importe) throws ReglaNegocioExcepcion {
        if (motivo == null || motivo.isBlank()) {
            throw new ReglaNegocioExcepcion("La nota de ajuste necesita un motivo");
        }
        if (importe == 0) {
            throw new ReglaNegocioExcepcion("El ajuste no puede ser de $0");
        }
        if (getTotal() + importe < -TOLERANCIA) {
            throw new ReglaNegocioExcepcion("El ajuste dejaría el total de la reserva en negativo");
        }
        NotaAjuste nota = new NotaAjuste(fecha, motivo, importe);
        ajustes.add(nota);
        return nota;
    }

    void finalizar() {
        if (estado == EstadoReserva.CONFIRMADA) {
            estado = EstadoReserva.FINALIZADA;
        }
    }

    private void verificarModificable() throws EstadoInvalidoExcepcion {
        if (estado == EstadoReserva.ANULADA || estado == EstadoReserva.FINALIZADA) {
            throw new EstadoInvalidoExcepcion("La reserva " + numero + " está " + estado + " y no se puede modificar");
        }
        EstadoSalida estadoSalida = salida.getEstado();
        if (estadoSalida == EstadoSalida.EN_CURSO || estadoSalida == EstadoSalida.FINALIZADA
                || estadoSalida == EstadoSalida.CANCELADA) {
            throw new EstadoInvalidoExcepcion("La salida " + salida.getCodigo() + " está " + estadoSalida);
        }
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100) / 100.0;
    }

    // ---------------------------------------------------------------- importes

    public double getDescuentoPromocion() {
        return totalTarifas * porcentajePromocion;
    }

    public double getTotalAdicionales() {
        double total = 0;
        for (Pasajero pasajero : pasajeros) {
            total += pasajero.getTotalServicios();
        }
        return total;
    }

    public double getTotalAjustes() {
        double total = 0;
        for (NotaAjuste ajuste : ajustes) {
            total += ajuste.getImporte();
        }
        return total;
    }

    /** Total = tarifas - promoción + adicionales + ajustes. */
    public double getTotal() {
        return totalTarifas - getDescuentoPromocion() + getTotalAdicionales() + getTotalAjustes();
    }

    public double getTotalPagado() {
        double total = 0;
        for (Pago pago : pagos) {
            total += pago.getImporte();
        }
        return total;
    }

    public double getSaldo() {
        return getTotal() - getTotalPagado();
    }

    public double getMontoSena() {
        return getTotal() * PORCENTAJE_SENA;
    }

    public LocalDate getFechaLimiteSaldo() {
        return salida.getFechaInicio().minusDays(DIAS_LIMITE_SALDO);
    }

    // ---------------------------------------------------------------- getters

    public int getNumero() {
        return numero;
    }

    public LocalDate getFechaReserva() {
        return fechaReserva;
    }

    public Cliente getTitular() {
        return titular;
    }

    public Salida getSalida() {
        return salida;
    }

    public Vendedor getVendedor() {
        return vendedor;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public int getCantidadPasajeros() {
        return pasajeros.size();
    }

    public List<Pasajero> getPasajeros() {
        return Collections.unmodifiableList(pasajeros);
    }

    public List<Pago> getPagos() {
        return Collections.unmodifiableList(pagos);
    }

    public List<NotaAjuste> getAjustes() {
        return Collections.unmodifiableList(ajustes);
    }

    public Cancelacion getCancelacion() {
        return cancelacion;
    }

    public double getTotalTarifas() {
        return totalTarifas;
    }

    public double getPorcentajePromocion() {
        return porcentajePromocion;
    }

    public String getPromocionAplicada() {
        return promocionAplicada;
    }

    @Override
    public String toString() {
        return "Reserva " + numero + " - " + titular.getNombreCompleto() + " - " + salida.getCodigo();
    }
}