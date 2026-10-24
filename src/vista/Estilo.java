package vista;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.util.Arrays;
import java.util.List;

/**
 * Colores, fuentes y componentes con estilo que comparten todas las ventanas.
 * Tema oscuro inspirado en la terminal Warp: fondo casi negro, letra
 * monoespaciada y cada cálculo mostrado como un "bloque".
 * Las propiedades "FlatLaf.style" solo tienen efecto si FlatLaf está cargado;
 * sin él, los componentes se ven con el estilo normal del sistema.
 */
final class Estilo {

    static final Color FONDO = new Color(0x0E1015);
    static final Color BARRA = new Color(0x090A0E);
    static final Color SUPERFICIE = new Color(0x161922);
    static final Color SUPERFICIE_ALTA = new Color(0x1F2330);
    static final Color BORDE = new Color(0x2A2F3C);
    static final Color TEXTO = new Color(0xE6E8EE);
    static final Color TEXTO_SUAVE = new Color(0x8A91A0);
    static final Color ACENTO = new Color(0x5AC8FA);
    static final Color EXITO = new Color(0x7EE787);
    static final Color MORADO = new Color(0xD2A8FF);
    static final Color ERROR = new Color(0xFF7B72);
    static final Color BORDE_ERROR = new Color(0x5C2B2B);

    /** Primera fuente monoespaciada instalada de esta lista (Windows 11 trae Cascadia Mono). */
    static final String FUENTE_TERMINAL = elegirFuente(
            Arrays.asList("Cascadia Mono", "JetBrains Mono", "Consolas"), Font.MONOSPACED);

    private Estilo() {
    }

    private static String elegirFuente(List<String> preferidas, String porDefecto) {
        List<String> instaladas = Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        for (String fuente : preferidas) {
            if (instaladas.contains(fuente)) {
                return fuente;
            }
        }
        return porDefecto;
    }

    static Font fuente(int estilo, float tamano) {
        return new Font(FUENTE_TERMINAL, estilo, Math.round(tamano));
    }

    // ---------------- Componentes ----------------

    /** Botón principal: fondo de acento y texto oscuro. */
    static JButton botonPrimario(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(fuente(Font.BOLD, 13));
        boton.putClientProperty("FlatLaf.style",
                "background: #5AC8FA; foreground: #0E1015; hoverBackground: #7DD6FB;"
                        + " pressedBackground: #3BB4EC;"
                        + " default.background: #5AC8FA; default.foreground: #0E1015;"
                        + " default.hoverBackground: #7DD6FB; default.pressedBackground: #3BB4EC;"
                        + " default.borderWidth: 0; borderWidth: 0; focusWidth: 0");
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    /** Botón secundario: oscuro con borde fino. */
    static JButton botonSecundario(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(fuente(Font.PLAIN, 13));
        boton.putClientProperty("FlatLaf.style",
                "background: #161922; foreground: #E6E8EE; hoverBackground: #1F2330;"
                        + " pressedBackground: #2A2F3C; borderColor: #2A2F3C; focusWidth: 0");
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    /** Botón pequeño y redondeado, como una etiqueta que se puede pulsar. */
    static JButton chip(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(fuente(Font.PLAIN, 13));
        boton.putClientProperty("FlatLaf.style",
                "arc: 999; background: #1F2330; foreground: #5AC8FA; hoverBackground: #2A2F3C;"
                        + " pressedBackground: #343A49; borderWidth: 0; focusWidth: 0; margin: 2,10,2,10");
        boton.setFocusable(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    /** Botón que queda marcado al seleccionarlo (operación o variable). */
    static JToggleButton opcion(String texto) {
        JToggleButton boton = new JToggleButton(texto);
        boton.setFont(fuente(Font.PLAIN, 13));
        boton.putClientProperty("FlatLaf.style",
                "arc: 999; background: #161922; foreground: #8A91A0; hoverBackground: #1F2330;"
                        + " selectedBackground: #173041; selectedForeground: #5AC8FA;"
                        + " borderColor: #2A2F3C; focusWidth: 0; margin: 3,12,3,12");
        boton.setFocusable(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    /** Campo de texto oscuro sin bordes, como la línea de comandos de una terminal. */
    static JTextField campoTerminal(int columnas, float tamano) {
        JTextField campo = new JTextField(columnas);
        campo.setFont(fuente(Font.PLAIN, tamano));
        campo.setBackground(SUPERFICIE);
        campo.setForeground(TEXTO);
        campo.setCaretColor(ACENTO);
        campo.putClientProperty("FlatLaf.style",
                "background: #161922; foreground: #E6E8EE; borderColor: #2A2F3C;"
                        + " focusedBorderColor: #5AC8FA; focusWidth: 0; placeholderForeground: #5C6370");
        return campo;
    }

    static JLabel texto(String texto, Color color, int estilo, float tamano) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(fuente(estilo, tamano));
        etiqueta.setForeground(color);
        return etiqueta;
    }

    /** Barra superior oscura, como la barra de pestañas de Warp. */
    static JPanel barraSuperior(String titulo, String subtitulo, JComponent derecha) {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBackground(BARRA);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                BorderFactory.createEmptyBorder(10, 16, 10, 12)));

        JLabel lblTitulo = texto("●  " + titulo, TEXTO, Font.BOLD, 14);
        JLabel lblSubtitulo = texto(subtitulo, TEXTO_SUAVE, Font.PLAIN, 12);
        JPanel textos = new JPanel(new BorderLayout(12, 0));
        textos.setOpaque(false);
        textos.add(lblTitulo, BorderLayout.WEST);
        textos.add(lblSubtitulo, BorderLayout.CENTER);
        panel.add(textos, BorderLayout.CENTER);

        if (derecha != null) {
            panel.add(derecha, BorderLayout.EAST);
        }
        return panel;
    }

    /** Panel con esquinas redondeadas y borde fino (los "bloques" de Warp). */
    static class Tarjeta extends JPanel {

        private Color fondo = SUPERFICIE;
        private Color borde = BORDE;

        Tarjeta(LayoutManager layout) {
            super(layout);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        }

        void setColores(Color fondo, Color borde) {
            this.fondo = fondo;
            this.borde = borde;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fondo);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.setColor(borde);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.dispose();
        }
    }
}
