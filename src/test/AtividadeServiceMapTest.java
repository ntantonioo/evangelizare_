import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import exception.AtividadeNaoEncontradaException;
import exception.TurmaNaoEncontradaException;
import model.*;
import service.*;
import service.atividade.AtividadeServiceMap;

/**
 * Testa as regras das atividades. A data de "hoje" é fixa (04/11/2026).
 *
 *   ADM001 (administrador)
 *   CAT001 Maria -> INF-01 (Catequese Infantil), com o catequisando CQS001
 *   CAT002 João  -> CJ-01  (Crisma Jovem), com o catequisando CQS002
 *   CAT003 Rosa e CAT004 Pedro -> DUO-01 (Crisma Adulto), os dois juntos
 *   CAT005 Lúcia -> sem turma
 */
class AtividadeServiceMapTest {

    static {
        // Deixa o hash de senha rápido nos testes (só afeta esta execução).
        System.setProperty("evangelizare.hash.iteracoes", "1000");
    }

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 4);
    private static final Clock RELOGIO =
            Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"), ZoneId.of("UTC"));

    private SistemaEvangelizareMap sistema;
    private AtividadeServiceMap atividades;
    private Administrador admin;
    private Catequista maria, joao, rosa, pedro, lucia;
    private Turma infantil, jovem, dupla;
    private Catequisando alunoInfantil, alunoJovem;

    @BeforeEach
    void setUp() {
        sistema = new SistemaEvangelizareMap();
        atividades = new AtividadeServiceMap(sistema, RELOGIO);

        admin = new Administrador("ADM001", "Administrador", "83999999999", 30, "admin123");
        sistema.cadastrarAdministrador(admin);

        maria = catequista("CAT001", "Maria Souza");
        joao = catequista("CAT002", "João Pereira");
        rosa = catequista("CAT003", "Rosa Lima");
        pedro = catequista("CAT004", "Pedro Alves");
        lucia = catequista("CAT005", "Lúcia Reis");
        for (Catequista c : List.of(maria, joao, rosa, pedro, lucia)) {
            sistema.cadastrarCatequista(c);
        }

        infantil = new Turma("INF-01", TipoTurma.CATEQUESE, maria);
        jovem = new Turma("CJ-01", TipoTurma.CRISMA_JOVEM, joao);
        dupla = new Turma("DUO-01", TipoTurma.CRISMA_ADULTO, rosa);
        dupla.adicionarCatequista(pedro);
        for (Turma t : List.of(infantil, jovem, dupla)) {
            sistema.cadastrarTurma(t);
        }

        alunoInfantil = catequisando("CQS001", infantil);
        alunoJovem = catequisando("CQS002", jovem);
        sistema.cadastrarCatequisando(alunoInfantil);
        sistema.cadastrarCatequisando(alunoJovem);
    }

    // ---------- auxiliares ----------

    private static Catequista catequista(String id, String nome) {
        return new Catequista(id, nome, "83988880000", 35, "senha123", "2018");
    }

    private static Catequisando catequisando(String id, Turma turma) {
        Catequisando c = new Catequisando(id, "Aluno " + id, "83966660000", 12, "senha123", turma);
        c.adicionarResponsavel(new Responsavel("Paula Lima", "83977770000", "Mãe"));
        return c;
    }

    private Atividade publicar(String id, Catequista autor, String idTurma, LocalDate prazo) {
        return atividades.cadastrarAtividade(id, "Título " + id, "Descrição " + id, prazo, idTurma, autor);
    }

    private static List<String> ids(List<Atividade> lista) {
        return lista.stream().map(Atividade::getIdAtividade).collect(Collectors.toList());
    }

    private static void assertMensagemContem(IllegalArgumentException e, String trecho) {
        assertTrue(e.getMessage().contains(trecho),
                "Mensagem esperada com \"" + trecho + "\", mas foi: " + e.getMessage());
    }

    // =====================================================================

    @Nested
    @DisplayName("Publicar")
    class Publicar {

        @Test
        @DisplayName("catequista publica para a própria turma; a data de publicação é hoje")
        void publicaParaPropriaTurma() {
            Atividade a = publicar("ATV-01", maria, "INF-01", HOJE.plusDays(7));

            assertSame(a, atividades.pesquisarAtividadePorId("ATV-01"));
            assertSame(maria, a.getAutor());
            assertSame(infantil, a.getTurma());
            assertEquals(HOJE, a.getDataPublicacao());
            assertEquals(HOJE.plusDays(7), a.getPrazo());
        }

        @Test
        @DisplayName("o prazo é opcional")
        void prazoOpcional() {
            assertNull(publicar("ATV-01", maria, "INF-01", null).getPrazo());
        }

        @Test
        @DisplayName("aceita prazo para hoje, mas rejeita prazo no passado")
        void prazoNoPassado() {
            assertDoesNotThrow(() -> publicar("ATV-01", maria, "INF-01", HOJE));

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> publicar("ATV-02", maria, "INF-01", HOJE.minusDays(1)));
            assertMensagemContem(e, "passado");
            assertThrows(AtividadeNaoEncontradaException.class, () -> atividades.pesquisarAtividadePorId("ATV-02"));
        }

        @Test
        @DisplayName("qualquer catequista de uma turma com dois catequistas pode publicar")
        void doisCatequistasNaMesmaTurma() {
            publicar("ATV-01", rosa, "DUO-01", null);
            publicar("ATV-02", pedro, "DUO-01", null);

            assertEquals(2, atividades.pesquisarAtividadesDisponiveisPara(rosa).size());
        }

        @Test
        @DisplayName("catequista não publica para turma de outro catequista")
        void naoPublicaParaTurmaAlheia() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> publicar("ATV-01", maria, "CJ-01", null));
            assertMensagemContem(e, "suas próprias turmas");
        }

        @Test
        @DisplayName("catequista sem turma não publica")
        void catequistaSemTurma() {
            assertThrows(IllegalArgumentException.class, () -> publicar("ATV-01", lucia, "INF-01", null));
        }

        @Test
        @DisplayName("administrador e catequisando não publicam")
        void soCatequistaPublica() {
            IllegalArgumentException e1 = assertThrows(IllegalArgumentException.class,
                    () -> atividades.cadastrarAtividade("ATV-01", "T", "D", null, "INF-01", admin));
            assertMensagemContem(e1, "Somente catequistas");

            assertThrows(IllegalArgumentException.class,
                    () -> atividades.cadastrarAtividade("ATV-01", "T", "D", null, "INF-01", alunoInfantil));
        }

        @Test
        @DisplayName("exige usuário autenticado no sistema")
        void exigeUsuarioCadastrado() {
            Catequista intruso = catequista("CAT666", "Intruso");
            Catequista copiaDeMaria = catequista("CAT001", "Maria Souza");

            assertThrows(IllegalArgumentException.class,
                    () -> atividades.cadastrarAtividade("ATV-01", "T", "D", null, "INF-01", null));
            assertThrows(IllegalArgumentException.class,
                    () -> atividades.cadastrarAtividade("ATV-01", "T", "D", null, "INF-01", intruso));
            assertThrows(IllegalArgumentException.class,
                    () -> atividades.cadastrarAtividade("ATV-01", "T", "D", null, "INF-01", copiaDeMaria),
                    "um objeto igual, mas que não é o cadastrado, não vale");
        }

        @Test
        @DisplayName("turma inexistente lança TurmaNaoEncontradaException; turma em branco é rejeitada")
        void turmaInvalida() {
            assertThrows(TurmaNaoEncontradaException.class, () -> publicar("ATV-01", maria, "NAO-EXISTE", null));
            assertThrows(IllegalArgumentException.class, () -> publicar("ATV-01", maria, "  ", null));
            assertThrows(IllegalArgumentException.class, () -> publicar("ATV-01", maria, null, null));
        }

        @Test
        @DisplayName("rejeita ID repetido, e a atividade original não muda")
        void idRepetido() {
            publicar("ATV-01", maria, "INF-01", null);

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> atividades.cadastrarAtividade("ATV-01", "Outro título", "Outra descrição", null, "INF-01", maria));
            assertMensagemContem(e, "Já existe");
            assertEquals("Título ATV-01", atividades.pesquisarAtividadePorId("ATV-01").getTitulo());
        }

        @Test
        @DisplayName("rejeita ID, título ou descrição em branco")
        void camposObrigatorios() {
            assertThrows(IllegalArgumentException.class,
                    () -> atividades.cadastrarAtividade(" ", "T", "D", null, "INF-01", maria));
            assertThrows(IllegalArgumentException.class,
                    () -> atividades.cadastrarAtividade("ATV-01", "", "D", null, "INF-01", maria));
            assertThrows(IllegalArgumentException.class,
                    () -> atividades.cadastrarAtividade("ATV-01", "T", null, null, "INF-01", maria));
            assertTrue(atividades.pesquisarAtividadesDisponiveisPara(admin).isEmpty());
        }
    }

    // =====================================================================

    @Nested
    @DisplayName("Alterar")
    class Alterar {

        @Test
        @DisplayName("o autor altera título, descrição e prazo")
        void autorAltera() {
            publicar("ATV-01", maria, "INF-01", HOJE.plusDays(7));

            atividades.atualizarAtividade("ATV-01", "Novo título", "Nova descrição", HOJE.plusDays(14), maria);

            Atividade a = atividades.pesquisarAtividadePorId("ATV-01");
            assertEquals("Novo título", a.getTitulo());
            assertEquals("Nova descrição", a.getDescricao());
            assertEquals(HOJE.plusDays(14), a.getPrazo());
        }

        @Test
        @DisplayName("o autor pode remover o prazo")
        void removePrazo() {
            publicar("ATV-01", maria, "INF-01", HOJE.plusDays(7));

            atividades.atualizarAtividade("ATV-01", "Título", "Descrição", null, maria);

            assertNull(atividades.pesquisarAtividadePorId("ATV-01").getPrazo());
        }

        @Test
        @DisplayName("só o autor altera: nem o outro catequista da turma, nem o administrador")
        void soOAutorAltera() {
            publicar("ATV-01", rosa, "DUO-01", null);

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> atividades.atualizarAtividade("ATV-01", "X", "Y", null, pedro));
            assertMensagemContem(e, "Somente o catequista que publicou");
            assertThrows(IllegalArgumentException.class,
                    () -> atividades.atualizarAtividade("ATV-01", "X", "Y", null, admin));
            assertThrows(IllegalArgumentException.class,
                    () -> atividades.atualizarAtividade("ATV-01", "X", "Y", null, alunoInfantil));
            assertEquals("Título ATV-01", atividades.pesquisarAtividadePorId("ATV-01").getTitulo());
        }

        @Test
        @DisplayName("prazo no passado é rejeitado e nada muda")
        void prazoNoPassadoNaoAlteraNada() {
            publicar("ATV-01", maria, "INF-01", HOJE.plusDays(7));

            assertThrows(IllegalArgumentException.class,
                    () -> atividades.atualizarAtividade("ATV-01", "Novo título", "Nova descrição", HOJE.minusDays(1), maria));

            Atividade a = atividades.pesquisarAtividadePorId("ATV-01");
            assertEquals("Título ATV-01", a.getTitulo());
            assertEquals(HOJE.plusDays(7), a.getPrazo());
        }

        @Test
        @DisplayName("atividade inexistente lança exceção")
        void inexistente() {
            assertThrows(AtividadeNaoEncontradaException.class,
                    () -> atividades.atualizarAtividade("NAO-EXISTE", "X", "Y", null, maria));
        }
    }

    // =====================================================================

    @Nested
    @DisplayName("Remover e pesquisar")
    class RemoverEPesquisar {

        @Test
        @DisplayName("o autor remove a própria atividade")
        void autorRemove() {
            publicar("ATV-01", maria, "INF-01", null);

            atividades.removerAtividade("ATV-01", maria);

            assertThrows(AtividadeNaoEncontradaException.class, () -> atividades.pesquisarAtividadePorId("ATV-01"));
        }

        @Test
        @DisplayName("o administrador remove qualquer atividade")
        void adminRemove() {
            publicar("ATV-01", maria, "INF-01", null);

            atividades.removerAtividade("ATV-01", admin);

            assertTrue(atividades.pesquisarAtividadesDisponiveisPara(admin).isEmpty());
        }

        @Test
        @DisplayName("outro catequista e o catequisando não removem")
        void outrosNaoRemovem() {
            publicar("ATV-01", maria, "INF-01", null);

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> atividades.removerAtividade("ATV-01", joao));
            assertMensagemContem(e, "Somente o administrador ou o catequista que publicou");
            assertThrows(IllegalArgumentException.class, () -> atividades.removerAtividade("ATV-01", alunoInfantil));
            assertNotNull(atividades.pesquisarAtividadePorId("ATV-01"));
        }

        @Test
        @DisplayName("remover atividade inexistente lança exceção")
        void removerInexistente() {
            assertThrows(AtividadeNaoEncontradaException.class, () -> atividades.removerAtividade("NAO-EXISTE", admin));
        }

        @Test
        @DisplayName("pesquisa por ID ignora espaços; ID nulo ou inexistente lança exceção")
        void pesquisaPorId() {
            Atividade a = publicar("ATV-01", maria, "INF-01", null);

            assertSame(a, atividades.pesquisarAtividadePorId("  ATV-01 "));
            assertThrows(AtividadeNaoEncontradaException.class, () -> atividades.pesquisarAtividadePorId("NAO-EXISTE"));
            assertThrows(AtividadeNaoEncontradaException.class, () -> atividades.pesquisarAtividadePorId(null));
        }
    }

    // =====================================================================

    @Nested
    @DisplayName("Quem vê o quê")
    class Visibilidade {

        @BeforeEach
        void publicaAtividades() {
            publicar("INF-A", maria, "INF-01", HOJE.plusDays(10));
            publicar("JOV-A", joao, "CJ-01", HOJE.plusDays(3));
            publicar("DUO-A", rosa, "DUO-01", null);
            publicar("DUO-B", pedro, "DUO-01", HOJE.plusDays(1));
        }

        @Test
        @DisplayName("administrador vê todas")
        void adminVeTodas() {
            assertEquals(4, atividades.pesquisarAtividadesDisponiveisPara(admin).size());
        }

        @Test
        @DisplayName("catequista vê as das suas turmas, inclusive as do outro catequista da turma")
        void catequistaVeSuasTurmas() {
            assertEquals(List.of("INF-A"), ids(atividades.pesquisarAtividadesDisponiveisPara(maria)));
            assertEquals(List.of("JOV-A"), ids(atividades.pesquisarAtividadesDisponiveisPara(joao)));
            assertEquals(List.of("DUO-B", "DUO-A"), ids(atividades.pesquisarAtividadesDisponiveisPara(rosa)));
            assertEquals(List.of("DUO-B", "DUO-A"), ids(atividades.pesquisarAtividadesDisponiveisPara(pedro)));
            assertTrue(atividades.pesquisarAtividadesDisponiveisPara(lucia).isEmpty());
        }

        @Test
        @DisplayName("catequisando vê só as da própria turma")
        void catequisandoVeSuaTurma() {
            assertEquals(List.of("INF-A"), ids(atividades.pesquisarAtividadesDisponiveisPara(alunoInfantil)));
            assertEquals(List.of("JOV-A"), ids(atividades.pesquisarAtividadesDisponiveisPara(alunoJovem)));
        }

        @Test
        @DisplayName("depois de transferido, o catequisando passa a ver as da nova turma")
        void transferidoVeNovaTurma() {
            sistema.transferirCatequisando("CQS001", "CJ-01");

            assertEquals(List.of("JOV-A"), ids(atividades.pesquisarAtividadesDisponiveisPara(alunoInfantil)));
        }

        @Test
        @DisplayName("ordena pelo prazo mais próximo; sem prazo por último")
        void ordenacao() {
            assertEquals(List.of("DUO-B", "JOV-A", "INF-A", "DUO-A"),
                    ids(atividades.pesquisarAtividadesDisponiveisPara(admin)));
        }

        @Test
        @DisplayName("turma removida: some para catequista e catequisando, o administrador ainda vê")
        void turmaRemovida() {
            sistema.removerCatequisando("CQS002");
            sistema.removerTurma("CJ-01");

            assertTrue(atividades.pesquisarAtividadesDisponiveisPara(joao).isEmpty());
            assertEquals(4, atividades.pesquisarAtividadesDisponiveisPara(admin).size());
        }

        @Test
        @DisplayName("exige usuário autenticado no sistema")
        void exigeUsuarioCadastrado() {
            assertThrows(IllegalArgumentException.class, () -> atividades.pesquisarAtividadesDisponiveisPara(null));
            assertThrows(IllegalArgumentException.class,
                    () -> atividades.pesquisarAtividadesDisponiveisPara(catequista("CAT666", "Intruso")));
        }
    }
}