package model;

import java.util.*;
public class FabricaCartas {
    private FabricaCartas() {}
    public static List<Carta> crearMazoCompleto() {
        List<Carta> mazo = new ArrayList<>();
        mazo.addAll(crearMonstruos());
        mazo.addAll(crearMagicas());
        mazo.addAll(crearTrampas());
        return mazo;
    }
    private static List<Carta> crearMonstruos() {
        List<Carta> lista = new ArrayList<>();
        lista.add(new Monstruo("Slime del Pantano",     300,  200, 1));
        lista.add(new Monstruo("Rata del Laberinto",    550,  400, 2));
        lista.add(new Monstruo("Goblin Ladrón",         600,  500, 2));
        lista.add(new Monstruo("Guerrero Humilde",     1200,  800, 3));
        lista.add(new Monstruo("Maga de la Luna",      1000, 1200, 3));
        lista.add(new Monstruo("Arquero del Viento",   1100,  900, 3));
        lista.add(new Monstruo("Golem de Piedra",       800, 1500, 3));
        lista.add(new Monstruo("Espectro Errante",     1300,  700, 3));
        lista.add(new Monstruo("Guerrero Llameante",   1500, 1200, 4));
        lista.add(new Monstruo("Kuriboh Evolucionado", 1400, 1300, 4));
        lista.add(new Monstruo("Jinzo Jr.",            1300, 1100, 4));
        lista.add(new Monstruo("Señor del Tiempo",     1600,  900, 4));
        lista.add(new Monstruo("Dragón Bebé",          1400, 1000, 4));
        lista.add(new Monstruo("Mago de la Oscuridad", 1700, 1400, 4));
        lista.add(new Monstruo("Caballero Celestial",  2000, 1600, 5));
        lista.add(new Monstruo("Bestia de las Sombras",1900, 1700, 5));
        lista.add(new Monstruo("Fénix Ardiente",       2100, 1200, 5));
        lista.add(new Monstruo("Dragón del Caos",      2200, 1800, 6));
        lista.add(new Monstruo("Espectro Oscuro",      2300, 1500, 6));
        lista.add(new Monstruo("Golem de Hierro",      1800, 2400, 6));
        lista.add(new Monstruo("Mago Oscuro",          2500, 2100, 7));
        lista.add(new Monstruo("Dragón Blanco",        3000, 2500, 7));
        lista.add(new Monstruo("Exodia el Prohibido",  2800, 2400, 7));
        lista.add(new Monstruo("Dragón Azul de Ojos Blancos", 3000, 2500, 8));
        lista.add(new Monstruo("Señor de los Dragones",       2800, 2300, 8));
        lista.add(new Monstruo("Arcángel Bahamut",            2600, 2700, 8));
        lista.add(new Monstruo("Fénix Divino",               3200, 2700, 9));
        lista.add(new Monstruo("Coloso Eterno",              3100, 3000, 9));
        lista.add(new Monstruo("Dragón Supremo",             3500, 3000, 11));
        lista.add(new Monstruo("Dios del Abismo",            4000, 3500, 12));
        return lista;
    }
    private static List<Carta> crearMagicas() {
        List<Carta> lista = new ArrayList<>();
        lista.add(new Magica(
            "Olla de la Codicia",
            "Robas 2 cartas de tu mazo.",
            (u, o) -> { u.robarCarta(); u.robarCarta(); }
        ));
        lista.add(new Magica(
            "Dian Keto la Curadora",
            "Recuperas 1000 LP.",
            (u, o) -> {
                u.recuperarLp(1000);
                System.out.println(u.getNombre() + " recupera 1000 LP. LP actual: " + u.getLp());
            }
        ));
        lista.add(new Magica(
            "Trueno del Cielo",
            "Destruye el monstruo con menos ATK del oponente.",
            (u, o) -> {
                if (!o.tieneMonstruos()) {
                    System.out.println("El oponente no tiene monstruos en campo.");
                    return;
                }
                Monstruo objetivo = o.getCampo().get(0);
                for (Monstruo m : o.getCampo()) {
                    if (m.getAtk() < objetivo.getAtk()) objetivo = m;
                }
                System.out.println(objetivo.getNombre() + " fue destruido por Trueno del Cielo.");
                o.removerMonstruo(objetivo);
            }
        ));
        lista.add(new Magica(
            "Axe of Despair",
            "El monstruo con más ATK propio gana 1000 ATK este turno.",
            (u, o) -> {
                if (!u.tieneMonstruos()) {
                    System.out.println("No tienes monstruos en campo.");
                    return;
                }
                Monstruo objetivo = u.getCampo().get(0);
                for (Monstruo m : u.getCampo()) {
                    if (m.getAtk() > objetivo.getAtk()) objetivo = m;
                }
                objetivo.aumentarAtk(1000);
                System.out.println(objetivo.getNombre() + " ahora tiene ATK: " + objetivo.getAtk());
            }
        ));
        lista.add(new Magica(
            "Misil de Fuego",
            "El oponente recibe 800 puntos de daño directo.",
            (u, o) -> {
                o.recibirDanio(800);
                System.out.println(o.getNombre() + " recibe 800 de daño. LP: " + o.getLp());
            }
        ));
        lista.add(new Magica(
            "Tierra Arrasada",
            "Destruye todos los monstruos del oponente.",
            (u, o) -> {
                if (!o.tieneMonstruos()) {
                    System.out.println("El oponente no tiene monstruos.");
                    return;
                }
                System.out.println("¡Todos los monstruos de " + o.getNombre() + " son destruidos!");
                o.limpiarCampo();
            }
        ));
        lista.add(new Magica(
            "Libro de la Luna",
            "El monstruo rival con más ATK pasa a posición de DEFENSA.",
            (u, o) -> {
                if (!o.tieneMonstruos()) {
                    System.out.println("El oponente no tiene monstruos.");
                    return;
                }
                Monstruo objetivo = o.getCampo().get(0);
                for (Monstruo m : o.getCampo()) {
                    if (m.getAtk() > objetivo.getAtk()) objetivo = m;
                }
                objetivo.setPosicion(Posicion.DEFENSA);
                objetivo.agotarAtaque();
                System.out.println(objetivo.getNombre() + " ahora está en DEFENSA.");
            }
        ));
        lista.add(new Magica(
            "Torbellino Oscuro",
            "Robas 1 carta y el oponente recibe 500 de daño.",
            (u, o) -> {
                u.robarCarta();
                o.recibirDanio(500);
                System.out.println(u.getNombre() + " roba 1 carta. " + o.getNombre() + " recibe 500 de daño.");
            }
        ));
        lista.add(new Magica(
            "Oferta de Sangre",
            "Descartas la primera carta de tu mano y recuperas 2000 LP.",
            (u, o) -> {
                if (u.getMano().isEmpty()) {
                    System.out.println("No tienes cartas en mano para descartar.");
                    return;
                }
                Carta descartada = u.getMano().remove(0);
                u.recuperarLp(2000);
                System.out.println("Descartaste " + descartada.getNombre() + " y recuperaste 2000 LP.");
            }
        ));
        lista.add(new Magica(
            "Equilibrio Roto",
            "Si tienes menos LP que el oponente, le infliges la diferencia como daño.",
            (u, o) -> {
                if (u.getLp() < o.getLp()) {
                    int diferencia = o.getLp() - u.getLp();
                    o.recibirDanio(diferencia);
                    System.out.println("¡Diferencia de " + diferencia + " aplicada a " + o.getNombre() + "! LP: " + o.getLp());
                } else {
                    System.out.println("No tienes menos LP que el oponente. Sin efecto.");
                }
            }
        ));
        return lista;
    }
    private static List<Carta> crearTrampas() {
        List<Carta> lista = new ArrayList<>();
        lista.add(new Trampa(
            "Cilindro Mágico",
            "Cuando el oponente ataca, refleja el ATK del atacante como daño a su LP.",
            CondicionTrampa.EN_ATAQUE,
            (u, o) -> {
                if (!o.tieneMonstruos()) return;
                Monstruo atacante = o.getCampo().get(o.getCampo().size() - 1);
                o.recibirDanio(atacante.getAtk());
                System.out.println(o.getNombre() + " recibe " + atacante.getAtk() + " de daño reflejado. LP: " + o.getLp());
            }
        ));
        lista.add(new Trampa(
            "Fuerza de Espejo",
            "Cuando el oponente ataca, destruye todos sus monstruos en posición de ataque.",
            CondicionTrampa.EN_ATAQUE,
            (u, o) -> {
                List<Monstruo> aDestruir = new ArrayList<>();
                for (Monstruo m : o.getCampo()) {
                    if (m.getPosicion() == Posicion.ATAQUE) aDestruir.add(m);
                }
                for (Monstruo m : aDestruir) o.removerMonstruo(m);
                System.out.println("¡Todos los monstruos atacantes de " + o.getNombre() + " destruidos!");
            }
        ));
        lista.add(new Trampa(
            "Trampa del Agujero",
            "El monstruo que ataca pierde 1500 ATK hasta el fin del turno.",
            CondicionTrampa.EN_ATAQUE,
            (u, o) -> {
                if (!o.tieneMonstruos()) return;
                Monstruo atacante = null;
                for (Monstruo m : o.getCampo()) {
                    if (!m.puedeAtacar()) { atacante = m; break; }
                }
                if (atacante == null) atacante = o.getCampo().get(o.getCampo().size() - 1);
                int reduccion = Math.min(1500, atacante.getAtk());
                atacante.aumentarAtk(-reduccion);
                System.out.println(atacante.getNombre() + " pierde 1500 ATK. ATK actual: " + atacante.getAtk());
            }
        ));
        lista.add(new Trampa(
            "Agujero Trampa",
            "Cuando el oponente invoca un monstruo con ATK ≥ 1000, lo destruye.",
            CondicionTrampa.AL_INVOCAR,
            (u, o) -> {
                if (!o.tieneMonstruos()) return;
                Monstruo recienInvocado = o.getCampo().get(o.getCampo().size() - 1);
                if (recienInvocado.getAtk() >= 1000) {
                    o.removerMonstruo(recienInvocado);
                    System.out.println(recienInvocado.getNombre() + " fue destruido al ser invocado (ATK ≥ 1000).");
                } else {
                    System.out.println("El monstruo invocado tiene ATK < 1000, la trampa no aplica.");
                }
            }
        ));
        lista.add(new Trampa(
            "Trampa Dimensional",
            "Cuando el oponente invoca un monstruo, lo regresa a su mano.",
            CondicionTrampa.AL_INVOCAR,
            (u, o) -> {
                if (!o.tieneMonstruos()) return;
                Monstruo recienInvocado = o.getCampo().get(o.getCampo().size() - 1);
                o.removerMonstruo(recienInvocado);
                o.getMano().add(recienInvocado);
                System.out.println(recienInvocado.getNombre() + " regresó a la mano de " + o.getNombre() + ".");
            }
        ));
        lista.add(new Trampa(
            "Escudo Divino",
            "Cuando vayas a recibir daño de combate, lo reduces a la mitad.",
            CondicionTrampa.AL_RECIBIR_DANIO,
            (u, o) -> {
                System.out.println(u.getNombre() + " activa Escudo Divino: el daño se reduce a la mitad.");
            }
        ));
        lista.add(new Trampa(
            "Espejo del Alma",
            "Cuando vayas a recibir daño, el oponente recibe la misma cantidad.",
            CondicionTrampa.AL_RECIBIR_DANIO,
            (u, o) -> {
                System.out.println(u.getNombre() + " activa Espejo del Alma: el oponente también recibirá el daño.");
            }
        ));
        lista.add(new Trampa(
            "Juicio Solemne",
            "Pagas la mitad de tus LP para limpiar el campo del oponente.",
            CondicionTrampa.INMEDIATA,
            (u, o) -> {
                int costo = u.getLp() / 2;
                u.recibirDanio(costo);
                o.limpiarCampo();
                System.out.println(u.getNombre() + " paga " + costo + " LP. Campo de " + o.getNombre() + " limpiado.");
            }
        ));
        lista.add(new Trampa(
            "Tributo Torrencial",
            "Cuando un monstruo es invocado, destruye todos los monstruos en campo.",
            CondicionTrampa.INMEDIATA,
            (u, o) -> {
                u.limpiarCampo();
                o.limpiarCampo();
                System.out.println("¡Tributo Torrencial! Todos los monstruos en campo fueron destruidos.");
            }
        ));
        lista.add(new Trampa(
            "Ataque Imprudente",
            "Robas 2 cartas pero pierdes 1000 LP.",
            CondicionTrampa.INMEDIATA,
            (u, o) -> {
                u.recibirDanio(1000);
                u.robarCarta();
                u.robarCarta();
                System.out.println(u.getNombre() + " pierde 1000 LP y roba 2 cartas.");
            }
        ));
        return lista;
    }
}
