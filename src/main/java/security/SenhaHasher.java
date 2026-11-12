package seguranca;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class SenhaHasher {

    private static final String PREFIXO = "pbkdf2-sha256";
    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    // 600 mil é o valor de uso real. Os testes automatizados reduzem este valor
    // (mínimo 1000) pela propriedade evangelizare.hash.iteracoes, para não
    // ficarem lentos; em produção essa propriedade não deve ser definida.
    private static final int ITERACOES = Math.max(1_000,
            Integer.getInteger("evangelizare.hash.iteracoes", 600_000));
    private static final int BYTES_SALT = 16;
    private static final int BITS_HASH = 256;
    private static final int MAX_ITERACOES = 10_000_000;

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private SenhaHasher() {
    }

    public static String gerarHash(String senha) {
        if (senha == null || senha.isEmpty()) {
            throw new IllegalArgumentException("A senha não pode ficar vazia.");
        }

        byte[] salt = new byte[BYTES_SALT];
        ALEATORIO.nextBytes(salt);

        byte[] hash = derivar(senha, salt, ITERACOES, BITS_HASH);

        return PREFIXO + "$" + ITERACOES + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    // Retorna true se a senha digitada corresponde ao hash guardado
    public static boolean verificar(String senhaDigitada, String hashGuardado) {
        if (senhaDigitada == null || !ehHash(hashGuardado)) {
            return false;
        }

        String[] partes = hashGuardado.split("\\$");

        try {
            int iteracoes = Integer.parseInt(partes[1]);
            byte[] salt = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);

            if (iteracoes < 1 || iteracoes > MAX_ITERACOES || esperado.length == 0) {
                return false;
            }

            byte[] calculado = derivar(
                    senhaDigitada, salt, iteracoes, esperado.length * 8);

            // Comparação em tempo constante: não revela, pelo tempo de resposta,
            // quantos bytes iniciais estavam certos.
            return MessageDigest.isEqual(calculado, esperado);

        } catch (IllegalArgumentException e) {
            // hash guardado malformado
            return false;
        }
    }

    // Diz se o texto tem o formato de um hash gerado por esta classe
    public static boolean ehHash(String valor) {
        return valor != null
                && valor.startsWith(PREFIXO + "$")
                && valor.split("\\$").length == 4;
    }

    private static byte[] derivar(String senha, byte[] salt, int iteracoes, int bits) {
        PBEKeySpec especificacao =
                new PBEKeySpec(senha.toCharArray(), salt, iteracoes, bits);

        try {
            return SecretKeyFactory.getInstance(ALGORITMO)
                    .generateSecret(especificacao)
                    .getEncoded();

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Não foi possível calcular o hash da senha.", e);

        } finally {
            especificacao.clearPassword();
        }
    }
}