package security;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Gera o que antes era digitado à mão: o ID interno de cada usuário, o nome de
 * usuário (login) e o código de primeiro acesso.
 */
public final class GeradorDeCredenciais {

    // Sem 0, O, 1, I e L, que se confundem quando o código é lido ou ditado.
    private static final String ALFABETO = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    /** Por quantos dias o código de primeiro acesso vale. */
    public static final int VALIDADE_DO_CODIGO_EM_DIAS = 7;

    private static final int TAMANHO_DO_CODIGO = 8;
    private static final Set<String> PARTICULAS = Set.of("de", "da", "do", "dos", "das", "e");

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private GeradorDeCredenciais() {
    }

    /** ID interno: ninguém digita nem escolhe, e ninguém consegue adivinhar. */
    public static String novoId() {
        return UUID.randomUUID().toString();
    }

    /** Código de primeiro acesso, como K7M2-9QXA. */
    public static String novoCodigo() {
        StringBuilder codigo = new StringBuilder();

        for (int i = 0; i < TAMANHO_DO_CODIGO; i++) {
            if (i == TAMANHO_DO_CODIGO / 2) {
                codigo.append('-');
            }

            codigo.append(ALFABETO.charAt(ALEATORIO.nextInt(ALFABETO.length())));
        }

        return codigo.toString();
    }

    /**
     * Base do nome de usuário a partir do nome: sem acentos, minúscula, primeiro
     * e último nome separados por ponto. "João da Silva Pereira" vira "joao.pereira".
     * Quem escolhe entre repetidos (maria.souza, maria.souza2) é o serviço.
     */
    public static String baseDeLogin(String nome) {
        String semAcentos = Normalizer
                .normalize(nome == null ? "" : nome, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        List<String> palavras = new ArrayList<>();

        for (String palavra : semAcentos.toLowerCase().split("\\s+")) {
            String limpa = palavra.replaceAll("[^a-z0-9]", "");

            if (!limpa.isEmpty() && !PARTICULAS.contains(limpa)) {
                palavras.add(limpa);
            }
        }

        if (palavras.isEmpty()) {
            return "usuario";
        }

        String base = palavras.size() == 1
                ? palavras.get(0)
                : palavras.get(0) + "." + palavras.get(palavras.size() - 1);

        while (base.length() < 3) {
            base += "0";
        }

        return base.length() > 40 ? base.substring(0, 40) : base;
    }
}