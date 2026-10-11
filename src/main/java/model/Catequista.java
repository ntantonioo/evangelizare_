package model;

public class Catequista extends Usuario {

    private String anoDeIngresso;

    public Catequista(String id, String nome, int idade, String senha, String anoDeIngresso) {
        super(id, nome, idade, senha);
        this.anoDeIngresso = anoDeIngresso;
    }

    public String getAnoDeIngresso() {
        return anoDeIngresso;
    }
    public void setAnoDeIngresso(String anoDeIngresso) {
        this.anoDeIngresso = anoDeIngresso;
    }
}
