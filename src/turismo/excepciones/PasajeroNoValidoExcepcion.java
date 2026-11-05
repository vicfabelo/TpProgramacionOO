package turismo.excepciones;

/** Se lanza cuando un pasajero no cumple las condiciones del tour (ej: edad mínima en aventura). */
public class PasajeroNoValidoExcepcion extends ReglaNegocioExcepcion {
    private static final long serialVersionUID = 1L;

    public PasajeroNoValidoExcepcion(String mensaje) {
        super(mensaje);
    }
}