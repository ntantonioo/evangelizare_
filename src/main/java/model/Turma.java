
package model;

public class Turma {

    private String idTurma;
    private TipoTurma tipoTurma;
    private Catequista catequista;

    public Turma(
            String idTurma,
            TipoTurma tipoTurma,
            Catequista catequista) {
        this.idTurma = idTurma;
        this.tipoTurma = tipoTurma;
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

    public Catequista getCatequista() {
        return catequista;
    }

    public void setCatequista(Catequista catequista) {
        this.catequista = catequista;
    }

    @Override
    public String toString() {
        return "Turma{" +
                "idTurma='" + idTurma + '\'' +
                ", tipoTurma=" + tipoTurma +
                ", catequista=" +
                (catequista != null ? catequista.getNome() : "Não definido") +
                '}';
    }
}
