package turismo.excepciones;

/** Se lanza cuando el guía no está habilitado para la categoría o ya tiene otra salida superpuesta. */
public class GuiaNoDisponibleExcepcion extends ReglaNegocioExcepcion {
    private static final long serialVersionUID = 1L;

    public GuiaNoDisponibleExcepcion(String mensaje) {
        super(mensaje);
    }
}