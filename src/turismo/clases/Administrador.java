package turismo.clases;

/** Administrador: mantiene el catálogo, liquida a los guías y emite reportes. */
public class Administrador extends Empleado {
    private static final long serialVersionUID = 1L;

    public Administrador(String legajo, String nombre, String apellido, String documento, String telefono) {
        super(legajo, nombre, apellido, documento, telefono);
    }

    @Override
    public String getRol() {
        return "Administrador";
    }
}