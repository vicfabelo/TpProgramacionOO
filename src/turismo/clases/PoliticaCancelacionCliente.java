package turismo.clases;

import turismo.interfaces.PoliticaCancelacion;

/** Cancelación pedida por el cliente: 90 % con 30 días o más, 50 % de 29 a 15, 20 % de 14 a 7, nada con menos de 7. */
public class PoliticaCancelacionCliente implements PoliticaCancelacion {
    private static final long serialVersionUID = 1L;

    @Override
    public double calcularPorcentajeDevolucion(long diasDeAnticipacion) {
        if (diasDeAnticipacion >= 30) {
            return 0.90;
        }
        if (diasDeAnticipacion >= 15) {
            return 0.50;
        }
        if (diasDeAnticipacion >= 7) {
            return 0.20;
        }
        return 0.0;
    }

    @Override
    public String getDescripcion() {
        return "Cancelación del cliente";
    }
}