package model;

public class Catequisando extends Usuario{

    private Turma turma;

    public Catequisando(String id, String nome, int idade, Turma turma) {
        super(id, nome, idade);
        this.turma = turma;
    }

    public Turma getTurma() {
        return turma;
    }
    public void setTurma(Turma turma) {
        this.turma = turma;
    }
}
