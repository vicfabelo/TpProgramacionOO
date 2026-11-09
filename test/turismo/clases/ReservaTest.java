package turismo.clases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static turismo.Escenario.FECHA_RESERVA;
import static turismo.Escenario.INICIO;
import static turismo.Escenario.TARIFA_BAJA;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import turismo.Escenario;
import turismo.excepciones.EstadoInvalidoExcepcion;
import turismo.excepciones.PasajeroNoValidoExcepcion;
import turismo.excepciones.ReglaNegocioExcepcion;

public class ReservaTest {

    private static final double DELTA = 0.01;

    private Salida salida;

    @Before
    public void prepararSalida() throws ReglaNegocioExcepcion {
        salida = Escenario.salida(Escenario.tourNaturaleza(2, 15), 15);
    }

    // ------------------------------------------------------------ precio

    @Test
    public void elTotalSumaLaTarifaDeCadaPasajeroSegunSuTipo() throws ReglaNegocioExcepcion {
        Reserva reserva = Escenario.reservar(salida, List.of(Escenario.adulto("A"), Escenario.conEdad("Chico", 8),
                Escenario.conEdad("Bebe", 1), Escenario.jubilado("J")), FECHA_RESERVA);
        // 100 % + 60 % + 0 % + 90 % = 2,5 tarifas
        assertEquals(TARIFA_BAJA * 2.5, reserva.getTotal(), DELTA);
        assertEquals(4, reserva.getCantidadPasajeros());
    }

    @Test
    public void reservaConMasDeSesentaDiasTieneQuincePorCiento() throws ReglaNegocioExcepcion {
        Reserva reserva = Escenario.reservar(salida, Escenario.adultos(2), INICIO.minusDays(61));
        assertEquals("Reserva anticipada", reserva.getPromocionAplicada());
        assertEquals(TARIFA_BAJA * 2 * 0.85, reserva.getTotal(), DELTA);
    }

    @Test
    public void conExactamenteSesentaDiasNoHayPromocion() throws ReglaNegocioExcepcion {
        Reserva reserva = Escenario.reservar(salida, Escenario.adultos(2), INICIO.minusDays(60));
        assertEquals("", reserva.getPromocionAplicada());
        assertEquals(TARIFA_BAJA * 2, reserva.getTotal(), DELTA);
    }

    @Test
    public void grupoDeSeisPasajerosTieneDiezPorCiento() throws ReglaNegocioExcepcion {
        Reserva reserva = Escenario.reservar(salida, 6);
        assertEquals("Grupo", reserva.getPromocionAplicada());
        assertEquals(TARIFA_BAJA * 6 * 0.90, reserva.getTotal(), DELTA);
    }

    @Test
    public void lasPromocionesNoSeAcumulanYSeAplicaLaMasFavorable() throws ReglaNegocioExcepcion {
        Reserva reserva = Escenario.reservar(salida, Escenario.adultos(6), INICIO.minusDays(90));
        assertEquals("Reserva anticipada", reserva.getPromocionAplicada());
        assertEquals(TARIFA_BAJA * 6 * 0.85, reserva.getTotal(), DELTA);
    }

    @Test
    public void laPromocionNoSeAplicaALosAdicionales() throws ReglaNegocioExcepcion {
        Reserva reserva = Escenario.reservar(salida, Escenario.adultos(1), INICIO.minusDays(90));
        ServicioAdicional traslado = salida.getTour().getServiciosDisponibles().get(0);
        reserva.contratarServicio(reserva.getPasajeros().get(0), traslado);
        assertEquals(TARIFA_BAJA * 0.85 + 8_000, reserva.getTotal(), DELTA);
    }

    @Test
    public void enAventuraElSeguroSeCargaSoloACadaPasajero() throws ReglaNegocioExcepcion {
        Salida aventura = Escenario.salida(Escenario.tourAventura(), 10);
        Reserva reserva = Escenario.reservar(aventura, 3);
        assertEquals(3 * Escenario.PRECIO_SEGURO, reserva.getTotalAdicionales(), DELTA);
        for (Pasajero pasajero : reserva.getPasajeros()) {
            assertEquals(1, pasajero.getServicios().size());
        }
    }

    @Test(expected = ReglaNegocioExcepcion.class)
    public void noSePuedeContratarDosVecesElMismoServicio() throws ReglaNegocioExcepcion {
        Reserva reserva = Escenario.reservar(salida, 1);
        ServicioAdicional traslado = salida.getTour().getServiciosDisponibles().get(0);
        reserva.contratarServicio(reserva.getPasajeros().get(0), traslado);
        reserva.contratarServicio(reserva.getPasajeros().get(0), traslado);
    }

