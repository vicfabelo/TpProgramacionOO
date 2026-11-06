package turismo.interfaces;

import java.io.Serializable;

/**
 * Regla que define cuánto se devuelve al cancelar una reserva.
 * OCP + Polimorfismo: hay una política para el cliente, otra para la agencia y otra
 * para la anulación por falta de pago; Reserva.cancelar() funciona igual con cualquiera.
 */
public interface PoliticaCancelacion extends Serializable {

    /**
     * @param diasDeAnticipacion días que faltan para la salida al momento de cancelar
     * @return fracción a devolver (0.9 = 90 %)
     */
    double calcularPorcentajeDevolucion(long diasDeAnticipacion);

    String getDescripcion();
}