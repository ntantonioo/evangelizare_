package service;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import exception.CatequistaNaoEncontradoException;
import exception.CatequisandoNaoEncontradoException;
import exception.TurmaNaoEncontradaException;
import exception.NoticiaNaoEncontradaException;

import model.Administrador;
import model.Usuario;
import model.Catequisando;
import model.Catequista;
import model.Turma;
import model.Noticia;
import model.TipoNoticia;

public class SistemaEvangelizareMap implements  SistemaEvangelizare {

    private Map<String, Usuario> usuarios = new HashMap<>();

    private Map<String, Catequista> catequistas = new HashMap<>();
    private Map<String, Catequisando> catequisandos = new HashMap<>();
    private Map<String, Turma> turmas = new HashMap<>();
    private Map<String, Noticia> noticias = new HashMap<>();

    //CATEQUISTA

    @Override
    public void cadastrarCatequista(Catequista catequista) {
        validarNovoUsuario(catequista);

        catequistas.put(catequista.getId(), catequista);
        usuarios.put(catequista.getId(), catequista);
    }

    @Override
    public void removerCatequista(String id) throws CatequistaNaoEncontradoException {
        if (!catequistas.containsKey(id)) {
            throw new CatequistaNaoEncontradoException("Catequista não encontrado: " + id);
        }
        catequistas.remove(id);
        usuarios.remove(id);
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
    public void cadastrarCatequisando(Catequisando catequisando)
            throws CatequisandoNaoEncontradoException {

        validarNovoUsuario(catequisando);

        if (catequisando.getTurma() == null) {
            throw new IllegalArgumentException(
                    "O catequisando precisa estar associado a uma turma."
            );
        }

        String idTurma = catequisando.getTurma().getIdTurma();

        Turma turma = turmas.get(idTurma);

        if (turma == null) {
            throw new TurmaNaoEncontradaException(
                    "Turma não encontrada: " + idTurma
            );
        }

        catequisando.setTurma(turma);

        catequisandos.put(catequisando.getId(), catequisando);
        usuarios.put(catequisando.getId(), catequisando);
    }

    @Override
    public void removerCatequisando(String id) throws CatequisandoNaoEncontradoException {
        if (!catequisandos.containsKey(id)) {
            throw new CatequisandoNaoEncontradoException("Catequisando nao encontrado: " + id);
        }
        catequisandos.remove(id);
        usuarios.remove(id);
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
            throws CatequisandoNaoEncontradoException {

        if (turma == null) {
            throw new IllegalArgumentException(
                    "A turma não pode ser nula"
            );
        }

        if (turma.getIdTurma() == null
            || turma.getIdTurma().isBlank()) {
        throw new IllegalArgumentException(
                "O ID da turma é obrigatório."
        );
    }

        pesquisarCatequisandoPorId(turma.getCatequista().getId());

        turmas.put(turma.getIdTurma(), turma);
    }


    @Override
    public void removerTurma(String idTurma) throws TurmaNaoEncontradaException {
        Turma turma = turmas.get(idTurma);

        if (turma == null) {
            throw new TurmaNaoEncontradaException(
                    "Turma não encontrada: " + idTurma
            );
        }

        for (Catequisando catequisando : catequisandos.values()) {
            if (catequisando.getTurma() != null
                    && catequisando.getTurma().getIdTurma().equals(idTurma)) {
                catequisando.setTurma(null);
            }
        }

        turmas.remove(idTurma);
    }


    @Override
    public List<Turma> pesquisarTurmaPorId(String idTurma)
            throws TurmaNaoEncontradaException {

        Turma turma = turmas.get(idTurma);

        if (turma == null) {
            throw new TurmaNaoEncontradaException(
                    "Turma nao encontrada: " + idTurma
            );
        }

        return List.of(turma);
    }

    @Override
    public List<Turma> pesquisarTurmaPorTipo(String tipoTurma)
            throws TurmaNaoEncontradaException {

        List<Turma> resultado = new ArrayList<>();

        for (Turma turma : turmas.values()) {
            if (turma.getTipoTurma() != null
                    && turma.getTipoTurma().name().equalsIgnoreCase(tipoTurma)) {
                resultado.add(turma);
            }
        }

        if (resultado.isEmpty()) {
            throw new TurmaNaoEncontradaException(
                    "Nenhuma turma encontrada do tipo: " + tipoTurma
            );
        }

        return resultado;
    }

    @Override
    public List<Turma> pesquisarTurmaPorCatequista(String catequistaId)
        throws CatequistaNaoEncontradoException,
            TurmaNaoEncontradaException {

        Catequista catequista = pesquisarCatequistaPorId(catequistaId);

        List<Turma> resultado = turmas.values().stream()
                .filter(turma -> turma.getCatequista() != null
                        && turma.getCatequista().getId().equals(catequista.getId()))
                .collect(Collectors.toList());

        if (resultado.isEmpty()) {
            throw new TurmaNaoEncontradaException(
                    "Nenhuma turma encontrada para o catequista: " + catequistaId
            );
        }

        return resultado;
    }

    @Override
    public List<Turma> pesquisarTurmaPorCatequisando(String catequisandoId)
        throws CatequisandoNaoEncontradoException,
            TurmaNaoEncontradaException {

        Catequisando catequisando = pesquisarCatequisandoPorId(catequisandoId);

        Turma turma = catequisando.getTurma();

        if (turma == null) {
            throw new TurmaNaoEncontradaException(
                    "O catequisando não está associado a nenhuma turma: "
                            + catequisandoId
            );
        }

        return List.of(turma);
    }

    // USÚARIOS E AUTENTIFICAÇÃO

    @Override
    public void cadastrarAdministrador(Administrador administrador) {
        validarNovoUsuario(administrador);
        usuarios.put(administrador.getId(), administrador);
    }

    @Override
    public Usuario pesquisarUsuarioPorId(String id) {
        return usuarios.get(id);
    }

    @Override
    public boolean autenticarUsuario(String id, String senha) {
        Usuario usuario = pesquisarUsuarioPorId(id);

        return usuario != null
                && usuario.getSenha() != null
                && usuario.getSenha().equals(senha);
    }

    private void validarNovoUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException(
                    "O usuário não pode ser nulo."
            );
        }

        if (usuario.getId() == null || usuario.getId().isBlank()) {
            throw new IllegalArgumentException(
                    "O ID do usuário é obrigatório."
            );
        }

        if (usuarios.containsKey(usuario.getId())) {
            throw new IllegalArgumentException(
                    "Já existe um usuário cadastrado com esse ID."
            );
        }
    }

