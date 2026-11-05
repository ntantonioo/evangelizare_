import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import model.*;

class AtividadeTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 4);

    private Catequista maria;
    private Turma turma;

    @BeforeEach
    void setUp() {
        maria = new Catequista("CAT001", "Maria", "83900000001", 40, "senha123", "2015");
        turma = new Turma("INF-01", TipoTurma.CATEQUESE, maria);
    }

    private Atividade nova(LocalDate prazo) {
        return new Atividade("ATV-01", "Rezar o terço", "Rezar em família", HOJE, prazo, maria, turma);
    }

    @Test
    @DisplayName("cria atividade válida, com ou sem prazo")
    void criaValida() {
        Atividade comPrazo = nova(HOJE.plusDays(7));
        assertEquals("ATV-01", comPrazo.getIdAtividade());
        assertEquals(HOJE.plusDays(7), comPrazo.getPrazo());
        assertSame(maria, comPrazo.getAutor());
        assertSame(turma, comPrazo.getTurma());

        assertNull(nova(null).getPrazo());
    }

    @Test
    @DisplayName("aceita prazo igual à data de publicação, mas não anterior")
    void prazoNoPassado() {
        assertDoesNotThrow(() -> nova(HOJE));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> nova(HOJE.minusDays(1)));
        assertTrue(e.getMessage().contains("passado"));
    }

    @Test
    @DisplayName("exige ID, título e descrição preenchidos")
    void exigeTextos() {
        assertThrows(IllegalArgumentException.class,
                () -> new Atividade(" ", "T", "D", HOJE, null, maria, turma));
        assertThrows(IllegalArgumentException.class,
                () -> new Atividade("A", "", "D", HOJE, null, maria, turma));
        assertThrows(IllegalArgumentException.class,
                () -> new Atividade("A", "T", null, HOJE, null, maria, turma));
    }

    @Test
    @DisplayName("exige data de publicação, autor e turma")
    void exigeDemais() {
        assertThrows(IllegalArgumentException.class,
                () -> new Atividade("A", "T", "D", null, null, maria, turma));
        assertThrows(IllegalArgumentException.class,
                () -> new Atividade("A", "T", "D", HOJE, null, null, turma));
        assertThrows(IllegalArgumentException.class,
                () -> new Atividade("A", "T", "D", HOJE, null, maria, null));
    }

    @Test
    @DisplayName("remove espaços extras dos textos")
    void removeEspacos() {
        Atividade a = new Atividade("  ATV-01 ", "  Título  ", " Descrição ", HOJE, null, maria, turma);

        assertEquals("ATV-01", a.getIdAtividade());
        assertEquals("Título", a.getTitulo());
        assertEquals("Descrição", a.getDescricao());
    }

    @Test
    @DisplayName("estaEncerrada: só depois do dia do prazo; sem prazo nunca encerra")
    void encerrada() {
        Atividade a = nova(HOJE.plusDays(2));

        assertFalse(a.estaEncerrada(HOJE));
        assertFalse(a.estaEncerrada(HOJE.plusDays(2)), "no dia do prazo ainda está aberta");
        assertTrue(a.estaEncerrada(HOJE.plusDays(3)));
        assertFalse(nova(null).estaEncerrada(HOJE.plusYears(10)));
    }

    @Test
    @DisplayName("diasRestantes conta os dias até o prazo; nulo sem prazo")
    void diasRestantes() {
        assertEquals(5L, nova(HOJE.plusDays(5)).diasRestantes(HOJE));
        assertEquals(0L, nova(HOJE).diasRestantes(HOJE));
        assertNull(nova(null).diasRestantes(HOJE));
    }

    @Test
    @DisplayName("atualizar altera título, descrição e prazo")
    void atualiza() {
        Atividade a = nova(HOJE.plusDays(7));

        a.atualizar("Novo título", "Nova descrição", null, HOJE);

        assertEquals("Novo título", a.getTitulo());
        assertEquals("Nova descrição", a.getDescricao());
        assertNull(a.getPrazo(), "atualizar com prazo nulo remove o prazo");
    }

    @Test
    @DisplayName("atualização inválida não altera nada")
    void atualizacaoInvalidaNaoAlteraNada() {
        Atividade a = nova(HOJE.plusDays(7));

        assertThrows(IllegalArgumentException.class, () -> a.atualizar("Novo título", "Nova descrição", HOJE.minusDays(1), HOJE));
        assertThrows(IllegalArgumentException.class, () -> a.atualizar("Novo título", " ", HOJE.plusDays(9), HOJE));
        assertThrows(IllegalArgumentException.class, () -> a.atualizar(null, "Nova descrição", HOJE.plusDays(9), HOJE));

        assertEquals("Rezar o terço", a.getTitulo());
        assertEquals("Rezar em família", a.getDescricao());
        assertEquals(HOJE.plusDays(7), a.getPrazo());
    }
}