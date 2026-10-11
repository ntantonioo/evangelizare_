package model;

public abstract class Usuario {

    private String id;
    private String nome;
    private int idade;
    private String senha;

    public Usuario(String id, String nome, int idade, String senha) {
        this.id = id;
        this.nome = nome;
        this.idade = idade;
        this.senha = senha;

    }

    public String getId() {
        return id;
    }
    public void serId(String id) {
        this.id = id;
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public int getIdade() {
        return idade;
    }
    public void setIdade(int idade) {
        this.idade = idade;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
