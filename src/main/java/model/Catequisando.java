package model;

import java.util.ArrayList;
import java.util.List;

public class Catequisando extends Usuario{

    private Turma turma;
    private List<VinculoResponsavel> vinculos;

    public Catequisando(
            String id,
            String nome,
            String telefone,
            int idade,
            String senha,
            Turma turma
    ) {
        super(
                validarCampo(id, "O ID"),
                validarCampo(nome, "O nome"),
                validarCampo(telefone, "O telefone"),
                validarIdade(idade),
                validarCampo(senha, "a senha")
        );

        if (turma == null) {
            throw new IllegalArgumentException(
                    "A turma deve ser informada."
            );
        }

        this.turma = turma;
        this.vinculos = new ArrayList<>();
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
        if (idade <= 0 ) {
            throw new IllegalArgumentException(
                    "A idade deve ser maior que zero."
            );
        }

        return idade;
    }

    public Turma getTurma() {
        return turma;
    }
    public void setTurma(Turma turma) {
        if (turma == null) {
            throw new IllegalArgumentException(
                    "A turma não pode ser nula."
            );
        }

        this.turma = turma;
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

    // Substitui todos os responsáveis. Valida a lista inteira antes de alterar
    public void atualizarResponsaveis(List<VinculoResponsavel> novosVinculos) {
        if (novosVinculos == null) {
            throw new IllegalArgumentException(
                    "A lista de responsáveis não pode ser nula."
            );
        }

        List<VinculoResponsavel> novaLista = new ArrayList<>();

        for (VinculoResponsavel vinculo : novosVinculos) {
            if (vinculo == null) {
                throw new IllegalArgumentException(
                        "A lista não pode conter responsáveis nulos."
                );
            }

            if (contem(novaLista, vinculo.getResponsavel().getId())) {
                throw new IllegalArgumentException(
                        "O responsável " + vinculo.getResponsavel().getNome()
                                + " foi informado mais de uma vez."
                );
            }

            novaLista.add(vinculo);
        }

        this.vinculos = novaLista;
    }

    public void adicionarResponsavel(Responsavel responsavel, String parentesco) {
        VinculoResponsavel vinculo = new VinculoResponsavel(responsavel, parentesco);

        if (contem(vinculos, responsavel.getId())) {
            throw new IllegalArgumentException(
                    "Este responsável já está vinculado ao catequisando."
            );
        }

        vinculos.add(vinculo);
    }

    public List<VinculoResponsavel> getVinculos() {
        return new ArrayList<>(vinculos);
    }

    public List<Responsavel> getResponsaveis() {
        List<Responsavel> lista = new ArrayList<>();

        for (VinculoResponsavel vinculo : vinculos) {
            lista.add(vinculo.getResponsavel());
        }

        return lista;
    }

    public boolean possuiResponsavel(String idResponsavel) {
        return contem(vinculos, idResponsavel);
    }

    /** Parentesco com o responsável informado, ou null se não for responsável dele. */
    public String getParentescoDe(String idResponsavel) {
        for (VinculoResponsavel vinculo : vinculos) {
            if (vinculo.getResponsavel().getId().equals(idResponsavel)) {
                return vinculo.getParentesco();
            }
        }

        return null;
    }

    private static boolean contem(List<VinculoResponsavel> lista, String idResponsavel) {
        return lista.stream()
                .anyMatch(v -> v.getResponsavel().getId().equals(idResponsavel));
    }

    @Override
    public String toString() {
        return "Catequisando{" +
                "id='" + getId() + '\'' +
                ", nome='" + getNome() + '\'' +
                ", telefone='" + getTelefone() + '\'' +
                ", idade=" + getIdade() +
                ", turma=" + turma +
                ", quantidadeResponsaveis=" + vinculos.size() +
                '}';
    }
}