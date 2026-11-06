package turismo.clases;

/** Vende reservas y cobra una comisión sobre lo vendido. */
public class Vendedor extends Empleado {
    private static final long serialVersionUID = 1L;

    private final double porcentajeComision;

    public Vendedor(String legajo, String nombre, String apellido, String documento, String telefono,
                    double porcentajeComision) {
        super(legajo, nombre, apellido, documento, telefono);
        if (porcentajeComision < 0 || porcentajeComision > 1) {
            throw new IllegalArgumentException("La comisión debe estar entre 0 y 1");
        }
        this.porcentajeComision = porcentajeComision;
    }

    public double calcularComision(double totalVendido) {
        return totalVendido * porcentajeComision;
    }

    @Override
    public String getRol() {
        return "Vendedor";
    }

    @Override
    public String getDescripcion() {
        return super.getDescripcion() + " - comisión " + Math.round(porcentajeComision * 1000) / 10.0 + " %";
    }

    public double getPorcentajeComision() {
        return porcentajeComision;
    }
}