package ui.telas;

import exception.CatequistaNaoEncontradoException;
import exception.TurmaNaoEncontradaException;

import model.*;
import security.GeradorDeCredenciais;
import service.SistemaEvangelizare;
import ui.Console;

public class TelaCatequista {

    private final SistemaEvangelizare sistema;
    private final Console console;
    private final Seletor seletor;

    public TelaCatequista(SistemaEvangelizare sistema, Console console) {
        this.sistema = sistema;
        this.console = console;
        this.seletor = new Seletor(console);
    }

    // CADASTRO DE USUÁRIO

    public void cadastrarUsuario() {
        System.out.println("\n========== CADASTRO DE USUÁRIO ==========");
        System.out.println("1 - Catequista");
        System.out.println("2 - Catequisando");
        System.out.println("3 - Administrador");
        System.out.println("0 - Voltar");
        System.out.print("Escolha uma opção: ");

        int perfil = console.lerInt();

        switch (perfil) {
            case 1:
                System.out.println("Perfil selecionado: Catequista.");
                cadastrarCatequista();
                break;

            case 2:
                System.out.println("O cadastro de catequisandos deve ser realizado pelo catequista responsável");
                break;

            case 3:
                System.out.println("Perfil selecionado: Administrador.");
                cadastrarAdministrador();
                break;

            case 0:
                System.out.println("Retornando ao menu do administrador...");
                break;

            default:
                System.out.println("Opção de perfil inválida.");
        }
    }

    // CADASTRAR CATEQUISTA

    private void cadastrarCatequista() {
        System.out.println("\n========== CADASTRO DO CATEQUISTA ==========");

        System.out.println("Nome: ");
        String nome = console.lerTexto();

        System.out.println("Telefone: ");
        String telefone = console.lerTexto();

        System.out.println("Idade: ");
        int idade = console.lerInt();

        System.out.println("Ano de ingresso na catequese: ");
        String anoIngresso = console.lerTexto();

        String login = lerLogin(nome);
        String codigo = GeradorDeCredenciais.novoCodigo();

        try {
            Catequista catequista = new Catequista(
                    GeradorDeCredenciais.novoId(), nome, telefone, idade, codigo, anoIngresso
            );
            catequista.setLogin(login);

            sistema.cadastrarCatequista(catequista);

            System.out.println("\nCatequista cadastrado com sucesso!");
            Credenciais.exibir(catequista.getNome(), login, codigo);

        } catch (IllegalArgumentException e) {
            System.out.println("\nNão foi possível cadastrar: " + e.getMessage());
        }
    }

    // CADASTRAR ADMINISTRADOR

    private void cadastrarAdministrador() {
        System.out.println("\n========== CADASTRO DO ADMINISTRADOR ==========");

        System.out.println("Nome: ");
        String nome = console.lerTexto().trim();

        System.out.println("Telefone: ");
        String telefone = console.lerTexto().trim();

        System.out.println("Idade: ");
        int idade = console.lerInt();

        String login = lerLogin(nome);
        String codigo = GeradorDeCredenciais.novoCodigo();

        try {
            Administrador administrador = new Administrador(
                    GeradorDeCredenciais.novoId(), nome, telefone, idade, codigo
            );
            administrador.setLogin(login);

            sistema.cadastrarAdministrador(administrador);

            System.out.println("\nAdministrador cadastrado com sucesso!");
            Credenciais.exibir(nome, login, codigo);

        } catch (IllegalArgumentException e) {
            System.out.println("\nNão foi possível cadastrar: " + e.getMessage());
        }
    }

    // Sugere o nome de usuário e deixa a pessoa aceitar ou digitar outro.
    private String lerLogin(String nome) {
        String sugestao = sistema.sugerirLogin(nome);

        System.out.println("Nome de usuário [" + sugestao + "] (Enter para aceitar): ");
        String digitado = console.lerTexto().trim();

        return digitado.isEmpty() ? sugestao : digitado;
    }

    // EDITAR CATEQUISTA

    public void editar() {
        System.out.println("\n========== EDITAR CATEQUISTA ==========");

        Catequista catequista = seletor.buscarEEscolher(
                "catequista",
                sistema::pesquisarCatequistaPorNome,
                c -> c.getNome() + " | " + c.getTelefone() + " | usuário: " + c.getLogin()
        );

        if (catequista == null) {
            System.out.println("Edição cancelada.");
            return;
        }

        try {
            System.out.println("\nCatequista: " + catequista.getNome());
            System.out.println("Telefone atual: " + catequista.getTelefone());
            System.out.println("Idade atual: " + catequista.getIdade());
            System.out.println("Ano de ingresso atual: " + catequista.getAnoDeIngresso());

            System.out.println("\nNovo telefone");
            String telefone = console.lerTexto().trim();

            System.out.println("Nova idade: ");
            int idade = console.lerInt();

            System.out.println("Novo ano de ingresso: ");
            String anoDeIngresso = console.lerTexto().trim();

            sistema.atualizarCatequista(
                    catequista.getId(),
                    telefone,
                    idade,
                    anoDeIngresso
            );

            System.out.println("\nCatequista atualizado com sucesso!");

        } catch (CatequistaNaoEncontradoException e) {
            System.out.println(e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Não foi possível atualizar: " + e.getMessage());
        }
    }

    // TRANSFERIR CATEQUISTA

    public void transferir() {
        System.out.println("\n========== TRANSFERIR CATEQUISTA ==========");

        Catequista catequista = seletor.buscarEEscolher(
                "catequista",
                sistema::pesquisarCatequistaPorNome,
                c -> c.getNome() + " | " + c.getTelefone()
        );

        if (catequista == null) {
            System.out.println("Transferência cancelada.");
            return;
        }

        Turma novaTurma = seletor.escolherTurma(sistema, "Turma de destino");

        if (novaTurma == null) {
            System.out.println("Transferência cancelada.");
            return;
        }

        try {
            sistema.transferirCatequista(catequista.getId(), novaTurma.getIdTurma());

            System.out.println("Catequista transferido com sucesso!");

        } catch (CatequistaNaoEncontradoException
                 | TurmaNaoEncontradaException
                 | IllegalArgumentException e) {

            System.out.println(
                    "Não foi possível realizar a transferência: "
                            + e.getMessage()
            );
        }
    }
}