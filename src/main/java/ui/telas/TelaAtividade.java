package ui.telas;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import exception.AtividadeNaoEncontradaException;
import exception.CatequistaNaoEncontradoException;
import exception.TurmaNaoEncontradaException;
import model.*;
import service.atividade.AtividadeService;
import service.SistemaEvangelizare;
import ui.Console;

public class TelaAtividade {

    private final SistemaEvangelizare sistema;
    private final AtividadeService atividades;
    private final Console console;

    public TelaAtividade(
            SistemaEvangelizare sistema,
            AtividadeService atividades,
            Console console) {
        this.sistema = sistema;
        this.atividades = atividades;
        this.console = console;
    }

    // MENU (catequista e administrador)

    public void menu(Usuario usuarioLogado) {
        boolean catequista = usuarioLogado instanceof Catequista;
        int opcao;

        do {
            System.out.println("\n========== MENU DE ATIVIDADES ==========");

            if (catequista) {
                System.out.println("1 - Publicar atividade");
                System.out.println("2 - Consultar atividades");
                System.out.println("3 - Alterar atividade");
                System.out.println("4 - Remover atividade");
            } else {
                System.out.println("1 - Consultar atividades");
                System.out.println("2 - Remover atividade");
            }

            System.out.println("0 - Voltar");
            System.out.print("Escolha uma opção: ");

            opcao = console.lerInt();

            if (opcao == 0) {
                System.out.println("Retornando ao menu anterior...");
            } else if (catequista) {
                switch (opcao) {
                    case 1:
                        publicar((Catequista) usuarioLogado);
                        break;
                    case 2:
                        consultarDisponiveis(usuarioLogado);
                        break;
                    case 3:
                        alterar(usuarioLogado);
                        break;
                    case 4:
                        remover(usuarioLogado);
                        break;
                    default:
                        System.out.println("Opção inválida.");
                }
            } else {
                switch (opcao) {
                    case 1:
                        consultarDisponiveis(usuarioLogado);
                        break;
                    case 2:
                        remover(usuarioLogado);
                        break;
                    default:
                        System.out.println("Opção inválida.");
                }
            }

        } while (opcao != 0);
    }

    // PUBLICAR

    private void publicar(Catequista catequista) {
        System.out.println("\n========== PUBLICAR ATIVIDADE ==========");

        try {
            List<Turma> turmas =
                    sistema.pesquisarTurmaPorCatequista(catequista.getId());

            if (turmas.isEmpty()) {
                System.out.println("Você não possui turmas cadastradas.");
                return;
            }

            System.out.println("\nSuas turmas:");

            for (Turma turma : turmas) {
                System.out.println(
                        "ID: " + turma.getIdTurma()
                                + " | Tipo: " + turma.getTipoTurma()
                );
            }

            System.out.println("\nID da atividade: ");
            String id = console.lerTexto();

            System.out.println("Título: ");
            String titulo = console.lerTexto();

            System.out.println("Descrição: ");
            String descricao = console.lerTexto();

            System.out.println("Prazo (dd/mm/aaaa, vazio = sem prazo): ");
            LocalDate prazo = console.lerDataOpcional();

            System.out.println("ID da turma: ");
            String idTurma = console.lerTexto();

            atividades.cadastrarAtividade(
                    id, titulo, descricao, prazo, idTurma, catequista
            );

            System.out.println("\nAtividade publicada com sucesso!");

        } catch (CatequistaNaoEncontradoException
                 | TurmaNaoEncontradaException e) {
            System.out.println("Erro ao localizar os dados: " + e.getMessage());

        } catch (IllegalArgumentException e) {
            System.out.println("Não foi possível publicar: " + e.getMessage());
        }
    }

    // CONSULTAR (também usado pelo menu do catequisando)

    public void consultarDisponiveis(Usuario usuarioLogado) {
        System.out.println("\n========== ATIVIDADES ==========");

        try {
            List<Atividade> lista =
                    atividades.pesquisarAtividadesDisponiveisPara(usuarioLogado);

            if (lista.isEmpty()) {
                System.out.println("Nenhuma atividade disponível.");
                return;
            }

            for (Atividade atividade : lista) {
                exibir(atividade);
            }

        } catch (IllegalArgumentException e) {
            System.out.println("Não foi possível consultar: " + e.getMessage());
        }
    }

