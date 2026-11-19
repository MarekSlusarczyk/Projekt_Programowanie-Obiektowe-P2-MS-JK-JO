import java.text.Collator;
import java.util.Locale;

/**
 * Abstrakcyjny klient warsztatu. Konkretne rodzaje klientów:
 * {@link KlientIndywidualny} i {@link KlientFirmowy}.
 *
 * Format linii w pliku dane/klienci.txt:
 *   I;id;imię;nazwisko;telefon;email;stały(TAK/NIE)
 *   F;id;imię;nazwisko;telefon;email;NIP;nazwa firmy
 */
public abstract class Klient extends Osoba implements Raportowalny, ZapisywalnyTekstowo, Comparable<Klient> {

    private static final long serialVersionUID = 1L;
    public static final String SEPARATOR = ";";
    private static final Collator POLSKI = Collator.getInstance(new Locale("pl", "PL"));

    private final int id;
    private final String email;

    protected Klient(int id, String imie, String nazwisko, String telefon, String email)
            throws NieprawidloweDaneException {
        super(imie, nazwisko, telefon);
        if (id <= 0) {
            throw new NieprawidloweDaneException("ID klienta musi być dodatnie.");
        }
        this.id = id;
        this.email = Walidacja.email(email);
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    /** Nazwa rodzaju klienta wyświetlana w tabeli. */
    public abstract String getTyp();

    /** Rabat na usługi warsztatu (0.10 = 10%). */
    public abstract double getRabat();

    /** Informacje specyficzne dla danego rodzaju klienta. */
    public abstract String getSzczegoly();

    /** Jednoliterowy kod typu zapisywany w pliku. */
    protected abstract String kodTypu();

    /** Pola dodatkowe zapisywane za polami wspólnymi. */
    protected abstract String[] poleDodatkowe();

    @Override
    public String doLinii() {
        StringBuilder sb = new StringBuilder();
        sb.append(kodTypu()).append(SEPARATOR)
                .append(id).append(SEPARATOR)
                .append(getImie()).append(SEPARATOR)
                .append(getNazwisko()).append(SEPARATOR)
                .append(getTelefon()).append(SEPARATOR)
                .append(email);
        for (String pole : poleDodatkowe()) {
            sb.append(SEPARATOR).append(pole);
        }
        return sb.toString();
    }

    @Override
    public String wierszRaportu() {
        return String.format("%-4d %-13s %-24s %-13s %-28s %5.0f%%  %s",
                id, getTyp(), getPelneImie(), getTelefon(),
                email.isEmpty() ? "-" : email, getRabat() * 100, getSzczegoly());
    }

    @Override
    public String opis() {
        return getTyp() + ": " + getPelneImie() + " (tel. " + getTelefon() + ")";
    }

    @Override
    public int compareTo(Klient inny) {
        int wynik = POLSKI.compare(getNazwisko(), inny.getNazwisko());
        return wynik != 0 ? wynik : POLSKI.compare(getImie(), inny.getImie());
    }

    /** Tworzy odpowiedni obiekt klienta na podstawie linii z pliku (fabryka). */
    public static Klient zLinii(String linia, int nrLinii) throws BlednyFormatDanychException {
        String[] p = linia.split(SEPARATOR, -1);
        try {
            switch (p[0].trim().toUpperCase()) {
                case "I":
                    wymagajPol(p, 7, nrLinii);
                    return new KlientIndywidualny(Integer.parseInt(p[1].trim()), p[2], p[3], p[4], p[5],
                            "TAK".equalsIgnoreCase(p[6].trim()));
                case "F":
                    wymagajPol(p, 8, nrLinii);
                    return new KlientFirmowy(Integer.parseInt(p[1].trim()), p[2], p[3], p[4], p[5],
                            p[6], p[7]);
                default:
                    throw new BlednyFormatDanychException(nrLinii, "nieznany typ klienta „" + p[0] + "”");
            }
        } catch (NumberFormatException e) {
            throw new BlednyFormatDanychException(nrLinii, "ID klienta nie jest liczbą");
        } catch (NieprawidloweDaneException e) {
            throw new BlednyFormatDanychException(nrLinii, e.getMessage());
        }
    }

    private static void wymagajPol(String[] pola, int ile, int nrLinii) throws BlednyFormatDanychException {
        if (pola.length != ile) {
            throw new BlednyFormatDanychException(nrLinii,
                    "oczekiwano " + ile + " pól, znaleziono " + pola.length);
        }
    }
}
