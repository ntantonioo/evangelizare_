package ui.telas;

import model.*;
import security.GeradorDeCredenciais;
import security.PoliticaDeSenha;
import service.SistemaEvangelizare;
import ui.Console;

public class TelaSenha {

    private final SistemaEvangelizare sistema;
    private final Console console;
    private final Seletor seletor;

    public TelaSenha(SistemaEvangelizare sistema, Console console) {
        this.sistema = sistema;
        this.console = console;
        this.seletor = new Seletor(console);
    }

    // ALTERAR A PRÓPRIA SENHA

    public void alterar(Usuario usuario) {
        System.out.println("\n========== ALTERAR MINHA SENHA ==========");

        System.out.println("Senha atual: ");
        String senhaAtual = console.lerSenha();

        String novaSenha = lerNovaSenhaConfirmada();

        if (novaSenha == null) {
            System.out.println("Alteração cancelada.");
            return;
        }

        try {
            sistema.alterarSenha(usuario.getId(), senhaAtual, novaSenha);

            System.out.println("\nSenha alterada com sucesso!");

        } catch (IllegalArgumentException e) {
            System.out.println("\nNão foi possível alterar: " + e.getMessage());
        }
    }

    // TROCA OBRIGATÓRIA (senha provisória)

    /**
     * Exige que o usuário defina uma nova senha antes de entrar no sistema.
     * Devolve true se a senha foi trocada, ou false se ele cancelou.
     */
    public boolean trocarSenhaProvisoria(Usuario usuario, String senhaAtual) {
        System.out.println("\nEste é o seu primeiro acesso (ou o seu código foi renovado).");
        System.out.println("Crie a sua senha para continuar.");

        while (true) {
            String novaSenha = lerNovaSenhaConfirmada();

            if (novaSenha == null) {
                System.out.println("Troca cancelada. O acesso não foi liberado.");
                return false;
            }

            try {
                sistema.alterarSenha(usuario.getId(), senhaAtual, novaSenha);

                System.out.println("\nSenha alterada com sucesso!");
                return true;

            } catch (IllegalArgumentException e) {
                System.out.println("\nNão foi possível alterar: " + e.getMessage());
            }
        }
    }

    // GERAR UM NOVO CÓDIGO DE ACESSO PARA OUTRO USUÁRIO (administrador)

    public void redefinir(Usuario administrador) {
        System.out.println("\n========== REDEFINIR SENHA DE USUÁRIO ==========");
        System.out.println("Gera um novo código de primeiro acesso. A pessoa criará uma nova senha.");

        Usuario usuario = seletor.buscarEEscolher(
                "usuário",
                sistema::pesquisarUsuarioPorNome,
                u -> u.getNome() + " | " + perfil(u) + " | usuário: " + u.getLogin()
        );

        if (usuario == null) {
            System.out.println("Operação cancelada.");
            return;
        }

        String codigo = GeradorDeCredenciais.novoCodigo();

        try {
            sistema.redefinirSenha(usuario.getId(), codigo, administrador);

            System.out.println("\nNovo código gerado com sucesso!");
            Credenciais.exibir(usuario.getNome(), usuario.getLogin(), codigo);

        } catch (IllegalArgumentException e) {
            System.out.println("\nNão foi possível redefinir: " + e.getMessage());
        }
    }

    private static String perfil(Usuario usuario) {
        if (usuario instanceof Administrador) {
            return "Administrador";
        } else if (usuario instanceof Catequista) {
            return "Catequista";
        } else if (usuario instanceof Responsavel) {
            return "Responsável";
        }

        return "Catequisando";
    }

    // Devolve a nova senha confirmada, ou null se o usuário deixou em branco
    // (cancelar). Se as duas digitações não conferem, avisa e pergunta de novo.
    private String lerNovaSenhaConfirmada() {
        System.out.println(PoliticaDeSenha.DESCRICAO);

        while (true) {
            System.out.println("Nova senha (em branco para cancelar): ");
            String novaSenha = console.lerSenha();

            if (novaSenha.isEmpty()) {
                return null;
            }

            System.out.println("Confirme a nova senha: ");
            String confirmacao = console.lerSenha();

            if (novaSenha.equals(confirmacao)) {
                return novaSenha;
            }

            System.out.println("As senhas não conferem. Tente novamente.");
        }
    }
}