    // ALTERAR

    private void alterar(Usuario usuarioLogado) {
        System.out.println("\n========== ALTERAR ATIVIDADE ==========");

        System.out.println("ID da atividade: ");
        String id = console.lerTexto();

        try {
            Atividade atual = atividades.pesquisarAtividadePorId(id);

            System.out.println();
            exibir(atual);

            System.out.println("Deixe em branco para manter o valor atual.");

            System.out.println("Novo título: ");
            String titulo = console.lerTexto();
            if (titulo.isBlank()) {
                titulo = atual.getTitulo();
            }

            System.out.println("Nova descrição: ");
            String descricao = console.lerTexto();
            if (descricao.isBlank()) {
                descricao = atual.getDescricao();
            }

            LocalDate prazo = lerNovoPrazo(atual.getPrazo());

            atividades.atualizarAtividade(
                    id, titulo, descricao, prazo, usuarioLogado
            );

            System.out.println("\nAtividade atualizada com sucesso!");

        } catch (AtividadeNaoEncontradaException e) {
            System.out.println(e.getMessage());

        } catch (IllegalArgumentException e) {
            System.out.println("Não foi possível atualizar: " + e.getMessage());
        }
    }

    // Em branco mantém o prazo atual; "sem" remove o prazo.
    private LocalDate lerNovoPrazo(LocalDate prazoAtual) {
        while (true) {
            System.out.println(
                    "Novo prazo (dd/mm/aaaa, \"sem\" para remover o prazo): "
            );
            String texto = console.lerTexto().trim();

            if (texto.isEmpty()) {
                return prazoAtual;
            }

            if (texto.equalsIgnoreCase("sem")) {
                return null;
            }

            try {
                return Console.converterData(texto);
            } catch (DateTimeParseException e) {
                System.out.println("Data inválida. Use dd/mm/aaaa.");
            }
        }
    }

    // REMOVER

    private void remover(Usuario usuarioLogado) {
        System.out.println("\n========== REMOVER ATIVIDADE ==========");

        System.out.println("ID da atividade: ");
        String id = console.lerTexto();

        try {
            Atividade atividade = atividades.pesquisarAtividadePorId(id);

            System.out.println();
            exibir(atividade);

            System.out.println("Confirma a remoção? (S/N): ");
            String confirmacao = console.lerTexto().trim();

            if (confirmacao.equalsIgnoreCase("S")) {
                atividades.removerAtividade(id, usuarioLogado);
                System.out.println("Atividade removida com sucesso!");
            } else {
                System.out.println("Remoção cancelada.");
            }

        } catch (AtividadeNaoEncontradaException e) {
            System.out.println(e.getMessage());

        } catch (IllegalArgumentException e) {
            System.out.println("Operação não permitida: " + e.getMessage());
        }
    }

    // EXIBIÇÃO

    private void exibir(Atividade atividade) {
        LocalDate hoje = LocalDate.now();

        System.out.println("------------------------------");
        System.out.println("[" + atividade.getIdAtividade() + "] " + atividade.getTitulo());
        System.out.println(
                "Turma: " + atividade.getTurma().getIdTurma()
                        + " | Publicada em "
                        + Console.formatarData(atividade.getDataPublicacao())
                        + " por " + atividade.getAutor().getNome()
        );
        System.out.println("Prazo: " + descreverPrazo(atividade, hoje));
        System.out.println("Descrição: " + atividade.getDescricao());
    }

    private String descreverPrazo(Atividade atividade, LocalDate hoje) {
        if (atividade.getPrazo() == null) {
            return "sem prazo";
        }

        String data = Console.formatarData(atividade.getPrazo());

        if (atividade.estaEncerrada(hoje)) {
            return data + " (encerrada)";
        }

        long dias = atividade.diasRestantes(hoje);

        if (dias == 0) {
            return data + " (vence hoje)";
        }

        return data + " (" + (dias == 1 ? "falta 1 dia" : "faltam " + dias + " dias") + ")";
    }
}