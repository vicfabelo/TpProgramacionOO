package turismo.interfaces;

import java.io.Serializable;

import turismo.clases.Reserva;

/**
 * Promoción aplicable sobre la tarifa de una reserva.
 * OCP: para sumar una promoción nueva se crea otra clase que implemente esta interfaz,
 * sin tocar Reserva. Polimorfismo (GRASP): Reserva elige la mejor sin preguntar de qué tipo es.
 */
public interface Promocion extends Serializable {

    String getNombre();

    /** Indica si la promoción se puede aplicar a la reserva. */
    boolean aplicaA(Reserva reserva);

    /** Porcentaje de descuento expresado como fracción (0.15 = 15 %). */
    double getPorcentajeDescuento();
}