package ui;

import java.util.List;

import exception.CatequistaNaoEncontradoException;
import exception.TurmaNaoEncontradaException;

import model.*;
import service.SistemaEvangelizare;

public class TelaTurma {

    private final SistemaEvangelizare sistema;
    private final Console console;

    public TelaTurma(SistemaEvangelizare sistema, Console console) {
        this.sistema = sistema;
        this.console = console;
    }

    // CADASTRO DE TURMA

    public void cadastrar() {
        System.out.println("\n========== CADASTRO DE TURMA ==========");
        System.out.println("1 - Catequese Infantil");
        System.out.println("2 - Crisma Jovem");
        System.out.println("3 - Crisma Adulto");
        System.out.println("0 - Voltar");
        System.out.print("Escolha uma opção: ");

        int etapaTurma = console.lerInt();

        TipoTurma tipoTurma;

        switch (etapaTurma) {
            case 1:
                tipoTurma = TipoTurma.CATEQUESE;
                break;
            case 2:
                tipoTurma = TipoTurma.CRISMA_JOVEM;
                break;
            case 3:
                tipoTurma = TipoTurma.CRISMA_ADULTO;
                break;
            case 0:
                System.out.println("Retornando ao menu...");
                return;
            default:
                System.out.println("Opção inválida.");
                return;
        }

        System.out.print("Digite o ID da turma: ");
        String idTurma = console.lerTexto().trim();

        System.out.print("Digite o ID do primeiro catequista: ");
        String primeiroCatequistaId = console.lerTexto().trim();

        try {
            Catequista primeiroCatequista =
                    sistema.pesquisarCatequistaPorId(primeiroCatequistaId);

            Turma turma = new Turma(
                    idTurma,
                    tipoTurma,
                    primeiroCatequista
            );

            while (true) {
                System.out.print(
                        "Cadastrar mais um catequista na turma (S/N)? "
                );

                String resposta = console.lerTexto().trim();

                if (resposta.equalsIgnoreCase("N")) {
                    break;
                }

                if (resposta.equalsIgnoreCase("S")) {
                    System.out.print("Digite o ID do segundo catequista: ");
                    String segundoCatequistaId = console.lerTexto().trim();

                    Catequista segundoCatequista =
                            sistema.pesquisarCatequistaPorId(segundoCatequistaId);

                    turma.adicionarCatequista(segundoCatequista);
                    break;
                }

                System.out.println("Resposta inválida. Digite S ou N.");
            }

            sistema.cadastrarTurma(turma);

            System.out.println("Turma cadastrada com sucesso!");
            System.out.println("ID da turma: " + turma.getIdTurma());
            System.out.println("Etapa: " + turma.getTipoTurma());
            System.out.println("Catequistas responsáveis:");

            for (Catequista catequista : turma.getCatequistas()) {
                System.out.println(
                        "- " + catequista.getNome()
                                + " (ID: " + catequista.getId() + ")"
                );
            }

        } catch (CatequistaNaoEncontradoException e) {
            System.out.println(e.getMessage());

        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    // EDITAR TURMA
    public void editar() {
        System.out.println("\n========== EDITAR TURMA ==========");

        System.out.println("Digite o ID da turma que deseja editar: ");
        String idTurma = console.lerTexto().trim();

        List<Turma> turmasEncontradas;

        try {
            turmasEncontradas = sistema.pesquisarTurmaPorId(idTurma);

        } catch (TurmaNaoEncontradaException e) {
            System.out.println(e.getMessage());
            return;
        }

        if (turmasEncontradas.isEmpty()) {
            System.out.println("Nenhuma turma encontrada.");
            return;
        }

        Turma turmaAtual = turmasEncontradas.get(0);

        System.out.println("\n========== DADOS ATUAIS DA TURMA ==========");
        System.out.println("ID: " + turmaAtual.getIdTurma());
        System.out.println("Tipo: " + turmaAtual.getTipoTurma());
        System.out.println("Catequistas responsáveis:");

        for (Catequista catequista : turmaAtual.getCatequistas()) {
            System.out.println(
                    "- " + catequista.getNome()
                            + " (ID: " + catequista.getId() + ")"
            );
        }

        System.out.println("\nSelecione o novo tipo da turma:");
        System.out.println("1 - Catequese Infantil");
        System.out.println("2 - Crisma Jovem");
        System.out.println("3 - Crisma Adulto");
        System.out.println("0 - Cancelar");
        System.out.print("Escolha uma opção: ");

        int opcaoTipo = console.lerInt();

        TipoTurma tipoTurma;

        switch (opcaoTipo) {
            case 1:
                tipoTurma = TipoTurma.CATEQUESE;
                break;
            case 2:
                tipoTurma = TipoTurma.CRISMA_JOVEM;
                break;
            case 3:
                tipoTurma = TipoTurma.CRISMA_ADULTO;
                break;
            case 0:
                System.out.println("Edição cancelada.");
                return;
            default:
                System.out.println("Opção inválida.");
                return;

        }

        try {
            System.out.println("\nDeseja alterar os catequistas responsáveis?");
            System.out.println("1 - Manter os catequistas atuais");
            System.out.println("2 - Substituir os catequistas");
            System.out.println("Escolha uma opção: ");

            String opcaoCatequistas = console.lerTexto().trim();

            Turma turmaAtualizada;

            switch (opcaoCatequistas) {
                case "1":
                    turmaAtualizada = new Turma(
                            idTurma,
                            tipoTurma,
                            turmaAtual.getCatequistas().get(0)
                    );

                    for (int i = 1; i < turmaAtual.getCatequistas().size(); i ++) {
                        turmaAtualizada.adicionarCatequista(
                                turmaAtual.getCatequistas().get(i)
                        );
                    }
                    break;

                case "2":
                    System.out.println("Digite o ID do primeiro catequista: ");
                    String idPrimeiro = console.lerTexto().trim();

                    Catequista primeiroCatequista =
                            sistema.pesquisarCatequistaPorId(idPrimeiro);

                    turmaAtualizada = new Turma(
                            idTurma,
                            tipoTurma,
                            primeiroCatequista
                    );
                    break;

                default:
                    System.out.println("Opção inválida. Edição cancelada.");
                    return;
            }

            while (true) {
                System.out.println(
                        "Deseja cadastrar um segundo catequista? (S/N ):"
                );

                String resposta = console.lerTexto().trim();

                if (resposta.equalsIgnoreCase("N")) {
                    break;
                }

                if (resposta.equalsIgnoreCase("S")) {
                    System.out.print("Digite o ID do segundo catequista: ");
                    String idSegundo = console.lerTexto().trim();

                    Catequista segundoCatequista =
                            sistema.pesquisarCatequistaPorId(idSegundo);

                    turmaAtualizada.adicionarCatequista(segundoCatequista);
                    break;
                }

                System.out.println("Resposta inválida. Digite S ou N.");
            }

            sistema.editarTurma(turmaAtualizada);

            System.out.println("\nTurma editada com sucesso!");
            System.out.println("ID: " + idTurma);
            System.out.println("Tipo: " + tipoTurma);
            System.out.println("Catequistas responsáveis:");

            for (Catequista catequista : turmaAtualizada.getCatequistas()) {
                System.out.println(
                        "- " + catequista.getNome()
                                + " (ID: " + catequista.getId() + ")"
                );
            }

        } catch (TurmaNaoEncontradaException
                 | CatequistaNaoEncontradoException e) {
            System.out.println(e.getMessage());

        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
}