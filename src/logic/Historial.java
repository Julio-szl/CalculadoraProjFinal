package logic;

import data.HistorialDAO;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Historial {

    public static class Entrada {
        private final String operacion;
        private final String funcion;
        private final char variable;
        private final String resultado;
        private final String hora;

        public Entrada(String operacion, String funcion, char variable, String resultado) {
            this(operacion, funcion, variable, resultado,
                 LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }

        public Entrada(String operacion, String funcion, char variable, String resultado, String hora) {
            this.operacion = operacion;
            this.funcion = funcion;
            this.variable = variable;
            this.resultado = resultado;
            this.hora = hora;
        }

        public String getOperacion() { return operacion; }
        public String getFuncion()   { return funcion; }
        public char getVariable()    { return variable; }
        public String getResultado() { return resultado; }
        public String getHora()      { return hora; }

        @Override
        public String toString() {
            return "[" + hora + "] " + operacion + ": " + funcion + "  →  " + resultado;
        }
    }

    private final List<Entrada> entradas = new ArrayList<>();
    private final HistorialDAO dao = new HistorialDAO();

    public Historial() {
        // Si hay base de datos, recupera las operaciones guardadas (más reciente primero).
        for (String[] f : dao.cargar()) {
            entradas.add(new Entrada(f[0], f[1], f[2].charAt(0), f[3], f[4]));
        }
    }

    public void agregar(String operacion, String funcion, char variable, String resultado) {
        Entrada e = new Entrada(operacion, funcion, variable, resultado);
        entradas.add(0, e);
        dao.guardar(e.operacion, e.funcion, e.variable, e.resultado, e.hora);
    }

    public List<Entrada> obtenerTodas() {
        return Collections.unmodifiableList(entradas);
    }

    public void limpiar() {
        entradas.clear();
        dao.limpiar();
    }

    //---------------- Base de datos ----------------
    public boolean usaBaseDeDatos() {
        return dao.estaDisponible();
    }
}