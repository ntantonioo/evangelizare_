package ui.telas;

import java.util.ArrayList;
import java.util.List;

import exception.CatequisandoNaoEncontradoException;
import exception.CatequistaNaoEncontradoException;
import exception.ResponsavelNaoEncontradoException;
import exception.TurmaNaoEncontradaException;

import model.*;
import ui.Console;
import service.SistemaEvangelizare;

public class TelaCatequisando {

    private final SistemaEvangelizare sistema;
    private final Console console;

    public TelaCatequisando(SistemaEvangelizare sistema, Console console) {
        this.sistema = sistema;
        this.console = console;
    }

    // CONSULTA DE CATEQUISANDOS

    public void consultar(Catequista catequista) {

        System.out.println("\n========== CONSULTAR CATEQUISANDOS ==========");

        try {
            List<Turma> turmasDisponiveis =
                    sistema.pesquisarTurmaPorCatequista(catequista.getId());

            if (turmasDisponiveis.isEmpty()) {
                System.out.println("Você não possui turmas cadastradas.");
                return;
            }

            System.out.println("\nSuas turmas:");

            for (Turma turma : turmasDisponiveis) {
                System.out.println(
                        "ID: " + turma.getIdTurma()
                                + " | Tipo: " + turma.getTipoTurma()
                );
            }

            System.out.println("\nDigite o ID da turma que deseja consultar: ");
            String idTurma = console.lerTexto().trim();

            boolean turmaPermitida = turmasDisponiveis.stream()
                    .anyMatch(t -> t.getIdTurma().equals(idTurma));

            if (!turmaPermitida) {
                System.out.println(
                        "Você não tem permissão para consultar essa turma."
                );
                return;
            }

            // Busca os catequisandos vinculados à turma escolhida
            List<Catequisando> catequisandos =
                    sistema.pesquisarCatequisandosPorTurma(idTurma);

            if (catequisandos.isEmpty()) {
                System.out.println(
                        "Nenhum catequisando cadastrado nesta turma."
                );
                return;
            }

            System.out.println("\nCatequisandos da turma:");

            for (Catequisando c : catequisandos) {
                System.out.println("------------------------------");
                System.out.println("ID: " + c.getId());
                System.out.println("Nome: " + c.getNome());
                System.out.println("Telefone: " + c.getTelefone());
                System.out.println("Idade: " + c.getIdade());
                System.out.println("\nResponsáveis:");

                if (c.getResponsaveis().isEmpty()) {
                    System.out.println("Nenhum responsável cadastrado.");
                } else {
                    for (VinculoResponsavel vinculo : c.getVinculos()) {
                        Responsavel responsavel = vinculo.getResponsavel();

                        System.out.println("------------------------------");
                        System.out.println("ID: " + responsavel.getId());
                        System.out.println("Nome: " + responsavel.getNome());
                        System.out.println("Telefone: " + responsavel.getTelefone());
                        System.out.println("Parentesco: " + vinculo.getParentesco());
                    }
                }
            }

        } catch (CatequistaNaoEncontradoException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    // CONSULTAR MINHA TURMA

    public void consultarMinhaTurma(Catequisando catequisando) {
        Turma turma = catequisando.getTurma();

        System.out.println("\n========== MINHA TURMA ==========");

        if (turma == null) {
            System.out.println("Você não está associado a nenhuma turma.");
            return;
        }

        System.out.println("ID da turma: " + turma.getIdTurma());
        System.out.println("Tipo: " + turma.getTipoTurma());

        System.out.println("Catequistas responsáveis:");

        for (Catequista catequista : turma.getCatequistas()) {
            System.out.println(
                    "- " + catequista.getNome()
                            + " (ID: " + catequista.getId() + ")"
            );
        }
    }

    // CADASTRO DE CATEQUISANDO

    public void cadastrar(Catequista catequista) {

        System.out.println("\n========== CADASTRO DE CATEQUISANDO ==========");

        try {
            List<Turma> turmasDisponiveis = sistema.pesquisarTurmaPorCatequista(catequista.getId());

            if (turmasDisponiveis.isEmpty()) {
                System.out.println("Você ainda não possui turmas cadastradas.");
                return;
            }

            System.out.println("\nTurmas disponíveis:");

            for (Turma turma : turmasDisponiveis) {
                System.out.println(
                        "ID: " + turma.getIdTurma()
                                + " | Tipo: " + turma.getTipoTurma()
                );
            }

            System.out.println("\nDigite o ID do catequisando: ");
            String id = console.lerTexto();

            System.out.println("Digite o nome do catequisando: ");
            String nome = console.lerTexto();

            System.out.println("Digite o telefone do catequisando: ");
            String telefone = console.lerTexto();

            System.out.println("Digite a idade: ");
            int idade = console.lerInt();

            System.out.println("Digite a senha: ");
            String senha = console.lerTexto();

            System.out.println("Digite o ID da turma: ");
            String idTurma = console.lerTexto().trim();

            Turma turmaSelecionada = null;

            for (Turma turma : turmasDisponiveis) {
                if (turma.getIdTurma().equals(idTurma)) {
                    turmaSelecionada = turma;
                    break;
                }
            }

            if (turmaSelecionada == null) {
                System.out.println(
                        "A turma informada não pertence a você."
                );
                return;
            }

            Catequisando novoCatequisando = new Catequisando(
                    id,
                    nome,
                    telefone,
                    idade,
                    senha,
                    turmaSelecionada
            );

            TipoTurma tipo = turmaSelecionada.getTipoTurma();

            boolean exigeResponsavel =
                    tipo == TipoTurma.CATEQUESE
                            || tipo == TipoTurma.CRISMA_JOVEM;

            // Responsáveis: contas já existentes (pelo ID) ou novas
            for (VinculoResponsavel vinculo : lerResponsaveis(exigeResponsavel)) {
                novoCatequisando.adicionarResponsavel(
                        vinculo.getResponsavel(),
                        vinculo.getParentesco()
                );
            }

            sistema.cadastrarCatequisando(novoCatequisando);

            System.out.println(
                    "\nCatequisando cadastrado com sucesso!"
            );

        } catch (CatequistaNaoEncontradoException | TurmaNaoEncontradaException e) {

            System.out.println(e.getMessage());

        }  catch (IllegalArgumentException e) {
            System.out.println("Não foi possível cadastrar: " + e.getMessage());
        }
    }

    // EDITAR CATEQUISANDO

    public void editar(Catequista catequista) {

        System.out.println("\n========== EDITAR CATEQUISANDO ==========");

        try {
            // Busca somente as turmas do catequista logado.
            List<Turma> turmasDisponiveis =
                    sistema.pesquisarTurmaPorCatequista(catequista.getId());

            if (turmasDisponiveis.isEmpty()) {
                System.out.println("Você não possui turmas cadastradas.");
                return;
            }

            System.out.println("\nSuas turmas:");

            for (Turma turma : turmasDisponiveis) {
                System.out.println(
                        "ID: " + turma.getIdTurma()
                                + " | Tipo: " + turma.getTipoTurma()
                );
            }

            System.out.print("\nDigite o ID da turma: ");
            String idTurma = console.lerTexto().trim();

            boolean turmaPermitida = turmasDisponiveis.stream()
                    .anyMatch(t -> t.getIdTurma().equals(idTurma));

            if (!turmaPermitida) {
                System.out.println(
                        "Você não tem permissão para editar catequisandos desta turma."
                );
                return;
            }

            List<Catequisando> catequisandos =
                    sistema.pesquisarCatequisandosPorTurma(idTurma);

            if (catequisandos.isEmpty()) {
                System.out.println("Esta turma não possui catequisandos.");
                return;
            }

            System.out.println("\nCatequisandos da turma:");

            for (Catequisando c : catequisandos) {
                System.out.println(
                        "ID: " + c.getId() + " | Nome: " + c.getNome()
                );
            }

            System.out.print(
                    "\nDigite o ID do catequisando que deseja editar: "
            );
            String idCatequisando = console.lerTexto().trim();

            // Localiza o catequisando somente na turma autorizada.
            Catequisando selecionado = null;

            for (Catequisando c : catequisandos) {
                if (c.getId().equals(idCatequisando)) {
                    selecionado = c;
                    break;
                }
            }

            if (selecionado == null) {
                System.out.println(
                        "Catequisando não encontrado nesta turma."
                );
                return;
            }

            System.out.println("\nCatequisando: " + selecionado.getNome());

            System.out.print(
                    "Novo telefone (atual: "
                            + selecionado.getTelefone() + "): "
            );
            String telefone = console.lerTexto().trim();

            System.out.print(
                    "Nova idade (atual: "
                            + selecionado.getIdade() + "): "
            );
            int idade = console.lerInt();

            System.out.println("\nCadastre os responsáveis atualizados.");
            System.out.println(
                    "A lista atual será substituída pela nova lista informada."
            );

            TipoTurma tipo = selecionado.getTurma().getTipoTurma();

            boolean exigeResponsavel =
                    tipo == TipoTurma.CATEQUESE
                            || tipo == TipoTurma.CRISMA_JOVEM;

            List<VinculoResponsavel> vinculos = lerResponsaveis(exigeResponsavel);

            if (vinculos.isEmpty() && exigeResponsavel) {
                System.out.println(
                        "Esta turma exige pelo menos um responsável."
                );
                return;
            }

            sistema.atualizarCatequisando(
                    idCatequisando,
                    telefone,
                    idade,
                    vinculos
            );

            System.out.println("\nDados atualizados com sucesso!");

        } catch (CatequistaNaoEncontradoException
                 | CatequisandoNaoEncontradoException e) {

            System.out.println("Erro: " + e.getMessage());

        } catch (IllegalArgumentException e) {
            System.out.println(
                    "Não foi possível atualizar: " + e.getMessage()
            );
        }
    }

    // REMOVER CATEQUISANDO

    public void remover(Catequista catequista) {
        try {
            // busca as turmas do catequista
            List<Turma> turmasDisponiveis =
                    sistema.pesquisarTurmaPorCatequista(catequista.getId());

            if (turmasDisponiveis.isEmpty()) {
                System.out.println("Você não possui turmas cadastradas.");
                return;
            }

            System.out.println("\n========== REMOVER CATEQUISANDO ==========");
            System.out.println("Suas turmas");

            for (Turma turma : turmasDisponiveis) {
                System.out.println(
                        "ID: " + turma.getIdTurma()
                );
            }

            System.out.println("\nInforme o ID da turma: ");
            String idTurma = console.lerTexto().trim();

            boolean turmaPermitida = turmasDisponiveis.stream()
                    .anyMatch(t -> t.getIdTurma().equals(idTurma));

            if (!turmaPermitida) {
                System.out.println("Você não tem acesso a essa turma.");
                return;
            }

            // listar catequisandos da turma
            List<Catequisando> catequisandos =
                    sistema.pesquisarCatequisandosPorTurma(idTurma);

            if (catequisandos.isEmpty()) {
                System.out.println("Não há catequisandos nesta turma.");
                return;
            }

            System.out.println("\nCatequisandos disponíveis:");

            for (Catequisando c : catequisandos) {
                System.out.println(
                        "ID: " + c.getId()
                                + " | Nome: " + c.getNome()
                );
            }

            System.out.println("\nInforme o ID do catequisando: ");
            String idCatequisando = console.lerTexto().trim();

            boolean catequisandoEncontrado = catequisandos.stream()
                    .anyMatch(c -> c.getId().equals(idCatequisando));

            if (!catequisandoEncontrado) {
                System.out.println(
                        "Catequisando não encontrado nesta turma."
                );
                return;
            }

            // confirmação de remoção
            System.out.println(
                    "Deseja realmente remover este catequisando? (S/N): "
            );
            String confirmacao = console.lerTexto().trim();

            if (!confirmacao.equalsIgnoreCase("S")) {
                System.out.println("Remoção cancelada");
                return;
            }

            sistema.removerCatequisando(idCatequisando);

            System.out.println("Catequisando removido com sucesso!");

        } catch (CatequisandoNaoEncontradoException e) {
            System.out.println(e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    // TRANSFERIR CATEQUISANDO

    public void transferir() {

        System.out.println("\n========== TRANSFERIR CATEQUISANDO ==========");

        try {
            System.out.println("Informe o ID do catequisando: ");
            String idCatequisando = console.lerTexto();

            System.out.println("Informe o ID da nova Turma: ");
            String idNovaTurma = console.lerTexto();

            sistema.transferirCatequisando(
                    idCatequisando,
                    idNovaTurma
            );

            System.out.println(
                    "Catequisando transferido com sucesso!"
            );

        } catch (CatequisandoNaoEncontradoException
                 | TurmaNaoEncontradaException
                 | IllegalArgumentException e) {

            System.out.println("Não foi possível realizar a transferência: "
                    + e.getMessage());
        }
    }

    // RESPONSÁVEIS (usados no cadastro e na edição de catequisandos)

    // Lê os responsáveis do catequisando. Cada um pode ser uma conta que já
    // existe (informando o ID) ou uma conta nova, cadastrada junto.
    private List<VinculoResponsavel> lerResponsaveis(boolean obrigatorio) {
        List<VinculoResponsavel> vinculos = new ArrayList<>();

        boolean adicionar = true;

        // Nas turmas que não exigem responsáveis, o cadastro é opcional.
        if (!obrigatorio) {
            System.out.println("\nDeseja cadastrar responsáveis? (S/N): ");
            adicionar = lerSimNao();
        }

        while (adicionar) {
            System.out.println("\n--- RESPONSÁVEL ---");
            System.out.println(
                    "ID do responsável (deixe em branco para cadastrar um novo): "
            );
            String id = console.lerTexto().trim();

            Responsavel responsavel =
                    id.isEmpty() ? lerNovoResponsavel() : buscarResponsavel(id);

            if (responsavel != null) {
                adicionarVinculo(vinculos, responsavel);
            }

            System.out.println("\nDeseja adicionar outro responsável? (S/N): ");
            adicionar = lerSimNao();
        }

        return vinculos;
    }

    private void adicionarVinculo(
            List<VinculoResponsavel> vinculos, Responsavel responsavel) {

        boolean jaInformado = vinculos.stream()
                .anyMatch(v -> v.getResponsavel().getId().equals(responsavel.getId()));

        if (jaInformado) {
            System.out.println("Este responsável já foi informado.");
            return;
        }

        System.out.println("Parentesco com o catequisando: ");
        String parentesco = console.lerTexto();

        try {
            vinculos.add(new VinculoResponsavel(responsavel, parentesco));
        } catch (IllegalArgumentException e) {
            System.out.println("Não foi possível adicionar: " + e.getMessage());
        }
    }

    private Responsavel buscarResponsavel(String id) {
        try {
            Responsavel responsavel = sistema.pesquisarResponsavelPorId(id);

            System.out.println("Responsável encontrado: " + responsavel.getNome());

            return responsavel;

        } catch (ResponsavelNaoEncontradoException e) {
            System.out.println(
                    e.getMessage()
                            + ". Para cadastrar um novo, deixe o ID em branco."
            );
            return null;
        }
    }

    private Responsavel lerNovoResponsavel() {
        System.out.println("Novo responsável (ele poderá entrar no sistema com este ID).");

        System.out.println("ID de acesso: ");
        String id = console.lerTexto().trim();

        if (sistema.pesquisarUsuarioPorId(id) != null) {
            System.out.println(
                    "Já existe um usuário com esse ID. Se ele já é um responsável "
                            + "cadastrado, informe o ID dele na pergunta anterior."
            );
            return null;
        }

        System.out.println("Nome: ");
        String nome = console.lerTexto();

        System.out.println("Telefone: ");
        String telefone = console.lerTexto();

        System.out.println("Idade: ");
        int idade = console.lerInt();

        System.out.println("Senha inicial: ");
        String senha = console.lerTexto();

        System.out.println("Confirme a senha: ");
        String confirmacao = console.lerTexto();

        if (!senha.equals(confirmacao)) {
            System.out.println("As senhas não conferem. Responsável não adicionado.");
            return null;
        }

        try {
            return new Responsavel(id, nome, telefone, idade, senha);

        } catch (IllegalArgumentException e) {
            System.out.println("Não foi possível adicionar: " + e.getMessage());
            return null;
        }
    }

    private boolean lerSimNao() {
        String resposta = console.lerTexto().trim();

        while (!resposta.equalsIgnoreCase("S") && !resposta.equalsIgnoreCase("N")) {
            System.out.print("Resposta inválida. Digite S ou N: ");
            resposta = console.lerTexto().trim();
        }

        return resposta.equalsIgnoreCase("S");
    }
}