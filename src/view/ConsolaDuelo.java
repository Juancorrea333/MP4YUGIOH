package view;

import controller.Controlador;
import model.*;

import java.util.Scanner;

public class ConsolaDuelo implements VistaDuelo {

    private final Scanner sc = new Scanner(System.in);
    private Controlador controlador;
    private MotorJuego motor;

    public void setControlador(Controlador controlador) {
        this.controlador = controlador;
    }

    public void iniciar() {

        while (!motor.isJuegoTerminado()) {

            mostrarEstado();

            controlador.ejecutarTurno();
        }

        System.out.println("Juego terminado.");
    }
        }

    @Override
    public void actualizarVista(MotorJuego motor) {
        this.motor = motor;

        System.out.println("\n===== TURNO " + motor.getNumeroTurno() + " =====");
        System.out.println("Jugador activo: " + motor.getActivo().getNombre());
        System.out.println("LP: " + motor.getActivo().getLp());
        System.out.println("LP rival: " + motor.getOponente().getLp());

        System.out.println("\nMano:");
        for (int i = 0; i < motor.getActivo().getMano().size(); i++) {
            Carta c = motor.getActivo().getMano().get(i);
            System.out.println(i + ": " + c.getNombre());
        }

        System.out.println("\nCampo propio:");
        for (int i = 0; i < motor.getActivo().getCampo().size(); i++) {
            Monstruo m = motor.getActivo().getCampo().get(i);
            System.out.println(i + ": " + m.getNombre() + " " + m.getPosicion());
        }

        System.out.println("\nCampo rival:");
        for (int i = 0; i < motor.getOponente().getCampo().size(); i++) {
            Monstruo m = motor.getOponente().getCampo().get(i);
            System.out.println(i + ": " + m.getNombre() + " " + m.getPosicion());
        }

        System.out.println();
        for (String log : motor.getLog()) {
            System.out.println(log);
        }

    @Override
    public void mostrarError(String mensaje) {
        System.out.println("ERROR: " + mensaje);

    @Override
    public void mostrarAviso(String mensaje) {
        System.out.println("AVISO: " + mensaje);

    @Override
    public void mostrarCambioDeTurno(int turno, String jugador) {
        System.out.println("Turno " + turno + " - " + jugador);

    @Override
    public int getIndiceCartaSeleccionada() {
        System.out.print("Indice de carta: ");
        return sc.nextInt();

    @Override
    public int solicitarIndiceAtacante() {
        System.out.print("Indice atacante: ");
        return sc.nextInt();

    @Override
    public int solicitarIndiceDefensor() {
        System.out.print("Indice defensor (-1 directo): ");
        return sc.nextInt();

    @Override
    public int solicitarPosicionInvocacion() {
        System.out.println("0. Ataque");
        System.out.println("1. Defensa");
        return sc.nextInt();

    @Override
    public int solicitarSacrificio() {
        System.out.print("Indice sacrificio: ");
        return sc.nextInt();

    @Override
    public int solicitarSegundoSacrificio() {
        System.out.print("Indice segundo sacrificio: ");
        return sc.nextInt();

    @Override
    public void finalizar() {
        System.out.println("Ganador: " + motor.getGanador().getNombre());
    }
}
