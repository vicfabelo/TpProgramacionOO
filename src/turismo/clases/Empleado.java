package turismo.clases;

import java.io.Serializable;

/**
 * Clase abstracta base para todo el personal de la agencia de turismo.
 */
public abstract class Empleado implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String legajo;
    private final String nombre;
    private final String apellido;
    private final String documento;
    private final String telefono;
    private final String email;

    public Empleado(String legajo, String nombre, String apellido, String documento,
                    String telefono, String email) {
        if (legajo == null || legajo.isBlank()) {
            throw new IllegalArgumentException("El legajo es obligatorio");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException("El apellido es obligatorio");
        }
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento es obligatorio");
        }

        this.legajo = legajo.trim();
        this.nombre = nombre.trim();
        this.apellido = apellido.trim();
        this.documento = documento.trim();
        this.telefono = telefono == null ? "" : telefono.trim();
        this.email = email == null ? "" : email.trim();
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public String getLegajo() { return legajo; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getDocumento() { return documento; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }

    @Override
    public String toString() {
        return getNombreCompleto() + " [Legajo: " + legajo + "]";
    }
}