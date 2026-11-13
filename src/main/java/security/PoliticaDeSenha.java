package security;

public class PoliticaDeSenha {

    public static final int TAMANHO_MINIMO = 8;

    public static final String DESCRICAO =
            "A senha deve ter pelo menos " + TAMANHO_MINIMO
                + " caracteres, com letras e números.";

    private PoliticaDeSenha() {
    }

    // lança IllegalArgumentException se a senha nao atende à regra
    public static void validar(String senha) {
        if (senha == null || senha.length() < TAMANHO_MINIMO) {
            throw new IllegalArgumentException(
                    "A senha deve ter pelo menos " + TAMANHO_MINIMO + " caracteres."
            );
        }

        boolean temLetra = senha.chars().anyMatch(Character::isLetter);
        boolean temNumero = senha.chars().anyMatch(Character::isDigit);

        if (!temLetra || !temNumero) {
            throw new IllegalArgumentException(
                    "A senha deve ter letras e números."
            );
        }
    }
}
