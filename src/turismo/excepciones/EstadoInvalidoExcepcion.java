package turismo.excepciones;

/** Se lanza cuando se intenta una operación que el estado actual no permite (ej: reservar en una salida cancelada). */
public class EstadoInvalidoExcepcion extends ReglaNegocioExcepcion {
    private static final long serialVersionUID = 1L;

    public EstadoInvalidoExcepcion(String mensaje) {
        super(mensaje);
    }
}