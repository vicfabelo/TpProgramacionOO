package turismo.clases;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Pago de una reserva. El importe es lo que se imputa a la deuda; si se paga con tarjeta
 * en cuotas se cobra además un recargo del 7 % que no reduce el saldo.
 */
public class Pago implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final double RECARGO_TARJETA_EN_CUOTAS = 0.07;

    private final LocalDate fecha;
    private final double importe;
    private final MedioPago medio;
    private final int cuotas;

    public Pago(LocalDate fecha, double importe, MedioPago medio, int cuotas) {
        if (medio == null) {
            throw new IllegalArgumentException("El medio de pago es obligatorio");
        }
        if (cuotas < 1) {
            throw new IllegalArgumentException("Las cuotas deben ser al menos 1");
        }
        if (cuotas > 1 && medio != MedioPago.TARJETA) {
            throw new IllegalArgumentException("Solo con tarjeta se puede pagar en cuotas");
        }
        this.fecha = fecha;
        this.importe = importe;
        this.medio = medio;
        this.cuotas = cuotas;
    }

    public double getRecargo() {
        return medio == MedioPago.TARJETA && cuotas > 1 ? importe * RECARGO_TARJETA_EN_CUOTAS : 0;
    }

    /** Lo que efectivamente paga el cliente (importe + recargo). */
    public double getImporteCobrado() {
        return importe + getRecargo();
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public double getImporte() {
        return importe;
    }

    public MedioPago getMedio() {
        return medio;
    }

    public int getCuotas() {
        return cuotas;
    }

    @Override
    public String toString() {
        String texto = fecha + " - $" + importe + " - " + medio;
        return cuotas > 1 ? texto + " en " + cuotas + " cuotas (recargo $" + getRecargo() + ")" : texto;
    }
}