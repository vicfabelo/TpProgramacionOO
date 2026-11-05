package turismo.clases;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Contenedor del conjunto de actividades de un Tour.
 * Mantiene las actividades ordenadas automáticamente aprovechando compareTo() de Actividad.
 */
public class Itinerario implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int duracionDias;
    private final SortedSet<Actividad> actividades;

    public Itinerario(int duracionDias) {
        if (duracionDias < 1) {
            throw new IllegalArgumentException("La duración del itinerario debe ser de al menos 1 día");
        }
        this.duracionDias = duracionDias;
        this.actividades = new TreeSet<>();
    }

    public void agregarActividad(Actividad actividad) {
        if (actividad == null) {
            throw new IllegalArgumentException("No se puede agregar una actividad nula");
        }
        if (actividad.getDia() > duracionDias) {
            throw new IllegalArgumentException(
                    "El día de la actividad (" + actividad.getDia() + ") excede la duración del tour (" + duracionDias + " días)"
            );
        }
        actividades.add(actividad);
    }

    /**
     * Devuelve una lista inmutable de las actividades ordenadas.
     */
    public List<Actividad> getActividades() {
        return Collections.unmodifiableList(new ArrayList<>(actividades));
    }

    /**
     * Filtra y devuelve únicamente las actividades correspondientes a un día en particular.
     */
    public List<Actividad> getActividadesPorDia(int dia) {
        List<Actividad> porDia = new ArrayList<>();
        for (Actividad act : actividades) {
            if (act.getDia() == dia) {
                porDia.add(act);
            }
        }
        return Collections.unmodifiableList(porDia);
    }

    /**
     * Retorna la lista de proveedores cuyos servicios de terceros son requeridos en el itinerario (GRASP Experto).
     */
    public List<Proveedor> getProveedoresRequeridos() {
        List<Proveedor> proveedores = new ArrayList<>();
        for (Actividad act : actividades) {
            if (act.incluyeServicioDeTerceros() && !proveedores.contains(act.getProveedor())) {
                proveedores.add(act.getProveedor());
            }
        }
        return Collections.unmodifiableList(proveedores);
    }

    public int getDuracionDias() {
        return duracionDias;
    }

    public int getTotalActividades() {
        return actividades.size();
    }

    @Override
    public String toString() {
        return "Itinerario de " + duracionDias + " días (" + actividades.size() + " actividades programadas)";
    }
}