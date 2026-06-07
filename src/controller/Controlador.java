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
