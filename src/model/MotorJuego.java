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

        public String atacar(int indiceAtacante, int indiceDefensor) {
        if (esPrimerTurno()) return "No se puede atacar en el primer turno.";
        if (yaAtaco)         return "Ya realizaste un ataque este turno.";

        List<Monstruo> campo = activo.getCampo();
        if (indiceAtacante < 0 || indiceAtacante >= campo.size())
            return "Monstruo atacante no válido.";

        Monstruo atacante = campo.get(indiceAtacante);
        if (!atacante.puedeAtacar())
            return atacante.getNombre() + " no puede atacar (ya atacó o está en DEFENSA).";

        revisarTrampas(CondicionTrampa.EN_ATAQUE);
        if (juegoTerminado) { yaAtaco = true; return null; }

        if (!oponente.tieneMonstruos()) {
            resolverAtaqueDirecto(atacante);
        } else {
            List<Monstruo> campoRival = oponente.getCampo();
            if (indiceDefensor < 0 || indiceDefensor >= campoRival.size())
                return "Monstruo defensor no válido.";
            resolverCombate(atacante, campoRival.get(indiceDefensor));
        }

        atacante.agotarAtaque();
        yaAtaco = true;
        verificarFinDeJuego();
        return null;
    }

    public void pasarTurno() {
        activo.agotarAtaquesMonstruos();
        log(activo.getNombre() + " pasa el turno.");
        cambiarTurno();
        notificarObservadores(); // RF3 Observer
    }

    public List<Trampa> getTrampasActivables(CondicionTrampa condicion) {
        List<Trampa> resultado = new ArrayList<>();
        for (Trampa t : oponente.getTrampas()) {
            if (!t.isActivada() && t.getCondicion() == condicion) resultado.add(t);
        }
        return resultado;
    }

    public void activarTrampa(Trampa trampa) {
        trampa.activar(oponente, activo);
        oponente.removerTrampa(trampa);
        log("¡Trampa activada: " + trampa.getNombre() + "!");
        verificarFinDeJuego();
    }

    private void actualizarTreeMap() {
        campoOrdenadoPorAtk.clear();
        for (Monstruo m : activo.getCampo()) {
            campoOrdenadoPorAtk.put(m.getAtk(), m); 
        }
    }

    /** @return  */
    public Monstruo getMonstruoMasFuerteActivo() {
        actualizarTreeMap();
        if (campoOrdenadoPorAtk.isEmpty()) return null;
        return campoOrdenadoPorAtk.lastEntry().getValue();  
    }

    /** @return  */
    public Monstruo getMonstruoMasDebilActivo() {
        actualizarTreeMap();
        if (campoOrdenadoPorAtk.isEmpty()) return null;
        return campoOrdenadoPorAtk.firstEntry().getValue(); 
    }

    /** @return  */
    public Monstruo getMonstruoMasFuerteOponente() {
        TreeMap<Integer, Monstruo> mapaOponente = new TreeMap<>();
        for (Monstruo m : oponente.getCampo()) mapaOponente.put(m.getAtk(), m);
        if (mapaOponente.isEmpty()) return null;
        return mapaOponente.lastEntry().getValue();
    }

    private void revisarTrampas(CondicionTrampa condicion) {
        List<Trampa> activables = new ArrayList<>(oponente.getTrampas());
        for (Trampa trampa : activables) {
            if (!trampa.isActivada() && trampa.getCondicion() == condicion) {
                trampa.activar(oponente, activo);
                oponente.removerTrampa(trampa);
                log("¡Trampa activada automáticamente: " + trampa.getNombre() + "!");
                verificarFinDeJuego();
            }
        }
    }

    private void resolverAtaqueDirecto(Monstruo atacante) {
        log("¡ATAQUE DIRECTO de " + atacante.getNombre() + "! (" + atacante.getAtk() + " ATK)");

        boolean hayEscudo = tieneTrampaActiva(oponente, CondicionTrampa.AL_RECIBIR_DANIO, "Escudo Divino");
        boolean hayEspejo = tieneTrampaActiva(oponente, CondicionTrampa.AL_RECIBIR_DANIO, "Espejo del Alma");

        int danio = atacante.getAtk();
        if (hayEscudo) { danio /= 2; log("¡Escudo Divino reduce el daño a " + danio + "!"); removerTrampaUsada(oponente, "Escudo Divino"); }
        if (hayEspejo) { activo.recibirDanio(danio); log("¡Espejo del Alma! " + activo.getNombre() + " también recibe " + danio + " de daño."); removerTrampaUsada(oponente, "Espejo del Alma"); }

        oponente.recibirDanio(danio);
        log(oponente.getNombre() + " recibe " + danio + " de daño. LP: " + oponente.getLp());
    }

    private void resolverCombate(Monstruo atacante, Monstruo defensor) {
        int valorDefensor = (defensor.getPosicion() == Posicion.DEFENSA)
                ? defensor.getDef() : defensor.getAtk();

        log(atacante.getNombre() + " (" + atacante.getAtk() + " ATK) ataca a "
                + defensor.getNombre() + " (" + valorDefensor + " " + defensor.getPosicion() + ")");

        if (atacante.getAtk() > valorDefensor) {
            if (defensor.getPosicion() == Posicion.ATAQUE) {
                int danio = atacante.getAtk() - valorDefensor;
                boolean hayEscudo = tieneTrampaActiva(oponente, CondicionTrampa.AL_RECIBIR_DANIO, "Escudo Divino");
                boolean hayEspejo = tieneTrampaActiva(oponente, CondicionTrampa.AL_RECIBIR_DANIO, "Espejo del Alma");
                if (hayEscudo) { danio /= 2; log("¡Escudo Divino reduce el daño a " + danio + "!"); removerTrampaUsada(oponente, "Escudo Divino"); }
                if (hayEspejo) { activo.recibirDanio(danio); log("¡Espejo del Alma! " + activo.getNombre() + " recibe " + danio + " de daño."); removerTrampaUsada(oponente, "Espejo del Alma"); }
                oponente.recibirDanio(danio);
                log(defensor.getNombre() + " destruido. Daño: " + danio + ". LP " + oponente.getNombre() + ": " + oponente.getLp());
            } else {
                log(defensor.getNombre() + " destruido en defensa.");
            }
            oponente.removerMonstruo(defensor);

        } else if (atacante.getAtk() == valorDefensor) {
            if (defensor.getPosicion() == Posicion.ATAQUE) {
                activo.removerMonstruo(atacante);
                oponente.removerMonstruo(defensor);
                log("¡Empate! Ambos monstruos destruidos.");
            } else {
                log("Empate en defensa. Nadie es destruido.");
            }
        } else {
            int danio = valorDefensor - atacante.getAtk();
            boolean hayEscudo = tieneTrampaActiva(activo, CondicionTrampa.AL_RECIBIR_DANIO, "Escudo Divino");
            boolean hayEspejo = tieneTrampaActiva(activo, CondicionTrampa.AL_RECIBIR_DANIO, "Espejo del Alma");
            if (hayEscudo) { danio /= 2; log("¡Escudo Divino reduce el daño a " + danio + "!"); removerTrampaUsada(activo, "Escudo Divino"); }
            if (hayEspejo) { oponente.recibirDanio(danio); log("¡Espejo del Alma! " + oponente.getNombre() + " recibe " + danio + " de daño."); removerTrampaUsada(activo, "Espejo del Alma"); }
            activo.recibirDanio(danio);
            activo.removerMonstruo(atacante);
            log(atacante.getNombre() + " destruido. Daño a " + activo.getNombre() + ": " + danio + ". LP: " + activo.getLp());
        }
    }

    private boolean tieneTrampaActiva(Jugador jugador, CondicionTrampa condicion, String nombre) {
        for (Trampa t : jugador.getTrampas()) {
            if (!t.isActivada() && t.getCondicion() == condicion && t.getNombre().equals(nombre))
                return true;
        }
        return false;
    }

    private void removerTrampaUsada(Jugador jugador, String nombre) {
        Iterator<Trampa> it = jugador.getTrampas().iterator();
        while (it.hasNext()) {
            Trampa t = it.next();
            if (t.getNombre().equals(nombre)) { it.remove(); return; }
        }
    }

    private void verificarFinDeJuego() {
        if (!jugador1.estaVivo()) terminarJuego(jugador2);
        else if (!jugador2.estaVivo()) terminarJuego(jugador1);
    }

    private void terminarJuego(Jugador ganador) {
        this.juegoTerminado = true;
        this.ganador        = ganador;
        log("¡El duelo ha terminado! Ganador: " + ganador.getNombre());
    }

    private void cambiarTurno() {
        Jugador temp = activo;
        activo       = oponente;
        oponente     = temp;
        numeroTurno++;
    }
}