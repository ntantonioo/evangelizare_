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

import model.*;

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
    public void removerCatequista(String id)
            throws CatequistaNaoEncontradoException {

        if (!catequistas.containsKey(id)) {
            throw new CatequistaNaoEncontradoException(
                    "Catequista não encontrado: " + id
            );
        }

        boolean possuiTurmas = turmas.values().stream()
                .anyMatch(turma ->
                        turma.getCatequistas().stream()
                                .anyMatch(catequista ->
                                        catequista.getId().equals(id)
                                )
                );

        if (possuiTurmas) {
            throw new IllegalArgumentException(
                    "Não é possível remover este catequista, "
                            + "pois ele ainda possui turmas associadas."
            );
        }

        catequistas.remove(id);
        usuarios.remove(id);
    }

    @Override
    public void atualizarCatequista(String id, String telefone, int idade, String anoDeIngresso)
        throws CatequistaNaoEncontradoException {

        Catequista catequista = pesquisarCatequistaPorId(id);

        catequista.atualizarTelefone(telefone);
        catequista.atualizarIdade(idade);
        catequista.atualizarAnoDeIngresso(anoDeIngresso);
    }

    @Override
    public Catequista pesquisarCatequistaPorId(String id) throws CatequistaNaoEncontradoException {
        Catequista catequista = catequistas.get(id);
        if (catequista == null) {
            throw new CatequistaNaoEncontradoException("Catequista não encontrado: " + id);
        }
        return catequista;
    }

    @Override
    public List<Catequista> pesquisarCatequistaPorNome(String nome) {
        return catequistas.values().stream()
                .filter(c -> c.getNome().toLowerCase().contains(nome.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public void transferirCatequista(
            String idCatequista,
            String idNovaTurma
    ) throws CatequistaNaoEncontradoException,
            TurmaNaoEncontradaException {

        if (idCatequista == null || idCatequista.isBlank()) {
            throw new IllegalArgumentException(
                    "O ID do catequista é obrigatório"
            );
        }

        if (idNovaTurma == null || idNovaTurma.isBlank()) {
            throw new IllegalArgumentException(
                    "O ID da nova turma é obrigatório."
            );
        }

        Catequista catequista =
                pesquisarCatequistaPorId(idCatequista);

        Turma novaTurma = turmas.get(idNovaTurma);

        if (novaTurma == null) {
            throw new TurmaNaoEncontradaException(
                    "Turma não encontrada: " + idNovaTurma
            );
        }

        Turma turmaAtual = turmas.values().stream()
                .filter(turma ->
                        turma.getCatequistas().stream()
                                .anyMatch(c ->
                                        c.getId().equals(idCatequista)
                                )
                )
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "O catequista não está associado a nenhuma turma."
                ));

        if (turmaAtual.getIdTurma().equals(idNovaTurma)) {
            throw new IllegalArgumentException(
                    "O catequista já está associado a esta turma."
            );
        }

        if (turmaAtual.getCatequistas().size() <= 1) {
            throw new IllegalArgumentException(
                    "A transferência não pode deixar a turma de origem sem catequistas."
            );
        }

        if (novaTurma.getCatequistas().size() >= 2) {
            throw new IllegalArgumentException(
                    "A turma de destino já possui o número máximo de catequistas."
            );
        }

        List<Catequista> catequistasOrigem =
                new ArrayList<>(turmaAtual.getCatequistas());

        catequistasOrigem.removeIf(c ->
                c.getId().equals(idCatequista)
        );

        turmaAtual.atualizarCatequistas(catequistasOrigem);

        novaTurma.adicionarCatequista(catequista);
    }

    //CATEQUISANDO

    @Override
    public void cadastrarCatequisando(Catequisando catequisando)
            throws CatequisandoNaoEncontradoException {

        if (catequisando == null) {
            throw new IllegalArgumentException(
                    "O catequisando não pode ser nulo"
            );
        }

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

        if (turma.getCatequistas().isEmpty()) {
            throw new IllegalArgumentException(
                    "Não é possível cadastrar um catequisando em uma turma sem catequistas."
            );
        }

        TipoTurma tipo = turma.getTipoTurma();

        if (catequisando.getResponsaveis() == null) {
            throw new IllegalArgumentException(
                    "A lista de responsáveis não pode ser nula."
            );
        }

        boolean exigeResponsavel =
                tipo == TipoTurma.CATEQUESE
                || tipo == TipoTurma.CRISMA_JOVEM;

        if (exigeResponsavel
                && catequisando.getResponsaveis().isEmpty()) {

            throw new IllegalArgumentException(
                    "Esta turma exige pelo menos um responsável cadastrado."
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
    public void atualizarCatequisando(String id, String telefone, int idade, List<Responsavel> responsaveis)
        throws CatequisandoNaoEncontradoException {

        // verifica se o catequisando existe
        Catequisando catequisando = pesquisarCatequisandoPorId(id);

        // verifica se a lista de responsaveis foi informada
        if (responsaveis == null) {
            throw new IllegalArgumentException(
                    "A lista de responsáveis não pode ser nula."
            );
        }

        if (catequisando.getTurma() == null) {
            throw new IllegalArgumentException(
                    "O catequisando precisa estar vinculado a uma turma."
            );
        }

        if (turmas.get(catequisando.getTurma().getIdTurma()) == null) {
            throw new TurmaNaoEncontradaException(
                    "A turma vinculada ao catequisando não está cadastrada no sistema."
            );
        }

        // verifica as regras da turma
        TipoTurma tipo = catequisando.getTurma().getTipoTurma();

        boolean exigeResponsavel =
                tipo == TipoTurma.CATEQUESE
                || tipo == TipoTurma.CRISMA_JOVEM;

        if (exigeResponsavel && responsaveis.isEmpty()) {
            throw new IllegalArgumentException(
                    "Esta turma exige pelo menos um responsável cadastrado."
            );
        }

        // Atualiza os dados após validar as regras
        catequisando.atualizarTelefone(telefone);
        catequisando.atualizarIdade(idade);
        catequisando.atualizarResponsaveis(responsaveis);
    }

    @Override
    public List<Catequisando> pesquisarCatequisandoPorNome(String nome) {
        return catequisandos.values().stream()
                .filter(c -> c.getNome().toLowerCase().contains(nome.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Catequisando> pesquisarCatequisandosPorTurma(
            String idTurma) {

        return catequisandos.values().stream()
                .filter(c -> c.getTurma() != null)
                .filter(c -> c.getTurma().getIdTurma().equals(idTurma))
                .collect(Collectors.toList());
    }

    @Override
    public void transferirCatequisando(
            String idCatequisando,
            String idNovaTurma
    ) throws CatequisandoNaoEncontradoException,
            TurmaNaoEncontradaException {

        // valida os IDs informados
        if (idCatequisando == null || idCatequisando.isBlank()) {
            throw new IllegalArgumentException(
                    "O ID do catequisando é obrigatório."
            );
        }

        if (idNovaTurma == null || idNovaTurma.isBlank()) {
            throw new IllegalArgumentException(
                    "O ID da nova turma é obrigatório."
            );
        }

        // localiza o catequisando
        Catequisando catequisando = pesquisarCatequisandoPorId(idCatequisando);

        // localiza a turma de destino
        Turma novaTurma = turmas.get(idNovaTurma);

        if (novaTurma == null) {
            throw new TurmaNaoEncontradaException(
                    "Turma não encontrada: " + idNovaTurma
            );
        }

        // verifica se a turma possui catequistas
        if (novaTurma.getCatequistas().isEmpty()) {
            throw new IllegalArgumentException(
                    "A turma de destino precisa ter pelo menos um catequista."
            );
        }

        // impede transferencia para a mesma turma
        if (catequisando.getTurma().getIdTurma().equals(idNovaTurma)) {
            throw new IllegalArgumentException(
                    "O catequisando já está associado a esta turma."
            );
        }

        // verifica se a nova turma exige responsaveis
        TipoTurma tipo = novaTurma.getTipoTurma();

        boolean exigeResponsavel =
                tipo == TipoTurma.CATEQUESE
                    || tipo == TipoTurma.CRISMA_JOVEM;

        if (exigeResponsavel
                && (catequisando.getResponsaveis() == null
                || catequisando.getResponsaveis().isEmpty())) {

            throw new IllegalArgumentException(
                    "A turma de destino exige pelo menos um responsável cadastrado."
            );
        }

        // realiza a transferencia
        catequisando.setTurma(novaTurma);
    }

    //TURMA

    @Override
    public void cadastrarTurma(Turma turma)
            throws CatequistaNaoEncontradoException {

        if (turma == null) {
            throw new IllegalArgumentException(
                    "A turma não pode ser nula."
            );
        }

        if (turma.getIdTurma() == null
                || turma.getIdTurma().isBlank()) {
            throw new IllegalArgumentException(
                    "O ID da turma é obrigatório."
            );
        }

        if (turma.getCatequistas().isEmpty()) {
            throw new IllegalArgumentException(
                    "A turma precisa ter pelo menos um catequista."
            );
        }

        if (turma.getCatequistas().size() > 2) {
            throw new IllegalArgumentException(
                    "A turma pode ter no máximo dois catequistas."
            );
        }

        if (turmas.containsKey(turma.getIdTurma())) {
            throw new IllegalArgumentException(
                    "Já existe uma turma com esse ID."
            );
        }

       for (Catequista catequista : turma.getCatequistas()) {

           // verifica se o catequista esta cadastrado
           pesquisarCatequistaPorId(catequista.getId());

           // verifica se ja esta associado a outra turma
           boolean possuiOutraTurma = turmas.values().stream()
                   .anyMatch(turmaExistente ->
                           turmaExistente.getCatequistas().stream()
                                   .anyMatch(catequistaExistente ->
                                           catequistaExistente.getId()
                                                   .equals(catequista.getId())
                                   )
                   );

           if (possuiOutraTurma) {
               throw new IllegalArgumentException(
                       "O catequista " + catequista.getNome()
                       + " já está associado a outra turma."
               );
           }
       }

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

        boolean possuiCatequisandos = catequisandos.values().stream()
                .anyMatch(catequisando ->
                        catequisando.getTurma() !=null
                        && catequisando.getTurma()
                                .getIdTurma().equals(idTurma)
                );

        if (possuiCatequisandos) {
            throw new IllegalArgumentException(
                    "Não é possível remover esta turma, pois "
                    + "existem catequisandos vinculados a ela. "
                    + "Transfira-os para outra turma antes de remôve-la."
            );
        }

        turmas.remove(idTurma);
    }

    @Override
    public void editarTurma(Turma turma)
            throws TurmaNaoEncontradaException,
            CatequistaNaoEncontradoException {

        if (turma == null) {
            throw new IllegalArgumentException(
                    "A turma não pode ser nula."
            );
        }

        String idTurma = turma.getIdTurma();

        if (idTurma == null || idTurma.isBlank()) {
            throw new IllegalArgumentException(
                    "O ID da turma é obrigatório."
            );
        }

        Turma turmaExistente = turmas.get(idTurma);

        if (turmaExistente == null) {
            throw new TurmaNaoEncontradaException(
                    "Turma não encontrada: " + idTurma
            );
        }

        if (turma.getTipoTurma() == null) {
            throw new IllegalArgumentException(
                    "O tipo da turma é obrigatório."
            );
        }

        List<Catequista> novosCatequistas = turma.getCatequistas();

        if (novosCatequistas.isEmpty()) {
            throw new IllegalArgumentException(
                    "A turma precisa ter pelo menos um catequista."
            );
        }

        if (novosCatequistas.size() > 2) {
            throw new IllegalArgumentException(
                    "A turma pode ter no máximo dois catequistas."
            );
        }

        for (Catequista catequista : novosCatequistas) {
            if (catequista == null || catequista.getId() == null) {
                throw new IllegalArgumentException(
                        "O catequista e seu ID são obrigatórios."
                );
            }

            pesquisarCatequistaPorId(catequista.getId());
        }

        // valida a lista antes de modificar a turma existente.
        List<Catequista> listaValidada = new ArrayList<>();

        for (Catequista catequista : novosCatequistas) {

            boolean duplicado = listaValidada.stream()
                    .anyMatch(existente ->
                            existente.getId().equals(catequista.getId())
                    );

            if (duplicado) {
                throw new IllegalArgumentException(
                        "Este catequista já está associado à turma."
                );
            }

            // verifica se o catequista já pertence a outra turma.
            boolean associadoAOutraTurma = turmas.values().stream()
                    .filter(outraTurma ->
                            !outraTurma.getIdTurma().equals(idTurma)
                    )
                    .anyMatch(outraTurma ->
                            outraTurma.getCatequistas().stream()
                                    .anyMatch(catequistaExistente ->
                                            catequistaExistente.getId()
                                                    .equals(catequista.getId())
                                    )
                    );

            if (associadoAOutraTurma) {
                throw new IllegalArgumentException(
                        "O catequista " + catequista.getNome()
                                + " já está associado a outra turma."
                );
            }

            listaValidada.add(catequista);
        }

        // verifica se a mudança de tipo respeita as regras dos responsáveis.
        if (turma.getTipoTurma() == TipoTurma.CATEQUESE
                || turma.getTipoTurma() == TipoTurma.CRISMA_JOVEM) {

            for (Catequisando catequisando : catequisandos.values()) {

                if (catequisando.getTurma() != null
                        && catequisando.getTurma().getIdTurma().equals(idTurma)
                        && (catequisando.getResponsaveis() == null
                        || catequisando.getResponsaveis().isEmpty())) {

                    throw new IllegalArgumentException(
                            "Não é possível alterar o tipo da turma. "
                                    + "O catequisando "
                                    + catequisando.getNome()
                                    + " não possui responsável cadastrado."
                    );
                }
            }
        }

        turmaExistente.atualizarCatequistas(listaValidada);
        turmaExistente.setTipoTurma(turma.getTipoTurma());
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
    public List<Turma> pesquisarTurmaPorCatequista(String idCatequista)
            throws CatequistaNaoEncontradoException {

        pesquisarCatequistaPorId(idCatequista);

        return turmas.values().stream()
                .filter(turma ->
                        turma.getCatequistas().stream()
                                .anyMatch(catequista ->
                                        catequista.getId().equals(idCatequista)
                                )
                )
                .toList();
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

            boolean catequistaResponsavel = turma.getCatequistas().stream()
                    .anyMatch(catequista ->
                            catequista.getId().equals(usuarioLogado.getId())
                    );

            if (!catequistaResponsavel) {
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

        List<Noticia> resultado = noticias.values().stream()
                .filter(noticia -> {

                    Turma turmaNoticia = noticia.getTurma();

                    // adm pode consultar todas as noticias
                    if (usuarioLogado instanceof Administrador) {
                        return true;
                    }

                    if (turmaNoticia == null) {
                        return true;
                    }

                    // catequista consulta noticias das suas turmas
                    if (usuarioLogado instanceof Catequista) {
                        Catequista catequista = (Catequista) usuarioLogado;

                        return turmaNoticia.getCatequistas().stream()
                                .anyMatch(responsavel ->
                                        responsavel.getId().equals(catequista.getId())
                                );
                    }

                    // catequisando consulta noticias da propria turma
                    if (usuarioLogado instanceof Catequisando) {
                        Catequisando catequisando =
                                (Catequisando) usuarioLogado;

                        Turma turmaCatequisando = catequisando.getTurma();

                        return turmaCatequisando != null
                                && turmaCatequisando.getIdTurma()
                                .equals(turmaNoticia.getIdTurma());
                    }

                    // outros perfils que não têm acesso às noticias
                    return false;
                })
                .toList();

        if (resultado.isEmpty()) {
            throw new NoticiaNaoEncontradaException(
                    "Nenhuma notícia disponível para este usuário."
            );
        }

        return resultado;
    }
}
