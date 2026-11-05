package turismo.clases;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * Actividad del itinerario de un tour.
 * Implementa Comparable: el orden natural es por día y, dentro del día, por orden.
 */
public class Actividad implements Comparable<Actividad>, Serializable {
    private static final long serialVersionUID = 1L;

    private final int dia;
    private final int orden;
    private final LocalTime horario;
    private final String descripcion;
    private final String lugar;
    private final TipoActividad tipo;
    private final Proveedor proveedor; // puede ser null si no hay servicio de terceros

    public Actividad(int dia, int orden, LocalTime horario, String descripcion, String lugar,
                     TipoActividad tipo, Proveedor proveedor) {
        if (dia < 1 || orden < 1) {
            throw new IllegalArgumentException("El día y el orden empiezan en 1");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria");
        }
        this.dia = dia;
        this.orden = orden;
        this.horario = horario;
        this.descripcion = descripcion.trim();
        this.lugar = lugar == null ? "" : lugar.trim();
        this.tipo = tipo;
        this.proveedor = proveedor;
    }

    @Override
    public int compareTo(Actividad otra) {
        int porDia = Integer.compare(dia, otra.dia);
        return porDia != 0 ? porDia : Integer.compare(orden, otra.orden);
    }

    public boolean incluyeServicioDeTerceros() {
        return proveedor != null;
    }

    public int getDia() {
        return dia;
    }

    public int getOrden() {
        return orden;
    }

    public LocalTime getHorario() {
        return horario;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getLugar() {
        return lugar;
    }

    public TipoActividad getTipo() {
        return tipo;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    @Override
    public String toString() {
        String texto = "Día " + dia + " #" + orden + " " + horario + " - " + tipo + ": " + descripcion + " (" + lugar + ")";
        return proveedor == null ? texto : texto + " - proveedor: " + proveedor.getNombre();
    }
}
