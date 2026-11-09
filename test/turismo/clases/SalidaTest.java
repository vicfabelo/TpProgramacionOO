package turismo.clases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static turismo.Escenario.FECHA_RESERVA;
import static turismo.Escenario.INICIO;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import turismo.Escenario;
import turismo.excepciones.CupoExcedidoExcepcion;
import turismo.excepciones.EstadoInvalidoExcepcion;
import turismo.excepciones.GuiaNoDisponibleExcepcion;
import turismo.excepciones.PasajeroNoValidoExcepcion;
import turismo.excepciones.ReglaNegocioExcepcion;

public class SalidaTest {

    private static final double DELTA = 0.01;

    /** Tour de 3 días, cupo mínimo 4. Salida con cupo 6. */
    private Salida salida;

    @Before
    public void prepararSalida() throws ReglaNegocioExcepcion {
        salida = Escenario.salida(Escenario.tourNaturaleza(4, 15), 6);
    }

    private Reserva reservaConSena(int pasajeros) throws ReglaNegocioExcepcion {
        Reserva reserva = Escenario.reservar(salida, pasajeros);
        reserva.registrarPago(FECHA_RESERVA, reserva.getMontoSena(), MedioPago.EFECTIVO, 1);
        return reserva;
    }

    // ------------------------------------------------------------ cupo y estado

    @Test(expected = CupoExcedidoExcepcion.class)
    public void noSePuedeReservarMasLugaresQueLosDisponibles() throws ReglaNegocioExcepcion {
        Escenario.reservar(salida, 4);
        Escenario.reservar(salida, 3);
    }

    @Test
    public void seConfirmaCuandoLosPasajerosConSenaLleganAlCupoMinimo() throws ReglaNegocioExcepcion {
        reservaConSena(3);
        assertEquals(EstadoSalida.PROGRAMADA, salida.getEstado());
        reservaConSena(1);
        assertEquals(EstadoSalida.CONFIRMADA, salida.getEstado());
    }

    @Test
    public void lasReservasSinSenaNoConfirmanLaSalida() throws ReglaNegocioExcepcion {
        Escenario.reservar(salida, 5);
        assertEquals(EstadoSalida.PROGRAMADA, salida.getEstado());
    }

    @Test
    public void alLlegarAlCupoMaximoQuedaCompletaYNoAdmiteMasReservas() throws ReglaNegocioExcepcion {
        reservaConSena(6);
        assertEquals(EstadoSalida.COMPLETA, salida.getEstado());
        try {
            Escenario.reservar(salida, 1);
            fail("Una salida completa no admite reservas");
        } catch (EstadoInvalidoExcepcion e) {
            assertTrue(e.getMessage().contains("COMPLETA"));
        }
    }

    @Test
    public void siSeLiberaLugarUnaSalidaCompletaVuelveAConfirmada() throws ReglaNegocioExcepcion {
        reservaConSena(4);
        Reserva otra = reservaConSena(2);
        assertEquals(EstadoSalida.COMPLETA, salida.getEstado());
        otra.cancelar(FECHA_RESERVA, "", new PoliticaCancelacionCliente());
        assertEquals(EstadoSalida.CONFIRMADA, salida.getEstado());
    }

    @Test(expected = PasajeroNoValidoExcepcion.class)
    public void laSalidaValidaLasCondicionesDelTourAlReservar() throws ReglaNegocioExcepcion {
        Salida aventura = Escenario.salida(Escenario.tourAventura(), 10);
        Escenario.reservar(aventura, List.of(Escenario.adulto("Papá"), Escenario.conEdad("Hijo", 10)), FECHA_RESERVA);
    }

    @Test
    public void seControlaElCupoMinimoSieteDiasAntes() throws ReglaNegocioExcepcion {
        Escenario.reservar(salida, 2);
        assertFalse(salida.debeCancelarsePorCupoMinimo(INICIO.minusDays(8)));
        assertTrue(salida.debeCancelarsePorCupoMinimo(INICIO.minusDays(7)));
    }

    @Test
    public void siLaCancelaLaAgenciaSeDevuelveElCienPorCientoATodos() throws ReglaNegocioExcepcion {
        Reserva reserva = reservaConSena(2);
        double pagado = reserva.getTotalPagado();
        salida.cancelarPorAgencia(INICIO.minusDays(7), "Cupo mínimo no alcanzado");
        assertEquals(EstadoSalida.CANCELADA, salida.getEstado());
        assertEquals(EstadoReserva.ANULADA, reserva.getEstado());
        assertEquals(pagado, reserva.getCancelacion().getImporteDevuelto(), DELTA);
    }

