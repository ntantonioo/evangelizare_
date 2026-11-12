import static org.junit.jupiter.api.Assertions.*;

import model.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ResponsavelTest {

    @Test
    @DisplayName("cria responsável válido, que é um usuário")
    void criaValido() {
        Responsavel r = new Responsavel("RESP-PAULA", "Paula Lima", "83911110000", 40, "senha123");

        assertInstanceOf(Usuario.class, r);
        assertEquals("RESP-PAULA", r.getId());
        assertEquals("Paula Lima", r.getNome());
        assertEquals(40, r.getIdade());
    }

    @Test
    @DisplayName("remove espaços do ID, nome e telefone, mas não da senha")
    void removeEspacos() {
        Responsavel r = new Responsavel("  RESP-PAULA ", " Paula ", " 83911110000 ", 40, " senha com espaço ");

        assertEquals("RESP-PAULA", r.getId());
        assertEquals("Paula", r.getNome());
        assertEquals("83911110000", r.getTelefone());
        assertEquals(" senha com espaço ", r.getSenha());
    }

    @Test
    @DisplayName("exige ID, nome, telefone e senha preenchidos")
    void exigeCampos() {
        assertThrows(IllegalArgumentException.class, () -> new Responsavel(" ", "Paula", "83911110000", 40, "senha123"));
        assertThrows(IllegalArgumentException.class, () -> new Responsavel("R", "", "83911110000", 40, "senha123"));
        assertThrows(IllegalArgumentException.class, () -> new Responsavel("R", "Paula", null, 40, "senha123"));
        assertThrows(IllegalArgumentException.class, () -> new Responsavel("R", "Paula", "83911110000", 40, " "));
        assertThrows(IllegalArgumentException.class, () -> new Responsavel("R", "Paula", "83911110000", 40, null));
    }

    @Test
    @DisplayName("exige idade positiva")
    void exigeIdade() {
        assertThrows(IllegalArgumentException.class, () -> new Responsavel("R", "Paula", "83911110000", 0, "senha123"));
        assertThrows(IllegalArgumentException.class, () -> new Responsavel("R", "Paula", "83911110000", -1, "senha123"));
    }

    @Test
    @DisplayName("vínculo exige responsável e parentesco")
    void vinculoExigeCampos() {
        Responsavel r = new Responsavel("R", "Paula", "83911110000", 40, "senha123");

        assertThrows(IllegalArgumentException.class, () -> new VinculoResponsavel(null, "Mãe"));
        assertThrows(IllegalArgumentException.class, () -> new VinculoResponsavel(r, " "));
        assertThrows(IllegalArgumentException.class, () -> new VinculoResponsavel(r, null));
        assertEquals("Mãe", new VinculoResponsavel(r, "  Mãe ").getParentesco());
    }
}