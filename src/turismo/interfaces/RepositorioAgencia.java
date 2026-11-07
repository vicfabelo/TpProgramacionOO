package turismo.interfaces;

import turismo.excepciones.PersistenciaExcepcion;
import turismo.persistencia.DatosAgencia;

/**
 * Abstracción de dónde se guardan los datos.
 * DIP: el controlador depende de esta interfaz y no de la clase que escribe el archivo,
 * por eso en los tests se puede usar un repositorio en memoria.
 */
public interface RepositorioAgencia {

    /** @return los datos guardados, o null si todavía no hay nada guardado */
    DatosAgencia cargar() throws PersistenciaExcepcion;

    void guardar(DatosAgencia datos) throws PersistenciaExcepcion;
}