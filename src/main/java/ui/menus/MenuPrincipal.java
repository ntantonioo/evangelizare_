package ui.menus;

import model.*;
import service.SistemaEvangelizare;
import ui.Console;
import ui.telas.TelaSenha;
import security.PoliticaDeSenha;

public class MenuPrincipal {

    private final SistemaEvangelizare sistema;
    private final Console console;
    private final MenuAdministrador menuAdministrador;
    private final MenuCatequista menuCatequista;
    private final MenuCatequisando menuCatequisando;
    private final MenuResponsavel menuResponsavel;
    private final TelaSenha telaSenha;

    public MenuPrincipal(SistemaEvangelizare sistema, Console console, MenuAdministrador
                                 menuAdministrador, MenuCatequista menuCatequista, MenuCatequisando menuCatequisando,
                         MenuResponsavel menuResponsavel, TelaSenha telaSenha) {
        this.sistema = sistema;
        this.console = console;
        this.menuAdministrador = menuAdministrador;
        this.menuCatequista = menuCatequista;
        this.menuCatequisando = menuCatequisando;
        this.menuResponsavel = menuResponsavel;
        this.telaSenha = telaSenha;
    }

    public void iniciar() {
        int opcao;

        System.out.println("================================");
        System.out.println("       EVANGELIZARE");
        System.out.println("  Sistema de Apoio à Catequese");
        System.out.println("================================");
        System.out.println("Bem-vindo ao Evangelizare!");

        if (!sistema.existeAdministrador() && !configurarPrimeiroAcesso()) {
            System.out.println(
                    "\nÉ preciso criar o administrador para usar o sistema. Encerrando."
            );
            return;
        }

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

            // senha definida por outra pessoa: preicsa ser trocada para entrar
            if (usuario.isSenhaProvisoria()
                    && !telaSenha.trocarSenhaProvisoria(usuario, senha)) {
                return;
            }

            if (usuario instanceof Administrador) {
                menuAdministrador.exibir(usuario);

            } else if (usuario instanceof Catequista catequista) {
                System.out.println("Perfil: Catequista");
                menuCatequista.exibir(catequista);

            } else if (usuario instanceof Catequisando catequisando) {
                System.out.println("Perfil: Catequisando");
                menuCatequisando.exibir(catequisando);

            } else if (usuario instanceof Responsavel responsavel) {
                System.out.println("Perfil: Responsável");
                menuResponsavel.exibir(responsavel);
            }

        } else {
            System.out.println("\nID ou senha incorretos.");
        }
    }

    // PRIMEIRO ACESSO

    private boolean configurarPrimeiroAcesso() {
        System.out.println("\n========== PRIMEIRO ACESSO ==========");
        System.out.println("Ainda não existe um administrador. Crie o primeiro agora.");
        System.out.println(PoliticaDeSenha.DESCRICAO);

        while (true) {
            System.out.println("\nID do administrador (em branco para sair): ");
            String id = console.lerTexto().trim();

            if (id.isEmpty()) {
                return false;
            }

            System.out.println("Nome: ");
            String nome = console.lerTexto().trim();

            System.out.println("Telefone: ");
            String telefone = console.lerTexto().trim();

            System.out.println("Idade: ");
            int idade = console.lerInt();

            System.out.println("Senha: ");
            String senha = console.lerSenha();

            System.out.println("Confirme a senha: ");
            String confirmacao = console.lerSenha();

            if (!senha.equals(confirmacao)) {
                System.out.println("\nAs senhas não conferem. Tente novamente.");
                continue;
            }

            try {
                sistema.cadastrarAdministradorInicial(
                        new Administrador(id, nome, telefone, idade, senha)
                );

                System.out.println("\nAdministrador criado com sucesso! Entre com o seu ID e a sua senha");

                return true;

            } catch (IllegalArgumentException e) {
                System.out.println("\nNão foi possível criar: " + e.getMessage());
            }
        }
    }
}