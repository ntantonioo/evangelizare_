package model;

public class Responsavel {

    private String nomeResponsavel;
    private String telefoneResponsavel;
    private String parentesco;

    public Responsavel(
            String nomeResponsavel,
            String telefoneResponsavel,
            String parentesco) {

        validarCampo(nomeResponsavel, "O nome do responsável");
        validarCampo(telefoneResponsavel, "O telefone do responsável");
        validarCampo(parentesco, "O parentesco");

        this.nomeResponsavel = nomeResponsavel;
        this.telefoneResponsavel = telefoneResponsavel;
        this.parentesco = parentesco;
    }

    private void validarCampo(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    campo + " não pode ficar vazio."
            );
        }
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
