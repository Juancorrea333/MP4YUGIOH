package patterns;

import model.*;

import java.util.ArrayList;
import java.util.List;


public class Comandos {

    private Comandos() {}

    public static class ComandoJugarCarta implements Comando {

        private final MotorJuego motor;
        private final int        indiceCarta;
        private final Posicion   posicion;
        private final int        indiceSacrificio;
        private final int        indiceSacrificio2;


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
            
            cartaJugada  = motor.getActivo().getMano().get(indiceCarta);
            campoAntesJ1 = new ArrayList<>(motor.getJugador1().getCampo());
            campoAntesJ2 = new ArrayList<>(motor.getJugador2().getCampo());

            return motor.jugarCarta(indiceCarta, posicion, indiceSacrificio, indiceSacrificio2);
        }

        @Override
        public void undo() {
            
            Jugador activo = motor.getActivo();
            if (cartaJugada != null && !activo.getMano().contains(cartaJugada)) {
                activo.getMano().add(indiceCarta > activo.getMano().size()
                        ? activo.getMano().size() : indiceCarta, cartaJugada);
            }

            System.out.println("[UNDO] Carta " + (cartaJugada != null ? cartaJugada.getNombre() : "?")
                    + " devuelta a la mano.");
        }

        @Override
        public String getDescripcion() {
            return "Jugar carta [" + indiceCarta + "]"
                    + (cartaJugada != null ? " – " + cartaJugada.getNombre() : "");
        }
    }



    public static class ComandoAtacar implements Comando {

        private final MotorJuego motor;
        private final int        indiceAtacante;
        private final int        indiceDefensor;


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

            System.out.println("[UNDO] No se puede deshacer 'Pasar Turno' sin un Memento previo.");
        }

        @Override
        public String getDescripcion() {
            return "Pasar turno (era turno " + numeroTurnoAntes + ")";
        }
    }



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

            motor.getOponente().colocarTrampa(trampa);
            System.out.println("[UNDO] Trampa " + trampa.getNombre() + " devuelta al campo.");
        }

        @Override
        public String getDescripcion() {
            return "Activar trampa: " + trampa.getNombre();
        }
    }
}
