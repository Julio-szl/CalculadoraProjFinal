package vista;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Insets;

/**
 * Punto de entrada de la aplicación: solo abre la VentanaPrincipal.
 */
public class Main {

    public static void main(String[] args) {
        aplicarAspecto();

        // Swing debe crearse y manejarse desde su propio hilo (Event Dispatch Thread).
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }

    /**
     * Usa FlatLaf oscuro (lib/flatlaf-*.jar) para un aspecto tipo terminal. Se carga por
     * nombre: si la librería no está, la app no falla y usa el aspecto del sistema operativo.
     */
    private static void aplicarAspecto() {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("Component.focusColor", Estilo.ACENTO);
            UIManager.put("TitlePane.background", Estilo.BARRA);
            UIManager.put("TitlePane.foreground", Estilo.TEXTO_SUAVE);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("ScrollBar.track", Estilo.FONDO);
            UIManager.put("TabbedPane.selectedForeground", Estilo.ACENTO);
            UIManager.put("TabbedPane.underlineColor", Estilo.ACENTO);
            UIManager.put("ToolTip.background", Estilo.SUPERFICIE_ALTA);
        } catch (Exception sinFlatLaf) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Si también falla, Swing usa su aspecto por defecto; no es un error grave.
            }
        }
    }
}
