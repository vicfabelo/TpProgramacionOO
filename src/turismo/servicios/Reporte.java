package turismo.servicios;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Resultado tabular de un reporte: lo muestra la interfaz en una tabla o se exporta a texto. */
public class Reporte {

    private final String titulo;
    private final String[] columnas;
    private final List<Object[]> filas = new ArrayList<>();

    public Reporte(String titulo, String... columnas) {
        this.titulo = titulo;
        this.columnas = columnas.clone();
    }

    void agregarFila(Object... valores) {
        filas.add(valores.clone());
    }

    /** Arma el reporte como líneas de texto con columnas alineadas. */
    public List<String> aLineas() {
        int[] anchos = new int[columnas.length];
        for (int i = 0; i < columnas.length; i++) {
            anchos[i] = columnas[i].length();
        }
        for (Object[] fila : filas) {
            for (int i = 0; i < fila.length; i++) {
                anchos[i] = Math.max(anchos[i], String.valueOf(fila[i]).length());
            }
        }
        List<String> lineas = new ArrayList<>();
        lineas.add(titulo);
        lineas.add(formatear(columnas, anchos));
        StringBuilder separador = new StringBuilder();
        for (int ancho : anchos) {
            separador.append("-".repeat(ancho)).append("  ");
        }
        lineas.add(separador.toString().stripTrailing());
        for (Object[] fila : filas) {
            lineas.add(formatear(fila, anchos));
        }
        if (filas.isEmpty()) {
            lineas.add("(sin datos)");
        }
        return lineas;
    }

    private static String formatear(Object[] valores, int[] anchos) {
        StringBuilder linea = new StringBuilder();
        for (int i = 0; i < valores.length; i++) {
            linea.append(String.format("%-" + anchos[i] + "s  ", valores[i]));
        }
        return linea.toString().stripTrailing();
    }

    public String getTitulo() {
        return titulo;
    }

    public String[] getColumnas() {
        return columnas.clone();
    }

    public List<Object[]> getFilas() {
        return Collections.unmodifiableList(filas);
    }
}