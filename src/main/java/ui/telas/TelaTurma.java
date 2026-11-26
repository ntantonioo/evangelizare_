package ui.telas;

import java.util.List;

import exception.CatequistaNaoEncontradoException;
import exception.TurmaNaoEncontradaException;

import model.*;
import ui.Console;
import service.SistemaEvangelizare;

public class TelaTurma {

    private final SistemaEvangelizare sistema;
    private final Console console;
    private final Seletor seletor;

    public TelaTurma(SistemaEvangelizare sistema, Console console) {
        this.sistema = sistema;
        this.console = console;
        this.seletor = new Seletor(console);
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

        Catequista primeiroCatequista = escolherCatequista("primeiro catequista");

        if (primeiroCatequista == null) {
            System.out.println("Cadastro cancelado.");
            return;
        }

        try {
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
                    Catequista segundoCatequista = escolherCatequista("segundo catequista");

                    if (segundoCatequista != null) {
                        turma.adicionarCatequista(segundoCatequista);
                    }

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
                                + " (" + catequista.getTelefone() + ")"
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
                            + " (" + catequista.getTelefone() + ")"
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
                    Catequista primeiroCatequista = escolherCatequista("primeiro catequista");

                    if (primeiroCatequista == null) {
                        System.out.println("Edição cancelada.");
                        return;
                    }

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
                    Catequista segundoCatequista = escolherCatequista("segundo catequista");

                    if (segundoCatequista != null) {
                        turmaAtualizada.adicionarCatequista(segundoCatequista);
                    }

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
                                + " (" + catequista.getTelefone() + ")"
                );
            }

        } catch (TurmaNaoEncontradaException
                 | CatequistaNaoEncontradoException e) {
            System.out.println(e.getMessage());

        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    // Escolhe um catequista pelo nome, em vez de digitar o ID dele.
    private Catequista escolherCatequista(String titulo) {
        return seletor.buscarEEscolher(
                titulo,
                Seletor.buscaCatequistas(sistema),
                c -> c.getNome() + " | " + c.getTelefone()
        );
    }

    // VISÃO GERAL (administrador)

    public void visaoGeral(Usuario administrador) {
        System.out.println("\n========== VISÃO GERAL DE TURMAS ==========");
        System.out.println("1 - Uma turma");
        System.out.println("2 - Todas as turmas");
        System.out.println("0 - Voltar");
        System.out.print("Escolha uma opção: ");

        int opcao = console.lerInt();

        try {
            if (opcao == 1) {
                Turma turma = seletor.escolherTurma(sistema, "Turma");

                if (turma != null) {
                    exibirResumo(sistema.resumirTurma(turma.getIdTurma(), administrador));
                }

            } else if (opcao == 2) {
                List<Turma> turmas = Seletor.todasAsTurmas(sistema);

                if (turmas.isEmpty()) {
                    System.out.println("Nenhuma turma cadastrada.");
                }

                for (Turma turma : turmas) {
                    exibirResumo(sistema.resumirTurma(turma.getIdTurma(), administrador));
                }

            } else if (opcao != 0) {
                System.out.println("Opção inválida.");
            }

        } catch (TurmaNaoEncontradaException | IllegalArgumentException e) {
            System.out.println("Não foi possível exibir: " + e.getMessage());
        }
    }

    private void exibirResumo(ResumoDaTurma resumo) {
        Turma turma = resumo.getTurma();

        System.out.println("\n==================================================");
        System.out.println("TURMA " + turma.getIdTurma() + " | " + turma.getTipoTurma());
        System.out.println("==================================================");

        System.out.println("Catequistas:");

        for (Catequista catequista : resumo.getCatequistas()) {
            System.out.println(
                    "  - " + catequista.getNome() + " | " + catequista.getTelefone()
                            + (catequista.isCoordenador() ? " | coordenador" : "")
            );
        }

        System.out.println(
                "\nCatequisandos: " + resumo.getTotalDeCatequisandos()
                        + " | Responsáveis: " + resumo.getTotalDeResponsaveis()
        );

        if (resumo.getCatequisandos().isEmpty()) {
            System.out.println("  (nenhum catequisando nesta turma)");
            return;
        }

        boolean acessoProprio = turma.getTipoTurma() != TipoTurma.CATEQUESE;
        int numero = 1;

        for (Catequisando catequisando : resumo.getCatequisandos()) {
            System.out.println("--------------------------------------------------");
            System.out.println(
                    numero++ + ". " + catequisando.getNome() + " | " + catequisando.getIdade()
                            + " anos | " + catequisando.getTelefone()
            );
            System.out.println(
                    "   Acesso: " + (acessoProprio
                            ? "usuário " + catequisando.getLogin()
                            : "sem acesso próprio (acompanhado pelos responsáveis)")
            );

            if (catequisando.getVinculos().isEmpty()) {
                System.out.println("   Responsáveis: nenhum cadastrado");
                continue;
            }

            System.out.println("   Responsáveis:");

            for (VinculoResponsavel vinculo : catequisando.getVinculos()) {
                Responsavel responsavel = vinculo.getResponsavel();

                System.out.println(
                        "     - " + responsavel.getNome() + " (" + vinculo.getParentesco() + ")"
                                + " | " + responsavel.getTelefone()
                                + " | usuário " + responsavel.getLogin()
                );
            }
        }
    }
}