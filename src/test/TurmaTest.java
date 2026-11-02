import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import model.*;

class TurmaTest {

    private Catequista maria, joao, pedro;
    private Turma turma;

    @BeforeEach
    void setUp() {
        maria = new Catequista("CAT001", "Maria", "83900000001", 40, "senha123", "2015");
        joao = new Catequista("CAT002", "João", "83900000002", 35, "senha123", "2019");
        pedro = new Catequista("CAT003", "Pedro", "83900000003", 30, "senha123", "2021");
        turma = new Turma("INF-01", TipoTurma.CATEQUESE, maria);
    }

    @Test
    @DisplayName("nasce com o catequista informado")
    void nasceComCatequista() {
        assertEquals(List.of(maria), turma.getCatequistas());
        assertSame(maria, turma.getCatequista());
    }

    @Test
    @DisplayName("aceita um segundo catequista")
    void aceitaSegundoCatequista() {
        turma.adicionarCatequista(joao);

        assertEquals(2, turma.getCatequistas().size());
    }

    @Test
    @DisplayName("não aceita um terceiro catequista")
    void naoAceitaTerceiro() {
        turma.adicionarCatequista(joao);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> turma.adicionarCatequista(pedro));
        assertTrue(e.getMessage().contains("dois catequistas"));
        assertEquals(2, turma.getCatequistas().size());
    }

    @Test
    @DisplayName("não aceita o mesmo catequista duas vezes, nem catequista nulo")
    void naoAceitaDuplicadoNemNulo() {
        assertThrows(IllegalArgumentException.class, () -> turma.adicionarCatequista(maria));
        assertThrows(IllegalArgumentException.class, () -> turma.adicionarCatequista(null));
        assertEquals(1, turma.getCatequistas().size());
    }

    @Test
    @DisplayName("a lista devolvida é uma cópia: alterá-la não muda a turma")
    void listaDevolvidaEhCopia() {
        turma.getCatequistas().clear();

        assertEquals(1, turma.getCatequistas().size());
    }

    @Test
    @DisplayName("atualizarCatequistas troca a lista, validando antes de alterar")
    void atualizarCatequistas() {
        turma.atualizarCatequistas(List.of(joao, pedro));
        assertEquals(List.of(joao, pedro), turma.getCatequistas());

        assertThrows(IllegalArgumentException.class, () -> turma.atualizarCatequistas(List.of()));
        assertThrows(IllegalArgumentException.class, () -> turma.atualizarCatequistas(List.of(maria, joao, pedro)));
        assertThrows(IllegalArgumentException.class, () -> turma.atualizarCatequistas(List.of(maria, maria)));
        assertEquals(List.of(joao, pedro), turma.getCatequistas(), "listas inválidas não podem alterar a turma");
    }
}