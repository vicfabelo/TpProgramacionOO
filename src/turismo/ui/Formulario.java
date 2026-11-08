package turismo.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Arma diálogos de carga de datos (etiqueta + campo) y convierte lo que escribe el usuario.
 * Si un dato no es válido lanza IllegalArgumentException con un mensaje claro.
 */
final class Formulario {

    static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    /** Se lanza cuando el usuario cierra o cancela el diálogo: la acción se corta sin mostrar error. */
    static final class CanceladoPorUsuario extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private final JPanel panel = new JPanel(new GridBagLayout());
    private int fila;

    JTextField texto(String etiqueta, String valorInicial) {
        JTextField campo = new JTextField(valorInicial == null ? "" : valorInicial, 22);
        agregar(etiqueta, campo);
        return campo;
    }

    <T> JComboBox<T> combo(String etiqueta, List<T> opciones) {
        JComboBox<T> combo = new JComboBox<>();
        for (T opcion : opciones) {
            combo.addItem(opcion);
        }
        agregar(etiqueta, combo);
        return combo;
    }

    JCheckBox check(String etiqueta) {
        JCheckBox check = new JCheckBox();
        agregar(etiqueta, check);
        return check;
    }

    void nota(String texto) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = fila++;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 4, 8, 4);
        panel.add(new JLabel("<html>" + texto + "</html>"), c);
    }

    private void agregar(String etiqueta, JComponent campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = fila;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(3, 4, 3, 6);
        panel.add(new JLabel(etiqueta + ":"), c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        panel.add(campo, c);
        fila++;
    }

    /** Muestra el diálogo. Si el usuario cancela, corta la acción. */
    void mostrar(Component padre, String titulo) {
        int opcion = JOptionPane.showConfirmDialog(padre, panel, titulo, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) {
            throw new CanceladoPorUsuario();
        }
    }

    // ------------------------------------------------------------ conversiones

    static String requerido(JTextField campo, String nombre) {
        String valor = campo.getText().trim();
        if (valor.isEmpty()) {
            throw new IllegalArgumentException("El campo '" + nombre + "' es obligatorio");
        }
        return valor;
    }

    static int entero(JTextField campo, String nombre) {
        try {
            return Integer.parseInt(campo.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo '" + nombre + "' debe ser un número entero");
        }
    }

    static double decimal(JTextField campo, String nombre) {
        try {
            return Double.parseDouble(campo.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo '" + nombre + "' debe ser un número");
        }
    }

    static LocalDate fecha(JTextField campo, String nombre) {
        try {
            return LocalDate.parse(campo.getText().trim(), FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("El campo '" + nombre + "' debe tener formato dd/mm/aaaa");
        }
    }

    static LocalTime hora(JTextField campo, String nombre) {
        try {
            return LocalTime.parse(campo.getText().trim(), FORMATO_HORA);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("El campo '" + nombre + "' debe tener formato hh:mm");
        }
    }

    @SuppressWarnings("unchecked")
    static <T> T seleccion(JComboBox<T> combo, String nombre) {
        Object valor = combo.getSelectedItem();
        if (valor == null) {
            throw new IllegalArgumentException("Elegí un valor para '" + nombre + "'");
        }
        return (T) valor;
    }
}