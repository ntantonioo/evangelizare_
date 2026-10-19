package model;

import java.util.ArrayList;
import java.util.List;

public class Catequisando extends Usuario{

    private Turma turma;
    private List<Responsavel> responsaveis;

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
        this.responsaveis = new ArrayList<>();
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

    public void atualizarResponsaveis(List<Responsavel> responsaveis) {
        if (responsaveis == null) {
            throw new IllegalArgumentException(
                    "A lista de responsáveis não pode ser nula."
            );
        }

        List<Responsavel> novaLista = new ArrayList<>();

        for (Responsavel responsavel : responsaveis) {
            if (responsavel == null) {
                throw new IllegalArgumentException(
                        "A lista não pode conter responsáveis nulos."
                );
            }

            novaLista.add(responsavel);
        }

        this.responsaveis = novaLista;
    }

    public List<Responsavel> getResponsaveis() {
        return new ArrayList<>(responsaveis);
    }

    public void adicionarResponsavel(Responsavel responsavel) {
        if (responsavel == null) {
            throw new IllegalArgumentException(
                    "O responsável não pode ser nulo"
            );
        }

        responsaveis.add(responsavel);
    }

    @Override
    public String toString() {
        return "Catequisando{" +
                "id='" + getId() + '\'' +
                ", nome='" + getNome() + '\'' +
                ", telefone='" + getTelefone() + '\'' +
                ", idade=" + getIdade() +
                ", turma=" + turma +
                ", quantidadeResponsaveis=" + responsaveis.size() +
                '}';
    }
}
