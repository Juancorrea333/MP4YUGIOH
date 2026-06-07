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

    
    public MotorJuego(Jugador j1, Jugador j2, Jugador activo, Jugador oponente,
                      int numeroTurno, Jugador primerJugador,
                      boolean yaJugoUnaCarta, boolean yaAtaco) {
        this.jugador1       = j1;
        this.jugador2       = j2;
        this.activo         = activo;
        this.oponente       = oponente;
        this.numeroTurno    = numeroTurno;
        this.primerJugador  = primerJugador;
        this.juegoTerminado = false;
        this.ganador        = null;
        this.yaJugoUnaCarta = yaJugoUnaCarta;
        this.yaAtaco        = yaAtaco;
    }

    
    
    public Jugador  getJugador1()      { return jugador1; }
    public Jugador  getJugador2()      { return jugador2; }
    public Jugador  getActivo()        { return activo; }
    public Jugador  getOponente()      { return oponente; }
    public int      getNumeroTurno()   { return numeroTurno; }
    public boolean  isJuegoTerminado() { return juegoTerminado; }
    public Jugador  getGanador()       { return ganador; }
    public boolean  yaJugoUnaCarta()   { return yaJugoUnaCarta; }
    public boolean  yaAtaco()          { return yaAtaco; }
    public List<String> getLog()       { return log; }
    public Jugador  getPrimerJugador() { return primerJugador; }

   
    @Override
    public void agregarObservador(ObservadorJuego obs) {
        if (obs != null && !observadores.contains(obs)) observadores.add(obs);
    }

    @Override
    public void eliminarObservador(ObservadorJuego obs) {
        observadores.remove(obs);
    }

    @Override
    public void notificarObservadores() {
        for (ObservadorJuego obs : observadores) {
            obs.actualizar(this);
        }
    }

    

    
    public MementoJuego crearMemento() {
        return new MementoJuego(
            numeroTurno,
            activo.getNombre(),
            primerJugador.getNombre(),
            yaJugoUnaCarta,
            yaAtaco,
            new MementoJuego.InstantaneaJugador(jugador1),
            new MementoJuego.InstantaneaJugador(jugador2)
        );
    }

    
    public void restaurarMemento(MementoJuego memento) {
        if (memento == null) return;
        this.numeroTurno    = memento.getNumeroTurno();
        this.yaJugoUnaCarta = memento.isYaJugoCarta();
        this.yaAtaco        = memento.isYaAtaco();
        restaurarJugadorDesdeSnap(jugador1, memento.getSnapJ1());
        restaurarJugadorDesdeSnap(jugador2, memento.getSnapJ2());
        this.activo        = memento.getNombreActivo().equals(jugador1.getNombre()) ? jugador1 : jugador2;
        this.oponente      = (activo == jugador1) ? jugador2 : jugador1;
        this.primerJugador = memento.getNombrePrimerJugador().equals(jugador1.getNombre()) ? jugador1 : jugador2;
        this.juegoTerminado = false;
        this.ganador        = null;
        notificarObservadores();
        System.out.println("[Memento] Estado restaurado al turno " + numeroTurno + " – turno de " + activo.getNombre());
    }

    private void restaurarJugadorDesdeSnap(Jugador j, MementoJuego.InstantaneaJugador snap) {
        int lpActual = j.getLp();
        if (snap.lp < lpActual)      j.recibirDanio(lpActual - snap.lp);
        else if (snap.lp > lpActual) j.recuperarLp(snap.lp - lpActual);
        j.getMano().clear();
        for (String nombre : snap.manoNombres) {
            Carta c = FabricaCartas.porNombre(nombre);
            if (c != null) j.getMano().add(c);
        }
        j.limpiarCampo();
        for (String entry : snap.campoStats) {
            String[] p = entry.split(":");
            if (p.length < 6) continue;
            Monstruo m = new Monstruo(p[0], Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]));
            m.setPosicion(Posicion.valueOf(p[4]));
            if (Boolean.parseBoolean(p[5])) m.habilitarAtaque();
            j.invocarMonstruo(m);
        }
        j.getTrampas().clear();
        for (String nombre : snap.trampaNames) {
            Carta c = FabricaCartas.porNombre(nombre);
            if (c instanceof Trampa) j.colocarTrampa((Trampa) c);
        }
        j.getMazo().clear();
        for (int i = snap.mazoNombres.size() - 1; i >= 0; i--) {
            Carta c = FabricaCartas.porNombre(snap.mazoNombres.get(i));
            if (c != null) j.agregarAlMazo(c);
        }
    }

    public boolean esPrimerTurno() {
        return numeroTurno <= 2 && (
            (activo == primerJugador && numeroTurno == 1) ||
            (activo != primerJugador && numeroTurno == 2)
        );
    }

    public String getUltimoMensaje() {
        if (log.isEmpty()) return "";
        return log.get(log.size() - 1);
    }

    
    private void log(String mensaje) {
        colaEventos.offer(mensaje);  
        log.add(mensaje);            
    }

 
     
    public List<String> drenaEventosTurno() {
        List<String> eventos = new ArrayList<>();
        while (!colaEventos.isEmpty()) {
            eventos.add(colaEventos.poll());  

    public void iniciarTurno() {
        yaJugoUnaCarta = false;
        yaAtaco        = false;
        log("TURNO " + numeroTurno + " — " + activo.getNombre());

        if (!activo.robarCarta()) {
            log(activo.getNombre() + " no tiene cartas en su mazo. ¡PIERDE!");
            terminarJuego(oponente);
        } else {
            log(activo.getNombre() + " roba una carta.");
        }
    }

    public String jugarCarta(int indiceCarta, Posicion posicion, int indiceSacrificio) {
        return jugarCarta(indiceCarta, posicion, indiceSacrificio, -1);
    }

    public String jugarCarta(int indiceCarta, Posicion posicion,
                              int indiceSacrificio, int indiceSacrificio2) {
        if (yaJugoUnaCarta) return "Ya jugaste una carta este turno.";

        List<Carta> mano = activo.getMano();
        if (indiceCarta < 0 || indiceCarta >= mano.size()) return "Carta no válida.";

        Carta carta = mano.get(indiceCarta);

        if (carta.esMonstruo() && carta.comoMonstruo().getNivel() > 4) {
            int requeridos = carta.comoMonstruo().getNivel() >= 7 ? 2 : 1;
            List<Monstruo> campo = activo.getCampo();
            if (campo.size() < requeridos)
                return "Necesitas " + requeridos + " monstruo(s) en campo para invocar nivel " + carta.comoMonstruo().getNivel() + ".";

            if (indiceSacrificio < 0 || indiceSacrificio >= campo.size())
                return "Índice de sacrificio no válido.";

            Monstruo primero = campo.get(indiceSacrificio);
            activo.removerMonstruo(primero);

            if (requeridos == 2) {
                if (indiceSacrificio2 < 0 || indiceSacrificio2 >= activo.getCampo().size()) {
                    activo.invocarMonstruo(primero);
                    return "Índice del segundo sacrificio no válido.";
                }
                activo.removerMonstruo(activo.getCampo().get(indiceSacrificio2));
                log("¡Doble sacrificio realizado!");
            } else {
                log("¡Sacrificio realizado!");
            }
        }

        if (carta.esMonstruo()) carta.comoMonstruo().setPosicion(posicion);

        activo.removerDeMano(indiceCarta); 
        carta.jugar(activo, oponente);
        log(activo.getNombre() + " juega: " + carta.getNombre());

        if (carta.esMonstruo()) {
            Monstruo m = carta.comoMonstruo();
            if (m.getPosicion() == Posicion.ATAQUE) m.habilitarAtaque();
            else log(m.getNombre() + " está en DEFENSA y no puede atacar este turno.");
            revisarTrampas(CondicionTrampa.AL_INVOCAR);
        }

        yaJugoUnaCarta = true;
        verificarFinDeJuego();
        return null;
    }