package turismo.clases;

import java.time.temporal.ChronoUnit;

import turismo.interfaces.Promocion;

/** 15 % de descuento si se reserva con más de 60 días de anticipación. */
public class PromocionReservaAnticipada implements Promocion {
    private static final long serialVersionUID = 1L;

    public static final int DIAS_MINIMOS = 60;

    @Override
    public String getNombre() {
        return "Reserva anticipada";
    }

    @Override
    public boolean aplicaA(Reserva reserva) {
        long dias = ChronoUnit.DAYS.between(reserva.getFechaReserva(), reserva.getSalida().getFechaInicio());
        return dias > DIAS_MINIMOS;
    }

    @Override
    public double getPorcentajeDescuento() {
        return 0.15;
    }
}