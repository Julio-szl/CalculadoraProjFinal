package logic;

import exceptions.ExpresionInvalidaException;
import model.Polinomio;
import model.Termino;

public class ParserPolinomio {

    public Polinomio parsear(String texto, char variable) throws ExpresionInvalidaException {
        if (texto == null) {
            throw new ExpresionInvalidaException("La función está vacía.");
        }
        String limpio = texto.replaceAll("\\s+", "");

        validar(limpio, variable);

        String[] partes = limpio.split("(?<!\\^)(?=[+-])");

        Polinomio polinomio = new Polinomio(variable);
        for (String parte : partes) {
            polinomio.agregarTermino(parsearTermino(parte, variable));
        }
        return polinomio;
    }

    private void validar(String texto, char variable) throws ExpresionInvalidaException {
        if (variable != 'x' && variable != 'y') {
            throw new ExpresionInvalidaException("La variable debe ser x o y.");
        }
        if (texto.isEmpty()) {
            throw new ExpresionInvalidaException("Escribe una función, por ejemplo 3x^2 + 5x.");
        }

        for (char c : texto.toCharArray()) {
            if (Character.isLetter(c) && c != variable) {
                throw new ExpresionInvalidaException(
                        "Carácter no permitido: '" + c + "'. Usa solo la variable " + variable + ".");
            }
        }

        // coeficiente: 3 o 3.5      parte variable: x, x^2 o x^-1
        String coeficiente = "\\d+(\\.\\d+)?";
        String parteVariable = variable + "(\\^-?\\d+)?";
        String formato = "[+-]?(" + coeficiente + "(" + parteVariable + ")?|" + parteVariable + ")";

        String[] partes = texto.split("(?<!\\^)(?=[+-])");
        for (String parte : partes) {
            if (!parte.matches(formato)) {
                throw new ExpresionInvalidaException("Término inválido: \"" + parte + "\".");
            }
        }
    }

    private Termino parsearTermino(String texto, char variable) throws ExpresionInvalidaException {
        try {
            int posVariable = texto.indexOf(variable);
            double coeficiente;
            int exponente;

            if (posVariable == -1) {
                coeficiente = Double.parseDouble(texto);
                exponente = 0;
            } else {
                String parteCoeficiente = texto.substring(0, posVariable);
                String parteExponente = texto.substring(posVariable + 1);

                if (parteCoeficiente.isEmpty() || parteCoeficiente.equals("+")) {
                    coeficiente = 1;
                } else if (parteCoeficiente.equals("-")) {
                    coeficiente = -1;
                } else {
                    coeficiente = Double.parseDouble(parteCoeficiente);
                }

                if (parteExponente.isEmpty()) {
                    exponente = 1;
                } else {
                    exponente = Integer.parseInt(parteExponente.substring(1)); // quita el '^'
                }
            }
            return new Termino(coeficiente, exponente);
        } catch (NumberFormatException e) {
            throw new ExpresionInvalidaException("No se pudo interpretar el término \"" + texto + "\".");
        }
    }
}