package turismo.servicios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import turismo.Escenario;
import turismo.clases.Cancelacion;
import turismo.clases.CategoriaTour;
import turismo.clases.Cliente;
import turismo.clases.EstadoReserva;
import turismo.clases.EstadoSalida;
import turismo.clases.Guia;
import turismo.clases.MedioPago;
import turismo.clases.Reserva;
import turismo.clases.Salida;
import turismo.clases.TipoVehiculo;
import turismo.clases.Tour;
import turismo.clases.Vehiculo;
import turismo.clases.Vendedor;
import turismo.excepciones.CupoExcedidoExcepcion;
import turismo.excepciones.ReglaNegocioExcepcion;
import turismo.interfaces.RepositorioAgencia;
import turismo.persistencia.DatosAgencia;

/**
 * Prueba el controlador con un repositorio en memoria (posible gracias a DIP)
 * y un reloj fijo para controlar la fecha de "hoy".
 */
public class AgenciaControllerTest {

    @Rule
    public TemporaryFolder carpeta = new TemporaryFolder();

    /** Repositorio falso: guarda en memoria y cuenta cuántas veces se guardó. */
    private static class RepositorioEnMemoria implements RepositorioAgencia {
        private DatosAgencia guardados;
        private int vecesGuardado;

        @Override
        public DatosAgencia cargar() {
            return guardados;
        }

        @Override
        public void guardar(DatosAgencia datos) {
            guardados = datos;
            vecesGuardado++;
        }
    }

    private RepositorioEnMemoria repositorio;
    private DatosAgencia datos;
    private Tour tour;
    private Cliente cliente;
    private Vendedor vendedor;

    @Before
    public void preparar() {
        repositorio = new RepositorioEnMemoria();
        datos = new DatosAgencia();
        tour = Escenario.tourNaturaleza(4, 15);
        cliente = Escenario.cliente();
        vendedor = Escenario.vendedor();
        datos.agregarTour(tour);
        datos.agregarCliente(cliente);
        datos.agregarEmpleado(vendedor);
    }

