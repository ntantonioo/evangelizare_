package ui;


import java.util.Scanner;

public class TelaPrincipal {

    public static void main(String[] args) {
        TelaPrincipal tela = new TelaPrincipal();
        tela.iniciar();
    }

    public void iniciar() {
        Scanner scanner = new Scanner(System.in);
        int opcao;

        System.out.println("================================");
        System.out.println("       EVANGELIZARE");
        System.out.println("  Sistema de Apoio à Catequese");
        System.out.println("================================");

        System.out.println("Bem-vindo ao Evangelizare!");

        do {
            System.out.println("\n========== MENU PRINCIPAL ==========");
            System.out.println("1 - Cadastrar Usuário");
            System.out.println("2 - Cadastrar Turma");
            System.out.println("3 - Notícias");
            System.out.println("0 - Sair");
            System.out.print("Escolha uma opção: ");

            opcao = scanner.nextInt();

            switch (opcao) {
                case 1:
                    cadastrarUsuario(scanner);
                    break;

                case 2:
                    //TODO
                    break;

                case 3:
                    //TODO
                    break;

                case 0:
                    //TODO
                    break;

                default:
                    System.out.println("Opção inválida");
            }

        } while (opcao != 0);

        scanner.close();
    }

    private void cadastrarUsuario(Scanner scanner) {
        System.out.println("\\n========== CADASTRO DE USUÁRIO ==========");
        System.out.println("Selecione o perfil do usuário:");
        System.out.println("1 - Catequista");
        System.out.println("2 - Catequisando");
        System.out.println("3 - Administrador");
        System.out.println("0 - Voltar ao menu principal");
        System.out.println("Escolha uma opção: ");

        int perfil = scanner.nextInt();

        switch (perfil) {
            case 1:
                System.out.println("Perfil selecionado: Catequista");
                // TODO: cadastrar catequista
                break;

            case 2:
                System.out.println("Perfil selecionado: Catequisando");
                // TODO: cadastrar catequisando
                break;

            case 3:
                System.out.println("Perfil selecionado: Administrador");
                // TODO: cadastrar administrador
                break;

            case 0:
                break;

                default:
                    System.out.println("Opção de perfil inválida!");
        }
    }
}