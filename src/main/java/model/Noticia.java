package model;

import java.time.LocalDateTime;

public class Noticia {

    private String idNoticia;
    private String titulo;
    private String conteudo;
    private TipoNoticia tipoNoticia;
    private LocalDateTime dataPublicacao;
    private Usuario autor;
    private Turma turma;

    public Noticia(
            String idNoticia,
            String titulo,
            String conteudo,
            TipoNoticia tipoNoticia,
            Usuario autor,
            Turma turma
    ) {
        this.idNoticia = idNoticia;
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.tipoNoticia = tipoNoticia;
        this.autor = autor;
        this.dataPublicacao = LocalDateTime.now();
        this.turma = turma;
    }

    public String getIdNoticia() {
        return idNoticia;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public TipoNoticia getTipoNoticia() {
        return tipoNoticia;
    }

    public void setTipoNoticia(TipoNoticia tipoNoticia) {
        this.tipoNoticia = tipoNoticia;
    }

    public LocalDateTime getDataPublicacao() {
        return dataPublicacao;
    }

    public Usuario getAutor() {
        return autor;
    }

    public Turma getTurma() {
        return turma;
    }

    @Override
    public String toString() {
        return "Noticia{" +
                "idNoticia='" + idNoticia + '\'' +
                ", titulo='" + titulo + '\'' +
                ", tipoNoticia=" + tipoNoticia +
                ", dataPublicacao=" + dataPublicacao +
                ", autor=" + (autor != null
                ? autor.getNome()
                : "Não informado") +
                ", turma=" + (turma != null
                ? turma.getIdTurma()
                : "Todas as turmas") +
                '}';
    }

}
