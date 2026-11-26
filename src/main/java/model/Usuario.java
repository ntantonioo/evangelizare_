package model;

import java.time.LocalDateTime;

public abstract class Usuario {

    private String id;
    private String nome;
    private String telefone;
    private int idade;
    private String senha;
    private boolean senhaProvisoria;
    private LocalDateTime provisoriaValidaAte;
    private String login;

    public Usuario(String id, String nome, String telefone,
                   int idade, String senha) {
        this.id = id;
        this.nome = nome;
        this.telefone = telefone;
        this.idade = idade;
        this.senha = senha;
        this.login = id;
    }

    public boolean ehAdministrador() {
        return false;
    }

    public LocalDateTime getProvisoriaValidaAte() {
        return provisoriaValidaAte;
    }

    public void setProvisoriaValidaAte(LocalDateTime provisoriaValidaAte) {
        this.provisoriaValidaAte = provisoriaValidaAte;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login == null ? null : login.trim();
    }

    public boolean isSenhaProvisoria() {
        return senhaProvisoria;
    }

    public void setSenhaProvisoria(boolean senhaProvisoria) {
        this.senhaProvisoria = senhaProvisoria;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        if (telefone == null || telefone.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "O telefone não pode ficar vazio."
            );
        }

        this.telefone = telefone.trim();
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