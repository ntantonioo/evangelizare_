import persistencia.*;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import exception.CatequistaNaoEncontradoException;
import model.*;
import service.SistemaEvangelizare;
import service.SistemaEvangelizareMap;
import service.atividade.AtividadeService;
import service.atividade.AtividadeServiceMap;

class PersistenciaTest {

    static {
        // Deixa o hash de senha rápido nos testes (só afeta esta execução).
        System.setProperty("evangelizare.hash.iteracoes", "1000");
    }

    @TempDir
    Path pasta;

    // Um "programa em execução" sobre o arquivo do banco.
    static class Programa implements AutoCloseable {
        final RepositorioSqlite repositorio;
        final SistemaEvangelizare sistema;
        final AtividadeService atividades;
        final PersistenciaAutomatica persistencia;

        Programa(Path arquivo) {
            repositorio = RepositorioSqlite.abrir(arquivo);

            SistemaEvangelizareMap sistemaBase = new SistemaEvangelizareMap();
            AtividadeServiceMap atividadesBase = new AtividadeServiceMap(sistemaBase);

            persistencia = new PersistenciaAutomatica(repositorio, sistemaBase, atividadesBase);
            persistencia.carregar();

            sistema = persistencia.envolver(SistemaEvangelizare.class, sistemaBase);
            atividades = persistencia.envolver(AtividadeService.class, atividadesBase);
        }

        @Override
        public void close() {
            repositorio.close();
        }
    }

    private Path arquivo() {
        return pasta.resolve("teste.db");
    }

    private Programa abrir() {
        return new Programa(arquivo());
    }

    private static Administrador popular(Programa p) {
        Administrador admin = new Administrador("ADM001", "Administrador", "83999999999", 30, "admin1234");
        p.sistema.cadastrarAdministradorInicial(admin);

        Catequista maria = new Catequista("CAT001", "Maria Souza", "83988880000", 40, "senha123", "2015");
        Catequista joao = new Catequista("CAT002", "João Pereira", "83988880001", 35, "senha123", "2019");
        Catequista rosa = new Catequista("CAT003", "Rosa Lima", "83988880002", 50, "senha123", "2010");
        p.sistema.cadastrarCatequista(maria);
        p.sistema.cadastrarCatequista(joao);
        p.sistema.cadastrarCatequista(rosa);

        Turma infantil = new Turma("INF-01", TipoTurma.CATEQUESE, maria);
        p.sistema.cadastrarTurma(infantil);
        p.sistema.cadastrarTurma(new Turma("CJ-01", TipoTurma.CRISMA_JOVEM, joao));

        Catequisando ana = new Catequisando("CQS001", "Ana Clara", "83966660000", 9, "senha123", infantil);
        ana.adicionarResponsavel(new Responsavel("RESP-PAULA", "Paula Lima", "83977770000", 40, "senha123"), "Mãe");
        ana.adicionarResponsavel(new Responsavel("RESP-JOSE", "José Lima", "83977770001", 42, "senha123"), "Pai");
        p.sistema.cadastrarCatequisando(ana);

        p.sistema.cadastrarNoticia(
                new Noticia("N1", "Missa de abertura", "Domingo às 9h — com acentuação!", TipoNoticia.values()[0], admin, null), admin);
        p.sistema.cadastrarNoticia(
                new Noticia("N2", "Aviso da turma", "Fichas até sexta", TipoNoticia.values()[1], maria, infantil), maria);

        p.atividades.cadastrarAtividade("ATV-01", "Rezar o terço", "Em família", LocalDate.now().plusDays(10), "INF-01", maria);
        p.atividades.cadastrarAtividade("ATV-02", "Trazer a Bíblia", "No próximo encontro", null, "INF-01", maria);

        return admin;
    }

    private static Set<String> idsDeNoticias(List<Noticia> lista) {
        return lista.stream().map(Noticia::getIdNoticia).collect(Collectors.toSet());
    }

    private static Set<String> idsDeAtividades(List<Atividade> lista) {
        return lista.stream().map(Atividade::getIdAtividade).collect(Collectors.toSet());
    }

    @Nested
    @DisplayName("Abrir e carregar")
    class AbrirECarregar {

        @Test
        @DisplayName("banco novo: o arquivo é criado, vazio, e o sistema começa sem administrador")
        void bancoNovo() {
            try (Programa p = abrir()) {
                assertFalse(p.sistema.existeAdministrador());
                assertTrue(p.sistema.pesquisarCatequistaPorNome("").isEmpty());
            }

            assertTrue(arquivo().toFile().exists());
        }

