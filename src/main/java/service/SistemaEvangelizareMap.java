package service;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import exception.CatequistaNaoExisteException;
import exception.CatequisandoNaoExisteException;
import exception.TurmaNaoExisteException;
import model.Catequisando;
import model.Catequista;
import model.Turma;

public class SistemaEvangelizareMap implements  SistemaEvangelizare {

    private Map<String, Catequista> catequistas = new HashMap<>();
    private Map<String, Catequisando> catequisandos = new HashMap<>();
    private Map<String, Turma> turmas = new HashMap<>();

    //CATEQUISANDO

    @Override
    public void cadastrarCatequista(Catequista catequista) {
        catequistas.put(catequista.getId(), catequista);
    }

    @Override
    public void removerCatequista(String id) throws CatequistaNaoExisteException {
        if (!catequistas.containsKey(id)) {
            throw new CatequistaNaoExisteException("Catequista não encontrado: " + id);
        }
        catequistas.remove(id);
    }

    @Override
    public Catequista pesquisarCatequistaPorId(String id) throws CatequistaNaoExisteException {
        Catequista catequista = catequistas.get(id);
        if (catequista == null) {
            throw new CatequistaNaoExisteException("Vendedor não encontrado: " + id);
        }
        return catequista;
    }

    @Override
    public List<Catequista> pesquisarCatequistaPorNome(String nome) {
        return catequistas.values().stream()
                .filter(c -> c.getNome().toLowerCase().contains(nome.toLowerCase()))
                .collect(Collectors.toList());
    }

    //CATEQUISANDO

    @Override
    public void cadastrarCatequisando(Catequisando catequisando) {
        catequisandos.put(catequisando.getId(), catequisando);
    }

    @Override
    public void removerCatequisando(String id) throws CatequisandoNaoExisteException {
        if (!catequisandos.containsKey(id)) {
            throw new CatequisandoNaoExisteException("Catequisando nao encontrado: " + id );
        }
        catequisandos.remove(id);
    }

    @Override
    public Catequisando pesquisarCatequisandoPorId(String id) throws CatequisandoNaoExisteException {
        Catequisando catequisando = catequisandos.get(id);
        if (catequisando == null) {
            throw new CatequisandoNaoExisteException("Catequisando nao encontrado: " + id);
        }
        return catequisando;
    }

    @Override
    public List<Catequisando> pesquisarCatequisandoPorNome(String nome) {
        return catequisandos.values().stream()
                .filter(c -> c.getNome().toLowerCase().contains(nome.toLowerCase()))
                .collect(Collectors.toList());
    }

    //TURMA

    @Override
    public void cadastrarTurma(Turma turma)
            throws CatequisandoNaoExisteException, CatequistaNaoExisteException {

        pesquisarCatequisandoPorId(turma.getCatequisando().getId());
        pesquisarCatequistaPorId(turma.getCatequista().getId());
        turmas.put(turma.getIdTurma(), turma);

    }

}
