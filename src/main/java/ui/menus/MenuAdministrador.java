package ui.menus;

import model.Usuario;
import ui.Console;
import ui.telas.*;

public class MenuAdministrador {

    private final Console console;
    private final TelaCatequista telaCatequista;
    private final TelaTurma telaTurma;
    private final TelaNoticia telaNoticia;
    private final TelaCatequisando telaCatequisando;
    private final TelaAtividade telaAtividade;
    private final TelaSenha telaSenha;

    public MenuAdministrador(
            Console console,
            TelaCatequista telaCatequista,
            TelaTurma telaTurma,
            TelaNoticia telaNoticia,
            TelaCatequisando telaCatequisando,
            TelaAtividade telaAtividade,
            TelaSenha telaSenha) {
        this.console = console;
        this.telaCatequista = telaCatequista;
        this.telaTurma = telaTurma;
        this.telaNoticia = telaNoticia;
        this.telaCatequisando = telaCatequisando;
        this.telaAtividade = telaAtividade;
        this.telaSenha = telaSenha;
    }

    public void exibir(Usuario administrador) {
        int opcao;

        do {
            System.out.println("\n========== MENU DO ADMINISTRADOR ==========");
            System.out.println("1 - Cadastrar usuário (catequista ou administrador)");
            System.out.println("2 - Catequisandos");
            System.out.println("3 - Turmas");
            System.out.println("4 - Catequistas");
            System.out.println("5 - Notícias");
            System.out.println("6 - Atividades");
            System.out.println("7 - Redefinir senha de usuário");
            System.out.println("8 - Alterar minha senha");
            System.out.println("0 - Encerrar sessão");
            System.out.print("Escolha uma opção: ");

            opcao = console.lerInt();

            switch (opcao) {
                case 1:
                    telaCatequista.cadastrarUsuario(administrador);
                    break;

                case 2:
                    menuCatequisandos(administrador);
                    break;

                case 3:
                    menuTurmas(administrador);
                    break;

                case 4:
                    menuCatequistas(administrador);
                    break;

                case 5:
                    telaNoticia.menu(administrador);
                    break;

                case 6:
                    telaAtividade.menu(administrador);
                    break;

                case 7:
                    telaSenha.redefinir(administrador);
                    break;

                case 8:
                    telaSenha.alterar(administrador);
                    break;

                case 0:
                    System.out.println("Sessão encerrada");
                    break;

                default:
                    System.out.println("Opção inválida");
            }
        } while (opcao != 0);
    }

    private void menuCatequisandos(Usuario administrador) {
        int opcao;

        do {
            System.out.println("\n========== CATEQUISANDOS ==========");
            System.out.println("1 - Cadastrar catequisando");
            System.out.println("2 - Consultar catequisandos de uma turma");
            System.out.println("3 - Editar catequisando");
            System.out.println("4 - Remover catequisando");
            System.out.println("5 - Transferir catequisando de turma");
            System.out.println("0 - Voltar");
            System.out.print("Escolha uma opção: ");

            opcao = console.lerInt();

            switch (opcao) {
                case 1:
                    telaCatequisando.cadastrar(administrador);
                    break;

                case 2:
                    telaCatequisando.consultar(administrador);
                    break;

                case 3:
                    telaCatequisando.editar(administrador);
                    break;

                case 4:
                    telaCatequisando.remover(administrador);
                    break;

                case 5:
                    telaCatequisando.transferir();
                    break;

                case 0:
                    break;

                default:
                    System.out.println("Opção inválida");
            }
        } while (opcao != 0);
    }

    private void menuTurmas(Usuario administrador) {
        int opcao;

        do {
            System.out.println("\n========== TURMAS ==========");
            System.out.println("1 - Visão geral (catequistas, catequisandos e responsáveis)");
            System.out.println("2 - Cadastrar turma");
            System.out.println("3 - Editar turma");
            System.out.println("0 - Voltar");
            System.out.print("Escolha uma opção: ");

            opcao = console.lerInt();

            switch (opcao) {
                case 1:
                    telaTurma.visaoGeral(administrador);
                    break;

                case 2:
                    telaTurma.cadastrar();
                    break;

                case 3:
                    telaTurma.editar();
                    break;

                case 0:
                    break;

                default:
                    System.out.println("Opção inválida");
            }
        } while (opcao != 0);
    }

    private void menuCatequistas(Usuario administrador) {
        int opcao;

        do {
            System.out.println("\n========== CATEQUISTAS ==========");
            System.out.println("1 - Editar catequista");
            System.out.println("2 - Transferir catequista de turma");
            System.out.println("3 - Coordenação (permissões de administrador)");
            System.out.println("0 - Voltar");
            System.out.print("Escolha uma opção: ");

            opcao = console.lerInt();

            switch (opcao) {
                case 1:
                    telaCatequista.editar();
                    break;

                case 2:
                    telaCatequista.transferir();
                    break;

                case 3:
                    telaCatequista.gerenciarCoordenacao(administrador);
                    break;

                case 0:
                    break;

                default:
                    System.out.println("Opção inválida");
            }
        } while (opcao != 0);
    }
}