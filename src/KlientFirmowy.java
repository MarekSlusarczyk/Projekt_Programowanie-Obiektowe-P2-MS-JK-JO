public class KlientFirmowy extends Klient {

    private static final long serialVersionUID = 1L;

    private final String nip;
    private final String nazwaFirmy;

    public KlientFirmowy(int id, String imie, String nazwisko, String telefon, String email,
                         String nip, String nazwaFirmy) throws NieprawidloweDaneException {
        super(id, imie, nazwisko, telefon, email);
        this.nip = Walidacja.nip(nip);
        this.nazwaFirmy = Walidacja.tekst(nazwaFirmy, "Nazwa firmy", true);
    }

    public String getNip() {
        return nip;
    }

    public String getNazwaFirmy() {
        return nazwaFirmy;
    }

    @Override
    public String getTyp() {
        return "Firmowy";
    }

    @Override
    public double getRabat() {
        return 0.10;
    }

    @Override
    public String getSzczegoly() {
        return nazwaFirmy + ", NIP " + nip;
    }

    @Override
    public String opis() {
        return super.opis() + " – " + nazwaFirmy;
    }

    @Override
    protected String kodTypu() {
        return "F";
    }

    @Override
    protected String[] poleDodatkowe() {
        return new String[]{nip, nazwaFirmy};
    }
}
