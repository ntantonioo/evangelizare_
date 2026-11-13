package service;

import java.util.List;

import exception.CatequisandoNaoEncontradoException;
import exception.CatequistaNaoEncontradoException;
import exception.TurmaNaoEncontradaException;
import exception.NoticiaNaoEncontradaException;
import exception.ResponsavelNaoEncontradoException;

import model.Noticia;
import model.TipoNoticia;
import model.Administrador;
import model.Catequisando;
import model.Catequista;
import model.Turma;
import model.Usuario;
import model.Responsavel;
import model.VinculoResponsavel;

public interface SistemaEvangelizare {

    // Usuários e autenticação

    void cadastrarAdministrador(Administrador administrador);

    Usuario pesquisarUsuarioPorId(String id);

    boolean autenticarUsuario(String id, String senha);

    boolean existeAdministrador();

    void cadastrarAdministradorInicial(Administrador administrador);

    // Troca a senha do próprio usuário, que precisa informar a senha atual
    void alterarSenha(String idUsuario, String senhaAtual, String novaSenha);

    /**
     * Define uma senha provisória para um usuário que esqueceu a dele. Só o
     * administrador pode; o usuário será obrigado a trocá-la ao entrar
     */
    void redefinirSenha(String idUsuario, String senhaProvisoria, Usuario administradorLogado);

    // Catequista

    void cadastrarCatequista(Catequista catequista);

    void removerCatequista(String id)
            throws CatequistaNaoEncontradoException;

    Catequista pesquisarCatequistaPorId(String id)
            throws CatequistaNaoEncontradoException;

    List<Catequista> pesquisarCatequistaPorNome(String nome);

    void atualizarCatequista(String id, String telefone, int idade, String anoDeIngresso)
            throws CatequistaNaoEncontradoException;

    void transferirCatequista(
            String idCatequista,
            String idNovaTurma
    ) throws CatequistaNaoEncontradoException,
            TurmaNaoEncontradaException;

    // Catequisando

    void cadastrarCatequisando(Catequisando catequisando)
            throws TurmaNaoEncontradaException;

    void atualizarCatequisando(String id, String telefone, int idade, List<VinculoResponsavel> vinculos)
            throws CatequisandoNaoEncontradoException;

    void removerCatequisando(String id)
            throws CatequisandoNaoEncontradoException;

    Catequisando pesquisarCatequisandoPorId(String id)
            throws CatequisandoNaoEncontradoException;

    List<Catequisando> pesquisarCatequisandoPorNome(String nome);

    List<Catequisando> pesquisarCatequisandosPorTurma(String idTurma);

    void transferirCatequisando(String idCatequisando, String idNovaTurma)
            throws CatequisandoNaoEncontradoException,
            TurmaNaoEncontradaException;

    // Responsável

    Responsavel pesquisarResponsavelPorId(String id)
            throws ResponsavelNaoEncontradoException;

    List<Catequisando> pesquisarCatequisandosPorResponsavel(String idResponsavel)
            throws ResponsavelNaoEncontradoException;

    // Turma

    void cadastrarTurma(Turma turma)
            throws CatequistaNaoEncontradoException;

    void removerTurma(String idTurma)
            throws TurmaNaoEncontradaException;

    void editarTurma(Turma turma)
            throws TurmaNaoEncontradaException,
            CatequistaNaoEncontradoException;

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