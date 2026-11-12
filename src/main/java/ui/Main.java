package ui;

import service.SistemaEvangelizare;
import service.SistemaEvangelizareMap;
import service.atividade.AtividadeService;
import service.atividade.AtividadeServiceMap;
import service.responsavel.ResponsavelService;
import service.responsavel.ResponsavelServiceMap;
import ui.menus.MenuAdministrador;
import ui.menus.MenuCatequisando;
import ui.menus.MenuCatequista;
import ui.menus.MenuPrincipal;
import ui.menus.MenuResponsavel;
import ui.telas.*;

public class Main {

    public static void main(String[] args) {
        SistemaEvangelizare sistema = new SistemaEvangelizareMap();
        Console console = new Console();

        //TELAS

        TelaCatequista telaCatequista = new TelaCatequista(sistema, console);
        TelaCatequisando telaCatequisando = new TelaCatequisando(sistema, console);
        TelaTurma telaTurma = new TelaTurma(sistema, console);
        TelaNoticia telaNoticia = new TelaNoticia(sistema, console);

        AtividadeService atividadeService = new AtividadeServiceMap(sistema);
        TelaAtividade telaAtividade = new TelaAtividade(sistema, atividadeService, console);

        ResponsavelService responsavelService = new ResponsavelServiceMap(sistema, atividadeService);
        TelaResponsavel telaResponsavel = new TelaResponsavel(responsavelService);

        //MENUS

        MenuAdministrador menuAdministrador = new MenuAdministrador(
                console, telaCatequista, telaTurma, telaNoticia, telaCatequisando, telaAtividade);
        MenuCatequista menuCatequista = new MenuCatequista(
                console, telaCatequisando, telaNoticia, telaAtividade);
        MenuCatequisando menuCatequisando = new MenuCatequisando(console, telaCatequisando, telaNoticia, telaAtividade);
        MenuResponsavel menuResponsavel = new MenuResponsavel(console, telaResponsavel);

        new MenuPrincipal(
                sistema, console, menuAdministrador, menuCatequista, menuCatequisando, menuResponsavel
        ).iniciar();
    }
}