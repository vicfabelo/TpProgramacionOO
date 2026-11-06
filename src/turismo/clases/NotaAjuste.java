package turismo.clases;

import java.io.Serializable;
import java.time.LocalDate;

/** Corrección de importe de una reserva (positiva = cargo, negativa = bonificación). */
public class NotaAjuste implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LocalDate fecha;
    private final String motivo;
    private final double importe;

    public NotaAjuste(LocalDate fecha, String motivo, double importe) {
        this.fecha = fecha;
        this.motivo = motivo;
        this.importe = importe;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getMotivo() {
        return motivo;
    }

    public double getImporte() {
        return importe;
    }

    @Override
    public String toString() {
        return fecha + " - " + motivo + ": $" + importe;
    }
}