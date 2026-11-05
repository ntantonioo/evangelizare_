package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Atividade {

    private final String idAtividade;
    private String titulo;
    private String descricao;
    private final LocalDate dataPublicacao;
    private LocalDate prazo;
    private final Catequista autor;
    private final Turma turma;

    public Atividade(
            String idAtividade,
            String titulo,
            String descricao,
            LocalDate dataPublicacao,
            LocalDate prazo,
            Catequista autor,
            Turma turma
    ) {
        exigirTexto(idAtividade, "O ID da atividade é obrigatório.");
        exigirTexto(titulo, "O título da atividade é obrigatório.");
        exigirTexto(descricao, "A descrição da atividade é obrigatória.");

        if (dataPublicacao == null) {
            throw new IllegalArgumentException(
                    "A data de publicação é obrigatória."
            );
        }

        if (autor == null) {
            throw new IllegalArgumentException(
                    "A atividade precisa ter um catequista como autor."
            );
        }

        if (turma == null) {
            throw new IllegalArgumentException(
                    "A atividade precisa estar vinculada a uma turma."
            );
        }

        exigirPrazoValido(prazo, dataPublicacao);

        this.idAtividade = idAtividade.trim();
        this.titulo = titulo.trim();
        this.descricao = descricao.trim();
        this.dataPublicacao = dataPublicacao;
        this.prazo = prazo;
        this.autor = autor;
        this.turma = turma;
    }

    public void atualizar(String titulo, String descricao, LocalDate prazo, LocalDate hoje) {
        exigirTexto(titulo, "O título da atividade é obrigatório.");
        exigirTexto(descricao, "A descrição da atividade é obrigatória.");
        exigirPrazoValido(prazo, hoje);

        this.titulo = titulo.trim();
        this.descricao = descricao.trim();
        this.prazo = prazo;
    }

    public boolean estaEncerrada(LocalDate hoje) {
        return prazo != null && hoje.isAfter(prazo);
    }

    // Dias até o prazo (0 = vence hoje), ou null se a atividade não tem prazo.
    public Long diasRestantes(LocalDate hoje) {
        return prazo == null ? null : ChronoUnit.DAYS.between(hoje, prazo);
    }

    private static void exigirTexto(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    private static void exigirPrazoValido(LocalDate prazo, LocalDate referencia) {
        if (prazo != null && prazo.isBefore(referencia)) {
            throw new IllegalArgumentException(
                    "O prazo não pode estar no passado."
            );
        }
    }

    public String getIdAtividade() {
        return idAtividade;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDate getDataPublicacao() {
        return dataPublicacao;
    }

    // Nulo quando a atividade não tem prazo.
    public LocalDate getPrazo() {
        return prazo;
    }

    public Catequista getAutor() {
        return autor;
    }

    public Turma getTurma() {
        return turma;
    }
}