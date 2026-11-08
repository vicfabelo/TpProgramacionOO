package turismo.servicios;

import java.nio.file.Path;
import java.text.Normalizer;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import turismo.clases.Actividad;
import turismo.clases.CategoriaTour;
import turismo.clases.Cancelacion;
import turismo.clases.Cliente;
import turismo.clases.EstadoSalida;
import turismo.clases.Guia;
import turismo.clases.Liquidacion;
import turismo.clases.MedioPago;
import turismo.clases.NivelDificultad;
import turismo.clases.NotaAjuste;
import turismo.clases.Pago;
import turismo.clases.Pasajero;
import turismo.clases.PoliticaCancelacionAgencia;
import turismo.clases.PoliticaCancelacionCliente;
import turismo.clases.PromocionGrupo;
import turismo.clases.PromocionReservaAnticipada;
import turismo.clases.Proveedor;
import turismo.clases.Reserva;
import turismo.clases.Salida;
import turismo.clases.ServicioAdicional;
import turismo.clases.Tarifa;
import turismo.clases.Temporada;
import turismo.clases.TipoActividad;
import turismo.clases.TipoServicio;
import turismo.clases.Tour;
import turismo.clases.TourAventura;
import turismo.clases.TourCultural;
import turismo.clases.TourNaturaleza;
import turismo.clases.Vehiculo;
import turismo.clases.Vendedor;
import turismo.excepciones.EstadoInvalidoExcepcion;
import turismo.excepciones.PersistenciaExcepcion;
import turismo.excepciones.ReglaNegocioExcepcion;
import turismo.interfaces.PoliticaCancelacion;
import turismo.interfaces.Promocion;
import turismo.interfaces.RepositorioAgencia;
import turismo.persistencia.DatosAgencia;
import turismo.persistencia.DatosDePrueba;
import turismo.persistencia.ExportadorTexto;

/**
 * Controlador (GRASP): recibe cada pedido de la interfaz y lo delega en las clases del dominio.
 * La interfaz gráfica no conoce las reglas: solo llama a estos métodos.
 * DIP (SOLID): depende de RepositorioAgencia (interfaz), que se le inyecta por constructor.
 * El reloj también se inyecta para poder probar con fechas fijas.
 */
public class AgenciaController {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RepositorioAgencia repositorio;
    private final Clock reloj;
    private final Path carpetaExportacion;
    private final List<Promocion> promociones = List.of(new PromocionReservaAnticipada(), new PromocionGrupo());
    private final GeneradorReportes generadorReportes = new GeneradorReportes();
    private final ExportadorTexto exportador = new ExportadorTexto();
    private DatosAgencia datos;

    public AgenciaController(RepositorioAgencia repositorio, Clock reloj, Path carpetaExportacion) {
        this.repositorio = repositorio;
        this.reloj = reloj;
        this.carpetaExportacion = carpetaExportacion;
    }

    /** Carga los datos guardados; si es la primera ejecución, crea los datos de prueba. */
    public void inicializar() throws PersistenciaExcepcion, ReglaNegocioExcepcion {
        datos = repositorio.cargar();
        if (datos == null) {
            datos = DatosDePrueba.crear(hoy());
            repositorio.guardar(datos);
        }
    }

    /** Para tests: arranca con datos ya armados. */
    public void inicializar(DatosAgencia datosIniciales) {
        datos = datosIniciales;
    }

    public void guardar() throws PersistenciaExcepcion {
        repositorio.guardar(datos);
    }

    public LocalDate hoy() {
        return LocalDate.now(reloj);
    }

    // ================================================================= CU01 Administrar catálogo

    public Tour crearTourAventura(String codigo, String nombre, String descripcion, int dias, int cupoMinimo,
                                  int cupoMaximo, NivelDificultad dificultad, int edadMinima) throws ReglaNegocioExcepcion {
        return registrarTour(new TourAventura(codigo, nombre, descripcion, dias, cupoMinimo, cupoMaximo, dificultad, edadMinima));
    }

    public Tour crearTourCultural(String codigo, String nombre, String descripcion, int dias, int cupoMinimo,
                                  int cupoMaximo, String tematica) throws ReglaNegocioExcepcion {
        return registrarTour(new TourCultural(codigo, nombre, descripcion, dias, cupoMinimo, cupoMaximo, tematica));
    }

    public Tour crearTourNaturaleza(String codigo, String nombre, String descripcion, int dias, int cupoMinimo,
                                    int cupoMaximo, String areaNatural) throws ReglaNegocioExcepcion {
        return registrarTour(new TourNaturaleza(codigo, nombre, descripcion, dias, cupoMinimo, cupoMaximo, areaNatural));
    }

    private Tour registrarTour(Tour tour) throws ReglaNegocioExcepcion {
        for (Tour existente : datos.getTours()) {
            if (existente.getCodigo().equals(tour.getCodigo())) {
                throw new ReglaNegocioExcepcion("Ya existe un tour con código " + tour.getCodigo());
            }
        }
        datos.agregarTour(tour);
        return tour;
    }

