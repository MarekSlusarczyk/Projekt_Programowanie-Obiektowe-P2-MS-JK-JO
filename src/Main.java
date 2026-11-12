import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

    static Color ROZ_GORACY = new Color(255, 79, 163);
    static Color ROZ = new Color(255, 133, 192);
    static Color ROZ_PUDROWY = new Color(255, 194, 222);
    static Color ROZ_MLECZNY = new Color(255, 236, 245);
    static Color LAWENDA = new Color(214, 180, 255);
    static Color LAWENDA_JASNA = new Color(242, 230, 255);
    static Color MALINA = new Color(196, 30, 118);
    static Color SLIWKA = new Color(140, 40, 110);
    static Color BIALY = Color.WHITE;

    static Color[][] KOLORY_KART = {
            {ROZ_PUDROWY, LAWENDA_JASNA},
            {LAWENDA_JASNA, ROZ_MLECZNY},
            {new Color(255, 214, 232), new Color(255, 239, 218)},
            {new Color(232, 214, 255), new Color(216, 238, 255)}
    };
    static int licznikKart = 0;


    static Font czcionka(int styl, int rozmiar, String... kandydaci) {
        java.util.List<String> dostepne = java.util.Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()
        );
        for (String nazwa : kandydaci) {
            if (dostepne.contains(nazwa)) {
                return new Font(nazwa, styl, rozmiar);
            }
        }
        return new Font(Font.SANS_SERIF, styl, rozmiar);
    }

    static void wygladz(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    static Shape serce(double cx, double cy, double s) {
        Path2D p = new Path2D.Double();
        p.moveTo(0.5, 0.25);
        p.curveTo(0.5, 0.0, 0.0, 0.0, 0.0, 0.3);
        p.curveTo(0.0, 0.6, 0.5, 0.75, 0.5, 1.0);
        p.curveTo(0.5, 0.75, 1.0, 0.6, 1.0, 0.3);
        p.curveTo(1.0, 0.0, 0.5, 0.0, 0.5, 0.25);
        p.closePath();
        AffineTransform at = new AffineTransform();
        at.translate(cx - s / 2, cy - s / 2);
        at.scale(s, s);
        return at.createTransformedShape(p);
    }

    static Shape gwiazdka(double cx, double cy, double r) {
        Path2D p = new Path2D.Double();
        for (int i = 0; i < 8; i++) {
            double kat = -Math.PI / 2 + i * Math.PI / 4;
            double rr = (i % 2 == 0) ? r : r * 0.28;
            double px = cx + Math.cos(kat) * rr;
            double py = cy + Math.sin(kat) * rr;
            if (i == 0) p.moveTo(px, py); else p.lineTo(px, py);
        }
        p.closePath();
        return p;
    }

    static float[] rownomierne(int n) {
        float[] f = new float[n];
        for (int i = 0; i < n; i++) f[i] = n == 1 ? 0f : (float) i / (n - 1);
        return f;
    }

    static Image ikonaSerca() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        wygladz(g2);
        Shape s = serce(32, 34, 56);
        g2.setPaint(new GradientPaint(0, 0, ROZ, 64, 64, ROZ_GORACY));
        g2.fill(s);
        g2.setColor(BIALY);
        g2.setStroke(new BasicStroke(3f));
        g2.draw(s);
        g2.fill(gwiazdka(46, 20, 8));
        g2.dispose();
        return img;
    }

    static class Czasteczka {
        double x, y, rozmiar, predkosc, faza;
        int typ;
        Color kolor;

        Czasteczka(Random r, Color kolor) {
            x = r.nextDouble();
            y = r.nextDouble();
            rozmiar = 6 + r.nextDouble() * 14;
            predkosc = 0.0006 + r.nextDouble() * 0.0014;
            faza = r.nextDouble() * Math.PI * 2;
            typ = r.nextInt(3);
            this.kolor = kolor;
        }

        void krok() {
            y -= predkosc;
            if (y < -0.08) y = 1.08;
        }

        void rysuj(Graphics2D g2, int w, int h, double t) {
            double px = x * w + Math.sin(t * 0.8 + faza) * 12;
            double py = y * h;
            double blysk = 0.5 + 0.5 * Math.sin(t * 2.2 + faza);
            int alfa = (int) (50 + 150 * blysk);
            g2.setColor(new Color(kolor.getRed(), kolor.getGreen(), kolor.getBlue(), alfa));
            if (typ == 0) {
                g2.fill(serce(px, py, rozmiar));
            } else if (typ == 1) {
                g2.fill(gwiazdka(px, py, rozmiar * 0.6 * (0.6 + 0.4 * blysk)));
            } else {
                g2.fill(new Ellipse2D.Double(px - rozmiar / 4, py - rozmiar / 4, rozmiar / 2, rozmiar / 2));
            }
        }
    }

    static class MagicznyPanel extends JPanel {
        private final Color[] gradient;
        private final boolean pionowy;
        private final List<Czasteczka> czasteczki = new ArrayList<>();
        private boolean koronka;
        private double czas;

        MagicznyPanel(LayoutManager layout, Color[] gradient, boolean pionowy,
                      int ile, Color[] kolory, long ziarno) {
            super(layout);
            this.gradient = gradient;
            this.pionowy = pionowy;
            Random r = new Random(ziarno);
            for (int i = 0; i < ile; i++) {
                czasteczki.add(new Czasteczka(r, kolory[r.nextInt(kolory.length)]));
            }
            new Timer(40, e -> {
                czas += 0.04;
                for (Czasteczka c : czasteczki) c.krok();
                repaint();
            }).start();
        }

        void ustawKoronke(boolean k) {
            koronka = k;
        }

        @Override
        protected void paintComponent(Graphics g) {
            int w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;
            Graphics2D g2 = (Graphics2D) g.create();
            wygladz(g2);

            g2.setPaint(new LinearGradientPaint(0, 0, pionowy ? 0 : w, pionowy ? h : 0,
                    rownomierne(gradient.length), gradient));
            g2.fillRect(0, 0, w, h);

            for (Czasteczka c : czasteczki) c.rysuj(g2, w, h, czas);

            if (koronka) {
                int r = 9;
                g2.setColor(new Color(255, 255, 255, 170));
                for (int x = 0; x < w + r * 2; x += r * 2) {
                    g2.fill(new Ellipse2D.Double(x - r, h - r, r * 2, r * 2));
                }
                g2.setColor(new Color(255, 255, 255, 230));
                for (int x = 0; x < w + r * 2; x += r * 2) {
                    g2.fill(new Ellipse2D.Double(x - 2, h - r - 7, 4, 4));
                }
            }
            g2.dispose();
        }
    }

    static class BajkowyNapis extends JComponent {

        private static final double POCHYLENIE = 0.3;
        private static final int MARGINES = 16;
        private final String tekst;
        private final Font font;

        BajkowyNapis(String tekst) {
            this.tekst = tekst;
            this.font = czcionka(Font.BOLD, 44, "Segoe Script", "Gabriola", "Monotype Corsiva", "Lucida Calligraphy");
            setOpaque(false);
        }

        private Shape kontur() {
            FontRenderContext frc = new FontRenderContext(null, true, true);
            return new TextLayout(tekst, font, frc).getOutline(null);
        }

        @Override
        public Dimension getPreferredSize() {
            Rectangle2D b = kontur().getBounds2D();
            int szer = (int) (b.getWidth() - b.getY() * POCHYLENIE + 2 * MARGINES + 60);
            int wys = (int) (b.getHeight() + 2 * MARGINES);
            return new Dimension(szer, wys);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            wygladz(g2);

            Shape k = kontur();
            Rectangle2D b = k.getBounds2D();
            double t = System.currentTimeMillis() / 1000.0;

            g2.translate(MARGINES - b.getX(), MARGINES - b.getY());
            AffineTransform bezPochylenia = g2.getTransform();
            g2.shear(-POCHYLENIE, 0);

            BasicStroke okragla = new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

            for (int i = 4; i >= 1; i--) {
                g2.setColor(new Color(255, 255, 255, 28));
                g2.setStroke(new BasicStroke(8 + i * 5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(k);
            }

            Shape cien = AffineTransform.getTranslateInstance(3, 4).createTransformedShape(k);
            g2.setColor(new Color(SLIWKA.getRed(), SLIWKA.getGreen(), SLIWKA.getBlue(), 110));
            g2.setStroke(okragla);
            g2.draw(cien);
            g2.fill(cien);

            g2.setColor(BIALY);
            g2.draw(k);
            g2.setColor(MALINA);
            g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(k);

            g2.setPaint(new LinearGradientPaint(
                    (float) b.getX(), (float) b.getY(), (float) b.getMaxX(), (float) b.getMaxY(),
                    new float[]{0f, 0.5f, 1f},
                    new Color[]{ROZ_MLECZNY, ROZ_PUDROWY, LAWENDA}
            ));
            g2.fill(k);

            g2.setTransform(bezPochylenia);
            double puls = 1 + 0.12 * Math.sin(t * 6);
            double blysk = 0.6 + 0.4 * Math.sin(t * 4);

            double hy1 = b.getY() * 0.5;
            Shape duzeSerce = serce(b.getMaxX() + 26 - POCHYLENIE * hy1, hy1, 26 * puls);
            g2.setColor(ROZ_GORACY);
            g2.fill(duzeSerce);
            g2.setColor(BIALY);
            g2.setStroke(new BasicStroke(2.5f));
            g2.draw(duzeSerce);

            double hy2 = b.getY() * 0.95;
            Shape maleSerce = serce(b.getMaxX() + 46 - POCHYLENIE * hy2, hy2, 13);
            g2.setColor(LAWENDA_JASNA);
            g2.fill(maleSerce);
            g2.setColor(BIALY);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(maleSerce);

            g2.setColor(BIALY);
            g2.fill(gwiazdka(b.getX() - 4, b.getY() + 2, 8 * blysk));
            g2.fill(gwiazdka(b.getCenterX() + 20, b.getY() - 6, 5 * (1.6 - blysk)));
            g2.fill(gwiazdka(b.getMaxX() + 8, b.getMaxY() - 2, 6 * blysk));

            g2.dispose();
        }
    }

    static class SerduszkowyPrzycisk extends JButton {
        private boolean najechany;

        SerduszkowyPrzycisk(String tekst) {
            super(tekst);
            setFont(czcionka(Font.BOLD, 15, "Segoe Print", "Comic Sans MS"));
            setForeground(BIALY);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(6, 46, 6, 10));
            setHorizontalAlignment(SwingConstants.LEFT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    najechany = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    najechany = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            wygladz(g2);
            int w = getWidth(), h = getHeight();
            double luk = h - 8;
            int przes = getModel().isPressed() ? 2 : 0;

            g2.setColor(new Color(196, 30, 118, najechany ? 90 : 55));
            g2.fill(new RoundRectangle2D.Double(3, 6, w - 6, h - 8, luk, luk));

            RoundRectangle2D ksztalt = new RoundRectangle2D.Double(2, 2 + przes, w - 6, h - 8, luk, luk);
            Color c1 = najechany ? LAWENDA : ROZ_GORACY;
            Color c2 = najechany ? ROZ : ROZ;
            g2.setPaint(new GradientPaint(0, 0, c1, w, h, c2));
            g2.fill(ksztalt);

            g2.setPaint(new GradientPaint(0, 2 + przes, new Color(255, 255, 255, 160),
                    0, h / 2f, new Color(255, 255, 255, 0)));
            g2.fill(new RoundRectangle2D.Double(12, 4 + przes, w - 30, (h - 8) / 2.0, luk / 2, luk / 2));

            g2.setColor(new Color(255, 255, 255, 230));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(ksztalt);

            double puls = najechany ? 1 + 0.15 * Math.sin(System.currentTimeMillis() / 110.0) : 1;
            g2.setColor(BIALY);
            g2.fill(serce(25, 2 + przes + (h - 8) / 2.0, 17 * puls));
            if (najechany) {
                g2.fill(gwiazdka(w - 24, 2 + przes + (h - 8) / 2.0, 6));
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class KartaPanel extends JPanel {
        private final Color a, b;
        private boolean najechana;

        KartaPanel(Color a, Color b) {
            super(new GridLayout(3, 1, 5, 5));
            this.a = a;
            this.b = b;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(22, 28, 26, 28));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    najechana = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    najechana = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            wygladz(g2);
            int w = getWidth(), h = getHeight();
            double luk = 40;
            double t = System.currentTimeMillis() / 1000.0;

            g2.setColor(new Color(255, 79, 163, najechana ? 80 : 40));
            g2.fill(new RoundRectangle2D.Double(6, najechana ? 12 : 8, w - 10, h - 14, luk, luk));

            RoundRectangle2D cialo = new RoundRectangle2D.Double(2, 2, w - 10, h - 14, luk, luk);
            g2.setPaint(new GradientPaint(0, 0, a, w, h, b));
            g2.fill(cialo);

            g2.setColor(new Color(255, 255, 255, 230));
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1f, new float[]{7f, 7f}, 0f));
            g2.draw(new RoundRectangle2D.Double(10, 10, w - 26, h - 30, luk - 14, luk - 14));

            g2.setColor(new Color(255, 133, 192, najechana ? 230 : 160));
            g2.setStroke(new BasicStroke(najechana ? 3f : 2f));
            g2.draw(cialo);

            double puls = najechana ? 1 + 0.1 * Math.sin(t * 7) : 1;
            g2.setColor(new Color(255, 255, 255, 190));
            g2.fill(serce(w - 58, 50, 52 * puls));
            g2.setColor(ROZ_GORACY);
            g2.fill(serce(w - 58, 50, 22 * puls));

            double blysk = 0.5 + 0.5 * Math.sin(t * 3 + a.getRed());
            g2.setColor(BIALY);
            g2.fill(gwiazdka(w - 98, 26, 4 + 5 * blysk));
            g2.fill(gwiazdka(w - 30, 92, 3 + 4 * (1 - blysk)));
            g2.fill(gwiazdka(w - 40, h - 40, 3 + 3 * blysk));

            g2.dispose();
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame okno = new JFrame("AutoSerwis ♥ System zarządzania");
            okno.setSize(1100, 720);
            okno.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            okno.setLocationRelativeTo(null);
            okno.setLayout(new BorderLayout());
            okno.getContentPane().setBackground(ROZ_MLECZNY);
            okno.setIconImage(ikonaSerca());

            MagicznyPanel naglowek = new MagicznyPanel(new BorderLayout(),
                    new Color[]{ROZ_GORACY, ROZ, LAWENDA}, false, 26,
                    new Color[]{BIALY, ROZ_MLECZNY, LAWENDA_JASNA}, 1);
            naglowek.ustawKoronke(true);
            naglowek.setBorder(BorderFactory.createEmptyBorder(10, 24, 24, 30));

            BajkowyNapis nazwa = new BajkowyNapis("AutoSerwis");

            JLabel podtytul = new JLabel("SYSTEM ZARZĄDZANIA WARSZTATEM SAMOCHODOWYM");
            podtytul.setFont(czcionka(Font.BOLD, 12, "Segoe UI Semibold", "Segoe UI"));
            podtytul.setForeground(BIALY);
            podtytul.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));

            JPanel tekstNaglowka = new JPanel();
            tekstNaglowka.setLayout(new BoxLayout(tekstNaglowka, BoxLayout.Y_AXIS));
            tekstNaglowka.setOpaque(false);
            nazwa.setAlignmentX(Component.LEFT_ALIGNMENT);
            podtytul.setAlignmentX(Component.LEFT_ALIGNMENT);
            tekstNaglowka.add(nazwa);
            tekstNaglowka.add(podtytul);

            JLabel pozdrowienie = new JLabel("Miłego dnia, słoneczko!");
            pozdrowienie.setFont(czcionka(Font.BOLD, 17, "Segoe Print", "Comic Sans MS"));
            pozdrowienie.setForeground(BIALY);

            naglowek.add(tekstNaglowka, BorderLayout.WEST);
            naglowek.add(pozdrowienie, BorderLayout.EAST);

            MagicznyPanel menu = new MagicznyPanel(new GridLayout(7, 1, 0, 12),
                    new Color[]{ROZ_MLECZNY, ROZ_PUDROWY, LAWENDA_JASNA}, true, 20,
                    new Color[]{BIALY, ROZ, LAWENDA}, 2);
            menu.setBorder(BorderFactory.createEmptyBorder(25, 15, 25, 15));

            String[] nazwyPrzyciskow = {
                    "Panel główny",
                    "Klienci",
                    "Samochody",
                    "Naprawy",
                    "Magazyn części",
                    "Historia napraw",
                    "Raporty"
            };

            for (String nazwaPrzycisku : nazwyPrzyciskow) {
                menu.add(new SerduszkowyPrzycisk(nazwaPrzycisku));
            }

            menu.setPreferredSize(new Dimension(250, 600));

            MagicznyPanel panelGlowny = new MagicznyPanel(new BorderLayout(20, 20),
                    new Color[]{ROZ_MLECZNY, new Color(255, 246, 251), LAWENDA_JASNA}, true, 34,
                    new Color[]{ROZ_PUDROWY, LAWENDA, ROZ}, 3);
            panelGlowny.setBorder(BorderFactory.createEmptyBorder(26, 30, 30, 30));

            JLabel powitanie = new JLabel("Witaj w systemie AutoSerwis!");
            powitanie.setFont(czcionka(Font.BOLD, 28, "Segoe Print", "Comic Sans MS"));
            powitanie.setForeground(MALINA);

            JLabel opis = new JLabel(
                    "Zarządzaj klientami, samochodami i naprawami w jednym miejscu."
            );
            opis.setFont(czcionka(Font.ITALIC, 15, "Segoe UI"));
            opis.setForeground(SLIWKA);

            JPanel tekstPowitalny = new JPanel(new GridLayout(2, 1, 0, 2));
            tekstPowitalny.setOpaque(false);
            tekstPowitalny.add(powitanie);
            tekstPowitalny.add(opis);

            JPanel karty = new JPanel(new GridLayout(2, 2, 22, 22));
            karty.setOpaque(false);

            karty.add(stworzKarte("Klienci", "0",
                    "Zarejestrowani klienci"));

            karty.add(stworzKarte("Samochody", "0",
                    "Pojazdy w warsztacie"));

            karty.add(stworzKarte("Naprawy", "0",
                    "Aktywne naprawy"));

            karty.add(stworzKarte("Magazyn", "0",
                    "Rodzaje części"));

            panelGlowny.add(tekstPowitalny, BorderLayout.NORTH);
            panelGlowny.add(karty, BorderLayout.CENTER);

            okno.add(naglowek, BorderLayout.NORTH);
            okno.add(menu, BorderLayout.WEST);
            okno.add(panelGlowny, BorderLayout.CENTER);

            okno.setVisible(true);
        });
    }

    public static JPanel stworzKarte(
            String tytul, String liczba, String opis) {

        Color[] kolory = KOLORY_KART[licznikKart++ % KOLORY_KART.length];
        KartaPanel karta = new KartaPanel(kolory[0], kolory[1]);

        JLabel etykieta = new JLabel(tytul);
        etykieta.setFont(czcionka(Font.BOLD, 18, "Segoe Print", "Comic Sans MS"));
        etykieta.setForeground(MALINA);

        JLabel wartosc = new JLabel(liczba);
        wartosc.setFont(czcionka(Font.BOLD, 40, "Segoe Script", "Segoe Print", "Comic Sans MS"));
        wartosc.setForeground(ROZ_GORACY);

        JLabel opisKarty = new JLabel(opis);
        opisKarty.setFont(czcionka(Font.PLAIN, 13, "Segoe UI"));
        opisKarty.setForeground(SLIWKA);

        karta.add(etykieta);
        karta.add(wartosc);
        karta.add(opisKarty);

        return karta;
    }
}
