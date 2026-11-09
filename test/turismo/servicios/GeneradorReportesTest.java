package turismo.servicios;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import turismo.Escenario;
import turismo.clases.Cliente;
import turismo.clases.MedioPago;
import turismo.clases.PoliticaCancelacionCliente;
import turismo.clases.Reserva;
import turismo.clases.Salida;
import turismo.clases.Vendedor;
import turismo.excepciones.ReglaNegocioExcepcion;

public class GeneradorReportesTest {

    private final GeneradorReportes generador = new GeneradorReportes();

    @Test
    public void ventasPorVendedorOrdenadasDeMayorAMenorSinAnuladas() throws ReglaNegocioExcepcion {
        Salida salida = Escenario.salida(Escenario.tourNaturaleza(2, 15), 15);
        Cliente cliente = Escenario.cliente();
        Vendedor laura = new Vendedor("V1", "Laura", "Gómez", "1", "", 0.03);
        Vendedor martin = new Vendedor("V2", "Martín", "Pereyra", "2", "", 0.02);
        salida.crearReserva(1, cliente, laura, Escenario.adultos(1), Escenario.FECHA_RESERVA, Escenario.PROMOCIONES);
        salida.crearReserva(2, cliente, martin, Escenario.adultos(3), Escenario.FECHA_RESERVA, Escenario.PROMOCIONES);
        Reserva anulada = salida.crearReserva(3, cliente, laura, Escenario.adultos(5), Escenario.FECHA_RESERVA,
                Escenario.PROMOCIONES);
        anulada.cancelar(Escenario.FECHA_RESERVA, "", new PoliticaCancelacionCliente());

        Reporte reporte = generador.ventasPorVendedor(salida.getReservas());
        List<Object[]> filas = reporte.getFilas();
        assertEquals(2, filas.size());
        assertEquals("Pereyra, Martín", filas.get(0)[0]);
        assertEquals("Gómez, Laura", filas.get(1)[0]);
        assertEquals(1, filas.get(1)[1]); // la anulada no cuenta
    }

    @Test
    public void losIngresosDescuentanLoDevuelto() throws ReglaNegocioExcepcion {
        Salida salida = Escenario.salida(Escenario.tourNaturaleza(2, 15), 15);
        Reserva reserva = Escenario.reservar(salida, 1);
        reserva.registrarPago(Escenario.FECHA_RESERVA, 40_000, MedioPago.EFECTIVO, 1);
        reserva.cancelar(Escenario.FECHA_RESERVA, "Cambio de planes", new PoliticaCancelacionCliente()); // 30 días: 90 %

        Reporte reporte = generador.ingresosPorTour(salida.getReservas());
        assertEquals(GeneradorReportes.pesos(4_000), reporte.getFilas().get(0)[3]);
    }

    @Test
    public void elReporteDeCancelacionesSoloListaReservasAnuladas() throws ReglaNegocioExcepcion {
        Salida salida = Escenario.salida(Escenario.tourNaturaleza(2, 15), 15);
        Escenario.reservar(salida, 1);
        Reserva anulada = Escenario.reservar(salida, 2);
        anulada.cancelar(Escenario.FECHA_RESERVA, "Enfermedad", new PoliticaCancelacionCliente());

        Reporte reporte = generador.cancelaciones(salida.getReservas());
        assertEquals(1, reporte.getFilas().size());
        assertEquals("Enfermedad", reporte.getFilas().get(0)[4]);
    }

    @Test
    public void elReporteSePuedeConvertirATexto() throws ReglaNegocioExcepcion {
        Salida salida = Escenario.salida(Escenario.tourNaturaleza(2, 15), 15);
        List<String> lineas = generador.ocupacionPorSalida(List.of(salida)).aLineas();
        assertEquals("Ocupación por salida", lineas.get(0));
        assertEquals(4, lineas.size()); // título, encabezado, separador y una fila
    }
}