    private AgenciaController controladorEnFecha(LocalDate hoy) {
        Clock reloj = Clock.fixed(hoy.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        AgenciaController controlador = new AgenciaController(repositorio, reloj, carpeta.getRoot().toPath());
        controlador.inicializar(datos);
        return controlador;
    }

    @Test
    public void laPrimeraVezCreaDatosDePruebaYLosGuarda() throws Exception {
        AgenciaController controlador = new AgenciaController(repositorio, Clock.systemDefaultZone(),
                carpeta.getRoot().toPath());
        controlador.inicializar();
        assertEquals(1, repositorio.vecesGuardado);
        assertEquals(3, controlador.getTours().size());
        assertTrue(controlador.getReservas().size() >= 4);
    }

    @Test
    public void losNumerosDeReservaSonCorrelativosAunqueUnaFalle() throws ReglaNegocioExcepcion {
        AgenciaController controlador = controladorEnFecha(LocalDate.of(2026, 4, 1));
        Salida salida = controlador.programarSalida(tour, Escenario.INICIO, 6);
        Reserva primera = controlador.crearReserva(salida, cliente, vendedor, Escenario.adultos(2));
        try {
            controlador.crearReserva(salida, cliente, vendedor, Escenario.adultos(10));
            fail("Debía fallar por cupo");
        } catch (CupoExcedidoExcepcion e) {
            // esperado
        }
        Reserva segunda = controlador.crearReserva(salida, cliente, vendedor, Escenario.adultos(1));
        assertEquals(1, primera.getNumero());
        assertEquals(2, segunda.getNumero());
    }

    @Test
    public void losCodigosDeSalidaNoSeSalteanAunqueFalle() throws ReglaNegocioExcepcion {
        AgenciaController controlador = controladorEnFecha(LocalDate.of(2026, 4, 1));
        Salida primera = controlador.programarSalida(tour, Escenario.INICIO, 6);
        try {
            controlador.programarSalida(tour, Escenario.INICIO, 99);
            fail("Debía fallar por cupo mayor al del tour");
        } catch (ReglaNegocioExcepcion e) {
            // esperado
        }
        Salida segunda = controlador.programarSalida(tour, Escenario.INICIO.plusDays(5), 6);
        assertEquals("SAL-001", primera.getCodigo());
        assertEquals("SAL-002", segunda.getCodigo());
    }

    @Test(expected = ReglaNegocioExcepcion.class)
    public void noSeProgramanSalidasEnElPasado() throws ReglaNegocioExcepcion {
        controladorEnFecha(LocalDate.of(2026, 6, 1)).programarSalida(tour, Escenario.INICIO, 6);
    }

    @Test(expected = ReglaNegocioExcepcion.class)
    public void noSeRegistranDosClientesConElMismoDocumento() throws ReglaNegocioExcepcion {
        controladorEnFecha(LocalDate.of(2026, 4, 1))
                .registrarCliente("Otra", "Persona", cliente.getDocumento(), "", "", "");
    }

    @Test
    public void cancelarUsaLaPoliticaSegunQuienCancela() throws ReglaNegocioExcepcion {
        AgenciaController controlador = controladorEnFecha(LocalDate.of(2026, 4, 25)); // 20 días antes
        Salida salida = controlador.programarSalida(tour, Escenario.INICIO, 6);
        Reserva delCliente = controlador.crearReserva(salida, cliente, vendedor, Escenario.adultos(1));
        controlador.registrarPago(delCliente, 50_000, MedioPago.EFECTIVO, 1);
        Reserva deLaAgencia = controlador.crearReserva(salida, cliente, vendedor, Escenario.adultos(1));
        controlador.registrarPago(deLaAgencia, 50_000, MedioPago.EFECTIVO, 1);

        Cancelacion c1 = controlador.cancelarReserva(delCliente, "Viaje de trabajo", false);
        Cancelacion c2 = controlador.cancelarReserva(deLaAgencia, "Sin vehículo", true);
        assertEquals(25_000, c1.getImporteDevuelto(), 0.01);
        assertEquals(50_000, c2.getImporteDevuelto(), 0.01);
    }

    @Test
    public void procesarVencimientosAnulaSaldosImpagosYCancelaSalidasSinCupoMinimo() throws ReglaNegocioExcepcion {
        AgenciaController alReservar = controladorEnFecha(LocalDate.of(2026, 4, 1));
        Salida salida = alReservar.programarSalida(tour, Escenario.INICIO, 6);
        Reserva conSena = alReservar.crearReserva(salida, cliente, vendedor, Escenario.adultos(2));
        alReservar.registrarPago(conSena, conSena.getMontoSena(), MedioPago.EFECTIVO, 1);
        Reserva pagada = alReservar.crearReserva(salida, cliente, vendedor, Escenario.adultos(1));
        alReservar.registrarPago(pagada, pagada.getTotal(), MedioPago.TRANSFERENCIA, 1);

        // 8 días antes: ya venció el saldo (límite = 10 días antes) pero todavía no se controla el cupo
        List<String> acciones = controladorEnFecha(Escenario.INICIO.minusDays(8)).procesarVencimientos();
        assertEquals(1, acciones.size());
        assertEquals(EstadoReserva.ANULADA, conSena.getEstado());
        assertEquals(0, conSena.getCancelacion().getImporteDevuelto(), 0.01);
        assertEquals(EstadoSalida.PROGRAMADA, salida.getEstado());

        // 7 días antes: solo 1 pasajero de 4 mínimos -> la agencia cancela y devuelve todo
        controladorEnFecha(Escenario.INICIO.minusDays(7)).procesarVencimientos();
        assertEquals(EstadoSalida.CANCELADA, salida.getEstado());
        assertEquals(pagada.getTotalPagado(), pagada.getCancelacion().getImporteDevuelto(), 0.01);
    }

    @Test
    public void unVehiculoNoPuedeEstarEnDosSalidasSuperpuestas() throws ReglaNegocioExcepcion {
        AgenciaController controlador = controladorEnFecha(LocalDate.of(2026, 4, 1));
        Vehiculo combi = new Vehiculo("AC456EF", 15, TipoVehiculo.COMBI);
        Salida primera = controlador.programarSalida(tour, Escenario.INICIO, 6);
        Salida segunda = controlador.programarSalida(tour, Escenario.INICIO.plusDays(1), 6);
        controlador.asignarVehiculo(primera, combi);
        try {
            controlador.asignarVehiculo(segunda, combi);
            fail("La combi ya está ocupada");
        } catch (ReglaNegocioExcepcion e) {
            assertTrue(e.getMessage().contains(primera.getCodigo()));
        }
        assertSame(combi, primera.getVehiculo());
    }

    @Test
    public void laListaDePasajerosSeGuardaEnUnArchivo() throws Exception {
        AgenciaController controlador = controladorEnFecha(LocalDate.of(2026, 4, 1));
        Salida salida = controlador.programarSalida(tour, Escenario.INICIO, 6);
        Reserva reserva = controlador.crearReserva(salida, cliente, vendedor, Escenario.adultos(4));
        controlador.registrarPago(reserva, reserva.getMontoSena(), MedioPago.EFECTIVO, 1);
        Guia guia = Escenario.guia(CategoriaTour.NATURALEZA);
        controlador.asignarGuia(salida, guia);

        Path archivo = controlador.emitirListaPasajeros(salida);
        assertTrue(Files.exists(archivo));
        List<String> lineas = Files.readAllLines(archivo);
        assertTrue(lineas.get(0).contains(salida.getCodigo()));
        assertTrue(lineas.get(lineas.size() - 1).contains("4"));
        controlador.iniciarSalida(salida); // con lista y guía ya se puede iniciar
        assertEquals(EstadoSalida.EN_CURSO, salida.getEstado());
    }

    @Test
    public void exportarUnReporteGeneraUnArchivoDeTexto() throws Exception {
        AgenciaController controlador = controladorEnFecha(LocalDate.of(2026, 4, 1));
        Path archivo = controlador.exportarReporte(controlador.reporteOcupacion());
        assertNotNull(archivo);
        assertEquals("Ocupación por salida", Files.readAllLines(archivo).get(0));
    }
}