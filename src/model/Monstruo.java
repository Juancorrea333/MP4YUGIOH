package model;

public class Monstruo extends Carta {
    private int     atk;
    private int     def;
    private int     nivel;
    private boolean puedeAtacar;
    private Posicion posicion;

    public Monstruo(String nombre, int atk, int def, int nivel) {
        super(nombre);
        if (atk < 0)                 throw new IllegalArgumentException("ATK no puede ser negativo.");
        if (def < 0)                 throw new IllegalArgumentException("DEF no puede ser negativa.");
        if (nivel < 1 || nivel > 12) throw new IllegalArgumentException("El nivel debe estar entre 1 y 12.");
        this.atk         = atk;
        this.def         = def;
        this.nivel       = nivel;
        this.puedeAtacar = false;
        this.posicion    = Posicion.ATAQUE;
    }

    public int      getAtk()      { return atk; }
    public int      getDef()      { return def; }
    public int      getNivel()    { return nivel; }
    public boolean  puedeAtacar() { return puedeAtacar; }
    public Posicion getPosicion() { return posicion; }

    public void setPosicion(Posicion posicion) { this.posicion = posicion; }

    public void aumentarAtk(int cantidad) {
        this.atk += cantidad;
        if (this.atk < 0) this.atk = 0;
    }

    public void habilitarAtaque() { this.puedeAtacar = true; }
    public void agotarAtaque()    { this.puedeAtacar = false; }

    @Override
    public void jugar(Jugador usuario, Jugador oponente) {
        usuario.invocarMonstruo(this);
    }

    @Override
    public void mostrarInfo() {
        System.out.print(toString());
    }

    @Override
    public String toString() {
        return "[" + getNombre()
                + " ATK:" + atk
                + " DEF:" + def
                + " LVL:" + nivel
                + " POS:" + posicion + "]";
    }
}
