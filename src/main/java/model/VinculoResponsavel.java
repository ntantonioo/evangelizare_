package model;

// Liga um responsável a um catequisando, com o grau de parentesco
public class VinculoResponsavel {

    private final Responsavel responsavel;
    private final String parentesco;

    public VinculoResponsavel(Responsavel responsavel, String parentesco) {
        if (responsavel == null) {
            throw new IllegalArgumentException(
                    "O responsável não pode ser nulo."
            );
        }

        if (parentesco == null || parentesco.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "O parentesco não pode ficar vazio."
            );
        }

        this.responsavel = responsavel;
        this.parentesco = parentesco.trim();
    }

    public Responsavel getResponsavel() {
        return responsavel;
    }

    public String getParentesco() {
        return parentesco;
    }
}