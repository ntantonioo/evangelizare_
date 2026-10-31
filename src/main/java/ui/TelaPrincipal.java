package ui;

import model.*;
import service.SistemaEvangelizare;
import service.SistemaEvangelizareMap;

public class TelaPrincipal {

    private final SistemaEvangelizare sistema =
            new SistemaEvangelizareMap();

    private final Console console = new Console();

    private final TelaTurma telaTurma =
            new TelaTurma(sistema, console);

    private final TelaNoticia telaNoticia =
            new TelaNoticia(sistema, console);

    private final TelaCatequista telaCatequista =
            new TelaCatequista(sistema, console);

    private final TelaCatequisando telaCatequisando =
            new TelaCatequisando(sistema, console);

    public static void main(String[] args) {
        TelaPrincipal tela = new TelaPrincipal();
        tela.iniciar();
    }

    public void iniciar() {
        cadastrarAdministradorInicial();

        int opcao;

        System.out.println("================================");
        System.out.println("       EVANGELIZARE");
        System.out.println("  Sistema de Apoio à Catequese");
        System.out.println("================================");
        System.out.println("Bem-vindo ao Evangelizare!");

        // MENU PRINCIPAL

        do {
            System.out.println("\n========== MENU PRINCIPAL ==========");
            System.out.println("1 - Entrar");
            System.out.println("0 - Sair");
            System.out.print("Escolha uma opção: ");

            opcao = console.lerInt();

            switch (opcao) {
                case 1:
                    autentificarUsuario();
                    break;

                case 0:
                    System.out.println(
                            "Ide por todo o mundo e pregai o evangelho a toda criatura."
                    );
                    break;

                default:
                    System.out.println("Opção inválida.");
            }

        } while (opcao != 0);
    }

    // AUTENTIFICAÇÃO DO USUARIO

    private void autentificarUsuario() {

        System.out.println("\n========== LOGIN ==========");

        System.out.print("ID do usuário: ");
        String id = console.lerTexto();

        System.out.print("Senha: ");
        String senha = console.lerTexto();

        if (sistema.autenticarUsuario(id, senha)) {
            Usuario usuario = sistema.pesquisarUsuarioPorId(id);

            System.out.println("\nLogin realizado com sucesso!");
            System.out.println("Bem-vindo(a), " + usuario.getNome() + "!");

            if (usuario instanceof Administrador) {
                menuAdministrador(usuario);

            } else if (usuario instanceof Catequista catequista) {
                System.out.println("Perfil: Catequista");
                menuCatequista(catequista);

            } else if (usuario instanceof Catequisando catequisando) {
                System.out.println("Perfil: Catequisando");
                menuCatequisando(catequisando);
            }

        } else {
            System.out.println("\nID ou senha incorretos.");
        }
    }

    // MENU DO ADMINISTRADOR

    private void menuAdministrador(Usuario usuarioLogado) {
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

                case 0:
                    System.out.println("Sessão encerrada");
                    break;

                default:
                    System.out.println("Opção inválida");
            }
        } while (opcao != 0);
    }

    // MENU CATEQUISTA

    private void menuCatequista(Catequista catequista) {
        int opcao;

        do {
            System.out.println("\n========== MENU DO CATEQUISTA ==========");
            System.out.println("1 - Cadastrar Catequisando");
            System.out.println("2 - Consultar Catequisandos");
            System.out.println("3 - Editar Catequisando");
            System.out.println("4 - Remover Catequisando");
            System.out.println("5 - Notícias");
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

                case 0:
                    System.out.println("Sessão encerrada");
                    break;

                default:
                    System.out.println("Opção inválida.");
            }

        } while (opcao != 0);
    }

    //MENU CATEQUISANDO

    private void menuCatequisando(Catequisando catequisando) {

        int opcao;

        do {
            System.out.println("\n========== MENU DO CATEQUISANDO ==========");
            System.out.println("1 - Consultar minha turma");
            System.out.println("2 - Consultar notícias");
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

                case 0:
                    System.out.println("Sessão encerrada.");
                    break;

                default:
                    System.out.println("Opção inválida.");
            }

        } while (opcao != 0);
    }

    private void cadastrarAdministradorInicial() {
        Administrador administrador = new Administrador(
                "ADM001",
                "Administrador",
                "83999999999",
                30,
                "admin123"
        );

        sistema.cadastrarAdministrador(administrador);
    }
}