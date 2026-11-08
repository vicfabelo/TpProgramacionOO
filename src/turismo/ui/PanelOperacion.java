package turismo.ui;

import java.awt.BorderLayout;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import turismo.clases.Guia;
import turismo.clases.Liquidacion;
import turismo.clases.Salida;
import turismo.clases.Vehiculo;
import turismo.servicios.AgenciaController;

/**
 * CU10 Asignar guía y transporte, CU11 Emitir lista de pasajeros, CU12 Iniciar/cerrar salida,
 * CU13 Liquidar honorarios y CU15 Procesar vencimientos.
 */
class PanelOperacion extends PanelBase {
    private static final long serialVersionUID = 1L;

    private final JTable tablaSalidas = crearTabla("Salida", "Tour", "Categoría", "Inicio", "Fin", "Estado",
            "Ocupados/Cupo", "Guía", "Vehículo", "Lista emitida", "Liquidación");
    private transient List<Salida> salidasMostradas = new ArrayList<>();

    PanelOperacion(AgenciaController controlador, Runnable alCambiarDatos) {
        super(controlador, alCambiarDatos);
        add(barraBotones(
                boton("Asignar guía", this::asignarGuia),
                boton("Asignar vehículo", this::asignarVehiculo),
                boton("Emitir lista de pasajeros", this::emitirLista),
                boton("Iniciar salida", this::iniciar),
                boton("Finalizar salida", this::finalizar),
                boton("Liquidar guía", this::liquidar),
                boton("Procesar vencimientos del día", this::procesarVencimientos)), BorderLayout.NORTH);
        add(conTitulo("Salidas (seleccioná una y elegí la operación)", new JScrollPane(tablaSalidas)), BorderLayout.CENTER);
        refrescar();
    }

    @Override
    void refrescar() {
        salidasMostradas = copia(controlador.getSalidas());
        List<Object[]> filas = new ArrayList<>();
        for (Salida s : salidasMostradas) {
            filas.add(new Object[] {s.getCodigo(), s.getTour().getNombre(), s.getTour().getCategoria(),
                    s.getFechaInicio().format(Formulario.FORMATO_FECHA), s.getFechaFin().format(Formulario.FORMATO_FECHA),
                    s.getEstado(), s.getLugaresOcupados() + "/" + s.getCupoMaximo(),
                    s.getGuia() == null ? "-" : s.getGuia().getNombreCompleto(),
                    s.getVehiculo() == null ? "-" : s.getVehiculo().getPatente(),
                    s.isListaPasajerosEmitida() ? "Sí" : "No",
                    s.getLiquidacion() == null ? "-" : pesos(s.getLiquidacion().getTotal())});
        }
        cargarFilas(tablaSalidas, filas);
    }

    private Salida salidaElegida() {
        return exigirSeleccion(tablaSalidas, salidasMostradas, "una salida");
    }

    private String asignarGuia() throws Exception {
        Salida salida = salidaElegida();
        Formulario f = new Formulario();
        JComboBox<Guia> guia = f.combo("Guía", controlador.getGuias());
        f.nota("Debe estar habilitado para tours de " + salida.getTour().getCategoria()
                + " y no tener otra salida entre esas fechas.");
        f.mostrar(this, "Asignar guía a " + salida.getCodigo());
        Guia elegido = Formulario.seleccion(guia, "Guía");
        controlador.asignarGuia(salida, elegido);
        return elegido.getNombreCompleto() + " asignado a la salida " + salida.getCodigo() + ".";
    }

    private String asignarVehiculo() throws Exception {
        Salida salida = salidaElegida();
        Formulario f = new Formulario();
        JComboBox<Vehiculo> vehiculo = f.combo("Vehículo", controlador.getVehiculos());
        f.nota("La capacidad no puede ser menor al cupo de la salida (" + salida.getCupoMaximo() + ").");
        f.mostrar(this, "Asignar vehículo a " + salida.getCodigo());
        Vehiculo elegido = Formulario.seleccion(vehiculo, "Vehículo");
        controlador.asignarVehiculo(salida, elegido);
        return "Vehículo " + elegido + " asignado a la salida " + salida.getCodigo() + ".";
    }

    private String emitirLista() throws Exception {
        Salida salida = salidaElegida();
        Path archivo = controlador.emitirListaPasajeros(salida);
        return "Lista guardada en " + archivo.toAbsolutePath() + "\n\n" + String.join("\n", Files.readAllLines(archivo));
    }

    private String iniciar() throws Exception {
        Salida salida = salidaElegida();
        controlador.iniciarSalida(salida);
        return "La salida " + salida.getCodigo() + " está EN CURSO.";
    }

    private String finalizar() throws Exception {
        Salida salida = salidaElegida();
        int opcion = JOptionPane.showConfirmDialog(this, "¿Cerrar la salida " + salida.getCodigo() + "?",
                "Finalizar salida", JOptionPane.YES_NO_OPTION);
        if (opcion != JOptionPane.YES_OPTION) {
            throw new Formulario.CanceladoPorUsuario();
        }
        controlador.finalizarSalida(salida);
        return "Salida " + salida.getCodigo() + " finalizada. Ya se puede liquidar al guía.";
    }

    private String liquidar() throws Exception {
        Salida salida = salidaElegida();
        Liquidacion l = controlador.liquidarGuia(salida);
        return "Liquidación de " + l.getGuia().getNombreCompleto() + " - salida " + salida.getCodigo() + "\n\n"
                + String.format("Monto fijo:   %d días x %s = %s%n", l.getDiasTrabajados(), pesos(l.getHonorarioPorDia()),
                pesos(l.getMontoFijo()))
                + String.format("Adicionales:  5 %% de %s = %s%n", pesos(l.getVentasAdicionales()),
                pesos(l.getComisionAdicionales()))
                + "TOTAL:        " + pesos(l.getTotal());
    }

    private String procesarVencimientos() throws Exception {
        List<String> acciones = controlador.procesarVencimientos();
        return acciones.isEmpty()
                ? "No hay vencimientos para procesar hoy (" + controlador.hoy().format(Formulario.FORMATO_FECHA) + ")."
                : String.join("\n", acciones);
    }
}