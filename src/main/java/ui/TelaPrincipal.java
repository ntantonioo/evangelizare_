package ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

    public static void main(String[] args) {
        TelaPrincipal tela = new TelaPrincipal();
        tela.iniciar();
    }

    public void iniciar() {
        cadastrarAdministradorInicial();

        Scanner scanner = new Scanner(System.in);
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

            opcao = scanner.nextInt();

            switch (opcao) {
                case 1:
                    autentificarUsuario(scanner);
                    break;

                case 2:
                    System.out.println(
                            "Funcionalidade de notícias em desenvolvimento."
                    );
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

        scanner.close();
    }

    // AUTENTIFICAÇÃO DO USUARIO

    private void autentificarUsuario(Scanner scanner) {
        scanner.nextLine();

        System.out.println("\n========== LOGIN ==========");

        System.out.print("ID do usuário: ");
        String id = scanner.nextLine();

        System.out.print("Senha: ");
        String senha = scanner.nextLine();

        if (sistema.autenticarUsuario(id, senha)) {
            Usuario usuario = sistema.pesquisarUsuarioPorId(id);

            System.out.println("\nLogin realizado com sucesso!");
            System.out.println("Bem-vindo(a), " + usuario.getNome() + "!");

            if (usuario instanceof Administrador) {
                menuAdministrador(scanner, usuario);

            } else if (usuario instanceof Catequista catequista) {
                System.out.println("Perfil: Catequista");
                menuCatequista(scanner, catequista);

            } else if (usuario instanceof Catequisando catequisando) {
                System.out.println("Perfil: Catequisando");
                menuCatequisando(scanner, catequisando);
            }

        } else {
            System.out.println("\nID ou senha incorretos.");
        }
    }

    // MENU DO ADMINISTRADOR

    private void menuAdministrador(Scanner scanner, Usuario usuarioLogado) {
        int opcao;

        do {
            System.out.println("\n========== MENU DO ADMINISTRADOR ==========");
            System.out.println("1 - Cadastrar Usuário");
            System.out.println("2 - Cadastrar Turma");
            System.out.println("3 - Notícias");
            System.out.println("0 - Encerrar sessão");

            opcao = scanner.nextInt();

            switch (opcao) {
                case 1:
                    cadastrarUsuario(scanner);
                    break;

                case 2:
                    cadastrarTurma(scanner);
                    break;

                case 3:
                    menuNoticias(scanner, usuarioLogado);
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

    private void menuCatequista(Scanner scanner, Catequista catequista) {
        int opcao;

        do {
            System.out.println("\n========== MENU DO CATEQUISTA ==========");
            System.out.println("1 - Cadastrar Catequisando");
            System.out.println("2 - Consultar Catequisandos");
            System.out.println("3 - Editar Catequisando");
            System.out.println("4 - Remover Catequisando");
            System.out.println("5 - Notícias");
            System.out.println("0 - Encerrar sessão");

            System.out.println("Escolha uma opção: ");

            opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1:
                    cadastrarCatequisando(scanner, catequista);
                    break;

                case 2:
                    consultarCatequisandos(scanner, catequista);
                    break;

                case 3:
                    editarCatequisando(scanner, catequista);
                    break;

                case 4:
                    removerCatequisando(scanner, catequista);
                    break;

                case 5:
                    menuNoticias(scanner, catequista);

                case 0:
                    System.out.println("Sessão encerrada");

                default:
                    System.out.println("Opção inválida.");
            }

        } while (opcao != 0);
    }

    //MENU CATEQUISANDO

    private void menuCatequisando(Scanner scanner, Catequisando catequisando) {

        int opcao;

        do {
            System.out.println("\n========== MENU DO CATEQUISANDO ==========");
            System.out.println("1 - Consultar minha turma");
            System.out.println("2 - Consultar notícias");
            System.out.println("0 - Encerrar sessão");
            System.out.print("Escolha uma opção: ");

            opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1:
                    consultarMinhaTurma(catequisando);
                    break;

                case 2:
                    consultarNoticias(catequisando);
                    break;

                case 0:
                    System.out.println("Sessão encerrada.");

                default:
                    System.out.println("Opção inválida.");
            }

        } while (opcao != 0);
    }

    // CONSULTA DE CATEQUISANDOS

    private void consultarCatequisandos(
            Scanner scanner, Catequista catequista) {

        System.out.println("\\n========== CONSULTAR CATEQUISANDOS ==========");

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
            String idTurma = scanner.nextLine().trim();

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

    private void menuNoticias(Scanner scanner, Usuario usuarioLogado) {
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

            opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1:
                    if (usuarioLogado instanceof Administrador
                            || usuarioLogado instanceof Catequista) {
                        cadastrarNoticia(scanner, usuarioLogado);

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
                        removerNoticia(scanner, usuarioLogado);
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

    private void cadastrarNoticia(Scanner scanner, Usuario usuarioLogado) {
        System.out.println("\n========== CADASTRO DE NOTÍCIA ==========");

        System.out.print("ID da notícia: ");
        String id = scanner.nextLine();

        System.out.print("Título: ");
        String titulo = scanner.nextLine();

        System.out.print("Conteúdo: ");
        String conteudo = scanner.nextLine();

        // Escolher a categoria da notícia
        System.out.println("\nCategorias disponíveis:");

        TipoNoticia[] categorias = TipoNoticia.values();

        for (int i = 0; i < categorias.length; i++) {
            System.out.println((i + 1) + " - " + categorias[i]);
        }

        System.out.print("Escolha a categoria: ");
        int opcaoCategoria = scanner.nextInt();
        scanner.nextLine();

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

                int opcaoTurma = scanner.nextInt();
                scanner.nextLine();

                if (opcaoTurma == 1) {
                    turmaSelecionada = null;

                } else if (opcaoTurma == 2) {
                    System.out.print("ID da turma: ");
                    String idTurma = scanner.nextLine();

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
                String idTurma = scanner.nextLine();

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

    private void removerNoticia(Scanner scanner, Usuario usuarioLogado) {
        System.out.println("\n========== REMOVER NOTÍCIA ==========");

        // Proteção adicional na interface.
        if (!(usuarioLogado instanceof Administrador)) {
            System.out.println(
                    "Somente o administrador pode remover notícias."
            );
            return;
        }

        System.out.print("Digite o ID da notícia que deseja remover: ");
        String idNoticia = scanner.nextLine();

        try {
            // Verifica se a notícia existe antes de removê-la.
            Noticia noticia = sistema.pesquisarNoticiaPorId(idNoticia);

            System.out.println("\nNotícia selecionada:");
            System.out.println(noticia);

            System.out.print("Confirma a remoção? (S/N): ");
            String confirmacao = scanner.nextLine();

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

        if (turma.getCatequista() != null) {
            System.out.println(
                    "Catequista responsável: "
                    + turma.getCatequista().getNome()
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
                        "Autor: " + (noticia.getAutor() == null
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

    private void cadastrarUsuario(Scanner scanner) {
        System.out.println("\n========== CADASTRO DE USUÁRIO ==========");
        System.out.println("1 - Catequista");
        System.out.println("2 - Catequisando");
        System.out.println("3 - Administrador");
        System.out.println("0 - Voltar");
        System.out.print("Escolha uma opção: ");

        int perfil = scanner.nextInt();
        scanner.nextLine();

        switch (perfil) {
            case 1:
                System.out.println("Perfil selecionado: Catequista.");
                cadastrarCatequista(scanner);
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

    private void cadastrarCatequista(Scanner scanner) {
        System.out.println("\n========== CADASTRO DO CATEQUISTA ==========");
        scanner.nextLine();

        System.out.println("ID do catequista: ");
        String idCatequista = scanner.nextLine();

        System.out.println("Nome: ");
        String nome = scanner.nextLine();

        System.out.println("Telefone: ");
        String telefone = scanner.nextLine();

        System.out.println("Idade: ");
        int idade = scanner.nextInt();
        scanner.nextLine();

        System.out.println("Senha: ");
        String senha = scanner.nextLine();

        System.out.println("Ano de ingresso na catequese: ");
        String anoIngresso = scanner.nextLine();

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

    // CADASTRO DE CATEQUISANDO

    private void cadastrarCatequisando(
            Scanner scanner, Catequista catequista) {

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
            String id = scanner.nextLine();

            System.out.println("Digite o nome do catequisando: ");
            String nome = scanner.nextLine();

            System.out.println("Digite o telefone do catequisando: ");
            String telefone = scanner.nextLine();

            System.out.println("Digite a idade: ");
            int idade = scanner.nextInt();
            scanner.nextLine();

            System.out.println("Digite a senha: ");
            String senha = scanner.nextLine();

            System.out.println("Digite o ID da turma: ");
            String idTurma = scanner.nextLine();

            Turma turmaSelecionada = null;

            for (Turma turma : turmasDisponiveis) {
                if (turma.getIdTurma().equals(idTurma)) {
                    turmaSelecionada = turma;
                    break;
                }
            }

            if (turmaSelecionada == null) {
                System.out.println(
                        "A turma  infomrada não pertence a você."
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
                        scanner.nextLine().trim().equalsIgnoreCase("S");
            }

            while (cadastrarResponsavel) {
                System.out.println("\n--- DADOS DO RESPONSÁVEL ---");

                System.out.println("Nome do responsável: ");
                String nomeResponsavel = scanner.nextLine();

                System.out.println("Telefone do responsável: ");
                String telefoneResponsavel = scanner.nextLine();

                System.out.println("Parentesco: ");
                String parentesco = scanner.nextLine();

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
                        scanner.nextLine().trim().equalsIgnoreCase("S");
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

    private void editarCatequisando(
            Scanner scanner, Catequista catequista) {

        System.out.println("\n========== EDITAR CATEQUISANDO ==========");

        try {
            // busca somente as turmas do catequista logado
            List<Turma> turmasDisponiveis = sistema.pesquisarTurmaPorCatequista(catequista.getId());

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

            System.out.println("\nDigite o ID da turma: ");
            String idTurma = scanner.nextLine().trim();

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

            System.out.println("\nDigite o ID do catequisando que deseja editar: ");
            String idCatequisando = scanner.nextLine().trim();

            // localiza o catequisando somente entre os da turma autorizada
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

            System.out.println("Novo telefone (atual: " + selecionado.getTelefone() + "): "
            );
            String telefone = scanner.nextLine().trim();

            System.out.println(
                    "Nova idade (atual: " + selecionado.getIdade() + "): "
            );
            int idade;

            try {
                idade = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Idade inválida. Informe um número inteiro.");
                return;
            }

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

            do {
                System.out.println("\n--- DADOS DO RESPONSÁVEL ---");

                System.out.println("Nome: ");
                String nomeResponsavel = scanner.nextLine();

                System.out.println("Telefone: ");
                String telefoneResponsavel = scanner.nextLine();

                System.out.println("Parentesco: ");
                String parentesco = scanner.nextLine();

                responsaveis.add(
                        new Responsavel(
                                nomeResponsavel,
                                telefoneResponsavel,
                                parentesco
                        )
                );

                System.out.println("Deseja adicionar outro responsável? (S/N): ");
                adicionarResponsavel =
                        scanner.nextLine().trim().equalsIgnoreCase("S");

            } while (adicionarResponsavel);

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

            System.out.println("\nDados atualizados com sucesso");

        } catch (CatequistaNaoEncontradoException
                | CatequisandoNaoEncontradoException e) {

            System.out.println("Erro: " + e.getMessage());

        } catch (IllegalArgumentException e) {
            System.out.println("Não foi possível atualizar: " + e.getMessage()

            );
        }
    }

    // REMOVER CATEQUISANDO

    private void removerCatequisando(Scanner scanner, Catequista catequista) {
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
            String idTurma = scanner.nextLine().trim();

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
            String idCatequisando = scanner.nextLine().trim();

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
            String confirmacao = scanner.nextLine().trim();

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

    // CADASTRO DE TURMA

    private void cadastrarTurma(Scanner scanner) {
        System.out.println("\n========== CADASTRO DE TURMA ==========");
        System.out.println("1 - Catequese Infantil");
        System.out.println("2 - Crisma Jovem");
        System.out.println("3 - Crisma Adulto");
        System.out.println("0 - Voltar");
        System.out.print("Escolha uma opção: ");

        int etapaTurma = scanner.nextInt();
        scanner.nextLine();

        TipoTurma tipoTurma = null;

        switch (etapaTurma) {
            case 1:
                System.out.println("Etapa selecionada: Catequese Infantil.");
                tipoTurma = TipoTurma.CATEQUESE;
                break;

            case 2:
                System.out.println("Etapa selecionada: Crisma Jovem.");
                tipoTurma = TipoTurma.CRISMA_JOVEM;
                break;

            case 3:
                System.out.println("Etapa selecionada: Crisma Adulto.");
                tipoTurma = TipoTurma.CRISMA_ADULTO;
                break;

            case 0:
                System.out.println("Retornando ao menu...");
                break;

            default:
                System.out.println("Opção inválida.");
                return;
        }

        System.out.println("Digite o ID da turma: ");
        String idTurma = scanner.nextLine();

        System.out.println("Digite o ID do catequista responsável: ");
        String catequistaId = scanner.nextLine();

        try {
            Catequista catequista = sistema.pesquisarCatequistaPorId(catequistaId);

            Turma turma = new Turma(idTurma, tipoTurma, catequista);

            sistema.cadastrarTurma(turma);

            System.out.println("Turma cadastrada com sucesso!");

        } catch (CatequistaNaoEncontradoException e) {
            System.out.println(e.getMessage());

        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
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
