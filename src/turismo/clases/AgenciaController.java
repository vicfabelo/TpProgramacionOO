package turismo.clases;

import turismo.excepciones.*;
import turismo.interfaces.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Controlador principal (Fachada / Controller GRASP) del sistema Rumbo Sur.
 */
public class AgenciaController implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Tour> tours = new ArrayList<>();
    private final List<Salida> salidas = new ArrayList<>();
    private final List<Cliente> clientes = new ArrayList<>();
    private final List<Empleado> empleados = new ArrayList<>();
    private final List<Vehiculo> vehiculos = new ArrayList<>();
    private final List<Proveedor> proveedores = new ArrayList<>();

    private int autoincrementalReserva = 1;

    private transient RepositorioAgencia repositorio;
    private transient GeneradorReportes generadorReportes;

    public AgenciaController() {
        this.generadorReportes = new GeneradorReportes();
    }

    public void setRepositorio(RepositorioAgencia repositorio) {
        this.repositorio = repositorio;
    }

    // =========================================================================
    // CASO DE USO 1: CATALOGO Y PROGRAMACION DE SALIDAS
    // =========================================================================

    public Salida programarSalida(String codigoTour, LocalDate fechaInicio, int cupoMaximo) throws ReglaNegocioExcepcion {
        Tour tour = buscarTourPorCodigo(codigoTour)
                .orElseThrow(() -> new ReglaNegocioExcepcion("No existe un tour registrado con el código: " + codigoTour));

        String codigoSalida = "SAL-" + tour.getCodigo() + "-" + fechaInicio.toString();
        Salida salida = new Salida(codigoSalida, tour, fechaInicio, cupoMaximo);
        salidas.add(salida);
        return salida;
    }

    // =========================================================================
    // CASO DE USO 2: RESERVAS Y PASAJEROS
    // =========================================================================

    public Reserva crearReserva(String codigoSalida, String documentoCliente, String legajoVendedor,
                                List<Pasajero> pasajeros, List<Promocion> promociones) throws ReglaNegocioExcepcion {

        Salida salida = buscarSalidaPorCodigo(codigoSalida)
                .orElseThrow(() -> new ReglaNegocioExcepcion("No se encontró la salida: " + codigoSalida));

        Cliente cliente = buscarClientePorDocumento(documentoCliente)
                .orElseThrow(() -> new ReglaNegocioExcepcion("No existe el cliente con documento: " + documentoCliente));

        Empleado emp = buscarEmpleadoPorLegajo(legajoVendedor)
                .orElseThrow(() -> new ReglaNegocioExcepcion("No se encontró el vendedor con legajo: " + legajoVendedor));

        if (!(emp instanceof Vendedor)) {
            throw new ReglaNegocioExcepcion("El empleado con legajo " + legajoVendedor + " no cumple el rol de vendedor.");
        }

        int numeroReserva = autoincrementalReserva++;
        return salida.crearReserva(numeroReserva, cliente, (Vendedor) emp, pasajeros, LocalDate.now(), promociones);
    }

    // =========================================================================
    // CASO DE USO 3: REGISTRO DE PAGOS Y SEÑAS
    // =========================================================================

    public Pago registrarPago(String codigoSalida, int numeroReserva, double importe, MedioPago medio, int cuotas)
            throws ReglaNegocioExcepcion {

        Salida salida = buscarSalidaPorCodigo(codigoSalida)
                .orElseThrow(() -> new ReglaNegocioExcepcion("No se encontró la salida: " + codigoSalida));

        Reserva reserva = salida.getReservas().stream()
                .filter(r -> r.getNumero() == numeroReserva)
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioExcepcion("No se encontró la reserva #" + numeroReserva + " en la salida " + codigoSalida));

        return reserva.registrarPago(LocalDate.now(), importe, medio, cuotas);
    }

    // =========================================================================
    // CASO DE USO 4: CANCELACIONES
    // =========================================================================

    public void cancelarReservaPorCliente(String codigoSalida, int numeroReserva, String motivo) throws ReglaNegocioExcepcion {
        Salida salida = buscarSalidaPorCodigo(codigoSalida)
                .orElseThrow(() -> new ReglaNegocioExcepcion("No existe la salida: " + codigoSalida));

        Reserva reserva = salida.getReservas().stream()
                .filter(r -> r.getNumero() == numeroReserva)
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioExcepcion("No existe la reserva #" + numeroReserva));

        PoliticaCancelacion politica = new PoliticaCancelacionCliente();
        reserva.cancelar(LocalDate.now(), motivo, politica);
        salida.actualizarEstado();
    }

    public void cancelarSalidaPorAgencia(String codigoSalida, String motivo) throws EstadoInvalidoExcepcion, ReglaNegocioExcepcion {
        Salida salida = buscarSalidaPorCodigo(codigoSalida)
                .orElseThrow(() -> new ReglaNegocioExcepcion("No existe la salida: " + codigoSalida));

        salida.cancelarPorAgencia(LocalDate.now(), motivo);
    }

    // =========================================================================
    // CASO DE USO 5: OPERACION Y ASIGNACION DE RECURSOS
    // =========================================================================

    public void asignarRecursosASalida(String codigoSalida, String legajoGuia, String patenteVehiculo)
            throws ReglaNegocioExcepcion {

        Salida salida = buscarSalidaPorCodigo(codigoSalida)
                .orElseThrow(() -> new ReglaNegocioExcepcion("No se encontró la salida: " + codigoSalida));

        if (legajoGuia != null && !legajoGuia.isBlank()) {
            Empleado emp = buscarEmpleadoPorLegajo(legajoGuia)
                    .orElseThrow(() -> new ReglaNegocioExcepcion("No se encontró el guía con legajo: " + legajoGuia));

            if (!(emp instanceof Guia)) {
                throw new ReglaNegocioExcepcion("El empleado con legajo " + legajoGuia + " no es un Guía.");
            }
            salida.asignarGuia((Guia) emp);
        }

        if (patenteVehiculo != null && !patenteVehiculo.isBlank()) {
            Vehiculo vehiculo = buscarVehiculoPorPatente(patenteVehiculo)
                    .orElseThrow(() -> new ReglaNegocioExcepcion("No se encontró el vehículo con patente: " + patenteVehiculo));

            salida.asignarVehiculo(vehiculo);
        }
    }

    // =========================================================================
    // CASO DE USO 6: LIQUIDACION DE HONORARIOS Y CIERRE
    // =========================================================================

    public Liquidacion cerrarSalidaYLiquidar(String codigoSalida) throws EstadoInvalidoExcepcion, ReglaNegocioExcepcion {
        Salida salida = buscarSalidaPorCodigo(codigoSalida)
                .orElseThrow(() -> new ReglaNegocioExcepcion("No se encontró la salida: " + codigoSalida));

        salida.finalizar();
        return salida.liquidarHonorariosGuia(LocalDate.now());
    }

    // =========================================================================
    // PERSISTENCIA
    // =========================================================================

    public void guardarDatos() throws PersistenciaExcepcion {
        if (repositorio != null) {
            repositorio.guardar(this);
        }
    }

    public void cargarDatos() throws PersistenciaExcepcion {
        if (repositorio != null) {
            AgenciaController cargado = repositorio.cargar();
            if (cargado != null) {
                this.tours.clear(); this.tours.addAll(cargado.getTours());
                this.salidas.clear(); this.salidas.addAll(cargado.getSalidas());
                this.clientes.clear(); this.clientes.addAll(cargado.getClientes());
                this.empleados.clear(); this.empleados.addAll(cargado.getEmpleados());
                this.vehiculos.clear(); this.vehiculos.addAll(cargado.getVehiculos());
                this.proveedores.clear(); this.proveedores.addAll(cargado.getProveedores());
            }
        }
    }

    // =========================================================================
    // BUSQUEDAS Y ACCESORES
    // =========================================================================

    public Optional<Tour> buscarTourPorCodigo(String codigo) {
        return tours.stream().filter(t -> t.getCodigo().equalsIgnoreCase(codigo)).findFirst();
    }

    public Optional<Salida> buscarSalidaPorCodigo(String codigo) {
        return salidas.stream().filter(s -> s.getCodigo().equalsIgnoreCase(codigo)).findFirst();
    }

    public Optional<Cliente> buscarClientePorDocumento(String doc) {
        return clientes.stream().filter(c -> c.getDocumento().equalsIgnoreCase(doc)).findFirst();
    }

    public Optional<Empleado> buscarEmpleadoPorLegajo(String legajo) {
        return empleados.stream().filter(e -> e.getLegajo().equalsIgnoreCase(legajo)).findFirst();
    }

    public Optional<Vehiculo> buscarVehiculoPorPatente(String patente) {
        return vehiculos.stream().filter(v -> v.getPatente().equalsIgnoreCase(patente)).findFirst();
    }

    public void registrarTour(Tour tour) { tours.add(tour); }
    public void registrarCliente(Cliente cliente) { clientes.add(cliente); }
    public void registrarEmpleado(Empleado empleado) { empleados.add(empleado); }
    public void registrarVehiculo(Vehiculo vehiculo) { vehiculos.add(vehiculo); }
    public void registrarProveedor(Proveedor proveedor) { proveedores.add(proveedor); }

    public List<Tour> getTours() { return Collections.unmodifiableList(tours); }
    public List<Salida> getSalidas() { return Collections.unmodifiableList(salidas); }
    public List<Cliente> getClientes() { return Collections.unmodifiableList(clientes); }
    public List<Empleado> getEmpleados() { return Collections.unmodifiableList(empleados); }
    public List<Vehiculo> getVehiculos() { return Collections.unmodifiableList(vehiculos); }
    public List<Proveedor> getProveedores() { return Collections.unmodifiableList(proveedores); }
}