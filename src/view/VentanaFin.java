package view;

import javax.swing.*;
import java.awt.*;

public class VentanaFin extends JFrame {

    public VentanaFin(String nombreGanador) {
        setTitle("¡Duelo Terminado!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 220);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponentes(nombreGanador);
    }

    private void initComponentes(String nombreGanador) {
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel lblGanador = new JLabel("¡" + nombreGanador + " GANA EL DUELO!", SwingConstants.CENTER);
        lblGanador.setFont(new Font("Arial", Font.BOLD, 16));

        JLabel lblFrase = new JLabel("\"Confia en el corazon de las cartas\"", SwingConstants.CENTER);
        lblFrase.setFont(new Font("Arial", Font.ITALIC, 12));

        JLabel lblAutor = new JLabel("— Yugi Muto", SwingConstants.CENTER);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> System.exit(0));

        panel.add(lblGanador);
        panel.add(lblFrase);
        panel.add(lblAutor);
        panel.add(btnCerrar);

        add(panel);
    }
}
