import security.*;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GeradorDeCredenciaisTest {

    @Test
    @DisplayName("o ID gerado é um código único, que ninguém digita nem adivinha")
    void novoId() {
        String id = GeradorDeCredenciais.novoId();

        assertDoesNotThrow(() -> UUID.fromString(id));
        assertNotEquals(id, GeradorDeCredenciais.novoId());
    }

    @Test
    @DisplayName("o código de primeiro acesso tem o formato XXXX-XXXX, sem caracteres que se confundem")
    void formatoDoCodigo() {
        for (int i = 0; i < 500; i++) {
            String codigo = GeradorDeCredenciais.novoCodigo();

            assertTrue(codigo.matches("[A-HJKMNP-Z2-9]{4}-[A-HJKMNP-Z2-9]{4}"), codigo);
            assertFalse(codigo.matches(".*[01OIL].*"), "sem 0, 1, O, I nem L: " + codigo);
        }
    }

    @Test
    @DisplayName("os códigos gerados não se repetem")
    void codigosDiferentes() {
        Set<String> vistos = new HashSet<>();

        for (int i = 0; i < 2000; i++) {
            assertTrue(vistos.add(GeradorDeCredenciais.novoCodigo()));
        }
    }

    @Test
    @DisplayName("o código tem 9 caracteres: 8 letras e números e um hífen no meio")
    void tamanhoDoCodigo() {
        assertEquals(9, GeradorDeCredenciais.novoCodigo().length());
    }

    @Test
    @DisplayName("login: primeiro e último nome, sem acentos e em minúsculas")
    void loginBasico() {
        assertEquals("joao.pereira", GeradorDeCredenciais.baseDeLogin("João da Silva Pereira"));
        assertEquals("maria.souza", GeradorDeCredenciais.baseDeLogin("Maria Souza"));
        assertEquals("angela.conceicao", GeradorDeCredenciais.baseDeLogin("Ângela Conceição"));
        assertEquals("ana.lima", GeradorDeCredenciais.baseDeLogin("  ANA   de   LIMA  "));
    }

    @Test
    @DisplayName("login: nome de uma palavra só, e pontuação é descartada")
    void loginPalavraUnica() {
        assertEquals("madalena", GeradorDeCredenciais.baseDeLogin("Madalena"));
        assertEquals("davila.souza", GeradorDeCredenciais.baseDeLogin("D'Ávila Souza"));
        assertEquals("davilasouza", GeradorDeCredenciais.baseDeLogin("D'Ávila-Souza"), "uma palavra só, sem espaço");
    }

    @Test
    @DisplayName("login: nomes vazios ou curtos demais ainda geram um login válido")
    void loginCasosLimite() {
        assertEquals("usuario", GeradorDeCredenciais.baseDeLogin(null));
        assertEquals("usuario", GeradorDeCredenciais.baseDeLogin("   "));
        assertEquals("usuario", GeradorDeCredenciais.baseDeLogin("de da do"));
        assertEquals("li0", GeradorDeCredenciais.baseDeLogin("Li"));
        assertTrue(GeradorDeCredenciais.baseDeLogin("A".repeat(100)).length() <= 40);
        assertTrue(GeradorDeCredenciais.baseDeLogin("José " + "Silvestre".repeat(10)).matches("[a-z0-9.]{3,40}"));
    }
}