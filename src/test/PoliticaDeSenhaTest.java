import security.*;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PoliticaDeSenhaTest {

    @Test
    @DisplayName("aceita senha com 8 ou mais caracteres, com letras e números")
    void aceitaSenhaValida() {
        assertDoesNotThrow(() -> PoliticaDeSenha.validar("senha123"));
        assertDoesNotThrow(() -> PoliticaDeSenha.validar("1234abcd"));
        assertDoesNotThrow(() -> PoliticaDeSenha.validar("Catequese2026!"));
        assertDoesNotThrow(() -> PoliticaDeSenha.validar("senhaçã1"), "letras acentuadas contam como letras");
    }

    @Test
    @DisplayName("rejeita senha curta, mesmo com letras e números")
    void rejeitaSenhaCurta() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> PoliticaDeSenha.validar("abc1234"));
        assertTrue(e.getMessage().contains("8 caracteres"));
    }

    @Test
    @DisplayName("rejeita senha só com letras ou só com números")
    void rejeitaSemMistura() {
        IllegalArgumentException soLetras = assertThrows(IllegalArgumentException.class,
                () -> PoliticaDeSenha.validar("abcdefghij"));
        assertTrue(soLetras.getMessage().contains("letras e números"));

        assertThrows(IllegalArgumentException.class, () -> PoliticaDeSenha.validar("1234567890"));
    }

    @Test
    @DisplayName("rejeita senha nula, vazia ou só com espaços")
    void rejeitaNulaOuVazia() {
        assertThrows(IllegalArgumentException.class, () -> PoliticaDeSenha.validar(null));
        assertThrows(IllegalArgumentException.class, () -> PoliticaDeSenha.validar(""));
        assertThrows(IllegalArgumentException.class, () -> PoliticaDeSenha.validar("          "));
    }

    @Test
    @DisplayName("a descrição cita o tamanho mínimo")
    void descricao() {
        assertTrue(PoliticaDeSenha.DESCRICAO.contains("8"));
    }
}