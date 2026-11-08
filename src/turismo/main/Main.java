package turismo.main;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import turismo.excepciones.PersistenciaExcepcion;
import turismo.excepciones.ReglaNegocioExcepcion;
import turismo.interfaces.RepositorioAgencia;
import turismo.persistencia.RepositorioArchivo;
import turismo.servicios.AgenciaController;
import turismo.ui.VentanaPrincipal;

/**
 * Punto de entrada. Arma las dependencias (inyección por constructor) y abre la ventana.
 * Los datos se guardan en la carpeta "datos" del directorio desde donde se ejecuta.
 */
public class Main {

    public static void main(String[] args) {
        Path carpetaDatos = Paths.get("datos");
        RepositorioAgencia repositorio = new RepositorioArchivo(carpetaDatos.resolve("agencia.dat"));
        AgenciaController controlador = new AgenciaController(repositorio, Clock.systemDefaultZone(),
                carpetaDatos.resolve("exportaciones"));
        try {
            controlador.inicializar();
        } catch (PersistenciaExcepcion | ReglaNegocioExcepcion e) {
            JOptionPane.showMessageDialog(null, "No se pudieron cargar los datos:\n" + e.getMessage()
                            + "\n\nSi el archivo está dañado, borrá la carpeta 'datos' y volvé a ejecutar.",
                    "Error al iniciar", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (Exception e) {
                // si Nimbus no está disponible se usa el aspecto por defecto
            }
            new VentanaPrincipal(controlador).setVisible(true);
        });
    }
}