package turismo.clases;

import java.io.Serializable;
import java.time.LocalDate;

/** Precio base por pasajero de un tour para una temporada, vigente desde una fecha. */
public class Tarifa implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Temporada temporada;
    private final LocalDate vigenteDesde;
    private final double precioBase;

    public Tarifa(Temporada temporada, LocalDate vigenteDesde, double precioBase) {
        if (temporada == null || vigenteDesde == null) {
            throw new IllegalArgumentException("La temporada y la fecha de vigencia son obligatorias");
        }
        if (precioBase <= 0) {
            throw new IllegalArgumentException("El precio base debe ser positivo");
        }
        this.temporada = temporada;
        this.vigenteDesde = vigenteDesde;
        this.precioBase = precioBase;
    }

    public Temporada getTemporada() {
        return temporada;
    }

    public LocalDate getVigenteDesde() {
        return vigenteDesde;
    }

    public double getPrecioBase() {
        return precioBase;
    }

    @Override
    public String toString() {
        return temporada + " desde " + vigenteDesde + ": $" + precioBase;
    }
}
