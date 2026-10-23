package vista;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punto de entrada de la aplicación: solo abre la VentanaPrincipal.
 */
public class Main {

    public static void main(String[] args) {
        try {
            // Usa el aspecto nativo del sistema operativo (Windows, macOS, etc.).
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si falla, Swing usa su aspecto por defecto; no es un error grave.
        }

        // Swing debe crearse y manejarse desde su propio hilo (Event Dispatch Thread).
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
