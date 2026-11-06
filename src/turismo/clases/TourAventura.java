package turismo.clases;

import java.time.LocalDate;

import turismo.excepciones.PasajeroNoValidoExcepcion;
import turismo.excepciones.ReglaNegocioExcepcion;

/** Tour de aventura: exige edad mínima, indica dificultad y obliga a contratar seguro de viaje. */
public class TourAventura extends Tour {
    private static final long serialVersionUID = 1L;

    private final NivelDificultad nivelDificultad;
    private final int edadMinima;

    public TourAventura(String codigo, String nombre, String descripcion, int duracionDias,
                        int cupoMinimo, int cupoMaximo, NivelDificultad nivelDificultad, int edadMinima) {
        super(codigo, nombre, descripcion, duracionDias, cupoMinimo, cupoMaximo);
        if (edadMinima < 0) {
            throw new IllegalArgumentException("La edad mínima no puede ser negativa");
        }
        this.nivelDificultad = nivelDificultad;
        this.edadMinima = edadMinima;
    }

    @Override
    public CategoriaTour getCategoria() {
        return CategoriaTour.AVENTURA;
    }

    @Override
    public String getDetalleCategoria() {
        return "Aventura - dificultad " + nivelDificultad + " - edad mínima " + edadMinima;
    }

    @Override
    public void validarPasajero(Pasajero pasajero, LocalDate fechaSalida) throws PasajeroNoValidoExcepcion {
        int edad = pasajero.calcularEdad(fechaSalida);
        if (edad < edadMinima) {
            throw new PasajeroNoValidoExcepcion(pasajero.getNombreCompleto() + " tiene " + edad
                    + " años y el tour " + nombre + " exige " + edadMinima);
        }
    }

    /** En aventura el seguro de viaje es obligatorio aunque en el catálogo figure como opcional. */
    @Override
    public boolean esObligatorio(ServicioAdicional servicio) {
        return servicio.getTipo() == TipoServicio.SEGURO_VIAJE || super.esObligatorio(servicio);
    }

    @Override
    public void validarServiciosObligatorios() throws ReglaNegocioExcepcion {
        super.validarServiciosObligatorios();
        for (ServicioAdicional servicio : getServiciosDisponibles()) {
            if (servicio.getTipo() == TipoServicio.SEGURO_VIAJE) {
                return;
            }
        }
        throw new ReglaNegocioExcepcion("El tour de aventura " + nombre + " debe ofrecer seguro de viaje");
    }

    public NivelDificultad getNivelDificultad() {
        return nivelDificultad;
    }

    public int getEdadMinima() {
        return edadMinima;
    }
}