package patterns;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * RF3 – Patrón Memento: Caretaker (Cuidador).
 *
 * Almacena y gestiona los Mementos sin conocer su contenido interno.
 * Permite apilar múltiples snapshots para un historial de undo.
 *
 * GestorMemento es independiente del Originator (MotorJuego):
 *  - MotorJuego crea el Memento y lo entrega al Caretaker.
 *  - Cuando se deshace, el Caretaker devuelve el Memento al MotorJuego
 *    para que éste restaure su estado.
 */
public class GestorMemento {

    /** Pila de instantáneas (tope = la más reciente). */
    private final Deque<MementoJuego> pila = new ArrayDeque<>();

    /** Límite de snapshots retenidos para no consumir memoria ilimitada. */
    private static final int MAX_SNAPSHOTS = 20;

    /**
     * Guarda un nuevo snapshot.
     * Si se supera el límite, descarta el más antiguo.
     */
    public void guardar(MementoJuego memento) {
        if (pila.size() >= MAX_SNAPSHOTS) {
            // Eliminamos el snapshot más antiguo (fondo de la deque)
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

    /** Número de snapshots actualmente almacenados. */
    public int cantidadSnapshots() {
        return pila.size();
    }

    /** Vacía todos los snapshots (p.ej. al iniciar nueva partida). */
    public void limpiar() {
        pila.clear();
    }
}
