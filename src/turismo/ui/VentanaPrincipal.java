package turismo.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import turismo.excepciones.PersistenciaExcepcion;
import turismo.servicios.AgenciaController;

/** Ventana principal: una pestaña por grupo de casos de uso. */
public final class VentanaPrincipal extends JFrame {
    private static final long serialVersionUID = 1L;

    private final transient AgenciaController controlador;
    private final List<PanelBase> paneles;

    public VentanaPrincipal(AgenciaController controlador) {
        super("Rumbo Sur - Gestión de tours y reservas");
        this.controlador = controlador;

        Runnable refrescarTodo = this::refrescarTodo;
        PanelCatalogo catalogo = new PanelCatalogo(controlador, refrescarTodo);
        PanelReservas reservas = new PanelReservas(controlador, refrescarTodo);
        PanelOperacion operacion = new PanelOperacion(controlador, refrescarTodo);
        PanelReportes reportes = new PanelReportes(controlador, refrescarTodo);
        paneles = List.of(catalogo, reservas, operacion, reportes);

        JTabbedPane pestanias = new JTabbedPane();
        pestanias.addTab("Catálogo y salidas", catalogo);
        pestanias.addTab("Clientes y reservas", reservas);
        pestanias.addTab("Operación", operacion);
        pestanias.addTab("Reportes", reportes);
        pestanias.addChangeListener(evento -> refrescarTodo()); // al cambiar de pestaña se actualizan los datos

        JLabel encabezado = new JLabel("Agencia Rumbo Sur   |   Hoy: "
                + controlador.hoy().format(Formulario.FORMATO_FECHA)
                + "   |   Los cambios se guardan automáticamente en la carpeta datos/");
        encabezado.setFont(encabezado.getFont().deriveFont(Font.BOLD, 13f));
        encabezado.setBorder(BorderFactory.createEmptyBorder(8, 12, 4, 12));

        setLayout(new BorderLayout());
        add(encabezado, BorderLayout.NORTH);
        add(pestanias, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evento) {
                cerrar();
            }
        });
        setSize(1250, 760);
        setLocationRelativeTo(null);
    }

    private void refrescarTodo() {
        for (PanelBase panel : paneles) {
            panel.refrescar();
        }
    }

    /** Guarda antes de salir; si falla, avisa y deja elegir si salir igual. */
    private void cerrar() {
        try {
            controlador.guardar();
            dispose();
            System.exit(0);
        } catch (PersistenciaExcepcion e) {
            int opcion = JOptionPane.showConfirmDialog(this, e.getMessage() + "\n¿Salir sin guardar?",
                    "Error al guardar", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            if (opcion == JOptionPane.YES_OPTION) {
                dispose();
                System.exit(1);
            }
        }
    }
}