package ui.telas;

import java.time.LocalDate;
import java.util.List;

import model.*;
import service.responsavel.ResponsavelService;
import ui.Console;

public class TelaResponsavel {

    private final ResponsavelService servico;

    public TelaResponsavel(ResponsavelService servico) {
        this.servico = servico;
    }

    // MEUS FILHOS

    public void consultarFilhos(Responsavel responsavel) {
        System.out.println("\n========== MEUS FILHOS ==========");

        List<Catequisando> filhos = servico.pesquisarFilhos(responsavel);

        if (filhos.isEmpty()) {
            System.out.println("Nenhum catequisando vinculado a você.");
            return;
        }

        for (Catequisando filho : filhos) {
            Turma turma = filho.getTurma();

            System.out.println("------------------------------");
            System.out.println("Nome: " + filho.getNome() + " (ID: " + filho.getId() + ")");
            System.out.println("Parentesco: " + filho.getParentescoDe(responsavel.getId()));
            System.out.println(
                    "Turma: " + turma.getIdTurma() + " | " + turma.getTipoTurma()
            );
            System.out.println("Catequistas responsáveis:");

            for (Catequista catequista : turma.getCatequistas()) {
                System.out.println(
                        "- " + catequista.getNome()
                                + " (" + catequista.getTelefone() + ")"
                );
            }
        }
    }

    // NOTÍCIAS

    public void consultarNoticias(Responsavel responsavel) {
        System.out.println("\n========== NOTÍCIAS ==========");

        List<Noticia> noticias = servico.pesquisarNoticiasDosFilhos(responsavel);

        if (noticias.isEmpty()) {
            System.out.println("Nenhuma notícia disponível.");
            return;
        }

        for (Noticia noticia : noticias) {
            System.out.println("\nTítulo: " + noticia.getTitulo());
            System.out.println("Tipo: " + noticia.getTipoNoticia());
            System.out.println("Conteúdo: " + noticia.getConteudo());
            System.out.println("Data: " + noticia.getDataPublicacao());
            System.out.println(
                    "Para: " + (noticia.getTurma() == null
                            ? "toda a comunidade"
                            : "turma " + noticia.getTurma().getIdTurma())
            );
            System.out.println("Autor: " + noticia.getAutor().getNome());
            System.out.println("--------------------------------");
        }
    }

    // ATIVIDADES

    public void consultarAtividades(Responsavel responsavel) {
        System.out.println("\n========== ATIVIDADES ==========");

        List<Atividade> atividades = servico.pesquisarAtividadesDosFilhos(responsavel);

        if (atividades.isEmpty()) {
            System.out.println("Nenhuma atividade disponível.");
            return;
        }

        LocalDate hoje = LocalDate.now();

        for (Atividade atividade : atividades) {
            System.out.println("------------------------------");
            System.out.println(
                    "[" + atividade.getIdAtividade() + "] " + atividade.getTitulo()
            );
            System.out.println(
                    "Turma: " + atividade.getTurma().getIdTurma()
                            + " | Publicada em "
                            + Console.formatarData(atividade.getDataPublicacao())
                            + " por " + atividade.getAutor().getNome()
            );
            System.out.println("Prazo: " + descreverPrazo(atividade, hoje));
            System.out.println("Descrição: " + atividade.getDescricao());
        }
    }

    private String descreverPrazo(Atividade atividade, LocalDate hoje) {
        if (atividade.getPrazo() == null) {
            return "sem prazo";
        }

        String data = Console.formatarData(atividade.getPrazo());

        if (atividade.estaEncerrada(hoje)) {
            return data + " (encerrada)";
        }

        long dias = atividade.diasRestantes(hoje);

        if (dias == 0) {
            return data + " (vence hoje)";
        }

        return data + " (" + (dias == 1 ? "falta 1 dia" : "faltam " + dias + " dias") + ")";
    }
}