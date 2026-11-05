package service.atividade;

import java.time.LocalDate;
import java.util.List;

import exception.AtividadeNaoEncontradaException;
import exception.TurmaNaoEncontradaException;
import model.Atividade;
import model.Usuario;

public interface AtividadeService {

    // CADASTRO DE ATIVIDADE
    // Publica uma atividade (apenas um catequista, e apenas para a turma que é responsavel)

    Atividade cadastrarAtividade(
            String idAtividade,
            String titulo,
            String descricao,
            LocalDate prazo,
            String idTurma,
            Usuario usuarioLogado
    ) throws TurmaNaoEncontradaException;

    // ATUALIZAR ATIVIDADE
    // So o catequista que publicou pode alterar

    void atualizarAtividade(
            String idAtividade,
            String titulo,
            String descricao,
            LocalDate prazo,
            Usuario usuarioLogado
    ) throws AtividadeNaoEncontradaException;

    // REMOVER ATIVIDADE
    // apenas catequista ou administrador

    void removerAtividade(String idAtividade, Usuario usuarioLogado)
            throws AtividadeNaoEncontradaException;

    Atividade pesquisarAtividadePorId(String idAtividade)
            throws AtividadeNaoEncontradaException;

    List<Atividade> pesquisarAtividadesDisponiveisPara(Usuario usuario);
}
