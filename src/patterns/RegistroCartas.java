package patterns;

import model.Carta;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class RegistroCartas {


    private static volatile RegistroCartas instancia;


    private final Map<String, Carta> catalogo = new HashMap<>();


    private RegistroCartas() {}


    public static RegistroCartas getInstance() {
        if (instancia == null) {
            synchronized (RegistroCartas.class) {
                if (instancia == null) {
                    instancia = new RegistroCartas();
                }
            }
        }
        return instancia;
    }



    public void registrar(Carta carta) {
        catalogo.put(carta.getNombre(), carta);
    }


    public Carta buscar(String nombre) {
        return catalogo.get(nombre);
    }

    /** @return vista inmutable del conjunto de nombres registrados. */
    public Set<String> getNombres() {
        return Collections.unmodifiableSet(catalogo.keySet());
    }

    /** @return número de cartas registradas. */
    public int tamanio() {
        return catalogo.size();
    }


    public void limpiar() {
        catalogo.clear();
    }

    @Override
    public String toString() {
        return "RegistroCartas[" + catalogo.size() + " cartas]";
    }
}
