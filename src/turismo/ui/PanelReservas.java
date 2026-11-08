package turismo.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import turismo.clases.Cancelacion;
import turismo.clases.Cliente;
import turismo.clases.EstadoReserva;
import turismo.clases.MedioPago;
import turismo.clases.NotaAjuste;
import turismo.clases.Pago;
import turismo.clases.Pasajero;
import turismo.clases.Reserva;
import turismo.clases.Salida;
import turismo.clases.ServicioAdicional;
import turismo.clases.TipoPasajero;
import turismo.clases.Vendedor;
import turismo.servicios.AgenciaController;

/**
 * CU03 Registrar cliente, CU04 Crear reserva, CU06 Registrar pago, CU07 Contratar servicios,
 * CU08 Cancelar reserva y CU09 Modificar reserva.
 */
class PanelReservas extends PanelBase {
    private static final long serialVersionUID = 1L;

    private final JTable tablaClientes = crearTabla("Apellido y nombre", "Documento", "Teléfono", "Email", "País");
    private final JTable tablaReservas = crearTabla("N°", "Titular", "Salida", "Inicio", "Pasajeros", "Total",
            "Pagado", "Saldo", "Estado", "Vendedor");
    private final JTextArea detalle = new JTextArea();
    private transient List<Reserva> reservasMostradas = new ArrayList<>();

