package model;

import java.util.*;
import patterns.ObservadorJuego;
import patterns.SujetoJuego;
import patterns.MementoJuego;


public class MotorJuego implements SujetoJuego {

  
    private final List<ObservadorJuego> observadores = new ArrayList<>();

    private Jugador jugador1;
    private Jugador jugador2;
    private Jugador activo;
    private Jugador oponente;
    private int     numeroTurno;
    private boolean juegoTerminado;
    private Jugador ganador;

    private boolean yaJugoUnaCarta;
    private boolean yaAtaco;

   
    private Queue<String>    colaEventos  = new LinkedList<>();

   
    private List<String>     log          = new ArrayList<>();

   
    private TreeMap<Integer, Monstruo> campoOrdenadoPorAtk = new TreeMap<>();

    private Jugador primerJugador;

    public MotorJuego(Jugador j1, Jugador j2) {
        this.jugador1       = j1;
        this.jugador2       = j2;
        this.numeroTurno    = 1;
        this.juegoTerminado = false;
        this.ganador        = null;
        this.yaJugoUnaCarta = false;
        this.yaAtaco        = false;

        if (Math.random() > 0.5) { this.activo = j1; this.oponente = j2; }
        else                     { this.activo = j2; this.oponente = j1; }
        this.primerJugador = this.activo;
    }