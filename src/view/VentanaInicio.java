package view;

import controller.Controlador;

import javax.swing.*;
import java.awt.*;

public class VentanaInicio extends JFrame {

    private JTextField  txtJugador1;
    private JTextField  txtJugador2;
    private JButton     btnIniciar;
    private JButton     btnCargar;
    private JButton     btnEstadisticas;

    private Controlador controlador;

    public VentanaInicio() {
        setTitle("Yu-Gi-Oh! — Inicio");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 280);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponentes();
    }

    public void setControlador(Controlador controlador) {
        this.controlador = controlador;
    }

    public String getNombreJugador1() { return txtJugador1.getText().trim(); }
    public String getNombreJugador2() { return txtJugador2.getText().trim(); }

    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Nombres inválidos", JOptionPane.WARNING_MESSAGE);
    }

    private void initComponentes() {
        JLabel lblTitulo = new JLabel("★  YU-GI-OH!  ★", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JPanel panelCampos = new JPanel(new GridLayout(2, 2, 10, 10));
        panelCampos.setBorder(BorderFactory.createEmptyBorder(5, 30, 5, 30));
        panelCampos.add(new JLabel("Nombre Duelista 1:"));
        txtJugador1 = new JTextField();
        panelCampos.add(txtJugador1);
        panelCampos.add(new JLabel("Nombre Duelista 2:"));
        txtJugador2 = new JTextField();
        panelCampos.add(txtJugador2);

        btnIniciar       = new JButton("¡INICIAR DUELO!");
        btnCargar        = new JButton("Cargar Partida");
        btnEstadisticas  = new JButton("Ver Estadísticas");

        JPanel panelBotones = new JPanel(new GridLayout(1, 3, 8, 0));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(8, 30, 15, 30));
        panelBotones.add(btnIniciar);
        panelBotones.add(btnCargar);
        panelBotones.add(btnEstadisticas);

        setLayout(new BorderLayout(5, 5));
        add(lblTitulo,    BorderLayout.NORTH);
        add(panelCampos,  BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);

        btnIniciar.addActionListener(e -> { if (controlador != null) controlador.accionIniciarDuelo(); });
        btnCargar.addActionListener(e  -> { if (controlador != null) controlador.accionCargarPartida(this); });
        btnEstadisticas.addActionListener(e -> { if (controlador != null) controlador.accionMostrarEstadisticas(this); });
    }
}
