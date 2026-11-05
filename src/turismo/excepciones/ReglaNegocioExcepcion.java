package turismo.excepciones;

/**
 * Excepción base para cualquier regla de negocio que no se cumple.
 * Es chequeada (extends Exception) para que el compilador obligue a tratarla
 * con try/catch o a declararla con throws.
 */

public class ReglaNegocioExcepcion extends Exception {
    private static final long serialVersionUID = 1L;

    public ReglaNegocioExcepcion(String mensaje) {
        super(mensaje);
    }
}