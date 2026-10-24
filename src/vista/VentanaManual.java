package vista;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

/**
 * Manual de usuario dentro de la aplicación, dividido en secciones (pestañas).
 * No es modal: el usuario puede leerlo mientras usa la calculadora.
 */
public class VentanaManual extends JDialog {

    // Colores del HTML (los mismos de Estilo, escritos como texto para el CSS).
    private static final String C_SUPERFICIE = "#161922";
    private static final String C_SUPERFICIE_ALTA = "#1F2330";
    private static final String C_ACENTO = "#5AC8FA";
    private static final String C_FONDO_OK = "#12241A";
    private static final String C_FONDO_MAL = "#2A1618";
    private static final String C_FONDO_NOTA = "#2A2414";

    // ---------------- Contenido de cada sección ----------------

    private static final String INICIO =
            "<h2>Bienvenido</h2>"
            + "<p>Esta calculadora resuelve <b>derivadas</b> e <b>integrales</b> de polinomios "
            + "y te muestra el procedimiento completo para que entiendas cómo se llegó al resultado. "
            + "Funciona como una terminal: cada cálculo aparece como un <b>bloque</b> en la lista.</p>"

            + "<table width='100%' cellspacing='8' cellpadding='10'><tr>"
            + tarjeta("Derivada", "f'(x)", "Cómo cambia la función.")
            + tarjeta("Integral indefinida", "∫ f(x) dx", "La primitiva F(x) + C.")
            + tarjeta("Integral definida", "∫ de a a b", "El valor numérico entre a y b.")
            + "</tr></table>"

            + "<h3>Inicio rápido</h3>"
            + "<table width='100%' cellspacing='0' cellpadding='6'>"
            + paso("1", "En la barra de abajo, elige la operación: <b>derivada</b>, "
                    + "<b>∫ indefinida</b> o <b>∫ definida</b>.")
            + paso("2", "Elige la variable en <b>var</b>: <b>x</b> o <b>y</b>.")
            + paso("3", "Escribe la función después del símbolo <code>❯</code>, "
                    + "o pulsa uno de los <b>ejemplos</b> del primer bloque.")
            + paso("4", "Para la integral definida, escribe también los límites <b>a</b> y <b>b</b>.")
            + paso("5", "Pulsa <b>Enter</b> (o <b>Calcular</b>). El resultado aparece como un bloque nuevo.")
            + "</table>"

            + "<h3>Atajos de teclado</h3>"
            + "<table width='100%' cellspacing='0' cellpadding='7'>"
            + filaTitulo("Tecla", "Acción")
            + fila("<code>Enter</code>", "Calcular")
            + fila("<code>Ctrl + P</code>", "Ver los pasos del procedimiento")
            + fila("<code>Ctrl + L</code>", "Limpiar todos los bloques y empezar de nuevo")
            + fila("<code>F1</code>", "Abrir este manual")
            + "</table>";

    private static final String ESCRIBIR =
            "<h2>Cómo escribir la función</h2>"
            + "<p>La calculadora entiende <b>polinomios</b>: sumas y restas de términos "
            + "como <code>3x^2</code>, <code>-5x</code> o <code>7</code>.</p>"

            + "<h3>Reglas</h3>"
            + "<table width='100%' cellspacing='0' cellpadding='7'>"
            + filaTitulo("Para escribir...", "Usa", "Ejemplo")
            + fila("Una potencia", "el símbolo <b>^</b>", "<code>x^3</code>")
            + fila("Un coeficiente", "el número pegado a la variable, <b>sin *</b>", "<code>5x</code>")
            + fila("Un decimal", "<b>punto</b>, no coma", "<code>2.5x^2</code>")
            + fila("Un exponente negativo", "<b>^-</b>", "<code>x^-2</code>")
            + fila("Una constante", "solo el número", "<code>7</code>")
            + "</table>"
            + "<p>Los espacios no importan: <code>3x^2+5x</code> y <code>3x^2 + 5x</code> son iguales. "
            + "Usa solo la variable que elegiste (x o y).</p>"

            + "<h3>Ejemplos</h3>"
            + "<table width='100%' cellspacing='8' cellpadding='10'><tr>"
            + "<td width='50%' bgcolor='" + C_FONDO_OK + "' valign='top'>"
            + "<p class='ok'><b>✔ Válidos</b></p>"
            + "<p><code>3x^2 + 5x - 2</code><br><code>-y^3 + 4y</code><br>"
            + "<code>x^-2 + 1</code><br><code>0.5x^4 - x</code></p></td>"
            + "<td width='50%' bgcolor='" + C_FONDO_MAL + "' valign='top'>"
            + "<p class='mal'><b>✘ No válidos</b></p>"
            + "<p><code>3*x</code> &nbsp;<span class='suave'>usa 3x</span><br>"
            + "<code>x^2.5</code> &nbsp;<span class='suave'>exponente decimal</span><br>"
            + "<code>(x+1)^2</code> &nbsp;<span class='suave'>paréntesis</span><br>"
            + "<code>sin(x)</code> &nbsp;<span class='suave'>no es polinomio</span></p></td>"
            + "</tr></table>";

