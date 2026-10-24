package vista;

import exceptions.CalculadoraException;
import logic.Calculadora;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Ventana principal de la calculadora, con estilo de terminal (inspirado en Warp):
 * cada cálculo aparece como un "bloque" en la lista y la función se escribe abajo,
 * en la barra de entrada.
 * Solo se comunica con la fachada Calculadora: le pasa el texto que escribió
 * el usuario y muestra el String que devuelve (o el mensaje de error).
 */
public class VentanaPrincipal extends JFrame {

    /** Ejemplos que el usuario puede pulsar para probar rápido (se escriben con x). */
    private static final String[] EJEMPLOS = {"3x^2 + 5x - 2", "x^3 - 4x + 1", "2x^-2 + 6"};

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Calculadora calculadora;

    private PanelBloques panelBloques;
    private JScrollPane scrollBloques;
    private JTextField txtFuncion;
    private JToggleButton tglX;
    private JToggleButton tglDerivar;
    private JToggleButton tglIndefinida;
    private JToggleButton tglDefinida;
    private JPanel panelLimites;
    private JTextField txtLimiteA;
    private JTextField txtLimiteB;

    private VentanaManual ventanaManual;

    public VentanaPrincipal() {
        this.calculadora = new Calculadora();
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setTitle("Calculadora de Derivadas e Integrales");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton btnManual = Estilo.botonSecundario("? Manual  F1");
        btnManual.addActionListener(e -> abrirManual());

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilo.FONDO);
        raiz.add(Estilo.barraSuperior("calculadora", "derivadas e integrales de polinomios", btnManual),
                BorderLayout.NORTH);
        raiz.add(crearAreaBloques(), BorderLayout.CENTER);
        raiz.add(crearBarraEntrada(), BorderLayout.SOUTH);
        setContentPane(raiz);

        registrarAtajos();
        actualizarLimites();
        agregarBloqueBienvenida();

