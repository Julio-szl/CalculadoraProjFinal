package vista;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

/**
 * Manual de usuario dentro de la aplicación.
 * No es modal: el usuario puede leerlo mientras usa la calculadora.
 */
public class VentanaManual extends JDialog {

    private static final String CONTENIDO =
            "<html><body style='font-family:sans-serif; font-size:12pt; margin:10px'>"
            + "<h2>Manual de usuario</h2>"

            + "<h3>1. ¿Qué hace la calculadora?</h3>"
            + "<p>Calcula la <b>derivada</b>, la <b>integral indefinida</b> y la "
            + "<b>integral definida</b> de un polinomio.</p>"

            + "<h3>2. Cómo escribir la función</h3>"
            + "<ul>"
            + "<li>Escribe los términos separados por <b>+</b> o <b>-</b>. Los espacios no importan.</li>"
            + "<li>La potencia se escribe con <b>^</b>: <code>x^3</code>.</li>"
            + "<li>El coeficiente va pegado a la variable, sin <code>*</code>: <code>5x</code>.</li>"
            + "<li>Los decimales llevan punto: <code>2.5x^2</code>.</li>"
            + "<li>Se permiten exponentes negativos: <code>x^-2</code>.</li>"
            + "<li>Usa solo la variable elegida (<b>x</b> o <b>y</b>).</li>"
            + "</ul>"
            + "<p><b>Ejemplos válidos:</b> <code>3x^2 + 5x - 2</code>, <code>-y^3 + 4y</code>, "
            + "<code>x^-2 + 1</code></p>"
            + "<p><b>Ejemplos no válidos:</b> <code>3*x</code>, <code>x^2.5</code>, "
            + "<code>(x+1)^2</code>, <code>sin(x)</code></p>"

            + "<h3>3. Pasos para calcular</h3>"
            + "<ol>"
            + "<li>Escribe la función en el campo <b>Función</b>.</li>"
            + "<li>Elige la <b>variable</b> (x o y).</li>"
            + "<li>Elige la <b>operación</b>.</li>"
            + "<li>Si es <b>integral definida</b>, escribe los límites <b>a</b> (inferior) "
            + "y <b>b</b> (superior). Los límites se habilitan solo para esta operación.</li>"
            + "<li>Presiona <b>Calcular</b> o la tecla <b>Enter</b>.</li>"
            + "</ol>"
            + "<p>El botón <b>Limpiar</b> borra todos los campos.</p>"

            + "<h3>4. Cómo leer el resultado</h3>"
            + "<ul>"
            + "<li><b>Derivada:</b> <code>f'(x) = 6x + 5</code></li>"
            + "<li><b>Integral indefinida:</b> <code>F(x) = x^3 + C</code> "
            + "(C es la constante de integración).</li>"
            + "<li><b>Integral definida:</b> <code>Resultado = 4</code>. Si a es mayor que b, "
            + "el resultado sale negativo; es correcto.</li>"
            + "</ul>"

            + "<h3>5. Mensajes de error</h3>"
            + "<ul>"
            + "<li><b>Término inválido / Carácter no permitido:</b> la función no sigue el "
            + "formato de la sección 2.</li>"
            + "<li><b>El límite está vacío / no es un número válido:</b> revisa los campos a y b.</li>"
            + "<li><b>Integral de x^-1:</b> su resultado es ln|x|, que no es un polinomio, "
            + "por eso no está soportada.</li>"
            + "<li><b>No se puede evaluar:</b> uno de los límites es un punto donde la "
            + "función no existe (por ejemplo, x^-2 con a = 0, porque no se puede dividir entre cero).</li>"
            + "</ul>"
            + "</body></html>";

    public VentanaManual(JFrame propietario) {
        super(propietario, "Manual de usuario", false);
        inicializarComponentes();
        setLocationRelativeTo(propietario);
    }

    private void inicializarComponentes() {
        JEditorPane texto = new JEditorPane("text/html", CONTENIDO);
        texto.setEditable(false);
        texto.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(texto);
        scroll.setPreferredSize(new Dimension(560, 520));

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> setVisible(false));
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBoton.add(btnCerrar);

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        contenido.add(scroll, BorderLayout.CENTER);
        contenido.add(panelBoton, BorderLayout.SOUTH);
        setContentPane(contenido);
        pack();
    }
}
