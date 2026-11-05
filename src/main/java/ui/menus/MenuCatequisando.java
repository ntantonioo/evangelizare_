package ui.menus;

import model.*;
import ui.Console;
import ui.telas.TelaAtividade;
import ui.telas.TelaCatequisando;
import ui.telas.TelaNoticia;

public class MenuCatequisando {

    private final Console console;
    private final TelaCatequisando telaCatequisando;
    private final TelaNoticia telaNoticia;
    private final TelaAtividade telaAtividade;

    public MenuCatequisando(Console console, TelaCatequisando telaCatequisando, TelaNoticia
            telaNoticia, TelaAtividade telaAtividade) {
        this.console = console;
        this.telaCatequisando = telaCatequisando;
        this.telaNoticia = telaNoticia;
        this.telaAtividade = telaAtividade;
    }

    public void exibir(Catequisando catequisando) {

        int opcao;

        do {
            System.out.println("\n========== MENU DO CATEQUISANDO ==========");
            System.out.println("1 - Consultar minha turma");
            System.out.println("2 - Consultar notícias");
            System.out.println("3 - Consultar atividades");
            System.out.println("0 - Encerrar sessão");
            System.out.print("Escolha uma opção: ");

            opcao = console.lerInt();

            switch (opcao) {
                case 1:
                    telaCatequisando.consultarMinhaTurma(catequisando);
                    break;

                case 2:
                    telaNoticia.consultarDisponiveis(catequisando);
                    break;

                case 3:
                    telaAtividade.consultarDisponiveis(catequisando);
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