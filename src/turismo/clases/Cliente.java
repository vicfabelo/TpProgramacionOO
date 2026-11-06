package turismo.clases;

/** Titular de las reservas. */
public class Cliente extends Persona {
    private static final long serialVersionUID = 1L;

    private String email;
    private String paisResidencia;

    public Cliente(String nombre, String apellido, String documento, String telefono,
                   String email, String paisResidencia) {
        super(nombre, apellido, documento, telefono);
        this.email = email == null ? "" : email.trim();
        this.paisResidencia = esVacio(paisResidencia) ? "Argentina" : paisResidencia.trim();
    }

    @Override
    public String getDescripcion() {
        return "Cliente " + getNombreCompleto() + " - " + paisResidencia + " - " + email;
    }

    public String getEmail() {
        return email;
    }

    public String getPaisResidencia() {
        return paisResidencia;
    }
}