    // ------------------------------------------------------------ pagos

    @Test
    public void laSenaDelTreintaPorCientoConfirmaLaReserva() throws ReglaNegocioExcepcion {
        Reserva reserva = Escenario.reservar(salida, 2); // total 200.000, seña 60.000
        reserva.registrarPago(FECHA_RESERVA, 59_000, MedioPago.EFECTIVO, 1);
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        reserva.registrarPago(FECHA_RESERVA, 1_000, MedioPago.TRANSFERENCIA, 1);
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertEquals(140_000, reserva.getSaldo(), DELTA);
    }

    @Test
    public void tarjetaEnCuotasTieneRecargoQueNoDescuentaSaldo() throws ReglaNegocioExcepcion {
        Reserva reserva = Escenario.reservar(salida, 1);
        Pago pago = reserva.registrarPago(FECHA_RESERVA, 50_000, MedioPago.TARJETA, 3);
        assertEquals(3_500, pago.getRecargo(), DELTA);
        assertEquals(53_500, pago.getImporteCobrado(), DELTA);
        assertEquals(50_000, reserva.getSaldo(), DELTA);
    }

    @Test
    public void tarjetaEnUnPagoNoTieneRecargo() throws ReglaNegocioExcepcion {
        Reserva reserva = Escenario.reservar(salida, 1);
        assertEquals(0, reserva.registrarPago(FECHA_RESERVA, 50_000, MedioPago.TARJETA, 1).getRecargo(), DELTA);
    }

    @Test(expected = ReglaNegocioExcepcion.class)
    public void soloConTarjetaSePagaEnCuotas() throws ReglaNegocioExcepcion {
        Escenario.reservar(salida, 1).registrarPago(FECHA_RESERVA, 10_000, MedioPago.TRANSFERENCIA, 3);
    }

    @Test(expected = ReglaNegocioExcepcion.class)
    public void noSePuedePagarMasQueElSaldo() throws ReglaNegocioExcepcion {
        Escenario.reservar(salida, 1).registrarPago(FECHA_RESERVA, 100_001, MedioPago.EFECTIVO, 1);
    }

    @Test
    public void desdeLaFechaLimiteSoloSeAceptaElSaldoCompleto() throws ReglaNegocioExcepcion {
        Reserva reserva = Escenario.reservar(salida, 1);
        reserva.registrarPago(FECHA_RESERVA, 30_000, MedioPago.EFECTIVO, 1);
        try {
            reserva.registrarPago(INICIO.minusDays(10), 10_000, MedioPago.EFECTIVO, 1);
            fail("Debía exigir el saldo completo");
        } catch (ReglaNegocioExcepcion e) {
            assertTrue(e.getMessage().contains("saldo completo"));
        }
        reserva.registrarPago(INICIO.minusDays(10), 70_000, MedioPago.EFECTIVO, 1);
        assertEquals(0, reserva.getSaldo(), DELTA);
    }

    @Test
    public void elSaldoVencePasadaLaFechaLimite() throws ReglaNegocioExcepcion {
        Reserva reserva = Escenario.reservar(salida, 1);
        reserva.registrarPago(FECHA_RESERVA, 30_000, MedioPago.EFECTIVO, 1);
        assertFalse(reserva.tieneSaldoVencido(INICIO.minusDays(10)));
        assertTrue(reserva.tieneSaldoVencido(INICIO.minusDays(9)));
        Cancelacion cancelacion = reserva.anularPorFaltaDePago(INICIO.minusDays(9));
        assertEquals(EstadoReserva.ANULADA, reserva.getEstado());
        assertEquals(0, cancelacion.getImporteDevuelto(), DELTA);
    }

    // ------------------------------------------------------------ cancelaciones

    @Test
    public void politicaDelClienteSegunDiasDeAnticipacion() {
        PoliticaCancelacionCliente politica = new PoliticaCancelacionCliente();
        assertEquals(0.90, politica.calcularPorcentajeDevolucion(45), DELTA);
        assertEquals(0.90, politica.calcularPorcentajeDevolucion(30), DELTA);
        assertEquals(0.50, politica.calcularPorcentajeDevolucion(29), DELTA);
        assertEquals(0.50, politica.calcularPorcentajeDevolucion(15), DELTA);
        assertEquals(0.20, politica.calcularPorcentajeDevolucion(14), DELTA);
        assertEquals(0.20, politica.calcularPorcentajeDevolucion(7), DELTA);
        assertEquals(0.0, politica.calcularPorcentajeDevolucion(6), DELTA);
    }

