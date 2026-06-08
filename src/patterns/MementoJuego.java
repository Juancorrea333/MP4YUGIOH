package patterns;

import model.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;


public class MementoJuego {


    private final int     numeroTurno;
    private final String  nombreActivo;
    private final String  nombrePrimerJugador;
    private final boolean yaJugoCarta;
    private final boolean yaAtaco;


    private final InstantaneaJugador snapJ1;
    private final InstantaneaJugador snapJ2;


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


    public int                  getNumeroTurno()         { return numeroTurno; }
    public String               getNombreActivo()        { return nombreActivo; }
    public String               getNombrePrimerJugador() { return nombrePrimerJugador; }
    public boolean              isYaJugoCarta()          { return yaJugoCarta; }
    public boolean              isYaAtaco()              { return yaAtaco; }
    public InstantaneaJugador   getSnapJ1()              { return snapJ1; }
    public InstantaneaJugador   getSnapJ2()              { return snapJ2; }



    public static class InstantaneaJugador {
        public final String        nombre;
        public final int           lp;
        public final List<String>  mazoNombres;   
        public final List<String>  manoNombres;
        public final List<String>  trampaNames;

        public final List<String>  campoStats;

        public InstantaneaJugador(Jugador j) {
            this.nombre = j.getNombre();
            this.lp     = j.getLp();


            mazoNombres = new ArrayList<>();
            Stack<Carta> mazo = j.getMazo();

            List<String> temp = new ArrayList<>();
            for (Carta c : mazo) temp.add(c.getNombre());
            for (int i = temp.size() - 1; i >= 0; i--) mazoNombres.add(temp.get(i));


            manoNombres = new ArrayList<>();
            for (Carta c : j.getMano()) manoNombres.add(c.getNombre());


            trampaNames = new ArrayList<>();
            for (Trampa t : j.getTrampas()) trampaNames.add(t.getNombre());

            campoStats = new ArrayList<>();
            for (Monstruo m : j.getCampo()) {
                campoStats.add(m.getNombre() + ":" + m.getAtk() + ":" + m.getDef()
                        + ":" + m.getNivel() + ":" + m.getPosicion() + ":" + m.puedeAtacar());
            }
        }
    }
}