    private void validarUsuarioLogado(Usuario usuarioLogado) {

        if (usuarioLogado == null
                || usuarioLogado.getId() == null) {
            throw new IllegalArgumentException(
                    "É necessário estar autenticado."
            );
        }

        Usuario usuarioCadastrado =
                usuarios.get(usuarioLogado.getId());

        if (usuarioCadastrado != usuarioLogado) {
            throw new IllegalArgumentException(
                    "O usuário informado não está autenticado no sistema."
            );
        }
    }

    // NOTÍCIAS

    @Override
    public void cadastrarNoticia(Noticia noticia, Usuario usuarioLogado) {

        validarUsuarioLogado(usuarioLogado);

        if (noticia == null) {
            throw new IllegalArgumentException(
                    "A notícia não pode ser nula."
            );
        }

        if (noticia.getIdNoticia() == null
                || noticia.getIdNoticia().isBlank()) {
            throw new IllegalArgumentException(
                    "O ID da notícia é obrigatório."
            );
        }

        if (noticias.containsKey(noticia.getIdNoticia())) {
            throw new IllegalArgumentException(
                    "Já existe uma notícia cadastrada com esse ID."
            );
        }

        if (noticia.getTitulo() == null
                || noticia.getTitulo().isBlank()) {
            throw new IllegalArgumentException(
                    "O título da notícia é obrigatório."
            );
        }

        if (noticia.getConteudo() == null
                || noticia.getConteudo().isBlank()) {
            throw new IllegalArgumentException(
                    "O conteúdo da notícia é obrigatório."
            );
        }

        if (noticia.getTipoNoticia() == null) {
            throw new IllegalArgumentException(
                    "A categoria da notícia é obrigatória."
            );
        }

        if (noticia.getAutor() != usuarioLogado) {
            throw new IllegalArgumentException(
                    "O autor da notícia deve ser o usuário logado."
            );
        }

        Turma turma = noticia.getTurma();

        if (turma != null) {
            Turma turmaCadastrada = turmas.get(turma.getIdTurma());

            if (turmaCadastrada == null || turmaCadastrada != turma) {
                throw new IllegalArgumentException(
                        "A turma informada não está cadastrada."
                );
            }
        }

        if (usuarioLogado instanceof Administrador) {

        } else if (usuarioLogado instanceof Catequista) {

            if (turma == null) {
                throw new IllegalArgumentException(
                        "O catequista deve informar sua turma."
                );
            }

            if (turma.getCatequista() == null
                    || !turma.getCatequista().getId()
                    .equals(usuarioLogado.getId())) {
                throw new IllegalArgumentException(
                        "O catequista só pode publicar notícias para sua turma."
                );
            }

        } else {
            throw new IllegalArgumentException(
                    "Este usuário não tem permissão para publicar notícias."
            );
        }

        noticias.put(noticia.getIdNoticia(), noticia);
    }

