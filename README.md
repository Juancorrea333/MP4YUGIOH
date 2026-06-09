# MP4YUGIOH
# Yu-Gi-Oh! — Mini Proyecto 4
**Programación Orientada a Eventos · Java 21 · MVC**

---

## ¿Qué es el juego?

Simulador del juego de cartas Yu-Gi-Oh! para dos jugadores. Cada uno comienza con **8.000 LP** y un mazo de 25 cartas. Por turno, el jugador activo puede invocar monstruos, jugar magias, colocar trampas, atacar y guardar la partida. Gana quien deje al oponente en **0 LP** o lo haga quedarse sin cartas.

Ejecutable en dos modos: **interfaz gráfica (Swing)** o **consola**.

---

## Cómo ejecutar

**Requisito:** Java 21 instalado. Sin dependencias externas.

```bash
# Compilar
mkdir -p out
javac -d out -sourcepath src src/Main.java

# Ejecutar en modo gráfico (por defecto)
java -cp out Main

# Ejecutar en consola
java -cp out Main consola
```

El menú principal permite: nuevo duelo, cargar partida guardada y ver estadísticas históricas.

---

## RF1 — Estructuras de datos

Se incorporaron las seis estructuras posibles. A continuación la justificación de cada una, con su ubicación exacta en el código.

| Estructura | Archivo · Línea de declaración | Por qué se usó aquí y no otra cosa |
|---|---|---|
| `Stack<Carta>` — **Pila** | `src/model/Jugador.java` · línea 12 | El mazo es LIFO por definición del juego: siempre se roba del tope (`mazo.pop()`, línea 48) y se apila con `mazo.push()` (línea 43). Un `ArrayList` obligaría a calcular y mantener el índice del tope manualmente, lo que introduce errores y no expresa la semántica LIFO. |
| `LinkedList<Carta>` — **Lista enlazada** | `src/model/Jugador.java` · línea 15 | La mano crece por el final (`mano.add()`, línea 49) y pierde cartas en cualquier posición (`mano.remove(indice)`, línea 57). `LinkedList` realiza ambas operaciones en O(1) sin desplazar elementos; un `ArrayList` desplazaría en O(n) cada vez que se juega una carta del centro. |
| `HashSet<String>` — **Conjunto** | `src/model/Jugador.java` · línea 17 | Lleva control de los monstruos ya invocados en el duelo. La unicidad es una invariante del juego (no se puede invocar el mismo monstruo dos veces), y `HashSet` la garantiza por diseño. La pertenencia se verifica en O(1); una lista requeriría iterar en O(n) con `contains()`. |
| `HashMap<String, Carta>` — **Tabla Hash** | `src/model/Jugador.java` · línea 20 | Índice de la mano del jugador: permite buscar cualquier carta por nombre en O(1) (`buscarEnMano()`, línea 53). Sin este mapa, el controlador tendría que recorrer la `LinkedList` entera cada vez que el jugador selecciona una carta. También se usa en `RegistroCartas` como catálogo global del juego. |
| `TreeMap<Integer, Monstruo>` — **Árbol** | `src/model/MotorJuego.java` · línea 32 | Mantiene los monstruos del campo ordenados por ATK usando un árbol rojo-negro internamente. Consultar el monstruo más fuerte (`lastEntry()`, línea 317) o más débil (`firstEntry()`, línea 324) es O(log n) y sin reordenamiento manual. Un `ArrayList` ordenado requeriría `Collections.sort()` en cada cambio del campo (O(n log n)). |
| `Queue<String>` — **Cola** | `src/model/MotorJuego.java` · línea 26 | Los eventos del turno (invocaciones, ataques, efectos) deben procesarse en el orden exacto en que ocurren (FIFO). Se encolan con `offer()` (línea 174) y se desencolan con `poll()` (línea 182) para alimentar el log de la vista y resolver efectos encadenados en el orden correcto. Una pila invertiría ese orden. |

---

## RF2 — Persistencia en archivos de texto

