package patterns;

import model.Jugador;
import model.Monstruo;
import model.Posicion;
import model.Carta;

import java.util.ArrayList;
import java.util.List;


public class Efectos {

    private Efectos() {} 


    public static class RobarDosCartas implements EstrategiaEfecto {
        @Override public void activar(Jugador usuario, Jugador oponente) {
            usuario.robarCarta();
            usuario.robarCarta();
            System.out.println(usuario.getNombre() + " roba 2 cartas.");
        }
        @Override public String getNombreEfecto() { return "RobarDosCartas"; }
        @Override public String getDescripcion()  { return "Robas 2 cartas de tu mazo."; }
    }


    public static class Curar1000 implements EstrategiaEfecto {
        @Override public void activar(Jugador usuario, Jugador oponente) {
            usuario.recuperarLp(1000);
            System.out.println(usuario.getNombre() + " recupera 1000 LP. LP: " + usuario.getLp());
        }
        @Override public String getNombreEfecto() { return "Curar1000"; }
        @Override public String getDescripcion()  { return "Recuperas 1000 LP."; }
    }


    public static class DanoDirecto800 implements EstrategiaEfecto {
        @Override public void activar(Jugador usuario, Jugador oponente) {
            oponente.recibirDanio(800);
            System.out.println(oponente.getNombre() + " recibe 800 de daño. LP: " + oponente.getLp());
        }
        @Override public String getNombreEfecto() { return "DanoDirecto800"; }
        @Override public String getDescripcion()  { return "El oponente recibe 800 puntos de daño directo."; }
    }


    public static class DanoDirecto500RobarUna implements EstrategiaEfecto {
        @Override public void activar(Jugador usuario, Jugador oponente) {
            usuario.robarCarta();
            oponente.recibirDanio(500);
            System.out.println(usuario.getNombre() + " roba 1 carta. "
                    + oponente.getNombre() + " recibe 500 de daño.");
        }
        @Override public String getNombreEfecto() { return "DanoDirecto500RobarUna"; }
        @Override public String getDescripcion()  { return "Robas 1 carta y el oponente recibe 500 de daño."; }
    }


    public static class DestruirTodosCampoOponente implements EstrategiaEfecto {
        @Override public void activar(Jugador usuario, Jugador oponente) {
            if (!oponente.tieneMonstruos()) {
                System.out.println("El oponente no tiene monstruos en campo.");
                return;
            }
            System.out.println("¡Todos los monstruos de " + oponente.getNombre() + " son destruidos!");
            oponente.limpiarCampo();
        }
        @Override public String getNombreEfecto() { return "DestruirTodosCampoOponente"; }
        @Override public String getDescripcion()  { return "Destruye todos los monstruos del oponente."; }
    }


    public static class DestruirMonstruoMenorAtk implements EstrategiaEfecto {
        @Override public void activar(Jugador usuario, Jugador oponente) {
            if (!oponente.tieneMonstruos()) {
                System.out.println("El oponente no tiene monstruos en campo.");
                return;
            }
            Monstruo objetivo = oponente.getCampo().get(0);
            for (Monstruo m : oponente.getCampo()) {
                if (m.getAtk() < objetivo.getAtk()) objetivo = m;
            }
            System.out.println(objetivo.getNombre() + " fue destruido.");
            oponente.removerMonstruo(objetivo);
        }
        @Override public String getNombreEfecto() { return "DestruirMonstruoMenorAtk"; }
        @Override public String getDescripcion()  { return "Destruye el monstruo con menos ATK del oponente."; }
    }


    public static class AumentarAtkMasFuerte1000 implements EstrategiaEfecto {
        @Override public void activar(Jugador usuario, Jugador oponente) {
            if (!usuario.tieneMonstruos()) {
                System.out.println("No tienes monstruos en campo.");
                return;
            }
            Monstruo objetivo = usuario.getCampo().get(0);
            for (Monstruo m : usuario.getCampo()) {
                if (m.getAtk() > objetivo.getAtk()) objetivo = m;
            }
            objetivo.aumentarAtk(1000);
            System.out.println(objetivo.getNombre() + " ahora tiene ATK: " + objetivo.getAtk());
        }
        @Override public String getNombreEfecto() { return "AumentarAtkMasFuerte1000"; }
        @Override public String getDescripcion()  { return "El monstruo con más ATK propio gana 1000 ATK."; }
    }


