package vista;

import java.awt.Insets;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        aplicarAspecto();
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }

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
            }
        }
    }
}
