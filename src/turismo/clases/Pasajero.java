package turismo.clases;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;

/**
 * Pasajero individual asignado a una reserva.
 */
public class Pasajero implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String nombre;
    private final String documento;
    private final LocalDate fechaNacimiento;
    private final TipoPasajero tipo;
    private final String contactoEmergencia;
    private final String restriccionesSaludAlimentarias;

    public Pasajero(String nombre, String documento, LocalDate fechaNacimiento,
                    TipoPasajero tipo, String contactoEmergencia, String restriccionesSaludAlimentarias) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del pasajero es obligatorio");
        }
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento es obligatorio");
        }
        if (fechaNacimiento == null || fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser nula ni futura");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de pasajero es obligatorio");
        }

        this.nombre = nombre.trim();
        this.documento = documento.trim();
        this.fechaNacimiento = fechaNacimiento;
        this.tipo = tipo;
        this.contactoEmergencia = contactoEmergencia == null ? "" : contactoEmergencia.trim();
        this.restriccionesSaludAlimentarias = restriccionesSaludAlimentarias == null ? "" : restriccionesSaludAlimentarias.trim();
    }

    public int calcularEdad() {
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    public boolean tieneRestricciones() {
        return !restriccionesSaludAlimentarias.isBlank();
    }

    public String getNombre() { return nombre; }
    public String getDocumento() { return documento; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public TipoPasajero getTipo() { return tipo; }
    public String getContactoEmergencia() { return contactoEmergencia; }
    public String getRestriccionesSaludAlimentarias() { return restriccionesSaludAlimentarias; }

    @Override
    public String toString() {
        return nombre + " (" + tipo + ", Doc: " + documento + ")";
    }
}
