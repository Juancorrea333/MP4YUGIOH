package patterns;

import model.MotorJuego;

/**
 * RF3 – Patrón Observer (Observador).
 *
 * Interfaz que deben implementar todos los componentes que quieran
 * recibir notificaciones cuando el estado del MotorJuego cambie.
 *
 * Uso típico:
 *  - VentanaDuelo implementa ObservadorJuego → se actualiza automáticamente.
 *  - PanelMano, PanelCampo, PanelLP pueden ser observadores independientes.
 *
 * Ventaja: el modelo (MotorJuego) no conoce a la vista directamente;
 * sólo notifica a quienes se registraron, eliminando el acoplamiento directo.
 */
public interface ObservadorJuego {

    /**
     * Llamado por el sujeto (MotorJuego / SujetoJuego) cada vez que el
     * estado del juego cambia (carta jugada, ataque, cambio de turno, fin).
     *
     * @param motor  referencia al motor para que el observador consulte
     *               el nuevo estado sin que el sujeto tenga que enviarlo todo.
     */
    void actualizar(MotorJuego motor);
}
