package ui.telas;

import model.Usuario;
import security.PoliticaDeSenha;
import service.SistemaEvangelizare;
import ui.Console;

public class TelaSenha {

    private final SistemaEvangelizare sistema;
    private final Console console;

    public TelaSenha(SistemaEvangelizare sistema, Console console) {
        this.sistema = sistema;
        this.console = console;
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

    public boolean trocarSenhaProvisoria(Usuario usuario, String senhaAtual) {
        System.out.println("\nSua senha é provisória. Defina uma nova senha para continuar.");

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

    // REDEFINIR A SENHA DE OUTRO USUÁRIO (administrador)

    public void redefinir(Usuario administrador) {
        System.out.println("\n========== REDEFINIR SENHA DE USUÁRIO ==========");

        System.out.println("ID do usuário: ");
        String id = console.lerTexto().trim();

        System.out.println("Senha provisória (o usuário terá de trocá-la ao entrar): ");
        String provisoria = console.lerSenha();

        System.out.println("Confirme a senha provisória: ");
        String confirmacao = console.lerSenha();

        if (!provisoria.equals(confirmacao)) {
            System.out.println("\nAs senhas não conferem. Operação cancelada.");
            return;
        }

        try {
            sistema.redefinirSenha(id, provisoria, administrador);

            System.out.println("\nSenha redefinida com sucesso!");

        } catch (IllegalArgumentException e) {
            System.out.println("\nNão foi possível redefinir: " + e.getMessage());
        }
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