package ui;

import java.nio.file.Path;

import model.Administrador;
import persistencia.PersistenciaAutomatica;
import persistencia.PersistenciaException;
import persistencia.RepositorioSqlite;
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

        SistemaEvangelizareMap sistemaBase = new SistemaEvangelizareMap();
        AtividadeServiceMap atividadesBase = new AtividadeServiceMap(sistemaBase);

        RepositorioSqlite repositorio;
        PersistenciaAutomatica persistencia;

        try {
            repositorio = RepositorioSqlite.abrir(caminhoDoBanco());
            persistencia = new PersistenciaAutomatica(repositorio, sistemaBase, atividadesBase);
            persistencia.carregar();
        } catch (PersistenciaException e) {
            System.err.println("Não foi possível abrir o banco de dados: " + e.getMessage());
            System.err.println("O sistema não foi iniciado, para não colocar os dados em risco.");
            System.exit(1);
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(repositorio::close));

        SistemaEvangelizare sistema =
                persistencia.envolver(SistemaEvangelizare.class, sistemaBase);
        AtividadeService atividadeService =
                persistencia.envolver(AtividadeService.class, atividadesBase);

        Console console = new Console();

        criarAdministradorPeloAmbiente(sistema);

        //TELAS

        TelaCatequista telaCatequista = new TelaCatequista(sistema, console);
        TelaCatequisando telaCatequisando = new TelaCatequisando(sistema, console);
        TelaTurma telaTurma = new TelaTurma(sistema, console);
        TelaNoticia telaNoticia = new TelaNoticia(sistema, console);

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

    // Atalho para desenvolvimento: enquanto os dados ficam só na memória, o
    // primeiro acesso se repete a cada execução. Se a variável de ambiente
    // EVANGELIZARE_ADMIN_SENHA estiver definida, o administrador é criado com ela
    // (ID em EVANGELIZARE_ADMIN_ID, ou ADM001). A senha segue a política de senhas.
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

    private static Path caminhoDoBanco() {
        String configurado = System.getenv("EVANGELIZARE_BANCO");

        if (configurado == null || configurado.isBlank()) {
            return Path.of("dados", "evangelizare.db");
        }

        return Path.of(configurado.trim());
    }
}