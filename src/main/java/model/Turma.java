package model;

public class Turma {

    private String idTurma;
    private TipoTurma tipoTurma;
    private Catequisando catequisando;
    private Catequista catequista;

    public Turma(String idTurma, TipoTurma tipoTurma, Catequisando catequisando,
                 Catequista catequista) {

        this.idTurma =  idTurma;
        this.tipoTurma = tipoTurma;
        this.catequisando = catequisando;
        this.catequista = catequista;
    }

    public String getIdTurma() {
        return idTurma;
    }
    public void setIdTurma(String idTurma) {
        this.idTurma = idTurma;
    }
    public TipoTurma getTipoTurma() {
        return tipoTurma;
    }
    public void setTipoTurma(TipoTurma tipoTurma) {
        this.tipoTurma = tipoTurma;
    }
    public Catequisando getCatequisando() {
        return catequisando;
    }
    public void setCatequisando(Catequisando catequisando) {
        this.catequisando = catequisando;
    }
    public Catequista getCatequista() {
        return catequista;
    }
    public void setCatequista(Catequista catequista) {
        this.catequista = catequista;
    }

    @Override
    public String toString() {
        return "Turma{" +
                "id='" + idTurma + '\'' +
                ", catequisando=" + catequisando.getNome() +
                ", catequista=" + catequista.getNome() +
                ", etapa da catequese=" + tipoTurma +
                '}';
    }
}
