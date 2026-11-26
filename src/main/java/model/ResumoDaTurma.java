package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Tudo sobre a turma num lugar
public class ResumoDaTurma {

    private final Turma turma;
    private final List<Catequista> catequistas;
    private final List<Catequisando> catequisandos;

    public ResumoDaTurma(
            Turma turma, List<Catequista> catequistas, List<Catequisando> catequisandos) {
        this.turma = turma;
        this.catequistas = new ArrayList<>(catequistas);
        this.catequisandos = new ArrayList<>(catequisandos);
    }

    public Turma getTurma() {
        return turma;
    }

    public List<Catequista> getCatequistas() {
        return new ArrayList<>(catequistas);
    }

    public List<Catequisando> getCatequisandos() {
        return new ArrayList<>(catequisandos);
    }

    public int getTotalDeCatequisandos() {
        return catequisandos.size();
    }

    public int getTotalDeResponsaveis() {
        Set<String> ids = new HashSet<>();

        for (Catequisando catequisando : catequisandos) {
            for (Responsavel responsavel : catequisando.getResponsaveis()) {
                ids.add(responsavel.getId());
            }
        }

        return ids.size();
    }
}