    PanelReservas(AgenciaController controlador, Runnable alCambiarDatos) {
        super(controlador, alCambiarDatos);
        detalle.setEditable(false);
        detalle.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        tablaReservas.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting()) {
                mostrarDetalle();
            }
        });

        JPanel clientes = new JPanel(new BorderLayout());
        clientes.add(barraBotones(boton("Registrar cliente", this::registrarCliente)), BorderLayout.NORTH);
        clientes.add(new JScrollPane(tablaClientes), BorderLayout.CENTER);

        JPanel reservas = new JPanel(new BorderLayout());
        reservas.add(barraBotones(
                boton("Nueva reserva", this::nuevaReserva),
                boton("Registrar pago", this::registrarPago),
                boton("Contratar servicio", this::contratarServicio),
                boton("Reemplazar pasajero", this::reemplazarPasajero),
                boton("Nota de ajuste", this::notaAjuste),
                boton("Cancelar reserva", this::cancelarReserva)), BorderLayout.NORTH);
        JScrollPane scrollDetalle = new JScrollPane(detalle);
        scrollDetalle.setPreferredSize(new Dimension(430, 200));
        JSplitPane tablaYDetalle = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(tablaReservas), scrollDetalle);
        tablaYDetalle.setResizeWeight(1.0);
        reservas.add(tablaYDetalle, BorderLayout.CENTER);

        JSplitPane principal = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                conTitulo("Clientes", clientes), conTitulo("Reservas (seleccioná una para ver el detalle)", reservas));
        principal.setResizeWeight(0.3);
        add(principal, BorderLayout.CENTER);
        refrescar();
    }

    @Override
    void refrescar() {
        List<Object[]> filasClientes = new ArrayList<>();
        for (Cliente cliente : controlador.getClientes()) {
            filasClientes.add(new Object[] {cliente.getNombreCompleto(), cliente.getDocumento(), cliente.getTelefono(),
                    cliente.getEmail(), cliente.getPaisResidencia()});
        }
        cargarFilas(tablaClientes, filasClientes);

        Reserva anterior = seleccionado(tablaReservas, reservasMostradas);
        reservasMostradas = copia(controlador.getReservas());
        List<Object[]> filas = new ArrayList<>();
        for (Reserva r : reservasMostradas) {
            filas.add(new Object[] {r.getNumero(), r.getTitular().getNombreCompleto(), r.getSalida().getCodigo(),
                    r.getSalida().getFechaInicio().format(Formulario.FORMATO_FECHA), r.getCantidadPasajeros(),
                    pesos(r.getTotal()), pesos(r.getTotalPagado()), pesos(r.getSaldo()), r.getEstado(),
                    r.getVendedor().getNombreCompleto()});
        }
        cargarFilas(tablaReservas, filas);
        int indiceAnterior = reservasMostradas.indexOf(anterior);
        if (indiceAnterior >= 0) {
            int indice = tablaReservas.convertRowIndexToView(indiceAnterior);
            tablaReservas.setRowSelectionInterval(indice, indice);
        }
        mostrarDetalle();
    }

    private void mostrarDetalle() {
        Reserva r = seleccionado(tablaReservas, reservasMostradas);
        if (r == null) {
            detalle.setText("Seleccioná una reserva.");
            return;
        }
        StringBuilder t = new StringBuilder();
        t.append("Reserva ").append(r.getNumero()).append(" - ").append(r.getEstado()).append('\n');
        t.append("Salida: ").append(r.getSalida()).append('\n');
        t.append("Titular: ").append(r.getTitular().getNombreCompleto()).append('\n');
        t.append("Reservada el ").append(r.getFechaReserva().format(Formulario.FORMATO_FECHA))
                .append(" por ").append(r.getVendedor().getNombreCompleto()).append("\n\n");
        t.append("PASAJEROS\n");
        for (Pasajero p : r.getPasajeros()) {
            t.append("  ").append(p.getNombreCompleto()).append(" (").append(p.getTipo()).append(", ")
                    .append(p.calcularEdad(r.getSalida().getFechaInicio())).append(" años) paga ")
                    .append(Math.round(p.calcularFactorTarifa(r.getSalida().getFechaInicio()) * 100)).append(" % de la tarifa\n");
            for (ServicioAdicional s : p.getServicios()) {
                t.append("      + ").append(s.getNombre()).append(' ').append(pesos(s.getPrecio())).append('\n');
            }
        }
        t.append("\nIMPORTES\n");
        t.append("  Tarifas:            ").append(pesos(r.getTotalTarifas())).append('\n');
        if (!r.getPromocionAplicada().isEmpty()) {
            t.append("  Promoción ").append(r.getPromocionAplicada()).append(" (")
                    .append(Math.round(r.getPorcentajePromocion() * 100)).append(" %): -")
                    .append(pesos(r.getDescuentoPromocion())).append('\n');
        }
        t.append("  Adicionales:        ").append(pesos(r.getTotalAdicionales())).append('\n');
        for (NotaAjuste nota : r.getAjustes()) {
            t.append("  Ajuste ").append(nota).append('\n');
        }
        t.append("  TOTAL:              ").append(pesos(r.getTotal())).append('\n');
        t.append("  Seña (30 %):        ").append(pesos(r.getMontoSena())).append('\n');
        t.append("  Saldo hasta el ").append(r.getFechaLimiteSaldo().format(Formulario.FORMATO_FECHA))
                .append(": ").append(pesos(r.getSaldo())).append("\n\nPAGOS\n");
        for (Pago pago : r.getPagos()) {
            t.append("  ").append(pago.getFecha().format(Formulario.FORMATO_FECHA)).append("  ")
                    .append(pesos(pago.getImporte())).append("  ").append(pago.getMedio());
            if (pago.getCuotas() > 1) {
                t.append(" en ").append(pago.getCuotas()).append(" cuotas (recargo cobrado ")
                        .append(pesos(pago.getRecargo())).append(')');
            }
            t.append('\n');
        }
        Cancelacion c = r.getCancelacion();
        if (c != null) {
            t.append("\nCANCELACIÓN ").append(c.getFecha().format(Formulario.FORMATO_FECHA)).append(" - ")
                    .append(c.getPolitica()).append('\n').append("  Motivo: ").append(c.getMotivo()).append('\n');
            for (Map.Entry<MedioPago, Double> devolucion : c.getDevolucionesPorMedio().entrySet()) {
                t.append("  Devuelto por ").append(devolucion.getKey()).append(": ")
                        .append(pesos(devolucion.getValue())).append('\n');
            }
        }
        detalle.setText(t.toString());
        detalle.setCaretPosition(0);
    }

    private String registrarCliente() throws Exception {
        Formulario f = new Formulario();
        JTextField nombre = f.texto("Nombre", "");
        JTextField apellido = f.texto("Apellido", "");
        JTextField documento = f.texto("Documento", "");
        JTextField telefono = f.texto("Teléfono", "");
        JTextField email = f.texto("Correo electrónico", "");
        JTextField pais = f.texto("País de residencia", "Argentina");
        f.mostrar(this, "Registrar cliente");
        Cliente cliente = controlador.registrarCliente(Formulario.requerido(nombre, "Nombre"),
                Formulario.requerido(apellido, "Apellido"), Formulario.requerido(documento, "Documento"),
                telefono.getText(), email.getText(), pais.getText());
        return "Cliente " + cliente.getNombreCompleto() + " registrado.";
    }

    private String nuevaReserva() throws Exception {
        List<Salida> abiertas = new ArrayList<>();
        for (Salida salida : controlador.getSalidas()) {
            if (salida.estaAbiertaParaReservas()) {
                abiertas.add(salida);
            }
        }
        Formulario f = new Formulario();
        JComboBox<Salida> salida = f.combo("Salida", abiertas);
        JComboBox<Cliente> cliente = f.combo("Titular", controlador.getClientes());
        JComboBox<Vendedor> vendedor = f.combo("Vendedor", controlador.getVendedores());
        JTextField cantidad = f.texto("Cantidad de pasajeros", "1");
        JCheckBox titularViaja = f.check("El titular viaja (primer pasajero)");
        titularViaja.setSelected(true);
        f.nota("Solo se listan salidas PROGRAMADAS o CONFIRMADAS.");
        f.mostrar(this, "Nueva reserva");

        Salida elegida = Formulario.seleccion(salida, "Salida");
        Cliente titular = Formulario.seleccion(cliente, "Titular");
        int n = Formulario.entero(cantidad, "Cantidad de pasajeros");
        if (n < 1) {
            throw new IllegalArgumentException("Tiene que haber al menos un pasajero");
        }
        List<Pasajero> pasajeros = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            Cliente datosIniciales = i == 1 && titularViaja.isSelected() ? titular : null;
            pasajeros.add(pedirPasajero("Pasajero " + i + " de " + n, datosIniciales));
        }
        Reserva r = controlador.crearReserva(elegida, titular, Formulario.seleccion(vendedor, "Vendedor"), pasajeros);
        return "Reserva " + r.getNumero() + " creada (estado " + r.getEstado() + ").\n"
                + "Total: " + pesos(r.getTotal())
                + (r.getPromocionAplicada().isEmpty() ? "" : "  (promoción " + r.getPromocionAplicada() + " aplicada)")
                + "\nPara confirmarla debe abonar la seña de " + pesos(r.getMontoSena()) + ".";
    }

    private Pasajero pedirPasajero(String titulo, Cliente datosIniciales) {
        Formulario f = new Formulario();
        JTextField nombre = f.texto("Nombre", datosIniciales == null ? "" : datosIniciales.getNombre());
        JTextField apellido = f.texto("Apellido", datosIniciales == null ? "" : datosIniciales.getApellido());
        JTextField documento = f.texto("Documento", datosIniciales == null ? "" : datosIniciales.getDocumento());
        JTextField nacimiento = f.texto("Fecha de nacimiento", "");
        JComboBox<TipoPasajero> tipo = f.combo("Tipo", List.of(TipoPasajero.values()));
        JTextField emergencia = f.texto("Contacto de emergencia", "");
        JTextField restricciones = f.texto("Restricciones (alimentarias/salud)", "");
        f.nota("Fecha en formato dd/mm/aaaa. Menores de 12 pagan 60 %, menores de 3 no pagan, jubilados 90 %.");
        f.mostrar(this, titulo);
        return new Pasajero(Formulario.requerido(nombre, "Nombre"), Formulario.requerido(apellido, "Apellido"),
                Formulario.requerido(documento, "Documento"), Formulario.fecha(nacimiento, "Fecha de nacimiento"),
                Formulario.seleccion(tipo, "Tipo"), Formulario.requerido(emergencia, "Contacto de emergencia"),
                restricciones.getText());
    }

    private String registrarPago() throws Exception {
        Reserva r = exigirSeleccion(tablaReservas, reservasMostradas, "una reserva");
        double sugerido = r.getEstado() == EstadoReserva.PENDIENTE ? Math.min(r.getSaldo(), Math.ceil(r.getMontoSena()))
                : r.getSaldo();
        Formulario f = new Formulario();
        f.nota("Total " + pesos(r.getTotal()) + " · pagado " + pesos(r.getTotalPagado()) + " · saldo "
                + pesos(r.getSaldo()) + "<br>Seña para confirmar: " + pesos(r.getMontoSena())
                + " · fecha límite del saldo: " + r.getFechaLimiteSaldo().format(Formulario.FORMATO_FECHA));
        JTextField importe = f.texto("Importe", String.format("%.2f", sugerido).replace(',', '.'));
        JComboBox<MedioPago> medio = f.combo("Medio de pago", List.of(MedioPago.values()));
        JTextField cuotas = f.texto("Cuotas (solo tarjeta, recargo 7 %)", "1");
        f.mostrar(this, "Registrar pago - reserva " + r.getNumero());
        Pago pago = controlador.registrarPago(r, Formulario.decimal(importe, "Importe"),
                Formulario.seleccion(medio, "Medio de pago"), Formulario.entero(cuotas, "Cuotas"));
        return "Pago registrado: " + pesos(pago.getImporte())
                + (pago.getRecargo() > 0 ? " + recargo " + pesos(pago.getRecargo()) + " = " + pesos(pago.getImporteCobrado()) : "")
                + "\nEstado de la reserva: " + r.getEstado() + " · saldo " + pesos(r.getSaldo());
    }

    private String contratarServicio() throws Exception {
        Reserva r = exigirSeleccion(tablaReservas, reservasMostradas, "una reserva");
        Formulario f = new Formulario();
        JComboBox<Pasajero> pasajero = f.combo("Pasajero", r.getPasajeros());
        JComboBox<ServicioAdicional> servicio = f.combo("Servicio", r.getSalida().getTour().getServiciosDisponibles());
        f.mostrar(this, "Contratar servicio adicional - reserva " + r.getNumero());
        controlador.contratarServicio(r, Formulario.seleccion(pasajero, "Pasajero"), Formulario.seleccion(servicio, "Servicio"));
        return "Servicio contratado. Nuevo total: " + pesos(r.getTotal());
    }

    private String reemplazarPasajero() throws Exception {
        Reserva r = exigirSeleccion(tablaReservas, reservasMostradas, "una reserva");
        Formulario f = new Formulario();
        JComboBox<Pasajero> actual = f.combo("Pasajero a reemplazar", r.getPasajeros());
        f.nota("Sin cargo hasta 3 días antes de la salida. En el paso siguiente cargás al nuevo pasajero.");
        f.mostrar(this, "Reemplazar pasajero - reserva " + r.getNumero());
        Pasajero saliente = Formulario.seleccion(actual, "Pasajero");
        Pasajero nuevo = pedirPasajero("Nuevo pasajero (reemplaza a " + saliente.getNombreCompleto() + ")", null);
        controlador.reemplazarPasajero(r, saliente, nuevo);
        return saliente.getNombreCompleto() + " fue reemplazado por " + nuevo.getNombreCompleto() + " sin cargo.";
    }

    private String notaAjuste() throws Exception {
        Reserva r = exigirSeleccion(tablaReservas, reservasMostradas, "una reserva");
        Formulario f = new Formulario();
        JTextField motivo = f.texto("Motivo", "");
        JTextField importe = f.texto("Importe (negativo = bonificación)", "");
        f.nota("Las reservas no se borran ni se editan: las correcciones quedan registradas como notas de ajuste.");
        f.mostrar(this, "Nota de ajuste - reserva " + r.getNumero());
        controlador.registrarAjuste(r, motivo.getText(), Formulario.decimal(importe, "Importe"));
        return "Ajuste registrado. Nuevo total: " + pesos(r.getTotal());
    }

    private String cancelarReserva() throws Exception {
        Reserva r = exigirSeleccion(tablaReservas, reservasMostradas, "una reserva");
        Formulario f = new Formulario();
        JTextField motivo = f.texto("Motivo", "");
        JCheckBox agencia = f.check("La cancela la agencia (devolución 100 %)");
        f.nota("Cliente: 90 % con 30 días o más · 50 % de 29 a 15 · 20 % de 14 a 7 · sin devolución con menos de 7.");
        f.mostrar(this, "Cancelar reserva " + r.getNumero());
        int confirmar = JOptionPane.showConfirmDialog(this, "¿Confirmás la cancelación de la reserva " + r.getNumero() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmar != JOptionPane.YES_OPTION) {
            throw new Formulario.CanceladoPorUsuario();
        }
        Cancelacion c = controlador.cancelarReserva(r, motivo.getText(), agencia.isSelected());
        return "Reserva " + r.getNumero() + " anulada (" + c.getPolitica() + ").\nSe devuelve el "
                + Math.round(c.getPorcentajeDevolucion() * 100) + " % de lo pagado: " + pesos(c.getImporteDevuelto())
                + " por el mismo medio de pago.";
    }
}