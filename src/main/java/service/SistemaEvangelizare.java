package service;

import java.util.List;

import exception.CatequisandoNaoExisteException;
import exception.CatequistaNaoExisteException;
import exception.TurmaNaoExisteException;
import model.Catequisando;
import model.Catequista;
import model.Turma;

public interface SistemaEvangelizare {

    // Catequista
    void cadastrarCatequista(Catequista catequista);

    void removerCatequista(String id) throws CatequistaNaoExisteException;

    Catequista pesquisarCatequistaPorId(String id) throws CatequistaNaoExisteException;

    List<Catequista> pesquisarCatequistaPorNome(String nome);

    // Catequisando
    void cadastrarCatequisando (Catequisando catequisando);

    void removerCatequisando (String id) throws CatequisandoNaoExisteException;

    Catequisando pesquisarCatequisandoPorId(String id) throws CatequisandoNaoExisteException;

    List<Catequisando> pesquisarCatequisandoPorNome(String nome);

    //Turma
    void cadastrarTurma(Turma turma)
        throws TurmaNaoExisteException, CatequistaNaoExisteException, CatequisandoNaoExisteException;

    void removerTurma(String idTurma) throws TurmaNaoExisteException;

    List<Turma> pesquisarTurmaPorId(String idTurma) throws TurmaNaoExisteException;

    List<Turma> pesquisarTurmaPorTipo(String tipoTurma)  throws TurmaNaoExisteException;

    List<Turma> pesquisarTurmaPorCatequista(String catequista) throws CatequistaNaoExisteException, TurmaNaoExisteException;

    List<Turma> pesquisarTurmaPorCatequisando(String catequisando) throws CatequisandoNaoExisteException, TurmaNaoExisteException;

    //Noticia



    //Persistência
}
