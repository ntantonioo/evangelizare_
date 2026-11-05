package ui.menus;

import model.*;
import ui.*;
import ui.telas.*;

public class MenuAdministrador {

    private final Console console;
    private final TelaCatequista telaCatequista;
    private final TelaTurma telaTurma;
    private final TelaNoticia telaNoticia;
    private final TelaCatequisando telaCatequisando;
    private final TelaAtividade telaAtividade;

    public MenuAdministrador(Console console, TelaCatequista telaCatequista, TelaTurma
            telaTurma, TelaNoticia telaNoticia, TelaCatequisando telaCatequisando, TelaAtividade
            telaAtividade) {
        this.console = console;
        this.telaCatequista = telaCatequista;
        this.telaTurma = telaTurma;
        this.telaNoticia = telaNoticia;
        this.telaCatequisando = telaCatequisando;
        this.telaAtividade = telaAtividade;
    }

    public void exibir(Usuario usuarioLogado) {
        int opcao;

        do {
            System.out.println("\n========== MENU DO ADMINISTRADOR ==========");
            System.out.println("1 - Cadastrar Usuário");
            System.out.println("2 - Cadastrar Turma");
            System.out.println("3 - Editar Catequista");
            System.out.println("4 - Notícias");
            System.out.println("5 - Editar Turma");
            System.out.println("6 - Transferir Catequisando");
            System.out.println("7 - Transferir Catequista");
            System.out.println("8 - Atividades");
            System.out.println("0 - Encerrar sessão");
            System.out.print("Escolha uma opção: ");

            opcao = console.lerInt();

            switch (opcao) {
                case 1:
                    telaCatequista.cadastrarUsuario();
                    break;

                case 2:
                    telaTurma.cadastrar();
                    break;

                case 3:
                    telaCatequista.editar();
                    break;

                case 4:
                    telaNoticia.menu(usuarioLogado);
                    break;

                case 5:
                    telaTurma.editar();
                    break;

                case 6:
                    telaCatequisando.transferir();
                    break;

                case 7:
                    telaCatequista.transferir();
                    break;

                case 8:
                    telaAtividade.menu(usuarioLogado);
                    break;

                case 0:
                    System.out.println("Sessão encerrada");
                    break;

                default:
                    System.out.println("Opção inválida");
            }
        } while (opcao != 0);
    }
}