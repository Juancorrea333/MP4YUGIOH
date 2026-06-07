package patterns;

/**
 * RF3 – Patrón Command.
 *
 * Cada acción del jugador (invocar monstruo, activar magia, atacar, pasar turno)
 * se encapsula como un objeto que implementa esta interfaz.
 *
 * Beneficios:
 *  - Desacopla el emisor (Controlador) del receptor (MotorJuego).
 *  - Permite implementar DESHACER (undo) la última jugada.
 *  - Facilita un historial de comandos ejecutados.
 *
 * execute() → realiza la acción.
 * undo()    → revierte la acción (si aplica).
 */
public interface Comando {

    /**
     * Ejecuta la acción encapsulada.
     * @return mensaje de error si la acción no pudo realizarse, null si fue exitosa.
     */
    String execute();

    /**
     * Revierte la acción ejecutada.
     * Si el comando no soporta deshacer, puede lanzar UnsupportedOperationException
     * o simplemente no hacer nada (con un mensaje informativo).
     */
    void undo();

    /** Descripción legible del comando, usada en el historial. */
    String getDescripcion();
}
