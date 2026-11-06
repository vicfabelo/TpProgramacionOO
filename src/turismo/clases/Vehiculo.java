package turismo.clases;

import java.io.Serializable;

/** Vehículo de la flota que se asigna a una salida. */
public class Vehiculo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String patente;
    private final int capacidad;
    private final TipoVehiculo tipo;

    public Vehiculo(String patente, int capacidad, TipoVehiculo tipo) {
        if (patente == null || patente.isBlank()) {
            throw new IllegalArgumentException("La patente es obligatoria");
        }
        if (capacidad < 1) {
            throw new IllegalArgumentException("La capacidad debe ser al menos 1");
        }
        this.patente = patente.trim().toUpperCase();
        this.capacidad = capacidad;
        this.tipo = tipo;
    }

    public String getPatente() {
        return patente;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public TipoVehiculo getTipo() {
        return tipo;
    }

    @Override
    public String toString() {
        return tipo + " " + patente + " (" + capacidad + " lugares)";
    }
}