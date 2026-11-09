package turismo.clases;

import static org.junit.Assert.assertEquals;
import static turismo.Escenario.INICIO;

import java.time.LocalDate;

import org.junit.Test;

import turismo.Escenario;

public class PasajeroTest {

    private static final double DELTA = 0.0001;

    @Test
    public void adultoPagaElCienPorCiento() {
        assertEquals(1.0, Escenario.adulto("A").calcularFactorTarifa(INICIO), DELTA);
    }

    @Test
    public void menorDeDoceAniosPagaSesentaPorCiento() {
        assertEquals(0.60, Escenario.conEdad("Chico", 8).calcularFactorTarifa(INICIO), DELTA);
        assertEquals(0.60, Escenario.conEdad("Chico", 11).calcularFactorTarifa(INICIO), DELTA);
    }

    @Test
    public void menorDeTresAniosNoPaga() {
        assertEquals(0.0, Escenario.conEdad("Bebe", 2).calcularFactorTarifa(INICIO), DELTA);
    }

    @Test
    public void conTresAniosYaPagaComoMenor() {
        assertEquals(0.60, Escenario.conEdad("Nene", 3).calcularFactorTarifa(INICIO), DELTA);
    }

    @Test
    public void jubiladoTieneDiezPorCientoDeDescuento() {
        assertEquals(0.90, Escenario.jubilado("Abuelo").calcularFactorTarifa(INICIO), DELTA);
    }

    @Test
    public void laEdadSeCalculaALaFechaDeLaSalida() {
        // cumple 12 un día antes de la salida: ya paga la tarifa completa
        Pasajero pasajero = new Pasajero("C", "Cumple", "X1", INICIO.minusYears(12).minusDays(1),
                TipoPasajero.MENOR, "Contacto", "");
        assertEquals(1.0, pasajero.calcularFactorTarifa(INICIO), DELTA);
        assertEquals(0.60, pasajero.calcularFactorTarifa(INICIO.minusDays(2)), DELTA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sinContactoDeEmergenciaNoSePuedeCrear() {
        new Pasajero("A", "B", "123", LocalDate.of(1990, 1, 1), TipoPasajero.ADULTO, " ", "");
    }

    @Test
    public void comparadorOrdenaPorApellidoYNombre() {
        Pasajero perezAna = new Pasajero("Ana", "Pérez", "1", LocalDate.of(1990, 1, 1), TipoPasajero.ADULTO, "c", "");
        Pasajero gomez = new Pasajero("Zoe", "Gómez", "2", LocalDate.of(1990, 1, 1), TipoPasajero.ADULTO, "c", "");
        Pasajero perezBeto = new Pasajero("Beto", "Pérez", "3", LocalDate.of(1990, 1, 1), TipoPasajero.ADULTO, "c", "");
        java.util.List<Pasajero> lista = new java.util.ArrayList<>(java.util.List.of(perezBeto, perezAna, gomez));
        lista.sort(Pasajero.POR_APELLIDO_Y_NOMBRE);
        assertEquals(java.util.List.of(gomez, perezAna, perezBeto), lista);
    }
}