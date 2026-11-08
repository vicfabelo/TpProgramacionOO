package turismo.persistencia;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import turismo.excepciones.PersistenciaExcepcion;

/** Escribe archivos de texto (lista de pasajeros, reportes) para imprimir o enviar. */
public class ExportadorTexto {

    public Path escribir(Path archivo, List<String> lineas) throws PersistenciaExcepcion {
        try {
            if (archivo.getParent() != null) {
                Files.createDirectories(archivo.getParent());
            }
            try (BufferedWriter escritor = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
                for (String linea : lineas) {
                    escritor.write(linea);
                    escritor.newLine();
                }
            }
            return archivo;
        } catch (IOException e) {
            throw new PersistenciaExcepcion("No se pudo escribir " + archivo.toAbsolutePath(), e);
        }
    }

    public List<String> leer(Path archivo) throws PersistenciaExcepcion {
        try {
            return Files.readAllLines(archivo, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenciaExcepcion("No se pudo leer " + archivo.toAbsolutePath(), e);
        }
    }
}