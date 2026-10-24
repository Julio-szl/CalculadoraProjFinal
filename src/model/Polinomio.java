package model;
 
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

public class Polinomio extends Funcion {
 
    private final TreeMap<Double, Termino> terminos = new TreeMap<>(Comparator.reverseOrder());
    private char variable;
 
    public Polinomio() {
        this('x');
    }
 
    public Polinomio(char variable) {
        this.variable = variable;
    }
 
    public char getVariable() {
        return variable;
    }
 
    public void setVariable(char variable) {
        this.variable = variable;
    }
 
    public void agregar(Termino t) {
        if (t.getCoeficiente() == 0) {
            return;
        }
        Double exp = t.getExponente();
        Termino existente = terminos.get(exp);
        if (existente == null) {
            terminos.put(exp, t);
            return;
        }
        double nuevoCoef = existente.getCoeficiente() + t.getCoeficiente();
        if (nuevoCoef == 0) {
            terminos.remove(exp);
        } else {
            terminos.put(exp, new Termino(nuevoCoef, exp));
        }
    }
 
    /** Lista de solo lectura, de mayor a menor exponente. */
    public List<Termino> getTerminos() {
        return Collections.unmodifiableList(new ArrayList<>(terminos.values()));
    }
 
    public boolean esCero() {
        return terminos.isEmpty();
    }
 
    @Override
    public Polinomio derivar() {
        Polinomio resultado = new Polinomio(variable);
        for (Termino t : terminos.values()) {
            resultado.agregar(t.derivar());
        }
        return resultado;
    }
 
    @Override
    public Polinomio integrar() {
        Polinomio resultado = new Polinomio(variable);
        for (Termino t : terminos.values()) {
            resultado.agregar(t.integrar());
        }
        return resultado;
    }
 
    /**
     * Con exponentes negativos la función no existe en 0: si 0 está dentro
     * del intervalo, la integral no se puede calcular con la primitiva.
     */
    @Override
    public double integralDefinida(double a, double b) {
        if (tieneExponenteNegativo() && Math.min(a, b) <= 0 && Math.max(a, b) >= 0) {
            throw new ArithmeticException(
                    "La función no está definida en " + variable + " = 0, que está dentro del intervalo ["
                            + Termino.formatear(a) + ", " + Termino.formatear(b)
                            + "]; la integral no existe.");
        }
        return super.integralDefinida(a, b);
    }

    private boolean tieneExponenteNegativo() {
        for (Termino t : terminos.values()) {
            if (t.getExponente() < 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public double evaluar(double valor) {
        double suma = 0;
        for (Termino t : terminos.values()) {
            suma += t.evaluar(valor);
        }
        return suma;
    }
 
    public String integralIndefinidaTexto() {
        Polinomio primitiva = integrar();
        if (primitiva.esCero()) {
            return "C";
        }
        return primitiva + " + C";
    }
 
    @Override
    public String aTexto(char variable) {
        if (terminos.isEmpty()) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        boolean primero = true;
        for (Termino t : terminos.values()) {
            if (primero) {
                sb.append(t.aTexto(variable));
                primero = false;
            } else {
                sb.append(t.getCoeficiente() < 0 ? " - " : " + ");
                sb.append(t.aTextoAbsoluto(variable));
            }
        }
        return sb.toString();
    }
 
    @Override
    public String toString() {
        return aTexto(variable);
    }
}