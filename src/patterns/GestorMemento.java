package patterns;

import java.util.ArrayDeque;
import java.util.Deque;


public class GestorMemento {


    private final Deque<MementoJuego> pila = new ArrayDeque<>();


    private static final int MAX_SNAPSHOTS = 20;


    public void guardar(MementoJuego memento) {
        if (pila.size() >= MAX_SNAPSHOTS) {

            ((ArrayDeque<MementoJuego>) pila).removeLast();
        }
        pila.push(memento);
    }

    /**
     * Recupera (y elimina) el snapshot más reciente.
     * @return el último Memento guardado, o null si no hay ninguno.
     */
    public MementoJuego restaurar() {
        if (pila.isEmpty()) return null;
        return pila.pop();
    }

    /** @return true si hay al menos un snapshot guardado. */
    public boolean haySnapshot() {
        return !pila.isEmpty();
    }


    public int cantidadSnapshots() {
        return pila.size();
    }


    public void limpiar() {
        pila.clear();
    }
}