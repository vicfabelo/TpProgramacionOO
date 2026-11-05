package turismo.clases;

/**
 * Categorías de pasajero para cálculo de tarifas y descuentos.
 */
public enum TipoPasajero {
    ADULTO(1.0),
    MENOR_12(0.60),
    MENOR_3(0.0),
    JUBILADO(0.90);

    private final double factorTarifa;

    TipoPasajero(double factorTarifa) {
        this.factorTarifa = factorTarifa;
    }

    public double getFactorTarifa() {
        return factorTarifa;
    }
}