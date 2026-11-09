package turismo.persistencia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import turismo.clases.Reserva;
import turismo.clases.Salida;
import turismo.excepciones.PersistenciaExcepcion;

public class RepositorioArchivoTest {

    @Rule
    public TemporaryFolder carpeta = new TemporaryFolder();

    @Test
    public void siNoHayArchivoDevuelveNull() throws PersistenciaExcepcion {
        RepositorioArchivo repositorio = new RepositorioArchivo(carpeta.getRoot().toPath().resolve("no_existe.dat"));
        assertNull(repositorio.cargar());
    }

    @Test
    public void loGuardadoSeRecuperaIgualEnOtraEjecucion() throws Exception {
        Path archivo = carpeta.getRoot().toPath().resolve("sub/agencia.dat");
        DatosAgencia originales = DatosDePrueba.crear(LocalDate.of(2026, 3, 1));
        new RepositorioArchivo(archivo).guardar(originales);

        DatosAgencia leidos = new RepositorioArchivo(archivo).cargar();
        assertEquals(originales.getTours().size(), leidos.getTours().size());
        assertEquals(originales.getSalidas().size(), leidos.getSalidas().size());
        assertEquals(originales.getReservas().size(), leidos.getReservas().size());
        Reserva reserva = leidos.getReservas().get(0);
        assertEquals(originales.getReservas().get(0).getTotal(), reserva.getTotal(), 0.01);
        // las referencias entre objetos se conservan (la reserva apunta a la misma salida de la lista)
        Salida salida = leidos.getSalidas().get(0);
        assertSame(salida, salida.getReservas().get(0).getSalida());
        assertEquals(originales.getProximoNumeroReserva(), leidos.getProximoNumeroReserva());
    }

    @Test(expected = PersistenciaExcepcion.class)
    public void unArchivoDaniadoLanzaPersistenciaExcepcion() throws Exception {
        Path archivo = carpeta.newFile("roto.dat").toPath();
        Files.writeString(archivo, "esto no es un objeto serializado");
        new RepositorioArchivo(archivo).cargar();
    }
}