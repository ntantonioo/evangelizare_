
package model;

import java.util.ArrayList;
import java.util.List;

public class Turma {

    private String idTurma;
    private TipoTurma tipoTurma;
    private List<Catequista> catequistas;

    public Turma(
            String idTurma,
            TipoTurma tipoTurma,
            Catequista catequista) {

        this.idTurma = idTurma;
        this.tipoTurma = tipoTurma;
        this.catequistas = new ArrayList<>();

        if (catequista != null) {
            this.catequistas.add(catequista);
        }
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

    // Retorna o primeiro catequista, mantendo compatibilidade
    // com os trechos antigos do projeto.
    public Catequista getCatequista() {
        if (catequistas.isEmpty()) {
            return null;
        }

        return catequistas.get(0);
    }

    // Atualiza o primeiro catequista sem remover o segundo.
    public void setCatequista(Catequista catequista) {
        if (catequista == null) {
            throw new IllegalArgumentException(
                    "O catequista não pode ser nulo."
            );
        }

        if (catequistas.isEmpty()) {
            catequistas.add(catequista);
        } else {
            catequistas.set(0, catequista);
        }
    }

    // Retorna uma cópia da lista de catequistas.
    public List<Catequista> getCatequistas() {
        return new ArrayList<>(catequistas);
    }

    // Adiciona um segundo catequista, respeitando o limite de dois.
    public void adicionarCatequista(Catequista catequista) {
        if (catequista == null) {
            throw new IllegalArgumentException(
                    "O catequista não pode ser nulo."
            );
        }

        if (catequistas.size() >= 2) {
            throw new IllegalArgumentException(
                    "A turma já possui dois catequistas."
            );
        }

        for (Catequista existente : catequistas) {
            if (existente.getId().equals(catequista.getId())) {
                throw new IllegalArgumentException(
                        "Este catequista já está associado à turma."
                );
            }
        }

        catequistas.add(catequista);
    }

    @Override
    public String toString() {
        List<String> nomes = new ArrayList<>();

        for (Catequista catequista : catequistas) {
            nomes.add(catequista.getNome());
        }

        return "Turma{" +
                "idTurma='" + idTurma + '\'' +
                ", tipoTurma=" + tipoTurma +
                ", catequistas=" + nomes +
                '}';
    }
}
