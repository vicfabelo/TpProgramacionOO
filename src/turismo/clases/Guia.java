package turismo.clases;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Guía de turismo. Experto en información: conoce sus categorías habilitadas y sus salidas,
 * por eso es quien responde si puede tomar una salida nueva.
 */
public class Guia extends Empleado {
    private static final long serialVersionUID = 1L;

    private final List<String> idiomas = new ArrayList<>();
    private final Set<CategoriaTour> categoriasHabilitadas = EnumSet.noneOf(CategoriaTour.class);
    private final double honorarioPorDia;
    private final List<Salida> salidasAsignadas = new ArrayList<>();

    public Guia(String legajo, String nombre, String apellido, String documento, String telefono,
                Collection<String> idiomas, Collection<CategoriaTour> categorias, double honorarioPorDia) {
        super(legajo, nombre, apellido, documento, telefono);
        if (honorarioPorDia <= 0) {
            throw new IllegalArgumentException("El honorario por día debe ser positivo");
        }
        if (categorias == null || categorias.isEmpty()) {
            throw new IllegalArgumentException("El guía debe estar habilitado para al menos una categoría");
        }
        this.idiomas.addAll(idiomas);
        this.categoriasHabilitadas.addAll(categorias);
        this.honorarioPorDia = honorarioPorDia;
    }

    public boolean estaHabilitadoPara(CategoriaTour categoria) {
        return categoriasHabilitadas.contains(categoria);
    }

    /**
     * Un guía no puede tener dos salidas superpuestas.
     * @param excluida salida que se ignora (la misma que se está reasignando)
     */
    public boolean estaDisponible(LocalDate inicio, LocalDate fin, Salida excluida) {
        for (Salida salida : salidasAsignadas) {
            if (salida == excluida || salida.getEstado() == EstadoSalida.CANCELADA) {
                continue;
            }
            boolean seSuperponen = !inicio.isAfter(salida.getFechaFin()) && !fin.isBefore(salida.getFechaInicio());
            if (seSuperponen) {
                return false;
            }
        }
        return true;
    }

    void agregarSalida(Salida salida) {
        if (!salidasAsignadas.contains(salida)) {
            salidasAsignadas.add(salida);
        }
    }

    void quitarSalida(Salida salida) {
        salidasAsignadas.remove(salida);
    }

    @Override
    public String getRol() {
        return "Guía";
    }

    @Override
    public String getDescripcion() {
        return super.getDescripcion() + " - idiomas " + idiomas + " - habilitado para " + categoriasHabilitadas;
    }

    public List<String> getIdiomas() {
        return Collections.unmodifiableList(idiomas);
    }

    public Set<CategoriaTour> getCategoriasHabilitadas() {
        return Collections.unmodifiableSet(categoriasHabilitadas);
    }

    public double getHonorarioPorDia() {
        return honorarioPorDia;
    }

    public List<Salida> getSalidasAsignadas() {
        return Collections.unmodifiableList(salidasAsignadas);
    }
}