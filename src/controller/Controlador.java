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