    public static class PasarMonstruoMasFuerteDefensa implements EstrategiaEfecto {
        @Override public void activar(Jugador usuario, Jugador oponente) {
            if (!oponente.tieneMonstruos()) {
                System.out.println("El oponente no tiene monstruos.");
                return;
            }
            Monstruo objetivo = oponente.getCampo().get(0);
            for (Monstruo m : oponente.getCampo()) {
                if (m.getAtk() > objetivo.getAtk()) objetivo = m;
            }
            objetivo.setPosicion(Posicion.DEFENSA);
            objetivo.agotarAtaque();
            System.out.println(objetivo.getNombre() + " ahora está en DEFENSA.");
        }
        @Override public String getNombreEfecto() { return "PasarMonstruoMasFuerteDefensa"; }
        @Override public String getDescripcion()  { return "El monstruo rival con más ATK pasa a DEFENSA."; }
    }


    public static class DescartarRecuperar2000 implements EstrategiaEfecto {
        @Override public void activar(Jugador usuario, Jugador oponente) {
            if (usuario.getMano().isEmpty()) {
                System.out.println("No tienes cartas en mano para descartar.");
                return;
            }
            Carta descartada = usuario.getMano().remove(0);
            usuario.recuperarLp(2000);
            System.out.println("Descartaste " + descartada.getNombre() + " y recuperaste 2000 LP.");
        }
        @Override public String getNombreEfecto() { return "DescartarRecuperar2000"; }
        @Override public String getDescripcion()  { return "Descartas la primera carta y recuperas 2000 LP."; }
    }


    public static class DanoPorDiferenciaLP implements EstrategiaEfecto {
        @Override public void activar(Jugador usuario, Jugador oponente) {
            if (usuario.getLp() < oponente.getLp()) {
                int diferencia = oponente.getLp() - usuario.getLp();
                oponente.recibirDanio(diferencia);
                System.out.println("¡Diferencia de " + diferencia + " aplicada a "
                        + oponente.getNombre() + "! LP: " + oponente.getLp());
            } else {
                System.out.println("No tienes menos LP. Sin efecto.");
            }
        }
        @Override public String getNombreEfecto() { return "DanoPorDiferenciaLP"; }
        @Override public String getDescripcion()  { return "Si tienes menos LP, el oponente recibe la diferencia."; }
    }


    public static class ReflejarAtaqueComoLpDano implements EstrategiaEfecto {
        @Override public void activar(Jugador usuario, Jugador oponente) {
            if (!oponente.tieneMonstruos()) return;
            Monstruo atacante = oponente.getCampo().get(oponente.getCampo().size() - 1);
            oponente.recibirDanio(atacante.getAtk());
            System.out.println(oponente.getNombre() + " recibe " + atacante.getAtk()
                    + " de daño reflejado. LP: " + oponente.getLp());
        }
        @Override public String getNombreEfecto() { return "ReflejarAtaqueComoLpDano"; }
        @Override public String getDescripcion()  { return "Refleja el ATK del atacante como daño."; }
    }


    public static class DestruirMonstruosAtaqueOponente implements EstrategiaEfecto {
        @Override public void activar(Jugador usuario, Jugador oponente) {
            List<Monstruo> aDestruir = new ArrayList<>();
            for (Monstruo m : oponente.getCampo()) {
                if (m.getPosicion() == Posicion.ATAQUE) aDestruir.add(m);
            }
            for (Monstruo m : aDestruir) oponente.removerMonstruo(m);
            System.out.println("¡Todos los monstruos atacantes de " + oponente.getNombre() + " destruidos!");
        }
        @Override public String getNombreEfecto() { return "DestruirMonstruosAtaqueOponente"; }
        @Override public String getDescripcion()  { return "Destruye todos los monstruos atacantes del oponente."; }
    }


    public static class ReducirAtacanteAtk1500 implements EstrategiaEfecto {
        @Override public void activar(Jugador usuario, Jugador oponente) {
            if (!oponente.tieneMonstruos()) return;
            Monstruo atacante = oponente.getCampo().get(oponente.getCampo().size() - 1);
            int reduccion = Math.min(1500, atacante.getAtk());
            atacante.aumentarAtk(-reduccion);
            System.out.println(atacante.getNombre() + " pierde 1500 ATK. ATK actual: " + atacante.getAtk());
        }
        @Override public String getNombreEfecto() { return "ReducirAtacanteAtk1500"; }
        @Override public String getDescripcion()  { return "El monstruo que ataca pierde 1500 ATK."; }
    }


