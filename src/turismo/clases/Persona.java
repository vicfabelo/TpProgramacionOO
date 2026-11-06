package turismo.clases;

import java.io.Serializable;

/**
 * Superclase abstracta de todas las personas del sistema.
 * Los atributos son protected para que las subclases (Cliente, Pasajero, Empleado)
 * los usen directamente en sus métodos sobrescritos.
 */
public abstract class Persona implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String nombre;
    protected String apellido;
    protected String documento;
    protected String telefono;

    protected Persona(String nombre, String apellido, String documento, String telefono) {
        if (esVacio(nombre) || esVacio(apellido)) {
            throw new IllegalArgumentException("El nombre y el apellido son obligatorios");
        }
        if (esVacio(documento)) {
            throw new IllegalArgumentException("El documento es obligatorio");
        }
        this.nombre = nombre.trim();
        this.apellido = apellido.trim();
        this.documento = documento.trim();
        this.telefono = telefono == null ? "" : telefono.trim();
    }

    protected static boolean esVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    /** Cada subclase se describe según su rol (se sobrescribe en todas las subclases). */
    public abstract String getDescripcion();

    public String getNombreCompleto() {
        return apellido + ", " + nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getDocumento() {
        return documento;
    }

    public String getTelefono() {
        return telefono;
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " (" + documento + ")";
    }
}