        setSize(900, 680);
        setMinimumSize(new Dimension(760, 520));
        setLocationRelativeTo(null);
        SwingUtilities.invokeLater(() -> txtFuncion.requestFocusInWindow());
    }

    // ---------------- Construcción de la interfaz ----------------

    private JScrollPane crearAreaBloques() {
        panelBloques = new PanelBloques();
        scrollBloques = new JScrollPane(panelBloques);
        scrollBloques.setBorder(BorderFactory.createEmptyBorder());
        scrollBloques.getViewport().setBackground(Estilo.FONDO);
        scrollBloques.getVerticalScrollBar().setUnitIncrement(16);
        return scrollBloques;
    }

    private JPanel crearBarraEntrada() {
        // Fila 1: operación, variable y límites
        tglDerivar = Estilo.opcion("derivada  f'(x)");
        tglIndefinida = Estilo.opcion("∫ indefinida");
        tglDefinida = Estilo.opcion("∫ definida  [a, b]");
        ButtonGroup grupoOperacion = new ButtonGroup();
        for (JToggleButton boton : new JToggleButton[]{tglDerivar, tglIndefinida, tglDefinida}) {
            grupoOperacion.add(boton);
            boton.addActionListener(e -> actualizarLimites());
        }
        tglDerivar.setSelected(true);

        tglX = Estilo.opcion("x");
        JToggleButton tglY = Estilo.opcion("y");
        ButtonGroup grupoVariable = new ButtonGroup();
        grupoVariable.add(tglX);
        grupoVariable.add(tglY);
        tglX.setSelected(true);

        txtLimiteA = Estilo.campoTerminal(5, 14);
        txtLimiteB = Estilo.campoTerminal(5, 14);
        txtLimiteA.setHorizontalAlignment(SwingConstants.CENTER);
        txtLimiteB.setHorizontalAlignment(SwingConstants.CENTER);
        txtLimiteA.putClientProperty("JTextField.placeholderText", "a");
        txtLimiteB.putClientProperty("JTextField.placeholderText", "b");

        panelLimites = filaTransparente(FlowLayout.LEFT, 6);
        panelLimites.add(Estilo.texto("   a =", Estilo.TEXTO_SUAVE, Font.PLAIN, 13));
        panelLimites.add(txtLimiteA);
        panelLimites.add(Estilo.texto(" b =", Estilo.TEXTO_SUAVE, Font.PLAIN, 13));
        panelLimites.add(txtLimiteB);

        JPanel filaOpciones = filaTransparente(FlowLayout.LEFT, 6);
        filaOpciones.add(tglDerivar);
        filaOpciones.add(tglIndefinida);
        filaOpciones.add(tglDefinida);
        filaOpciones.add(Estilo.texto("   var", Estilo.TEXTO_SUAVE, Font.PLAIN, 13));
        filaOpciones.add(tglX);
        filaOpciones.add(tglY);
        filaOpciones.add(panelLimites);

        // Fila 2: prompt + función + botones
        JLabel prompt = Estilo.texto("❯", Estilo.ACENTO, Font.BOLD, 20);

        txtFuncion = Estilo.campoTerminal(30, 18);
        txtFuncion.putClientProperty("JTextField.placeholderText", "escribe una función, por ejemplo 3x^2 + 5x - 2");
        txtFuncion.putClientProperty("JTextField.showClearButton", true);
        txtFuncion.putClientProperty("FlatLaf.style",
                "background: #161922; foreground: #E6E8EE; borderWidth: 0; focusWidth: 0;"
                        + " margin: 4,6,4,6; placeholderForeground: #5C6370");
        txtFuncion.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        JButton btnCalcular = Estilo.botonPrimario("Calcular ⏎");
        JButton btnPasos = Estilo.botonSecundario("Pasos");
        JButton btnLimpiar = Estilo.botonSecundario("Limpiar");
        btnCalcular.addActionListener(e -> calcular());
        btnPasos.addActionListener(e -> verPasos());
        btnLimpiar.addActionListener(e -> limpiar());
        // Presionar Enter en cualquier campo equivale a pulsar "Calcular".
        getRootPane().setDefaultButton(btnCalcular);

        JPanel botones = filaTransparente(FlowLayout.RIGHT, 8);
        botones.add(btnCalcular);
        botones.add(btnPasos);
        botones.add(btnLimpiar);

        JPanel filaComando = new JPanel(new BorderLayout(10, 0));
        filaComando.setOpaque(false);
        filaComando.add(prompt, BorderLayout.WEST);
        filaComando.add(txtFuncion, BorderLayout.CENTER);
        filaComando.add(botones, BorderLayout.EAST);

        // Fila 3: atajos de teclado
        JLabel atajos = Estilo.texto(
                "enter calcular   ·   ctrl+p pasos   ·   ctrl+l limpiar   ·   f1 manual",
                new Color(0x5C6370), Font.PLAIN, 12);

        JPanel barra = new JPanel();
        barra.setLayout(new BoxLayout(barra, BoxLayout.Y_AXIS));
        barra.setBackground(Estilo.SUPERFICIE);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Estilo.BORDE),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        for (JComponent fila : new JComponent[]{filaOpciones, filaComando, atajos}) {
            fila.setAlignmentX(Component.LEFT_ALIGNMENT);
            barra.add(fila);
            barra.add(Box.createVerticalStrut(8));
        }
        return barra;
    }

    private static JPanel filaTransparente(int alineacion, int separacion) {
        JPanel panel = new JPanel(new FlowLayout(alineacion, separacion, 0));
        panel.setOpaque(false);
        return panel;
    }

    private void registrarAtajos() {
        JComponent raiz = getRootPane();
        atajo(raiz, KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK), "pasos", this::verPasos);
        atajo(raiz, KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK), "limpiar", this::limpiar);
        atajo(raiz, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "manual", this::abrirManual);
    }

    private static void atajo(JComponent componente, KeyStroke tecla, String nombre, Runnable accion) {
        componente.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(tecla, nombre);
        componente.getActionMap().put(nombre, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accion.run();
            }
        });
    }

    // ---------------- Bloques ----------------

    private void agregarBloqueBienvenida() {
        Estilo.Tarjeta bloque = new Estilo.Tarjeta(new BorderLayout(0, 8));

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setOpaque(false);
        textos.add(Estilo.texto("Calculadora de Derivadas e Integrales", Estilo.ACENTO, Font.BOLD, 16));
        textos.add(Box.createVerticalStrut(4));
        textos.add(Estilo.texto("Elige la operación, escribe un polinomio abajo y pulsa Enter.",
                Estilo.TEXTO_SUAVE, Font.PLAIN, 13));
        bloque.add(textos, BorderLayout.NORTH);

        JPanel ejemplos = filaTransparente(FlowLayout.LEFT, 6);
        ejemplos.add(Estilo.texto("ejemplos:", Estilo.TEXTO_SUAVE, Font.PLAIN, 13));
        for (String ejemplo : EJEMPLOS) {
            JButton chip = Estilo.chip(ejemplo);
            chip.setToolTipText("Usar este ejemplo");
            chip.addActionListener(e -> usarEjemplo(ejemplo));
            ejemplos.add(chip);
        }
        bloque.add(ejemplos, BorderLayout.CENTER);

        panelBloques.agregar(bloque);
    }

    /**
     * Agrega un bloque como los de Warp: cabecera (operación y hora), el "comando"
     * con el prompt y debajo la salida (resultado, pasos o error).
     */
    private void agregarBloque(String funcion, String salida, boolean esError) {
        Estilo.Tarjeta bloque = new Estilo.Tarjeta(new BorderLayout(0, 6));
        if (esError) {
            bloque.setColores(new Color(0x1A1214), Estilo.BORDE_ERROR);
        }

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);
        cabecera.add(Estilo.texto(descripcionOperacion(), Estilo.TEXTO_SUAVE, Font.PLAIN, 12),
                BorderLayout.WEST);
        cabecera.add(Estilo.texto(LocalTime.now().format(FORMATO_HORA), Estilo.TEXTO_SUAVE, Font.PLAIN, 12),
                BorderLayout.EAST);

        JPanel comando = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        comando.setOpaque(false);
        comando.add(Estilo.texto("❯", Estilo.ACENTO, Font.BOLD, 15));
        comando.add(Estilo.texto(funcion.trim().isEmpty() ? "(vacío)" : funcion.trim(),
                Estilo.TEXTO, Font.PLAIN, 15));

        JPanel arriba = new JPanel(new BorderLayout(0, 6));
        arriba.setOpaque(false);
        arriba.add(cabecera, BorderLayout.NORTH);
        arriba.add(comando, BorderLayout.CENTER);
        bloque.add(arriba, BorderLayout.NORTH);

        // JTextArea para que la salida pueda tener varias líneas (los pasos) y partir las largas.
        JTextArea areaSalida = new JTextArea(esError ? "× " + salida : salida.trim());
        areaSalida.setEditable(false);
        areaSalida.setOpaque(false);
        areaSalida.setLineWrap(true);
        areaSalida.setWrapStyleWord(true);
        areaSalida.setBorder(BorderFactory.createEmptyBorder(2, 22, 0, 0));
        areaSalida.setFont(Estilo.fuente(Font.PLAIN, 15));
        areaSalida.setForeground(esError ? Estilo.ERROR : Estilo.TEXTO);
        // Un resultado de una sola línea se resalta en verde y negrita.
        if (!esError && !salida.trim().contains("\n")) {
            areaSalida.setFont(Estilo.fuente(Font.BOLD, 17));
            areaSalida.setForeground(Estilo.EXITO);
        }
        bloque.add(areaSalida, BorderLayout.CENTER);

        panelBloques.agregar(bloque);
        // Igual que en una terminal: se baja hasta el último bloque.
        SwingUtilities.invokeLater(() -> {
            Rectangle zona = bloque.getBounds();
            panelBloques.scrollRectToVisible(new Rectangle(0, zona.y, 1, zona.height));
        });
    }

    private String descripcionOperacion() {
        String operacion;
        if (tglDerivar.isSelected()) {
            operacion = "derivada";
        } else if (tglIndefinida.isSelected()) {
            operacion = "integral indefinida";
        } else {
            operacion = "integral definida  a = " + txtLimiteA.getText().trim()
                    + ", b = " + txtLimiteB.getText().trim();
        }
        return operacion + "  ·  var " + variableSeleccionada();
    }

    // ---------------- Acciones ----------------

    private char variableSeleccionada() {
        return tglX.isSelected() ? 'x' : 'y';
    }

    /** Los límites solo se muestran para la integral definida. */
    private void actualizarLimites() {
        panelLimites.setVisible(tglDefinida.isSelected());
        if (tglDefinida.isSelected()) {
            txtLimiteA.requestFocusInWindow();
        }
    }

    /** Los ejemplos están escritos con x; se adaptan si la variable elegida es y. */
    private void usarEjemplo(String ejemplo) {
        txtFuncion.setText(ejemplo.replace('x', variableSeleccionada()));
        txtFuncion.requestFocusInWindow();
    }

    private void calcular() {
        String funcion = txtFuncion.getText();
        char variable = variableSeleccionada();

        try {
            String resultado;
            if (tglDerivar.isSelected()) {
                resultado = calculadora.derivar(funcion, variable);
            } else if (tglIndefinida.isSelected()) {
                resultado = calculadora.integrarIndefinida(funcion, variable);
            } else {
                resultado = calculadora.integrarDefinida(
                        funcion, variable, txtLimiteA.getText(), txtLimiteB.getText());
            }
            agregarBloque(funcion, resultado, false);
        } catch (CalculadoraException e) {
            agregarBloque(funcion, e.getMessage(), true);
        } catch (RuntimeException e) {
            // Red de seguridad: ningún error inesperado debe cerrar la ventana.
            agregarBloque(funcion, "No se pudo realizar la operación: " + e.getMessage(), true);
        }
    }

    /** Agrega un bloque con el procedimiento completo de la operación elegida. */
    private void verPasos() {
        String funcion = txtFuncion.getText();
        char variable = variableSeleccionada();

        try {
            String pasos;
            if (tglDerivar.isSelected()) {
                pasos = calculadora.pasosDerivar(funcion, variable);
            } else if (tglIndefinida.isSelected()) {
                pasos = calculadora.pasosIntegrarIndefinida(funcion, variable);
            } else {
                pasos = calculadora.pasosIntegrarDefinida(
                        funcion, variable, txtLimiteA.getText(), txtLimiteB.getText());
            }
            agregarBloque(funcion, pasos, false);
        } catch (CalculadoraException e) {
            agregarBloque(funcion, e.getMessage(), true);
        } catch (RuntimeException e) {
            agregarBloque(funcion, "No se pudo realizar la operación: " + e.getMessage(), true);
        }
    }

    /** Como "clear" en una terminal: borra los bloques y deja todo como al inicio. */
    private void limpiar() {
        panelBloques.vaciar();
        agregarBloqueBienvenida();
        txtFuncion.setText("");
        txtLimiteA.setText("");
        txtLimiteB.setText("");
        tglDerivar.setSelected(true);
        tglX.setSelected(true);
        actualizarLimites();
        txtFuncion.requestFocusInWindow();
    }

    private void abrirManual() {
        if (ventanaManual == null) {
            ventanaManual = new VentanaManual(this);
        }
        ventanaManual.setVisible(true);
        ventanaManual.toFront();
    }

    // ---------------- Lista de bloques ----------------

    /**
     * Columna de bloques que siempre ocupa el ancho visible (así el texto largo
     * se parte en líneas en lugar de crear una barra de desplazamiento horizontal).
     */
    private static class PanelBloques extends JPanel implements Scrollable {

        PanelBloques() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(Estilo.FONDO);
            setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        }

        void agregar(JComponent bloque) {
            if (getComponentCount() > 0) {
                add(Box.createVerticalStrut(10));
            }
            bloque.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(bloque);
            revalidate();
            repaint();
        }

        void vaciar() {
            removeAll();
            revalidate();
            repaint();
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visible, int orientacion, int direccion) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visible, int orientacion, int direccion) {
            return visible.height - 32;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
