package patterns;

import model.*;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * RF3 – Factory Method + Reflection API de Java.
 *
 * Lee un archivo de texto (.txt) y crea objetos Carta en tiempo de ejecución
 * sin usar if/switch.  Agregar una nueva carta al juego sólo requiere añadir
 * una línea al archivo y (si el efecto es nuevo) crear una clase en Efectos.java;
 * no se toca ni recompila ninguna otra clase.
 *
 * ═══════════════════════════════════════════════════════════════════
 * FORMATO DEL ARCHIVO  cartas/cartas_dinamicas.txt
 * ═══════════════════════════════════════════════════════════════════
 *
 *  Líneas en blanco o que empiezan con '#' son comentarios/ignoradas.
 *
 *  MONSTRUO
 *  --------
 *  MONSTRUO|nombre|atk|def|nivel
 *  Ejemplo:
 *    MONSTRUO|Slime Acuático|450|300|2
 *
 *  MAGICA
 *  ------
 *  MAGICA|nombre|ClaseEfecto
 *  donde ClaseEfecto es el nombre simple de una clase interna de patterns.Efectos.
 *  Ejemplo:
 *    MAGICA|Rayo Cósmico|DanoDirecto800
 *
 *  TRAMPA
 *  ------
 *  TRAMPA|nombre|condicion|ClaseEfecto
 *  condicion ∈ {EN_ATAQUE, AL_INVOCAR, INMEDIATA, AL_RECIBIR_DANIO}
 *  Ejemplo:
 *    TRAMPA|Red de Sombras|EN_ATAQUE|ReducirAtacanteAtk1500
 *
 * ═══════════════════════════════════════════════════════════════════
 *
 * Uso de Reflection (java.lang.reflect):
 *   1. Class.forName("patterns.Efectos$" + claseEfecto)
 *      → localiza la clase en tiempo de ejecución.
 *   2. getDeclaredConstructor()
 *      → obtiene el constructor por defecto.
 *   3. constructor.newInstance()
 *      → crea la instancia sin new.
 *
 * Las cartas creadas se registran en el Singleton RegistroCartas para
 * que cualquier componente las encuentre.
 */
public class FabricaCartasReflection {

    /** Ruta por defecto del archivo de cartas dinámicas. */
    private static final String ARCHIVO_DEFECTO = "cartas/cartas_dinamicas.txt";

    /** Prefijo del paquete de efectos para Reflection. */
    private static final String PAQUETE_EFECTOS = "patterns.Efectos$";

    // ── Singleton de GestorPersistencia se accede desde persistencia.GestorPersistencia
    //    pero aquí no lo usamos directamente (separación de responsabilidades).

    // =========================================================================
    // API pública
    // =========================================================================

    /**
     * Lee el archivo por defecto y carga todas las cartas en RegistroCartas.
     *
     * @return lista de cartas cargadas exitosamente.
     * @throws IOException si el archivo no existe o no se puede leer.
     */
    public static List<Carta> cargarDesdeArchivo() throws IOException {
        return cargarDesdeArchivo(ARCHIVO_DEFECTO);
    }

    /**
     * Lee el archivo indicado y carga todas las cartas en RegistroCartas.
     *
     * @param rutaArchivo ruta relativa o absoluta del archivo .txt.
     * @return lista de cartas cargadas exitosamente.
     * @throws IOException si el archivo no existe.
     */
    public static List<Carta> cargarDesdeArchivo(String rutaArchivo) throws IOException {
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            throw new FileNotFoundException("Archivo de cartas no encontrado: " + rutaArchivo);
        }

        List<Carta> cargadas = new ArrayList<>();
        RegistroCartas registro = RegistroCartas.getInstance();  // Singleton

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            int numeroLinea = 0;

