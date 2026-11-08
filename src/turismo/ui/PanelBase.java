package turismo.ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import turismo.excepciones.PersistenciaExcepcion;
import turismo.excepciones.ReglaNegocioExcepcion;
import turismo.servicios.AgenciaController;

/**
 * Base de todos los paneles: centraliza el manejo de errores de las acciones del usuario
 * (try / catch / finally) y algunas utilidades para tablas.
 */
abstract class PanelBase extends JPanel {
    private static final long serialVersionUID = 1L;

    /** Acción que dispara un botón. Devuelve el mensaje a mostrar (o null). */
    @FunctionalInterface
    interface Accion {
        String ejecutar() throws Exception;
    }

    protected final transient AgenciaController controlador;
    private final transient Runnable alCambiarDatos;

    protected PanelBase(AgenciaController controlador, Runnable alCambiarDatos) {
        super(new BorderLayout(8, 8));
        this.controlador = controlador;
        this.alCambiarDatos = alCambiarDatos;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    /** Vuelve a cargar las tablas desde el controlador. */
    abstract void refrescar();

    /**
     * Ejecuta la acción de un botón. Si sale bien guarda los datos y refresca todas las pestañas;
     * si una regla de negocio no se cumple, muestra el motivo.
     */
    protected void ejecutar(Accion accion) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            String mensaje = accion.ejecutar();
            controlador.guardar();
            alCambiarDatos.run();
            if (mensaje != null) {
                mostrarTexto("Operación realizada", mensaje);
            }
        } catch (Formulario.CanceladoPorUsuario e) {
            // el usuario cerró el diálogo: no se hace nada
        } catch (ReglaNegocioExcepcion e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "No se puede realizar la operación",
                    JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Dato inválido", JOptionPane.WARNING_MESSAGE);
        } catch (PersistenciaExcepcion e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error de archivo", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error inesperado: " + e, "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    protected void mostrarTexto(String titulo, String texto) {
        JTextArea area = new JTextArea(texto);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setRows(Math.min(22, texto.split("\n").length + 1));
        area.setColumns(Math.min(110, maximoLargoLinea(texto) + 2));
        JOptionPane.showMessageDialog(this, new JScrollPane(area), titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    private static int maximoLargoLinea(String texto) {
        int maximo = 20;
        for (String linea : texto.split("\n")) {
            maximo = Math.max(maximo, linea.length());
        }
        return maximo;
    }

    protected static JTable crearTabla(String... columnas) {
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        JTable tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.setRowHeight(22);
        tabla.setFillsViewportHeight(true);
        return tabla;
    }

    /** Recarga las filas de una tabla. */
    protected static void cargarFilas(JTable tabla, List<Object[]> filas) {
        DefaultTableModel modelo = (DefaultTableModel) tabla.getModel();
        modelo.setRowCount(0);
        for (Object[] fila : filas) {
            modelo.addRow(fila);
        }
    }

    /** Devuelve el objeto de la fila seleccionada (teniendo en cuenta el orden de la tabla) o null. */
    protected static <T> T seleccionado(JTable tabla, List<T> objetos) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        return objetos.get(tabla.convertRowIndexToModel(fila));
    }

    protected <T> T exigirSeleccion(JTable tabla, List<T> objetos, String que) {
        T objeto = seleccionado(tabla, objetos);
        if (objeto == null) {
            throw new IllegalArgumentException("Primero seleccioná " + que + " en la tabla");
        }
        return objeto;
    }

    protected static JPanel barraBotones(JButton... botones) {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        for (JButton boton : botones) {
            barra.add(boton);
        }
        return barra;
    }

    protected JButton boton(String texto, Accion accion) {
        JButton boton = new JButton(texto);
        boton.addActionListener(evento -> ejecutar(accion)); // manejo de eventos
        return boton;
    }

    protected static JPanel conTitulo(String titulo, java.awt.Component contenido) {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder(titulo));
        panel.add(contenido, BorderLayout.CENTER);
        return panel;
    }

    protected static String pesos(double importe) {
        return String.format("$ %,.2f", importe);
    }

    protected static <T> List<T> copia(List<T> lista) {
        return new ArrayList<>(lista);
    }
}