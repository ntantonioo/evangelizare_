package model;

public class Responsavel extends Usuario {

    public Responsavel(
            String id,
            String nome,
            String telefone,
            int idade,
            String senha
    ) {
        super(
                validarCampo(id, "O ID"),
                validarCampo(nome, "O nome"),
                validarCampo(telefone, "O telefone"),
                validarIdade(idade),
                validarSenha(senha)
        );
    }

    private static String validarCampo(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    campo + " não pode ficar vazio."
            );
        }

        return valor.trim();
    }

    private static int validarIdade(int idade) {
        if (idade <= 0) {
            throw new IllegalArgumentException(
                    "A idade deve ser maior que zero."
            );
        }

        return idade;
    }

    private static String validarSenha(String senha) {
        if (senha == null || senha.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "A senha não pode ficar vazia."
            );
        }

        return senha;
    }

    @Override
    public String toString() {
        return "Responsavel{id='" + getId() + "', nome='" + getNome() + "'}";
    }
}