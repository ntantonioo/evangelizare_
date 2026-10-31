package ui;

import service.SistemaEvangelizare;
import service.SistemaEvangelizareMap;
import ui.menus.MenuAdministrador;
import ui.menus.MenuCatequisando;
import ui.menus.MenuCatequista;
import ui.menus.MenuPrincipal;
import ui.telas.TelaCatequisando;
import ui.telas.TelaCatequista;
import ui.telas.TelaNoticia;
import ui.telas.TelaTurma;

public class Main {

    public static void main(String[] args) {
        SistemaEvangelizare sistema = new SistemaEvangelizareMap();
        Console console = new Console();

        TelaCatequista telaCatequista = new TelaCatequista(sistema, console);
        TelaCatequisando telaCatequisando = new TelaCatequisando(sistema, console);
        TelaTurma telaTurma = new TelaTurma(sistema, console);
        TelaNoticia telaNoticia = new TelaNoticia(sistema, console);

        MenuAdministrador menuAdministrador = new MenuAdministrador(
                console, telaCatequista, telaTurma, telaNoticia, telaCatequisando);
        MenuCatequista menuCatequista = new MenuCatequista(
                console, telaCatequisando, telaNoticia);
        MenuCatequisando menuCatequisando = new MenuCatequisando(
                console, telaCatequisando, telaNoticia);

        new MenuPrincipal(
                sistema, console, menuAdministrador, menuCatequista, menuCatequisando
        ).iniciar();
    }
}