    public static class DestruirInvocadoConAtk1000 implements EstrategiaEfecto {
        @Override public void activar(Jugador usuario, Jugador oponente) {
            if (!oponente.tieneMonstruos()) return;
            Monstruo recien = oponente.getCampo().get(oponente.getCampo().size() - 1);
            if (recien.getAtk() >= 1000) {
                oponente.removerMonstruo(recien);
                System.out.println(recien.getNombre() + " destruido al ser invocado (ATK ≥ 1000).");
            } else {
                System.out.println("ATK < 1000, trampa no aplica.");
            }
        }
        @Override public String getNombreEfecto() { return "DestruirInvocadoConAtk1000"; }
        @Override public String getDescripcion()  { return "Destruye el monstruo invocado si ATK ≥ 1000."; }
    }


    public static class RegresarInvocadoAMano implements EstrategiaEfecto {
        @Override public void activar(Jugador usuario, Jugador oponente) {
            if (!oponente.tieneMonstruos()) return;
            Monstruo recien = oponente.getCampo().get(oponente.getCampo().size() - 1);
            oponente.removerMonstruo(recien);
            oponente.getMano().add(recien);
            System.out.println(recien.getNombre() + " regresó a la mano de " + oponente.getNombre() + ".");
        }
        @Override public String getNombreEfecto() { return "RegresarInvocadoAMano"; }
        @Override public String getDescripcion()  { return "El monstruo invocado regresa a la mano."; }
    }


    public static class EscudoReducirDanioAMitad implements EstrategiaEfecto {
        @Override public void activar(Jugador usuario, Jugador oponente) {
            System.out.println(usuario.getNombre() + " activa Escudo Divino: el daño se reduce a la mitad.");
        }
        @Override public String getNombreEfecto() { return "EscudoReducirDanioAMitad"; }
        @Override public String getDescripcion()  { return "El daño de combate entrante se reduce a la mitad."; }
    }


    public static class EspejoReflectarDanio implements EstrategiaEfecto {
        @Override public void activar(Jugador usuario, Jugador oponente) {
            System.out.println(usuario.getNombre() + " activa Espejo del Alma: el oponente también recibirá el daño.");
        }
        @Override public String getNombreEfecto() { return "EspejoReflectarDanio"; }
        @Override public String getDescripcion()  { return "El oponente recibe la misma cantidad de daño."; }
    }


    public static class PagarMitadLpLimpiarCampo implements EstrategiaEfecto {
        @Override public void activar(Jugador usuario, Jugador oponente) {
            int costo = usuario.getLp() / 2;
            usuario.recibirDanio(costo);
            oponente.limpiarCampo();
            System.out.println(usuario.getNombre() + " paga " + costo
                    + " LP. Campo de " + oponente.getNombre() + " limpiado.");
        }
        @Override public String getNombreEfecto() { return "PagarMitadLpLimpiarCampo"; }
        @Override public String getDescripcion()  { return "Pagas la mitad de LP para limpiar el campo rival."; }
    }


    public static class DestruirTodosLosMonstruos implements EstrategiaEfecto {
        @Override public void activar(Jugador usuario, Jugador oponente) {
            usuario.limpiarCampo();
            oponente.limpiarCampo();
            System.out.println("¡Tributo Torrencial! Todos los monstruos destruidos.");
        }
        @Override public String getNombreEfecto() { return "DestruirTodosLosMonstruos"; }
        @Override public String getDescripcion()  { return "Destruye todos los monstruos en ambos campos."; }
    }


    public static class Perder1000LpRobarDos implements EstrategiaEfecto {
        @Override public void activar(Jugador usuario, Jugador oponente) {
            usuario.recibirDanio(1000);
            usuario.robarCarta();
            usuario.robarCarta();
            System.out.println(usuario.getNombre() + " pierde 1000 LP y roba 2 cartas.");
        }
        @Override public String getNombreEfecto() { return "Perder1000LpRobarDos"; }
        @Override public String getDescripcion()  { return "Pierdes 1000 LP y robas 2 cartas."; }
    }
}
