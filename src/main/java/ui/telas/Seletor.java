package ui.telas;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import exception.TurmaNaoEncontradaException;
import model.Catequisando;
import model.Catequista;
import model.Responsavel;
import model.TipoTurma;
import model.Turma;
import model.Usuario;
import service.SistemaEvangelizare;
import ui.Console;

class Seletor {

    private static final int LIMITE_DE_RESULTADOS = 30;

    private final Console console;

    Seletor(Console console) {
        this.console = console;
    }

    <T> T escolher(String titulo, List<T> opcoes, Function<T, String> descricao) {
        if (opcoes.isEmpty()) {
            System.out.println("Nenhuma opção disponível.");
            return null;
        }

        System.out.println("\n" + titulo + ":");

        for (int i = 0; i < opcoes.size(); i++) {
            System.out.println((i + 1) + " - " + descricao.apply(opcoes.get(i)));
        }

        System.out.println("0 - Cancelar");

        while (true) {
            System.out.print("Escolha uma opção: ");
            int numero = console.lerInt();

            if (numero == 0) {
                return null;
            }

            if (numero >= 1 && numero <= opcoes.size()) {
                return opcoes.get(numero - 1);
            }

            System.out.println("Opção inválida.");
        }
    }

    <T> T buscarEEscolher(
            String titulo, Function<String, List<T>> busca, Function<T, String> descricao) {

        while (true) {
            System.out.println(
                    "\nBuscar " + titulo + " (parte do nome ou do telefone; em branco lista todos; 0 cancela): "
            );
            String texto = console.lerTexto().trim();

            if (texto.equals("0")) {
                return null;
            }

            List<T> resultados = busca.apply(texto);

            if (resultados.isEmpty()) {
                System.out.println("Nenhum resultado para essa busca.");
                continue;
            }

            if (resultados.size() > LIMITE_DE_RESULTADOS) {
                System.out.println(
                        "Mais de " + LIMITE_DE_RESULTADOS + " resultados. Digite mais letras do nome."
                );
                continue;
            }

            return escolher("Resultados", resultados, descricao);
        }
    }

    static List<Turma> todasAsTurmas(SistemaEvangelizare sistema) {
        List<Turma> turmas = new ArrayList<>();

        for (TipoTurma tipo : TipoTurma.values()) {
            try {
                turmas.addAll(sistema.pesquisarTurmaPorTipo(tipo.name()));
            } catch (TurmaNaoEncontradaException e) {
                // não há turmas deste tipo
            }
        }

        turmas.sort(java.util.Comparator.comparing(Turma::getIdTurma));

        return turmas;
    }

    Turma escolherTurma(SistemaEvangelizare sistema, String titulo) {
        return escolher(
                titulo,
                todasAsTurmas(sistema),
                t -> t.getIdTurma() + " | " + t.getTipoTurma()
        );
    }

    static <T> Function<String, List<T>> porNomeOuTelefone(
            Supplier<List<T>> todos, Function<T, String> nome, Function<T, String> telefone) {

        return texto -> {
            String procurado = texto.trim().toLowerCase();
            String digitos = procurado.replaceAll("\\D", "");

            return todos.get().stream()
                    .filter(t -> nome.apply(t).toLowerCase().contains(procurado)
                            || (!digitos.isEmpty()
                            && telefone.apply(t).replaceAll("\\D", "").contains(digitos)))
                    .collect(Collectors.toList());
        };
    }

    static Function<String, List<Catequista>> buscaCatequistas(SistemaEvangelizare sistema) {
        return porNomeOuTelefone(
                () -> sistema.pesquisarCatequistaPorNome(""),
                Catequista::getNome, Catequista::getTelefone);
    }

    static Function<String, List<Catequisando>> buscaCatequisandos(SistemaEvangelizare sistema) {
        return porNomeOuTelefone(
                () -> sistema.pesquisarCatequisandoPorNome(""),
                Catequisando::getNome, Catequisando::getTelefone);
    }

    static Function<String, List<Responsavel>> buscaResponsaveis(SistemaEvangelizare sistema) {
        return porNomeOuTelefone(
                () -> sistema.pesquisarResponsavelPorNome(""),
                Responsavel::getNome, Responsavel::getTelefone);
    }

    static Function<String, List<Usuario>> buscaUsuarios(SistemaEvangelizare sistema) {
        return porNomeOuTelefone(
                () -> sistema.pesquisarUsuarioPorNome(""),
                Usuario::getNome, Usuario::getTelefone);
    }
}