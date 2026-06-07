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