Toda la lógica de persistencia está centralizada en `src/persistencia/GestorPersistencia.java`. La clase es un Singleton con Double-Checked Locking (líneas 13–24) para garantizar un único punto de escritura. No se usa serialización binaria ni librerías externas; solo `java.io` (`PrintWriter`, `BufferedReader`, `FileWriter`).

### Guardar partida en curso
Método: `guardarPartida(MotorJuego motor, String nombreArchivo)` — línea 39.  
Llamado desde el controlador en `src/controller/Controlador.java` líneas 251 y 266 (el jugador puede guardar en cualquier momento del duelo).  
Escribe el estado completo en `partidas/<nombreArchivo>.ygo`, un archivo de texto plano legible en cualquier editor:

```
[TURNO]
numero=7
activo=Yugi
primerJugador=Yugi
yaJugoCarta=false
yaAtaco=false

[J1]
nombre=Yugi
lp=5200
mazo=Jinzo,Dark Magician,...
mano=Pot of Greed,Trap Hole
campo=Dark Magician:2500:2100:7:ATAQUE:true
trampas=Mirror Force
invocados=Dark Magician,Jinzo

[J2]
nombre=Kaiba
lp=3400
...
```

El campo se serializa como `nombre:atk:def:nivel:posicion:puedeAtacar` separado por `|`; mazo, mano, trampas e invocados van como listas separadas por comas.

### Cargar partida
Método: `cargarPartida(String nombreArchivo)` — línea 112.  
Llamado desde `src/controller/Controlador.java` líneas 59 y 291 (accesible desde el menú principal).  
El método parsea el archivo `.ygo` sección por sección (`parsearArchivo()`, línea 211), reconstruye ambos `Jugador` con su mazo, mano, campo, trampas e invocados, y devuelve un `MotorJuego` en exactamente el mismo estado en que se guardó.

### Listar partidas disponibles
Método: `listarPartidas()` — línea 238.  
Llamado desde `src/controller/Controlador.java` líneas 50 y 277.  
Recorre la carpeta `partidas/` y devuelve los nombres de todos los archivos `.ygo` para que el menú los muestre al jugador.

### Historial de resultados
Método: `registrarResultado(MotorJuego motor)` — línea 251.  
Llamado automáticamente al terminar cada duelo desde `src/controller/Controlador.java` línea 321.  
Agrega al final de `resultados.txt` una línea con formato de campos separados por `|`:

```
2025-06-07 14:32:11|Yugi|Kaiba|Yugi|12|4100|0
```

Los campos son: `fecha/hora | jugador1 | jugador2 | ganador | turnos | LP_J1 | LP_J2`.

### Estadísticas históricas
Método: `leerEstadisticas()` — línea 267.  
Llamado desde `src/controller/Controlador.java` línea 308 al seleccionar "Estadísticas" en el menú principal.  
Lee `resultados.txt` línea por línea y calcula: total de partidas jugadas, victorias por duelista (usando un `TreeMap<String, Integer>` interno para mostrarlas en orden alfabético), partida más larga y partida más corta. Ejemplo de salida:

```
═══════════════════════════════════════
        ESTADÍSTICAS HISTÓRICAS
═══════════════════════════════════════
Total de partidas: 5

Victorias por duelista:
  Kaiba: 2 victoria(s)
  Yugi: 3 victoria(s)

Partida más larga:  2025-06-07 14:32:11 — Yugi vs Kaiba (12 turnos)
Partida más corta:  2025-06-07 10:15:44 — Kaiba vs Yugi (4 turnos)
═══════════════════════════════════════
```

---

## RF3 — Patrones de Diseño + Reflection

### Observer
`src/patterns/ObservadorJuego.java` (interfaz) · `src/patterns/SujetoJuego.java` (interfaz) · `src/model/MotorJuego.java` (implementa `SujetoJuego`, línea 9) · `src/view/VentanaDuelo.java` (implementa `ObservadorJuego`, línea 21)

