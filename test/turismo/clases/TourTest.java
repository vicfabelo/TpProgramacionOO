package turismo.clases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.Test;

import turismo.Escenario;
import turismo.excepciones.PasajeroNoValidoExcepcion;
import turismo.excepciones.ReglaNegocioExcepcion;

public class TourTest {

    @Test
    public void elItinerarioQuedaOrdenadoPorDiaYOrden() throws ReglaNegocioExcepcion {
        TourNaturaleza tour = Escenario.tourNaturaleza(2, 10);
        Actividad dia2 = new Actividad(2, 1, LocalTime.NOON, "Cañón", "Talampaya", TipoActividad.VISITA, null);
        Actividad dia1Segunda = new Actividad(1, 2, LocalTime.NOON, "Almuerzo", "Villa Unión", TipoActividad.COMIDA, null);
        Actividad dia1Primera = new Actividad(1, 1, LocalTime.of(8, 0), "Traslado", "Chilecito", TipoActividad.TRASLADO, null);
        tour.agregarActividad(dia2);
        tour.agregarActividad(dia1Segunda);
        tour.agregarActividad(dia1Primera);
        assertEquals(List.of(dia1Primera, dia1Segunda, dia2), tour.getItinerario());
    }

    @Test(expected = ReglaNegocioExcepcion.class)
    public void noSeAceptaUnaActividadFueraDeLaDuracion() throws ReglaNegocioExcepcion {
        Escenario.tourNaturaleza(2, 10).agregarActividad(
                new Actividad(4, 1, LocalTime.NOON, "Extra", "", TipoActividad.VISITA, null));
    }

    @Test
    public void elPrecioDependeDeLaTemporadaDeLaSalida() throws ReglaNegocioExcepcion {
        Tour tour = Escenario.tourNaturaleza(2, 10);
        LocalDate reserva = LocalDate.of(2026, 3, 1);
        assertEquals(Escenario.TARIFA_BAJA, tour.obtenerPrecioBase(LocalDate.of(2026, 5, 20), reserva), 0.001);
        assertEquals(Escenario.TARIFA_MEDIA, tour.obtenerPrecioBase(LocalDate.of(2026, 4, 20), reserva), 0.001);
        assertEquals(Escenario.TARIFA_ALTA, tour.obtenerPrecioBase(LocalDate.of(2026, 7, 20), reserva), 0.001);
    }

    @Test
    public void seUsaLaTarifaVigenteMasRecienteALaFechaDeReserva() throws ReglaNegocioExcepcion {
        Tour tour = Escenario.tourNaturaleza(2, 10);
        tour.agregarTarifa(new Tarifa(Temporada.BAJA, LocalDate.of(2026, 4, 1), 130_000));
        LocalDate salida = LocalDate.of(2026, 5, 20);
        assertEquals(100_000, tour.obtenerPrecioBase(salida, LocalDate.of(2026, 3, 31)), 0.001);
        assertEquals(130_000, tour.obtenerPrecioBase(salida, LocalDate.of(2026, 4, 1)), 0.001);
    }

    @Test(expected = ReglaNegocioExcepcion.class)
    public void sinTarifaVigenteNoHayPrecio() throws ReglaNegocioExcepcion {
        Escenario.tourNaturaleza(2, 10).obtenerPrecioBase(LocalDate.of(2026, 5, 20), LocalDate.of(2025, 12, 1));
    }

    @Test
    public void aventuraRechazaPasajerosMenoresALaEdadMinima() {
        TourAventura tour = Escenario.tourAventura();
        try {
            tour.validarPasajero(Escenario.conEdad("Chico", 13), Escenario.INICIO);
            fail("Debía rechazar a un pasajero de 13 años");
        } catch (PasajeroNoValidoExcepcion e) {
            assertTrue(e.getMessage().contains("14"));
        }
    }

    @Test
    public void aventuraAceptaPasajerosConLaEdadMinima() throws PasajeroNoValidoExcepcion {
        Escenario.tourAventura().validarPasajero(Escenario.conEdad("Justo", 14), Escenario.INICIO);
    }

    @Test
    public void elSeguroEsObligatorioSoloEnAventura() {
        TourAventura aventura = Escenario.tourAventura();
        TourCultural cultural = Escenario.tourCultural();
        ServicioAdicional seguroAventura = aventura.getServiciosDisponibles().get(0);
        ServicioAdicional seguroCultural = cultural.getServiciosDisponibles().get(0);
        assertTrue(aventura.esObligatorio(seguroAventura));
        assertFalse(cultural.esObligatorio(seguroCultural));
    }

    @Test(expected = ReglaNegocioExcepcion.class)
    public void noSeProgramaUnaAventuraSinSeguro() throws ReglaNegocioExcepcion {
        TourAventura sinSeguro = new TourAventura("AVE-X", "Sin seguro", "", 1, 2, 10, NivelDificultad.ALTO, 16);
        Escenario.cargarTarifas(sinSeguro);
        new Salida("SAL-X", sinSeguro, Escenario.INICIO, 10);
    }

    @Test(expected = ReglaNegocioExcepcion.class)
    public void elCupoDeLaSalidaNoPuedeSuperarAlDelTour() throws ReglaNegocioExcepcion {
        new Salida("SAL-X", Escenario.tourNaturaleza(2, 10), Escenario.INICIO, 11);
    }

    @Test
    public void laTemporadaSeDeterminaPorElMes() {
        assertEquals(Temporada.ALTA, Temporada.desde(LocalDate.of(2026, 1, 10)));
        assertEquals(Temporada.ALTA, Temporada.desde(LocalDate.of(2026, 7, 10)));
        assertEquals(Temporada.MEDIA, Temporada.desde(LocalDate.of(2026, 10, 10)));
        assertEquals(Temporada.BAJA, Temporada.desde(LocalDate.of(2026, 6, 10)));
    }
}