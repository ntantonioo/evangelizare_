package service;

import java.util.List;

import exception.CatequisandoNaoEncontradoException;
import exception.CatequistaNaoEncontradoException;
import exception.TurmaNaoEncontradoException;
import model.Catequisando;
import model.Catequista;
import model.Turma;

public interface SistemaEvangelizare {

    // Catequista
    void cadastrarCatequista(Catequista catequista);

    void removerCatequista(String id) throws CatequistaNaoEncontradoException;

    Catequista pesquisarCatequistaPorId(String id) throws CatequistaNaoEncontradoException;

    List<Catequista> pesquisarCatequistaPorNome(String nome);

    // Catequisando
    void cadastrarCatequisando (Catequisando catequisando);

    void removerCatequisando (String id) throws CatequisandoNaoEncontradoException;

    Catequisando pesquisarCatequisandoPorId(String id) throws CatequisandoNaoEncontradoException;

    List<Catequisando> pesquisarCatequisandoPorNome(String nome);

    //Turma
    void cadastrarTurma(Turma turma)
        throws TurmaNaoEncontradoException, CatequistaNaoEncontradoException, CatequisandoNaoEncontradoException;

    void removerTurma(String idTurma) throws TurmaNaoEncontradoException;

    List<Turma> pesquisarTurmaPorId(String idTurma) throws TurmaNaoEncontradoException;

    List<Turma> pesquisarTurmaPorTipo(String tipoTurma)  throws TurmaNaoEncontradoException;

    List<Turma> pesquisarTurmaPorCatequista(String catequistaId) throws CatequistaNaoEncontradoException, TurmaNaoEncontradoException;

    List<Turma> pesquisarTurmaPorCatequisando(String catequisandoId) throws CatequisandoNaoEncontradoException, TurmaNaoEncontradoException;

    //Noticia



    //Persistência
}
