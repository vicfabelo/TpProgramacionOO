package turismo.clases;

import java.io.Serializable;

/**
 * Cliente titular que realiza la reserva en la agencia.
 */
public class Cliente implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String idCliente;
    private final String nombre;
    private final String apellido;
    private final String documento;
    private final String telefono;
    private final String email;
    private final String paisResidencia;

    public Cliente(String idCliente, String nombre, String apellido, String documento,
                   String telefono, String email, String paisResidencia) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException("El apellido es obligatorio");
        }
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento es obligatorio");
        }

        this.idCliente = idCliente == null ? "" : idCliente.trim();
        this.nombre = nombre.trim();
        this.apellido = apellido.trim();
        this.documento = documento.trim();
        this.telefono = telefono == null ? "" : telefono.trim();
        this.email = email == null ? "" : email.trim();
        this.paisResidencia = (paisResidencia == null || paisResidencia.isBlank()) ? "Argentina" : paisResidencia.trim();
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public String getIdCliente() { return idCliente; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getDocumento() { return documento; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public String getPaisResidencia() { return paisResidencia; }

    @Override
    public String toString() {
        return getNombreCompleto() + " (Doc: " + documento + ") - " + paisResidencia;
    }
}