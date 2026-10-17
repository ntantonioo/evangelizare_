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
}
