package turismo.clases;

import turismo.interfaces.PoliticaCancelacion;

/** Cancelación provocada por la agencia: siempre se devuelve el 100 %. */
public class PoliticaCancelacionAgencia implements PoliticaCancelacion {
    private static final long serialVersionUID = 1L;

    @Override
    public double calcularPorcentajeDevolucion(long diasDeAnticipacion) {
        return 1.0;
    }

    @Override
    public String getDescripcion() {
        return "Cancelación de la agencia";
    }
}