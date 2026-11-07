package turismo.persistencia;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import turismo.excepciones.PersistenciaExcepcion;
import turismo.interfaces.RepositorioAgencia;

/**
 * Guarda y lee todos los datos en un archivo binario (serialización de objetos).
 * Primero escribe en un archivo temporal y después lo reemplaza, así un corte a mitad
 * de la escritura no deja el archivo original roto.
 */
public class RepositorioArchivo implements RepositorioAgencia {

    private final Path archivo;

    public RepositorioArchivo(Path archivo) {
        this.archivo = archivo;
    }

    @Override
    public DatosAgencia cargar() throws PersistenciaExcepcion {
        if (!Files.exists(archivo)) {
            return null;
        }
        ObjectInputStream entrada = null;
        try {
            entrada = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(archivo)));
            return (DatosAgencia) entrada.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            throw new PersistenciaExcepcion("No se pudieron leer los datos de " + archivo.toAbsolutePath(), e);
        } finally {
            cerrar(entrada);
        }
    }

    @Override
    public void guardar(DatosAgencia datos) throws PersistenciaExcepcion {
        Path temporal = archivo.resolveSibling(archivo.getFileName() + ".tmp");
        ObjectOutputStream salida = null;
        try {
            if (archivo.getParent() != null) {
                Files.createDirectories(archivo.getParent());
            }
            salida = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(temporal)));
            salida.writeObject(datos);
            salida.close();
            salida = null;
            Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new PersistenciaExcepcion("No se pudieron guardar los datos en " + archivo.toAbsolutePath(), e);
        } finally {
            cerrar(salida);
        }
    }

    private static void cerrar(AutoCloseable recurso) {
        if (recurso == null) {
            return;
        }
        try {
            recurso.close();
        } catch (Exception e) {
            // si falla el cierre no hay nada más que hacer; el error principal ya se informó
        }
    }

    public Path getArchivo() {
        return archivo;
    }
}