    private static final String OPERACIONES =
            "<h2>Operaciones</h2>"

            + "<h3>Derivada</h3>"
            + "<p>Aplica la regla de la potencia a cada término: "
            + "<code>d/dx (a·x^n) = n·a·x^(n-1)</code>. La derivada de una constante es 0.</p>"
            + ejemplo("3x^2 + 5x - 2", "f'(x) = 6x + 5")

            + "<h3>Integral indefinida</h3>"
            + "<p>Integra cada término con <code>∫ a·x^n dx = a·x^(n+1) / (n+1)</code> "
            + "y agrega la constante de integración <b>C</b>.</p>"
            + ejemplo("3x^2", "F(x) = x^3 + C")

            + "<h3>Integral definida</h3>"
            + "<p>Calcula la primitiva F y aplica el teorema fundamental del cálculo: "
            + "<code>F(b) - F(a)</code>. Escribe <b>a</b> (límite inferior) y <b>b</b> "
            + "(límite superior); los límites aceptan decimales con punto o coma.</p>"
            + ejemplo("x^2 + 2x,  a = 0,  b = 3", "Resultado = 18")
            + nota("Si <b>a</b> es mayor que <b>b</b>, el resultado sale con signo negativo. "
            + "Es correcto, no es un error.")

            + "<h3>Pasos del procedimiento</h3>"
            + "<p>El botón <b>Pasos</b> (o <code>Ctrl + P</code>) agrega un bloque con el procedimiento "
            + "completo: la regla que se aplica, la cuenta de cada término y cómo se llega al resultado. "
            + "Por ejemplo, para la derivada de <code>3x^2</code>:</p>"
            + "<table width='100%' cellpadding='10'><tr><td bgcolor='" + C_SUPERFICIE + "'><code>"
            + "d/dx (3x^2) = 2·3x^(2-1) = 6x</code></td></tr></table>";

    private static final String ERRORES =
            "<h2>Mensajes de error</h2>"
            + "<p>Cuando algo no se puede calcular, aparece un bloque con borde "
            + "<span class='mal'><b>rojo</b></span> y el símbolo <span class='mal'><b>×</b></span> "
            + "que explica el motivo.</p>"
            + "<table width='100%' cellspacing='0' cellpadding='7'>"
            + filaTitulo("Mensaje", "Por qué ocurre", "Qué hacer")
            + fila("<b>Término inválido</b> o <b>Carácter no permitido</b>",
                    "La función no sigue las reglas de escritura.",
                    "Revisa la sección <i>Escribir funciones</i>.")
            + fila("<b>Escribe una función</b>",
                    "La función está vacía.",
                    "Escribe una función o pulsa un ejemplo.")
            + fila("<b>El límite está vacío</b> o <b>no es un número válido</b>",
                    "Falta a o b, o tienen letras.",
                    "Escribe números, por ejemplo <code>0</code> o <code>2.5</code>.")
            + fila("<b>La integral de x^-1 ...</b>",
                    "Su resultado es ln|x|, que no es un polinomio.",
                    "Esta operación no está soportada.")
            + fila("<b>La función no está definida en x = 0</b>",
                    "Hay exponentes negativos y el intervalo [a, b] incluye el 0.",
                    "Usa un intervalo que no incluya el 0.")
            + "</table>";

    // ---------------- Ventana ----------------

    public VentanaManual(JFrame propietario) {
        super(propietario, "Manual de usuario", false);
        inicializarComponentes();
        setLocationRelativeTo(propietario);
    }

