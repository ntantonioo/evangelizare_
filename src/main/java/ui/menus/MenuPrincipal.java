package ui.menus;

import model.*;
import service.SistemaEvangelizare;
import ui.Console;

public class MenuPrincipal {

    private final SistemaEvangelizare sistema;
    private final Console console;
    private final MenuAdministrador menuAdministrador;
    private final MenuCatequista menuCatequista;
    private final MenuCatequisando menuCatequisando;

    public MenuPrincipal(SistemaEvangelizare sistema, Console console, MenuAdministrador menuAdministrador, MenuCatequista menuCatequista, MenuCatequisando menuCatequisando) {
        this.sistema = sistema;
        this.console = console;
        this.menuAdministrador = menuAdministrador;
        this.menuCatequista = menuCatequista;
        this.menuCatequisando = menuCatequisando;
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
                menuAdministrador.exibir(usuario);

            } else if (usuario instanceof Catequista catequista) {
                System.out.println("Perfil: Catequista");
                menuCatequista.exibir(catequista);

            } else if (usuario instanceof Catequisando catequisando) {
                System.out.println("Perfil: Catequisando");
                menuCatequisando.exibir(catequisando);
            }

        } else {
            System.out.println("\nID ou senha incorretos.");
        }
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