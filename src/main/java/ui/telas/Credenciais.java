package ui.telas;

import security.GeradorDeCredenciais;

/** Mostra, uma única vez, o acesso criado para alguém, para ser entregue a ela. */
final class Credenciais {

    private Credenciais() {
    }

    static void exibir(String nome, String login, String codigo) {
        System.out.println("\n--------------------------------------------------");
        System.out.println(" ACESSO CRIADO PARA: " + nome);
        System.out.println(" Usuário: " + login);
        System.out.println(" Código de primeiro acesso: " + codigo);
        System.out.println(
                " Vale por " + GeradorDeCredenciais.VALIDADE_DO_CODIGO_EM_DIAS
                        + " dias. No primeiro acesso, a própria pessoa cria a sua senha."
        );
        System.out.println(" Anote ou entregue agora: o código não será mostrado de novo.");
        System.out.println("--------------------------------------------------");
    }
}