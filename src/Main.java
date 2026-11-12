
import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame okno = new JFrame("Warsztat Samochodowy");

            okno.setSize(1000, 650);
            okno.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            okno.setLocationRelativeTo(null);
            okno.setLayout(new BorderLayout());

            JLabel naglowek = new JLabel(
                    "WARSZTAT SAMOCHODOWY",
                    SwingConstants.CENTER
            );

            naglowek.setFont(new Font("Arial", Font.BOLD, 26));
            naglowek.setForeground(Color.WHITE);
            naglowek.setBackground(new Color(35, 55, 80));
            naglowek.setOpaque(true);
            naglowek.setPreferredSize(new Dimension(1000, 90));

            JPanel menu = new JPanel();
            menu.setLayout(new GridLayout(6, 1, 10, 10));
            menu.setBackground(new Color(45, 65, 90));

            JButton klienci = new JButton("Klienci");
            JButton samochody = new JButton("Samochody");
            JButton naprawy = new JButton("Naprawy");
            JButton magazyn = new JButton("Magazyn");
            JButton historia = new JButton("Historia napraw");
            JButton raporty = new JButton("Raporty");

            menu.add(klienci);
            menu.add(samochody);
            menu.add(naprawy);
            menu.add(magazyn);
            menu.add(historia);
            menu.add(raporty);

            menu.setPreferredSize(new Dimension(220, 500));

            JPanel panelGlowny = new JPanel();
            panelGlowny.setBackground(new Color(240, 243, 247));

            JLabel powitanie = new JLabel(
                    "Witaj w systemie obslugi warsztatu!",
                    SwingConstants.CENTER
            );

            powitanie.setFont(new Font("Arial", Font.BOLD, 22));

            panelGlowny.setLayout(new BorderLayout());
            panelGlowny.add(powitanie, BorderLayout.CENTER);

            okno.add(naglowek, BorderLayout.NORTH);
            okno.add(menu, BorderLayout.WEST);
            okno.add(panelGlowny, BorderLayout.CENTER);

            okno.setVisible(true);
        });
    }
}
