package vista;

import exceptions.CalculadoraException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import logic.Calculadora;

/**
 * Ventana principal de la calculadora, con estilo de terminal (inspirado en
 * Warp):
 * cada cálculo aparece como un "bloque" en la lista y la función se escribe
 * abajo,
 * en la barra de entrada.
 * Solo se comunica con la fachada Calculadora: le pasa el texto que escribió
 * el usuario y muestra el String que devuelve (o el mensaje de error).
 */
public class VentanaPrincipal extends JFrame {

    /**
     * Ejemplos que el usuario puede pulsar para probar rápido (se escriben con x).
     */
    private static final String[] EJEMPLOS = { "3x^2 + 5x - 2", "x^3 - 4x + 1", "2x^-2 + 6" };

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final String TEXTO_VER_PASOS = "▸ ver pasos";
    private static final String TEXTO_OCULTAR_PASOS = "▾ ocultar pasos";
    private static final String TEXTO_VER_GRAFICA = "▸ ver gráfica";
    private static final String TEXTO_OCULTAR_GRAFICA = "▾ ocultar gráfica";

    private enum Operacion {
        DERIVADA("derivada"),
        INDEFINIDA("integral indefinida"),
        DEFINIDA("integral definida");

        final String nombre;

        Operacion(String nombre) {
            this.nombre = nombre;
        }
    }

    /**
     * Un cálculo con los datos con que se hizo. Se guardan para poder mostrar sus
     * pasos después, aunque el usuario ya haya cambiado la operación o los campos.
     */
    private static class Calculo {
        final Operacion operacion;
        final String funcion;
        final char variable;
        final String limiteA;
        final String limiteB;
        final String hora = LocalTime.now().format(FORMATO_HORA);

        // Solo para cálculos resueltos: el bloque y la parte de pasos que se despliega.
        Estilo.Tarjeta bloque;
        JButton btnPasos;
        JTextArea areaPasos;
        JButton btnGrafica;
        PanelGrafica grafica;
        /**
         * true si la gráfica se abrió sola (por ser el último resultado) y no a mano.
         */
        boolean graficaAutomatica;

        Calculo(Operacion operacion, String funcion, char variable, String limiteA, String limiteB) {
            this.operacion = operacion;
            this.funcion = funcion.trim();
            this.variable = variable;
            this.limiteA = limiteA.trim();
            this.limiteB = limiteB.trim();
        }
    }

    private final Calculadora calculadora;
    /** Cálculos resueltos con éxito, en el orden en que se hicieron. */
    private final List<Calculo> resueltos = new ArrayList<>();
    /**
     * Último cálculo cuya gráfica se abrió sola; se recoge cuando llega uno nuevo.
     */
    private Calculo ultimoConGraficaAutomatica;

    private PanelBloques panelBloques;
    private JTextField txtFuncion;
    private JToggleButton tglX;
    private JToggleButton tglDerivar;
    private JToggleButton tglIndefinida;
    private JToggleButton tglDefinida;
    private JPanel panelLimites;
    private JTextField txtLimiteA;
    private JTextField txtLimiteB;
    private JButton btnPasos;

    private VentanaManual ventanaManual;

    public VentanaPrincipal() {
        this.calculadora = new Calculadora();
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setTitle("Cuentas Claras");
        java.net.URL url = getClass().getResource("/resources/icono.png");
        if (url != null) {
            setIconImage(new javax.swing.ImageIcon(url).getImage());
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton btnBorrar = Estilo.botonSecundario("Borrar historial");
        btnBorrar.setToolTipText("Borrar todos los resultados y empezar de nuevo (Ctrl+L)");
        btnBorrar.addActionListener(e -> limpiar());
        JButton btnManual = Estilo.botonSecundario("? Manual  F1");
        btnManual.addActionListener(e -> abrirManual());

        // Borrar y Manual van juntos arriba a la derecha.
        JPanel botonesSuperiores = filaTransparente(FlowLayout.RIGHT, 8);
        botonesSuperiores.add(btnBorrar);
        botonesSuperiores.add(btnManual);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilo.FONDO);
        raiz.add(Estilo.barraSuperior("Cuentas Claras", "derivadas e integrales de polinomios", botonesSuperiores),
                BorderLayout.NORTH);
        raiz.add(crearAreaBloques(), BorderLayout.CENTER);
        raiz.add(crearBarraEntrada(), BorderLayout.SOUTH);
        setContentPane(raiz);

        registrarAtajos();
        actualizarLimites();
        agregarBloqueBienvenida();

        setMinimumSize(new Dimension(760, 520));
        ajustarTamanoInicial();
        SwingUtilities.invokeLater(() -> txtFuncion.requestFocusInWindow());
    }

