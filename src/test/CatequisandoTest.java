
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
        Responsavel mae = new Responsavel("RESP-PAULA", "Paula", "83911110000", 40, "senha123");
        Responsavel pai = new Responsavel("RESP-JOSE", "José", "83922220000", 42, "senha123");

        c.adicionarResponsavel(mae, "Mãe");
        assertEquals(List.of(mae), c.getResponsaveis());
        assertEquals("Mãe", c.getParentescoDe("RESP-PAULA"));
        assertTrue(c.possuiResponsavel("RESP-PAULA"));

        c.atualizarResponsaveis(List.of(new VinculoResponsavel(pai, "Pai")));
        assertEquals(List.of(pai), c.getResponsaveis());
        assertFalse(c.possuiResponsavel("RESP-PAULA"));
        assertNull(c.getParentescoDe("RESP-PAULA"));
    }

    @Test
    @DisplayName("rejeita responsável nulo e lista nula ou com nulo")
    void rejeitaResponsaveisNulos() {
        Catequisando c = new Catequisando("CQS001", "Ana", "83900000009", 9, "senha123", turma);

        Responsavel mae = new Responsavel("RESP-PAULA", "Paula", "83911110000", 40, "senha123");

        assertThrows(IllegalArgumentException.class, () -> c.adicionarResponsavel(null, "Mãe"));
        assertThrows(IllegalArgumentException.class, () -> c.atualizarResponsaveis(null));
        assertThrows(IllegalArgumentException.class, () -> c.atualizarResponsaveis(Arrays.asList(
                new VinculoResponsavel(mae, "Mãe"), null)));
        assertTrue(c.getResponsaveis().isEmpty(), "lista recusada não pode alterar nada");
    }

    @Test
    @DisplayName("não aceita o mesmo responsável duas vezes")
    void naoAceitaResponsavelRepetido() {
        Catequisando c = new Catequisando("CQS001", "Ana", "83900000009", 9, "senha123", turma);
        Responsavel mae = new Responsavel("RESP-PAULA", "Paula", "83911110000", 40, "senha123");
        c.adicionarResponsavel(mae, "Mãe");

        assertThrows(IllegalArgumentException.class, () -> c.adicionarResponsavel(mae, "Tia"));
        assertThrows(IllegalArgumentException.class, () -> c.atualizarResponsaveis(List.of(
                new VinculoResponsavel(mae, "Mãe"), new VinculoResponsavel(mae, "Tia"))));
        assertEquals(1, c.getVinculos().size());
        assertEquals("Mãe", c.getParentescoDe("RESP-PAULA"));
    }

    @Test
    @DisplayName("a lista de vínculos devolvida é uma cópia")
    void listaDeVinculosEhCopia() {
        Catequisando c = new Catequisando("CQS001", "Ana", "83900000009", 9, "senha123", turma);
        c.adicionarResponsavel(new Responsavel("RESP-PAULA", "Paula", "83911110000", 40, "senha123"), "Mãe");

        c.getVinculos().clear();
        c.getResponsaveis().clear();

        assertEquals(1, c.getVinculos().size());
    }
}