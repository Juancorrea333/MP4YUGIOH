package patterns;

import model.Jugador;

/**
 * RF3 – Patrón Strategy (Estrategia).
 *
 * Define el contrato de los algoritmos intercambiables de efectos de carta.
 * Cada tipo de efecto (daño directo, robar cartas, curar LP, destruir monstruos…)
 * es una EstrategiaEfecto concreta.
 *
 * Beneficios frente al switch/if-else actual en FabricaCartas:
 *  - Agregar un nuevo efecto = crear una nueva clase, sin tocar código existente.
 *  - Las cartas cargadas por Reflection (RF3-B) reciben la estrategia apropiada
 *    en tiempo de ejecución, sin recompilar ni modificar un switch.
 *
 * Relación con AccionMagica:
 *  AccionMagica (RF1/RF2) es una interfaz funcional que ya cumple un rol similar
 *  pero dentro del paquete model. EstrategiaEfecto vive en patterns y añade
 *  metadatos (nombre, descripción) para la carga dinámica por Reflection.
 */
public interface EstrategiaEfecto {

    /**
     * Aplica el efecto de la carta.
     *
     * @param usuario  jugador que activó la carta.
     * @param oponente jugador que recibe el efecto.
     */
    void activar(Jugador usuario, Jugador oponente);

    /**
     * Nombre técnico único del efecto (usado por Reflection para instanciarlo).
     * Ejemplo: "DanoDirecto800", "RobarDosCartas", "DestruirTodosCampo".
     */
    String getNombreEfecto();

    /** Descripción legible para mostrar en la UI / archivo de cartas. */
    String getDescripcion();
}