    /**
     * La ventana abre con 890 px de ancho, centrada, y con todo el alto disponible
     * de la pantalla (sin tapar la barra de tareas), en cualquier resolución.
     */
    private void ajustarTamanoInicial() {
        Rectangle pantalla = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int ancho = Math.min(890, pantalla.width);
        setBounds(pantalla.x + (pantalla.width - ancho) / 2, pantalla.y, ancho, pantalla.height);
    }

    // ---------------- Construcción de la interfaz ----------------

    private JScrollPane crearAreaBloques() {
        panelBloques = new PanelBloques();
        JScrollPane scrollBloques = new JScrollPane(panelBloques);
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
        for (JToggleButton boton : new JToggleButton[] { tglDerivar, tglIndefinida, tglDefinida }) {
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
        // Botón redondo con una × para borrar la función; solo se ve cuando hay texto.
        JButton btnBorrarFuncion = Estilo.botonCerrarCircular();
        btnBorrarFuncion.setToolTipText("Borrar la función");
        btnBorrarFuncion.setVisible(false);
        btnBorrarFuncion.addActionListener(e -> {
            txtFuncion.setText("");
            txtFuncion.requestFocusInWindow();
        });
        txtFuncion.putClientProperty("JTextField.trailingComponent", btnBorrarFuncion);
        txtFuncion.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                btnBorrarFuncion.setVisible(!txtFuncion.getText().isEmpty());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                btnBorrarFuncion.setVisible(!txtFuncion.getText().isEmpty());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Solo cambia el formato del texto, no su contenido.
            }
        });
        txtFuncion.putClientProperty("FlatLaf.style",
                "background: #161922; foreground: #E6E8EE; borderWidth: 0; focusWidth: 0;"
                        + " margin: 4,6,4,6; placeholderForeground: #5C6370");
        txtFuncion.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        JButton btnCalcular = Estilo.botonPrimario("Calcular ⏎");
        btnPasos = Estilo.botonSecundario("Pasos ▴");
        btnCalcular.addActionListener(e -> calcular());
        btnPasos.setToolTipText("Elegir de qué resultado ver el procedimiento");
        btnPasos.addActionListener(e -> abrirMenuPasos());
        // Presionar Enter en cualquier campo equivale a pulsar "Calcular".
        getRootPane().setDefaultButton(btnCalcular);

        JPanel botones = filaTransparente(FlowLayout.RIGHT, 8);
        botones.add(btnCalcular);
        botones.add(btnPasos);

        JPanel filaComando = new JPanel(new BorderLayout(10, 0));
        filaComando.setOpaque(false);
        filaComando.add(prompt, BorderLayout.WEST);
        filaComando.add(txtFuncion, BorderLayout.CENTER);
        filaComando.add(botones, BorderLayout.EAST);

        // Fila 3: atajos de teclado
        JLabel atajos = Estilo.texto(
                "enter calcular   ·   ctrl+p elegir pasos   ·   ctrl+l borrar   ·   f1 manual",
                Color.WHITE, Font.PLAIN, 12);

        JPanel barra = new JPanel();
        barra.setLayout(new BoxLayout(barra, BoxLayout.Y_AXIS));
        barra.setBackground(Estilo.SUPERFICIE);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Estilo.BORDE),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        for (JComponent fila : new JComponent[] { filaOpciones, filaComando, atajos }) {
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
        atajo(raiz, KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK), "pasos", this::abrirMenuPasos);
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
        textos.add(Estilo.texto("Cuentas Claras", Estilo.ACENTO, Font.BOLD, 16));
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
     * Bloque como los de Warp: cabecera (operación y hora), el "comando" con el
     * prompt y debajo la salida. Si el cálculo salió bien, el bloque además tiene
     * el botón "ver pasos" (que despliega el procedimiento) y la gráfica.
     */
    private void agregarBloque(Calculo calculo, String salida, boolean esError) {
        Estilo.Tarjeta bloque = new Estilo.Tarjeta(new BorderLayout(0, 6));
        if (esError) {
            bloque.setColores(new Color(0x1A1214), Estilo.BORDE_ERROR);
        }

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);
        cabecera.add(Estilo.texto(descripcion(calculo), Estilo.TEXTO_SUAVE, Font.PLAIN, 12), BorderLayout.WEST);
        cabecera.add(Estilo.texto(calculo.hora, Estilo.TEXTO_SUAVE, Font.PLAIN, 12), BorderLayout.EAST);

        JPanel comando = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        comando.setOpaque(false);
        comando.add(Estilo.texto("❯", Estilo.ACENTO, Font.BOLD, 15));
        comando.add(Estilo.texto(calculo.funcion.isEmpty() ? "(vacío)" : calculo.funcion,
                Estilo.TEXTO, Font.PLAIN, 15));

        JPanel arriba = new JPanel(new BorderLayout(0, 6));
        arriba.setOpaque(false);
        arriba.add(cabecera, BorderLayout.NORTH);
        arriba.add(comando, BorderLayout.CENTER);
        bloque.add(arriba, BorderLayout.NORTH);

        // Debajo del comando, todo en columna: salida, botón de pasos, pasos y gráfica.
        JPanel cuerpo = new JPanel();
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
        cuerpo.setOpaque(false);
        bloque.add(cuerpo, BorderLayout.CENTER);

        JTextArea areaSalida = areaDeTexto(esError ? "× " + salida : salida);
        areaSalida.setFont(Estilo.fuente(esError ? Font.PLAIN : Font.BOLD, esError ? 15 : 17));
        areaSalida.setForeground(esError ? Estilo.ERROR : Estilo.EXITO);
        agregarEnColumna(cuerpo, areaSalida);

        if (!esError) {
            calculo.bloque = bloque;
            resueltos.add(calculo);

            calculo.btnPasos = Estilo.chip(TEXTO_VER_PASOS);
            calculo.btnPasos.setToolTipText("Mostrar cómo se llegó a este resultado");
            calculo.btnPasos.addActionListener(e -> alternarPasos(calculo));
            JPanel acciones = filaTransparente(FlowLayout.LEFT, 8);
            acciones.setBorder(BorderFactory.createEmptyBorder(6, 12, 2, 0));
            acciones.add(calculo.btnPasos);
            agregarEnColumna(cuerpo, acciones);

            calculo.areaPasos = areaDeTexto("");
            calculo.areaPasos.setFont(Estilo.fuente(Font.PLAIN, 14));
            calculo.areaPasos.setForeground(Estilo.TEXTO);
            calculo.areaPasos.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(6, 22, 4, 0),
                    BorderFactory.createMatteBorder(0, 2, 0, 0, Estilo.BORDE)));
            calculo.areaPasos.setVisible(false);
            agregarEnColumna(cuerpo, calculo.areaPasos);

            // Solo el último resultado muestra su gráfica de forma automática; en los
            // demás, el botón "ver gráfica" la despliega.
            calculo.grafica = graficaOpcional(calculo);
            if (calculo.grafica != null) {
                calculo.grafica.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
                agregarEnColumna(cuerpo, calculo.grafica);

                calculo.btnGrafica = Estilo.chip(TEXTO_VER_GRAFICA);
                calculo.btnGrafica.setToolTipText("Mostrar u ocultar la gráfica de este resultado");
                calculo.btnGrafica.addActionListener(e -> {
                    calculo.graficaAutomatica = false;
                    mostrarGrafica(calculo, !calculo.grafica.isVisible());
                });
                acciones.add(calculo.btnGrafica);

                if (ultimoConGraficaAutomatica != null && ultimoConGraficaAutomatica.graficaAutomatica) {
                    mostrarGrafica(ultimoConGraficaAutomatica, false);
                }
                calculo.graficaAutomatica = true;
                ultimoConGraficaAutomatica = calculo;
                mostrarGrafica(calculo, true);
            }
        }

        panelBloques.agregar(bloque);
        // Igual que en una terminal: se baja hasta el último bloque.
        SwingUtilities.invokeLater(() -> irABloque(bloque));
    }

    /**
     * JTextArea de solo lectura y transparente, que parte en líneas el texto largo.
     */
    private static JTextArea areaDeTexto(String texto) {
        JTextArea area = new JTextArea(texto.trim());
        area.setEditable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(2, 22, 0, 0));
        return area;
    }

    private static void agregarEnColumna(JPanel columna, JComponent componente) {
        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
        columna.add(componente);
    }

    private void irABloque(JComponent bloque) {
        Rectangle zona = bloque.getBounds();
        panelBloques.scrollRectToVisible(new Rectangle(0, zona.y, 1, zona.height));
    }

    private static String descripcion(Calculo calculo) {
        String texto = calculo.operacion.nombre;
        if (calculo.operacion == Operacion.DEFINIDA) {
            texto += "  a = " + calculo.limiteA + ", b = " + calculo.limiteB;
        }
        return texto + "  ·  var " + calculo.variable;
    }

    // ---------------- Cálculos (todo pasa por la Calculadora) ----------------

    private Calculo leerEntrada() {
        Operacion operacion = tglDerivar.isSelected() ? Operacion.DERIVADA
                : tglIndefinida.isSelected() ? Operacion.INDEFINIDA
                        : Operacion.DEFINIDA;
        return new Calculo(operacion, txtFuncion.getText(), tglX.isSelected() ? 'x' : 'y',
                txtLimiteA.getText(), txtLimiteB.getText());
    }

    private String resolver(Calculo c) throws CalculadoraException {
        switch (c.operacion) {
            case DERIVADA:
                return calculadora.derivar(c.funcion, c.variable);
            case INDEFINIDA:
                return calculadora.integrarIndefinida(c.funcion, c.variable);
            default:
                return calculadora.integrarDefinida(c.funcion, c.variable, c.limiteA, c.limiteB);
        }
    }

    private String pasos(Calculo c) throws CalculadoraException {
        switch (c.operacion) {
            case DERIVADA:
                return calculadora.pasosDerivar(c.funcion, c.variable);
            case INDEFINIDA:
                return calculadora.pasosIntegrarIndefinida(c.funcion, c.variable);
            default:
                return calculadora.pasosIntegrarDefinida(c.funcion, c.variable, c.limiteA, c.limiteB);
        }
    }

    /**
     * La gráfica es un extra: si por algo no se puede dibujar, el resultado se
     * muestra igual.
     */
    private PanelGrafica graficaOpcional(Calculo calculo) {
        try {
            return crearGrafica(calculo);
        } catch (CalculadoraException | RuntimeException e) {
            return null;
        }
    }

    /**
     * Gráfica según la operación: f y f' (derivada), f y F con C = 0 (indefinida),
     * o f con el área entre a y b sombreada (definida). Se llama solo después de
     * un cálculo exitoso, así que la función y los límites ya son válidos.
     */
    private PanelGrafica crearGrafica(Calculo c) throws CalculadoraException {
        double desde = -5;
        double hasta = 5;
        double a = 0;
        double b = 0;
        if (c.operacion == Operacion.DEFINIDA) {
            a = calculadora.parsearLimite(c.limiteA, "inferior");
            b = calculadora.parsearLimite(c.limiteB, "superior");
            // Se muestra el intervalo [a, b] con un poco de espacio a cada lado.
            double margen = Math.max(1, Math.abs(b - a) * 0.3);
            desde = Math.min(a, b) - margen;
            hasta = Math.max(a, b) + margen;
        }

        // Cada curva recibe cómo calcularse (siempre con la Calculadora); la gráfica
        // pide los valores con un punto por píxel según su ancho.
        PanelGrafica grafica = new PanelGrafica(desde, hasta);
        grafica.agregarSerie("f(" + c.variable + ")", Estilo.ACENTO,
                puntos -> calculadora.evaluar(c.funcion, c.variable, puntos));

        if (c.operacion == Operacion.DERIVADA) {
            grafica.agregarSerie("f'(" + c.variable + ")", Estilo.EXITO,
                    puntos -> calculadora.evaluarDerivada(c.funcion, c.variable, puntos));
        } else if (c.operacion == Operacion.INDEFINIDA) {
            grafica.agregarSerie("F(" + c.variable + ") con C = 0", Estilo.MORADO,
                    puntos -> calculadora.evaluarPrimitiva(c.funcion, c.variable, puntos));
        } else {
            grafica.sombrearArea(a, b);
        }
        return grafica;
    }

    // ---------------- Acciones ----------------

    /** Los límites solo se muestran para la integral definida. */
    private void actualizarLimites() {
        panelLimites.setVisible(tglDefinida.isSelected());
        if (tglDefinida.isSelected()) {
            txtLimiteA.requestFocusInWindow();
        }
    }

    /**
     * Los ejemplos están escritos con x; se adaptan si la variable elegida es y.
     */
    private void usarEjemplo(String ejemplo) {
        txtFuncion.setText(ejemplo.replace('x', tglX.isSelected() ? 'x' : 'y'));
        txtFuncion.requestFocusInWindow();
    }

    /**
     * Calcula lo que hay en la barra de entrada. Devuelve el cálculo si salió bien,
     * o null.
     */
    private Calculo calcular() {
        Calculo calculo = leerEntrada();
        try {
            agregarBloque(calculo, resolver(calculo), false);
            return calculo;
        } catch (CalculadoraException e) {
            agregarBloque(calculo, e.getMessage(), true);
        } catch (RuntimeException e) {
            // Red de seguridad: ningún error inesperado debe cerrar la ventana.
            agregarBloque(calculo, "No se pudo realizar la operación: " + e.getMessage(), true);
        }
        return null;
    }

    /** Muestra u oculta los pasos dentro del bloque de ese cálculo. */
    private void alternarPasos(Calculo calculo) {
        if (calculo.areaPasos.isVisible()) {
            calculo.areaPasos.setVisible(false);
            calculo.btnPasos.setText(TEXTO_VER_PASOS);
        } else {
            mostrarPasos(calculo);
        }
        calculo.bloque.revalidate();
        calculo.bloque.repaint();
    }

    /** Muestra u oculta la gráfica dentro del bloque de ese cálculo. */
    private void mostrarGrafica(Calculo calculo, boolean mostrar) {
        calculo.grafica.setVisible(mostrar);
        calculo.btnGrafica.setText(mostrar ? TEXTO_OCULTAR_GRAFICA : TEXTO_VER_GRAFICA);
        if (calculo.bloque != null) {
            calculo.bloque.revalidate();
            calculo.bloque.repaint();
            if (mostrar) {
                SwingUtilities.invokeLater(() -> irABloque(calculo.bloque));
            }
        }
    }

    private void mostrarPasos(Calculo calculo) {
        // Los pasos se piden la primera vez que se abren y después solo se muestran.
        if (calculo.areaPasos.getText().isEmpty()) {
            String texto;
            try {
                texto = pasos(calculo);
            } catch (CalculadoraException | RuntimeException e) {
                texto = "No se pudieron generar los pasos: " + e.getMessage();
            }
            calculo.areaPasos.setText(texto.trim());
        }
        calculo.areaPasos.setVisible(true);
        calculo.btnPasos.setText(TEXTO_OCULTAR_PASOS);
        calculo.bloque.revalidate();
        SwingUtilities.invokeLater(() -> irABloque(calculo.bloque));
    }

    /**
     * Menú para elegir de qué resultado ver los pasos (el más reciente arriba).
     * Si hay una función escrita, también ofrece calcularla y ver sus pasos.
     */
    private void abrirMenuPasos() {
        JPopupMenu menu = new JPopupMenu();

        if (!txtFuncion.getText().trim().isEmpty()) {
            JMenuItem actual = itemMenu("▶ calcular  ❯ " + txtFuncion.getText().trim()
                    + "  y ver sus pasos", Estilo.ACENTO);
            actual.addActionListener(e -> {
                Calculo calculo = calcular();
                if (calculo != null) {
                    mostrarPasos(calculo);
                }
            });
            menu.add(actual);
            if (!resueltos.isEmpty()) {
                menu.addSeparator();
            }
        }

        if (resueltos.isEmpty() && menu.getComponentCount() == 0) {
            JMenuItem vacio = itemMenu("Aún no hay resultados: escribe una función y pulsa Enter",
                    Estilo.TEXTO_SUAVE);
            vacio.setEnabled(false);
            menu.add(vacio);
        }

        for (int i = resueltos.size() - 1; i >= 0; i--) {
            Calculo calculo = resueltos.get(i);
            JMenuItem item = itemMenu(calculo.hora + "   " + calculo.operacion.nombre
                    + "   ❯ " + calculo.funcion, Estilo.TEXTO);
            item.addActionListener(e -> mostrarPasos(calculo));
            menu.add(item);
        }

        // El botón está abajo de la ventana, así que el menú se abre hacia arriba.
        menu.show(btnPasos, 0, -menu.getPreferredSize().height - 4);
    }

    private static JMenuItem itemMenu(String texto, Color color) {
        JMenuItem item = new JMenuItem(texto);
        item.setFont(Estilo.fuente(Font.PLAIN, 13));
        item.setForeground(color);
        return item;
    }

    /**
     * Como "clear" en una terminal: borra los bloques y deja todo como al inicio.
     */
    private void limpiar() {
        resueltos.clear();
        ultimoConGraficaAutomatica = null;
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
