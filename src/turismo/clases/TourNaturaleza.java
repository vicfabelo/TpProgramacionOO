package turismo.clases;

/** Tour de naturaleza (parques nacionales, avistaje de fauna). */
public class TourNaturaleza extends Tour {
    private static final long serialVersionUID = 1L;

    private final String areaNatural;

    public TourNaturaleza(String codigo, String nombre, String descripcion, int duracionDias,
                          int cupoMinimo, int cupoMaximo, String areaNatural) {
        super(codigo, nombre, descripcion, duracionDias, cupoMinimo, cupoMaximo);
        this.areaNatural = areaNatural == null ? "" : areaNatural.trim();
    }

    @Override
    public CategoriaTour getCategoria() {
        return CategoriaTour.NATURALEZA;
    }

    @Override
    public String getDetalleCategoria() {
        return "Naturaleza - " + areaNatural;
    }

    public String getAreaNatural() {
        return areaNatural;
    }
}