/**
 * Wyjątek zgłaszany, gdy dane obiektu (np. klienta) są niepoprawne.
 */
public class NieprawidloweDaneException extends Exception {

    public NieprawidloweDaneException(String komunikat) {
        super(komunikat);
    }
}
