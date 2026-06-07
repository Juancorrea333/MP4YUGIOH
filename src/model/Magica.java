package model;

public class Magica extends Carta implements AccionMagica {
    private String       descripcion;
    private AccionMagica efecto;

    public Magica(String nombre, String descripcion, AccionMagica efecto) {
        super(nombre);
        this.descripcion = descripcion;
        this.efecto      = efecto;
    }

    public String getDescripcion() { return descripcion; }

    @Override
    public void activarEfecto(Jugador usuario, Jugador oponente) {
        efecto.activarEfecto(usuario, oponente);
    }

    @Override
    public void jugar(Jugador usuario, Jugador oponente) {
        activarEfecto(usuario, oponente);
    }

    @Override
    public void mostrarInfo() {
        System.out.print(toString());
    }

    @Override
    public String toString() {
        return "[MAGICA: " + getNombre() + " - " + descripcion + "]";
    }
}
