package model;

public class Administrador extends Usuario {

    public Administrador(String id, String nome, String telefone, int idade, String senha) {
        super(id, nome, telefone, idade, senha);
    }

    @Override
    public String toString() {
        return "Administrador{" +
                "id='" + getId() + '\'' +
                ", telefone='" + getTelefone() + '\'' +
                ", nome='" + getNome() + '\'' +
                ", idade=" + getIdade() +
                '}';
    }
}
