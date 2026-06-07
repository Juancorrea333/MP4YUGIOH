package patterns;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/**
 * RF3 – Patrón Command: Gestor de comandos (Invoker).
 *
 * Mantiene una pila (Deque) de comandos ejecutados para permitir DESHACER.
 * También guarda el historial completo de acciones de la partida.
 *
 * Uso típico en el Controlador:
 *
 *   GestorComandos gestor = new GestorComandos();
 *   gestor.ejecutar(new ComandoJugarCarta(motor, indice, posicion, ...));
 *   // ... más acciones ...
 *   gestor.deshacer(); // revierte la última acción
 */
public class GestorComandos {

    /** Pila de comandos ejecutados (tope = último ejecutado). */
    private final Deque<Comando> pilaUndo  = new ArrayDeque<>();

    /** Historial completo (incluso los deshechos) para log/depuración. */
    private final List<String>   historial = new ArrayList<>();

    /**
     * Ejecuta un comando y lo apila para posible undo.
     * Si el comando falla (retorna error), NO se apila.
     *
     * @return mensaje de error o null si todo fue bien.
     */
    public String ejecutar(Comando cmd) {
        String error = cmd.execute();
        if (error == null) {
            pilaUndo.push(cmd);
            historial.add("[OK] " + cmd.getDescripcion());
        } else {
            historial.add("[ERR] " + cmd.getDescripcion() + " → " + error);
        }
        return error;
    }

    /**
     * Deshace el último comando ejecutado.
     * @return true si había algo que deshacer, false si la pila estaba vacía.
     */
    public boolean deshacer() {
        if (pilaUndo.isEmpty()) return false;
        Comando ultimo = pilaUndo.pop();
        ultimo.undo();
        historial.add("[UNDO] " + ultimo.getDescripcion());
        return true;
    }

    /** @return true si hay al menos un comando que se puede deshacer. */
    public boolean puedeDeshacer() {
        return !pilaUndo.isEmpty();
    }

    /** Historial de acciones (inmutable). */
    public List<String> getHistorial() {
        return Collections.unmodifiableList(historial);
    }

    /** Limpia la pila y el historial (al iniciar nueva partida). */
    public void reiniciar() {
        pilaUndo.clear();
        historial.clear();
    }
}
