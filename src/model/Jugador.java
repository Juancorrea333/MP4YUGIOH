package model;

import java.util.*;


public class Jugador {

    private String             nombre;
    private int                lp = 8000;

    
    private Stack<Carta>       mazo    = new Stack<>();


    private LinkedList<Carta>  mano    = new LinkedList<>();

    private Set<String>        monstruosInvocados = new HashSet<>();


    private Map<String, Carta> indiceMano = new HashMap<>();

    private List<Monstruo>     campo   = new ArrayList<>();
    private List<Trampa>       trampas = new ArrayList<>();

    public Jugador(String nombre) { this.nombre = nombre; }

    public String              getNombre()              { return nombre; }
    public int                 getLp()                  { return lp; }
    public LinkedList<Carta>   getMano()                { return mano; }
    public List<Monstruo>      getCampo()               { return campo; }
    public Stack<Carta>        getMazo()                { return mazo; }
    public List<Trampa>        getTrampas()             { return trampas; }
    public Set<String>         getMonstruosInvocados()  { return monstruosInvocados; }
    public Map<String, Carta>  getIndiceMano()          { return Collections.unmodifiableMap(indiceMano); }

    public boolean estaVivo()       { return lp > 0; }
    public boolean tieneMazo()      { return !mazo.isEmpty(); }
    public boolean tieneMano()      { return !mano.isEmpty(); }
    public boolean tieneMonstruos() { return !campo.isEmpty(); }
    public boolean tieneTrampas()   { return !trampas.isEmpty(); }

    
    public void agregarAlMazo(Carta c) { mazo.push(c); }

    
    public boolean robarCarta() {
        if (mazo.isEmpty()) return false;
        Carta carta = mazo.pop();        
        mano.add(carta);                  
        indiceMano.put(carta.getNombre(), carta); 
        return true;
    }

    
    public Carta buscarEnMano(String nombre) {
        return indiceMano.get(nombre); 
    }

    
    public Carta removerDeMano(int indice) {
        Carta carta = mano.remove(indice); 
        indiceMano.remove(carta.getNombre());
        return carta;
    }

    public void invocarMonstruo(Monstruo m) {
        campo.add(m);
        monstruosInvocados.add(m.getNombre());   
    }

    public void removerMonstruo(Monstruo m) { campo.remove(m); }
    public void limpiarCampo()              { campo.clear(); }

    public void agotarAtaquesMonstruos() {
        for (Monstruo m : campo) m.agotarAtaque();
    }

    public void colocarTrampa(Trampa t) { trampas.add(t); }
    public void removerTrampa(Trampa t) { trampas.remove(t); }

    public void recibirDanio(int cant) { lp -= cant; if (lp < 0) lp = 0; }
    public void recuperarLp(int cant)  { lp += cant; }
}
