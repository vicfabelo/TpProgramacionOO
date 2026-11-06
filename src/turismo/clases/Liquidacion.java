package turismo.clases;

import java.io.Serializable;
import java.time.LocalDate;

/** Honorarios del guía por una salida finalizada: monto fijo por día + 5 % de los adicionales vendidos. */
public class Liquidacion implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final double PORCENTAJE_ADICIONALES = 0.05;

    private final Guia guia;
    private final Salida salida;
    private final int diasTrabajados;
    private final double honorarioPorDia;
    private final double ventasAdicionales;
    private final LocalDate fecha;

    public Liquidacion(Guia guia, Salida salida, int diasTrabajados, double honorarioPorDia,
                       double ventasAdicionales, LocalDate fecha) {
        this.guia = guia;
        this.salida = salida;
        this.diasTrabajados = diasTrabajados;
        this.honorarioPorDia = honorarioPorDia;
        this.ventasAdicionales = ventasAdicionales;
        this.fecha = fecha;
    }

    public double getMontoFijo() {
        return diasTrabajados * honorarioPorDia;
    }

    public double getComisionAdicionales() {
        return ventasAdicionales * PORCENTAJE_ADICIONALES;
    }

    public double getTotal() {
        return getMontoFijo() + getComisionAdicionales();
    }

    public Guia getGuia() {
        return guia;
    }

    public Salida getSalida() {
        return salida;
    }

    public int getDiasTrabajados() {
        return diasTrabajados;
    }

    public double getHonorarioPorDia() {
        return honorarioPorDia;
    }

    public double getVentasAdicionales() {
        return ventasAdicionales;
    }

    public LocalDate getFecha() {
        return fecha;
    }
}