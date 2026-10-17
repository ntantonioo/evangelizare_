package model;

public class Responsavel {

    private String nomeResponsavel;
    private String telefoneResponsavel;
    private String parentesco;

    public Responsavel(String nomeResponsavel, String telefoneResponsavel, String parentesco) {
        this.nomeResponsavel = nomeResponsavel;
        this.telefoneResponsavel = telefoneResponsavel;
        this.parentesco = parentesco;
    }

    public String getNomeResponsavel() {
        return nomeResponsavel;
    }

    public String getTelefoneResponsavel() {
        return telefoneResponsavel;
    }

    public String getParentesco() {
        return parentesco;
    }
}