    public void agregarActividad(Tour tour, int dia, int orden, LocalTime horario, String descripcion, String lugar,
                                 TipoActividad tipo, Proveedor proveedor) throws ReglaNegocioExcepcion {
        tour.agregarActividad(new Actividad(dia, orden, horario, descripcion, lugar, tipo, proveedor));
    }

    public void agregarTarifa(Tour tour, Temporada temporada, LocalDate vigenteDesde, double precioBase) {
        tour.agregarTarifa(new Tarifa(temporada, vigenteDesde, precioBase));
    }

    public void agregarServicioAdicional(Tour tour, String nombre, TipoServicio tipo, double precio, boolean obligatorio) {
        tour.agregarServicio(new ServicioAdicional(nombre, tipo, precio, obligatorio));
    }

    // ================================================================= CU02 Programar salida

    public Salida programarSalida(Tour tour, LocalDate fechaInicio, int cupo) throws ReglaNegocioExcepcion {
        if (!fechaInicio.isAfter(hoy())) {
            throw new ReglaNegocioExcepcion("La salida tiene que ser en una fecha futura");
        }
        Salida salida = new Salida(datos.getProximoCodigoSalida(), tour, fechaInicio, cupo);
        datos.confirmarCodigoSalida();
        datos.agregarSalida(salida);
        return salida;
    }

    // ================================================================= CU03 Registrar cliente

    public Cliente registrarCliente(String nombre, String apellido, String documento, String telefono,
                                    String email, String pais) throws ReglaNegocioExcepcion {
        for (Cliente existente : datos.getClientes()) {
            if (existente.getDocumento().equalsIgnoreCase(documento.trim())) {
                throw new ReglaNegocioExcepcion("Ya existe un cliente con documento " + documento);
            }
        }
        Cliente cliente = new Cliente(nombre, apellido, documento, telefono, email, pais);
        datos.agregarCliente(cliente);
        return cliente;
    }

    // ================================================================= CU04 Crear reserva (incluye CU05 calcular precio)

    public Reserva crearReserva(Salida salida, Cliente titular, Vendedor vendedor, List<Pasajero> pasajeros)
            throws ReglaNegocioExcepcion {
        Reserva reserva = salida.crearReserva(datos.getProximoNumeroReserva(), titular, vendedor, pasajeros,
                hoy(), promociones);
        datos.confirmarNumeroReserva();
        return reserva;
    }

    // ================================================================= CU06 Registrar pago

    public Pago registrarPago(Reserva reserva, double importe, MedioPago medio, int cuotas) throws ReglaNegocioExcepcion {
        return reserva.registrarPago(hoy(), importe, medio, cuotas);
    }

    // ================================================================= CU07 Contratar servicio adicional

    public void contratarServicio(Reserva reserva, Pasajero pasajero, ServicioAdicional servicio)
            throws ReglaNegocioExcepcion {
        reserva.contratarServicio(pasajero, servicio);
    }

    // ================================================================= CU08 Cancelar reserva

    public Cancelacion cancelarReserva(Reserva reserva, String motivo, boolean provocadaPorAgencia)
            throws ReglaNegocioExcepcion {
        PoliticaCancelacion politica = provocadaPorAgencia
                ? new PoliticaCancelacionAgencia()
                : new PoliticaCancelacionCliente();
        return reserva.cancelar(hoy(), motivo, politica);
    }

    // ================================================================= CU09 Modificar reserva

    public void reemplazarPasajero(Reserva reserva, Pasajero actual, Pasajero nuevo) throws ReglaNegocioExcepcion {
        reserva.reemplazarPasajero(actual, nuevo, hoy());
    }

    public NotaAjuste registrarAjuste(Reserva reserva, String motivo, double importe) throws ReglaNegocioExcepcion {
        return reserva.registrarAjuste(hoy(), motivo, importe);
    }

    // ================================================================= CU10 Asignar guía y transporte

    public void asignarGuia(Salida salida, Guia guia) throws ReglaNegocioExcepcion {
        salida.asignarGuia(guia);
    }

    /** Supuesto: un vehículo no puede estar en dos salidas superpuestas. */
    public void asignarVehiculo(Salida salida, Vehiculo vehiculo) throws ReglaNegocioExcepcion {
        for (Salida otra : datos.getSalidas()) {
            boolean activa = otra.getEstado() != EstadoSalida.CANCELADA
                    && otra.getEstado() != EstadoSalida.FINALIZADA;
            if (otra != salida && activa && otra.getVehiculo() == vehiculo && otra.seSuperponeCon(salida)) {
                throw new ReglaNegocioExcepcion("El vehículo " + vehiculo.getPatente() + " ya está asignado a la salida "
                        + otra.getCodigo() + " en esas fechas");
            }
        }
        salida.asignarVehiculo(vehiculo);
    }

    // ================================================================= CU11 Emitir lista de pasajeros

