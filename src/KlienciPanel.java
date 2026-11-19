import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Widok modułu "Klienci": lista klientów z wyszukiwaniem, dodawaniem,
 * edycją, usuwaniem i generowaniem raportu.
 */
public class KlienciPanel extends JPanel {

    private static final Path KATALOG_RAPORTOW = Paths.get("raporty");

    private final BazaKlientow baza;
    private final Runnable poZmianie;
    private final ModelTabeli model = new ModelTabeli();
    private final JTable tabela = new JTable(model);
    private final JTextField szukaj = new JTextField(22);
    private final JLabel status = new JLabel(" ");
    private List<Klient> widoczni = new ArrayList<>();

    public KlienciPanel(BazaKlientow baza, Runnable poZmianie) {
        super(new BorderLayout(0, 16));
        this.baza = baza;
        this.poZmianie = poZmianie;
        setOpaque(false);

        add(budujGore(), BorderLayout.NORTH);
        add(budujTabele(), BorderLayout.CENTER);
        add(budujDol(), BorderLayout.SOUTH);

        odswiez();
    }

    // ---------------------------------------------------------------- budowa widoku

    private JComponent budujGore() {
        JLabel tytul = new JLabel("Klienci");
        tytul.setFont(Main.czcionka(Font.BOLD, 28, "Segoe Print", "Comic Sans MS"));
        tytul.setForeground(Main.MALINA);

        JLabel lupka = new JLabel("Szukaj:");
        lupka.setFont(Main.czcionka(Font.BOLD, 14, "Segoe UI"));
        lupka.setForeground(Main.SLIWKA);

        szukaj.setFont(Main.czcionka(Font.PLAIN, 14, "Segoe UI"));
        szukaj.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Main.ROZ, 2, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        // klasa anonimowa – filtrowanie listy przy każdej zmianie tekstu
        szukaj.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                odswiez();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                odswiez();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                odswiez();
            }
        });

        JPanel wyszukiwanie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        wyszukiwanie.setOpaque(false);
        wyszukiwanie.add(lupka);
        wyszukiwanie.add(szukaj);

        JPanel gora = new JPanel(new BorderLayout());
        gora.setOpaque(false);
        gora.add(tytul, BorderLayout.WEST);
        gora.add(wyszukiwanie, BorderLayout.EAST);
        return gora;
    }

    private JComponent budujTabele() {
        tabela.setRowHeight(30);
        tabela.setFont(Main.czcionka(Font.PLAIN, 13, "Segoe UI"));
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setShowVerticalLines(false);
        tabela.setGridColor(Main.ROZ_PUDROWY);
        tabela.setSelectionBackground(Main.ROZ);
        tabela.setSelectionForeground(Color.WHITE);
        tabela.setFillsViewportHeight(true);
        tabela.setBackground(Color.WHITE);

        JTableHeader naglowek = tabela.getTableHeader();
        naglowek.setReorderingAllowed(false);
        naglowek.setFont(Main.czcionka(Font.BOLD, 13, "Segoe UI"));
        naglowek.setBackground(Main.ROZ_GORACY);
        naglowek.setForeground(Color.WHITE);
        naglowek.setPreferredSize(new Dimension(0, 32));

        // klasa anonimowa – naprzemienne kolory wierszy
        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object wartosc, boolean zaznaczony,
                                                           boolean fokus, int wiersz, int kolumna) {
                super.getTableCellRendererComponent(t, wartosc, zaznaczony, fokus, wiersz, kolumna);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                if (!zaznaczony) {
                    setBackground(wiersz % 2 == 0 ? Color.WHITE : Main.ROZ_MLECZNY);
                    setForeground(Main.SLIWKA);
                }
                return this;
            }
        });

        int[] szerokosci = {40, 100, 170, 110, 190, 250, 60};
        for (int i = 0; i < szerokosci.length; i++) {
            tabela.getColumnModel().getColumn(i).setPreferredWidth(szerokosci[i]);
        }

        // klasa anonimowa – dwuklik na wierszu otwiera edycję
        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabela.getSelectedRow() >= 0) {
                    edytuj();
                }
            }
        });

        JScrollPane przewijanie = new JScrollPane(tabela);
        przewijanie.setBorder(BorderFactory.createLineBorder(Main.ROZ, 2, true));
        przewijanie.getViewport().setBackground(Color.WHITE);
        return przewijanie;
    }

    private JComponent budujDol() {
        Main.SerduszkowyPrzycisk dodaj = new Main.SerduszkowyPrzycisk("Dodaj");
        Main.SerduszkowyPrzycisk edytuj = new Main.SerduszkowyPrzycisk("Edytuj");
        Main.SerduszkowyPrzycisk usun = new Main.SerduszkowyPrzycisk("Usuń");
        Main.SerduszkowyPrzycisk raport = new Main.SerduszkowyPrzycisk("Raport do pliku");

        dodaj.addActionListener(e -> dodaj());
        edytuj.addActionListener(e -> edytuj());
        usun.addActionListener(e -> usun());
        raport.addActionListener(e -> raport());

        JPanel przyciski = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        przyciski.setOpaque(false);
        for (JButton b : new JButton[]{dodaj, edytuj, usun, raport}) {
            Dimension d = b.getPreferredSize();
            b.setPreferredSize(new Dimension(d.width + 20, 44));
            przyciski.add(b);
        }

        status.setFont(Main.czcionka(Font.ITALIC, 13, "Segoe UI"));
        status.setForeground(Main.SLIWKA);

        JPanel dol = new JPanel(new BorderLayout(0, 8));
        dol.setOpaque(false);
        dol.add(przyciski, BorderLayout.NORTH);
        dol.add(status, BorderLayout.SOUTH);
        return dol;
    }

    // ---------------------------------------------------------------- akcje

    private void odswiez() {
        widoczni = baza.szukaj(szukaj.getText());
        model.fireTableDataChanged();
        status.setText("Wyświetlono " + widoczni.size() + " z " + baza.liczba() + " klientów.");
    }

    private Klient zaznaczony() {
        int wiersz = tabela.getSelectedRow();
        if (wiersz < 0) {
            JOptionPane.showMessageDialog(this, "Najpierw zaznacz klienta na liście.",
                    "Brak zaznaczenia", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return widoczni.get(tabela.convertRowIndexToModel(wiersz));
    }

    private void dodaj() {
        Klient nowy = new FormularzKlienta(null).pokaz();
        if (nowy != null) {
            baza.dodaj(nowy);
            zapiszZmiany("Dodano klienta: " + nowy.getPelneImie());
        }
    }

    private void edytuj() {
        Klient stary = zaznaczony();
        if (stary == null) {
            return;
        }
        Klient nowy = new FormularzKlienta(stary).pokaz();
        if (nowy != null) {
            baza.zamien(stary, nowy);
            zapiszZmiany("Zapisano zmiany: " + nowy.getPelneImie());
        }
    }

    private void usun() {
        Klient k = zaznaczony();
        if (k == null) {
            return;
        }
        int odp = JOptionPane.showConfirmDialog(this,
                "Czy na pewno usunąć klienta " + k.getPelneImie() + "?",
                "Usuwanie klienta", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (odp == JOptionPane.YES_OPTION) {
            baza.usun(k);
            zapiszZmiany("Usunięto klienta: " + k.getPelneImie());
        }
    }

    private void raport() {
        try {
            Path plik = baza.generujRaport(KATALOG_RAPORTOW);
            int odp = JOptionPane.showConfirmDialog(this,
                    "Raport zapisano do pliku:\n" + plik.toAbsolutePath() + "\n\nOtworzyć go teraz?",
                    "Raport gotowy", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (odp == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(plik.toFile());
            }
        } catch (IOException ex) {
            pokazBlad("Nie udało się zapisać raportu", ex);
        }
    }

    private void zapiszZmiany(String komunikat) {
        odswiez();
        try {
            baza.zapisz();
            status.setText(komunikat + "  ♥  Zapisano do pliku.");
        } catch (IOException ex) {
            pokazBlad("Nie udało się zapisać bazy klientów", ex);
        }
        poZmianie.run();
    }

    private void pokazBlad(String tytul, Exception ex) {
        JOptionPane.showMessageDialog(this, tytul + ":\n" + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
    }

    // ---------------------------------------------------------------- klasy wewnętrzne

    /** Model tabeli – klasa wewnętrzna korzystająca z listy {@code widoczni} panelu. */
    private class ModelTabeli extends AbstractTableModel {

        private final String[] kolumny = {"ID", "Typ", "Imię i nazwisko", "Telefon", "E-mail", "Szczegóły", "Rabat"};

        @Override
        public int getRowCount() {
            return widoczni.size();
        }

        @Override
        public int getColumnCount() {
            return kolumny.length;
        }

        @Override
        public String getColumnName(int kolumna) {
            return kolumny[kolumna];
        }

        @Override
        public Object getValueAt(int wiersz, int kolumna) {
            Klient k = widoczni.get(wiersz);
            switch (kolumna) {
                case 0: return k.getId();
                case 1: return k.getTyp();
                case 2: return k.getPelneImie();
                case 3: return k.getTelefon();
                case 4: return k.getEmail();
                case 5: return k.getSzczegoly();
                case 6: return String.format("%.0f%%", k.getRabat() * 100);
                default: return "";
            }
        }
    }

    /** Okno dodawania/edycji klienta – klasa wewnętrzna. */
    private class FormularzKlienta {

        private final Klient edytowany;
        private final JComboBox<String> typ = new JComboBox<>(new String[]{"Indywidualny", "Firmowy"});
        private final JTextField imie = new JTextField(20);
        private final JTextField nazwisko = new JTextField(20);
        private final JTextField telefon = new JTextField(20);
        private final JTextField email = new JTextField(20);
        private final JCheckBox staly = new JCheckBox("Stały klient (rabat 5%)");
        private final JTextField nazwaFirmy = new JTextField(20);
        private final JTextField nip = new JTextField(20);

        FormularzKlienta(Klient edytowany) {
            this.edytowany = edytowany;
            if (edytowany != null) {
                imie.setText(edytowany.getImie());
                nazwisko.setText(edytowany.getNazwisko());
                telefon.setText(edytowany.getTelefon());
                email.setText(edytowany.getEmail());
                if (edytowany instanceof KlientFirmowy kf) {
                    typ.setSelectedItem("Firmowy");
                    nazwaFirmy.setText(kf.getNazwaFirmy());
                    nip.setText(kf.getNip());
                } else if (edytowany instanceof KlientIndywidualny ki) {
                    staly.setSelected(ki.isStaly());
                }
            }
            // klasa anonimowa – włączanie pól zależnie od typu klienta
            typ.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    przelaczPola();
                }
            });
            przelaczPola();
        }

        private void przelaczPola() {
            boolean firmowy = "Firmowy".equals(typ.getSelectedItem());
            nazwaFirmy.setEnabled(firmowy);
            nip.setEnabled(firmowy);
            staly.setEnabled(!firmowy);
        }

        /** Pokazuje formularz aż do poprawnego wypełnienia lub anulowania. */
        Klient pokaz() {
            JPanel p = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(4, 4, 4, 4);
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;

            Object[][] wiersze = {
                    {"Typ klienta:", typ}, {"Imię:", imie}, {"Nazwisko:", nazwisko},
                    {"Telefon:", telefon}, {"E-mail:", email}, {"", staly},
                    {"Nazwa firmy:", nazwaFirmy}, {"NIP:", nip}
            };
            for (int i = 0; i < wiersze.length; i++) {
                c.gridy = i;
                c.gridx = 0;
                p.add(new JLabel((String) wiersze[i][0]), c);
                c.gridx = 1;
                p.add((Component) wiersze[i][1], c);
            }

            String tytul = edytowany == null ? "Nowy klient" : "Edycja klienta #" + edytowany.getId();
            while (true) {
                int odp = JOptionPane.showConfirmDialog(KlienciPanel.this, p, tytul,
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (odp != JOptionPane.OK_OPTION) {
                    return null;
                }
                try {
                    return utworz();
                } catch (NieprawidloweDaneException ex) {
                    JOptionPane.showMessageDialog(KlienciPanel.this, ex.getMessage(),
                            "Niepoprawne dane", JOptionPane.WARNING_MESSAGE);
                }
            }
        }

        private Klient utworz() throws NieprawidloweDaneException {
            int id = edytowany != null ? edytowany.getId() : baza.nastepneId();
            if ("Firmowy".equals(typ.getSelectedItem())) {
                return new KlientFirmowy(id, imie.getText(), nazwisko.getText(), telefon.getText(),
                        email.getText(), nip.getText(), nazwaFirmy.getText());
            }
            return new KlientIndywidualny(id, imie.getText(), nazwisko.getText(), telefon.getText(),
                    email.getText(), staly.isSelected());
        }
    }
}
