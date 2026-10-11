package service;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import exception.CatequistaNaoEncontradoException;
import exception.CatequisandoNaoEncontradoException;
import exception.TurmaNaoEncontradoException;
import model.Catequisando;
import model.Catequista;
import model.Turma;

public class SistemaEvangelizareMap implements  SistemaEvangelizare {

    private Map<String, Catequista> catequistas = new HashMap<>();
    private Map<String, Catequisando> catequisandos = new HashMap<>();
    private Map<String, Turma> turmas = new HashMap<>();

    //CATEQUISTA

    @Override
    public void cadastrarCatequista(Catequista catequista) {
        catequistas.put(catequista.getId(), catequista);
    }

    @Override
    public void removerCatequista(String id) throws CatequistaNaoEncontradoException {
        if (!catequistas.containsKey(id)) {
            throw new CatequistaNaoEncontradoException("Catequista não encontrado: " + id);
        }
        catequistas.remove(id);
    }

    @Override
    public Catequista pesquisarCatequistaPorId(String id) throws CatequistaNaoEncontradoException {
        Catequista catequista = catequistas.get(id);
        if (catequista == null) {
            throw new CatequistaNaoEncontradoException("Vendedor não encontrado: " + id);
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
    public void removerCatequisando(String id) throws CatequisandoNaoEncontradoException {
        if (!catequisandos.containsKey(id)) {
            throw new CatequisandoNaoEncontradoException("Catequisando nao encontrado: " + id);
        }
        catequisandos.remove(id);
    }

    @Override
    public Catequisando pesquisarCatequisandoPorId(String id) throws CatequisandoNaoEncontradoException {
        Catequisando catequisando = catequisandos.get(id);
        if (catequisando == null) {
            throw new CatequisandoNaoEncontradoException("Catequisando nao encontrado: " + id);
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
            throws CatequisandoNaoEncontradoException, CatequistaNaoEncontradoException {

        pesquisarCatequisandoPorId(turma.getCatequisando().getId());
        pesquisarCatequistaPorId(turma.getCatequista().getId());
        turmas.put(turma.getIdTurma(), turma);
    }

    @Override
    public void removerTurma(String idTurma) throws TurmaNaoEncontradoException {
        Turma turma = turmas.get(idTurma);

        if (turma == null) {
            throw new TurmaNaoEncontradoException(
                    "Turma nao encontrada: " + idTurma
            );
        }

        Catequisando catequisando = turma.getCatequisando();

        if (catequisando != null) {
            catequisando.setTurma(null);
        }

        turmas.remove(idTurma);
    }

    @Override
    public List<Turma> pesquisarTurmaPorId(String idTurma)
            throws TurmaNaoEncontradoException {

        Turma turma = turmas.get(idTurma);

        if (turma == null) {
            throw new TurmaNaoEncontradoException(
                    "Turma nao encontrada: " + idTurma
            );
        }

        List<Turma> resultado = new ArrayList<>();
        resultado.add(turma);

        return resultado;
    }

    @Override
    public List<Turma> pesquisarTurmaPorTipo(String tipoTurma)
            throws TurmaNaoEncontradoException {

        List<Turma> resultado = new ArrayList<>();

        for (Turma turma : turmas.values()) {
            if (turma.getTipoTurma().name().equalsIgnoreCase(tipoTurma)) {
                resultado.add(turma);
            }
        }

        if (resultado.isEmpty()) {
            throw new TurmaNaoEncontradoException(
                    "Nenhuma turma encontrada do tipo: " + tipoTurma
            );
        }

        return resultado;
    }

    @Override
    public List<Turma> pesquisarTurmaPorCatequista(String catequistaId) throws CatequistaNaoEncontradoException, TurmaNaoEncontradoException {
        return turmas.values().stream()
                .filter(c -> c.getCatequista().getId().equals(catequistaId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Turma> pesquisarTurmaPorCatequisando(String catequisandoId) throws CatequisandoNaoEncontradoException, TurmaNaoEncontradoException {
        return turmas.values().stream()
                .filter(c -> c.getCatequisando().getId().equals(catequisandoId))
                .collect(Collectors.toList());
    }

}
