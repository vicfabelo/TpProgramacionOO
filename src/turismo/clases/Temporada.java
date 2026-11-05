package turismo.clases;

import java.time.LocalDate;

/**
 * Temporadas tarifarias.
 * Supuesto documentado: la temporada se determina por el mes de inicio de la salida.
 * ALTA: diciembre, enero, febrero y julio (vacaciones). MEDIA: marzo, abril, octubre y noviembre.
 * BAJA: mayo, junio, agosto y septiembre.
 */
public enum Temporada {
    BAJA, MEDIA, ALTA;

    public static Temporada desde(LocalDate fecha) {
        switch (fecha.getMonth()) {
            case DECEMBER:
            case JANUARY:
            case FEBRUARY:
            case JULY:
                return ALTA;
            case MARCH:
            case APRIL:
            case OCTOBER:
            case NOVEMBER:
                return MEDIA;
            default:
                return BAJA;
        }
    }
}
