
package model;

public abstract class Usuario {

    private String id;
    private String nome;
    private String telefone;
    private int idade;
    private String senha;

    public Usuario(String id, String nome, String telefone,
                   int idade, String senha) {
        this.id = id;
        this.nome = nome;
        this.telefone = telefone;
        this.idade = idade;
        this.senha = senha;
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
