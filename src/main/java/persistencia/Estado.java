package persistencia;

import java.util.List;

import model.Atividade;
import model.Noticia;
import model.Turma;
import model.Usuario;

// Tudo o que o sistema guarda, reunido em listas, para ir ao banco ou vir dele
public final class Estado {

    public final List<Usuario> usuarios;
    public final List<Turma> turmas;
    public final List<Noticia> noticias;
    public final List<Atividade> atividades;

    public Estado(
            List<Usuario> usuarios,
            List<Turma> turmas,
            List<Noticia> noticias,
            List<Atividade> atividades) {
        this.usuarios = usuarios;
        this.turmas = turmas;
        this.noticias = noticias;
        this.atividades = atividades;
    }
}