package patterns;

import model.Carta;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * RF3 – Patrón Singleton: Registro centralizado de cartas.
 *
 * Garantiza que exista UNA SOLA instancia del catálogo de cartas en toda
 * la aplicación. Si se usara más de una instancia, podrían existir catálogos
 * inconsistentes (especialmente al cargar cartas por Reflection en RF3-B).
 *
 * Diferencia con FabricaCartas (RF1/RF2):
 *  FabricaCartas crea objetos nuevos cada vez (fábrica).
 *  RegistroCartas es el directorio único de definiciones (singleton).
 *  La carga por Reflection registra las cartas aquí para que cualquier
 *  componente las encuentre sin tener que recargarlas.
 *
 * Implementación: inicialización perezosa thread-safe con doble bloqueo (DCL).
 */
public class RegistroCartas {

    // ---- Única instancia (volatile para visibilidad entre hilos) ----
    private static volatile RegistroCartas instancia;

    /** Catálogo: nombre → carta. */
    private final Map<String, Carta> catalogo = new HashMap<>();

    /** Constructor privado: nadie puede instanciar desde fuera. */
    private RegistroCartas() {}

    /**
     * Punto de acceso global a la única instancia.
     * Usa doble bloqueo (DCL) para ser thread-safe sin penalidad de rendimiento
     * en el caso común (instancia ya creada).
     */
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

    // ---- API pública ----

    /**
     * Registra (o sobreescribe) una carta en el catálogo.
     * Llamado por FabricaCartasReflection al cargar cartas desde archivo.
     */
    public void registrar(Carta carta) {
        catalogo.put(carta.getNombre(), carta);
    }

    /** Busca una carta por nombre. @return la carta o null si no existe. */
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

    /**
     * Limpia el catálogo.
     * Útil en tests o si se quiere recargar el archivo de cartas.
     */
    public void limpiar() {
        catalogo.clear();
    }

    @Override
    public String toString() {
        return "RegistroCartas[" + catalogo.size() + " cartas]";
    }
}
