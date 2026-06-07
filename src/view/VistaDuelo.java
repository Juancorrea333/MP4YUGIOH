package view;

import model.MotorJuego;

public interface VistaDuelo {
    void actualizarVista(MotorJuego motor);
    void mostrarError(String mensaje);
    void mostrarAviso(String mensaje);
    void mostrarCambioDeTurno(int turno, String jugador);
    int getIndiceCartaSeleccionada();
    int solicitarIndiceAtacante();
    int solicitarIndiceDefensor();
    int solicitarPosicionInvocacion();
    int solicitarSacrificio();
    int solicitarSegundoSacrificio();
    void finalizar();
}
