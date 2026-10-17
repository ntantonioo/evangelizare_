package model;

import java.util.ArrayList;
import java.util.List;

public class Catequisando extends Usuario{

    private Turma turma;
    private List<Responsavel> responsaveis;

    public Catequisando(String id, String nome, String telefone, int idade, String senha, Turma turma, Responsavel responsaveis) {
        super(id, nome, telefone, idade, senha);
        this.turma = turma;
        this.responsaveis = new ArrayList<>();
    }

    public Turma getTurma() {
        return turma;
    }
    public void setTurma(Turma turma) {
        this.turma = turma;
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
                '}';
    }
}
