import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * "Baza danych" klientów oparta na pliku tekstowym.
 * Dodatkowo przy każdym zapisie tworzona jest kopia zapasowa przez serializację (.ser),
 * z której baza jest odtwarzana, gdy pliku tekstowego brakuje.
 */
public class BazaKlientow {

    private static final DateTimeFormatter FORMAT_DATY = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FORMAT_PLIKU = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final Path plikDanych;
    private final Path plikKopii;
    private final List<Klient> klienci = new ArrayList<>();
    private final List<String> ostrzezenia = new ArrayList<>();

    public BazaKlientow(Path plikDanych) {
        this.plikDanych = plikDanych;
        this.plikKopii = plikDanych.resolveSibling(plikDanych.getFileName().toString().replaceFirst("\\.txt$", "") + ".ser");
    }

    // ---------------------------------------------------------------- odczyt / zapis

    public void wczytaj() throws IOException {
        klienci.clear();
        ostrzezenia.clear();

        if (!Files.exists(plikDanych)) {
            if (Files.exists(plikKopii)) {
                wczytajKopie();
                ostrzezenia.add("Brak pliku " + plikDanych + " – dane odtworzono z kopii " + plikKopii + ".");
            }
            return;
        }

        Set<Integer> uzyteId = new HashSet<>();
        try (BufferedReader czytnik = Files.newBufferedReader(plikDanych, StandardCharsets.UTF_8)) {
            String linia;
            int nr = 0;
            while ((linia = czytnik.readLine()) != null) {
                nr++;
                linia = linia.replace("﻿", "").trim();
                if (linia.isEmpty() || linia.startsWith("#")) {
                    continue;
                }
                try {
                    Klient k = Klient.zLinii(linia, nr);
                    if (!uzyteId.add(k.getId())) {
                        throw new BlednyFormatDanychException(nr, "zduplikowane ID " + k.getId());
                    }
                    klienci.add(k);
                } catch (BlednyFormatDanychException e) {
                    ostrzezenia.add(e.getMessage() + " – linia pominięta");
                }
            }
        }
        Collections.sort(klienci);
    }

    public void zapisz() throws IOException {
        if (plikDanych.getParent() != null) {
            Files.createDirectories(plikDanych.getParent());
        }
        try (BufferedWriter pisarz = Files.newBufferedWriter(plikDanych, StandardCharsets.UTF_8)) {
            pisarz.write("# Baza klientów AutoSerwis");
            pisarz.newLine();
            pisarz.write("# I;id;imię;nazwisko;telefon;email;stały(TAK/NIE)");
            pisarz.newLine();
            pisarz.write("# F;id;imię;nazwisko;telefon;email;NIP;nazwa firmy");
            pisarz.newLine();
            for (ZapisywalnyTekstowo k : klienci) {
                pisarz.write(k.doLinii());
                pisarz.newLine();
            }
        }
        zapiszKopie();
    }

    private void zapiszKopie() throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(plikKopii)))) {
            out.writeObject(new ArrayList<>(klienci));
        }
    }

    @SuppressWarnings("unchecked")
    private void wczytajKopie() throws IOException {
        try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(plikKopii)))) {
            klienci.addAll((List<Klient>) in.readObject());
            Collections.sort(klienci);
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new IOException("Uszkodzona kopia zapasowa: " + plikKopii, e);
        }
    }

    // ---------------------------------------------------------------- operacje na kolekcji

    public List<Klient> getKlienci() {
        return Collections.unmodifiableList(klienci);
    }

    public List<String> getOstrzezenia() {
        return Collections.unmodifiableList(ostrzezenia);
    }

    public int liczba() {
        return klienci.size();
    }

    public int nastepneId() {
        int max = 0;
        for (Klient k : klienci) {
            max = Math.max(max, k.getId());
        }
        return max + 1;
    }

    public List<Klient> szukaj(String fraza) {
        String f = fraza == null ? "" : fraza.trim().toLowerCase();
        if (f.isEmpty()) {
            return new ArrayList<>(klienci);
        }
        List<Klient> wynik = new ArrayList<>();
        for (Klient k : klienci) {
            String tekst = (k.getPelneImie() + " " + k.getTelefon() + " " + k.getEmail() + " "
                    + k.getSzczegoly() + " " + k.getTyp()).toLowerCase();
            if (tekst.contains(f)) {
                wynik.add(k);
            }
        }
        return wynik;
    }

    public void dodaj(Klient k) {
        klienci.add(k);
        Collections.sort(klienci);
    }

    public void zamien(Klient stary, Klient nowy) {
        int i = klienci.indexOf(stary);
        if (i < 0) {
            throw new NoSuchElementException("Nie znaleziono klienta " + stary);
        }
        klienci.set(i, nowy);
        Collections.sort(klienci);
    }

    public void usun(Klient k) {
        klienci.remove(k);
    }

    // ---------------------------------------------------------------- raport

    /** Zapisuje raport tekstowy do podanego katalogu i zwraca ścieżkę do pliku. */
    public Path generujRaport(Path katalog) throws IOException {
        Files.createDirectories(katalog);
        LocalDateTime teraz = LocalDateTime.now();
        Path plik = katalog.resolve("raport_klienci_" + teraz.format(FORMAT_PLIKU) + ".txt");
        Statystyki s = new Statystyki();
        String linia = "=".repeat(110);

        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(plik, StandardCharsets.UTF_8))) {
            out.println(linia);
            out.println("  AutoSerwis – RAPORT KLIENTÓW");
            out.println("  Wygenerowano: " + teraz.format(FORMAT_DATY));
            out.println(linia);
            out.println(String.format("%-4s %-13s %-24s %-13s %-28s %6s  %s",
                    "ID", "Typ", "Imię i nazwisko", "Telefon", "E-mail", "Rabat", "Szczegóły"));
            out.println("-".repeat(110));
            for (Raportowalny r : klienci) {
                out.println(r.wierszRaportu());
            }
            out.println("-".repeat(110));
            out.println("PODSUMOWANIE");
            out.println("  Liczba klientów:          " + klienci.size());
            out.println("  - indywidualnych:         " + s.indywidualni + " (w tym stałych: " + s.stali + ")");
            out.println("  - firmowych:              " + s.firmowi);
            out.println("  Klienci z adresem e-mail: " + s.zEmailem);
            out.println(String.format("  Średni rabat:             %.1f%%", s.sredniRabat() * 100));
            out.println(linia);
        }
        return plik;
    }

    /** Klasa wewnętrzna – liczy statystyki na podstawie kolekcji klientów obiektu zewnętrznego. */
    private class Statystyki {
        int indywidualni, stali, firmowi, zEmailem;
        double sumaRabatow;

        Statystyki() {
            for (Klient k : klienci) {
                if (k instanceof KlientFirmowy) {
                    firmowi++;
                } else if (k instanceof KlientIndywidualny ki) {
                    indywidualni++;
                    if (ki.isStaly()) {
                        stali++;
                    }
                }
                if (!k.getEmail().isEmpty()) {
                    zEmailem++;
                }
                sumaRabatow += k.getRabat();
            }
        }

        double sredniRabat() {
            return klienci.isEmpty() ? 0 : sumaRabatow / klienci.size();
        }
    }
}
