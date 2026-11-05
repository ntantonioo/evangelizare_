package ui.menus;

import model.*;
import ui.Console;
import ui.telas.TelaAtividade;
import ui.telas.TelaCatequisando;
import ui.telas.TelaNoticia;

public class MenuCatequista {

    private final Console console;
    private final TelaCatequisando telaCatequisando;
    private final TelaNoticia telaNoticia;
    private final TelaAtividade telaAtividade;

    public MenuCatequista(Console console, TelaCatequisando telaCatequisando, TelaNoticia
            telaNoticia, TelaAtividade telaAtividade) {
        this.console = console;
        this.telaCatequisando = telaCatequisando;
        this.telaNoticia = telaNoticia;
        this.telaAtividade = telaAtividade;
    }

    public void exibir(Catequista catequista) {
        int opcao;

        do {
            System.out.println("\n========== MENU DO CATEQUISTA ==========");
            System.out.println("1 - Cadastrar Catequisando");
            System.out.println("2 - Consultar Catequisandos");
            System.out.println("3 - Editar Catequisando");
            System.out.println("4 - Remover Catequisando");
            System.out.println("5 - Notícias");
            System.out.println("6 - Atividades");
            System.out.println("0 - Encerrar sessão");
            System.out.print("Escolha uma opção: ");

            opcao = console.lerInt();

            switch (opcao) {
                case 1:
                    telaCatequisando.cadastrar(catequista);
                    break;

                case 2:
                    telaCatequisando.consultar(catequista);
                    break;

                case 3:
                    telaCatequisando.editar(catequista);
                    break;

                case 4:
                    telaCatequisando.remover(catequista);
                    break;

                case 5:
                    telaNoticia.menu(catequista);
                    break;

                case 6:
                    telaAtividade.menu(catequista);
                    break;

                case 0:
                    System.out.println("Sessão encerrada");
                    break;

                default:
                    System.out.println("Opção inválida.");
            }

        } while (opcao != 0);
    }
}