            while ((linea = br.readLine()) != null) {
                numeroLinea++;
                linea = linea.strip();

                // Ignorar líneas vacías y comentarios
                if (linea.isEmpty() || linea.startsWith("#")) continue;

                try {
                    Carta carta = parsearLinea(linea);
                    if (carta != null) {
                        registro.registrar(carta);   // Singleton
                        cargadas.add(carta);
                        System.out.println("[Reflection] Carta cargada: " + carta.getNombre()
                                + " (" + carta.getClass().getSimpleName() + ")");
                    }
                } catch (Exception e) {
                    System.err.println("[Reflection] Error en línea " + numeroLinea
                            + " [" + linea + "]: " + e.getMessage());
                }
            }
        }

        System.out.println("[Reflection] Total cartas cargadas: " + cargadas.size());
        return cargadas;
    }

    /**
     * Crea el archivo de ejemplo si no existe, para que el proyecto funcione
     * inmediatamente sin necesidad de crearlo a mano.
     */
    public static void crearArchivoEjemplo() {
        File dir = new File("cartas");
        if (!dir.exists()) dir.mkdirs();

        File archivo = new File(ARCHIVO_DEFECTO);
        if (archivo.exists()) return; // ya existe, no sobreescribir

        try (PrintWriter pw = new PrintWriter(new FileWriter(archivo))) {
            pw.println("# ================================================================");
            pw.println("# cartas_dinamicas.txt  –  RF3: Carga de cartas por Reflection");
            pw.println("# ================================================================");
            pw.println("# Formato:");
            pw.println("#   MONSTRUO|nombre|atk|def|nivel");
            pw.println("#   MAGICA|nombre|ClaseEfecto");
            pw.println("#   TRAMPA|nombre|condicion|ClaseEfecto");
            pw.println("#");
            pw.println("# condicion: EN_ATAQUE | AL_INVOCAR | INMEDIATA | AL_RECIBIR_DANIO");
            pw.println("# ClaseEfecto: nombre de clase interna en patterns.Efectos");
            pw.println("# ================================================================");
            pw.println();
            pw.println("# ---- Monstruos adicionales ----");
            pw.println("MONSTRUO|Slime Acuático|450|300|2");
            pw.println("MONSTRUO|Caballero de Hierro|1800|1600|5");
            pw.println("MONSTRUO|Dragón de Cristal|2400|2000|7");
            pw.println("MONSTRUO|Elemental del Fuego|1600|1200|4");
            pw.println("MONSTRUO|Guardián del Templo|1000|2000|4");
            pw.println();
            pw.println("# ---- Cartas mágicas adicionales ----");
            pw.println("MAGICA|Rayo Cósmico|DanoDirecto800");
            pw.println("MAGICA|Viento Celestial|RobarDosCartas");
            pw.println("MAGICA|Poción Sagrada|Curar1000");
            pw.println("MAGICA|Tormenta Oscura|DestruirTodosCampoOponente");
            pw.println("MAGICA|Espada del Destino|AumentarAtkMasFuerte1000");
            pw.println("MAGICA|Niebla del Olvido|PasarMonstruoMasFuerteDefensa");
            pw.println("MAGICA|Pacto de Sangre|DescartarRecuperar2000");
            pw.println("MAGICA|Balanza Oscura|DanoPorDiferenciaLP");
            pw.println("MAGICA|Cometa Tóxico|DanoDirecto500RobarUna");
            pw.println("MAGICA|Demolición|DestruirMonstruoMenorAtk");
            pw.println();
            pw.println("# ---- Trampas adicionales ----");
            pw.println("TRAMPA|Red de Sombras|EN_ATAQUE|ReducirAtacanteAtk1500");
            pw.println("TRAMPA|Barrera Mágica|EN_ATAQUE|ReflejarAtaqueComoLpDano");
            pw.println("TRAMPA|Jaula de Cristal|AL_INVOCAR|DestruirInvocadoConAtk1000");
            pw.println("TRAMPA|Portal Inverso|AL_INVOCAR|RegresarInvocadoAMano");
            pw.println("TRAMPA|Manto Sagrado|AL_RECIBIR_DANIO|EscudoReducirDanioAMitad");
            pw.println("TRAMPA|Eco Mortal|AL_RECIBIR_DANIO|EspejoReflectarDanio");
            pw.println("TRAMPA|Gran Sacrificio|INMEDIATA|PagarMitadLpLimpiarCampo");
            pw.println("TRAMPA|Cataclismo|INMEDIATA|DestruirTodosLosMonstruos");
            pw.println("TRAMPA|Apuesta Extrema|INMEDIATA|Perder1000LpRobarDos");
            pw.println("TRAMPA|Destino Oscuro|EN_ATAQUE|DestruirMonstruosAtaqueOponente");
        } catch (IOException e) {
            System.err.println("[Reflection] No se pudo crear el archivo de ejemplo: " + e.getMessage());
        }
    }

    // =========================================================================
    // Lógica interna de parseo + Reflection
    // =========================================================================

    /**
     * Parsea una línea del archivo y crea la Carta correspondiente.
     *
     * @throws ReflectiveOperationException si la clase de efecto no se encuentra.
     * @throws IllegalArgumentException     si el formato de la línea es incorrecto.
     */
    private static Carta parsearLinea(String linea)
            throws ReflectiveOperationException, IllegalArgumentException {

        String[] partes = linea.split("\\|");
        if (partes.length < 2) throw new IllegalArgumentException("Línea con menos de 2 campos.");

        String tipo = partes[0].strip().toUpperCase();

        return switch (tipo) {
            case "MONSTRUO" -> parsearMonstruo(partes);
            case "MAGICA"   -> parsearMagica(partes);
            case "TRAMPA"   -> parsearTrampa(partes);
            default -> throw new IllegalArgumentException("Tipo desconocido: " + tipo);
        };
    }

    /** Crea un Monstruo: MONSTRUO|nombre|atk|def|nivel */
    private static Monstruo parsearMonstruo(String[] p) {
        if (p.length < 5) throw new IllegalArgumentException("MONSTRUO requiere 5 campos.");
        String nombre = p[1].strip();
        int    atk    = Integer.parseInt(p[2].strip());
        int    def    = Integer.parseInt(p[3].strip());
        int    nivel  = Integer.parseInt(p[4].strip());
        return new Monstruo(nombre, atk, def, nivel);
    }

    /**
     * Crea una Mágica usando Reflection: MAGICA|nombre|ClaseEfecto
     *
     * Reflection steps:
     *   1. Class.forName(PAQUETE_EFECTOS + claseEfecto)  – localiza la clase
     *   2. getDeclaredConstructor()                       – obtiene constructor vacío
     *   3. newInstance()                                  – crea el objeto
     */
    private static Magica parsearMagica(String[] p)
            throws ReflectiveOperationException {
        if (p.length < 3) throw new IllegalArgumentException("MAGICA requiere 3 campos.");
        String nombre      = p[1].strip();
        String claseEfecto = p[2].strip();

        // ── REFLECTION ──────────────────────────────────────────────────────
        Class<?> clazz = Class.forName(PAQUETE_EFECTOS + claseEfecto);  // 1
        Constructor<?> ctor = clazz.getDeclaredConstructor();            // 2
        ctor.setAccessible(true);
        EstrategiaEfecto efecto = (EstrategiaEfecto) ctor.newInstance(); // 3
        // ────────────────────────────────────────────────────────────────────

        // Adaptamos EstrategiaEfecto → AccionMagica (son interfaces equivalentes)
        return new Magica(nombre, efecto.getDescripcion(),
                (usuario, oponente) -> efecto.activar(usuario, oponente));
    }

    /**
     * Crea una Trampa usando Reflection: TRAMPA|nombre|condicion|ClaseEfecto
     *
     * Mismo proceso de Reflection que parsearMagica.
     */
    private static Trampa parsearTrampa(String[] p)
            throws ReflectiveOperationException {
        if (p.length < 4) throw new IllegalArgumentException("TRAMPA requiere 4 campos.");
        String          nombre     = p[1].strip();
        CondicionTrampa condicion  = CondicionTrampa.valueOf(p[2].strip().toUpperCase());
        String          claseEfecto = p[3].strip();

        // ── REFLECTION ──────────────────────────────────────────────────────
        Class<?> clazz = Class.forName(PAQUETE_EFECTOS + claseEfecto);  // 1
        Constructor<?> ctor = clazz.getDeclaredConstructor();            // 2
        ctor.setAccessible(true);
        EstrategiaEfecto efecto = (EstrategiaEfecto) ctor.newInstance(); // 3
        // ────────────────────────────────────────────────────────────────────

        return new Trampa(nombre, efecto.getDescripcion(), condicion,
                (usuario, oponente) -> efecto.activar(usuario, oponente));
    }
}
