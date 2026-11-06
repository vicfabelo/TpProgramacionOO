package turismo.clases;

/** Coordinador de operaciones: asigna guía y vehículo, emite la lista y abre/cierra las salidas. */
public class Coordinador extends Empleado {
    private static final long serialVersionUID = 1L;

    public Coordinador(String legajo, String nombre, String apellido, String documento, String telefono) {
        super(legajo, nombre, apellido, documento, telefono);
    }

    @Override
    public String getRol() {
        return "Coordinador de operaciones";
    }
}