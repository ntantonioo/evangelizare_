
package model;

import java.time.LocalDate;

public class Noticia {

    private String idNoticia;
    private String titulo;
    private String conteudo;
    private String autor;
    private LocalDate dataPublicacao;

    public Noticia(String idNoticia, String titulo, String conteudo,
                   String autor, LocalDate dataPublicacao) {

        this.idNoticia = idNoticia;
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.autor = autor;
        this.dataPublicacao = dataPublicacao;
    }

    public String getIdNoticia() {
        return idNoticia;
    }

    public void setIdNoticia(String idNoticia) {
        this.idNoticia = idNoticia;
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

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public LocalDate getDataPublicacao() {
        return dataPublicacao;
    }

    public void setDataPublicacao(LocalDate dataPublicacao) {
        this.dataPublicacao = dataPublicacao;
    }

    @Override
    public String toString() {
        return "Noticia{" +
                "idNoticia='" + idNoticia + '\'' +
                ", titulo='" + titulo + '\'' +
                ", conteudo='" + conteudo + '\'' +
                ", autor='" + autor + '\'' +
                ", dataPublicacao=" + dataPublicacao +
                '}';
    }
}