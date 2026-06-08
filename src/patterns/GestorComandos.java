package patterns;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;


public class GestorComandos {


    private final Deque<Comando> pilaUndo  = new ArrayDeque<>();


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
     * 
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


    public List<String> getHistorial() {
        return Collections.unmodifiableList(historial);
    }


    public void reiniciar() {
        pilaUndo.clear();
        historial.clear();
    }
}
