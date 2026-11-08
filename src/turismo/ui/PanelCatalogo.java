package turismo.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import turismo.clases.Actividad;
import turismo.clases.CategoriaTour;
import turismo.clases.NivelDificultad;
import turismo.clases.Proveedor;
import turismo.clases.Salida;
import turismo.clases.ServicioAdicional;
import turismo.clases.Tarifa;
import turismo.clases.Temporada;
import turismo.clases.TipoActividad;
import turismo.clases.TipoServicio;
import turismo.clases.Tour;
import turismo.servicios.AgenciaController;

/** CU01 Administrar catálogo de tours y CU02 Programar salidas. */
class PanelCatalogo extends PanelBase {
    private static final long serialVersionUID = 1L;

    private final JTable tablaTours = crearTabla("Código", "Nombre", "Categoría", "Días", "Cupo mín.", "Cupo máx.", "Detalle");
    private final JTable tablaSalidas = crearTabla("Salida", "Tour", "Inicio", "Fin", "Cupo", "Ocupados", "Estado");
    private final JTextArea detalleTour = new JTextArea();
    private transient List<Tour> toursMostrados = new ArrayList<>();

    PanelCatalogo(AgenciaController controlador, Runnable alCambiarDatos) {
        super(controlador, alCambiarDatos);

        detalleTour.setEditable(false);
        detalleTour.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        tablaTours.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting()) {
                mostrarDetalleTour();
            }
        });

        JPanel tours = new JPanel(new BorderLayout());
        tours.add(barraBotones(
                boton("Nuevo tour", this::nuevoTour),
                boton("Agregar actividad", this::agregarActividad),
                boton("Agregar tarifa", this::agregarTarifa),
                boton("Agregar servicio adicional", this::agregarServicio),
                boton("Programar salida", this::programarSalida)), BorderLayout.NORTH);
        JScrollPane scrollDetalle = new JScrollPane(detalleTour);
        scrollDetalle.setPreferredSize(new Dimension(470, 200));
        JSplitPane toursYDetalle = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(tablaTours), scrollDetalle);
        toursYDetalle.setResizeWeight(1.0);
        tours.add(toursYDetalle, BorderLayout.CENTER);

        JSplitPane principal = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                conTitulo("Catálogo de tours (seleccioná uno para ver itinerario, tarifas y servicios)", tours),
                conTitulo("Salidas programadas", new JScrollPane(tablaSalidas)));
        principal.setResizeWeight(0.6);
        tablaSalidas.setPreferredScrollableViewportSize(new Dimension(400, 150));
        add(principal, BorderLayout.CENTER);
        refrescar();
    }

    @Override
    void refrescar() {
        Tour anterior = seleccionado(tablaTours, toursMostrados);
        toursMostrados = copia(controlador.getTours());
        List<Object[]> filas = new ArrayList<>();
        for (Tour tour : toursMostrados) {
            filas.add(new Object[] {tour.getCodigo(), tour.getNombre(), tour.getCategoria(), tour.getDuracionDias(),
                    tour.getCupoMinimo(), tour.getCupoMaximo(), tour.getDetalleCategoria()});
        }
        cargarFilas(tablaTours, filas);
        int indiceAnterior = toursMostrados.indexOf(anterior);
        if (indiceAnterior >= 0) {
            int indice = tablaTours.convertRowIndexToView(indiceAnterior);
            tablaTours.setRowSelectionInterval(indice, indice);
        }

        List<Object[]> filasSalidas = new ArrayList<>();
        for (Salida salida : controlador.getSalidas()) {
            filasSalidas.add(new Object[] {salida.getCodigo(), salida.getTour().getNombre(),
                    salida.getFechaInicio().format(Formulario.FORMATO_FECHA), salida.getFechaFin().format(Formulario.FORMATO_FECHA),
                    salida.getCupoMaximo(), salida.getLugaresOcupados(), salida.getEstado()});
        }
        cargarFilas(tablaSalidas, filasSalidas);
        mostrarDetalleTour();
    }

    private void mostrarDetalleTour() {
        Tour tour = seleccionado(tablaTours, toursMostrados);
        if (tour == null) {
            detalleTour.setText("Seleccioná un tour.");
            return;
        }
        StringBuilder texto = new StringBuilder();
        texto.append(tour.getNombre()).append('\n').append(tour.getDescripcion()).append("\n\n");
        texto.append("ITINERARIO\n");
        for (Actividad actividad : tour.getItinerario()) {
            texto.append("  ").append(actividad).append('\n');
        }
        texto.append("\nTARIFAS\n");
        for (Tarifa tarifa : tour.getTarifas()) {
            texto.append("  ").append(tarifa.getTemporada()).append(" vigente desde ")
                    .append(tarifa.getVigenteDesde().format(Formulario.FORMATO_FECHA)).append(": ")
                    .append(pesos(tarifa.getPrecioBase())).append('\n');
        }
        texto.append("\nSERVICIOS ADICIONALES\n");
        for (ServicioAdicional servicio : tour.getServiciosDisponibles()) {
            texto.append("  ").append(servicio.getNombre()).append(" - ").append(pesos(servicio.getPrecio()))
                    .append(tour.esObligatorio(servicio) ? "  (OBLIGATORIO)" : "").append('\n');
        }
        detalleTour.setText(texto.toString());
        detalleTour.setCaretPosition(0);
    }

    private String nuevoTour() throws Exception {
        CategoriaTour categoria = (CategoriaTour) JOptionPane.showInputDialog(this, "Categoría del tour:", "Nuevo tour",
                JOptionPane.QUESTION_MESSAGE, null, CategoriaTour.values(), CategoriaTour.AVENTURA);
        if (categoria == null) {
            throw new Formulario.CanceladoPorUsuario();
        }
        Formulario f = new Formulario();
        JTextField codigo = f.texto("Código", "");
        JTextField nombre = f.texto("Nombre", "");
        JTextField descripcion = f.texto("Descripción", "");
        JTextField dias = f.texto("Duración (días)", "1");
        JTextField cupoMin = f.texto("Cupo mínimo", "4");
        JTextField cupoMax = f.texto("Cupo máximo", "15");
        JComboBox<NivelDificultad> dificultad = null;
        JTextField edadMinima = null;
        JTextField extra = null;
        if (categoria == CategoriaTour.AVENTURA) {
            dificultad = f.combo("Dificultad", List.of(NivelDificultad.values()));
            edadMinima = f.texto("Edad mínima", "14");
        } else {
            extra = f.texto(categoria == CategoriaTour.CULTURAL ? "Temática" : "Área natural", "");
        }
        f.mostrar(this, "Nuevo tour de " + categoria);

        String c = Formulario.requerido(codigo, "Código");
        String n = Formulario.requerido(nombre, "Nombre");
        int d = Formulario.entero(dias, "Duración");
        int min = Formulario.entero(cupoMin, "Cupo mínimo");
        int max = Formulario.entero(cupoMax, "Cupo máximo");
        Tour tour;
        switch (categoria) {
            case AVENTURA:
                tour = controlador.crearTourAventura(c, n, descripcion.getText(), d, min, max,
                        Formulario.seleccion(dificultad, "Dificultad"), Formulario.entero(edadMinima, "Edad mínima"));
                break;
            case CULTURAL:
                tour = controlador.crearTourCultural(c, n, descripcion.getText(), d, min, max, extra.getText());
                break;
            default:
                tour = controlador.crearTourNaturaleza(c, n, descripcion.getText(), d, min, max, extra.getText());
        }
        return "Tour " + tour + " creado. Ahora cargale tarifas" + (categoria == CategoriaTour.AVENTURA
                ? " y un seguro de viaje (obligatorio en aventura)" : "") + " para poder programar salidas.";
    }

    private String agregarActividad() throws Exception {
        Tour tour = exigirSeleccion(tablaTours, toursMostrados, "un tour");
        List<Object> proveedores = new ArrayList<>();
        proveedores.add("(sin proveedor)");
        proveedores.addAll(controlador.getProveedores());
        Formulario f = new Formulario();
        JTextField dia = f.texto("Día", "1");
        JTextField orden = f.texto("Orden en el día", String.valueOf(tour.getItinerario().size() + 1));
        JTextField horario = f.texto("Horario (hh:mm)", "09:00");
        JTextField descripcion = f.texto("Descripción", "");
        JTextField lugar = f.texto("Lugar", "");
        JComboBox<TipoActividad> tipo = f.combo("Tipo", List.of(TipoActividad.values()));
        JComboBox<Object> proveedor = f.combo("Proveedor", proveedores);
        f.mostrar(this, "Agregar actividad a " + tour.getNombre());
        Object elegido = proveedor.getSelectedItem();
        controlador.agregarActividad(tour, Formulario.entero(dia, "Día"), Formulario.entero(orden, "Orden"),
                Formulario.hora(horario, "Horario"), Formulario.requerido(descripcion, "Descripción"), lugar.getText(),
                Formulario.seleccion(tipo, "Tipo"), elegido instanceof Proveedor ? (Proveedor) elegido : null);
        return null;
    }

    private String agregarTarifa() throws Exception {
        Tour tour = exigirSeleccion(tablaTours, toursMostrados, "un tour");
        Formulario f = new Formulario();
        JComboBox<Temporada> temporada = f.combo("Temporada", List.of(Temporada.values()));
        JTextField desde = f.texto("Vigente desde", controlador.hoy().format(Formulario.FORMATO_FECHA));
        JTextField precio = f.texto("Precio base por pasajero", "");
        f.nota("Temporada ALTA: dic, ene, feb, jul · MEDIA: mar, abr, oct, nov · BAJA: may, jun, ago, sep");
        f.mostrar(this, "Agregar tarifa a " + tour.getNombre());
        controlador.agregarTarifa(tour, Formulario.seleccion(temporada, "Temporada"),
                Formulario.fecha(desde, "Vigente desde"), Formulario.decimal(precio, "Precio"));
        return null;
    }

    private String agregarServicio() throws Exception {
        Tour tour = exigirSeleccion(tablaTours, toursMostrados, "un tour");
        Formulario f = new Formulario();
        JTextField nombre = f.texto("Nombre", "");
        JComboBox<TipoServicio> tipo = f.combo("Tipo", List.of(TipoServicio.values()));
        JTextField precio = f.texto("Precio por pasajero", "");
        javax.swing.JCheckBox obligatorio = f.check("Obligatorio en este tour");
        f.mostrar(this, "Agregar servicio a " + tour.getNombre());
        controlador.agregarServicioAdicional(tour, Formulario.requerido(nombre, "Nombre"),
                Formulario.seleccion(tipo, "Tipo"), Formulario.decimal(precio, "Precio"), obligatorio.isSelected());
        return null;
    }

    private String programarSalida() throws Exception {
        Formulario f = new Formulario();
        Tour preseleccionado = seleccionado(tablaTours, toursMostrados);
        JComboBox<Tour> tour = f.combo("Tour", controlador.getTours());
        if (preseleccionado != null) {
            tour.setSelectedItem(preseleccionado);
        }
        JTextField fecha = f.texto("Fecha de inicio", controlador.hoy().plusDays(30).format(Formulario.FORMATO_FECHA));
        JTextField cupo = f.texto("Cupo", preseleccionado == null ? "" : String.valueOf(preseleccionado.getCupoMaximo()));
        f.nota("El cupo debe estar entre el mínimo y el máximo del tour.");
        f.mostrar(this, "Programar salida");
        Salida salida = controlador.programarSalida(Formulario.seleccion(tour, "Tour"),
                Formulario.fecha(fecha, "Fecha de inicio"), Formulario.entero(cupo, "Cupo"));
        return "Salida " + salida.getCodigo() + " programada del " + salida.getFechaInicio().format(Formulario.FORMATO_FECHA)
                + " al " + salida.getFechaFin().format(Formulario.FORMATO_FECHA) + ".";
    }
}