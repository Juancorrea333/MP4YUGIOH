package controller;

import model.*;
import persistencia.GestorPersistencia;
import patterns.*;
import view.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Controlador {

    private VentanaInicio ventanaInicio;
    private VistaDuelo    vistaDuelo;
    private MotorJuego    motor;

    private final GestorComandos gestorComandos = new GestorComandos();
    private final GestorMemento  gestorMemento  = new GestorMemento();


    public Controlador(VentanaInicio ventanaInicio) {
        this.ventanaInicio = ventanaInicio;
        this.ventanaInicio.setControlador(this);
    }

    public Controlador(VistaDuelo vistaDuelo) {
        this.vistaDuelo = vistaDuelo;
        if (vistaDuelo instanceof ConsolaDuelo consola) {
            iniciarJuegoConsola(consola);
        } else if (vistaDuelo instanceof VentanaDuelo ventana) {
            ventana.setControlador(this);
        }
    }

    private void iniciarJuegoConsola(ConsolaDuelo consola) {
        consola.setControlador(this);
        Scanner sc = new Scanner(System.in);

        System.out.println("=== YU-GI-OH! CONSOLA ===");
        System.out.println("1. Nuevo duelo");
        System.out.println("2. Cargar partida");
        System.out.print("Opción: ");
        String op = sc.nextLine().trim();

        if (op.equals("2")) {
            List<String> partidas = GestorPersistencia.listarPartidas();
            if (partidas.isEmpty()) {
                System.out.println("No hay partidas guardadas.");
            } else {
                System.out.println("Partidas disponibles:");
                for (int i = 0; i < partidas.size(); i++) System.out.println(i + ": " + partidas.get(i));
                System.out.print("Nombre de la partida: ");
                String nombre = sc.nextLine().trim();
                try {
                    motor = GestorPersistencia.cargarPartida(nombre);
                    System.out.println("✔ Partida cargada: " + nombre);
                    vistaDuelo = consola;
                    consola.actualizarVista(motor);
                    consola.iniciar();
                    return;
                } catch (IOException e) {
                    System.out.println("Error al cargar: " + e.getMessage());
                }
            }
        }

        System.out.print("Nombre Jugador 1: ");
        String n1 = sc.nextLine().trim();
        System.out.print("Nombre Jugador 2: ");
        String n2 = sc.nextLine().trim();

        inicializarJuego(new Jugador(n1.isEmpty() ? "Jugador1" : n1),
                         new Jugador(n2.isEmpty() ? "Jugador2" : n2));
        vistaDuelo = consola;
        consola.actualizarVista(motor);
        consola.iniciar();
    }

    public void accionIniciarDuelo() {
        String nombre1 = ventanaInicio.getNombreJugador1();
        String nombre2 = ventanaInicio.getNombreJugador2();

        if (nombre1.isEmpty() || nombre2.isEmpty()) {
            ventanaInicio.mostrarError("Por favor ingresa los nombres de ambos duelistas.");
            return;
        }
        if (nombre1.equals(nombre2)) {
            ventanaInicio.mostrarError("Los nombres deben ser diferentes.");
            return;
        }

        inicializarJuego(new Jugador(nombre1), new Jugador(nombre2));

        VentanaDuelo ventana = new VentanaDuelo();
        ventana.setControlador(this);
        vistaDuelo = ventana;
        vistaDuelo.actualizarVista(motor);
        ventana.setVisible(true);
        ventanaInicio.dispose();
    }
        private void inicializarJuego(Jugador j1, Jugador j2) {
        FabricaCartasReflection.crearArchivoEjemplo(); 
        try {
            java.util.List<Carta> cartasDinamicas = FabricaCartasReflection.cargarDesdeArchivo();
            System.out.println("[RF3] " + cartasDinamicas.size() + " cartas cargadas por Reflection.");
        } catch (java.io.IOException e) {
            System.out.println("[RF3] Aviso: no se pudo cargar cartas dinámicas: " + e.getMessage());
        }

        List<Carta> mazoCompleto = FabricaCartas.crearMazoCompleto();
        Collections.shuffle(mazoCompleto);

        for (int i = 0;  i < 25; i++) j1.agregarAlMazo(mazoCompleto.get(i));
        for (int i = 25; i < 50; i++) j2.agregarAlMazo(mazoCompleto.get(i));

        for (int i = 0; i < 5; i++) { j1.robarCarta(); j2.robarCarta(); }

        motor = new MotorJuego(j1, j2);
        if (vistaDuelo instanceof ObservadorJuego obs) motor.agregarObservador(obs);
        motor.iniciarTurno();

        gestorComandos.reiniciar();
        gestorMemento.limpiar();
    }

    public void accionJugarCarta() {
        int indiceCarta = vistaDuelo.getIndiceCartaSeleccionada();
        if (indiceCarta < 0) { vistaDuelo.mostrarAviso("Selecciona una carta de tu mano."); return; }
        if (motor.yaJugoUnaCarta()) { vistaDuelo.mostrarAviso("Ya jugaste una carta este turno."); return; }

        Carta carta = motor.getActivo().getMano().get(indiceCarta);

        gestorMemento.guardar(motor.crearMemento());

        if (carta.esMonstruo()) {
            accionJugarMonstruoCmd(indiceCarta, carta.comoMonstruo());
        } else {
            Comando cmd = new Comandos.ComandoJugarCarta(motor, indiceCarta, Posicion.ATAQUE, -1, -1);
            String error = gestorComandos.ejecutar(cmd);
            if (error != null) { vistaDuelo.mostrarError(error); gestorMemento.restaurar(); }
        }

        vistaDuelo.actualizarVista(motor);
        verificarFin();
    }

    private void accionJugarMonstruoCmd(int indiceCarta, Monstruo monstruo) {
        int indiceSacrificio  = -1;
        int indiceSacrificio2 = -1;
        if (monstruo.getNivel() > 4) {
            int requeridos = monstruo.getNivel() >= 7 ? 2 : 1;
            if (motor.getActivo().getCampo().size() < requeridos) {
                vistaDuelo.mostrarAviso("Necesitas " + requeridos + " sacrificio(s)."); return;
            }
            indiceSacrificio = vistaDuelo.solicitarSacrificio();
            if (requeridos == 2) indiceSacrificio2 = vistaDuelo.solicitarSegundoSacrificio();
        }
        int pos = vistaDuelo.solicitarPosicionInvocacion();
        if (pos < 0) return;
        Posicion posicion = pos == 0 ? Posicion.ATAQUE : Posicion.DEFENSA;
        Comando cmd = new Comandos.ComandoJugarCarta(motor, indiceCarta, posicion, indiceSacrificio, indiceSacrificio2);
        String error = gestorComandos.ejecutar(cmd);
        if (error != null) vistaDuelo.mostrarError(error);
    }

    private void accionJugarMonstruo(int indiceCarta, Monstruo monstruo) {
        int indiceSacrificio  = -1;
        int indiceSacrificio2 = -1;

        if (monstruo.getNivel() > 4) {
            int requeridos = monstruo.getNivel() >= 7 ? 2 : 1;
            if (motor.getActivo().getCampo().size() < requeridos) {
                vistaDuelo.mostrarAviso("Necesitas " + requeridos + " sacrificio(s).");
                return;
            }
            indiceSacrificio = vistaDuelo.solicitarSacrificio();
            if (requeridos == 2) indiceSacrificio2 = vistaDuelo.solicitarSegundoSacrificio();
        }

        int pos = vistaDuelo.solicitarPosicionInvocacion();
        if (pos < 0) return;

        Posicion posicion = pos == 0 ? Posicion.ATAQUE : Posicion.DEFENSA;
        String   error    = motor.jugarCarta(indiceCarta, posicion, indiceSacrificio, indiceSacrificio2);
        if (error != null) vistaDuelo.mostrarError(error);
    }

    public void accionAtacar() {
        if (motor.esPrimerTurno()) { vistaDuelo.mostrarAviso("No se puede atacar en el primer turno."); return; }
        if (motor.yaAtaco())       { vistaDuelo.mostrarAviso("Ya atacaste este turno."); return; }

        int indiceAtacante = vistaDuelo.solicitarIndiceAtacante();
        int indiceDefensor = motor.getOponente().getCampo().isEmpty() ? -1 : vistaDuelo.solicitarIndiceDefensor();

        gestorMemento.guardar(motor.crearMemento());
        Comando cmd = new Comandos.ComandoAtacar(motor, indiceAtacante, indiceDefensor);
        String error = gestorComandos.ejecutar(cmd);
        if (error != null) { vistaDuelo.mostrarError(error); gestorMemento.restaurar(); }

        vistaDuelo.actualizarVista(motor);
        verificarFin();
    }

    public void accionPasarTurno() {
        gestorMemento.guardar(motor.crearMemento());
        Comando cmd = new Comandos.ComandoPasarTurno(motor);
        gestorComandos.ejecutar(cmd);
        vistaDuelo.actualizarVista(motor);
        if (!motor.isJuegoTerminado()) {
            vistaDuelo.mostrarCambioDeTurno(motor.getNumeroTurno(), motor.getActivo().getNombre());
        }
    }
       public void accionDeshacer() {
        if (!gestorMemento.haySnapshot()) {
            vistaDuelo.mostrarAviso("No hay acciones para deshacer.");
            return;
        }
        MementoJuego memento = gestorMemento.restaurar();
        motor.restaurarMemento(memento);
        vistaDuelo.actualizarVista(motor);
        vistaDuelo.mostrarAviso("Acción deshecha. Turno " + motor.getNumeroTurno()
                + " – " + motor.getActivo().getNombre());
    }

    public java.util.List<String> getHistorialComandos() {
        return gestorComandos.getHistorial();
    }

    public void ejecutarTurnoConsola() {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n--- Acciones disponibles ---");
        System.out.println("1. Jugar carta");
        System.out.println("2. Atacar");
        System.out.println("3. Pasar turno");
        System.out.println("4. Guardar partida");
        System.out.print("Acción: ");
        String op = sc.nextLine().trim();

        switch (op) {
            case "1" -> accionJugarCarta();
            case "2" -> accionAtacar();
            case "3" -> accionPasarTurno();
            case "4" -> {
                System.out.print("Nombre para guardar: ");
                String nombre = sc.nextLine().trim();
                try {
                    GestorPersistencia.guardarPartida(motor, nombre.isEmpty() ? "partida_auto" : nombre);
                    System.out.println("✔ Partida guardada.");
                } catch (IOException e) {
                    System.out.println("Error al guardar: " + e.getMessage());
                }
            }
            default -> System.out.println("Opción no reconocida.");
        }
    }

    public void accionGuardarPartida(JFrame parent) {
        String nombre = JOptionPane.showInputDialog(parent,
                "Nombre para la partida guardada:", "Guardar Partida", JOptionPane.PLAIN_MESSAGE);
        if (nombre == null || nombre.isBlank()) return;
        try {
            GestorPersistencia.guardarPartida(motor, nombre.trim());
            JOptionPane.showMessageDialog(parent,
                    "Partida guardada como \"" + nombre.trim() + ".ygo\"",
                    "Guardado", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent,
                    "Error al guardar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void accionCargarPartida(JFrame parent) {
        List<String> partidas = GestorPersistencia.listarPartidas();
        if (partidas.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "No hay partidas guardadas.", "Sin partidas", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] opciones = partidas.toArray(new String[0]);
        String seleccion = (String) JOptionPane.showInputDialog(parent,
                "Selecciona una partida:", "Cargar Partida",
                JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);
        if (seleccion == null) return;

        try {
            motor = GestorPersistencia.cargarPartida(seleccion);
            VentanaDuelo ventana = new VentanaDuelo();
            ventana.setControlador(this);
            vistaDuelo = ventana;
            vistaDuelo.actualizarVista(motor);
            ventana.setVisible(true);
            if (parent instanceof VentanaInicio vi) vi.dispose();
            JOptionPane.showMessageDialog(ventana,
                    "Partida \"" + seleccion + "\" cargada correctamente.",
                    "Partida cargada", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent,
                    "Error al cargar la partida: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void accionMostrarEstadisticas(JFrame parent) {
        String stats = GestorPersistencia.leerEstadisticas();
        JTextArea ta = new JTextArea(stats);
        ta.setEditable(false);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(ta);
        scroll.setPreferredSize(new Dimension(430, 280));
        JOptionPane.showMessageDialog(parent, scroll, "Estadísticas históricas", JOptionPane.PLAIN_MESSAGE);
    }


    private void verificarFin() {
        if (motor.isJuegoTerminado()) {
            vistaDuelo.finalizar();
            GestorPersistencia.registrarResultado(motor);

            if (vistaDuelo instanceof VentanaDuelo ventana) {
                String ganador = motor.getGanador() != null ? motor.getGanador().getNombre() : "Nadie";
                SwingUtilities.invokeLater(() -> {
                    new VentanaFin(ganador).setVisible(true);
                    ventana.dispose();
                });
            }
        }
    }
}