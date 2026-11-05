package turismo.excepciones;

/** Envuelve los errores de lectura/escritura de archivos para que la interfaz los muestre de forma clara. */
public class PersistenciaExcepcion extends Exception {
    private static final long serialVersionUID = 1L;

    public PersistenciaExcepcion(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}