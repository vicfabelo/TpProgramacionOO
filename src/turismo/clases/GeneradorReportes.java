package turismo.clases;

import java.util.List;

public class GeneradorReportes {

    public String reporteOcupacionPorSalida(List<Salida> salidas) {
        StringBuilder sb = new StringBuilder("=== REPORTE DE OCUPACION POR SALIDA ===\n");
        for (Salida s : salidas) {
            sb.append("Salida: ").append(s.getCodigo())
                    .append(" | Tour: ").append(s.getTour().getNombre())
                    .append(" | Ocupados: ").append(s.getLugaresOcupados())
                    .append("/").append(s.getCupoMaximo())
                    .append(" | Estado: ").append(s.getEstado()).append("\n");
        }
        return sb.toString();
    }

    public String reporteVentasPorVendedor(List<Salida> salidas) {
        StringBuilder sb = new StringBuilder("=== REPORTE DE VENTAS POR VENDEDOR ===\n");
        for (Salida s : salidas) {
            for (Reserva r : s.getReservas()) {
                if (r.getEstado() != EstadoReserva.ANULADA) {
                    sb.append("Vendedor: ").append(r.getVendedor().getNombreCompleto())
                            .append(" | Reserva #: ").append(r.getNumero())
                            .append(" | Total: $").append(r.getTotal()).append("\n");
                }
            }
        }
        return sb.toString();
    }

    public String reporteIngresosPorTour(List<Salida> salidas) {
        StringBuilder sb = new StringBuilder("=== REPORTE DE INGRESOS POR TOUR ===\n");
        for (Salida s : salidas) {
            double ingresosSalida = 0;
            for (Reserva r : s.getReservas()) {
                if (r.getEstado() != EstadoReserva.ANULADA) {
                    ingresosSalida += r.getTotalPagado();
                }
            }
            sb.append("Tour: ").append(s.getTour().getNombre())
                    .append(" | Salida: ").append(s.getCodigo())
                    .append(" | Recaudado: $").append(ingresosSalida).append("\n");
        }
        return sb.toString();
    }

    public String reporteCancelaciones(List<Salida> salidas) {
        StringBuilder sb = new StringBuilder("=== REPORTE DE CANCELACIONES ===\n");
        for (Salida s : salidas) {
            for (Reserva r : s.getReservas()) {
                if (r.getEstado() == EstadoReserva.ANULADA && r.getCancelacion() != null) {
                    sb.append("Reserva #: ").append(r.getNumero())
                            .append(" | Devuelto: $").append(r.getCancelacion().getImporteDevuelto()).append("\n");
                }
            }
        }
        return sb.toString();
    }
}