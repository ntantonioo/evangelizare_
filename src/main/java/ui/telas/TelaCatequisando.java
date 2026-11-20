package ui.telas;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import exception.CatequisandoNaoEncontradoException;
import exception.CatequistaNaoEncontradoException;
import exception.TurmaNaoEncontradaException;
import model.*;
import security.GeradorDeCredenciais;
import service.SistemaEvangelizare;
import ui.Console;

public class TelaCatequisando {

    // Idade mínima de um responsável (a mesma regra do serviço, aqui só para avisar cedo).
    private static final int IDADE_MINIMA_RESPONSAVEL = 18;

    private final SistemaEvangelizare sistema;
    private final Console console;
    private final Seletor seletor;

    public TelaCatequisando(SistemaEvangelizare sistema, Console console) {
        this.sistema = sistema;
        this.console = console;
        this.seletor = new Seletor(console);
    }

    // CONSULTA DE CATEQUISANDOS

    public void consultar(Catequista catequista) {
        System.out.println("\n========== CONSULTAR CATEQUISANDOS ==========");

        try {
            Turma turma = escolherTurmaDoCatequista(catequista);

            if (turma == null) {
                return;
            }

            List<Catequisando> catequisandos =
                    sistema.pesquisarCatequisandosPorTurma(turma.getIdTurma());

            if (catequisandos.isEmpty()) {
                System.out.println("Nenhum catequisando cadastrado nesta turma.");
                return;
            }

            System.out.println("\nCatequisandos da turma:");

            for (Catequisando c : catequisandos) {
                exibir(c);
            }

        } catch (CatequistaNaoEncontradoException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void exibir(Catequisando c) {
        System.out.println("------------------------------");
        System.out.println("Nome: " + c.getNome());
        System.out.println("Telefone: " + c.getTelefone());
        System.out.println("Idade: " + c.getIdade());
        System.out.println(
                "Acesso: " + (temAcessoProprio(c.getTurma())
                        ? "usuário " + c.getLogin()
                        : "sem acesso próprio (acompanhado pelos responsáveis)")
        );
        System.out.println("\nResponsáveis:");

        if (c.getVinculos().isEmpty()) {
            System.out.println("Nenhum responsável cadastrado.");
            return;
        }

        for (VinculoResponsavel vinculo : c.getVinculos()) {
            Responsavel responsavel = vinculo.getResponsavel();

            System.out.println(
                    "- " + responsavel.getNome()
                            + " | " + vinculo.getParentesco()
                            + " | " + responsavel.getTelefone()
                            + " | usuário " + responsavel.getLogin()
            );
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
                            + " (" + catequista.getTelefone() + ")"
            );
        }
    }

    // CADASTRO DE CATEQUISANDO

    public void cadastrar(Catequista catequista) {
        System.out.println("\n========== CADASTRO DE CATEQUISANDO ==========");

        try {
            Turma turma = escolherTurmaDoCatequista(catequista);

            if (turma == null) {
                return;
            }

            System.out.println("Nome do catequisando: ");
            String nome = console.lerTexto();

            System.out.println("Telefone do catequisando: ");
            String telefone = console.lerTexto();

            System.out.println("Idade: ");
            int idade = console.lerInt();

            Set<String> loginsReservados = new HashSet<>();
            String login = lerLogin(nome, loginsReservados);

            // Criança da Catequese Infantil não entra no sistema: o código é gerado
            // só para cumprir o cadastro e ninguém o vê. Os demais recebem o código
            // de primeiro acesso, mostrado ao final.
            String codigo = GeradorDeCredenciais.novoCodigo();

            Catequisando novo = new Catequisando(
                    GeradorDeCredenciais.novoId(), nome, telefone, idade, codigo, turma
            );
            novo.setLogin(login);

            TipoTurma tipo = turma.getTipoTurma();
            boolean exigeResponsavel =
                    tipo == TipoTurma.CATEQUESE || tipo == TipoTurma.CRISMA_JOVEM;

            Map<String, String> codigosDosNovos = new LinkedHashMap<>();
            List<VinculoResponsavel> vinculos =
                    lerResponsaveis(exigeResponsavel, codigosDosNovos, loginsReservados);

            for (VinculoResponsavel vinculo : vinculos) {
                novo.adicionarResponsavel(vinculo.getResponsavel(), vinculo.getParentesco());
            }

            sistema.cadastrarCatequisando(novo);

            System.out.println("\nCatequisando cadastrado com sucesso!");

            if (temAcessoProprio(turma)) {
                Credenciais.exibir(novo.getNome(), login, codigo);
            } else {
                System.out.println(
                        "Esta criança não tem acesso próprio: quem acompanha é o responsável."
                );
            }

            exibirAcessosNovos(vinculos, codigosDosNovos);

        } catch (CatequistaNaoEncontradoException | TurmaNaoEncontradaException e) {

            System.out.println(e.getMessage());

        } catch (IllegalArgumentException e) {
            System.out.println("Não foi possível cadastrar: " + e.getMessage());
        }
    }

    // EDITAR CATEQUISANDO

    public void editar(Catequista catequista) {
        System.out.println("\n========== EDITAR CATEQUISANDO ==========");

        try {
            Catequisando selecionado = escolherCatequisando(catequista);

            if (selecionado == null) {
                return;
            }

            System.out.println("\nCatequisando: " + selecionado.getNome());

            System.out.print(
                    "Novo telefone (atual: " + selecionado.getTelefone() + "): "
            );
            String telefone = console.lerTexto().trim();

            System.out.print(
                    "Nova idade (atual: " + selecionado.getIdade() + "): "
            );
            int idade = console.lerInt();

            System.out.println("\nCadastre os responsáveis atualizados.");
            System.out.println(
                    "A lista atual será substituída pela nova lista informada."
            );

            TipoTurma tipo = selecionado.getTurma().getTipoTurma();

            boolean exigeResponsavel =
                    tipo == TipoTurma.CATEQUESE || tipo == TipoTurma.CRISMA_JOVEM;

            Map<String, String> codigosDosNovos = new LinkedHashMap<>();
            List<VinculoResponsavel> vinculos =
                    lerResponsaveis(exigeResponsavel, codigosDosNovos, new HashSet<>());

            if (vinculos.isEmpty() && exigeResponsavel) {
                System.out.println("Esta turma exige pelo menos um responsável.");
                return;
            }

            sistema.atualizarCatequisando(
                    selecionado.getId(),
                    telefone,
                    idade,
                    vinculos
            );

            System.out.println("\nDados atualizados com sucesso!");

            exibirAcessosNovos(vinculos, codigosDosNovos);

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
        System.out.println("\n========== REMOVER CATEQUISANDO ==========");

        try {
            Catequisando selecionado = escolherCatequisando(catequista);

            if (selecionado == null) {
                return;
            }

            System.out.println(
                    "Deseja realmente remover " + selecionado.getNome() + "? (S/N): "
            );

            if (!lerSimNao()) {
                System.out.println("Remoção cancelada");
                return;
            }

            sistema.removerCatequisando(selecionado.getId());

            System.out.println("Catequisando removido com sucesso!");

        } catch (CatequistaNaoEncontradoException
                 | CatequisandoNaoEncontradoException
                 | IllegalArgumentException e) {

            System.out.println(e.getMessage());
        }
    }

    // TRANSFERIR CATEQUISANDO

    public void transferir() {
        System.out.println("\n========== TRANSFERIR CATEQUISANDO ==========");

        Catequisando catequisando = seletor.buscarEEscolher(
                "catequisando",
                sistema::pesquisarCatequisandoPorNome,
                c -> c.getNome() + " | turma " + c.getTurma().getIdTurma()
        );

        if (catequisando == null) {
            System.out.println("Transferência cancelada.");
            return;
        }

        Turma novaTurma = seletor.escolherTurma(sistema, "Turma de destino");

        if (novaTurma == null) {
            System.out.println("Transferência cancelada.");
            return;
        }

        boolean ganhaAcesso = !temAcessoProprio(catequisando.getTurma())
                && temAcessoProprio(novaTurma);

        try {
            sistema.transferirCatequisando(catequisando.getId(), novaTurma.getIdTurma());

            System.out.println("Catequisando transferido com sucesso!");

            if (ganhaAcesso) {
                System.out.println(
                        catequisando.getNome() + " agora pode ter acesso próprio. Para gerar o código "
                                + "de primeiro acesso, use \"Redefinir senha de usuário\"."
                );
            }

        } catch (CatequisandoNaoEncontradoException
                 | TurmaNaoEncontradaException
                 | IllegalArgumentException e) {

            System.out.println("Não foi possível realizar a transferência: "
                    + e.getMessage());
        }
    }

    // ESCOLHAS

    private Turma escolherTurmaDoCatequista(Catequista catequista) {
        List<Turma> turmas = sistema.pesquisarTurmaPorCatequista(catequista.getId());

        if (turmas.isEmpty()) {
            System.out.println("Você não possui turmas cadastradas.");
            return null;
        }

        return seletor.escolher(
                "Suas turmas",
                turmas,
                t -> t.getIdTurma() + " | " + t.getTipoTurma()
        );
    }

    private Catequisando escolherCatequisando(Catequista catequista) {
        Turma turma = escolherTurmaDoCatequista(catequista);

        if (turma == null) {
            return null;
        }

        List<Catequisando> catequisandos =
                sistema.pesquisarCatequisandosPorTurma(turma.getIdTurma());

        if (catequisandos.isEmpty()) {
            System.out.println("Esta turma não possui catequisandos.");
            return null;
        }

        return seletor.escolher(
                "Catequisandos da turma",
                catequisandos,
                c -> c.getNome() + " | " + c.getTelefone()
        );
    }

    // Só a Catequese Infantil fica sem acesso próprio: o acesso é dos responsáveis.
    private boolean temAcessoProprio(Turma turma) {
        return turma != null && turma.getTipoTurma() != TipoTurma.CATEQUESE;
    }

    // RESPONSÁVEIS (usados no cadastro e na edição de catequisandos)

    // Lê os responsáveis do catequisando. Cada um pode ser uma conta que já
    // existe (escolhida pelo nome) ou uma conta nova, cadastrada junto. Os códigos
    // de primeiro acesso dos novos ficam em "codigosDosNovos" (ID -> código).
    private List<VinculoResponsavel> lerResponsaveis(
            boolean obrigatorio,
            Map<String, String> codigosDosNovos,
            Set<String> loginsReservados) {

        List<VinculoResponsavel> vinculos = new ArrayList<>();

        boolean adicionar = true;

        // Nas turmas que não exigem responsáveis, o cadastro é opcional.
        if (!obrigatorio) {
            System.out.println("\nDeseja cadastrar responsáveis? (S/N): ");
            adicionar = lerSimNao();
        }

        while (adicionar) {
            System.out.println("\n--- RESPONSÁVEL ---");
            System.out.println("1 - Escolher um responsável já cadastrado");
            System.out.println("2 - Cadastrar um novo responsável");
            System.out.print("Escolha uma opção: ");

            int opcao = console.lerInt();
            Responsavel responsavel = null;

            if (opcao == 1) {
                responsavel = escolherResponsavelExistente();
            } else if (opcao == 2) {
                responsavel = lerNovoResponsavel(codigosDosNovos, loginsReservados);
            } else {
                System.out.println("Opção inválida.");
            }

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

    private Responsavel escolherResponsavelExistente() {
        Responsavel responsavel = seletor.buscarEEscolher(
                "responsável",
                sistema::pesquisarResponsavelPorNome,
                r -> r.getNome() + " | " + r.getTelefone() + " | usuário: " + r.getLogin()
        );

        if (responsavel != null) {
            System.out.println("Responsável escolhido: " + responsavel.getNome());
        }

        return responsavel;
    }

    private Responsavel lerNovoResponsavel(
            Map<String, String> codigosDosNovos, Set<String> loginsReservados) {

        System.out.println("Novo responsável (ele poderá entrar no sistema).");

        System.out.println("Nome: ");
        String nome = console.lerTexto();

        System.out.println("Telefone: ");
        String telefone = console.lerTexto();

        System.out.println("Idade: ");
        int idade = console.lerInt();

        if (idade < IDADE_MINIMA_RESPONSAVEL) {
            System.out.println("O responsável precisa ser maior de idade. Responsável não adicionado.");
            return null;
        }

        String login = lerLogin(nome, loginsReservados);
        String codigo = GeradorDeCredenciais.novoCodigo();

        try {
            Responsavel responsavel = new Responsavel(
                    GeradorDeCredenciais.novoId(), nome, telefone, idade, codigo
            );
            responsavel.setLogin(login);

            codigosDosNovos.put(responsavel.getId(), codigo);

            return responsavel;

        } catch (IllegalArgumentException e) {
            System.out.println("Não foi possível adicionar: " + e.getMessage());
            return null;
        }
    }

    // Depois que o cadastro deu certo, mostra o acesso de cada responsável novo.
    private void exibirAcessosNovos(
            List<VinculoResponsavel> vinculos, Map<String, String> codigosDosNovos) {

        for (VinculoResponsavel vinculo : vinculos) {
            String codigo = codigosDosNovos.get(vinculo.getResponsavel().getId());

            if (codigo != null) {
                Credenciais.exibir(
                        vinculo.getResponsavel().getNome(),
                        vinculo.getResponsavel().getLogin(),
                        codigo
                );
            }
        }
    }

    // Sugere um nome de usuário livre e deixa a pessoa aceitar ou digitar outro.
    // "reservados" guarda os logins já escolhidos neste mesmo formulário.
    private String lerLogin(String nome, Set<String> reservados) {
        String base = GeradorDeCredenciais.baseDeLogin(nome);
        String sugestao = base;
        int numero = 2;

        while (sistema.pesquisarUsuarioPorLogin(sugestao) != null
                || reservados.contains(sugestao.toLowerCase())) {
            sugestao = base + numero++;
        }

        System.out.println("Nome de usuário [" + sugestao + "] (Enter para aceitar): ");
        String digitado = console.lerTexto().trim();
        String escolhido = digitado.isEmpty() ? sugestao : digitado;

        reservados.add(escolhido.toLowerCase());

        return escolhido;
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