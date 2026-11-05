package service.atividade;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import exception.AtividadeNaoEncontradaException;
import exception.TurmaNaoEncontradaException;
import model.*;
import service.SistemaEvangelizare;

public class AtividadeServiceMap implements AtividadeService {

    private final SistemaEvangelizare sistema;
    private final Clock relogio;
    private final Map<String, Atividade> atividades = new HashMap<>();

    public AtividadeServiceMap(SistemaEvangelizare sistema) {
        this(sistema, Clock.systemDefaultZone());
    }

    public AtividadeServiceMap(SistemaEvangelizare sistema, Clock relogio) {
        this.sistema = sistema;
        this.relogio = relogio;
    }

    // CADASTRAR ATIVIDADE
    @Override
    public Atividade cadastrarAtividade(
            String idAtividade,
            String titulo,
            String descricao,
            LocalDate prazo,
            String idTurma,
            Usuario usuarioLogado) {

        Usuario usuario = exigirUsuarioCadastrado(usuarioLogado);

        if(!(usuario instanceof Catequista autor)) {
            throw new IllegalArgumentException(
                    "Somente catequista podem publicar atividades."
            );
        }

        if (idTurma == null || idTurma.isBlank()) {
            throw new IllegalArgumentException(
                    "Informe a turma da atividade."
            );
        }

        Turma turma = buscarTurma(idTurma.trim());

        if (!turmaPertenceAo(turma, autor)) {
            throw new IllegalArgumentException(
                    " O catequista só pode publicar atividades para suas próprias turmas."
            );
        }

        Atividade atividade = new Atividade(
                idAtividade, titulo, descricao, hoje(), prazo, autor, turma
        );

        if (atividades.containsKey(atividade.getIdAtividade())) {
            throw new IllegalArgumentException(
                    "Já existe uma atividade com esse ID."
            );
        }

        atividades.put(atividade.getIdAtividade(), atividade);

        return atividade;
    }

    //ATUALIZAR/EDITAR ATIVIDADE
    @Override
    public void atualizarAtividade(
            String idAtividade,
            String titulo,
            String descricao,
            LocalDate prazo,
            Usuario usuarioLogado) {

        Usuario usuario = exigirUsuarioCadastrado(usuarioLogado);
        Atividade atividade = pesquisarAtividadePorId(idAtividade);

        if (!(usuario instanceof Catequista)
                || !atividade.getAutor().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException(
                    "Somente o catequista que publicou a atividade pode alterá-la."
            );
        }

        atividade.atualizar(titulo, descricao, prazo, hoje());
    }

    //REMOVER ATIVIDADE
    @Override
    public void removerAtividade(String idAtividade, Usuario usuarioLogado) {
        Usuario usuario = exigirUsuarioCadastrado(usuarioLogado);
        Atividade atividade = pesquisarAtividadePorId(idAtividade);

        boolean administrador = usuario instanceof Administrador;
        boolean autor = usuario instanceof Catequista
                && atividade.getAutor().getId().equals(usuario.getId());

        if (!administrador && !autor) {
            throw new IllegalArgumentException(
                    "Somente o administrador ou o catequista que publicou "
                    + "a atividade pode removê-la."
            );
        }

        atividades.remove(atividade.getIdAtividade());
    }

    //PESQUISAR ATIVIDADE POR ID
    @Override
    public Atividade pesquisarAtividadePorId(String idAtividade) {
        Atividade atividade = idAtividade == null
                ? null
                : atividades.get(idAtividade.trim());

        if (atividade == null) {
            throw new AtividadeNaoEncontradaException(
                    "Atividade não encontrada: " + idAtividade
            );
        }

        return atividade;
    }

    //PESQUISAR ATIVIDADES DISPONIVEIS PARA...
    @Override
    public List<Atividade> pesquisarAtividadesDisponiveisPara(Usuario usuario) {
        Usuario logado = exigirUsuarioCadastrado(usuario);

        // Prazo mais próximo primeiro; sem prazo por último; desempate pelo ID.
        Comparator<Atividade> ordem = Comparator
                .comparing(Atividade::getPrazo,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Atividade::getIdAtividade);

        if (logado instanceof Administrador) {
            return atividades.values().stream()
                    .sorted(ordem)
                    .collect(Collectors.toList());
        }

        if (logado instanceof Catequista catequista) {
            return atividades.values().stream()
                    .filter(a -> turmaAindaExiste(a.getTurma())
                            && turmaPertenceAo(a.getTurma(), catequista))
                    .sorted(ordem)
                    .collect(Collectors.toList());
        }

        if (logado instanceof Catequisando catequisando) {
            String idTurma = catequisando.getTurma().getIdTurma();

            return atividades.values().stream()
                    .filter(a -> turmaAindaExiste(a.getTurma())
                            && a.getTurma().getIdTurma().equals(idTurma))
                    .sorted(ordem)
                    .collect(Collectors.toList());
        }

        throw new IllegalArgumentException(
                "Este perfil não tem acesso às atividades."
        );
    }

    // AUXILIARES
    private LocalDate hoje() {
        return LocalDate.now(relogio);
    }

    // O usuário precisa ser exatamente o objeto cadastrado no sistema.
    private Usuario exigirUsuarioCadastrado(Usuario usuario) {
        if (usuario == null
                || sistema.pesquisarUsuarioPorId(usuario.getId()) != usuario) {
            throw new IllegalArgumentException(
                    "Usuário não autenticado no sistema."
            );
        }

        return usuario;
    }

    //BUSCAR TURMA
    private Turma buscarTurma(String idTurma) {
        return sistema.pesquisarTurmaPorId(idTurma).stream()
                .filter(t -> t.getIdTurma().equals(idTurma))
                .findFirst()
                .orElseThrow(() -> new TurmaNaoEncontradaException(
                        "Turma não encontrada: " + idTurma
                ));
    }

    private boolean turmaPertenceAo(Turma turma, Catequista catequista) {
        return turma.getCatequistas().stream()
                .anyMatch(c -> c.getId().equals(catequista.getId()));
    }

    // Uma turma removida do sistema esconde suas atividades de quem não é
    // administrador (o administrador ainda pode ver e remover).
    private boolean turmaAindaExiste(Turma turma) {
        try {
            return sistema.pesquisarTurmaPorId(turma.getIdTurma()).stream()
                    .anyMatch(t -> t.getIdTurma().equals(turma.getIdTurma()));
        } catch (TurmaNaoEncontradaException e) {
            return false;
        }
    }

}
