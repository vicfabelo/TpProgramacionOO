package turismo.persistencia;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import turismo.clases.Administrador;
import turismo.clases.CategoriaTour;
import turismo.clases.Cliente;
import turismo.clases.Coordinador;
import turismo.clases.Guia;
import turismo.clases.MedioPago;
import turismo.clases.NivelDificultad;
import turismo.clases.Pasajero;
import turismo.clases.PromocionGrupo;
import turismo.clases.PromocionReservaAnticipada;
import turismo.clases.Proveedor;
import turismo.clases.Reserva;
import turismo.clases.Salida;
import turismo.clases.ServicioAdicional;
import turismo.clases.Tarifa;
import turismo.clases.Temporada;
import turismo.clases.TipoActividad;
import turismo.clases.TipoPasajero;
import turismo.clases.TipoServicio;
import turismo.clases.TipoVehiculo;
import turismo.clases.Tour;
import turismo.clases.TourAventura;
import turismo.clases.TourCultural;
import turismo.clases.TourNaturaleza;
import turismo.clases.Vehiculo;
import turismo.clases.Vendedor;
import turismo.clases.Actividad;
import turismo.excepciones.ReglaNegocioExcepcion;
import turismo.interfaces.Promocion;

/**
 * Carga inicial de la agencia «Rumbo Sur» la primera vez que se ejecuta el programa.
 * Las fechas se calculan desde "hoy" para que los datos sirvan en cualquier momento.
 */
public final class DatosDePrueba {

    private DatosDePrueba() {
    }

