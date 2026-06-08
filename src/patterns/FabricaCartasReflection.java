package patterns;

import model.*;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;


public class FabricaCartasReflection {


    private static final String ARCHIVO_DEFECTO = "cartas/cartas_dinamicas.txt";


    private static final String PAQUETE_EFECTOS = "patterns.Efectos$";




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


                if (linea.isEmpty() || linea.startsWith("#")) continue;

                try {
                    Carta carta = parsearLinea(linea);
                    if (carta != null) {
                        registro.registrar(carta); 
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


    public static void crearArchivoEjemplo() {
        File dir = new File("cartas");
        if (!dir.exists()) dir.mkdirs();

        File archivo = new File(ARCHIVO_DEFECTO);
        if (archivo.exists()) return; 

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


    private static Monstruo parsearMonstruo(String[] p) {
        if (p.length < 5) throw new IllegalArgumentException("MONSTRUO requiere 5 campos.");
        String nombre = p[1].strip();
        int    atk    = Integer.parseInt(p[2].strip());
        int    def    = Integer.parseInt(p[3].strip());
        int    nivel  = Integer.parseInt(p[4].strip());
        return new Monstruo(nombre, atk, def, nivel);
    }


    private static Magica parsearMagica(String[] p)
            throws ReflectiveOperationException {
        if (p.length < 3) throw new IllegalArgumentException("MAGICA requiere 3 campos.");
        String nombre      = p[1].strip();
        String claseEfecto = p[2].strip();


        Class<?> clazz = Class.forName(PAQUETE_EFECTOS + claseEfecto);  
        Constructor<?> ctor = clazz.getDeclaredConstructor();            
        ctor.setAccessible(true);
        EstrategiaEfecto efecto = (EstrategiaEfecto) ctor.newInstance(); 


        return new Magica(nombre, efecto.getDescripcion(),
                (usuario, oponente) -> efecto.activar(usuario, oponente));
    }


    private static Trampa parsearTrampa(String[] p)
            throws ReflectiveOperationException {
        if (p.length < 4) throw new IllegalArgumentException("TRAMPA requiere 4 campos.");
        String          nombre     = p[1].strip();
        CondicionTrampa condicion  = CondicionTrampa.valueOf(p[2].strip().toUpperCase());
        String          claseEfecto = p[3].strip();


        Class<?> clazz = Class.forName(PAQUETE_EFECTOS + claseEfecto);  
        Constructor<?> ctor = clazz.getDeclaredConstructor();            
        ctor.setAccessible(true);
        EstrategiaEfecto efecto = (EstrategiaEfecto) ctor.newInstance(); 


        return new Trampa(nombre, efecto.getDescripcion(), condicion,
                (usuario, oponente) -> efecto.activar(usuario, oponente));
    }
}
