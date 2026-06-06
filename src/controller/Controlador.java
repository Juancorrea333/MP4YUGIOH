package controller;

import model.*;
import view.*;

import java.util.Collections;
import java.util.List;

public class Controlador {

    private VentanaInicio ventanaInicio;
    private VistaDuelo vistaDuelo;
    private MotorJuego motor;

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

        Jugador j1 = new Jugador("Jugador1");
        Jugador j2 = new Jugador("Jugador2");

        inicializarJuego(j1, j2);

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

        vistaDuelo = new VentanaDuelo();
        ((VentanaDuelo) vistaDuelo).setControlador(this);
        vistaDuelo.actualizarVista(motor);
        ((VentanaDuelo) vistaDuelo).setVisible(true);

        ventanaInicio.dispose();
    }

    private void inicializarJuego(Jugador j1, Jugador j2) {
        List<Carta> mazoCompleto = FabricaCartas.crearMazoCompleto();
        Collections.shuffle(mazoCompleto);

        for (int i = 0; i < 25; i++) j1.agregarAlMazo(mazoCompleto.get(i));
        for (int i = 25; i < 50; i++) j2.agregarAlMazo(mazoCompleto.get(i));

        for (int i = 0; i < 5; i++) {
            j1.robarCarta();
            j2.robarCarta();
        }

        motor = new MotorJuego(j1, j2);
        motor.iniciarTurno();
    }

    public void accionJugarCarta() {
        int indiceCarta = vistaDuelo.getIndiceCartaSeleccionada();

        if (indiceCarta < 0) {
            vistaDuelo.mostrarAviso("Selecciona una carta de tu mano.");
            return;
        }
        if (motor.yaJugoUnaCarta()) {
            vistaDuelo.mostrarAviso("Ya jugaste una carta este turno.");
            return;
        }

        Carta carta = motor.getActivo().getMano().get(indiceCarta);

        if (carta.esMonstruo()) {
            accionJugarMonstruo(indiceCarta, carta.comoMonstruo());
        } else {
            String error = motor.jugarCarta(indiceCarta, Posicion.ATAQUE, -1);
            if (error != null) {
                vistaDuelo.mostrarError(error);
            }
        }

        vistaDuelo.actualizarVista(motor);
        verificarFin();
    }

    private void accionJugarMonstruo(int indiceCarta, Monstruo monstruo) {
        int indiceSacrificio = -1;
        int indiceSacrificio2 = -1;

        if (monstruo.getNivel() > 4) {
            int requeridos = monstruo.getNivel() >= 7 ? 2 : 1;
            List<Monstruo> campo = motor.getActivo().getCampo();

            if (campo.size() < requeridos) {
                vistaDuelo.mostrarAviso("Necesitas sacrificios.");
                return;
            }

            indiceSacrificio = vistaDuelo.solicitarSacrificio();

            if (requeridos == 2) {
                indiceSacrificio2 = vistaDuelo.solicitarSegundoSacrificio();
            }
        }

        int pos = vistaDuelo.solicitarPosicionInvocacion();
        if (pos < 0) return;

        Posicion posicion = pos == 0 ? Posicion.ATAQUE : Posicion.DEFENSA;

        String error = motor.jugarCarta(indiceCarta, posicion, indiceSacrificio, indiceSacrificio2);

        if (error != null) {
            vistaDuelo.mostrarError(error);
        }
    }

    public void accionAtacar() {
        if (motor.esPrimerTurno()) {
            vistaDuelo.mostrarAviso("No se puede atacar en el primer turno.");
            return;
        }

        int indiceAtacante = vistaDuelo.solicitarIndiceAtacante();
        int indiceDefensor = motor.getOponente().getCampo().isEmpty()
                ? -1
                : vistaDuelo.solicitarIndiceDefensor();

        String error = motor.atacar(indiceAtacante, indiceDefensor);

        if (error != null) {
            vistaDuelo.mostrarError(error);
        }

        vistaDuelo.actualizarVista(motor);
        verificarFin();
    }

    public void accionPasarTurno() {
        motor.pasarTurno();
        motor.iniciarTurno();
        vistaDuelo.actualizarVista(motor);

        if (!motor.isJuegoTerminado()) {
            vistaDuelo.mostrarCambioDeTurno(motor.getNumeroTurno(), motor.getActivo().getNombre());
        }
    }

    private void verificarFin() {
        if (motor.isJuegoTerminado()) {
            vistaDuelo.finalizar();
        }
    }
}