    @Override
    public void removerNoticia(
            String idNoticia,
            Usuario usuarioLogado
    ) throws NoticiaNaoEncontradaException {

        validarUsuarioLogado(usuarioLogado);

        if (!(usuarioLogado instanceof Administrador)) {
            throw new IllegalArgumentException(
                    "Somente o administrador pode remover notícias."
            );
        }

        Noticia noticia = noticias.get(idNoticia);

        if (noticia == null) {
            throw new NoticiaNaoEncontradaException(
                    "Notícia não encontrada: " + idNoticia
            );
        }

        noticias.remove(idNoticia);
    }

    @Override
    public Noticia pesquisarNoticiaPorId(String idNoticia)
        throws NoticiaNaoEncontradaException {

        Noticia noticia = noticias.get(idNoticia);

        if (noticia == null) {
            throw new NoticiaNaoEncontradaException(
                    "Noticia não encontrada: " + idNoticia
            );
        }

        return noticia;
    }

    @Override
    public List<Noticia> pesquisarNoticiaPorTipo(TipoNoticia tipoNoticia)
        throws NoticiaNaoEncontradaException {

        if (tipoNoticia == null) {
            throw new IllegalArgumentException(
                    "O tipo da notícia não pode ser nulo."
            );
        }

        List<Noticia> resultado = noticias.values().stream()
                .filter(noticia -> noticia.getTipoNoticia() == tipoNoticia)
                .collect(Collectors.toList());

        if (resultado.isEmpty()) {
            throw new NoticiaNaoEncontradaException(
                    "Nenhuma notícia encontrada para o tipo: " + tipoNoticia
            );
        }

        return resultado;
    }

    @Override
    public List<Noticia> pesquisarNoticiasDisponiveisPara(
            Usuario usuarioLogado
    ) throws NoticiaNaoEncontradaException {

        validarUsuarioLogado(usuarioLogado);

        List<Noticia> resultado = new ArrayList<>(noticias.values());

        if (resultado.isEmpty()) {
            throw new NoticiaNaoEncontradaException(
                    "Nenhuma notícia cadastrada."
            );
        }

        return resultado;
    }

}
