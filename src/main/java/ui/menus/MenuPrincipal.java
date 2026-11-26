package ui.menus;

import model.*;
import service.SistemaEvangelizare;
import ui.Console;
import ui.telas.TelaSenha;
import security.GeradorDeCredenciais;
import security.PoliticaDeSenha;

public class MenuPrincipal {

    private final SistemaEvangelizare sistema;
    private final Console console;
    private final MenuAdministrador menuAdministrador;
    private final MenuCatequista menuCatequista;
    private final MenuCatequisando menuCatequisando;
    private final MenuResponsavel menuResponsavel;
    private final TelaSenha telaSenha;

    public MenuPrincipal(SistemaEvangelizare sistema, Console console, MenuAdministrador menuAdministrador, MenuCatequista menuCatequista, MenuCatequisando menuCatequisando, MenuResponsavel menuResponsavel, TelaSenha telaSenha) {
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

        // Sem administrador não há como cadastrar ninguém: cria o primeiro.
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

        System.out.print("Usuário: ");
        String login = console.lerTexto();

        System.out.print("Senha: ");
        String senha = console.lerSenha();

        if (sistema.autenticarUsuario(login, senha)) {
            Usuario usuario = sistema.pesquisarUsuarioPorLogin(login);

            System.out.println("\nLogin realizado com sucesso!");
            System.out.println("Bem-vindo(a), " + usuario.getNome() + "!");

            // Senha definida por outra pessoa: precisa ser trocada para entrar.
            if (usuario.isSenhaProvisoria()
                    && !telaSenha.trocarSenhaProvisoria(usuario, senha)) {
                return;
            }

            if (usuario instanceof Catequista coordenador && coordenador.isCoordenador()) {
                System.out.println("Perfis: Catequista e Administrador");
                escolherPerfilDoCoordenador(coordenador);

            } else if (usuario instanceof Administrador) {
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
            System.out.println(
                    "\nUsuário ou senha incorretos. Se for o primeiro acesso, o código pode ter vencido: "
                            + "peça um novo ao administrador."
            );
        }
    }

    private void escolherPerfilDoCoordenador(Catequista coordenador) {
        int opcao;

        do {
            System.out.println("\nEntrar como:");
            System.out.println("1 - Catequista");
            System.out.println("2 - Administrador");
            System.out.println("0 - Encerrar sessão");
            System.out.print("Escolha uma opção: ");

            opcao = console.lerInt();

            switch (opcao) {
                case 1:
                    menuCatequista.exibir(coordenador);
                    break;

                case 2:
                    menuAdministrador.exibir(coordenador);
                    break;

                case 0:
                    System.out.println("Sessão encerrada.");
                    break;

                default:
                    System.out.println("Opção inválida.");
            }
        } while (opcao != 0);
    }

    // PRIMEIRO ACESSO

    // Devolve true se o administrador foi criado, ou false se o usuário desistiu.
    private boolean configurarPrimeiroAcesso() {
        System.out.println("\n========== PRIMEIRO ACESSO ==========");
        System.out.println("Ainda não existe um administrador. Crie o primeiro agora.");
        System.out.println(PoliticaDeSenha.DESCRICAO);

        while (true) {
            System.out.println("\nNome do administrador (em branco para sair): ");
            String nome = console.lerTexto().trim();

            if (nome.isEmpty()) {
                return false;
            }

            System.out.println("Telefone: ");
            String telefone = console.lerTexto().trim();

            System.out.println("Idade: ");
            int idade = console.lerInt();

            String sugestao = sistema.sugerirLogin(nome);
            System.out.println("Nome de usuário [" + sugestao + "] (Enter para aceitar): ");
            String digitado = console.lerTexto().trim();
            String login = digitado.isEmpty() ? sugestao : digitado;

            System.out.println("Senha: ");
            String senha = console.lerSenha();

            System.out.println("Confirme a senha: ");
            String confirmacao = console.lerSenha();

            if (!senha.equals(confirmacao)) {
                System.out.println("\nAs senhas não conferem. Tente novamente.");
                continue;
            }

            try {
                Administrador administrador = new Administrador(
                        GeradorDeCredenciais.novoId(), nome, telefone, idade, senha
                );
                administrador.setLogin(login);

                sistema.cadastrarAdministradorInicial(administrador);

                System.out.println(
                        "\nAdministrador criado com sucesso! Entre com o usuário \""
                                + login + "\" e a sua senha."
                );
                return true;

            } catch (IllegalArgumentException e) {
                System.out.println("\nNão foi possível criar: " + e.getMessage());
            }
        }
    }
}