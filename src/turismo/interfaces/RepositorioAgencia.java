package turismo.interfaces;

import turismo.clases.AgenciaController;
import turismo.excepciones.PersistenciaExcepcion;

public interface RepositorioAgencia {
    void guardar(AgenciaController controller) throws PersistenciaExcepcion;
    AgenciaController cargar() throws PersistenciaExcepcion;
}