    private void inicializarComponentes() {
        JTabbedPane pestanas = new JTabbedPane(JTabbedPane.LEFT);
        pestanas.setFont(Estilo.fuente(Font.PLAIN, 14));
        pestanas.setBackground(Estilo.BARRA);
        pestanas.putClientProperty("JTabbedPane.tabAreaAlignment", "leading");
        pestanas.putClientProperty("JTabbedPane.minimumTabWidth", 170);
        pestanas.addTab("Inicio", crearSeccion(INICIO));
        pestanas.addTab("Escribir funciones", crearSeccion(ESCRIBIR));
        pestanas.addTab("Operaciones", crearSeccion(OPERACIONES));
        pestanas.addTab("Errores", crearSeccion(ERRORES));

        JButton btnCerrar = Estilo.botonPrimario("Cerrar");
        btnCerrar.setPreferredSize(new Dimension(110, 32));
        btnCerrar.addActionListener(e -> setVisible(false));
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 10));
        panelBoton.setBackground(Estilo.SUPERFICIE);
        panelBoton.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Estilo.BORDE));
        panelBoton.add(btnCerrar);

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setBackground(Estilo.FONDO);
        contenido.add(Estilo.barraSuperior("manual de usuario",
                "todo lo que necesitas para usar la calculadora", null), BorderLayout.NORTH);
        contenido.add(pestanas, BorderLayout.CENTER);
        contenido.add(panelBoton, BorderLayout.SOUTH);
        setContentPane(contenido);

        setSize(860, 620);
        setMinimumSize(new Dimension(660, 440));
    }

    private JScrollPane crearSeccion(String html) {
        // Cada sección tiene su propia hoja de estilos para no cambiar las de Swing.
        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet css = new StyleSheet();
        css.addRule("body { font-family: 'Segoe UI', sans-serif; font-size: 12pt; color: #C9CED8;"
                + " margin: 16px 22px; }");
        css.addRule("h2 { font-family: '" + Estilo.FUENTE_TERMINAL + "'; font-size: 17pt; color: "
                + C_ACENTO + "; margin-top: 0; margin-bottom: 6px; }");
        css.addRule("h3 { font-family: '" + Estilo.FUENTE_TERMINAL + "'; font-size: 13pt; color: #E6E8EE;"
                + " margin-top: 16px; margin-bottom: 4px; }");
        css.addRule("p { margin-top: 4px; margin-bottom: 6px; }");
        css.addRule("b { color: #E6E8EE; }");
        css.addRule("code { font-family: '" + Estilo.FUENTE_TERMINAL + "'; font-size: 12pt; color: "
                + C_ACENTO + "; }");
        css.addRule(".ok { color: #7EE787; }");
        css.addRule(".mal { color: #FF7B72; }");
        css.addRule(".suave { color: #8A91A0; }");
        kit.setStyleSheet(css);

        JEditorPane texto = new JEditorPane();
        texto.setEditorKit(kit);
        texto.setText("<html><body>" + html + "</body></html>");
        texto.setEditable(false);
        texto.setBackground(Estilo.FONDO);
        // FlatLaf pinta con otro color los textos no editables; aquí se quiere el fondo oscuro.
        texto.putClientProperty("FlatLaf.style", "inactiveBackground: #0E1015");
        texto.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(texto);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    // ---------------- Bloques de HTML reutilizables ----------------

    private static String tarjeta(String titulo, String simbolo, String descripcion) {
        return "<td width='33%' bgcolor='" + C_SUPERFICIE + "' valign='top'>"
                + "<p><b>" + titulo + "</b></p>"
                + "<p><code>" + simbolo + "</code></p>"
                + "<p class='suave'>" + descripcion + "</p></td>";
    }

    private static String paso(String numero, String texto) {
        return "<tr><td width='30' align='center' bgcolor='" + C_SUPERFICIE_ALTA + "'>"
                + "<code><b>" + numero + "</b></code></td>"
                + "<td>" + texto + "</td></tr>";
    }

    private static String filaTitulo(String... columnas) {
        StringBuilder sb = new StringBuilder("<tr>");
        for (String columna : columnas) {
            sb.append("<td bgcolor='").append(C_SUPERFICIE_ALTA).append("'><code><b>")
                    .append(columna).append("</b></code></td>");
        }
        return sb.append("</tr>").toString();
    }

    private static String fila(String... columnas) {
        StringBuilder sb = new StringBuilder("<tr>");
        for (String columna : columnas) {
            sb.append("<td bgcolor='").append(C_SUPERFICIE).append("' valign='top'>")
                    .append(columna).append("</td>");
        }
        return sb.append("</tr>").toString();
    }

    private static String ejemplo(String entrada, String salida) {
        return "<table width='100%' cellspacing='0' cellpadding='8'><tr>"
                + "<td width='50%' bgcolor='" + C_SUPERFICIE + "'><span class='suave'>❯ escribes</span><br>"
                + "<code>" + entrada + "</code></td>"
                + "<td width='50%' bgcolor='" + C_FONDO_OK + "'><span class='suave'>obtienes</span><br>"
                + "<span class='ok'><code><b>" + salida + "</b></code></span></td>"
                + "</tr></table>";
    }

    private static String nota(String texto) {
        return "<table width='100%' cellpadding='10'><tr><td bgcolor='" + C_FONDO_NOTA + "'>"
                + "<font color='#E3B341'><b>Nota:</b></font> " + texto + "</td></tr></table>";
    }
}
