package turismo.clases;

import turismo.interfaces.PoliticaCancelacion;

/**
 * Anulación porque no se pagó el saldo a tiempo.
 * Supuesto documentado: lo pagado (la seña) no se devuelve.
 */
public class PoliticaCancelacionFaltaDePago implements PoliticaCancelacion {
    private static final long serialVersionUID = 1L;

    @Override
    public double calcularPorcentajeDevolucion(long diasDeAnticipacion) {
        return 0.0;
    }

    @Override
    public String getDescripcion() {
        return "Anulación por falta de pago";
    }
}