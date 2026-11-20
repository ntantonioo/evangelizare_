package persistencia;

import java.util.List;

/**
 * As tabelas do banco e suas colunas. O tipo de cada coluna é uma letra:
 * S = texto, I = número inteiro. A chave é o conjunto de colunas que
 * identifica cada linha (a "chave primária" da tabela).
 */
enum Tabela {

    USUARIO("usuario",
            "id,tipo,nome,telefone,idade,senha_hash,senha_provisoria,ano_ingresso,id_turma,login,provisoria_valida_ate",
            "SSSSISISSSS", 0),

    TURMA("turma",
            "id,tipo",
            "SS", 0),

    TURMA_CATEQUISTA("turma_catequista",
            "id_turma,id_catequista,ordem",
            "SSI", 0, 1),

    VINCULO_RESPONSAVEL("vinculo_responsavel",
            "id_catequisando,id_responsavel,parentesco,ordem",
            "SSSI", 0, 1),

    NOTICIA("noticia",
            "id,titulo,conteudo,tipo,data_publicacao,id_autor,id_turma",
            "SSSSSSS", 0),

    ATIVIDADE("atividade",
            "id,titulo,descricao,data_publicacao,prazo,id_autor,id_turma",
            "SSSSSSS", 0);

    final String nomeSql;
    final String[] colunas;
    final String tipos;
    final int[] chave;

    Tabela(String nomeSql, String colunas, String tipos, int... chave) {
        this.nomeSql = nomeSql;
        this.colunas = colunas.split(",");
        this.tipos = tipos;
        this.chave = chave;
    }

    /** Texto que identifica a linha dentro da tabela (valores da chave juntos). */
    String chaveDe(List<Object> linha) {
        StringBuilder texto = new StringBuilder();

        for (int posicao : chave) {
            texto.append(linha.get(posicao)).append('\u0001');
        }

        return texto.toString();
    }

    private boolean ehChave(int posicao) {
        for (int c : chave) {
            if (c == posicao) {
                return true;
            }
        }

        return false;
    }

    String sqlSelect() {
        StringBuilder ordem = new StringBuilder();

        for (int posicao : chave) {
            ordem.append(ordem.length() == 0 ? "" : ", ").append(colunas[posicao]);
        }

        return "SELECT " + String.join(", ", colunas)
                + " FROM " + nomeSql + " ORDER BY " + ordem;
    }

    String sqlInsert() {
        String interrogacoes = String.join(", ", java.util.Collections.nCopies(colunas.length, "?"));

        return "INSERT INTO " + nomeSql + " (" + String.join(", ", colunas)
                + ") VALUES (" + interrogacoes + ")";
    }

    /** As colunas que não são chave vêm primeiro, depois as da chave (cláusula WHERE). */
    String sqlUpdate() {
        StringBuilder definicoes = new StringBuilder();

        for (int i = 0; i < colunas.length; i++) {
            if (!ehChave(i)) {
                definicoes.append(definicoes.length() == 0 ? "" : ", ").append(colunas[i]).append(" = ?");
            }
        }

        return "UPDATE " + nomeSql + " SET " + definicoes + " WHERE " + condicaoDaChave();
    }

    String sqlDelete() {
        return "DELETE FROM " + nomeSql + " WHERE " + condicaoDaChave();
    }

    private String condicaoDaChave() {
        StringBuilder condicao = new StringBuilder();

        for (int posicao : chave) {
            condicao.append(condicao.length() == 0 ? "" : " AND ").append(colunas[posicao]).append(" = ?");
        }

        return condicao.toString();
    }

    /** Posições das colunas que não fazem parte da chave. */
    int[] naoChave() {
        int[] resultado = new int[colunas.length - chave.length];
        int destino = 0;

        for (int i = 0; i < colunas.length; i++) {
            if (!ehChave(i)) {
                resultado[destino++] = i;
            }
        }

        return resultado;
    }
}