        @Test
        @DisplayName("cria a pasta do banco se ela não existir")
        void criaPasta() {
            Path aninhado = pasta.resolve("a").resolve("b").resolve("sistema.db");

            try (RepositorioSqlite r = RepositorioSqlite.abrir(aninhado)) {
                assertNotNull(r.carregar());
            }

            assertTrue(aninhado.toFile().exists());
        }

        @Test
        @DisplayName("carregar duas vezes seguidas não grava nada de novo")
        void carregarNaoAltera() {
            try (Programa p = abrir()) {
                popular(p);
            }

            try (RepositorioSqlite r = RepositorioSqlite.abrir(arquivo())) {
                Estado estado = r.carregar();
                r.sincronizar(estado);

                assertEquals(0, r.linhasAlteradasNaUltimaSincronizacao(),
                        "o que foi lido é igual ao que está no banco");
            }
        }

        @Test
        @DisplayName("recusa um banco de uma versão mais nova do sistema")
        void versaoMaisNova() throws SQLException {
            try (Programa p = abrir()) {
                // cria o arquivo
            }

            try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + arquivo());
                 Statement s = c.createStatement()) {
                s.execute("PRAGMA user_version = 99");
            }

            PersistenciaException e = assertThrows(PersistenciaException.class, () -> RepositorioSqlite.abrir(arquivo()));
            assertTrue(e.getMessage().contains("versão mais nova"));
        }

        @Test
        @DisplayName("o esquema tem chaves estrangeiras: o banco recusa referências quebradas")
        void esquemaTemChavesEstrangeiras() throws SQLException {
            try (Programa p = abrir()) {
                // cria o arquivo
            }

            try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + arquivo());
                 Statement s = c.createStatement()) {
                s.execute("PRAGMA foreign_keys = ON");

                assertThrows(SQLException.class, () -> s.execute(
                                "INSERT INTO noticia (id, titulo, conteudo, tipo, data_publicacao, id_autor, id_turma) "
                                        + "VALUES ('N1', 't', 'c', 'AVISO', '2026-01-01T10:00:00', 'NAO-EXISTE', NULL)"),
                        "notícia com autor inexistente");
                assertThrows(SQLException.class, () -> s.execute(
                                "INSERT INTO usuario (id, tipo, nome, telefone, idade, senha_hash, senha_provisoria, id_turma) "
                                        + "VALUES ('U1', 'CATEQUISANDO', 'n', 't', 9, 'h', 0, 'TURMA-FANTASMA')"),
                        "catequisando em turma inexistente");
            }
        }
    }

    @Nested
    @DisplayName("Os dados sobrevivem a reiniciar o sistema")
    class Sobrevivem {

        @Test
        @DisplayName("usuários: cada um volta com os mesmos dados e entra com a mesma senha")
        void usuarios() {
            try (Programa p = abrir()) {
                popular(p);
            }

            try (Programa p = abrir()) {
                assertTrue(p.sistema.autenticarUsuario("ADM001", "admin1234"));
                assertTrue(p.sistema.autenticarUsuario("CAT001", "senha123"));
                assertTrue(p.sistema.autenticarUsuario("CQS001", "senha123"));
                assertTrue(p.sistema.autenticarUsuario("RESP-PAULA", "senha123"));
                assertFalse(p.sistema.autenticarUsuario("CAT001", "errada123"));

                Catequista maria = p.sistema.pesquisarCatequistaPorId("CAT001");
                assertEquals("Maria Souza", maria.getNome());
                assertEquals("2015", maria.getAnoDeIngresso());
                assertEquals(40, maria.getIdade());
                assertFalse(maria.getSenha().contains("senha123"), "no banco a senha é só o hash");
            }
        }

        @Test
        @DisplayName("senha provisória: quem tem continua com ela, e o administrador inicial não")
        void senhaProvisoria() {
            try (Programa p = abrir()) {
                popular(p);
                p.sistema.alterarSenha("CAT002", "senha123", "novaSenha1");
            }

            try (Programa p = abrir()) {
                assertFalse(p.sistema.pesquisarUsuarioPorId("ADM001").isSenhaProvisoria());
                assertTrue(p.sistema.pesquisarUsuarioPorId("CAT001").isSenhaProvisoria());
                assertFalse(p.sistema.pesquisarUsuarioPorId("CAT002").isSenhaProvisoria(), "já trocou a senha");
                assertTrue(p.sistema.autenticarUsuario("CAT002", "novaSenha1"));
                assertFalse(p.sistema.autenticarUsuario("CAT002", "senha123"));
            }
        }

        @Test
        @DisplayName("turmas, catequistas, catequisandos e responsáveis voltam ligados entre si")
        void ligacoes() {
            try (Programa p = abrir()) {
                popular(p);
            }

            try (Programa p = abrir()) {
                Turma infantil = p.sistema.pesquisarTurmaPorId("INF-01").get(0);
                Catequista maria = p.sistema.pesquisarCatequistaPorId("CAT001");
                Catequisando ana = p.sistema.pesquisarCatequisandoPorId("CQS001");

                assertEquals(TipoTurma.CATEQUESE, infantil.getTipoTurma());
                assertSame(maria, infantil.getCatequistas().get(0), "a turma aponta para o MESMO catequista do sistema");
                assertSame(infantil, ana.getTurma(), "o catequisando aponta para a MESMA turma do sistema");

                assertEquals(2, ana.getResponsaveis().size());
                assertEquals("Mãe", ana.getParentescoDe("RESP-PAULA"));
                assertEquals("Pai", ana.getParentescoDe("RESP-JOSE"));
                assertEquals("RESP-PAULA", ana.getResponsaveis().get(0).getId(), "a ordem dos responsáveis é mantida");
                assertSame(ana.getResponsaveis().get(0), p.sistema.pesquisarResponsavelPorId("RESP-PAULA"));
                assertEquals(List.of(ana), p.sistema.pesquisarCatequisandosPorResponsavel("RESP-JOSE"));
            }
        }

        @Test
        @DisplayName("turma com dois catequistas mantém a ordem")
        void ordemDosCatequistas() {
            try (Programa p = abrir()) {
                popular(p);

                Turma editada = new Turma("INF-01", TipoTurma.CATEQUESE, p.sistema.pesquisarCatequistaPorId("CAT003"));
                editada.adicionarCatequista(p.sistema.pesquisarCatequistaPorId("CAT001"));
                p.sistema.editarTurma(editada);
            }

            try (Programa p = abrir()) {
                List<String> ids = p.sistema.pesquisarTurmaPorId("INF-01").get(0).getCatequistas().stream()
                        .map(Catequista::getId).collect(Collectors.toList());

                assertEquals(List.of("CAT003", "CAT001"), ids);
            }
        }

        @Test
        @DisplayName("notícias: voltam com a data original, o autor e a turma")
        void noticias() {
            LocalDateTimeHolder original = new LocalDateTimeHolder();

            try (Programa p = abrir()) {
                popular(p);
                original.valor = p.sistema.pesquisarNoticiaPorId("N2").getDataPublicacao();
            }

            try (Programa p = abrir()) {
                Noticia geral = p.sistema.pesquisarNoticiaPorId("N1");
                Noticia daTurma = p.sistema.pesquisarNoticiaPorId("N2");

                assertEquals("Domingo às 9h — com acentuação!", geral.getConteudo());
                assertNull(geral.getTurma());
                assertSame(p.sistema.pesquisarUsuarioPorId("ADM001"), geral.getAutor());
                assertEquals(original.valor, daTurma.getDataPublicacao(), "a data não pode virar a data de hoje");
                assertSame(p.sistema.pesquisarTurmaPorId("INF-01").get(0), daTurma.getTurma());
                assertEquals(Set.of("N1", "N2"), idsDeNoticias(p.sistema.pesquisarNoticiasDisponiveisPara(
                        p.sistema.pesquisarUsuarioPorId("ADM001"))));
            }
        }

        @Test
        @DisplayName("atividades: voltam com e sem prazo, e continuam respeitando quem as vê")
        void atividades() {
            try (Programa p = abrir()) {
                popular(p);
            }

            try (Programa p = abrir()) {
                Atividade comPrazo = p.atividades.pesquisarAtividadePorId("ATV-01");
                Atividade semPrazo = p.atividades.pesquisarAtividadePorId("ATV-02");

                assertEquals(LocalDate.now().plusDays(10), comPrazo.getPrazo());
                assertNull(semPrazo.getPrazo());
                assertEquals("Rezar o terço", comPrazo.getTitulo());
                assertSame(p.sistema.pesquisarCatequistaPorId("CAT001"), comPrazo.getAutor());

                Catequisando ana = p.sistema.pesquisarCatequisandoPorId("CQS001");
                assertEquals(Set.of("ATV-01", "ATV-02"), idsDeAtividades(p.atividades.pesquisarAtividadesDisponiveisPara(ana)));
                assertTrue(p.atividades.pesquisarAtividadesDisponiveisPara(
                        p.sistema.pesquisarCatequistaPorId("CAT002")).isEmpty());
            }
        }

        @Test
        @DisplayName("os acentos e a pontuação dos textos são preservados")
        void acentos() {
            try (Programa p = abrir()) {
                popular(p);
            }

            try (Programa p = abrir()) {
                assertEquals("João Pereira", p.sistema.pesquisarCatequistaPorId("CAT002").getNome());
                assertEquals("Domingo às 9h — com acentuação!", p.sistema.pesquisarNoticiaPorId("N1").getConteudo());
            }
        }

        // pequeno recipiente para guardar um valor dentro de um bloco try
        class LocalDateTimeHolder {
            java.time.LocalDateTime valor;
        }
    }

    @Nested
    @DisplayName("Alterações são gravadas")
    class Alteracoes {

        @Test
        @DisplayName("cada tipo de alteração aparece depois de reiniciar")
        void alteracoesVariadas() {
            try (Programa p = abrir()) {
                popular(p);

                p.sistema.atualizarCatequista("CAT001", "83900001111", 41, "2014");
                p.sistema.redefinirSenha("CAT003", "temporaria", p.sistema.pesquisarUsuarioPorId("ADM001"));
                p.sistema.transferirCatequisando("CQS001", "CJ-01");
                p.sistema.atualizarCatequisando("CQS001", "83900002222", 10, List.of(
                        new VinculoResponsavel(p.sistema.pesquisarResponsavelPorId("RESP-JOSE"), "Pai")));
                p.sistema.removerNoticia("N2", p.sistema.pesquisarUsuarioPorId("ADM001"));
                p.atividades.atualizarAtividade("ATV-01", "Novo título", "Nova descrição", null,
                        p.sistema.pesquisarCatequistaPorId("CAT001"));
                p.atividades.removerAtividade("ATV-02", p.sistema.pesquisarUsuarioPorId("ADM001"));
            }

            try (Programa p = abrir()) {
                Catequista maria = p.sistema.pesquisarCatequistaPorId("CAT001");
                assertEquals("83900001111", maria.getTelefone());
                assertEquals("2014", maria.getAnoDeIngresso());

                assertTrue(p.sistema.autenticarUsuario("CAT003", "temporaria"));
                assertTrue(p.sistema.pesquisarUsuarioPorId("CAT003").isSenhaProvisoria());

                Catequisando ana = p.sistema.pesquisarCatequisandoPorId("CQS001");
                assertEquals("CJ-01", ana.getTurma().getIdTurma());
                assertEquals("83900002222", ana.getTelefone());
                assertEquals(10, ana.getIdade());
                assertEquals(1, ana.getResponsaveis().size());
                assertEquals("RESP-JOSE", ana.getResponsaveis().get(0).getId());

                assertThrows(exception.NoticiaNaoEncontradaException.class, () -> p.sistema.pesquisarNoticiaPorId("N2"));

                Atividade atv = p.atividades.pesquisarAtividadePorId("ATV-01");
                assertEquals("Novo título", atv.getTitulo());
                assertNull(atv.getPrazo());
                assertThrows(exception.AtividadeNaoEncontradaException.class, () -> p.atividades.pesquisarAtividadePorId("ATV-02"));
            }
        }

        @Test
        @DisplayName("remover catequisando apaga os vínculos, mas a conta do responsável fica")
        void removerCatequisando() {
            try (Programa p = abrir()) {
                popular(p);
                p.sistema.removerCatequisando("CQS001");
            }

            try (Programa p = abrir()) {
                assertThrows(exception.CatequisandoNaoEncontradoException.class,
                        () -> p.sistema.pesquisarCatequisandoPorId("CQS001"));
                assertTrue(p.sistema.autenticarUsuario("RESP-PAULA", "senha123"));
                assertTrue(p.sistema.pesquisarCatequisandosPorResponsavel("RESP-PAULA").isEmpty());
            }
        }

        @Test
        @DisplayName("só as linhas que mudaram vão ao banco")
        void gravacaoIncremental() {
            try (Programa p = abrir()) {
                popular(p);

                p.sistema.atualizarCatequista("CAT001", "83900001111", 41, "2014");
                assertEquals(1, p.repositorio.linhasAlteradasNaUltimaSincronizacao(), "só a linha da Maria");

                p.sistema.alterarSenha("CAT001", "senha123", "novaSenha1");
                assertEquals(1, p.repositorio.linhasAlteradasNaUltimaSincronizacao());

                p.sistema.removerCatequisando("CQS001");
                assertEquals(3, p.repositorio.linhasAlteradasNaUltimaSincronizacao(),
                        "a linha do catequisando e os 2 vínculos com os responsáveis");

                p.sistema.cadastrarCatequista(new Catequista("CAT009", "Nova", "83900000009", 30, "senha123", "2024"));
                assertEquals(1, p.repositorio.linhasAlteradasNaUltimaSincronizacao());
            }
        }

        @Test
        @DisplayName("consultas e operações recusadas não gravam nada")
        void consultasENegadasNaoGravam() {
            try (Programa p = abrir()) {
                popular(p);
                p.sistema.atualizarCatequista("CAT001", "83900001111", 41, "2014");
                assertEquals(1, p.repositorio.linhasAlteradasNaUltimaSincronizacao());

                p.sistema.pesquisarCatequistaPorId("CAT001");
                p.sistema.autenticarUsuario("ADM001", "admin1234");
                assertThrows(IllegalArgumentException.class, () -> p.sistema.cadastrarCatequista(
                        new Catequista("CAT001", "Duplicada", "83900000009", 30, "senha123", "2024")));

                assertEquals(1, p.repositorio.linhasAlteradasNaUltimaSincronizacao(),
                        "a última gravação continua sendo a da atualização");
            }

            try (Programa p = abrir()) {
                assertEquals("Maria Souza", p.sistema.pesquisarCatequistaPorId("CAT001").getNome(),
                        "a tentativa de cadastro duplicado não mexeu na Maria");
            }
        }

        @Test
        @DisplayName("as exceções do serviço chegam como eram, sem embrulho")
        void excecoesOriginais() {
            try (Programa p = abrir()) {
                popular(p);

                assertThrows(CatequistaNaoEncontradoException.class, () -> p.sistema.pesquisarCatequistaPorId("NAO-EXISTE"));
                assertThrows(CatequistaNaoEncontradoException.class,
                        () -> p.sistema.atualizarCatequista("NAO-EXISTE", "83900001111", 41, "2014"));
                IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                        () -> p.sistema.removerCatequista("CAT001"));
                assertTrue(e.getMessage().contains("turmas associadas"));
            }
        }
    }

    @Nested
    @DisplayName("Integridade: o que depende de algo que saiu também sai")
    class Integridade {

        @Test
        @DisplayName("remover uma turma remove as notícias e atividades dela, na hora e depois de reiniciar")
        void removerTurma() {
            try (Programa p = abrir()) {
                popular(p);
                Administrador admin = (Administrador) p.sistema.pesquisarUsuarioPorId("ADM001");
                Catequista rosa = p.sistema.pesquisarCatequistaPorId("CAT003");
                Turma nova = new Turma("NOVA-01", TipoTurma.CRISMA_ADULTO, rosa);
                p.sistema.cadastrarTurma(nova);
                p.sistema.cadastrarNoticia(new Noticia("N3", "Da nova", "c", TipoNoticia.values()[0], rosa, nova), rosa);
                p.atividades.cadastrarAtividade("ATV-03", "Da nova", "d", null, "NOVA-01", rosa);

                p.sistema.removerTurma("NOVA-01");

                assertEquals(Set.of("N1", "N2"), idsDeNoticias(p.sistema.pesquisarNoticiasDisponiveisPara(admin)));
                assertEquals(Set.of("ATV-01", "ATV-02"), idsDeAtividades(p.atividades.pesquisarAtividadesDisponiveisPara(admin)));
            }

            try (Programa p = abrir()) {
                Administrador admin = (Administrador) p.sistema.pesquisarUsuarioPorId("ADM001");

                assertEquals(Set.of("N1", "N2"), idsDeNoticias(p.sistema.pesquisarNoticiasDisponiveisPara(admin)));
                assertEquals(Set.of("ATV-01", "ATV-02"), idsDeAtividades(p.atividades.pesquisarAtividadesDisponiveisPara(admin)));
            }
        }

        @Test
        @DisplayName("remover um catequista remove as publicações dele")
        void removerCatequista() {
            try (Programa p = abrir()) {
                popular(p);
                Administrador admin = (Administrador) p.sistema.pesquisarUsuarioPorId("ADM001");

                // Maria sai da turma INF-01 (que passa a ser da Rosa) e depois é removida.
                p.sistema.editarTurma(new Turma("INF-01", TipoTurma.CATEQUESE, p.sistema.pesquisarCatequistaPorId("CAT003")));
                p.sistema.removerCatequista("CAT001");

                assertEquals(Set.of("N1"), idsDeNoticias(p.sistema.pesquisarNoticiasDisponiveisPara(admin)),
                        "a notícia N2 era da Maria");
                assertTrue(p.atividades.pesquisarAtividadesDisponiveisPara(admin).isEmpty(),
                        "as duas atividades eram da Maria");
            }

            try (Programa p = abrir()) {
                Administrador admin = (Administrador) p.sistema.pesquisarUsuarioPorId("ADM001");

                assertEquals(Set.of("N1"), idsDeNoticias(p.sistema.pesquisarNoticiasDisponiveisPara(admin)));
                assertTrue(p.atividades.pesquisarAtividadesDisponiveisPara(admin).isEmpty());
                assertTrue(p.sistema.pesquisarCatequistaPorNome("Maria").isEmpty());
            }
        }

        @Test
        @DisplayName("o banco recusa uma gravação com referência quebrada e nada é gravado")
        void gravacaoInconsistenteEhRecusada() {
            try (Programa p = abrir()) {
                popular(p);
            }

            Administrador admin = new Administrador("ADM999", "Outro", "83911110000", 30, "hashqualquer");
            Turma turmaForaDoEstado = new Turma("FANTASMA", TipoTurma.CATEQUESE,
                    new Catequista("CAT999", "Fantasma", "83911110001", 30, "h", "2020"));
            Noticia noticia = new Noticia("NX", "t", "c", TipoNoticia.values()[0], admin, turmaForaDoEstado);

            try (RepositorioSqlite r = RepositorioSqlite.abrir(arquivo())) {
                r.carregar();

                // a notícia aponta para uma turma que não está no estado
                Estado inconsistente = new Estado(List.of(admin), List.of(), List.of(noticia), List.of());

                assertThrows(PersistenciaException.class, () -> r.sincronizar(inconsistente));
            }

            try (Programa p = abrir()) {
                assertTrue(p.sistema.autenticarUsuario("ADM001", "admin1234"), "os dados antigos continuam intactos");
                assertNull(p.sistema.pesquisarUsuarioPorId("ADM999"));
                assertEquals(Set.of("N1", "N2"), idsDeNoticias(p.sistema.pesquisarNoticiasDisponiveisPara(
                        p.sistema.pesquisarUsuarioPorId("ADM001"))));
            }
        }
    }

    @Test
    @DisplayName("duas pessoas usando ao mesmo tempo: nada se perde")
    void usoSimultaneo() throws Exception {
        List<String> idsEsperados = new ArrayList<>();

        try (Programa p = abrir()) {
            p.sistema.cadastrarAdministradorInicial(
                    new Administrador("ADM001", "Administrador", "83999999999", 30, "admin1234"));

            ExecutorService pessoas = Executors.newFixedThreadPool(2);
            List<Future<?>> tarefas = new ArrayList<>();

            for (int pessoa = 0; pessoa < 2; pessoa++) {
                final int numero = pessoa;

                tarefas.add(pessoas.submit(() -> {
                    for (int i = 0; i < 15; i++) {
                        p.sistema.cadastrarCatequista(new Catequista(
                                "CAT-" + numero + "-" + i, "Catequista " + numero + "-" + i,
                                "83900000000", 30, "senha123", "2020"));
                    }
                }));

                for (int i = 0; i < 15; i++) {
                    idsEsperados.add("CAT-" + numero + "-" + i);
                }
            }

            for (Future<?> tarefa : tarefas) {
                tarefa.get(); // propaga qualquer erro ocorrido nas threads
            }

            pessoas.shutdown();
        }

        try (Programa p = abrir()) {
            assertEquals(30, p.sistema.pesquisarCatequistaPorNome("Catequista").size());

            for (String id : idsEsperados) {
                assertNotNull(p.sistema.pesquisarCatequistaPorId(id), id);
            }
        }
    }
}