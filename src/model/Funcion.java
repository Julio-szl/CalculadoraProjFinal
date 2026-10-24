package model;
 

public abstract class Funcion implements Evaluable, Derivable, Integrable {
 
    @Override
    public abstract Funcion derivar();
 
    @Override
    public abstract Funcion integrar();
 
    @Override
    public abstract double evaluar(double valor);
 
    
    public abstract String aTexto(char variable);
 
    
    @Override
    public double integralDefinida(double a, double b) {
        Funcion primitiva = integrar();
        return primitiva.evaluar(b) - primitiva.evaluar(a);
    }
 
    
    public double derivadaEn(double valor) {
        return derivar().evaluar(valor);
    }
}