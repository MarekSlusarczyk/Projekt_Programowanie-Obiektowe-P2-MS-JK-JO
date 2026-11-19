public class KlientIndywidualny extends Klient {

    private static final long serialVersionUID = 1L;

    private final boolean staly;

    public KlientIndywidualny(int id, String imie, String nazwisko, String telefon, String email, boolean staly)
            throws NieprawidloweDaneException {
        super(id, imie, nazwisko, telefon, email);
        this.staly = staly;
    }

    public boolean isStaly() {
        return staly;
    }

    @Override
    public String getTyp() {
        return "Indywidualny";
    }

    @Override
    public double getRabat() {
        return staly ? 0.05 : 0.0;
    }

    @Override
    public String getSzczegoly() {
        return staly ? "Stały klient" : "-";
    }

    @Override
    protected String kodTypu() {
        return "I";
    }

    @Override
    protected String[] poleDodatkowe() {
        return new String[]{staly ? "TAK" : "NIE"};
    }
}
