package logic;

import java.util.List;
import modelo.Polinomio;
import modelo.Termino;

/**
 * Arma el texto de "Pasos del procedimiento" para cada operación.
 * Solo lo usa la Calculadora; la vista nunca lo llama directamente.
 * Recibe el resultado ya calculado para que el último paso coincida
 * exactamente con lo que muestra la calculadora.
 */
class GeneradorPasos {

    String derivar(Polinomio polinomio, String resultado) {
        char v = polinomio.getVariable();
        StringBuilder sb = new StringBuilder();

        sb.append("1. Función:\n");
        sb.append("   f(").append(v).append(") = ").append(polinomio).append("\n\n");

        sb.append("2. Regla de la potencia:\n");
        sb.append("   d/d").append(v).append(" (a·").append(v).append("^n) = n·a·")
                .append(v).append("^(n-1)\n");
        sb.append("   La derivada de una constante es 0.\n\n");

        sb.append("3. Se deriva cada término:\n");
        if (polinomio.esCero()) {
            sb.append("   La función es 0, su derivada es 0.\n");
        }
        for (Termino t : polinomio.getTerminos()) {
            sb.append("   ").append(derivarTermino(t, v)).append("\n");
        }
        sb.append("\n");

        sb.append("4. Se suman los resultados:\n");
        sb.append("   ").append(resultado).append("\n");
        return sb.toString();
    }

    String integralIndefinida(Polinomio polinomio, String resultado) {
        char v = polinomio.getVariable();
        StringBuilder sb = new StringBuilder();

        sb.append("1. Función:\n");
        sb.append("   f(").append(v).append(") = ").append(polinomio).append("\n\n");

        sb.append("2. Regla de la potencia para integrales (n ≠ -1):\n");
        sb.append("   ∫ a·").append(v).append("^n d").append(v).append(" = a·")
                .append(v).append("^(n+1) / (n+1)\n\n");

        sb.append("3. Se integra cada término:\n");
        agregarIntegralesPorTermino(sb, polinomio, v);
        sb.append("\n");

        sb.append("4. Se suman los resultados y se agrega la constante de integración C:\n");
        sb.append("   ").append(resultado).append("\n");
        return sb.toString();
    }

    String integralDefinida(Polinomio polinomio, double a, double b, String resultado) {
        char v = polinomio.getVariable();
        Polinomio primitiva = polinomio.integrar();
        String textoA = Termino.formatear(a);
        String textoB = Termino.formatear(b);
        double valorB = primitiva.evaluar(b);
        double valorA = primitiva.evaluar(a);
        StringBuilder sb = new StringBuilder();

        sb.append("1. Función y límites:\n");
        sb.append("   f(").append(v).append(") = ").append(polinomio).append("\n");
        sb.append("   a = ").append(textoA).append(",  b = ").append(textoB).append("\n\n");

        sb.append("2. Se calcula la primitiva F(").append(v).append(") término a término:\n");
        agregarIntegralesPorTermino(sb, polinomio, v);
        sb.append("   F(").append(v).append(") = ").append(primitiva).append("\n\n");

        sb.append("3. Teorema fundamental del cálculo:\n");
        sb.append("   ∫ de a a b de f(").append(v).append(") d").append(v).append(" = F(b) - F(a)\n\n");

        sb.append("4. Se evalúa F en los límites:\n");
        sb.append("   ").append(evaluarConDetalle(primitiva, b)).append("\n");
        sb.append("   ").append(evaluarConDetalle(primitiva, a)).append("\n\n");

        sb.append("5. Se restan:\n");
        sb.append("   F(").append(textoB).append(") - F(").append(textoA).append(") = ")
                .append(Termino.formatear(valorB)).append(" - ").append(entreParentesis(valorA))
                .append("\n");
        if (a > b) {
            sb.append("   Como a > b, el resultado cambia de signo; no es un error.\n");
        }
        sb.append("\n");

        sb.append("6. Resultado:\n");
        sb.append("   ").append(resultado).append("\n");
        return sb.toString();
    }

    // ---------------- Auxiliares ----------------

    /** Ejemplo: d/dx (3x^2) = 2·3x^(2-1) = 6x */
    private String derivarTermino(Termino t, char v) {
        String inicio = "d/d" + v + " (" + t.aTexto(v) + ") = ";
        double n = t.getExponente();
        if (n == 0) {
            return inicio + "0   (constante)";
        }
        return inicio + entreParentesis(n) + "·" + entreParentesis(t.getCoeficiente()) + v
                + "^(" + Termino.formatear(n) + "-1) = " + t.derivar().aTexto(v);
    }

    /** Ejemplo: ∫ 3x^2 dx = 3x^(2+1) / (2+1) = x^3 */
    private void agregarIntegralesPorTermino(StringBuilder sb, Polinomio polinomio, char v) {
        if (polinomio.esCero()) {
            sb.append("   La función es 0, su integral es solo la constante.\n");
        }
        for (Termino t : polinomio.getTerminos()) {
            String n = Termino.formatear(t.getExponente());
            sb.append("   ∫ ").append(t.aTexto(v)).append(" d").append(v).append(" = ")
                    .append(Termino.formatear(t.getCoeficiente())).append(v)
                    .append("^(").append(n).append("+1) / (").append(n).append("+1) = ")
                    .append(t.integrar().aTexto(v)).append("\n");
        }
    }

    /** Ejemplo: F(3) = 9 + 9 = 18   (el valor de cada término y luego la suma). */
    private String evaluarConDetalle(Polinomio primitiva, double valor) {
        String inicio = "F(" + Termino.formatear(valor) + ") = ";
        List<Termino> terminos = primitiva.getTerminos();
        if (terminos.size() <= 1) {
            return inicio + Termino.formatear(primitiva.evaluar(valor));
        }
        StringBuilder sb = new StringBuilder(inicio);
        for (int i = 0; i < terminos.size(); i++) {
            if (i > 0) {
                sb.append(" + ");
            }
            sb.append(entreParentesis(terminos.get(i).evaluar(valor)));
        }
        return sb.append(" = ").append(Termino.formatear(primitiva.evaluar(valor))).toString();
    }

    /** Los negativos van entre paréntesis para que la cuenta se lea bien: 2·(-3). */
    private String entreParentesis(double numero) {
        String texto = Termino.formatear(numero);
        return numero < 0 ? "(" + texto + ")" : texto;
    }
}
