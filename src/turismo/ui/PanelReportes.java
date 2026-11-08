package turismo.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import turismo.servicios.AgenciaController;
import turismo.servicios.Reporte;

/** CU14 Emitir reportes: ocupación, ventas por vendedor, ingresos por tour y cancelaciones. */
class PanelReportes extends PanelBase {
    private static final long serialVersionUID = 1L;

    private static final String OCUPACION = "Ocupación por salida";
    private static final String VENTAS = "Ventas por vendedor";
    private static final String INGRESOS = "Ingresos por tour";
    private static final String CANCELACIONES = "Cancelaciones";

    private final JComboBox<String> tipoReporte = new JComboBox<>(new String[] {OCUPACION, VENTAS, INGRESOS, CANCELACIONES});
    private final JTable tabla = new JTable();
    private transient Reporte reporteActual;

    PanelReportes(AgenciaController controlador, Runnable alCambiarDatos) {
        super(controlador, alCambiarDatos);
        tabla.setRowHeight(22);
        tabla.setAutoCreateRowSorter(true);
        tabla.setFillsViewportHeight(true);
        tipoReporte.addActionListener(evento -> refrescar()); // al cambiar el combo se regenera

        JButton exportar = boton("Exportar a archivo .txt", this::exportar);
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        barra.add(new JLabel("Reporte:"));
        barra.add(tipoReporte);
        barra.add(exportar);
        add(barra, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        refrescar();
    }

    @Override
    void refrescar() {
        String elegido = (String) tipoReporte.getSelectedItem();
        if (VENTAS.equals(elegido)) {
            reporteActual = controlador.reporteVentasPorVendedor();
        } else if (INGRESOS.equals(elegido)) {
            reporteActual = controlador.reporteIngresosPorTour();
        } else if (CANCELACIONES.equals(elegido)) {
            reporteActual = controlador.reporteCancelaciones();
        } else {
            reporteActual = controlador.reporteOcupacion();
        }
        DefaultTableModel modelo = new DefaultTableModel(reporteActual.getColumnas(), 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        for (Object[] fila : new ArrayList<>(reporteActual.getFilas())) {
            modelo.addRow(fila);
        }
        tabla.setModel(modelo);
    }

    private String exportar() throws Exception {
        Path archivo = controlador.exportarReporte(reporteActual);
        return "Reporte exportado en " + archivo.toAbsolutePath();
    }
}