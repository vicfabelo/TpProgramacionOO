package turismo.clases;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Persona que viaja dentro de una reserva.
 * Experto en información: conoce su edad y su tipo, por eso calcula qué parte de la tarifa paga.
 */
public class Pasajero extends Persona {
    private static final long serialVersionUID = 1L;

    /** Orden personalizado para la lista de pasajeros (Comparator). */
    public static final Comparator<Pasajero> POR_APELLIDO_Y_NOMBRE =
            Comparator.comparing(Pasajero::getApellido, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Pasajero::getNombre, String.CASE_INSENSITIVE_ORDER);

    private final LocalDate fechaNacimiento;
    private final TipoPasajero tipo;
    private final String contactoEmergencia;
    private final String restricciones;
    private final List<ServicioAdicional> servicios = new ArrayList<>();

    public Pasajero(String nombre, String apellido, String documento, LocalDate fechaNacimiento,
                    TipoPasajero tipo, String contactoEmergencia, String restricciones) {
        super(nombre, apellido, documento, contactoEmergencia);
        if (fechaNacimiento == null || fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no es válida");
        }
        if (esVacio(contactoEmergencia)) {
            throw new IllegalArgumentException("El contacto de emergencia es obligatorio");
        }
        this.fechaNacimiento = fechaNacimiento;
        this.tipo = tipo == null ? TipoPasajero.ADULTO : tipo;
        this.contactoEmergencia = contactoEmergencia.trim();
        this.restricciones = restricciones == null ? "" : restricciones.trim();
    }

    public int calcularEdad(LocalDate fecha) {
        return Period.between(fechaNacimiento, fecha).getYears();
    }

    /**
     * Fracción de la tarifa base que paga el pasajero.
     * Menor de 3 años: 0 %. Menor de 12: 60 %. Jubilado: 90 % (10 % de descuento). Resto: 100 %.
     * Supuesto: la edad se calcula a la fecha de inicio de la salida.
     */
    public double calcularFactorTarifa(LocalDate fechaSalida) {
        int edad = calcularEdad(fechaSalida);
        if (edad < 3) {
            return 0.0;
        }
        if (edad < 12) {
            return 0.60;
        }
        if (tipo == TipoPasajero.JUBILADO) {
            return 0.90;
        }
        return 1.0;
    }

    /** Solo la reserva agrega servicios (por eso es de paquete). */
    void agregarServicio(ServicioAdicional servicio) {
        if (!servicios.contains(servicio)) {
            servicios.add(servicio);
        }
    }

    public double getTotalServicios() {
        double total = 0;
        for (ServicioAdicional servicio : servicios) {
            total += servicio.getPrecio();
        }
        return total;
    }

    @Override
    public String getDescripcion() {
        String texto = "Pasajero " + getNombreCompleto() + " - " + tipo + " - emergencia: " + contactoEmergencia;
        return restricciones.isEmpty() ? texto : texto + " - restricciones: " + restricciones;
    }

    public List<ServicioAdicional> getServicios() {
        return Collections.unmodifiableList(servicios);
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public TipoPasajero getTipo() {
        return tipo;
    }

    public String getContactoEmergencia() {
        return contactoEmergencia;
    }

    public String getRestricciones() {
        return restricciones;
    }
}