import controller.Controlador;
import view.ConsolaDuelo;
import view.VentanaInicio;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        boolean usarGUI = true;

        if (args.length > 0 && args[0].equalsIgnoreCase("consola")) {
            usarGUI = false;
        }

        if (usarGUI) {
            SwingUtilities.invokeLater(() -> {
                VentanaInicio ventanaInicio = new VentanaInicio();
                new Controlador(ventanaInicio);
                ventanaInicio.setVisible(true);
            });
        } else {
            new Controlador(new ConsolaDuelo());
        }
    }
}
