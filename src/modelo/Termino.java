package modelo;
 
import java.math.BigDecimal;
import java.math.RoundingMode;
 

public class Termino extends Funcion {
 
    private static final double EPS = 1e-9;
 
    private final double coeficiente;
    private final double exponente;
 
    public Termino(double coeficiente, double exponente) {
        
        this.coeficiente = coeficiente + 0.0;
        this.exponente = exponente + 0.0;
    }
 
    public double getCoeficiente() {
        return coeficiente;
    }
 
    public double getExponente() {
        return exponente;
    }
 
   
    @Override
    public Termino derivar() {
        if (exponente == 0) {
            return new Termino(0, 0);
        }
        return new Termino(coeficiente * exponente, exponente - 1);
    }
 
    
    @Override
    public Termino integrar() {
        if (coeficiente == 0) {
            return new Termino(0, 0);
        }
        if (Math.abs(exponente + 1) < EPS) {
            throw new ArithmeticException(
                    "La integral de x^-1 es ln|x|, que no es un polinomio y no está soportada.");
        }
        return new Termino(coeficiente / (exponente + 1), exponente + 1);
    }
 
    @Override
    public double evaluar(double valor) {
        if (coeficiente == 0) {
            return 0;
        }
        double resultado = coeficiente * Math.pow(valor, exponente);
        if (Double.isNaN(resultado) || Double.isInfinite(resultado)) {
            throw new ArithmeticException(
                    "No se puede evaluar " + aTexto('x') + " en " + formatear(valor)
                            + " (resultado no real o división entre cero).");
        }
        return resultado;
    }
 
    
    @Override
    public String aTexto(char variable) {
        return (coeficiente < 0 ? "-" : "") + aTextoAbsoluto(variable);
    }
 
    public String aTextoAbsoluto(char variable) {
        double abs = Math.abs(coeficiente);
        if (exponente == 0) {
            return formatear(abs);
        }
        String coef = (abs == 1) ? "" : formatear(abs);
        if (exponente == 1) {
            return coef + variable;
        }
        return coef + variable + "^" + formatear(exponente);
    }
 
    @Override
    public String toString() {
        return aTexto('x');
    }
 
    public static String formatear(double n) {
        if (Math.abs(n - Math.rint(n)) < 1e-9) {
            return String.valueOf((long) Math.rint(n));
        }
        return new BigDecimal(n).setScale(4, RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString();
    }
}