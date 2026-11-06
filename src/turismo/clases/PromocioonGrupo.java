package turismo.clases;

import turismo.interfaces.Promocion;

/** 10 % de descuento para reservas de 6 o más pasajeros. */
public class PromocionGrupo implements Promocion {
    private static final long serialVersionUID = 1L;

    public static final int PASAJEROS_MINIMOS = 6;

    @Override
    public String getNombre() {
        return "Grupo";
    }

    @Override
    public boolean aplicaA(Reserva reserva) {
        return reserva.getCantidadPasajeros() >= PASAJEROS_MINIMOS;
    }

    @Override
    public double getPorcentajeDescuento() {
        return 0.10;
    }
}