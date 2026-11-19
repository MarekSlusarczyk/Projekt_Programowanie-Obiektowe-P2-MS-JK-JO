/**
 * Wyjątek zgłaszany, gdy linia w pliku tekstowym bazy danych ma zły format.
 */
public class BlednyFormatDanychException extends Exception {

    private final int numerLinii;

    public BlednyFormatDanychException(int numerLinii, String komunikat) {
        super("Linia " + numerLinii + ": " + komunikat);
        this.numerLinii = numerLinii;
    }

    public int getNumerLinii() {
        return numerLinii;
    }
}
