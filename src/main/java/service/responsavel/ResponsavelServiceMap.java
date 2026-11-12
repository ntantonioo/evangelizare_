package service.responsavel;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import exception.NoticiaNaoEncontradaException;
import model.Atividade;
import model.Catequisando;
import model.Noticia;
import model.Responsavel;
import service.SistemaEvangelizare;
import service.atividade.AtividadeService;

public class ResponsavelServiceMap implements ResponsavelService {

    private final SistemaEvangelizare sistema;
    private final AtividadeService atividades;

    public ResponsavelServiceMap(SistemaEvangelizare sistema, AtividadeService atividades) {
        this.sistema = sistema;
        this.atividades = atividades;
    }

    @Override
    public List<Catequisando> pesquisarFilhos(Responsavel responsavel) {
        // O responsável precisa ser exatamente o objeto cadastrado no sistema.
        if (responsavel == null
                || sistema.pesquisarUsuarioPorId(responsavel.getId()) != responsavel) {
            throw new IllegalArgumentException(
                    "Usuário não autenticado no sistema."
            );
        }

        return sistema.pesquisarCatequisandosPorResponsavel(responsavel.getId());
    }

    @Override
    public List<Noticia> pesquisarNoticiasDosFilhos(Responsavel responsavel) {
        Map<String, Noticia> unicas = new LinkedHashMap<>();

        for (Catequisando filho : pesquisarFilhos(responsavel)) {
            try {
                for (Noticia noticia : sistema.pesquisarNoticiasDisponiveisPara(filho)) {
                    unicas.putIfAbsent(noticia.getIdNoticia(), noticia);
                }
            } catch (NoticiaNaoEncontradaException e) {
                // Este filho ainda não tem notícias disponíveis.
            }
        }

        return unicas.values().stream()
                .sorted(Comparator.comparing(Noticia::getDataPublicacao).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Atividade> pesquisarAtividadesDosFilhos(Responsavel responsavel) {
        Map<String, Atividade> unicas = new LinkedHashMap<>();

        for (Catequisando filho : pesquisarFilhos(responsavel)) {
            for (Atividade atividade : atividades.pesquisarAtividadesDisponiveisPara(filho)) {
                unicas.putIfAbsent(atividade.getIdAtividade(), atividade);
            }
        }

        // Prazo mais próximo primeiro; sem prazo por último; desempate pelo ID.
        return unicas.values().stream()
                .sorted(Comparator
                        .comparing(Atividade::getPrazo,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Atividade::getIdAtividade))
                .collect(Collectors.toList());
    }
}