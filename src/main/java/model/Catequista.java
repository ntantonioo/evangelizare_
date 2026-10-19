package model;

public class Catequista extends Usuario {

    private String anoDeIngresso;

    public Catequista(
            String id,
            String nome,
            String telefone,
            int idade,
            String senha,
            String anoDeIngresso
    ) {
        super(id, nome, telefone, idade, senha);
        this.anoDeIngresso = anoDeIngresso;
    }

    public String getAnoDeIngresso() {
        return anoDeIngresso;
    }
    public void setAnoDeIngresso(String anoDeIngresso) {
        this.anoDeIngresso = anoDeIngresso;
    }

    public void atualizarTelefone(String telefone) {
        if (telefone == null || telefone.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "O telefone não pode ficar vazio"
            );
        }

        setTelefone(telefone.trim());
    }

    public void atualizarIdade(int idade) {
        if (idade <= 0) {
            throw new IllegalArgumentException(
                    "A idade deve ser maior que zero."
            );
        }

        setIdade(idade);
    }

    public void atualizarAnoDeIngresso(String anoDeIngresso) {
        if (anoDeIngresso == null || anoDeIngresso.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "O ano de ingresso não pode ficar vazio."
            );
        }

        this.anoDeIngresso = anoDeIngresso;
    }
}
