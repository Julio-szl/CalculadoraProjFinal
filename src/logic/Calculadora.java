package logic;

import exceptions.CalculadoraException;
import exceptions.LimiteInvalidoException;
import java.util.Locale;
import model.Polinomio;
import model.Termino;

public class Calculadora {

    private final ParserPolinomio parser;
    private final Historial historial;

    public Calculadora() {
        this.parser = new ParserPolinomio();
        this.historial = new Historial();
    }

    public Historial getHistorial() {
        return historial;
    }

    // ================= Operaciones principales =================

    /** Ejemplo de salida: "f'(x) = 6x + 5" */
    public String derivar(String funcion, char variable) throws CalculadoraException {
        Polinomio polinomio = parser.parsear(funcion, variable);
        Polinomio derivada = polinomio.derivar();

        String resultado = "f'(" + variable + ") = " + textoOCero(derivada);
        historial.agregar("Derivada", funcion, variable, resultado);
        return resultado;
    }

    /** Ejemplo de salida: "F(x) = x^2 + C" */
    public String integrarIndefinida(String funcion, char variable) throws CalculadoraException {
        Polinomio polinomio = parser.parsear(funcion, variable);
        Polinomio integral = polinomio.integrar(); // puede lanzar OperacionNoSoportadaException

        String resultado = "F(" + variable + ") = " + textoOCero(integral) + " + C";
        historial.agregar("Integral indefinida", funcion, variable, resultado);
        return resultado;
    }

    /**
     * Los límites llegan como texto desde la interfaz.
     * Ejemplo de salida: "Resultado = 4"
     * Nota: si a > b el resultado sale negativo; NO es un error.
     */
    public String integrarDefinida(String funcion, char variable, String a, String b)
            throws CalculadoraException {
        double limiteInferior = parsearLimite(a, "inferior");
        double limiteSuperior = parsearLimite(b, "superior");

        Polinomio polinomio = parser.parsear(funcion, variable);
        double valor = polinomio.integralDefinida(limiteInferior, limiteSuperior);

        String resultado = "Resultado = " + formatear(valor);
        historial.agregar("Integral definida [" + a.trim() + ", " + b.trim() + "]",
                funcion, variable, resultado);
        return resultado;
    }

    // ================= Pasos del procedimiento =================

    /** Pasos de la derivada, término por término. */
    public String pasosDerivar(String funcion, char variable) throws CalculadoraException {
        Polinomio p = parser.parsear(funcion, variable);
        StringBuilder sb = new StringBuilder();
        sb.append("Función: f(").append(variable).append(") = ").append(textoOCero(p)).append("\n");
        sb.append("Regla de la potencia: d/d").append(variable).append(" (a·")
                .append(variable).append("^n) = a·n·").append(variable).append("^(n-1)\n\n");

        for (Termino t : p.getTerminos()) {
            sb.append("d/d").append(variable).append(" (").append(t).append(") = ");
            if (t.getExponente() == 0) {
                sb.append("0   (la derivada de una constante es 0)\n");
            } else {
                sb.append(formatear(t.getCoeficiente())).append("·").append(t.getExponente())
                        .append(" ").append(variable).append("^").append(t.getExponente() - 1)
                        .append(" = ").append(t.derivar()).append("\n");
            }
        }
        sb.append("\nResultado: f'(").append(variable).append(") = ")
                .append(textoOCero(p.derivar()));
        return sb.toString();
    }

    /** Pasos de la integral indefinida. */
    public String pasosIntegrar(String funcion, char variable) throws CalculadoraException {
        Polinomio p = parser.parsear(funcion, variable);
        StringBuilder sb = new StringBuilder();
        sb.append("Función: f(").append(variable).append(") = ").append(textoOCero(p)).append("\n");
        sb.append("Regla de la potencia: ∫ a·").append(variable).append("^n d").append(variable)
                .append(" = a/(n+1)·").append(variable).append("^(n+1)\n\n");

        for (Termino t : p.getTerminos()) {
            sb.append("∫ (").append(t).append(") d").append(variable).append(" = ")
                    .append(formatear(t.getCoeficiente())).append("/").append(t.getExponente() + 1)
                    .append(" ").append(variable).append("^").append(t.getExponente() + 1)
                    .append(" = ").append(t.integrar()).append("\n"); // lanza si el exponente es -1
        }
        sb.append("\nResultado: F(").append(variable).append(") = ")
                .append(textoOCero(p.integrar())).append(" + C");
        return sb.toString();
    }

    /**
     * Pasos de la integral definida (regla de Barrow).
     */
    public String pasosIntegrarDefinida(String funcion, char variable, String a, String b)
            throws CalculadoraException {
        double inferior = parsearLimite(a, "inferior");
        double superior = parsearLimite(b, "superior");

        Polinomio F = parser.parsear(funcion, variable).integrar();
        double fSuperior = F.evaluar(superior);
        double fInferior = F.evaluar(inferior);

        return pasosIntegrar(funcion, variable)
                + "\n\nRegla de Barrow: F(b) - F(a)"
                + "\nF(" + formatear(superior) + ") = " + formatear(fSuperior)
                + "\nF(" + formatear(inferior) + ") = " + formatear(fInferior)
                + "\nResultado = " + formatear(fSuperior - fInferior);
    }

    // ================= Datos para la gráfica =================
    public double[] muestrear(String funcion, char variable, double desde, double hasta, int puntos)
            throws CalculadoraException {
        return calcularPuntos(parser.parsear(funcion, variable), desde, hasta, puntos);
    }

    /** Igual que muestrear, pero con la derivada de la función. */
    public double[] muestrearDerivada(String funcion, char variable, double desde, double hasta, int puntos)
            throws CalculadoraException {
        return calcularPuntos(parser.parsear(funcion, variable).derivar(), desde, hasta, puntos);
    }

    private double[] calcularPuntos(Polinomio p, double desde, double hasta, int puntos)
            throws CalculadoraException {
        if (desde >= hasta) {
            throw new LimiteInvalidoException("El rango de la gráfica no es válido.");
        }
        int n = Math.max(puntos, 2);
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            double x = desde + (hasta - desde) * i / (n - 1);
            double valor = p.evaluar(x);
            y[i] = (Double.isNaN(valor) || Double.isInfinite(valor)) ? Double.NaN : valor;
        }
        return y;
    }

    // ================= Auxiliares =================

    /**
     * Convierte el texto de un límite en número o lanza LimiteInvalidoException.
     */
    private double parsearLimite(String texto, String nombre) throws LimiteInvalidoException {
        if (texto == null || texto.trim().isEmpty()) {
            throw new LimiteInvalidoException("El límite " + nombre + " está vacío.");
        }
        try {
            // Se acepta la coma decimal escribiendo "0,5" además de "0.5".
            double valor = Double.parseDouble(texto.trim().replace(',', '.'));
            if (Double.isNaN(valor) || Double.isInfinite(valor)) {
                throw new LimiteInvalidoException("El límite " + nombre + " no es un número válido.");
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new LimiteInvalidoException("El límite " + nombre + " no es un número válido.");
        }
    }

    /** Si el polinomio quedó sin términos, mostrar "0" en vez de texto vacío. */
    private String textoOCero(Polinomio polinomio) {
        String texto = polinomio.toString();
        return (texto == null || texto.trim().isEmpty()) ? "0" : texto;
    }

    /** 4 -> "4" y 0.3333333 -> "0.3333". Usa punto decimal siempre. */
    private String formatear(double valor) {
        if (valor == Math.rint(valor)) {
            return String.valueOf((long) valor);
        }
        return String.format(Locale.US, "%.4f", valor);
    }
}