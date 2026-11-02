import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CatequisandoTest {

    private Turma turma;

    @BeforeEach
    void setUp() {
        Catequista maria = new Catequista("CAT001", "Maria", "83900000001", 40, "senha123", "2015");
        turma = new Turma("INF-01", TipoTurma.CATEQUESE, maria);
    }

    @Test
    @DisplayName("cria catequisando válido, sem responsáveis no início")
    void criaValido() {
        Catequisando c = new Catequisando("CQS001", "Ana", "83900000009", 9, "senha123", turma);

        assertEquals("CQS001", c.getId());
        assertSame(turma, c.getTurma());
        assertTrue(c.getResponsaveis().isEmpty());
    }

    @Test
    @DisplayName("exige ID, nome, telefone e senha preenchidos")
    void exigeCampos() {
        assertThrows(IllegalArgumentException.class, () -> new Catequisando(" ", "Ana", "83900000009", 9, "senha123", turma));
        assertThrows(IllegalArgumentException.class, () -> new Catequisando("CQS001", "", "83900000009", 9, "senha123", turma));
        assertThrows(IllegalArgumentException.class, () -> new Catequisando("CQS001", "Ana", null, 9, "senha123", turma));
        assertThrows(IllegalArgumentException.class, () -> new Catequisando("CQS001", "Ana", "83900000009", 9, " ", turma));
    }

    @Test
    @DisplayName("exige idade positiva e turma informada")
    void exigeIdadeETurma() {
        assertThrows(IllegalArgumentException.class, () -> new Catequisando("CQS001", "Ana", "83900000009", 0, "senha123", turma));
        assertThrows(IllegalArgumentException.class, () -> new Catequisando("CQS001", "Ana", "83900000009", -3, "senha123", turma));
        assertThrows(IllegalArgumentException.class, () -> new Catequisando("CQS001", "Ana", "83900000009", 9, "senha123", null));
    }

    @Test
    @DisplayName("adiciona e substitui responsáveis")
    void responsaveis() {
        Catequisando c = new Catequisando("CQS001", "Ana", "83900000009", 9, "senha123", turma);
        Responsavel mae = new Responsavel("Paula", "83911110000", "Mãe");
        Responsavel pai = new Responsavel("José", "83922220000", "Pai");

        c.adicionarResponsavel(mae);
        assertEquals(List.of(mae), c.getResponsaveis());

        c.atualizarResponsaveis(List.of(pai));
        assertEquals(List.of(pai), c.getResponsaveis());
    }

    @Test
    @DisplayName("rejeita responsável nulo e lista nula ou com nulo")
    void rejeitaResponsaveisNulos() {
        Catequisando c = new Catequisando("CQS001", "Ana", "83900000009", 9, "senha123", turma);

        assertThrows(IllegalArgumentException.class, () -> c.adicionarResponsavel(null));
        assertThrows(IllegalArgumentException.class, () -> c.atualizarResponsaveis(null));
        assertThrows(IllegalArgumentException.class, () -> c.atualizarResponsaveis(Arrays.asList(
                new Responsavel("Paula", "83911110000", "Mãe"), null)));
    }

    @Test
    @DisplayName("responsável exige nome, telefone e parentesco")
    void responsavelExigeCampos() {
        assertThrows(IllegalArgumentException.class, () -> new Responsavel("", "83911110000", "Mãe"));
        assertThrows(IllegalArgumentException.class, () -> new Responsavel("Paula", " ", "Mãe"));
        assertThrows(IllegalArgumentException.class, () -> new Responsavel("Paula", "83911110000", null));
    }
}