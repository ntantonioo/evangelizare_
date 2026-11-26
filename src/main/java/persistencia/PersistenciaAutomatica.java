package persistencia;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import service.SistemaEvangelizareMap;
import service.atividade.AtividadeServiceMap;

public class PersistenciaAutomatica {

    private final RepositorioSqlite repositorio;
    private final SistemaEvangelizareMap sistema;
    private final AtividadeServiceMap atividades;
    private final Object trava = new Object();

    public PersistenciaAutomatica(
            RepositorioSqlite repositorio,
            SistemaEvangelizareMap sistema,
            AtividadeServiceMap atividades) {
        this.repositorio = repositorio;
        this.sistema = sistema;
        this.atividades = atividades;
    }

    public void carregar() {
        synchronized (trava) {
            Estado estado = repositorio.carregar();

            sistema.restaurar(estado.usuarios, estado.turmas, estado.noticias);
            atividades.restaurar(estado.atividades);
        }
    }

    public void sincronizar() {
        synchronized (trava) {
            // Publicações de turmas ou catequistas que não existem mais saem junto.
            sistema.descartarOrfaos();
            atividades.descartarOrfaos();

            repositorio.sincronizar(new Estado(
                    sistema.listarUsuarios(),
                    sistema.listarTurmas(),
                    sistema.listarNoticias(),
                    atividades.listarAtividades()));
        }
    }

    public <T> T envolver(Class<T> tipoDoServico, T servico) {
        InvocationHandler tratador = (proxy, metodo, argumentos) -> {
            // equals, hashCode e toString não são operações do serviço.
            if (metodo.getDeclaringClass() == Object.class) {
                return metodo.invoke(servico, argumentos);
            }

            synchronized (trava) {
                Object resultado;

                try {
                    resultado = metodo.invoke(servico, argumentos);
                } catch (InvocationTargetException e) {
                    throw e.getCause(); // a exceção original do serviço, sem embrulho
                }

                if (!somenteConsulta(metodo)) {
                    sincronizar();
                }

                return resultado;
            }
        };

        return tipoDoServico.cast(Proxy.newProxyInstance(
                tipoDoServico.getClassLoader(), new Class<?>[]{tipoDoServico}, tratador));
    }

    private static boolean somenteConsulta(Method metodo) {
        String nome = metodo.getName();

        return nome.startsWith("pesquisar")
                || nome.startsWith("existe")
                || nome.startsWith("autenticar")
                || nome.startsWith("resumir")
                || nome.startsWith("listar");
    }
}