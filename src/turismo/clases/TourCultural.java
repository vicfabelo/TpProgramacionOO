package turismo.clases;

/** Tour cultural (circuitos históricos, bodegas, museos). */
public class TourCultural extends Tour {
    private static final long serialVersionUID = 1L;

    private final String tematica;

    public TourCultural(String codigo, String nombre, String descripcion, int duracionDias,
                        int cupoMinimo, int cupoMaximo, String tematica) {
        super(codigo, nombre, descripcion, duracionDias, cupoMinimo, cupoMaximo);
        this.tematica = tematica == null ? "" : tematica.trim();
    }

    @Override
    public CategoriaTour getCategoria() {
        return CategoriaTour.CULTURAL;
    }

    @Override
    public String getDetalleCategoria() {
        return "Cultural - temática " + tematica;
    }

    public String getTematica() {
        return tematica;
    }
}