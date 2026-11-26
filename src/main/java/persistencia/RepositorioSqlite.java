package persistencia;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RepositorioSqlite implements AutoCloseable {

    private static final int VERSAO_DO_ESQUEMA = 3;

    private static final String[] ESQUEMA = {
            """
            CREATE TABLE turma (
                id   TEXT PRIMARY KEY,
                tipo TEXT NOT NULL
            )""",
            """
            CREATE TABLE usuario (
                id               TEXT PRIMARY KEY,
                tipo             TEXT NOT NULL,
                nome             TEXT NOT NULL,
                telefone         TEXT NOT NULL,
                idade            INTEGER NOT NULL CHECK (idade > 0),
                senha_hash       TEXT NOT NULL,
                senha_provisoria INTEGER NOT NULL CHECK (senha_provisoria IN (0, 1)),
                ano_ingresso     TEXT,
                id_turma         TEXT REFERENCES turma (id) DEFERRABLE INITIALLY DEFERRED,
                login            TEXT NOT NULL DEFAULT '',
                provisoria_valida_ate TEXT,
                coordenador      INTEGER NOT NULL DEFAULT 0
            )""",
            """
            CREATE TABLE turma_catequista (
                id_turma      TEXT NOT NULL REFERENCES turma (id) DEFERRABLE INITIALLY DEFERRED,
                id_catequista TEXT NOT NULL REFERENCES usuario (id) DEFERRABLE INITIALLY DEFERRED,
                ordem         INTEGER NOT NULL,
                PRIMARY KEY (id_turma, id_catequista)
            )""",
            """
            CREATE TABLE vinculo_responsavel (
                id_catequisando TEXT NOT NULL REFERENCES usuario (id) DEFERRABLE INITIALLY DEFERRED,
                id_responsavel  TEXT NOT NULL REFERENCES usuario (id) DEFERRABLE INITIALLY DEFERRED,
                parentesco      TEXT NOT NULL,
                ordem           INTEGER NOT NULL,
                PRIMARY KEY (id_catequisando, id_responsavel)
            )""",
            """
            CREATE TABLE noticia (
                id              TEXT PRIMARY KEY,
                titulo          TEXT NOT NULL,
                conteudo        TEXT NOT NULL,
                tipo            TEXT NOT NULL,
                data_publicacao TEXT NOT NULL,
                id_autor        TEXT NOT NULL REFERENCES usuario (id) DEFERRABLE INITIALLY DEFERRED,
                id_turma        TEXT REFERENCES turma (id) DEFERRABLE INITIALLY DEFERRED
            )""",
            """
            CREATE TABLE atividade (
                id              TEXT PRIMARY KEY,
                titulo          TEXT NOT NULL,
                descricao       TEXT NOT NULL,
                data_publicacao TEXT NOT NULL,
                prazo           TEXT,
                id_autor        TEXT NOT NULL REFERENCES usuario (id) DEFERRABLE INITIALLY DEFERRED,
                id_turma        TEXT NOT NULL REFERENCES turma (id) DEFERRABLE INITIALLY DEFERRED
            )""",
            "CREATE INDEX indice_usuario_turma ON usuario (id_turma)",
            "CREATE UNIQUE INDEX indice_usuario_login ON usuario (lower(login))",
            "CREATE INDEX indice_noticia_turma ON noticia (id_turma)",
            "CREATE INDEX indice_atividade_turma ON atividade (id_turma)"
    };

    private static final String[][] MIGRACOES = {
            {
                    "ALTER TABLE usuario ADD COLUMN login TEXT NOT NULL DEFAULT ''",
                    "ALTER TABLE usuario ADD COLUMN provisoria_valida_ate TEXT",
                    "UPDATE usuario SET login = id",
                    "CREATE UNIQUE INDEX indice_usuario_login ON usuario (lower(login))"
            },
            {
                    "ALTER TABLE usuario ADD COLUMN coordenador INTEGER NOT NULL DEFAULT 0"
            }
    };

    private final Connection conexao;
    private Map<Tabela, Map<String, List<Object>>> base = Mapeador.linhasVazias();
    private int linhasAlteradas;

    private RepositorioSqlite(Connection conexao) {
        this.conexao = conexao;
    }

    /** Abre (ou cria) o banco no arquivo informado. */
    public static RepositorioSqlite abrir(Path arquivo) {
        try {
            Path pasta = arquivo.toAbsolutePath().getParent();

            if (pasta != null) {
                Files.createDirectories(pasta);
            }
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível criar a pasta do banco de dados.", e);
        }

        return abrirUrl("jdbc:sqlite:" + arquivo.toAbsolutePath());
    }

    /** Banco temporário, que some ao fechar. Útil em testes. */
    public static RepositorioSqlite abrirEmMemoria() {
        return abrirUrl("jdbc:sqlite::memory:");
    }

    private static RepositorioSqlite abrirUrl(String url) {
        Connection conexao = null;

        try {
            conexao = DriverManager.getConnection(url);

            try (Statement comando = conexao.createStatement()) {
                // Sem isto o SQLite ignora as chaves estrangeiras.
                comando.execute("PRAGMA foreign_keys = ON");

                try (ResultSet r = comando.executeQuery("PRAGMA foreign_keys")) {
                    if (!r.next() || r.getInt(1) != 1) {
                        throw new PersistenciaException(
                                "Esta versão do SQLite não oferece chaves estrangeiras.");
                    }
                }
            }

            prepararEsquema(conexao);

            return new RepositorioSqlite(conexao);

        } catch (SQLException | RuntimeException e) {
            fecharSilenciosamente(conexao);

            if (e instanceof PersistenciaException persistencia) {
                throw persistencia;
            }

            throw new PersistenciaException("Não foi possível abrir o banco de dados: " + e.getMessage(), e);
        }
    }

    private static void prepararEsquema(Connection conexao) throws SQLException {
        int versao;

        try (Statement comando = conexao.createStatement();
             ResultSet r = comando.executeQuery("PRAGMA user_version")) {
            r.next();
            versao = r.getInt(1);
        }

        if (versao > VERSAO_DO_ESQUEMA) {
            throw new PersistenciaException(
                    "Este banco foi criado por uma versão mais nova do sistema (esquema "
                            + versao + "). Atualize o sistema antes de abri-lo.");
        }

        if (versao == VERSAO_DO_ESQUEMA) {
            return;
        }

        // Banco novo cria o esquema atual; banco antigo recebe as migrações que faltam.
        // Tudo em uma transação: ou o banco passa para a versão nova, ou fica como estava.
        conexao.setAutoCommit(false);

        try (Statement comando = conexao.createStatement()) {
            if (versao == 0) {
                for (String instrucao : ESQUEMA) {
                    comando.execute(instrucao);
                }
            } else {
                for (int de = versao; de < VERSAO_DO_ESQUEMA; de++) {
                    for (String instrucao : MIGRACOES[de - 1]) {
                        comando.execute(instrucao);
                    }
                }
            }

            comando.execute("PRAGMA user_version = " + VERSAO_DO_ESQUEMA);
            conexao.commit();

        } catch (SQLException e) {
            conexao.rollback();
            throw e;

        } finally {
            conexao.setAutoCommit(true);
        }
    }

    // ---------- leitura ----------

    /** Lê tudo do banco e devolve os objetos já ligados entre si. */
    public synchronized Estado carregar() {
        Map<Tabela, Map<String, List<Object>>> lido = Mapeador.linhasVazias();

        try (Statement comando = conexao.createStatement()) {
            for (Tabela tabela : Tabela.values()) {
                try (ResultSet r = comando.executeQuery(tabela.sqlSelect())) {
                    while (r.next()) {
                        List<Object> linha = new ArrayList<>();

                        for (int i = 0; i < tabela.colunas.length; i++) {
                            if (tabela.tipos.charAt(i) == 'I') {
                                int valor = r.getInt(i + 1);
                                linha.add(r.wasNull() ? null : valor);
                            } else {
                                linha.add(r.getString(i + 1));
                            }
                        }

                        lido.get(tabela).put(tabela.chaveDe(linha), linha);
                    }
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Não foi possível ler o banco de dados: " + e.getMessage(), e);
        }

        Estado estado = Mapeador.paraObjetos(lido);
        base = lido;

        return estado;
    }

    // ---------- gravação ----------

    /**
     * Grava no banco as diferenças entre o estado informado e o que o banco já
     * tem. Se algo falhar (inclusive uma referência quebrada entre tabelas),
     * nada é gravado e a exceção é lançada.
     */
    public synchronized void sincronizar(Estado estado) {
        Map<Tabela, Map<String, List<Object>>> novo = Mapeador.paraLinhas(estado);
        int alteradas = 0;

        try {
            conexao.setAutoCommit(false);

            for (Tabela tabela : Tabela.values()) {
                alteradas += gravarDiferencas(tabela, base.get(tabela), novo.get(tabela));
            }

            conexao.commit();

        } catch (SQLException | RuntimeException e) {
            reverter();
            throw new PersistenciaException(
                    "Não foi possível gravar no banco de dados: " + e.getMessage(), e);

        } finally {
            try {
                conexao.setAutoCommit(true);
            } catch (SQLException ignorada) {
                // a conexão já está com problema; o erro principal é o que importa
            }
        }

        base = novo;
        linhasAlteradas = alteradas;
    }

    private int gravarDiferencas(
            Tabela tabela,
            Map<String, List<Object>> antes,
            Map<String, List<Object>> depois) throws SQLException {

        int total = 0;
        int[] naoChave = tabela.naoChave();

        try (PreparedStatement apagar = conexao.prepareStatement(tabela.sqlDelete());
             PreparedStatement inserir = conexao.prepareStatement(tabela.sqlInsert());
             PreparedStatement atualizar = conexao.prepareStatement(tabela.sqlUpdate())) {

            for (Map.Entry<String, List<Object>> entrada : antes.entrySet()) {
                if (!depois.containsKey(entrada.getKey())) {
                    for (int i = 0; i < tabela.chave.length; i++) {
                        ligar(apagar, i + 1, tabela, tabela.chave[i], entrada.getValue());
                    }

                    apagar.executeUpdate();
                    total++;
                }
            }

            for (Map.Entry<String, List<Object>> entrada : depois.entrySet()) {
                List<Object> anterior = antes.get(entrada.getKey());
                List<Object> linha = entrada.getValue();

                if (anterior == null) {
                    for (int i = 0; i < tabela.colunas.length; i++) {
                        ligar(inserir, i + 1, tabela, i, linha);
                    }

                    inserir.executeUpdate();
                    total++;

                } else if (!Objects.equals(anterior, linha)) {
                    int posicao = 1;

                    for (int coluna : naoChave) {
                        ligar(atualizar, posicao++, tabela, coluna, linha);
                    }

                    for (int coluna : tabela.chave) {
                        ligar(atualizar, posicao++, tabela, coluna, linha);
                    }

                    atualizar.executeUpdate();
                    total++;
                }
            }
        }

        return total;
    }

    private static void ligar(
            PreparedStatement comando, int indice, Tabela tabela, int coluna, List<Object> linha)
            throws SQLException {

        Object valor = linha.get(coluna);
        boolean inteiro = tabela.tipos.charAt(coluna) == 'I';

        if (valor == null) {
            comando.setNull(indice, inteiro ? Types.INTEGER : Types.VARCHAR);
        } else if (inteiro) {
            comando.setInt(indice, (Integer) valor);
        } else {
            comando.setString(indice, (String) valor);
        }
    }

    private void reverter() {
        try {
            conexao.rollback();
        } catch (SQLException ignorada) {
            // se nem o rollback funcionar, o SQLite descarta a transação ao fechar
        }
    }

    /** Quantas linhas (inserções, alterações e remoções) a última gravação enviou ao banco. */
    public synchronized int linhasAlteradasNaUltimaSincronizacao() {
        return linhasAlteradas;
    }

    @Override
    public synchronized void close() {
        fecharSilenciosamente(conexao);
    }

    private static void fecharSilenciosamente(Connection conexao) {
        if (conexao != null) {
            try {
                conexao.close();
            } catch (SQLException ignorada) {
                // nada a fazer
            }
        }
    }
}