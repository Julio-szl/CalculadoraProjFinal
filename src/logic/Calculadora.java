package logic;

import excepciones.CalculadoraException;
import excepciones.LimiteInvalidoException;
import modelo.Polinomio;

import java.util.Locale;

/**
 * Fachada de la calculadora: es lo ÚNICO que la interfaz (vista) debe llamar.
 * Recibe texto, usa el parser para obtener un Polinomio, aplica la operación
 * del modelo y devuelve un String ya formateado para mostrar en pantalla.
 */
public class Calculadora {

    private final ParserPolinomio parser;

    public Calculadora() {
        this.parser = new ParserPolinomio();
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
        Polinomio integral = polinomio.integrar();   // puede lanzar OperacionNoSoportadaException
        return "F(" + variable + ") = " + textoOCero(integral) + " + C";
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
        double resultado = polinomio.integrarDefinida(limiteInferior, limiteSuperior);
        return "Resultado = " + formatear(resultado);
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

    /** 4 -> "4"   y   0.3333333 -> "0.3333". Usa punto decimal siempre. */
    private String formatear(double valor) {
        if (valor == Math.rint(valor)) {
            return String.valueOf((long) valor);
        }
        return String.format(Locale.US, "%.4f", valor);
    }
}