package patterns;

import model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * RF3 – Patrón Command: comandos concretos del juego.
 *
 * Cada acción del jugador es un objeto Command que encapsula:
 *  - Los parámetros necesarios para ejecutar la acción.
 *  - La lógica de undo (reversión del estado antes de ejecutar).
 *
 * GestorComandos (Invoker) ejecuta y deshace estos comandos.
 * MotorJuego (Receiver) realiza el trabajo real.
 */
public class Comandos {

    private Comandos() {}

    // =========================================================================
    // Comando: Jugar Carta
    // =========================================================================

    /**
     * Encapsula la acción de jugar una carta de la mano.
     * Undo: devuelve la carta a la mano y revierte el flag yaJugoCarta.
     * Nota: revertir efectos de magia/trampa puede ser complejo; aquí
     *       restituimos la estructura de campo/mano, no los LP (simplificación).
     */
    public static class ComandoJugarCarta implements Comando {

        private final MotorJuego motor;
        private final int        indiceCarta;
        private final Posicion   posicion;
        private final int        indiceSacrificio;
        private final int        indiceSacrificio2;

        // Estado previo para undo
        private Carta            cartaJugada;
        private List<Monstruo>   campoAntesJ1;
        private List<Monstruo>   campoAntesJ2;

        public ComandoJugarCarta(MotorJuego motor, int indiceCarta,
                                  Posicion posicion,
                                  int indiceSacrificio, int indiceSacrificio2) {
            this.motor             = motor;
            this.indiceCarta       = indiceCarta;
            this.posicion          = posicion;
            this.indiceSacrificio  = indiceSacrificio;
            this.indiceSacrificio2 = indiceSacrificio2;
        }

        @Override
        public String execute() {
            // Capturamos estado previo para undo
            cartaJugada  = motor.getActivo().getMano().get(indiceCarta);
            campoAntesJ1 = new ArrayList<>(motor.getJugador1().getCampo());
            campoAntesJ2 = new ArrayList<>(motor.getJugador2().getCampo());

            return motor.jugarCarta(indiceCarta, posicion, indiceSacrificio, indiceSacrificio2);
        }

        @Override
        public void undo() {
            // Restauramos la carta a la mano del activo
            Jugador activo = motor.getActivo();
            if (cartaJugada != null && !activo.getMano().contains(cartaJugada)) {
                activo.getMano().add(indiceCarta > activo.getMano().size()
                        ? activo.getMano().size() : indiceCarta, cartaJugada);
            }
            // Nota: este undo básico restaura la mano; para efectos complejos
            // se recomienda usar GestorMemento.restaurar() en su lugar.
            System.out.println("[UNDO] Carta " + (cartaJugada != null ? cartaJugada.getNombre() : "?")
                    + " devuelta a la mano.");
        }

        @Override
        public String getDescripcion() {
            return "Jugar carta [" + indiceCarta + "]"
                    + (cartaJugada != null ? " – " + cartaJugada.getNombre() : "");
        }
    }

    // =========================================================================
    // Comando: Atacar
    // =========================================================================

    /**
     * Encapsula la acción de atacar con un monstruo.
     * Undo: los cambios de LP y destrucciones son difíciles de revertir
     *       sin un Memento; este comando documenta la limitación.
     */
    public static class ComandoAtacar implements Comando {

        private final MotorJuego motor;
        private final int        indiceAtacante;
        private final int        indiceDefensor;

        // Snapshot de LP antes del ataque
        private int lpActivoAntes;
        private int lpOponenteAntes;

        public ComandoAtacar(MotorJuego motor, int indiceAtacante, int indiceDefensor) {
            this.motor          = motor;
            this.indiceAtacante = indiceAtacante;
            this.indiceDefensor = indiceDefensor;
        }

        @Override
        public String execute() {
            lpActivoAntes   = motor.getActivo().getLp();
            lpOponenteAntes = motor.getOponente().getLp();
            return motor.atacar(indiceAtacante, indiceDefensor);
        }

        @Override
        public void undo() {
            // Reversión parcial de LP (sin restaurar monstruos destruidos)
            int danioActivo   = lpActivoAntes   - motor.getActivo().getLp();
            int danioOponente = lpOponenteAntes  - motor.getOponente().getLp();
            if (danioActivo   > 0) motor.getActivo().recuperarLp(danioActivo);
            if (danioOponente > 0) motor.getOponente().recuperarLp(danioOponente);
            System.out.println("[UNDO] LP revertidos. Para un undo completo use GestorMemento.");
        }

        @Override
        public String getDescripcion() {
            return "Atacar: atacante[" + indiceAtacante + "] → defensor[" + indiceDefensor + "]";
        }
    }

    // =========================================================================
    // Comando: Pasar Turno
    // =========================================================================

    /**
     * Encapsula pasar el turno.
     * Undo: revertir un turno completo es muy complejo; aquí sólo documentamos.
     * Para un undo real de turno, usar GestorMemento con snapshot previo al turno.
     */
    public static class ComandoPasarTurno implements Comando {

        private final MotorJuego motor;
        private int numeroTurnoAntes;

        public ComandoPasarTurno(MotorJuego motor) {
            this.motor = motor;
        }

        @Override
        public String execute() {
            numeroTurnoAntes = motor.getNumeroTurno();
            motor.pasarTurno();
            motor.iniciarTurno();
            return null; // pasar turno siempre es válido
        }

        @Override
        public void undo() {
            // Pasar turno no se deshace fácilmente en tiempo de ejecución;
            // el jugador debe usar GestorMemento para restaurar el snapshot
            // guardado antes de pasar el turno.
            System.out.println("[UNDO] No se puede deshacer 'Pasar Turno' sin un Memento previo.");
        }

        @Override
        public String getDescripcion() {
            return "Pasar turno (era turno " + numeroTurnoAntes + ")";
        }
    }

    // =========================================================================
    // Comando: Activar Trampa
    // =========================================================================

    public static class ComandoActivarTrampa implements Comando {

        private final MotorJuego motor;
        private final Trampa     trampa;

        public ComandoActivarTrampa(MotorJuego motor, Trampa trampa) {
            this.motor  = motor;
            this.trampa = trampa;
        }

        @Override
        public String execute() {
            motor.activarTrampa(trampa);
            return null;
        }

        @Override
        public void undo() {
            // Re-colocar la trampa en la zona de trampas del oponente
            motor.getOponente().colocarTrampa(trampa);
            System.out.println("[UNDO] Trampa " + trampa.getNombre() + " devuelta al campo.");
        }

        @Override
        public String getDescripcion() {
            return "Activar trampa: " + trampa.getNombre();
        }
    }
}
