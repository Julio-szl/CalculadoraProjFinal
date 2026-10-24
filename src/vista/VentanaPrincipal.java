package vista;

import exceptions.CalculadoraException;
import logic.Calculadora;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Ventana principal de la calculadora.
 * Solo se comunica con la fachada Calculadora: le pasa el texto que escribió
 * el usuario y muestra el String que devuelve (o el mensaje de error).
 */
public class VentanaPrincipal extends JFrame {

    private static final Color COLOR_RESULTADO = new Color(0, 90, 160);
    private static final Color COLOR_ERROR = new Color(180, 30, 30);

    private final Calculadora calculadora;

    private JTextField txtFuncion;
    private JComboBox<String> cmbVariable;
    private JRadioButton rbDerivar;
    private JRadioButton rbIndefinida;
    private JRadioButton rbDefinida;
    private JTextField txtLimiteA;
    private JTextField txtLimiteB;
    private JLabel lblResultado;

    private VentanaManual ventanaManual;

    public VentanaPrincipal() {
        this.calculadora = new Calculadora();
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setTitle("Calculadora de Derivadas e Integrales");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel contenido = new JPanel(new BorderLayout(10, 10));
        contenido.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        contenido.add(crearTitulo(), BorderLayout.NORTH);
        contenido.add(crearFormulario(), BorderLayout.CENTER);
        contenido.add(crearPanelInferior(), BorderLayout.SOUTH);
        setContentPane(contenido);

        actualizarLimites();
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    // ---------------- Construcción de la interfaz ----------------

    private JLabel crearTitulo() {
        JLabel titulo = new JLabel("Calculadora de Derivadas e Integrales", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        return titulo;
    }

    private JPanel crearFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;

        // Fila 0: función
        c.gridx = 0;
        c.gridy = 0;
        panel.add(new JLabel("Función:"), c);

        txtFuncion = new JTextField(25);
        txtFuncion.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        txtFuncion.setToolTipText("Ejemplo: 3x^2 + 5x - 2");
        c.gridx = 1;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        panel.add(txtFuncion, c);

        // Fila 1: variable
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        panel.add(new JLabel("Variable:"), c);

        cmbVariable = new JComboBox<>(new String[]{"x", "y"});
        // Con una sola letra, el estilo de Windows calcula el combo demasiado angosto.
        cmbVariable.setPrototypeDisplayValue("xxxx");
        c.gridx = 1;
        panel.add(cmbVariable, c);

        // Fila 2: operación
        c.gridx = 0;
        c.gridy = 2;
        panel.add(new JLabel("Operación:"), c);

        rbDerivar = new JRadioButton("Derivar", true);
        rbIndefinida = new JRadioButton("Integral indefinida");
        rbDefinida = new JRadioButton("Integral definida");
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbDerivar);
        grupo.add(rbIndefinida);
        grupo.add(rbDefinida);
        rbDerivar.addActionListener(e -> actualizarLimites());
        rbIndefinida.addActionListener(e -> actualizarLimites());
        rbDefinida.addActionListener(e -> actualizarLimites());

        JPanel panelOperaciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelOperaciones.add(rbDerivar);
        panelOperaciones.add(rbIndefinida);
        panelOperaciones.add(rbDefinida);
        c.gridx = 1;
        c.gridwidth = 3;
        panel.add(panelOperaciones, c);

        // Fila 3: límites (solo para la integral definida)
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        panel.add(new JLabel("Límites:"), c);

        txtLimiteA = new JTextField(6);
        txtLimiteA.setToolTipText("Límite inferior (a)");
        txtLimiteB = new JTextField(6);
        txtLimiteB.setToolTipText("Límite superior (b)");

        JPanel panelLimites = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelLimites.add(new JLabel("a ="));
        panelLimites.add(txtLimiteA);
        panelLimites.add(new JLabel("b ="));
        panelLimites.add(txtLimiteB);
        c.gridx = 1;
        c.gridwidth = 3;
        panel.add(panelLimites, c);

        return panel;
    }

    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JButton btnCalcular = new JButton("Calcular");
        JButton btnPasos = new JButton("Ver pasos");
        JButton btnLimpiar = new JButton("Limpiar");
        JButton btnManual = new JButton("Manual de usuario");
        btnCalcular.addActionListener(e -> calcular());
        btnPasos.addActionListener(e -> verPasos());
        btnLimpiar.addActionListener(e -> limpiar());
        btnManual.addActionListener(e -> abrirManual());
        // Presionar Enter en cualquier campo equivale a pulsar "Calcular".
        getRootPane().setDefaultButton(btnCalcular);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panelBotones.add(btnCalcular);
        panelBotones.add(btnPasos);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnManual);
        panel.add(panelBotones, BorderLayout.NORTH);

        lblResultado = new JLabel(" ", SwingConstants.CENTER);
        lblResultado.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
        lblResultado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Resultado"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        panel.add(lblResultado, BorderLayout.CENTER);

        return panel;
    }

    // ---------------- Acciones ----------------

    /** Los campos de los límites solo se habilitan para la integral definida. */
    private void actualizarLimites() {
        boolean definida = rbDefinida.isSelected();
        txtLimiteA.setEnabled(definida);
        txtLimiteB.setEnabled(definida);
    }

    private void calcular() {
        String funcion = txtFuncion.getText();
        char variable = ((String) cmbVariable.getSelectedItem()).charAt(0);

        try {
            String resultado;
            if (rbDerivar.isSelected()) {
                resultado = calculadora.derivar(funcion, variable);
            } else if (rbIndefinida.isSelected()) {
                resultado = calculadora.integrarIndefinida(funcion, variable);
            } else {
                resultado = calculadora.integrarDefinida(
                        funcion, variable, txtLimiteA.getText(), txtLimiteB.getText());
            }
            mostrarResultado(resultado);
        } catch (CalculadoraException e) {
            mostrarError(e.getMessage());
        } catch (RuntimeException e) {
            // Red de seguridad: ningún error inesperado debe cerrar la ventana.
            mostrarError("No se pudo realizar la operación: " + e.getMessage());
        }
    }

    /** Muestra en una ventana aparte cómo se llegó al resultado. */
    private void verPasos() {
        String funcion = txtFuncion.getText();
        char variable = ((String) cmbVariable.getSelectedItem()).charAt(0);

        try {
            String pasos;
            if (rbDerivar.isSelected()) {
                pasos = calculadora.pasosDerivar(funcion, variable);
            } else if (rbIndefinida.isSelected()) {
                pasos = calculadora.pasosIntegrarIndefinida(funcion, variable);
            } else {
                pasos = calculadora.pasosIntegrarDefinida(
                        funcion, variable, txtLimiteA.getText(), txtLimiteB.getText());
            }
            mostrarPasos(pasos);
        } catch (CalculadoraException e) {
            mostrarError(e.getMessage());
        } catch (RuntimeException e) {
            mostrarError("No se pudo realizar la operación: " + e.getMessage());
        }
    }

    private void mostrarPasos(String pasos) {
        JTextArea area = new JTextArea(pasos);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        area.setMargin(new Insets(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(area);
        Dimension tamano = area.getPreferredSize();
        // Crece con el texto, pero sin pasar de un tamaño razonable de pantalla.
        scroll.setPreferredSize(new Dimension(
                Math.min(tamano.width + 30, 750), Math.min(tamano.height + 30, 500)));

        JOptionPane.showMessageDialog(this, scroll, "Pasos del procedimiento",
                JOptionPane.PLAIN_MESSAGE);
    }

    private void limpiar() {
        txtFuncion.setText("");
        txtLimiteA.setText("");
        txtLimiteB.setText("");
        rbDerivar.setSelected(true);
        cmbVariable.setSelectedIndex(0);
        actualizarLimites();
        lblResultado.setText(" ");
        txtFuncion.requestFocusInWindow();
    }

    private void abrirManual() {
        if (ventanaManual == null) {
            ventanaManual = new VentanaManual(this);
        }
        ventanaManual.setVisible(true);
        ventanaManual.toFront();
    }

    private void mostrarResultado(String texto) {
        lblResultado.setForeground(COLOR_RESULTADO);
        lblResultado.setText(texto);
    }

    private void mostrarError(String mensaje) {
        lblResultado.setForeground(COLOR_ERROR);
        lblResultado.setText("Error");
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
