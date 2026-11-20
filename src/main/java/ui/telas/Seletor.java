package ui.telas;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import exception.TurmaNaoEncontradaException;
import model.TipoTurma;
import model.Turma;
import service.SistemaEvangelizare;
import ui.Console;

/**
 * Deixa o usuário escolher uma pessoa ou uma turma em uma lista numerada,
 * em vez de digitar um ID. Evita erros de digitação e dispensa que alguém
 * precise decorar identificadores.
 */
class Seletor {

    private static final int LIMITE_DE_RESULTADOS = 30;

    private final Console console;

    Seletor(Console console) {
        this.console = console;
    }

    /** Mostra a lista numerada e devolve a opção escolhida, ou null se cancelou. */
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
                    "\nBuscar " + titulo + " (parte do nome; em branco lista todos; 0 cancela): "
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

    /** Escolhe entre todas as turmas do sistema. */
    Turma escolherTurma(SistemaEvangelizare sistema, String titulo) {
        List<Turma> turmas = new ArrayList<>();

        for (TipoTurma tipo : TipoTurma.values()) {
            try {
                turmas.addAll(sistema.pesquisarTurmaPorTipo(tipo.name()));
            } catch (TurmaNaoEncontradaException e) {
                // não há turmas deste tipo
            }
        }

        return escolher(titulo, turmas, t -> t.getIdTurma() + " | " + t.getTipoTurma());
    }
}