package persistencia;

import model.*;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class GestorPersistencia {


    private static volatile GestorPersistencia instancia;

    private GestorPersistencia() {}   

    public static GestorPersistencia getInstance() {
        if (instancia == null) {
            synchronized (GestorPersistencia.class) {
                if (instancia == null) instancia = new GestorPersistencia();
            }
        }
        return instancia;
    }


    private static final String CARPETA_PARTIDAS  = "partidas";
    private static final String ARCHIVO_RESULTADOS = "resultados.txt";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    /**
     * Guarda el estado completo del motor en  partidas/<nombreArchivo>.ygo
     * @param motor  estado actual del juego
     * @param nombreArchivo  nombre base del archivo (sin extensión)
     * @throws IOException  si hay error de escritura
     */
    public static void guardarPartida(MotorJuego motor, String nombreArchivo) throws IOException {
        File carpeta = new File(CARPETA_PARTIDAS);
        if (!carpeta.exists()) carpeta.mkdirs();

        File archivo = new File(carpeta, nombreArchivo + ".ygo");
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(archivo)))) {


            pw.println("[TURNO]");
            pw.println("numero=" + motor.getNumeroTurno());
            pw.println("activo=" + motor.getActivo().getNombre());
            pw.println("primerJugador=" + motor.getPrimerJugador().getNombre());
            pw.println("yaJugoCarta=" + motor.yaJugoUnaCarta());
            pw.println("yaAtaco=" + motor.yaAtaco());
            pw.println();


            escribirJugador(pw, motor.getJugador1(), "J1");
            pw.println();


            escribirJugador(pw, motor.getJugador2(), "J2");
        }
    }

    private static void escribirJugador(PrintWriter pw, Jugador j, String tag) {
        pw.println("[" + tag + "]");
        pw.println("nombre=" + j.getNombre());
        pw.println("lp=" + j.getLp());


        pw.print("mazo=");
        Stack<Carta> mazo = j.getMazo();

        List<String> mazoNombres = new ArrayList<>();

        Deque<String> temp = new ArrayDeque<>();
        for (Carta c : mazo) temp.push(c.getNombre());     
        for (String n : temp) mazoNombres.add(n);
        pw.println(String.join(",", mazoNombres));


        pw.print("mano=");
        List<String> manoNombres = new ArrayList<>();
        for (Carta c : j.getMano()) manoNombres.add(c.getNombre());
        pw.println(String.join(",", manoNombres));


        pw.print("campo=");
        List<String> campoStr = new ArrayList<>();
        for (Monstruo m : j.getCampo()) {
            campoStr.add(m.getNombre() + ":" + m.getAtk() + ":" + m.getDef()
                    + ":" + m.getNivel() + ":" + m.getPosicion() + ":" + m.puedeAtacar());
        }
        pw.println(String.join("|", campoStr));


        pw.print("trampas=");
        List<String> trampaNames = new ArrayList<>();
        for (Trampa t : j.getTrampas()) trampaNames.add(t.getNombre());
        pw.println(String.join(",", trampaNames));


        pw.print("invocados=");
        pw.println(String.join(",", j.getMonstruosInvocados()));
    }



    /**
     * Carga una partida desde  partidas/<nombreArchivo>.ygo
     * @throws IOException  si el archivo no existe o su formato es inválido
     */
    public static MotorJuego cargarPartida(String nombreArchivo) throws IOException {
        File archivo = new File(CARPETA_PARTIDAS, nombreArchivo + ".ygo");
        if (!archivo.exists()) throw new FileNotFoundException("No se encontró la partida: " + archivo.getPath());

        Map<String, Map<String, String>> secciones = parsearArchivo(archivo);

        Map<String, String> secTurno = secciones.getOrDefault("[TURNO]", Collections.emptyMap());
        Map<String, String> secJ1    = secciones.getOrDefault("[J1]",    Collections.emptyMap());
        Map<String, String> secJ2    = secciones.getOrDefault("[J2]",    Collections.emptyMap());

        Jugador j1 = reconstruirJugador(secJ1);
        Jugador j2 = reconstruirJugador(secJ2);

        int     numeroTurno     = Integer.parseInt(secTurno.getOrDefault("numero", "1"));
        String  nombreActivo    = secTurno.getOrDefault("activo", j1.getNombre());
        String  nombrePrimero   = secTurno.getOrDefault("primerJugador", j1.getNombre());
        boolean yaJugoCarta     = Boolean.parseBoolean(secTurno.getOrDefault("yaJugoCarta", "false"));
        boolean yaAtaco         = Boolean.parseBoolean(secTurno.getOrDefault("yaAtaco", "false"));

        Jugador activo       = nombreActivo.equals(j1.getNombre())  ? j1 : j2;
        Jugador primerJugador = nombrePrimero.equals(j1.getNombre()) ? j1 : j2;

        return new MotorJuego(j1, j2, activo, j1 == activo ? j2 : j1,
                              numeroTurno, primerJugador, yaJugoCarta, yaAtaco);
    }

    private static Jugador reconstruirJugador(Map<String, String> sec) {
        String nombre = sec.getOrDefault("nombre", "Jugador");
        int    lp     = Integer.parseInt(sec.getOrDefault("lp", "8000"));

        Jugador j = new Jugador(nombre);

        if (lp < 8000) j.recibirDanio(8000 - lp);
        else if (lp > 8000) j.recuperarLp(lp - 8000);


        String mazoStr = sec.getOrDefault("mazo", "");
        if (!mazoStr.isBlank()) {
            String[] mazoNombres = mazoStr.split(",");

            for (int i = mazoNombres.length - 1; i >= 0; i--) {
                Carta c = buscarCarta(mazoNombres[i].trim());
                if (c != null) j.agregarAlMazo(c);
            }
        }


        String manoStr = sec.getOrDefault("mano", "");
        if (!manoStr.isBlank()) {
            for (String nombre2 : manoStr.split(",")) {
                Carta c = buscarCarta(nombre2.trim());
                if (c != null) j.getMano().add(c);
            }
        }


        String campoStr = sec.getOrDefault("campo", "");
        if (!campoStr.isBlank()) {
            for (String entry : campoStr.split("\\|")) {
                if (entry.isBlank()) continue;
                String[] partes = entry.split(":");
                if (partes.length < 6) continue;
                String   mNombre   = partes[0];
                int      atk       = Integer.parseInt(partes[1]);
                int      def       = Integer.parseInt(partes[2]);
                int      nivel     = Integer.parseInt(partes[3]);
                Posicion pos       = Posicion.valueOf(partes[4]);
                boolean  pAtacar   = Boolean.parseBoolean(partes[5]);
                Monstruo m = new Monstruo(mNombre, atk, def, nivel);
                m.setPosicion(pos);
                if (pAtacar) m.habilitarAtaque();
                j.invocarMonstruo(m);
            }
        }


        String trampaStr = sec.getOrDefault("trampas", "");
        if (!trampaStr.isBlank()) {
            for (String tn : trampaStr.split(",")) {
                Carta c = buscarCarta(tn.trim());
                if (c instanceof Trampa) j.colocarTrampa((Trampa) c);
            }
        }


        String invocadosStr = sec.getOrDefault("invocados", "");
        if (!invocadosStr.isBlank()) {
            for (String inv : invocadosStr.split(",")) {
                if (!inv.isBlank()) j.getMonstruosInvocados().add(inv.trim());
            }
        }

        return j;
    }


    private static Carta buscarCarta(String nombre) {

        return FabricaCartas.porNombre(nombre);
    }


    private static Map<String, Map<String, String>> parsearArchivo(File archivo) throws IOException {
        Map<String, Map<String, String>> resultado = new LinkedHashMap<>();
        String seccionActual = null;
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.strip();
                if (linea.isEmpty() || linea.startsWith("#")) continue;
                if (linea.startsWith("[") && linea.endsWith("]")) {
                    seccionActual = linea;
                    resultado.putIfAbsent(seccionActual, new LinkedHashMap<>());
                } else if (seccionActual != null && linea.contains("=")) {
                    int eq = linea.indexOf('=');
                    String clave = linea.substring(0, eq).strip();
                    String valor = linea.substring(eq + 1).strip();
                    resultado.get(seccionActual).put(clave, valor);
                }
            }
        }
        return resultado;
    }



    public static List<String> listarPartidas() {
        File carpeta = new File(CARPETA_PARTIDAS);
        List<String> nombres = new ArrayList<>();
        if (!carpeta.exists()) return nombres;
        for (File f : Objects.requireNonNull(carpeta.listFiles())) {
            if (f.getName().endsWith(".ygo")) {
                nombres.add(f.getName().replace(".ygo", ""));
            }
        }
        return nombres;
    }


    public static void registrarResultado(MotorJuego motor) {
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(ARCHIVO_RESULTADOS, true)))) {
            String fecha    = LocalDateTime.now().format(FMT);
            String j1       = motor.getJugador1().getNombre();
            String j2       = motor.getJugador2().getNombre();
            String ganador  = motor.getGanador() != null ? motor.getGanador().getNombre() : "EMPATE";
            int    turnos   = motor.getNumeroTurno();
            int    lpJ1     = motor.getJugador1().getLp();
            int    lpJ2     = motor.getJugador2().getLp();
            pw.println(fecha + "|" + j1 + "|" + j2 + "|" + ganador + "|" + turnos + "|" + lpJ1 + "|" + lpJ2);
        } catch (IOException e) {
            System.err.println("[Persistencia] No se pudo escribir el resultado: " + e.getMessage());
        }
    }


    public static String leerEstadisticas() {
        File f = new File(ARCHIVO_RESULTADOS);
        if (!f.exists()) return "No hay resultados registrados aún.";

        Map<String, Integer> victorias = new TreeMap<>();
        int totalPartidas = 0;
        int maxTurnos = 0;
        int minTurnos = Integer.MAX_VALUE;
        String partidaMasLarga  = "";
        String partidaMasCorta  = "";

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.strip();
                if (linea.isEmpty()) continue;
                String[] partes = linea.split("\\|");
                if (partes.length < 7) continue;

                totalPartidas++;
                String ganador = partes[3];
                int    turnos  = Integer.parseInt(partes[4]);

                victorias.merge(ganador, 1, Integer::sum);

                if (turnos > maxTurnos) {
                    maxTurnos = turnos;
                    partidaMasLarga = partes[0] + " — " + partes[1] + " vs " + partes[2] + " (" + turnos + " turnos)";
                }
                if (turnos < minTurnos) {
                    minTurnos = turnos;
                    partidaMasCorta = partes[0] + " — " + partes[1] + " vs " + partes[2] + " (" + turnos + " turnos)";
                }
            }
        } catch (IOException e) {
            return "Error al leer estadísticas: " + e.getMessage();
        }

        if (totalPartidas == 0) return "No hay resultados registrados aún.";

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════\n");
        sb.append("        ESTADÍSTICAS HISTÓRICAS        \n");
        sb.append("═══════════════════════════════════════\n");
        sb.append("Total de partidas: ").append(totalPartidas).append("\n\n");
        sb.append("Victorias por duelista:\n");
        victorias.forEach((nombre, wins) ->
            sb.append("  ").append(nombre).append(": ").append(wins).append(" victoria(s)\n")
        );
        sb.append("\nPartida más larga:  ").append(partidaMasLarga).append("\n");
        if (minTurnos != Integer.MAX_VALUE)
        sb.append("Partida más corta:  ").append(partidaMasCorta).append("\n");
        sb.append("═══════════════════════════════════════\n");
        return sb.toString();
    }
}
