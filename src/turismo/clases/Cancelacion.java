package turismo.clases;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** Registro de la cancelación de una reserva: fecha, motivo e importe devuelto por cada medio de pago. */
public class Cancelacion implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LocalDate fecha;
    private final String motivo;
    private final String politica;
    private final double porcentajeDevolucion;
    private final Map<MedioPago, Double> devolucionesPorMedio;

    public Cancelacion(LocalDate fecha, String motivo, String politica, double porcentajeDevolucion,
                       Map<MedioPago, Double> devolucionesPorMedio) {
        this.fecha = fecha;
        this.motivo = motivo == null || motivo.isBlank() ? "Sin motivo informado" : motivo.trim();
        this.politica = politica;
        this.porcentajeDevolucion = porcentajeDevolucion;
        this.devolucionesPorMedio = new EnumMap<>(MedioPago.class);
        this.devolucionesPorMedio.putAll(devolucionesPorMedio);
    }

    public double getImporteDevuelto() {
        double total = 0;
        for (double importe : devolucionesPorMedio.values()) {
            total += importe;
        }
        return total;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getPolitica() {
        return politica;
    }

    public double getPorcentajeDevolucion() {
        return porcentajeDevolucion;
    }

    public Map<MedioPago, Double> getDevolucionesPorMedio() {
        return Collections.unmodifiableMap(devolucionesPorMedio);
    }
}