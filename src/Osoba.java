import java.io.Serializable;

/**
 * Klasa abstrakcyjna – wspólna baza dla wszystkich osób w systemie
 * (klienci, w przyszłości np. mechanicy).
 */
public abstract class Osoba implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String imie;
    private final String nazwisko;
    private final String telefon;

    protected Osoba(String imie, String nazwisko, String telefon) throws NieprawidloweDaneException {
        this.imie = Walidacja.tekst(imie, "Imię", true);
        this.nazwisko = Walidacja.tekst(nazwisko, "Nazwisko", true);
        this.telefon = Walidacja.telefon(telefon);
    }

    public String getImie() {
        return imie;
    }

    public String getNazwisko() {
        return nazwisko;
    }

    public String getTelefon() {
        return telefon;
    }

    public String getPelneImie() {
        return imie + " " + nazwisko;
    }

    /** Krótki opis osoby – każda podklasa opisuje się po swojemu. */
    public abstract String opis();

    @Override
    public String toString() {
        return getPelneImie();
    }
}
