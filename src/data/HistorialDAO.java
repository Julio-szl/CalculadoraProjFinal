package data;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class HistorialDAO {

    private Connection conexion;   // null = sin base de datos

    public HistorialDAO() {
        conectar();
    }

    private void conectar() {
        try (FileInputStream in = new FileInputStream("config.properties")) {
            Properties config = new Properties();
            config.load(in);
            DriverManager.setLoginTimeout(3);
            conexion = DriverManager.getConnection(
                    config.getProperty("db.url"),
                    config.getProperty("db.usuario"),
                    config.getProperty("db.clave"));
            crearTablaSiNoExiste();
        } catch (Exception e) {
            System.err.println("Historial sin base de datos: " + e.getMessage());
            conexion = null;
        }
    }

    public boolean estaDisponible() {
        return conexion != null;
    }

    private void crearTablaSiNoExiste() throws SQLException {
        try (Statement st = conexion.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS historial ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "operacion VARCHAR(80) NOT NULL, "
                    + "funcion VARCHAR(255) NOT NULL, "
                    + "variable CHAR(1) NOT NULL, "
                    + "resultado VARCHAR(500) NOT NULL, "
                    + "hora VARCHAR(8) NOT NULL, "
                    + "fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        }
    }

    public void guardar(String operacion, String funcion, char variable,
                        String resultado, String hora) {
        if (conexion == null) return;
        String sql = "INSERT INTO historial (operacion, funcion, variable, resultado, hora) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, operacion);
            ps.setString(2, funcion);
            ps.setString(3, String.valueOf(variable));
            ps.setString(4, resultado);
            ps.setString(5, hora);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("No se pudo guardar en el historial: " + e.getMessage());
        }
    }

    /** Devuelve las últimas 50 filas, de la más reciente a la más antigua: {operacion, funcion, variable, resultado, hora}. */
    public List<String[]> cargar() {
        List<String[]> filas = new ArrayList<>();
        if (conexion == null) return filas;
        String sql = "SELECT operacion, funcion, variable, resultado, hora "
                   + "FROM historial ORDER BY id DESC LIMIT 50";
        try (Statement st = conexion.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                filas.add(new String[]{rs.getString(1), rs.getString(2),
                                       rs.getString(3), rs.getString(4), rs.getString(5)});
            }
        } catch (SQLException e) {
            System.err.println("No se pudo leer el historial: " + e.getMessage());
        }
        return filas;
    }

    public void limpiar() {
        if (conexion == null) return;
        try (Statement st = conexion.createStatement()) {
            st.executeUpdate("DELETE FROM historial");
        } catch (SQLException e) {
            System.err.println("No se pudo limpiar el historial: " + e.getMessage());
        }
    }
}