package exceptions;

public class OperacionNoSoportadaException extends CalculadoraException{

    public OperacionNoSoportadaException(String message) {
        super(message);
    }
}