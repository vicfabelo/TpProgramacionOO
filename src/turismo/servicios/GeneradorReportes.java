package turismo.servicios;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import turismo.clases.Cancelacion;
import turismo.clases.EstadoReserva;
import turismo.clases.Reserva;
import turismo.clases.Salida;
import turismo.clases.Tour;
import turismo.clases.Vendedor;

/**
 * Arma los reportes del administrador. SRP: los cálculos de reporte no ensucian
 * las clases del dominio ni la interfaz.
 */
public class GeneradorReportes {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Ocupación por salida, en el orden natural de las salidas (por fecha). */
    public Reporte ocupacionPorSalida(List<Salida> salidas) {
        Reporte reporte = new Reporte("Ocupación por salida", "Salida", "Tour", "Inicio", "Estado",
                "Ocupados", "Cupo", "Ocupación");
        List<Salida> ordenadas = new ArrayList<>(salidas);
        ordenadas.sort(null); // null = orden natural (Comparable de Salida)
        for (Salida salida : ordenadas) {
            double porcentaje = 100.0 * salida.getLugaresOcupados() / salida.getCupoMaximo();
            reporte.agregarFila(salida.getCodigo(), salida.getTour().getNombre(), fecha(salida.getFechaInicio()),
                    salida.getEstado(), salida.getLugaresOcupados(), salida.getCupoMaximo(),
                    String.format("%.1f %%", porcentaje));
        }
        return reporte;
    }

    /** Ventas por vendedor (reservas no anuladas), de mayor a menor importe (Comparator). */
    public Reporte ventasPorVendedor(List<Reserva> reservas) {
        Map<Vendedor, Double> totalPorVendedor = new HashMap<>();
        Map<Vendedor, Integer> cantidadPorVendedor = new HashMap<>();
        for (Reserva reserva : reservas) {
            if (reserva.getEstado() == EstadoReserva.ANULADA) {
                continue;
            }
            totalPorVendedor.merge(reserva.getVendedor(), reserva.getTotal(), Double::sum);
            cantidadPorVendedor.merge(reserva.getVendedor(), 1, Integer::sum);
        }
        List<Map.Entry<Vendedor, Double>> filas = new ArrayList<>(totalPorVendedor.entrySet());
        filas.sort(Map.Entry.<Vendedor, Double>comparingByValue().reversed());

        Reporte reporte = new Reporte("Ventas por vendedor", "Vendedor", "Reservas", "Total vendido", "Comisión");
        for (Map.Entry<Vendedor, Double> fila : filas) {
            Vendedor vendedor = fila.getKey();
            reporte.agregarFila(vendedor.getNombreCompleto(), cantidadPorVendedor.get(vendedor),
                    pesos(fila.getValue()), pesos(vendedor.calcularComision(fila.getValue())));
        }
        return reporte;
    }

    /**
     * Ingresos por tour: lo cobrado menos lo devuelto por cancelaciones.
     * Supuesto: el ingreso es caja neta (no incluye saldos pendientes ni recargos de tarjeta).
     */
    public Reporte ingresosPorTour(List<Reserva> reservas) {
        Map<Tour, Double> ingresos = new LinkedHashMap<>();
        for (Reserva reserva : reservas) {
            Cancelacion cancelacion = reserva.getCancelacion();
            double devuelto = cancelacion == null ? 0 : cancelacion.getImporteDevuelto();
            ingresos.merge(reserva.getSalida().getTour(), reserva.getTotalPagado() - devuelto, Double::sum);
        }
        List<Map.Entry<Tour, Double>> filas = new ArrayList<>(ingresos.entrySet());
        filas.sort(Comparator.comparing((Map.Entry<Tour, Double> e) -> e.getValue()).reversed()
                .thenComparing(e -> e.getKey().getNombre()));

        Reporte reporte = new Reporte("Ingresos por tour", "Código", "Tour", "Categoría", "Ingreso neto");
        for (Map.Entry<Tour, Double> fila : filas) {
            Tour tour = fila.getKey();
            reporte.agregarFila(tour.getCodigo(), tour.getNombre(), tour.getCategoria(), pesos(fila.getValue()));
        }
        return reporte;
    }

    /** Reservas anuladas, de la cancelación más reciente a la más vieja. */
    public Reporte cancelaciones(List<Reserva> reservas) {
        List<Reserva> anuladas = new ArrayList<>();
        for (Reserva reserva : reservas) {
            if (reserva.getCancelacion() != null) {
                anuladas.add(reserva);
            }
        }
        anuladas.sort(Comparator.comparing((Reserva r) -> r.getCancelacion().getFecha()).reversed()
                .thenComparing(Reserva::getNumero));

        Reporte reporte = new Reporte("Cancelaciones", "Reserva", "Cliente", "Salida", "Fecha", "Motivo",
                "Política", "Devolución", "Devuelto");
        for (Reserva reserva : anuladas) {
            Cancelacion cancelacion = reserva.getCancelacion();
            reporte.agregarFila(reserva.getNumero(), reserva.getTitular().getNombreCompleto(),
                    reserva.getSalida().getCodigo(), fecha(cancelacion.getFecha()), cancelacion.getMotivo(),
                    cancelacion.getPolitica(), Math.round(cancelacion.getPorcentajeDevolucion() * 100) + " %",
                    pesos(cancelacion.getImporteDevuelto()));
        }
        return reporte;
    }

    private static String fecha(LocalDate fecha) {
        return fecha.format(FORMATO_FECHA);
    }

    static String pesos(double importe) {
        return String.format("$ %,.2f", importe);
    }
}