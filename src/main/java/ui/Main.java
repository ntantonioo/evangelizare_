package ui;

import model.Administrador;
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

        criarAdministradorPeloAmbiente(sistema);

        //TELAS

        TelaCatequista telaCatequista = new TelaCatequista(sistema, console);
        TelaCatequisando telaCatequisando = new TelaCatequisando(sistema, console);
        TelaTurma telaTurma = new TelaTurma(sistema, console);
        TelaNoticia telaNoticia = new TelaNoticia(sistema, console);

        AtividadeService atividadeService = new AtividadeServiceMap(sistema);
        TelaAtividade telaAtividade = new TelaAtividade(sistema, atividadeService, console);

        ResponsavelService responsavelService = new ResponsavelServiceMap(sistema, atividadeService);
        TelaResponsavel telaResponsavel = new TelaResponsavel(responsavelService);

        TelaSenha telaSenha = new TelaSenha(sistema, console);

        //MENUS

        MenuAdministrador menuAdministrador = new MenuAdministrador(
                console, telaCatequista, telaTurma, telaNoticia, telaCatequisando, telaAtividade, telaSenha);
        MenuCatequista menuCatequista = new MenuCatequista(
                console, telaCatequisando, telaNoticia, telaAtividade, telaSenha);
        MenuCatequisando menuCatequisando = new MenuCatequisando(console, telaCatequisando, telaNoticia, telaAtividade, telaSenha);
        MenuResponsavel menuResponsavel = new MenuResponsavel(console, telaResponsavel, telaSenha);

        new MenuPrincipal(
                sistema, console, menuAdministrador, menuCatequista, menuCatequisando, menuResponsavel, telaSenha
        ).iniciar();
    }

    private static void criarAdministradorPeloAmbiente(SistemaEvangelizare sistema) {
        String senha = System.getenv("EVANGELIZARE_ADMIN_SENHA");

        if (senha == null || senha.isBlank() || sistema.existeAdministrador()) {
            return;
        }

        String id = System.getenv("EVANGELIZARE_ADMIN_ID");

        if (id == null || id.isBlank()) {
            id = "ADM001";
        }

        try {
            sistema.cadastrarAdministradorInicial(
                    new Administrador(id.trim(), "Administrador", "00000000000", 30, senha)
            );
        } catch (IllegalArgumentException e) {
            System.err.println(
                    "EVANGELIZARE_ADMIN_SENHA ignorada: " + e.getMessage()
            );
        }
    }
}