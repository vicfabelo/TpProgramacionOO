package turismo.clases;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import turismo.excepciones.PasajeroNoValidoExcepcion;
import turismo.excepciones.ReglaNegocioExcepcion;

/**
 * Tour del catálogo. Es abstracta: cada categoría (aventura, cultural, naturaleza)
 * sobrescribe lo que la distingue. Polimorfismo (GRASP): Salida y Reserva le piden
 * al tour que valide pasajeros o diga qué servicios son obligatorios, sin preguntar su tipo.
 */
public abstract class Tour implements Serializable {
    private static final long serialVersionUID = 1L;

    protected final String codigo;
    protected String nombre;
    protected String descripcion;
    protected int duracionDias;
    protected int cupoMinimo;
    protected int cupoMaximo;

    private final List<Actividad> itinerario = new ArrayList<>();
    private final List<Tarifa> tarifas = new ArrayList<>();
    private final List<ServicioAdicional> serviciosDisponibles = new ArrayList<>();

    protected Tour(String codigo, String nombre, String descripcion, int duracionDias,
                   int cupoMinimo, int cupoMaximo) {
        if (codigo == null || codigo.isBlank() || nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El código y el nombre del tour son obligatorios");
        }
        if (duracionDias < 1) {
            throw new IllegalArgumentException("El tour debe durar al menos un día");
        }
        if (cupoMinimo < 1 || cupoMaximo < cupoMinimo) {
            throw new IllegalArgumentException("El cupo mínimo debe ser al menos 1 y no superar al máximo");
        }
        this.codigo = codigo.trim().toUpperCase();
        this.nombre = nombre.trim();
        this.descripcion = descripcion == null ? "" : descripcion.trim();
        this.duracionDias = duracionDias;
        this.cupoMinimo = cupoMinimo;
        this.cupoMaximo = cupoMaximo;
    }

    /** Categoría del tour: la define cada subclase. */
    public abstract CategoriaTour getCategoria();

    /** Texto con los datos propios de la categoría. Las subclases lo sobrescriben. */
    public String getDetalleCategoria() {
        return getCategoria().toString();
    }

    /** Por defecto un tour no impone condiciones al pasajero. TourAventura lo sobrescribe. */
    public void validarPasajero(Pasajero pasajero, LocalDate fechaSalida) throws PasajeroNoValidoExcepcion {
        // sin restricciones
    }

    /** Indica si un servicio es obligatorio en este tour. TourAventura lo sobrescribe para el seguro. */
    public boolean esObligatorio(ServicioAdicional servicio) {
        return servicio.isObligatorio();
    }

    /** Controla que el catálogo tenga lo necesario para poder programar salidas. */
    public void validarServiciosObligatorios() throws ReglaNegocioExcepcion {
        if (tarifas.isEmpty()) {
            throw new ReglaNegocioExcepcion("El tour " + nombre + " no tiene tarifas cargadas");
        }
    }

    public void agregarActividad(Actividad actividad) throws ReglaNegocioExcepcion {
        if (actividad.getDia() > duracionDias) {
            throw new ReglaNegocioExcepcion("La actividad es del día " + actividad.getDia()
                    + " pero el tour dura " + duracionDias + " días");
        }
        for (Actividad existente : itinerario) {
            if (existente.compareTo(actividad) == 0) {
                throw new ReglaNegocioExcepcion("Ya existe una actividad en el día " + actividad.getDia()
                        + " con orden " + actividad.getOrden());
            }
        }
        itinerario.add(actividad);
        Collections.sort(itinerario); // usa el orden natural de Actividad (Comparable)
    }

    public void agregarTarifa(Tarifa tarifa) {
        tarifas.add(tarifa);
    }

    public void agregarServicio(ServicioAdicional servicio) {
        serviciosDisponibles.add(servicio);
    }

    /**
     * Precio base por pasajero. La temporada sale de la fecha de la salida y se usa la tarifa
     * de esa temporada con vigencia más reciente a la fecha de referencia (la fecha de reserva).
     */
    public double obtenerPrecioBase(LocalDate fechaSalida, LocalDate fechaReferencia) throws ReglaNegocioExcepcion {
        Temporada temporada = Temporada.desde(fechaSalida);
        Tarifa vigente = null;
        for (Tarifa tarifa : tarifas) {
            boolean esDeLaTemporada = tarifa.getTemporada() == temporada;
            boolean yaVigente = !tarifa.getVigenteDesde().isAfter(fechaReferencia);
            if (esDeLaTemporada && yaVigente
                    && (vigente == null || tarifa.getVigenteDesde().isAfter(vigente.getVigenteDesde()))) {
                vigente = tarifa;
            }
        }
        if (vigente == null) {
            throw new ReglaNegocioExcepcion("El tour " + nombre + " no tiene tarifa vigente para temporada " + temporada);
        }
        return vigente.getPrecioBase();
    }

    public List<ServicioAdicional> getServiciosObligatorios() {
        List<ServicioAdicional> obligatorios = new ArrayList<>();
        for (ServicioAdicional servicio : serviciosDisponibles) {
            if (esObligatorio(servicio)) {
                obligatorios.add(servicio);
            }
        }
        return obligatorios;
    }

    public boolean ofreceServicio(ServicioAdicional servicio) {
        return serviciosDisponibles.contains(servicio);
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getDuracionDias() {
        return duracionDias;
    }

    public int getCupoMinimo() {
        return cupoMinimo;
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public List<Actividad> getItinerario() {
        return Collections.unmodifiableList(itinerario);
    }

    public List<Tarifa> getTarifas() {
        return Collections.unmodifiableList(tarifas);
    }

    public List<ServicioAdicional> getServiciosDisponibles() {
        return Collections.unmodifiableList(serviciosDisponibles);
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}