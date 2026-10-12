package ui;

import java.util.Scanner;

import model.Administrador;
import model.Catequista;
import model.Catequisando;
import model.Usuario;
import service.SistemaEvangelizare;
import service.SistemaEvangelizareMap;

public class TelaPrincipal {

    private final SistemaEvangelizare sistema =
            new SistemaEvangelizareMap();

    public static void main(String[] args) {
        TelaPrincipal tela = new TelaPrincipal();
        tela.iniciar();
    }

    public void iniciar() {
        cadastrarAdministradorInicial();

        Scanner scanner = new Scanner(System.in);
        int opcao;

        System.out.println("================================");
        System.out.println("       EVANGELIZARE");
        System.out.println("  Sistema de Apoio à Catequese");
        System.out.println("================================");
        System.out.println("Bem-vindo ao Evangelizare!");

        do {
            System.out.println("\n========== MENU PRINCIPAL ==========");
            System.out.println("1 - Entrar");
            System.out.println("2 - Notícias");
            System.out.println("0 - Sair");
            System.out.print("Escolha uma opção: ");

            opcao = scanner.nextInt();

            switch (opcao) {
                case 1:
                    autentificarUsuario(scanner);
                    break;

                case 2:
                    System.out.println(
                            "Funcionalidade de notícias em desenvolvimento."
                    );
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

        scanner.close();
    }

    private void autentificarUsuario(Scanner scanner) {
        scanner.nextLine();

        System.out.println("\n========== LOGIN ==========");

        System.out.print("ID do usuário: ");
        String id = scanner.nextLine();

        System.out.print("Senha: ");
        String senha = scanner.nextLine();

        if (sistema.autenticarUsuario(id, senha)) {
            Usuario usuario = sistema.pesquisarUsuarioPorId(id);

            System.out.println("\nLogin realizado com sucesso!");
            System.out.println("Bem-vindo(a), " + usuario.getNome() + "!");

            if (usuario instanceof Administrador) {
                System.out.println("Perfil: Administrador");

            } else if (usuario instanceof Catequista) {
                System.out.println("Perfil: Catequista");

            } else if (usuario instanceof Catequisando) {
                System.out.println("Perfil: Catequisando");
            }

        } else {
            System.out.println("\nID ou senha incorretos.");
        }
    }

    private void cadastrarUsuario(Scanner scanner) {
        System.out.println("\n========== CADASTRO DE USUÁRIO ==========");
        System.out.println("1 - Catequista");
        System.out.println("2 - Catequisando");
        System.out.println("3 - Administrador");
        System.out.println("0 - Voltar");
        System.out.print("Escolha uma opção: ");

        int perfil = scanner.nextInt();

        switch (perfil) {
            case 1:
                System.out.println("Perfil selecionado: Catequista.");
                //TODO
                break;

            case 2:
                System.out.println("Perfil selecionado: Catequisando.");
                //TODO
                break;

            case 3:
                System.out.println("Perfil selecionado: Administrador.");
                //TODO
                break;

            case 0:
                break;

            default:
                System.out.println("Opção de perfil inválida.");
        }
    }

    private void cadastrarTurma(Scanner scanner) {
        System.out.println("\n========== CADASTRO DE TURMA ==========");
        System.out.println("1 - Catequese Infantil");
        System.out.println("2 - Crisma Jovem");
        System.out.println("3 - Crisma Adulto");
        System.out.println("0 - Voltar");
        System.out.print("Escolha uma opção: ");

        int etapaTurma = scanner.nextInt();

        switch (etapaTurma) {
            case 1:
                System.out.println("Etapa selecionada: Catequese Infantil.");
                //TODO
                break;

            case 2:
                System.out.println("Etapa selecionada: Crisma Jovem.");
                //TODO
                break;

            case 3:
                System.out.println("Etapa selecionada: Crisma Adulto.");
                //TODO
                break;

            case 0:
                break;

            default:
                System.out.println("Opção inválida.");
        }
    }

    private void cadastrarAdministradorInicial() {
        Administrador administrador = new Administrador(
                "ADM001",
                "Administrador",
                30,
                "admin123"
        );

        sistema.cadastrarAdministrador(administrador);
    }
}
