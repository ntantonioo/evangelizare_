package model;

public class Administrador extends Usuario {

    public Administrador(String id, String nome, int idade, String senha) {
        super(id, nome, idade, senha);
    }

    @Override
    public String toString() {
        return "Administrador{" +
                "id='" + getId() + '\'' +
                ", nome='" + getNome() + '\'' +
                ", idade=" + getIdade() +
                '}';
    }
}
