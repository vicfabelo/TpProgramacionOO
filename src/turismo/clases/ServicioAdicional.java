package turismo.clases;

import java.io.Serializable;

/** Servicio que se contrata por pasajero (excursión opcional, traslado, seguro de viaje). */
public class ServicioAdicional implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String nombre;
    private final TipoServicio tipo;
    private final double precio;
    private final boolean obligatorio;

    public ServicioAdicional(String nombre, TipoServicio tipo, double precio, boolean obligatorio) {
        if (nombre == null || nombre.isBlank() || tipo == null) {
            throw new IllegalArgumentException("El nombre y el tipo del servicio son obligatorios");
        }
        if (precio < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        this.nombre = nombre.trim();
        this.tipo = tipo;
        this.precio = precio;
        this.obligatorio = obligatorio;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoServicio getTipo() {
        return tipo;
    }

    public double getPrecio() {
        return precio;
    }

    public boolean isObligatorio() {
        return obligatorio;
    }

    @Override
    public String toString() {
        return nombre + " ($" + precio + (obligatorio ? ", obligatorio" : "") + ")";
    }
}