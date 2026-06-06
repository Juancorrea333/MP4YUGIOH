package model;

public class Trampa extends Carta {
    private String          descripcion;
    private AccionMagica    efecto;
    private CondicionTrampa condicion;
    private boolean         activada;

    public Trampa(String nombre, String descripcion, CondicionTrampa condicion, AccionMagica efecto) {
        super(nombre);
        this.descripcion = descripcion;
        this.condicion   = condicion;
        this.efecto      = efecto;
        this.activada    = false;
    }

    public String          getDescripcion() { return descripcion; }
    public CondicionTrampa getCondicion()   { return condicion; }
    public boolean         isActivada()     { return activada; }

    public void activar(Jugador duenio, Jugador oponente) {
        if (activada) return;
        activada = true;
        efecto.activarEfecto(duenio, oponente);
    }

    @Override
    public void jugar(Jugador usuario, Jugador oponente) {
        usuario.colocarTrampa(this);
    }

    @Override
    public void mostrarInfo() {
        System.out.print(toString());
    }

    @Override
    public String toString() {
        return "[TRAMPA: " + getNombre() + " (" + condicion + ") - " + descripcion + "]";
    }
}
