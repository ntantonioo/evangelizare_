package ui.menus;

import model.Responsavel;
import ui.Console;
import ui.telas.TelaResponsavel;

public class MenuResponsavel {

    private final Console console;
    private final TelaResponsavel telaResponsavel;

    public MenuResponsavel(Console console, TelaResponsavel telaResponsavel) {
        this.console = console;
        this.telaResponsavel = telaResponsavel;
    }

    public void exibir(Responsavel responsavel) {
        int opcao;

        do {
            System.out.println("\n========== MENU DO RESPONSÁVEL ==========");
            System.out.println("1 - Meus filhos");
            System.out.println("2 - Consultar notícias");
            System.out.println("3 - Consultar atividades");
            System.out.println("0 - Encerrar sessão");
            System.out.print("Escolha uma opção: ");

            opcao = console.lerInt();

            switch (opcao) {
                case 1:
                    telaResponsavel.consultarFilhos(responsavel);
                    break;

                case 2:
                    telaResponsavel.consultarNoticias(responsavel);
                    break;

                case 3:
                    telaResponsavel.consultarAtividades(responsavel);
                    break;

                case 0:
                    System.out.println("Sessão encerrada.");
                    break;

                default:
                    System.out.println("Opção inválida.");
            }

        } while (opcao != 0);
    }
}