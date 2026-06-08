package patterns;

import model.Jugador;


public interface EstrategiaEfecto {

    /**
     * Aplica el efecto de la carta.
     *
     * @param usuario  jugador que activó la carta.
     * @param oponente jugador que recibe el efecto.
     */
    void activar(Jugador usuario, Jugador oponente);


    String getNombreEfecto();


    String getDescripcion();
}