`MotorJuego` notifica a sus observadores (`notificarObservadores()`, línea 92) tras cada cambio de estado (LP, campo, turno — llamadas en líneas 126 y 288). `VentanaDuelo` se registra como observador y actualiza toda la interfaz automáticamente, sin que el modelo conozca nada de Swing.

### Command
`src/patterns/Comando.java` (interfaz) · `src/patterns/Comandos.java` (implementaciones) · `src/patterns/GestorComandos.java` · `src/controller/Controlador.java` (uso en líneas 143, 166, 201, 211)

Cada acción del jugador (jugar carta, atacar) es un objeto con `execute()` y `undo()`. `GestorComandos` apila los comandos ejecutados; si el jugador elige "Deshacer" (`Controlador`, línea 219), se llama `undo()` sobre el último comando y el estado se restaura.

### Memento
`src/patterns/MementoJuego.java` · `src/patterns/GestorMemento.java` · `src/controller/Controlador.java` (instanciado en línea 22)

`MementoJuego` captura una instantánea inmutable del estado completo (mazo, mano, campo, LP y turno de ambos jugadores). El sistema de persistencia se apoya en esta lógica para serializar y reconstruir partidas desde archivo.

### Factory Method
`src/model/FabricaCartas.java` · `src/patterns/FabricaCartasReflection.java`

`FabricaCartas` centraliza la creación estática de cartas predefinidas sin `new` sueltos en el resto del código. `FabricaCartasReflection` extiende esto con carga dinámica por Reflection (ver sección siguiente).

### Strategy
`src/patterns/EstrategiaEfecto.java` (interfaz) · `src/patterns/Efectos.java` (implementaciones)

Cada efecto de carta (curar LP, robar cartas, daño directo, destruir monstruo) es una clase interna que implementa `EstrategiaEfecto`. El motor llama a `efecto.activar(usuario, oponente)` sin saber qué algoritmo ejecutará. Agregar un efecto nuevo no requiere tocar ningún `switch`.

### Singleton
`src/patterns/RegistroCartas.java` (línea 11) · `src/persistencia/GestorPersistencia.java` (línea 11)

Ambas clases usan Double-Checked Locking con `volatile` para garantizar una única instancia hilo-segura. Se aplicó solo donde tiene sentido: el catálogo de cartas (una sola fuente de verdad) y el gestor de archivos (un solo punto de escritura).

---

## Reflection API

**Archivo:** `src/patterns/FabricaCartasReflection.java` — **Datos:** `cartas/cartas_dinamicas.txt`

Las cartas mágicas y trampa se definen en texto plano:
```
# Tipo|Nombre|Descripcion|ClaseEfecto
MAGICA|Pot of Greed|Roba 2 cartas de tu mazo.|RobarDosCartas
TRAMPA|Mirror Force|Destruye monstruos atacantes.|DestruirMonstruosAtacantes
```

Al cargar (`cargarDesdeArchivo()`, línea 39), se instancia el efecto en tiempo de ejecución con (líneas 176–179):

```java
Class<?> clazz = Class.forName("patterns.Efectos$" + claseEfecto);
Constructor<?> ctor  = clazz.getDeclaredConstructor();
EstrategiaEfecto efecto = (EstrategiaEfecto) ctor.newInstance();
```

**Resultado:** agregar una carta nueva solo requiere editar el archivo `.txt` y crear la clase de efecto. No se recompila ningún `switch`, no se modifica ningún otro archivo.

---

## Tablero KanbanFlow

🔗 [https://kanbanflow.com/board/tVcBN9R](https://kanbanflow.com/board/tVcBN9R)

Columnas: **Backlog → To Do → In Progress → Review → Done**. Tareas distribuidas entre los tres integrantes, cada una vinculada a un RF del proyecto.

---

## Declaración de uso de IA

Se usó IA generativa exclusivamente para resolver dudas conceptuales sobre la Reflection API y el patrón Memento. El código fue escrito íntegramente por el equipo.

---

*Programación Orientada a Eventos — Java 21 · Continuación del MP2 y MP3*
