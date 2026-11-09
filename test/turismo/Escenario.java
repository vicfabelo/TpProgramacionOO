package turismo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import turismo.clases.CategoriaTour;
import turismo.clases.Cliente;
import turismo.clases.Guia;
import turismo.clases.NivelDificultad;
import turismo.clases.Pasajero;
import turismo.clases.PromocionGrupo;
import turismo.clases.PromocionReservaAnticipada;
import turismo.clases.Reserva;
import turismo.clases.Salida;
import turismo.clases.ServicioAdicional;
import turismo.clases.Tarifa;
import turismo.clases.Temporada;
import turismo.clases.TipoPasajero;
import turismo.clases.TipoServicio;
import turismo.clases.Tour;
import turismo.clases.TourAventura;
import turismo.clases.TourCultural;
import turismo.clases.TourNaturaleza;
import turismo.clases.Vendedor;
import turismo.excepciones.ReglaNegocioExcepcion;
import turismo.interfaces.Promocion;

/**
 * Datos fijos para las pruebas. Todas las fechas son fijas para que los resultados
 * no dependan del día en que se ejecutan los tests.
 */
public final class Escenario {

    /** Mayo = temporada BAJA. */
    public static final LocalDate INICIO = LocalDate.of(2026, 5, 15);
    /** 30 días antes de la salida: no aplica la promoción por reserva anticipada. */
    public static final LocalDate FECHA_RESERVA = INICIO.minusDays(30);
    public static final LocalDate VIGENCIA = LocalDate.of(2026, 1, 1);
    public static final double TARIFA_BAJA = 100_000;
    public static final double TARIFA_MEDIA = 120_000;
    public static final double TARIFA_ALTA = 150_000;
    public static final double PRECIO_SEGURO = 5_000;
    public static final List<Promocion> PROMOCIONES = List.of(new PromocionReservaAnticipada(), new PromocionGrupo());

    private static int contadorDocumentos = 1;

    private Escenario() {
    }

    public static TourNaturaleza tourNaturaleza(int cupoMinimo, int cupoMaximo) {
        TourNaturaleza tour = new TourNaturaleza("NAT-T", "Talampaya test", "", 3, cupoMinimo, cupoMaximo, "Talampaya");
        cargarTarifas(tour);
        tour.agregarServicio(new ServicioAdicional("Traslado", TipoServicio.TRASLADO, 8_000, false));
        return tour;
    }

    /** Aventura de 2 días, cupo 2 a 10, edad mínima 14, con seguro opcional en el catálogo. */
    public static TourAventura tourAventura() {
        TourAventura tour = new TourAventura("AVE-T", "Rafting test", "", 2, 2, 10, NivelDificultad.MEDIO, 14);
        cargarTarifas(tour);
        tour.agregarServicio(new ServicioAdicional("Seguro", TipoServicio.SEGURO_VIAJE, PRECIO_SEGURO, false));
        return tour;
    }

    public static TourCultural tourCultural() {
        TourCultural tour = new TourCultural("CUL-T", "Bodegas test", "", 1, 2, 20, "Vino");
        cargarTarifas(tour);
        tour.agregarServicio(new ServicioAdicional("Seguro", TipoServicio.SEGURO_VIAJE, PRECIO_SEGURO, false));
        return tour;
    }

    public static void cargarTarifas(Tour tour) {
        tour.agregarTarifa(new Tarifa(Temporada.BAJA, VIGENCIA, TARIFA_BAJA));
        tour.agregarTarifa(new Tarifa(Temporada.MEDIA, VIGENCIA, TARIFA_MEDIA));
        tour.agregarTarifa(new Tarifa(Temporada.ALTA, VIGENCIA, TARIFA_ALTA));
    }

    public static Salida salida(Tour tour, int cupo) throws ReglaNegocioExcepcion {
        return new Salida("SAL-T", tour, INICIO, cupo);
    }

    public static Cliente cliente() {
        return new Cliente("Ana", "Titular", "11111111", "111", "ana@mail.com", "Argentina");
    }

    public static Vendedor vendedor() {
        return new Vendedor("V-T", "Vera", "Vendedora", "22222222", "222", 0.03);
    }

    public static Guia guia(CategoriaTour... categorias) {
        return new Guia("G-" + contadorDocumentos, "Gabi", "Guía" + contadorDocumentos, "G" + contadorDocumentos++,
                "333", List.of("Español"), List.of(categorias), 40_000);
    }

    public static Pasajero adulto(String apellido) {
        return conEdad(apellido, 35);
    }

    /** Pasajero que tiene exactamente esa edad el día de inicio de la salida. */
    public static Pasajero conEdad(String apellido, int edadAlInicio) {
        return new Pasajero("P", apellido, "D" + contadorDocumentos++, INICIO.minusYears(edadAlInicio).minusDays(10),
                TipoPasajero.ADULTO, "Contacto 123", "");
    }

    public static Pasajero jubilado(String apellido) {
        return new Pasajero("J", apellido, "D" + contadorDocumentos++, LocalDate.of(1955, 1, 1),
                TipoPasajero.JUBILADO, "Contacto 123", "");
    }

    public static List<Pasajero> adultos(int cantidad) {
        List<Pasajero> pasajeros = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            pasajeros.add(adulto("Adulto" + (char) ('A' + i)));
        }
        return pasajeros;
    }

    public static Reserva reservar(Salida salida, List<Pasajero> pasajeros, LocalDate fecha) throws ReglaNegocioExcepcion {
        return salida.crearReserva(1, cliente(), vendedor(), pasajeros, fecha, PROMOCIONES);
    }

    public static Reserva reservar(Salida salida, int adultos) throws ReglaNegocioExcepcion {
        return reservar(salida, adultos(adultos), FECHA_RESERVA);
    }
}