    // ------------------------------------------------------------ guía y vehículo

    @Test(expected = GuiaNoDisponibleExcepcion.class)
    public void elGuiaTieneQueEstarHabilitadoParaLaCategoria() throws ReglaNegocioExcepcion {
        salida.asignarGuia(Escenario.guia(CategoriaTour.CULTURAL));
    }

    @Test
    public void unGuiaNoPuedeTenerDosSalidasSuperpuestas() throws ReglaNegocioExcepcion {
        Guia guia = Escenario.guia(CategoriaTour.NATURALEZA);
        salida.asignarGuia(guia);
        Salida superpuesta = new Salida("SAL-2", salida.getTour(), INICIO.plusDays(2), 6);
        try {
            superpuesta.asignarGuia(guia);
            fail("El guía ya tiene una salida en esas fechas");
        } catch (GuiaNoDisponibleExcepcion e) {
            assertTrue(e.getMessage().contains("otra salida"));
        }
        Salida posterior = new Salida("SAL-3", salida.getTour(), INICIO.plusDays(3), 6);
        posterior.asignarGuia(guia); // empieza el día siguiente al fin: no se superpone
        assertEquals(2, guia.getSalidasAsignadas().size());
    }

    @Test(expected = ReglaNegocioExcepcion.class)
    public void laCapacidadDelVehiculoNoPuedeSerMenorAlCupo() throws ReglaNegocioExcepcion {
        salida.asignarVehiculo(new Vehiculo("AA111AA", 5, TipoVehiculo.CAMIONETA));
    }

    // ------------------------------------------------------------ operación y liquidación

    @Test(expected = EstadoInvalidoExcepcion.class)
    public void noSeIniciaSinGuia() throws ReglaNegocioExcepcion {
        reservaConSena(4);
        salida.emitirListaPasajeros();
        salida.iniciar();
    }

    @Test(expected = EstadoInvalidoExcepcion.class)
    public void noSeIniciaSinListaDePasajeros() throws ReglaNegocioExcepcion {
        reservaConSena(4);
        salida.asignarGuia(Escenario.guia(CategoriaTour.NATURALEZA));
        salida.iniciar();
    }

    @Test
    public void laListaIncluyeSoloReservasConfirmadasOrdenadasPorApellido() throws ReglaNegocioExcepcion {
        Reserva confirmada = Escenario.reservar(salida, List.of(Escenario.adulto("Zapata"), Escenario.adulto("Acosta"),
                Escenario.adulto("Molina"), Escenario.adulto("Benítez")), FECHA_RESERVA);
        confirmada.registrarPago(FECHA_RESERVA, confirmada.getMontoSena(), MedioPago.EFECTIVO, 1);
        Escenario.reservar(salida, List.of(Escenario.adulto("Pendiente")), FECHA_RESERVA);
        List<Pasajero> lista = salida.emitirListaPasajeros();
        assertEquals(4, lista.size());
        assertEquals("Acosta", lista.get(0).getApellido());
        assertEquals("Zapata", lista.get(3).getApellido());
        assertTrue(salida.isListaPasajerosEmitida());
    }

    @Test
    public void flujoCompletoYLiquidacionDelGuia() throws ReglaNegocioExcepcion {
        Reserva reserva = reservaConSena(4);
        ServicioAdicional traslado = salida.getTour().getServiciosDisponibles().get(0); // $8.000
        reserva.contratarServicio(reserva.getPasajeros().get(0), traslado);
        reserva.contratarServicio(reserva.getPasajeros().get(1), traslado);
        Guia guia = Escenario.guia(CategoriaTour.NATURALEZA); // $40.000 por día
        salida.asignarGuia(guia);
        salida.emitirListaPasajeros();
        salida.iniciar();
        assertEquals(EstadoSalida.EN_CURSO, salida.getEstado());
        salida.finalizar();
        assertEquals(EstadoSalida.FINALIZADA, salida.getEstado());
        assertEquals(EstadoReserva.FINALIZADA, reserva.getEstado());

        Liquidacion liquidacion = salida.liquidarHonorariosGuia(INICIO.plusDays(4));
        assertEquals(3 * 40_000, liquidacion.getMontoFijo(), DELTA);
        assertEquals(0.05 * 16_000, liquidacion.getComisionAdicionales(), DELTA);
        assertEquals(120_800, liquidacion.getTotal(), DELTA);
    }

    @Test(expected = EstadoInvalidoExcepcion.class)
    public void soloSeLiquidanSalidasFinalizadas() throws ReglaNegocioExcepcion {
        salida.liquidarHonorariosGuia(INICIO);
    }
}