package service.responsavel;

import java.util.List;

import model.Atividade;
import model.Catequisando;
import model.Noticia;
import model.Responsavel;

/**
 * O que um responsável acompanha: o que cada um dos seus filhos vê,
 * juntando tudo em uma lista só e sem repetir.
 */
public interface ResponsavelService {

    List<Catequisando> pesquisarFilhos(Responsavel responsavel);

    List<Noticia> pesquisarNoticiasDosFilhos(Responsavel responsavel);

    List<Atividade> pesquisarAtividadesDosFilhos(Responsavel responsavel);
}