    @Test
    public void laDevolucionSeHacePorElMismoMedioDeCadaPago() throws ReglaNegocioExcepcion {
        Reserva reserva = Escenario.reservar(salida, 1);
        reserva.registrarPago(FECHA_RESERVA, 30_000, MedioPago.TRANSFERENCIA, 1);
        reserva.registrarPago(FECHA_RESERVA, 20_000, MedioPago.TARJETA, 1);
        Cancelacion cancelacion = reserva.cancelar(INICIO.minusDays(20), "Enfermedad", new PoliticaCancelacionCliente());
        assertEquals(15_000, cancelacion.getDevolucionesPorMedio().get(MedioPago.TRANSFERENCIA), DELTA);
        assertEquals(10_000, cancelacion.getDevolucionesPorMedio().get(MedioPago.TARJETA), DELTA);
        assertEquals(25_000, cancelacion.getImporteDevuelto(), DELTA);
    }

    @Test
    public void siCancelaLaAgenciaSeDevuelveTodo() throws ReglaNegocioExcepcion {
        Reserva reserva = Escenario.reservar(salida, 1);
        reserva.registrarPago(FECHA_RESERVA, 40_000, MedioPago.EFECTIVO, 1);
        Cancelacion cancelacion = reserva.cancelar(INICIO.minusDays(2), "Clima", new PoliticaCancelacionAgencia());
        assertEquals(40_000, cancelacion.getImporteDevuelto(), DELTA);
    }

    @Test
    public void cancelarLiberaLosLugares() throws ReglaNegocioExcepcion {
        Reserva reserva = Escenario.reservar(salida, 4);
        assertEquals(11, salida.getLugaresDisponibles());
        reserva.cancelar(FECHA_RESERVA, "", new PoliticaCancelacionCliente());
        assertEquals(15, salida.getLugaresDisponibles());
    }

    @Test(expected = EstadoInvalidoExcepcion.class)
    public void noSePuedeCancelarDosVeces() throws ReglaNegocioExcepcion {
        Reserva reserva = Escenario.reservar(salida, 1);
        reserva.cancelar(FECHA_RESERVA, "", new PoliticaCancelacionCliente());
        reserva.cancelar(FECHA_RESERVA, "", new PoliticaCancelacionCliente());
    }

    // ------------------------------------------------------------ modificaciones

    @Test
    public void seReemplazaUnPasajeroHastaTresDiasAntes() throws ReglaNegocioExcepcion {
        Reserva reserva = Escenario.reservar(salida, 2);
        Pasajero saliente = reserva.getPasajeros().get(0);
        Pasajero nuevo = Escenario.adulto("Nuevo");
        double totalAntes = reserva.getTotal();
        reserva.reemplazarPasajero(saliente, nuevo, INICIO.minusDays(3));
        assertSame(nuevo, reserva.getPasajeros().get(0));
        assertEquals("el reemplazo es sin cargo", totalAntes, reserva.getTotal(), DELTA);
    }

    @Test(expected = ReglaNegocioExcepcion.class)
    public void conMenosDeTresDiasNoSeReemplaza() throws ReglaNegocioExcepcion {
        Reserva reserva = Escenario.reservar(salida, 2);
        reserva.reemplazarPasajero(reserva.getPasajeros().get(0), Escenario.adulto("Tarde"), INICIO.minusDays(2));
    }

    @Test(expected = PasajeroNoValidoExcepcion.class)
    public void elReemplazoTieneQueCumplirLasCondicionesDelTour() throws ReglaNegocioExcepcion {
        Salida aventura = Escenario.salida(Escenario.tourAventura(), 10);
        Reserva reserva = Escenario.reservar(aventura, 2);
        reserva.reemplazarPasajero(reserva.getPasajeros().get(0), Escenario.conEdad("Chico", 12), FECHA_RESERVA);
    }

    @Test
    public void lasCorreccionesSeHacenConNotasDeAjuste() throws ReglaNegocioExcepcion {
        Reserva reserva = Escenario.reservar(salida, 1);
        reserva.registrarAjuste(FECHA_RESERVA, "Bonificación por demora", -5_000);
        assertEquals(95_000, reserva.getTotal(), DELTA);
        assertEquals(1, reserva.getAjustes().size());
    }

    @Test(expected = ReglaNegocioExcepcion.class)
    public void unaNotaDeAjusteNecesitaMotivo() throws ReglaNegocioExcepcion {
        Escenario.reservar(salida, 1).registrarAjuste(FECHA_RESERVA, " ", 1_000);
    }
}