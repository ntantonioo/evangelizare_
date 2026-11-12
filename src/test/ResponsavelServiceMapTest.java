import static org.junit.jupiter.api.Assertions.*;

import service.responsavel.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import model.*;
import service.SistemaEvangelizareMap;
import service.atividade.AtividadeServiceMap;

class ResponsavelServiceMapTest {

    static {
        // Deixa o hash de senha rápido nos testes (só afeta esta execução).
        System.setProperty("evangelizare.hash.iteracoes", "1000");
    }

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 4);
    private static final Clock RELOGIO =
            Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"), ZoneId.of("UTC"));

    private SistemaEvangelizareMap sistema;
    private AtividadeServiceMap atividades;
    private ResponsavelServiceMap servico;

    private Administrador admin;
    private Catequista maria, joao, rosa;
    private Turma infantil, jovem, adulto;
    private Responsavel paula, jose, rita;
    private Catequisando ana, caio, dani;

    @BeforeEach
    void setUp() {
        sistema = new SistemaEvangelizareMap();
        atividades = new AtividadeServiceMap(sistema, RELOGIO);
        servico = new ResponsavelServiceMap(sistema, atividades);

        admin = new Administrador("ADM001", "Administrador", "83999999999", 30, "admin123");
        sistema.cadastrarAdministrador(admin);

        maria = new Catequista("CAT001", "Maria Souza", "83988880000", 35, "senha123", "2018");
        joao = new Catequista("CAT002", "João Pereira", "83988880001", 35, "senha123", "2018");
        rosa = new Catequista("CAT003", "Rosa Lima", "83988880002", 35, "senha123", "2018");
        for (Catequista c : List.of(maria, joao, rosa)) {
            sistema.cadastrarCatequista(c);
        }

        infantil = new Turma("INF-01", TipoTurma.CATEQUESE, maria);
        jovem = new Turma("CJ-01", TipoTurma.CRISMA_JOVEM, joao);
        adulto = new Turma("CA-01", TipoTurma.CRISMA_ADULTO, rosa);
        for (Turma t : List.of(infantil, jovem, adulto)) {
            sistema.cadastrarTurma(t);
        }

        paula = new Responsavel("RESP-PAULA", "Paula Lima", "83977770000", 40, "senha123");
        jose = new Responsavel("RESP-JOSE", "José Lima", "83977770001", 42, "senha123");
        rita = new Responsavel("RESP-RITA", "Rita Alves", "83977770002", 38, "senha123");

        ana = new Catequisando("CQS001", "Ana Lima", "83966660000", 9, "senha123", infantil);
        ana.adicionarResponsavel(paula, "Mãe");
        ana.adicionarResponsavel(jose, "Pai");
        caio = new Catequisando("CQS002", "Caio Souza", "83966660001", 15, "senha123", jovem);
        caio.adicionarResponsavel(paula, "Tia");
        dani = new Catequisando("CQS003", "Dani Alves", "83966660002", 30, "senha123", adulto);
        dani.adicionarResponsavel(rita, "Mãe");
        for (Catequisando c : List.of(ana, caio, dani)) {
            sistema.cadastrarCatequisando(c);
        }
    }

    private static Set<String> idsNoticias(List<Noticia> lista) {
        return lista.stream().map(Noticia::getIdNoticia).collect(Collectors.toSet());
    }

    private static List<String> idsAtividades(List<Atividade> lista) {
        return lista.stream().map(Atividade::getIdAtividade).collect(Collectors.toList());
    }

    private void noticia(String id, Usuario autor, Turma turma) {
        sistema.cadastrarNoticia(
                new Noticia(id, "Título " + id, "Conteúdo " + id, TipoNoticia.AVISO, autor, turma), autor);
    }

    private void atividade(String id, Catequista autor, String idTurma, LocalDate prazo) {
        atividades.cadastrarAtividade(id, "Título " + id, "Descrição " + id, prazo, idTurma, autor);
    }

    @Test
    @DisplayName("filhos: todos os catequisandos vinculados, em ordem alfabética")
    void filhos() {
        assertEquals(List.of(ana, caio), servico.pesquisarFilhos(paula));
        assertEquals(List.of(ana), servico.pesquisarFilhos(jose));
        assertEquals(List.of(dani), servico.pesquisarFilhos(rita));
    }

    @Test
    @DisplayName("responsável sem filhos recebe listas vazias, sem erro")
    void semFilhos() {
        sistema.removerCatequisando("CQS003");

        assertTrue(servico.pesquisarFilhos(rita).isEmpty());
        assertTrue(servico.pesquisarNoticiasDosFilhos(rita).isEmpty());
        assertTrue(servico.pesquisarAtividadesDosFilhos(rita).isEmpty());
    }

    @Test
    @DisplayName("exige um responsável autenticado no sistema")
    void exigeResponsavelCadastrado() {
        Responsavel intruso = new Responsavel("RESP-X", "Intruso", "83977770009", 40, "senha123");
        Responsavel copiaDePaula = new Responsavel("RESP-PAULA", "Paula Lima", "83977770000", 40, "senha123");

        assertThrows(IllegalArgumentException.class, () -> servico.pesquisarFilhos(null));
        assertThrows(IllegalArgumentException.class, () -> servico.pesquisarFilhos(intruso));
        assertThrows(IllegalArgumentException.class, () -> servico.pesquisarFilhos(copiaDePaula),
                "um objeto igual, mas que não é o cadastrado, não vale");
        assertThrows(IllegalArgumentException.class, () -> servico.pesquisarNoticiasDosFilhos(intruso));
        assertThrows(IllegalArgumentException.class, () -> servico.pesquisarAtividadesDosFilhos(intruso));
    }

    @Test
    @DisplayName("notícias: as gerais e as das turmas dos filhos, sem repetir")
    void noticias() {
        noticia("GERAL", admin, null);
        noticia("INF", maria, infantil);
        noticia("JOV", joao, jovem);
        noticia("ADU", rosa, adulto);

        assertEquals(Set.of("GERAL", "INF", "JOV"), idsNoticias(servico.pesquisarNoticiasDosFilhos(paula)));
        assertEquals(3, servico.pesquisarNoticiasDosFilhos(paula).size(), "a notícia geral não pode aparecer duas vezes");
        assertEquals(Set.of("GERAL", "INF"), idsNoticias(servico.pesquisarNoticiasDosFilhos(jose)));
        assertEquals(Set.of("GERAL", "ADU"), idsNoticias(servico.pesquisarNoticiasDosFilhos(rita)));
    }

    @Test
    @DisplayName("notícias: sem nenhuma disponível, a lista vem vazia")
    void semNoticias() {
        assertTrue(servico.pesquisarNoticiasDosFilhos(paula).isEmpty());
    }

    @Test
    @DisplayName("notícias: depois de transferido, o filho traz as notícias da nova turma")
    void noticiasAposTransferencia() {
        noticia("INF", maria, infantil);
        noticia("JOV", joao, jovem);

        sistema.transferirCatequisando("CQS001", "CJ-01");

        assertEquals(Set.of("JOV"), idsNoticias(servico.pesquisarNoticiasDosFilhos(jose)));
    }

    @Test
    @DisplayName("atividades: as das turmas dos filhos, pelo prazo mais próximo, sem repetir")
    void atividadesDosFilhos() {
        atividade("INF-A", maria, "INF-01", HOJE.plusDays(10));
        atividade("INF-B", maria, "INF-01", null);
        atividade("JOV-A", joao, "CJ-01", HOJE.plusDays(3));
        atividade("ADU-A", rosa, "CA-01", HOJE.plusDays(1));

        assertEquals(List.of("JOV-A", "INF-A", "INF-B"), idsAtividades(servico.pesquisarAtividadesDosFilhos(paula)));
        assertEquals(List.of("INF-A", "INF-B"), idsAtividades(servico.pesquisarAtividadesDosFilhos(jose)));
        assertEquals(List.of("ADU-A"), idsAtividades(servico.pesquisarAtividadesDosFilhos(rita)));
    }

    @Test
    @DisplayName("atividades: irmãos na mesma turma não repetem atividades")
    void irmaosNaMesmaTurma() {
        Catequisando bia = new Catequisando("CQS004", "Bia Lima", "83966660003", 8, "senha123", infantil);
        bia.adicionarResponsavel(paula, "Mãe");
        sistema.cadastrarCatequisando(bia);
        atividade("INF-A", maria, "INF-01", null);

        assertEquals(List.of("INF-A"), idsAtividades(servico.pesquisarAtividadesDosFilhos(paula)));
    }

    @Test
    @DisplayName("atividades: um responsável não vê atividades de turmas que não são dos filhos")
    void naoVeTurmasAlheias() {
        atividade("ADU-A", rosa, "CA-01", null);

        assertTrue(servico.pesquisarAtividadesDosFilhos(paula).isEmpty());
        assertTrue(servico.pesquisarAtividadesDosFilhos(jose).isEmpty());
    }
}