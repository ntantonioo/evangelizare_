package ui;

import java.util.List;
import java.util.Scanner;

import exception.CatequistaNaoEncontradoException;
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

    private void menuAdministrador(Scanner scanner, Usuario usuario) {
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
            System.out.println("2 - Notícias");
            System.out.println("0 - Encerrar sessão");
            System.out.println("Escolha uma opção: ");

            opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1:
                    cadastrarCatequisando(scanner, catequista);
                    break;

                case 2:
                    menuNoticias(scanner, catequista);
                    break;

                case 0:
                    System.out.println("Sessão encerrada.");
                    break;

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
                    menuNoticias(scanner, catequisando);
                    break;

                case 0:
                    System.out.println("Sessão encerrada.");

                default:
                    System.out.println("Opção inválida.");
            }

        } while (opcao != 0);
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
        String id = scanner.nextLine();

        System.out.println("Nome: ");
        String nome = scanner.nextLine();

        System.out.println("Idade: ");
        int idade = scanner.nextInt();
        scanner.nextLine();

        System.out.println("Senha: ");
        String senha = scanner.nextLine();

        System.out.println("Ano de ingresso na catequese: ");
        String anoIngresso = scanner.nextLine();

        Catequista catequista = new Catequista(
                id,
                nome,
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
                    id, nome, idade, senha, turmaSelecionada
            );

            sistema.cadastrarCatequisando(novoCatequisando);

            System.out.println("Catequisando cadastrado com sucesso!");

        } catch (CatequistaNaoEncontradoException | TurmaNaoEncontradaException e) {

            System.out.println(e.getMessage());

        }  catch (IllegalArgumentException e) {
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

        } catch (TurmaNaoEncontradaException e) {
            System.out.println(e.getMessage());

        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void cadastrarAdministradorInicial() {
        Administrador administrador = new Administrador(
                "ADM001",
                "Administrador",
                30,
                "admin123"
        );

        sistema.cadastrarAdministrador(administrador);
    }
}
