package turismo.clases;

/** Superclase abstracta del personal de la agencia. */
public abstract class Empleado extends Persona {
    private static final long serialVersionUID = 1L;

    protected final String legajo;

    protected Empleado(String legajo, String nombre, String apellido, String documento, String telefono) {
        super(nombre, apellido, documento, telefono);
        if (esVacio(legajo)) {
            throw new IllegalArgumentException("El legajo es obligatorio");
        }
        this.legajo = legajo.trim();
    }

    /** Cada tipo de empleado informa su rol (método abstracto que sobrescriben las subclases). */
    public abstract String getRol();

    @Override
    public String getDescripcion() {
        return getRol() + " - legajo " + legajo + " - " + getNombreCompleto();
    }

    public String getLegajo() {
        return legajo;
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " [" + getRol() + "]";
    }
}