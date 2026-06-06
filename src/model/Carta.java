package model;

public abstract class Carta {

    private String nombre;

    public Carta(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public abstract void jugar(Jugador usuario, Jugador oponente);
    public abstract void mostrarInfo();

    public boolean esMonstruo() {
        return this instanceof Monstruo;
    }

    public Monstruo comoMonstruo() {
        return (Monstruo) this;
    }
}
