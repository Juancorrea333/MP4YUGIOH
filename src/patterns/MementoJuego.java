package patterns;

import model.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * RF3 – Patrón Memento.
 *
 * Captura una "fotografía" del estado completo del juego en un momento dado.
 * El Memento es opaco: sólo el Caretaker (GestorMemento) lo almacena;
 * el Originator (MotorJuego) sabe cómo crearlo y restaurarlo.
 *
 * Encaja perfectamente con el guardado de partidas del RF2:
 *  - guardarPartida() puede serializar el Memento a disco.
 *  - cargarPartida() puede reconstruir el MotorJuego a partir de un Memento.
 *
 * Aquí se implementa la versión en-memoria (instantánea sin I/O) para
 * el DESHACER en tiempo de ejecución; el RF2 (GestorPersistencia) se
 * encarga de la persistencia real en archivo .ygo.
 */
public class MementoJuego {

    // ---- Estado del turno ----
    private final int     numeroTurno;
    private final String  nombreActivo;
    private final String  nombrePrimerJugador;
    private final boolean yaJugoCarta;
    private final boolean yaAtaco;

    // ---- Instantáneas de los jugadores ----
    private final InstantaneaJugador snapJ1;
    private final InstantaneaJugador snapJ2;

    /** Constructor: solo MotorJuego (Originator) debe crear Mementos. */
    public MementoJuego(int numeroTurno, String nombreActivo, String nombrePrimerJugador,
                 boolean yaJugoCarta, boolean yaAtaco,
                 InstantaneaJugador snapJ1, InstantaneaJugador snapJ2) {
        this.numeroTurno          = numeroTurno;
        this.nombreActivo         = nombreActivo;
        this.nombrePrimerJugador  = nombrePrimerJugador;
        this.yaJugoCarta          = yaJugoCarta;
        this.yaAtaco              = yaAtaco;
        this.snapJ1               = snapJ1;
        this.snapJ2               = snapJ2;
    }

    // ---- Getters publicos ----
    public int                  getNumeroTurno()         { return numeroTurno; }
    public String               getNombreActivo()        { return nombreActivo; }
    public String               getNombrePrimerJugador() { return nombrePrimerJugador; }
    public boolean              isYaJugoCarta()          { return yaJugoCarta; }
    public boolean              isYaAtaco()              { return yaAtaco; }
    public InstantaneaJugador   getSnapJ1()              { return snapJ1; }
    public InstantaneaJugador   getSnapJ2()              { return snapJ2; }

    // =========================================================================
    // Clase interna: instantánea de un jugador
    // =========================================================================

    /**
     * Captura todos los datos variables de un Jugador en un momento dado.
     * Almacena los nombres de las cartas (no referencias vivas) para evitar
     * aliasing: si el objeto original muta, la foto no cambia.
     */
    public static class InstantaneaJugador {
        public final String        nombre;
        public final int           lp;
        public final List<String>  mazoNombres;   // tope primero
        public final List<String>  manoNombres;
        public final List<String>  trampaNames;
        /** Campo guardado con stats completos: nombre:atk:def:nivel:pos:puedeAtacar */
        public final List<String>  campoStats;

        public InstantaneaJugador(Jugador j) {
            this.nombre = j.getNombre();
            this.lp     = j.getLp();

            // Mazo (Stack): copiamos de tope a fondo
            mazoNombres = new ArrayList<>();
            Stack<Carta> mazo = j.getMazo();
            // Iteramos la pila sin destruirla (iterator de Stack va de fondo a tope)
            // Necesitamos tope primero → invertimos
            List<String> temp = new ArrayList<>();
            for (Carta c : mazo) temp.add(c.getNombre());
            for (int i = temp.size() - 1; i >= 0; i--) mazoNombres.add(temp.get(i));

            // Mano (LinkedList)
            manoNombres = new ArrayList<>();
            for (Carta c : j.getMano()) manoNombres.add(c.getNombre());

            // Trampas
            trampaNames = new ArrayList<>();
            for (Trampa t : j.getTrampas()) trampaNames.add(t.getNombre());

            // Campo con stats
            campoStats = new ArrayList<>();
            for (Monstruo m : j.getCampo()) {
                campoStats.add(m.getNombre() + ":" + m.getAtk() + ":" + m.getDef()
                        + ":" + m.getNivel() + ":" + m.getPosicion() + ":" + m.puedeAtacar());
            }
        }
    }
}
