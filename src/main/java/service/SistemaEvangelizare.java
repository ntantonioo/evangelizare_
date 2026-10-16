package service;

import java.util.List;

import exception.CatequisandoNaoEncontradoException;
import exception.CatequistaNaoEncontradoException;
import exception.TurmaNaoEncontradaException;
import exception.NoticiaNaoEncontradaException;

import model.Noticia;
import model.TipoNoticia;
import model.Administrador;
import model.Catequisando;
import model.Catequista;
import model.Turma;
import model.Usuario;

public interface SistemaEvangelizare {

    // Usuários e autenticação

    void cadastrarAdministrador(Administrador administrador);

    Usuario pesquisarUsuarioPorId(String id);

    boolean autenticarUsuario(String id, String senha);

    // Catequista

    void cadastrarCatequista(Catequista catequista);

    void removerCatequista(String id)
            throws CatequistaNaoEncontradoException;

    Catequista pesquisarCatequistaPorId(String id)
            throws CatequistaNaoEncontradoException;

    List<Catequista> pesquisarCatequistaPorNome(String nome);

    // Catequisando

    void cadastrarCatequisando(Catequisando catequisando)
            throws TurmaNaoEncontradaException;

    void removerCatequisando(String id)
            throws CatequisandoNaoEncontradoException;

    Catequisando pesquisarCatequisandoPorId(String id)
            throws CatequisandoNaoEncontradoException;

    List<Catequisando> pesquisarCatequisandoPorNome(String nome);

    // Turma

    void cadastrarTurma(Turma turma)
            throws CatequistaNaoEncontradoException;

    void removerTurma(String idTurma)
            throws TurmaNaoEncontradaException;

    List<Turma> pesquisarTurmaPorId(String idTurma)
            throws TurmaNaoEncontradaException;

    List<Turma> pesquisarTurmaPorTipo(String tipoTurma)
            throws TurmaNaoEncontradaException;

    List<Turma> pesquisarTurmaPorCatequista(String catequistaId)
            throws CatequistaNaoEncontradoException,
            TurmaNaoEncontradaException;

    List<Turma> pesquisarTurmaPorCatequisando(String catequisandoId)
            throws CatequisandoNaoEncontradoException,
            TurmaNaoEncontradaException;

    // Noticias

    void cadastrarNoticia(Noticia noticia, Usuario usuarioLogado);

    void removerNoticia(String idNoticia, Usuario usuarioLogado)
            throws NoticiaNaoEncontradaException;

    Noticia pesquisarNoticiaPorId(String idNoticia)
        throws NoticiaNaoEncontradaException;

    List<Noticia> pesquisarNoticiaPorTipo(TipoNoticia tipoNoticia)
        throws NoticiaNaoEncontradaException;

    List<Noticia> pesquisarNoticiasDisponiveisPara(Usuario usuarioLogado)
        throws NoticiaNaoEncontradaException;
}