    /** Emite la lista y la guarda en un archivo de texto. */
    public Path emitirListaPasajeros(Salida salida) throws ReglaNegocioExcepcion, PersistenciaExcepcion {
        List<Pasajero> lista = salida.emitirListaPasajeros();
        List<String> lineas = new ArrayList<>();
        lineas.add("LISTA DE PASAJEROS - " + salida.getCodigo() + " - " + salida.getTour().getNombre());
        lineas.add("Fecha: " + salida.getFechaInicio().format(FORMATO_FECHA) + " al "
                + salida.getFechaFin().format(FORMATO_FECHA));
        lineas.add("Guía: " + (salida.getGuia() == null ? "SIN ASIGNAR" : salida.getGuia().getNombreCompleto())
                + " | Vehículo: " + (salida.getVehiculo() == null ? "SIN ASIGNAR" : salida.getVehiculo()));
        lineas.add("");
        int numero = 1;
        for (Pasajero pasajero : lista) {
            String restricciones = pasajero.getRestricciones().isEmpty() ? "-" : pasajero.getRestricciones();
            lineas.add(String.format("%2d. %-28s Doc: %-12s Emergencia: %-32s Restricciones: %s", numero++,
                    pasajero.getNombreCompleto(), pasajero.getDocumento(), pasajero.getContactoEmergencia(), restricciones));
        }
        lineas.add("");
        lineas.add("Total de pasajeros: " + lista.size());
        return exportador.escribir(carpetaExportacion.resolve("lista_" + salida.getCodigo() + ".txt"), lineas);
    }

    // ================================================================= CU12 Iniciar y cerrar salida

    public void iniciarSalida(Salida salida) throws EstadoInvalidoExcepcion {
        salida.iniciar();
    }

    public void finalizarSalida(Salida salida) throws EstadoInvalidoExcepcion {
        salida.finalizar();
    }

    // ================================================================= CU13 Liquidar honorarios

    public Liquidacion liquidarGuia(Salida salida) throws EstadoInvalidoExcepcion {
        return salida.liquidarHonorariosGuia(hoy());
    }

    // ================================================================= CU14 Emitir reportes

    public Reporte reporteOcupacion() {
        return generadorReportes.ocupacionPorSalida(datos.getSalidas());
    }

    public Reporte reporteVentasPorVendedor() {
        return generadorReportes.ventasPorVendedor(datos.getReservas());
    }

    public Reporte reporteIngresosPorTour() {
        return generadorReportes.ingresosPorTour(datos.getReservas());
    }

    public Reporte reporteCancelaciones() {
        return generadorReportes.cancelaciones(datos.getReservas());
    }

    public Path exportarReporte(Reporte reporte) throws PersistenciaExcepcion {
        return exportador.escribir(carpetaExportacion.resolve("reporte_" + nombreDeArchivo(reporte.getTitulo()) + ".txt"),
                reporte.aLineas());
    }

    /** Saca tildes y espacios para que el nombre de archivo funcione en cualquier sistema operativo. */
    private static String nombreDeArchivo(String texto) {
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase().replaceAll("[^a-z0-9]+", "_");
    }

    // ================================================================= CU15 Procesar vencimientos

    /**
     * Control diario: anula reservas con saldo vencido (10 días antes) y cancela con devolución total
     * las salidas que a 7 días no llegaron al cupo mínimo.
     * @return descripción de cada acción realizada
     */
    public List<String> procesarVencimientos() throws EstadoInvalidoExcepcion {
        LocalDate hoy = hoy();
        List<String> acciones = new ArrayList<>();
        for (Salida salida : datos.getSalidas()) {
            for (Reserva reserva : salida.getReservas()) {
                if (reserva.tieneSaldoVencido(hoy)) {
                    reserva.anularPorFaltaDePago(hoy);
                    acciones.add("Reserva " + reserva.getNumero() + " anulada por saldo impago (" + salida.getCodigo() + ")");
                }
            }
            if (salida.debeCancelarsePorCupoMinimo(hoy)) {
                salida.cancelarPorAgencia(hoy, "No se alcanzó el cupo mínimo " + Salida.DIAS_CONTROL_CUPO_MINIMO
                        + " días antes");
                acciones.add("Salida " + salida.getCodigo() + " cancelada por no alcanzar el cupo mínimo (devolución 100 %)");
            }
        }
        return acciones;
    }

    // ================================================================= consultas para la interfaz

    public List<Tour> getTours() {
        return datos.getTours();
    }

    public List<Salida> getSalidas() {
        return datos.getSalidas();
    }

    public List<Cliente> getClientes() {
        return datos.getClientes();
    }

    public List<Reserva> getReservas() {
        return datos.getReservas();
    }

    public List<Vendedor> getVendedores() {
        return datos.getEmpleados(Vendedor.class);
    }

    public List<Guia> getGuias() {
        return datos.getEmpleados(Guia.class);
    }

    public List<Vehiculo> getVehiculos() {
        return datos.getVehiculos();
    }

    public List<Proveedor> getProveedores() {
        return datos.getProveedores();
    }

    public Collection<Promocion> getPromociones() {
        return promociones;
    }

    public List<CategoriaTour> getCategorias() {
        return List.of(CategoriaTour.values());
    }

    public Path getCarpetaExportacion() {
        return carpetaExportacion;
    }
}