    public static DatosAgencia crear(LocalDate hoy) throws ReglaNegocioExcepcion {
        DatosAgencia datos = new DatosAgencia();
        List<Promocion> promociones = List.of(new PromocionReservaAnticipada(), new PromocionGrupo());

        // ---- proveedores
        Proveedor hotel = new Proveedor("Hotel Los Cardones", "30-71234567-8", "reservas@loscardones.com", "Alojamiento");
        Proveedor parrilla = new Proveedor("Parrilla El Algarrobo", "30-70987654-3", "351-4556677", "Comidas");
        Proveedor transporte = new Proveedor("Transportes Cuyo", "30-69876543-1", "261-4223344", "Traslados");
        datos.agregarProveedor(hotel);
        datos.agregarProveedor(parrilla);
        datos.agregarProveedor(transporte);

        // ---- tours
        LocalDate vigencia = hoy.minusDays(60);
        ServicioAdicional seguro = new ServicioAdicional("Seguro de viaje", TipoServicio.SEGURO_VIAJE, 9000, false);
        ServicioAdicional traslado = new ServicioAdicional("Traslado al aeropuerto", TipoServicio.TRASLADO, 15000, false);

        Tour talampaya = new TourNaturaleza("NAT-01", "Parque Nacional Talampaya",
                "Cañones rojos, geoglifos y avistaje de cóndores", 3, 5, 15, "Parque Nacional Talampaya (La Rioja)");
        talampaya.agregarActividad(new Actividad(1, 1, LocalTime.of(8, 0), "Traslado desde Chilecito", "Chilecito",
                TipoActividad.TRASLADO, transporte));
        talampaya.agregarActividad(new Actividad(1, 2, LocalTime.of(13, 0), "Almuerzo regional", "Villa Unión",
                TipoActividad.COMIDA, parrilla));
        talampaya.agregarActividad(new Actividad(1, 3, LocalTime.of(20, 0), "Noche de alojamiento", "Villa Unión",
                TipoActividad.ALOJAMIENTO, hotel));
        talampaya.agregarActividad(new Actividad(2, 1, LocalTime.of(9, 0), "Recorrido por el cañón", "Talampaya",
                TipoActividad.VISITA, null));
        talampaya.agregarActividad(new Actividad(3, 1, LocalTime.of(10, 0), "Avistaje de cóndores", "Quebrada Don Eduardo",
                TipoActividad.VISITA, null));
        cargarTarifas(talampaya, vigencia, 180000, 210000, 260000);
        talampaya.agregarServicio(seguro);
        talampaya.agregarServicio(traslado);
        talampaya.agregarServicio(new ServicioAdicional("Excursión nocturna de estrellas",
                TipoServicio.EXCURSION_OPCIONAL, 25000, false));

        Tour rafting = new TourAventura("AVE-01", "Rafting en el río Atuel",
                "Rafting clase III y tirolesa en el Cañón del Atuel", 2, 4, 12, NivelDificultad.MEDIO, 14);
        rafting.agregarActividad(new Actividad(1, 1, LocalTime.of(9, 0), "Charla de seguridad", "Valle Grande",
                TipoActividad.VISITA, null));
        rafting.agregarActividad(new Actividad(1, 2, LocalTime.of(10, 30), "Bajada en gomón", "Río Atuel",
                TipoActividad.VISITA, null));
        rafting.agregarActividad(new Actividad(1, 3, LocalTime.of(21, 0), "Cabañas", "Valle Grande",
                TipoActividad.ALOJAMIENTO, hotel));
        rafting.agregarActividad(new Actividad(2, 1, LocalTime.of(10, 0), "Tirolesa", "Cañón del Atuel",
                TipoActividad.VISITA, null));
        cargarTarifas(rafting, vigencia, 120000, 140000, 170000);
        rafting.agregarServicio(seguro); // en aventura el seguro es obligatorio (TourAventura.esObligatorio)
        rafting.agregarServicio(new ServicioAdicional("Fotos y video del descenso", TipoServicio.EXCURSION_OPCIONAL, 12000, false));

        Tour bodegas = new TourCultural("CUL-01", "Caminos del vino en Luján de Cuyo",
                "Tres bodegas con degustación y almuerzo", 1, 6, 20, "Bodegas y vitivinicultura");
        bodegas.agregarActividad(new Actividad(1, 1, LocalTime.of(9, 30), "Bodega Alta Vista", "Luján de Cuyo",
                TipoActividad.VISITA, null));
        bodegas.agregarActividad(new Actividad(1, 2, LocalTime.of(13, 0), "Almuerzo de pasos", "Bodega Ruca Malén",
                TipoActividad.COMIDA, parrilla));
        bodegas.agregarActividad(new Actividad(1, 3, LocalTime.of(16, 0), "Museo del vino", "Maipú",
                TipoActividad.VISITA, null));
        cargarTarifas(bodegas, vigencia, 95000, 110000, 130000);
        bodegas.agregarServicio(traslado);

        datos.agregarTour(talampaya);
        datos.agregarTour(rafting);
        datos.agregarTour(bodegas);

        // ---- flota
        Vehiculo micro = new Vehiculo("AB123CD", 40, TipoVehiculo.MICRO);
        Vehiculo combi = new Vehiculo("AC456EF", 15, TipoVehiculo.COMBI);
        Vehiculo camioneta = new Vehiculo("AD789GH", 12, TipoVehiculo.CAMIONETA);
        datos.agregarVehiculo(micro);
        datos.agregarVehiculo(combi);
        datos.agregarVehiculo(camioneta);

        // ---- personal
        Vendedor laura = new Vendedor("V-01", "Laura", "Gómez", "28111222", "261-5001001", 0.03);
        Vendedor martin = new Vendedor("V-02", "Martín", "Pereyra", "30222333", "261-5001002", 0.025);
        Guia sofia = new Guia("G-01", "Sofía", "Ruiz", "27333444", "261-5002001", List.of("Español", "Inglés"),
                List.of(CategoriaTour.AVENTURA, CategoriaTour.NATURALEZA), 45000);
        Guia diego = new Guia("G-02", "Diego", "Fernández", "26444555", "261-5002002", List.of("Español", "Portugués"),
                List.of(CategoriaTour.CULTURAL, CategoriaTour.NATURALEZA), 38000);
        datos.agregarEmpleado(laura);
        datos.agregarEmpleado(martin);
        datos.agregarEmpleado(sofia);
        datos.agregarEmpleado(diego);
        datos.agregarEmpleado(new Coordinador("C-01", "Ana", "Torres", "25555666", "261-5003001"));
        datos.agregarEmpleado(new Administrador("A-01", "Carlos", "Díaz", "22666777", "261-5004001"));

        // ---- clientes
        Cliente lucia = new Cliente("Lucía", "Méndez", "35111222", "11-4444-1111", "lucia.mendez@mail.com", "Argentina");
        Cliente pablo = new Cliente("Pablo", "Souza", "BR778899", "+55-11-9999-2222", "pablo.souza@mail.com", "Brasil");
        Cliente juan = new Cliente("Juan", "Ibáñez", "33444555", "261-4777-3333", "juan.ibanez@mail.com", "Argentina");
        datos.agregarCliente(lucia);
        datos.agregarCliente(pablo);
        datos.agregarCliente(juan);

        // ---- salidas y reservas
        Salida salidaTalampaya = new Salida(datos.siguienteCodigoSalida(), talampaya, hoy.plusDays(20), 15);
        datos.agregarSalida(salidaTalampaya);
        Reserva r1 = crearReserva(datos, salidaTalampaya, lucia, laura, hoy.minusDays(10), promociones, List.of(
                new Pasajero("Lucía", "Méndez", "35111222", LocalDate.of(1990, 3, 12), TipoPasajero.ADULTO,
                        "Raúl Méndez 11-4444-0000", "Celíaca"),
                new Pasajero("Tomás", "Méndez", "60111222", LocalDate.of(2018, 6, 2), TipoPasajero.MENOR,
                        "Lucía Méndez 11-4444-1111", ""),
                new Pasajero("Elena", "Ruiz", "10222333", LocalDate.of(1955, 9, 30), TipoPasajero.JUBILADO,
                        "Lucía Méndez 11-4444-1111", "Hipertensión"),
                new Pasajero("Martina", "Méndez", "70111222", LocalDate.of(2024, 1, 20), TipoPasajero.MENOR,
                        "Lucía Méndez 11-4444-1111", "")));
        r1.registrarPago(hoy.minusDays(10), Math.ceil(r1.getMontoSena()), MedioPago.TRANSFERENCIA, 1);
        Reserva r2 = crearReserva(datos, salidaTalampaya, pablo, martin, hoy.minusDays(8), promociones, List.of(
                new Pasajero("Pablo", "Souza", "BR778899", LocalDate.of(1985, 11, 5), TipoPasajero.ADULTO,
                        "Carla Souza +55-11-9999-3333", ""),
                new Pasajero("Carla", "Souza", "BR778800", LocalDate.of(1987, 2, 14), TipoPasajero.ADULTO,
                        "Pablo Souza +55-11-9999-2222", "Vegetariana")));
        r2.registrarPago(hoy.minusDays(8), r2.getSaldo(), MedioPago.TARJETA, 3);
        salidaTalampaya.asignarGuia(sofia);
        salidaTalampaya.asignarVehiculo(combi);

        Salida salidaRafting = new Salida(datos.siguienteCodigoSalida(), rafting, hoy.plusDays(40), 12);
        datos.agregarSalida(salidaRafting);
        crearReserva(datos, salidaRafting, juan, laura, hoy.minusDays(3), promociones, List.of(
                new Pasajero("Juan", "Ibáñez", "33444555", LocalDate.of(1988, 7, 21), TipoPasajero.ADULTO,
                        "Sol Ibáñez 261-4777-0000", ""),
                new Pasajero("Sol", "Ibáñez", "34555666", LocalDate.of(1991, 4, 9), TipoPasajero.ADULTO,
                        "Juan Ibáñez 261-4777-3333", ""),
                new Pasajero("Bruno", "Ibáñez", "50666777", LocalDate.of(2010, 12, 1), TipoPasajero.MENOR,
                        "Juan Ibáñez 261-4777-3333", "Alergia al maní")));

        Salida salidaBodegas = new Salida(datos.siguienteCodigoSalida(), bodegas, hoy.plusDays(75), 20);
        datos.agregarSalida(salidaBodegas);
        Reserva r4 = crearReserva(datos, salidaBodegas, lucia, martin, hoy, promociones, List.of(
                pasajeroAdulto("Lucía", "Méndez", "35111222", 1990),
                pasajeroAdulto("Andrés", "Paz", "29111000", 1982),
                pasajeroAdulto("Valeria", "Paz", "29111001", 1984),
                pasajeroAdulto("Hernán", "Gil", "31222000", 1979),
                pasajeroAdulto("Romina", "Gil", "31222001", 1981),
                pasajeroAdulto("Federico", "Luna", "32333000", 1992)));
        r4.registrarPago(hoy, Math.ceil(r4.getMontoSena()), MedioPago.TARJETA, 1);

        Salida salidaTalampaya2 = new Salida(datos.siguienteCodigoSalida(), talampaya, hoy.plusDays(110), 12);
        datos.agregarSalida(salidaTalampaya2);
        return datos;
    }

    private static void cargarTarifas(Tour tour, LocalDate vigencia, double baja, double media, double alta) {
        tour.agregarTarifa(new Tarifa(Temporada.BAJA, vigencia, baja));
        tour.agregarTarifa(new Tarifa(Temporada.MEDIA, vigencia, media));
        tour.agregarTarifa(new Tarifa(Temporada.ALTA, vigencia, alta));
    }

    private static Pasajero pasajeroAdulto(String nombre, String apellido, String documento, int anio) {
        return new Pasajero(nombre, apellido, documento, LocalDate.of(anio, 5, 10), TipoPasajero.ADULTO,
                "Contacto de " + nombre + " 261-4000000", "");
    }

    private static Reserva crearReserva(DatosAgencia datos, Salida salida, Cliente cliente, Vendedor vendedor,
                                        LocalDate fecha, List<Promocion> promociones, List<Pasajero> pasajeros)
            throws ReglaNegocioExcepcion {
        Reserva reserva = salida.crearReserva(datos.getProximoNumeroReserva(), cliente, vendedor, pasajeros, fecha, promociones);
        datos.confirmarNumeroReserva();
        return reserva;
    }
}