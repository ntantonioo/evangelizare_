package ui;

import exception.CatequistaNaoEncontradoException;
import exception.TurmaNaoEncontradaException;

import model.*;
import service.SistemaEvangelizare;

public class TelaCatequista {

    private final SistemaEvangelizare sistema;
    private final Console console;

    public TelaCatequista(SistemaEvangelizare sistema, Console console) {
        this.sistema = sistema;
        this.console = console;
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
                //TODO
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

        System.out.println("ID do catequista: ");
        String idCatequista = console.lerTexto();

        System.out.println("Nome: ");
        String nome = console.lerTexto();

        System.out.println("Telefone: ");
        String telefone = console.lerTexto();

        System.out.println("Idade: ");
        int idade = console.lerInt();

        System.out.println("Senha: ");
        String senha = console.lerTexto();

        System.out.println("Ano de ingresso na catequese: ");
        String anoIngresso = console.lerTexto();

        Catequista catequista = new Catequista(
                idCatequista,
                nome,
                telefone,
                idade,
                senha,
                anoIngresso
        );

        try {
            sistema.cadastrarCatequista(catequista);

            System.out.println("\nCatequista cadastrado com sucesso!");

        } catch (IllegalArgumentException e) {
            System.out.println("\nNao foi possível cadastrar: " + e.getMessage());
        }
    }

    // EDITAR CATEQUISTA

    public void editar() {
        System.out.println("\n========== EDITAR CATEQUISTA ==========");

        System.out.println("ID do catequista: ");
        String id = console.lerTexto().trim();

        try {
            Catequista catequista = sistema.pesquisarCatequistaPorId(id);

            System.out.println("\nCatequista encontrado: " + catequista.getNome());
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
                    id,
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

        try {
            System.out.println("Informe o ID do catequista: ");
            String idCatequista = console.lerTexto();

            System.out.println("Informe o ID da nova turma: ");
            String idNovaTurma = console.lerTexto();

            sistema.transferirCatequista(
                    idCatequista,
                    idNovaTurma
            );

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