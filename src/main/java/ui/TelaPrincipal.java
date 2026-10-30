package ui;

import java.util.ArrayList;
import java.util.List;

import exception.CatequisandoNaoEncontradoException;
import exception.CatequistaNaoEncontradoException;
import exception.NoticiaNaoEncontradaException;
import exception.TurmaNaoEncontradaException;

import model.*;
import service.SistemaEvangelizare;
import service.SistemaEvangelizareMap;

public class TelaPrincipal {

    private final SistemaEvangelizare sistema =
            new SistemaEvangelizareMap();

    private final Console console = new Console();

    private final TelaTurma telaTurma =
            new TelaTurma(sistema, console);

    public static void main(String[] args) {
        TelaPrincipal tela = new TelaPrincipal();
        tela.iniciar();
    }

    public void iniciar() {
        cadastrarAdministradorInicial();

        int opcao;

        System.out.println("================================");
        System.out.println("       EVANGELIZARE");
        System.out.println("  Sistema de Apoio à Catequese");
        System.out.println("================================");
        System.out.println("Bem-vindo ao Evangelizare!");

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

    // AUTENTIFICAÇÃO DO USUARIO

    private void autentificarUsuario() {

        System.out.println("\n========== LOGIN ==========");

        System.out.print("ID do usuário: ");
        String id = console.lerTexto();

        System.out.print("Senha: ");
        String senha = console.lerTexto();

        if (sistema.autenticarUsuario(id, senha)) {
            Usuario usuario = sistema.pesquisarUsuarioPorId(id);

            System.out.println("\nLogin realizado com sucesso!");
            System.out.println("Bem-vindo(a), " + usuario.getNome() + "!");

            if (usuario instanceof Administrador) {
                menuAdministrador(usuario);

            } else if (usuario instanceof Catequista catequista) {
                System.out.println("Perfil: Catequista");
                menuCatequista(catequista);

            } else if (usuario instanceof Catequisando catequisando) {
                System.out.println("Perfil: Catequisando");
                menuCatequisando(catequisando);
            }

        } else {
            System.out.println("\nID ou senha incorretos.");
        }
    }

    // MENU DO ADMINISTRADOR

    private void menuAdministrador(Usuario usuarioLogado) {
        int opcao;

        do {
            System.out.println("\n========== MENU DO ADMINISTRADOR ==========");
            System.out.println("1 - Cadastrar Usuário");
            System.out.println("2 - Cadastrar Turma");
            System.out.println("3 - Editar Catequista");
            System.out.println("4 - Notícias");
            System.out.println("5 - Editar Turma");
            System.out.println("6 - Transferir Catequisando");
            System.out.println("7 - Transferir Catequista");
            System.out.println("0 - Encerrar sessão");
            System.out.print("Escolha uma opção: ");

            opcao = console.lerInt();

            switch (opcao) {
                case 1:
                    cadastrarUsuario();
                    break;

                case 2:
                    telaTurma.cadastrar();
                    break;

                case 3:
                    editarCatequista();
                    break;

                case 4:
                    menuNoticias(usuarioLogado);
                    break;

                case 5:
                    telaTurma.editar();
                    break;

                case 6:
                    transferirCatequisando();
                    break;

                case 7:
                    transferirCatequista();
                    break;

                case 0:
                    System.out.println("Sessão encerrada");
                    break;

                default:
                    System.out.println("Opção inválida");
            }
        } while (opcao != 0);
    }

    // MENU CATEQUISTA

    private void menuCatequista(Catequista catequista) {
        int opcao;

        do {
            System.out.println("\n========== MENU DO CATEQUISTA ==========");
            System.out.println("1 - Cadastrar Catequisando");
            System.out.println("2 - Consultar Catequisandos");
            System.out.println("3 - Editar Catequisando");
            System.out.println("4 - Remover Catequisando");
            System.out.println("5 - Notícias");
            System.out.println("0 - Encerrar sessão");
            System.out.print("Escolha uma opção: ");

            opcao = console.lerInt();

            switch (opcao) {
                case 1:
                    cadastrarCatequisando(catequista);
                    break;

                case 2:
                    consultarCatequisandos(catequista);
                    break;

                case 3:
                    editarCatequisando(catequista);
                    break;

                case 4:
                    removerCatequisando(catequista);
                    break;

                case 5:
                    menuNoticias(catequista);
                    break;

                case 0:
                    System.out.println("Sessão encerrada");
                    break;

                default:
                    System.out.println("Opção inválida.");
            }

        } while (opcao != 0);
    }

    //MENU CATEQUISANDO

    private void menuCatequisando(Catequisando catequisando) {

        int opcao;

        do {
            System.out.println("\n========== MENU DO CATEQUISANDO ==========");
            System.out.println("1 - Consultar minha turma");
            System.out.println("2 - Consultar notícias");
            System.out.println("0 - Encerrar sessão");
            System.out.print("Escolha uma opção: ");

            opcao = console.lerInt();

            switch (opcao) {
                case 1:
                    consultarMinhaTurma(catequisando);
                    break;

                case 2:
                    consultarNoticias(catequisando);
                    break;

                case 0:
                    System.out.println("Sessão encerrada.");
                    break;

                default:
                    System.out.println("Opção inválida.");
            }

        } while (opcao != 0);
    }

    // CONSULTA DE CATEQUISANDOS

    private void consultarCatequisandos(Catequista catequista) {

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
                    for (Responsavel responsavel : c.getResponsaveis()) {
                        System.out.println("------------------------------");
                        System.out.println(
                                "Nome: " + responsavel.getNomeResponsavel()
                        );
                        System.out.println(
                                "Telefone: " + responsavel.getTelefoneResponsavel()
                        );
                        System.out.println(
                                "Parentesco: " + responsavel.getParentesco()
                        );
                    }
                }
            }

        } catch (CatequistaNaoEncontradoException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    // MENU NOTICIAS

    private void menuNoticias(Usuario usuarioLogado) {
        int opcao;

        do {
            System.out.println("\n========== MENU DE NOTÍCIAS ==========");

            if (usuarioLogado instanceof Administrador) {
                System.out.println("1 - Cadastrar notícia");
                System.out.println("2 - Consultar notícias");
                System.out.println("3 - Remover notícia");

            } else if (usuarioLogado instanceof Catequista) {
                System.out.println("1 - Cadastrar notícia");
                System.out.println("2 - Consultar notícias");

            } else if (usuarioLogado instanceof Catequisando) {
                System.out.println("1 - Consultar notícias");
            }

            System.out.println("0 - Voltar");
            System.out.print("Escolha uma opção: ");

            opcao = console.lerInt();

            switch (opcao) {
                case 1:
                    if (usuarioLogado instanceof Administrador
                            || usuarioLogado instanceof Catequista) {
                        cadastrarNoticia(usuarioLogado);

                    } else if (usuarioLogado instanceof Catequisando) {
                        consultarNoticias(usuarioLogado);
                    }
                    break;

                case 2:
                    if (usuarioLogado instanceof Administrador
                            || usuarioLogado instanceof Catequista) {
                        consultarNoticias(usuarioLogado);
                    } else {
                        System.out.println("Opção inválida.");
                    }
                    break;

                case 3:
                    if (usuarioLogado instanceof Administrador) {
                        removerNoticia(usuarioLogado);
                    } else {
                        System.out.println("Opção inválida.");
                    }
                    break;

                case 0:
                    System.out.println("Retornando ao menu anterior...");
                    break;

                default:
                    System.out.println("Opção inválida.");
            }

        } while (opcao != 0);
    }

    private void consultarNoticias(Usuario usuarioLogado) {
        System.out.println("\n========== NOTÍCIAS ==========");

        boolean encontrouNoticia = false;

        for (TipoNoticia tipo : TipoNoticia.values()) {
            try {
                List<Noticia> noticias = sistema.pesquisarNoticiaPorTipo(tipo);

                for (Noticia noticia : noticias) {
                    System.out.println(noticia);
                    encontrouNoticia = true;
                }

            } catch (NoticiaNaoEncontradaException e) {
                // Não há notícias desta categoria.
            }
        }

        if (!encontrouNoticia) {
            System.out.println("Nenhuma notícia cadastrada.");
        }
    }

    private void cadastrarNoticia(Usuario usuarioLogado) {
        System.out.println("\n========== CADASTRO DE NOTÍCIA ==========");

        System.out.print("ID da notícia: ");
        String id = console.lerTexto();

        System.out.print("Título: ");
        String titulo = console.lerTexto();

        System.out.print("Conteúdo: ");
        String conteudo = console.lerTexto();

        // Escolher a categoria da notícia
        System.out.println("\nCategorias disponíveis:");

        TipoNoticia[] categorias = TipoNoticia.values();

        for (int i = 0; i < categorias.length; i++) {
            System.out.println((i + 1) + " - " + categorias[i]);
        }

        System.out.print("Escolha a categoria: ");
        int opcaoCategoria = console.lerInt();

        if (opcaoCategoria < 1 || opcaoCategoria > categorias.length) {
            System.out.println("Categoria inválida.");
            return;
        }

        TipoNoticia tipoNoticia = categorias[opcaoCategoria - 1];

        Turma turmaSelecionada = null;

        try {
            // O administrador escolhe se a notícia será geral ou específica.
            if (usuarioLogado instanceof Administrador) {
                System.out.println("\n1 - Notícia geral");
                System.out.println("2 - Notícia para uma turma específica");
                System.out.print("Escolha uma opção: ");

                int opcaoTurma = console.lerInt();

                if (opcaoTurma == 1) {
                    turmaSelecionada = null;

                } else if (opcaoTurma == 2) {
                    System.out.print("ID da turma: ");
                    String idTurma = console.lerTexto();

                    List<Turma> turmas =
                            sistema.pesquisarTurmaPorId(idTurma);

                    if (turmas.isEmpty()) {
                        System.out.println("Turma não encontrada.");
                        return;
                    }

                    turmaSelecionada = turmas.get(0);

                } else {
                    System.out.println("Opção inválida.");
                    return;
                }

                // O catequista só pode escolher uma de suas próprias turmas.
            } else if (usuarioLogado instanceof Catequista catequista) {
                List<Turma> turmas =
                        sistema.pesquisarTurmaPorCatequista(catequista.getId());

                if (turmas.isEmpty()) {
                    System.out.println(
                            "Você não possui turmas cadastradas."
                    );
                    return;
                }

                System.out.println("\nSuas turmas:");

                for (Turma turma : turmas) {
                    System.out.println(
                            "ID: " + turma.getIdTurma()
                                    + " | Tipo: " + turma.getTipoTurma()
                    );
                }

                System.out.print("ID da turma: ");
                String idTurma = console.lerTexto();

                for (Turma turma : turmas) {
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

            } else {
                System.out.println(
                        "Você não tem permissão para cadastrar notícias."
                );
                return;
            }

            Noticia noticia = new Noticia(
                    id,
                    titulo,
                    conteudo,
                    tipoNoticia,
                    usuarioLogado,
                    turmaSelecionada
            );

            sistema.cadastrarNoticia(noticia, usuarioLogado);

            System.out.println("\nNotícia cadastrada com sucesso!");

        } catch (CatequistaNaoEncontradoException
                 | TurmaNaoEncontradaException e) {

            System.out.println("Erro ao localizar os dados: " + e.getMessage());

        } catch (IllegalArgumentException e) {
            System.out.println("Não foi possível cadastrar: " + e.getMessage());
        }
    }

    private void removerNoticia(Usuario usuarioLogado) {
        System.out.println("\n========== REMOVER NOTÍCIA ==========");

        // Proteção adicional na interface.
        if (!(usuarioLogado instanceof Administrador)) {
            System.out.println(
                    "Somente o administrador pode remover notícias."
            );
            return;
        }

        System.out.print("Digite o ID da notícia que deseja remover: ");
        String idNoticia = console.lerTexto();

        try {
            // Verifica se a notícia existe antes de removê-la.
            Noticia noticia = sistema.pesquisarNoticiaPorId(idNoticia);

            System.out.println("\nNotícia selecionada:");
            System.out.println(noticia);

            System.out.print("Confirma a remoção? (S/N): ");
            String confirmacao = console.lerTexto();

            if (confirmacao.equalsIgnoreCase("S")) {
                sistema.removerNoticia(idNoticia, usuarioLogado);

                System.out.println("Notícia removida com sucesso!");
            } else {
                System.out.println("Remoção cancelada.");
            }

        } catch (NoticiaNaoEncontradaException e) {
            System.out.println("Não foi possível remover: " + e.getMessage());

        } catch (IllegalArgumentException e) {
            System.out.println("Operação não permitida: " + e.getMessage());
        }
    }

    // CONSULTAR MINHA TURMA

    private void consultarMinhaTurma(Catequisando catequisando) {
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

    // CONSULTAR NOTICIAS

    private void consultarNoticias(Catequisando catequisando) {
        System.out.println("\n========== NOTÍCIAS ==========");

        try {
            List<Noticia> noticiasDisponiveis =
                    sistema.pesquisarNoticiasDisponiveisPara(catequisando);

            for (Noticia noticia : noticiasDisponiveis) {
                System.out.println("\nTítulo: " + noticia.getTitulo());
                System.out.println("Tipo: " + noticia.getTipoNoticia());
                System.out.println("Conteúdo: " + noticia.getConteudo());
                System.out.println("Data: " + noticia.getDataPublicacao());

                System.out.println(
                        "Autor: " + (noticia.getAutor() != null
                                ? noticia.getAutor().getNome()
                                : "Não informado")
                );

                System.out.println("--------------------------------");
            }

        } catch (NoticiaNaoEncontradaException e) {
            System.out.println(e.getMessage());
        }
    }

    // CADASTRO DE USUÁRIO

    private void cadastrarUsuario() {
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

    private void editarCatequista() {
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

    private void transferirCatequista() {

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

    // CADASTRO DE CATEQUISANDO

    private void cadastrarCatequisando(Catequista catequista) {

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

            boolean cadastrarResponsavel = exigeResponsavel;

            if (!exigeResponsavel) {
                System.out.println(
                        "\nDeseja cadastrar um responsavel? (S/N):"
                );

                cadastrarResponsavel =
                        console.lerTexto().trim().equalsIgnoreCase("S");
            }

            while (cadastrarResponsavel) {
                System.out.println("\n--- DADOS DO RESPONSÁVEL ---");

                System.out.println("Nome do responsável: ");
                String nomeResponsavel = console.lerTexto();

                System.out.println("Telefone do responsável: ");
                String telefoneResponsavel = console.lerTexto();

                System.out.println("Parentesco: ");
                String parentesco = console.lerTexto();

                Responsavel responsavel = new Responsavel(
                        nomeResponsavel,
                        telefoneResponsavel,
                        parentesco
                );

                novoCatequisando.adicionarResponsavel(responsavel);

                System.out.println(
                        "\nDeseja cadastrar outro responsável? (S/N): "
                );

                cadastrarResponsavel =
                        console.lerTexto().trim().equalsIgnoreCase("S");
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

    private void editarCatequisando(Catequista catequista) {

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

            List<Responsavel> responsaveis = new ArrayList<>();

            System.out.println("\nCadastre os responsáveis atualizados.");
            System.out.println(
                    "A lista atual será substituída pela nova lista informada."
            );

            TipoTurma tipo = selecionado.getTurma().getTipoTurma();

            boolean exigeResponsavel =
                    tipo == TipoTurma.CATEQUESE
                            || tipo == TipoTurma.CRISMA_JOVEM;

            boolean adicionarResponsavel = true;

            // Nas turmas que não exigem responsáveis, o cadastro é opcional.
            if (!exigeResponsavel) {
                System.out.print(
                        "\nDeseja cadastrar responsáveis? (S/N): "
                );

                String resposta = console.lerTexto().trim();

                while (!resposta.equalsIgnoreCase("S")
                        && !resposta.equalsIgnoreCase("N")) {
                    System.out.print("Resposta inválida. Digite S ou N: ");
                    resposta = console.lerTexto().trim();
                }

                adicionarResponsavel = resposta.equalsIgnoreCase("S");
            }

            while (adicionarResponsavel) {
                System.out.println("\n--- DADOS DO RESPONSÁVEL ---");

                System.out.print("Nome: ");
                String nomeResponsavel = console.lerTexto().trim();

                System.out.print("Telefone: ");
                String telefoneResponsavel = console.lerTexto().trim();

                System.out.print("Parentesco: ");
                String parentesco = console.lerTexto().trim();

                Responsavel responsavel = new Responsavel(
                        nomeResponsavel,
                        telefoneResponsavel,
                        parentesco
                );

                responsaveis.add(responsavel);

                System.out.print(
                        "Deseja adicionar outro responsável? (S/N): "
                );

                String resposta = console.lerTexto().trim();

                while (!resposta.equalsIgnoreCase("S")
                        && !resposta.equalsIgnoreCase("N")) {
                    System.out.print("Resposta inválida. Digite S ou N: ");
                    resposta = console.lerTexto().trim();
                }

                adicionarResponsavel = resposta.equalsIgnoreCase("S");
            }

            if (responsaveis.isEmpty() && exigeResponsavel) {
                System.out.println(
                        "Esta turma exige pelo menos um responsável."
                );
                return;
            }

            sistema.atualizarCatequisando(
                    idCatequisando,
                    telefone,
                    idade,
                    responsaveis
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

    private void removerCatequisando(Catequista catequista) {
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

    private void transferirCatequisando() {

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

    private void cadastrarAdministradorInicial() {
        Administrador administrador = new Administrador(
                "ADM001",
                "Administrador",
                "83999999999",
                30,
                "admin123"
        );

        sistema.cadastrarAdministrador(administrador);
    }
}