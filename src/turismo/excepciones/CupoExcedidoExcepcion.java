package turismo.excepciones;

/** Se lanza cuando una reserva pide más lugares de los que quedan en la salida. */
public class CupoExcedidoExcepcion extends ReglaNegocioExcepcion {
    private static final long serialVersionUID = 1L;

    public CupoExcedidoExcepcion(String mensaje) {
        super(mensaje);
    }
}