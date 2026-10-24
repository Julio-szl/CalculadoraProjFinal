package vista;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Caja con la gráfica de una o varias funciones (f, f' o F) ya evaluadas.
 * No calcula nada: recibe los puntos x y los valores y de cada curva.
 * Al pasar el mouse muestra los valores de cada curva en ese punto.
 */
class PanelGrafica extends JComponent {

    private static final int MARGEN_IZQ = 56;
    private static final int MARGEN_DER = 16;
    private static final int MARGEN_ARRIBA = 14;
    private static final int MARGEN_ABAJO = 26;

    private static final Color FONDO = new Color(0x11141B);
    private static final Color CUADRICULA = new Color(0x1E222C);

    /** Una curva de la gráfica. */
    private static class Serie {
        final String nombre;
        final Color color;
        final double[] valores;

        Serie(String nombre, Color color, double[] valores) {
            this.nombre = nombre;
            this.color = color;
            this.valores = valores;
        }
    }

    private final double[] puntosX;
    private final List<Serie> series = new ArrayList<>();
    private Double areaDesde;
    private Double areaHasta;

    private double xMin;
    private double xMax;
    private double yMin;
    private double yMax;
    private int mouseX = -1;

    PanelGrafica(double[] puntosX) {
        this.puntosX = puntosX;
        this.xMin = puntosX[0];
        this.xMax = puntosX[puntosX.length - 1];
        setPreferredSize(new Dimension(400, 250));
        setFont(Estilo.fuente(Font.PLAIN, 11));

        MouseAdapter raton = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseX = -1;
                repaint();
            }
        };
        addMouseListener(raton);
        addMouseMotionListener(raton);
    }

    /** Puntos x repartidos de forma pareja entre desde y hasta. */
    static double[] puntos(double desde, double hasta, int cantidad) {
        double[] xs = new double[cantidad];
        for (int i = 0; i < cantidad; i++) {
            xs[i] = desde + (hasta - desde) * i / (cantidad - 1);
        }
        return xs;
    }

    void agregarSerie(String nombre, Color color, double[] valores) {
        series.add(new Serie(nombre, color, valores));
        calcularRangoY();
    }

    /** Sombrea el área bajo la primera curva entre a y b (integral definida). */
    void sombrearArea(double a, double b) {
        areaDesde = Math.min(a, b);
        areaHasta = Math.max(a, b);
        calcularRangoY();
    }

    // ---------------- Escala ----------------

    /**
     * El rango vertical ignora el 2 % de valores más extremos de cada lado, para que
     * una curva que se dispara (por ejemplo x^-2 cerca de 0) no aplaste al resto.
     * Si hay un área sombreada, solo cuentan los valores entre a y b, que es lo importante.
     */
    private void calcularRangoY() {
        List<Double> finitos = new ArrayList<>();
        for (Serie serie : series) {
            for (int i = 0; i < puntosX.length; i++) {
                double y = serie.valores[i];
                boolean dentroDelArea = areaDesde == null
                        || (puntosX[i] >= areaDesde && puntosX[i] <= areaHasta);
                if (Double.isFinite(y) && dentroDelArea) {
                    finitos.add(y);
                }
            }
        }
        if (finitos.isEmpty()) {
            yMin = -1;
            yMax = 1;
            return;
        }
        double[] ordenados = finitos.stream().mapToDouble(Double::doubleValue).toArray();
        Arrays.sort(ordenados);
        int recorte = ordenados.length / 50;
        double bajo = ordenados[recorte];
        double alto = ordenados[ordenados.length - 1 - recorte];
        if (alto - bajo < 1e-9) {
            bajo -= 1;
            alto += 1;
        }
        // Se incluye el eje x si está razonablemente cerca, para dar referencia.
        double rango = alto - bajo;
        if (areaDesde != null) {
            // El área se mide desde el eje x, así que el 0 siempre debe verse.
            bajo = Math.min(bajo, 0);
            alto = Math.max(alto, 0);
        } else if (bajo > 0 && bajo < rango) {
            bajo = 0;
        } else if (alto < 0 && -alto < rango) {
            alto = 0;
        }
        double margen = (alto - bajo) * 0.1;
        yMin = bajo - margen;
        yMax = alto + margen;
    }

    private int ancho() {
        return getWidth() - MARGEN_IZQ - MARGEN_DER;
    }

    private int alto() {
        return getHeight() - MARGEN_ARRIBA - MARGEN_ABAJO;
    }

    private double aPantallaX(double x) {
        return MARGEN_IZQ + (x - xMin) / (xMax - xMin) * ancho();
    }

    private double aPantallaY(double y) {
        return MARGEN_ARRIBA + (yMax - y) / (yMax - yMin) * alto();
    }

    /** Paso "redondo" (1, 2 o 5 por una potencia de 10) para las marcas de los ejes. */
    private static double pasoRedondo(double rango, int marcas) {
        double crudo = rango / marcas;
        double potencia = Math.pow(10, Math.floor(Math.log10(crudo)));
        double fraccion = crudo / potencia;
        double redondo = fraccion < 1.5 ? 1 : fraccion < 3.5 ? 2 : fraccion < 7.5 ? 5 : 10;
        return redondo * potencia;
    }

    private static String formatear(double valor) {
        if (Math.abs(valor) < 1e-9) {
            return "0";
        }
        if (Math.abs(valor - Math.rint(valor)) < 1e-9 && Math.abs(valor) < 1e7) {
            return String.valueOf((long) Math.rint(valor));
        }
        String texto = String.format(java.util.Locale.US, "%.3f", valor);
        return texto.replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    // ---------------- Dibujo ----------------

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(FONDO);
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));

        dibujarCuadricula(g2);

        Shape recorteOriginal = g2.getClip();
        g2.clipRect(MARGEN_IZQ, MARGEN_ARRIBA, ancho() + 1, alto() + 1);
        dibujarArea(g2);
        for (Serie serie : series) {
            dibujarCurva(g2, serie);
        }
        g2.setClip(recorteOriginal);

        dibujarLeyenda(g2);
        dibujarValoresBajoMouse(g2);
        g2.dispose();
    }

    private void dibujarCuadricula(Graphics2D g2) {
        FontMetrics fm = g2.getFontMetrics();

        double pasoX = pasoRedondo(xMax - xMin, 8);
        for (double x = Math.ceil(xMin / pasoX) * pasoX; x <= xMax + 1e-9; x += pasoX) {
            int px = (int) Math.round(aPantallaX(x));
            boolean eje = Math.abs(x) < pasoX / 1000;
            g2.setColor(eje ? Estilo.TEXTO_SUAVE : CUADRICULA);
            g2.drawLine(px, MARGEN_ARRIBA, px, MARGEN_ARRIBA + alto());
            String etiqueta = formatear(x);
            g2.setColor(Estilo.TEXTO_SUAVE);
            g2.drawString(etiqueta, px - fm.stringWidth(etiqueta) / 2, getHeight() - 8);
        }

        double pasoY = pasoRedondo(yMax - yMin, 5);
        for (double y = Math.ceil(yMin / pasoY) * pasoY; y <= yMax + 1e-9; y += pasoY) {
            int py = (int) Math.round(aPantallaY(y));
            boolean eje = Math.abs(y) < pasoY / 1000;
            g2.setColor(eje ? Estilo.TEXTO_SUAVE : CUADRICULA);
            g2.drawLine(MARGEN_IZQ, py, MARGEN_IZQ + ancho(), py);
            String etiqueta = formatear(y);
            g2.setColor(Estilo.TEXTO_SUAVE);
            g2.drawString(etiqueta, MARGEN_IZQ - 8 - fm.stringWidth(etiqueta), py + fm.getAscent() / 2 - 1);
        }
    }

    /** Área bajo f entre a y b, con líneas punteadas en los límites. */
    private void dibujarArea(Graphics2D g2) {
        if (areaDesde == null || series.isEmpty()) {
            return;
        }
        double[] valores = series.get(0).valores;
        double cero = aPantallaY(0);
        Path2D area = new Path2D.Double();
        boolean abierta = false;
        for (int i = 0; i < puntosX.length; i++) {
            double x = puntosX[i];
            if (x < areaDesde || x > areaHasta || !Double.isFinite(valores[i])) {
                continue;
            }
            double px = aPantallaX(x);
            if (!abierta) {
                area.moveTo(px, cero);
                abierta = true;
            }
            area.lineTo(px, aPantallaY(valores[i]));
            if (i + 1 >= puntosX.length || puntosX[i + 1] > areaHasta) {
                area.lineTo(px, cero);
            }
        }
        if (abierta) {
            area.closePath();
            g2.setColor(new Color(Estilo.ACENTO.getRed(), Estilo.ACENTO.getGreen(), Estilo.ACENTO.getBlue(), 55));
            g2.fill(area);
        }

        g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
                new float[]{4f, 4f}, 0f));
        g2.setColor(Estilo.TEXTO_SUAVE);
        for (double limite : new double[]{areaDesde, areaHasta}) {
            int px = (int) Math.round(aPantallaX(limite));
            g2.drawLine(px, MARGEN_ARRIBA, px, MARGEN_ARRIBA + alto());
        }
        g2.setStroke(new BasicStroke(1f));
    }

    /** La curva se corta donde la función no existe o da un salto enorme (asíntota). */
    private void dibujarCurva(Graphics2D g2, Serie serie) {
        Path2D camino = new Path2D.Double();
        boolean dibujando = false;
        double rango = yMax - yMin;
        double anterior = Double.NaN;
        for (int i = 0; i < puntosX.length; i++) {
            double y = serie.valores[i];
            boolean valido = Double.isFinite(y) && y > yMin - 3 * rango && y < yMax + 3 * rango;
            boolean salto = dibujando && Math.abs(y - anterior) > 2 * rango;
            if (!valido || salto) {
                dibujando = false;
                anterior = y;
                if (!valido) {
                    continue;
                }
            }
            double px = aPantallaX(puntosX[i]);
            double py = aPantallaY(y);
            if (dibujando) {
                camino.lineTo(px, py);
            } else {
                camino.moveTo(px, py);
                dibujando = true;
            }
            anterior = y;
        }
        g2.setColor(serie.color);
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(camino);
        g2.setStroke(new BasicStroke(1f));
    }

    private void dibujarLeyenda(Graphics2D g2) {
        FontMetrics fm = g2.getFontMetrics();
        int x = MARGEN_IZQ + 10;
        int y = MARGEN_ARRIBA + 14;
        for (Serie serie : series) {
            g2.setColor(serie.color);
            g2.fillRoundRect(x, y - 5, 14, 3, 2, 2);
            g2.setColor(Estilo.TEXTO);
            g2.drawString(serie.nombre, x + 20, y);
            x += 20 + fm.stringWidth(serie.nombre) + 18;
        }
        if (areaDesde != null) {
            g2.setColor(new Color(Estilo.ACENTO.getRed(), Estilo.ACENTO.getGreen(), Estilo.ACENTO.getBlue(), 90));
            g2.fillRect(x, y - 9, 12, 10);
            g2.setColor(Estilo.TEXTO);
            g2.drawString("área entre a y b", x + 18, y);
        }
    }

    /** Línea vertical bajo el mouse y un recuadro con x y el valor de cada curva. */
    private void dibujarValoresBajoMouse(Graphics2D g2) {
        if (mouseX < MARGEN_IZQ || mouseX > MARGEN_IZQ + ancho() || series.isEmpty()) {
            return;
        }
        double x = xMin + (mouseX - MARGEN_IZQ) / (double) ancho() * (xMax - xMin);
        int indice = (int) Math.round((x - xMin) / (xMax - xMin) * (puntosX.length - 1));
        indice = Math.max(0, Math.min(puntosX.length - 1, indice));
        int px = (int) Math.round(aPantallaX(puntosX[indice]));

        g2.setColor(new Color(0x3A4050));
        g2.drawLine(px, MARGEN_ARRIBA, px, MARGEN_ARRIBA + alto());

        List<String> lineas = new ArrayList<>();
        List<Color> colores = new ArrayList<>();
        lineas.add("x = " + formatear(puntosX[indice]));
        colores.add(Estilo.TEXTO);
        for (Serie serie : series) {
            double y = serie.valores[indice];
            String nombre = serie.nombre.split(" ")[0];
            lineas.add(nombre + " = " + (Double.isFinite(y) ? formatear(y) : "no existe"));
            colores.add(serie.color);
            if (Double.isFinite(y) && y >= yMin && y <= yMax) {
                int py = (int) Math.round(aPantallaY(y));
                g2.setColor(serie.color);
                g2.fillOval(px - 4, py - 4, 8, 8);
            }
        }

        FontMetrics fm = g2.getFontMetrics();
        int anchoCaja = 0;
        for (String linea : lineas) {
            anchoCaja = Math.max(anchoCaja, fm.stringWidth(linea));
        }
        anchoCaja += 16;
        int altoCaja = lineas.size() * (fm.getHeight() + 1) + 8;
        int cajaX = px + 12 + anchoCaja > MARGEN_IZQ + ancho() ? px - 12 - anchoCaja : px + 12;
        int cajaY = MARGEN_ARRIBA + 24;

        g2.setColor(new Color(0x1F2330));
        g2.fillRoundRect(cajaX, cajaY, anchoCaja, altoCaja, 8, 8);
        g2.setColor(Estilo.BORDE);
        g2.drawRoundRect(cajaX, cajaY, anchoCaja, altoCaja, 8, 8);
        int lineaY = cajaY + 4 + fm.getAscent();
        for (int i = 0; i < lineas.size(); i++) {
            g2.setColor(colores.get(i));
            g2.drawString(lineas.get(i), cajaX + 8, lineaY);
            lineaY += fm.getHeight() + 1;
        }
    }
}
