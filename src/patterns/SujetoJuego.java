package patterns;

/**
 * RF3 – Patrón Observer: interfaz del Sujeto (Subject).
 *
 * Todo objeto que sea "observable" debe implementar estos tres métodos.
 * MotorJuego implementará esta interfaz para convertirse en sujeto.
 */
public interface SujetoJuego {

    /** Registra un nuevo observador. */
    void agregarObservador(ObservadorJuego obs);

    /** Elimina un observador previamente registrado. */
    void eliminarObservador(ObservadorJuego obs);

    /**
     * Notifica a TODOS los observadores registrados que el estado cambió.
     * Se llama internamente después de cada operación que modifica el estado.
     */
    void notificarObservadores();
}
