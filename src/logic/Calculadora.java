package logic;

import exceptions.CalculadoraException;
import exceptions.LimiteInvalidoException;
import exceptions.OperacionNoSoportadaException;
import java.util.Locale;
import modelo.Polinomio;

/**
 * Fachada de la calculadora: es lo ÚNICO que la interfaz (vista) debe llamar.
 * Recibe texto, usa el parser para obtener un Polinomio, aplica la operación
 * del modelo y devuelve un String ya formateado para mostrar en pantalla.
 */
public class Calculadora {

    private final ParserPolinomio parser;
    private final GeneradorPasos pasos;

    public Calculadora() {
        this.parser = new ParserPolinomio();
        this.pasos = new GeneradorPasos();
    }

    /** Ejemplo de salida: "f'(x) = 6x + 5" */
    public String derivar(String funcion, char variable) throws CalculadoraException {
        Polinomio polinomio = parser.parsear(funcion, variable);
        Polinomio derivada = polinomio.derivar();
        return "f'(" + variable + ") = " + textoOCero(derivada);
    }

    /** Ejemplo de salida: "F(x) = x^2 + C" */
    public String integrarIndefinida(String funcion, char variable) throws CalculadoraException {
        Polinomio polinomio = parser.parsear(funcion, variable);
        try {
            Polinomio integral = polinomio.integrar();
            return "F(" + variable + ") = " + textoOCero(integral) + " + C";
        } catch (ArithmeticException e) {
            // El modelo lanza ArithmeticException al integrar x^-1.
            throw new OperacionNoSoportadaException(e.getMessage());
        }
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
        try {
            double resultado = polinomio.integralDefinida(limiteInferior, limiteSuperior);
            return "Resultado = " + formatear(resultado);
        } catch (ArithmeticException e) {
            // x^-1, o una función que no existe en algún punto del intervalo.
            throw new OperacionNoSoportadaException(e.getMessage());
        }
    }

    // ---------------- Pasos del procedimiento ----------------
    // Primero se calcula el resultado normal: si hay un error, se lanza la misma
    // excepción que en la operación sin pasos y no se arma ningún texto.

    /** Texto de varias líneas que explica cómo se obtuvo la derivada. */
    public String pasosDerivar(String funcion, char variable) throws CalculadoraException {
        String resultado = derivar(funcion, variable);
        return pasos.derivar(parser.parsear(funcion, variable), resultado);
    }

    /** Texto de varias líneas que explica cómo se obtuvo la integral indefinida. */
    public String pasosIntegrarIndefinida(String funcion, char variable) throws CalculadoraException {
        String resultado = integrarIndefinida(funcion, variable);
        return pasos.integrarIndefinida(parser.parsear(funcion, variable), resultado);
    }

    /** Texto de varias líneas que explica cómo se obtuvo la integral definida. */
    public String pasosIntegrarDefinida(String funcion, char variable, String a, String b)
            throws CalculadoraException {
        String resultado = integrarDefinida(funcion, variable, a, b);
        return pasos.integrarDefinida(parser.parsear(funcion, variable),
                parsearLimite(a, "inferior"), parsearLimite(b, "superior"), resultado);
    }

    // ---------------- Auxiliares ----------------

    /** Convierte el texto de un límite en número o lanza LimiteInvalidoException. */
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

    /** 4 -> "4",   0.5 -> "0.5"   y   0.3333333 -> "0.3333". Usa punto decimal siempre. */
    private String formatear(double valor) {
        String texto = String.format(Locale.US, "%.4f", valor);
        // Quita los ceros sobrantes y, si queda al final, también el punto.
        texto = texto.replaceAll("0+$", "").replaceAll("\\.$", "");
        return texto.equals("-0") ? "0" : texto;
    }
}