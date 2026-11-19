/**
 * Pomocnicze metody sprawdzające poprawność danych wpisywanych przez użytkownika
 * i wczytywanych z plików.
 */
public final class Walidacja {

    private Walidacja() {
    }

    public static String tekst(String wartosc, String nazwaPola, boolean wymagane) throws NieprawidloweDaneException {
        String w = wartosc == null ? "" : wartosc.trim();
        if (wymagane && w.isEmpty()) {
            throw new NieprawidloweDaneException("Pole „" + nazwaPola + "” nie może być puste.");
        }
        if (w.contains(Klient.SEPARATOR)) {
            throw new NieprawidloweDaneException("Pole „" + nazwaPola + "” nie może zawierać znaku „" + Klient.SEPARATOR + "”.");
        }
        return w;
    }

    public static String telefon(String wartosc) throws NieprawidloweDaneException {
        String w = wartosc == null ? "" : wartosc.replaceAll("[\\s-]", "");
        if (!w.matches("(\\+48)?\\d{9}")) {
            throw new NieprawidloweDaneException("Telefon musi mieć 9 cyfr (opcjonalnie z prefiksem +48).");
        }
        return w;
    }

    public static String email(String wartosc) throws NieprawidloweDaneException {
        String w = tekst(wartosc, "E-mail", false);
        if (!w.isEmpty() && !w.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new NieprawidloweDaneException("Niepoprawny adres e-mail: " + w);
        }
        return w;
    }

    public static String nip(String wartosc) throws NieprawidloweDaneException {
        String w = wartosc == null ? "" : wartosc.replaceAll("[\\s-]", "");
        if (!w.matches("\\d{10}")) {
            throw new NieprawidloweDaneException("NIP musi składać się z 10 cyfr.");
        }
        return w;
    }
}
