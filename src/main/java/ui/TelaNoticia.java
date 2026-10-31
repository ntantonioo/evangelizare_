package ui;

import java.util.List;

import exception.CatequistaNaoEncontradoException;
import exception.NoticiaNaoEncontradaException;
import exception.TurmaNaoEncontradaException;

import model.*;
import service.SistemaEvangelizare;

public class TelaNoticia {

    private final SistemaEvangelizare sistema;
    private final Console console;

    public TelaNoticia(SistemaEvangelizare sistema, Console console) {
        this.sistema = sistema;
        this.console = console;
    }

    // MENU NOTICIAS

    public void menu(Usuario usuarioLogado) {
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
                        cadastrar(usuarioLogado);

                    } else if (usuarioLogado instanceof Catequisando) {
                        consultar(usuarioLogado);
                    }
                    break;

                case 2:
                    if (usuarioLogado instanceof Administrador
                            || usuarioLogado instanceof Catequista) {
                        consultar(usuarioLogado);
                    } else {
                        System.out.println("Opção inválida.");
                    }
                    break;

                case 3:
                    if (usuarioLogado instanceof Administrador) {
                        remover(usuarioLogado);
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

    private void consultar(Usuario usuarioLogado) {
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

    private void cadastrar(Usuario usuarioLogado) {
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

    private void remover(Usuario usuarioLogado) {
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

    // CONSULTAR NOTICIAS

    public void consultarDisponiveis(Catequisando catequisando) {
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
}