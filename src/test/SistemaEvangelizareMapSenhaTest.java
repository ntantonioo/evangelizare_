import service.*;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import model.*;

class SistemaEvangelizareMapSenhaTest {

    static {
        // Deixa o hash de senha rápido nos testes (só afeta esta execução).
        System.setProperty("evangelizare.hash.iteracoes", "1000");
    }

    private SistemaEvangelizareMap sistema;
    private Administrador admin;
    private Catequista maria;
    private Catequisando ana;
    private Responsavel paula;

    @BeforeEach
    void setUp() {
        sistema = new SistemaEvangelizareMap();

        admin = new Administrador("ADM001", "Administrador", "83999999999", 30, "admin1234");
        sistema.cadastrarAdministradorInicial(admin);

        maria = new Catequista("CAT001", "Maria Souza", "83988880000", 40, "senha123", "2015");
        sistema.cadastrarCatequista(maria);

        Turma infantil = new Turma("INF-01", TipoTurma.CATEQUESE, maria);
        sistema.cadastrarTurma(infantil);

        paula = new Responsavel("RESP-PAULA", "Paula Lima", "83977770000", 40, "senha123");
        ana = new Catequisando("CQS001", "Ana Clara", "83966660000", 9, "senha123", infantil);
        ana.adicionarResponsavel(paula, "Mãe");
        sistema.cadastrarCatequisando(ana);
    }

    private static void assertMensagemContem(IllegalArgumentException e, String trecho) {
        assertTrue(e.getMessage().contains(trecho),
                "Mensagem esperada com \"" + trecho + "\", mas foi: " + e.getMessage());
    }

    @Nested
    @DisplayName("Primeiro acesso")
    class PrimeiroAcesso {

        @Test
        @DisplayName("um sistema novo não tem administrador; depois do primeiro, passa a ter")
        void existeAdministrador() {
            assertFalse(new SistemaEvangelizareMap().existeAdministrador());
            assertTrue(sistema.existeAdministrador());
        }

        @Test
        @DisplayName("cria o administrador inicial, que entra com a própria senha e não é provisória")
        void criaAdministradorInicial() {
            assertTrue(sistema.autenticarUsuario("ADM001", "admin1234"));
            assertFalse(admin.isSenhaProvisoria(), "a senha foi escolhida por ele mesmo");
            assertNotEquals("admin1234", admin.getSenha(), "a senha não pode ficar em texto puro");
        }

        @Test
        @DisplayName("só pode ser criado enquanto não existe nenhum administrador")
        void soUmaVez() {
            Administrador outro = new Administrador("ADM002", "Outro", "83911110000", 31, "outra1234");

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> sistema.cadastrarAdministradorInicial(outro));
            assertMensagemContem(e, "só pode ser criado quando ainda não há administradores");
            assertNull(sistema.pesquisarUsuarioPorId("ADM002"));
        }

        @Test
        @DisplayName("exige senha dentro da política, e nada é criado se ela não atender")
        void exigePoliticaDeSenha() {
            SistemaEvangelizareMap novo = new SistemaEvangelizareMap();

            assertThrows(IllegalArgumentException.class, () -> novo.cadastrarAdministradorInicial(
                    new Administrador("ADM001", "Admin", "83999999999", 30, "curta1")));
            assertThrows(IllegalArgumentException.class, () -> novo.cadastrarAdministradorInicial(
                    new Administrador("ADM001", "Admin", "83999999999", 30, "somenteletras")));
            assertThrows(IllegalArgumentException.class, () -> novo.cadastrarAdministradorInicial(null));

            assertFalse(novo.existeAdministrador());
            assertNull(novo.pesquisarUsuarioPorId("ADM001"));
        }

        @Test
        @DisplayName("ainda exige os demais campos do administrador")
        void exigeDemaisCampos() {
            SistemaEvangelizareMap novo = new SistemaEvangelizareMap();

            assertThrows(IllegalArgumentException.class, () -> novo.cadastrarAdministradorInicial(
                    new Administrador("ADM001", " ", "83999999999", 30, "admin1234")));
            assertThrows(IllegalArgumentException.class, () -> novo.cadastrarAdministradorInicial(
                    new Administrador("ADM001", "Admin", "83999999999", 0, "admin1234")));
            assertFalse(novo.existeAdministrador());
        }
    }

    @Nested
    @DisplayName("Senha provisória")
    class SenhaProvisoria {

        @Test
        @DisplayName("quem é cadastrado por outra pessoa começa com senha provisória")
        void cadastradosPorOutraPessoa() {
            assertTrue(maria.isSenhaProvisoria(), "catequista cadastrado pelo administrador");
            assertTrue(ana.isSenhaProvisoria(), "catequisando cadastrado pelo catequista");
            assertTrue(paula.isSenhaProvisoria(), "responsável cadastrado junto com o catequisando");

            Administrador segundo = new Administrador("ADM002", "Segundo", "83911110000", 31, "temporaria1");
            sistema.cadastrarAdministrador(segundo);
            assertTrue(segundo.isSenhaProvisoria(), "administrador cadastrado por outro administrador");
        }

        @Test
        @DisplayName("responsável já existente, ligado a outro filho, não volta a ser provisório")
        void responsavelExistenteMantemEstado() {
            sistema.alterarSenha("RESP-PAULA", "senha123", "paulaNova1");
            assertFalse(paula.isSenhaProvisoria());

            Catequisando bia = new Catequisando("CQS002", "Bia", "83966660001", 8, "senha123", ana.getTurma());
            bia.adicionarResponsavel(paula, "Mãe");
            sistema.cadastrarCatequisando(bia);

            assertFalse(paula.isSenhaProvisoria(), "a senha dela já era dela");
            assertTrue(sistema.autenticarUsuario("RESP-PAULA", "paulaNova1"));
        }
    }

    @Nested
    @DisplayName("Alterar a própria senha")
    class AlterarSenha {

        @Test
        @DisplayName("troca a senha: a antiga deixa de valer, a nova passa a valer, e deixa de ser provisória")
        void trocaComSucesso() {
            sistema.alterarSenha("CAT001", "senha123", "novaSenha1");

            assertFalse(sistema.autenticarUsuario("CAT001", "senha123"));
            assertTrue(sistema.autenticarUsuario("CAT001", "novaSenha1"));
            assertFalse(maria.isSenhaProvisoria());
            assertFalse(maria.getSenha().contains("novaSenha1"), "a nova senha também é guardada como hash");
        }

        @Test
        @DisplayName("funciona para todos os perfis")
        void todosOsPerfis() {
            sistema.alterarSenha("ADM001", "admin1234", "adminNova1");
            sistema.alterarSenha("CQS001", "senha123", "anaNova123");
            sistema.alterarSenha("RESP-PAULA", "senha123", "paulaNova1");

            assertTrue(sistema.autenticarUsuario("ADM001", "adminNova1"));
            assertTrue(sistema.autenticarUsuario("CQS001", "anaNova123"));
            assertTrue(sistema.autenticarUsuario("RESP-PAULA", "paulaNova1"));
        }

        @Test
        @DisplayName("senha atual errada é rejeitada e nada muda")
        void senhaAtualErrada() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> sistema.alterarSenha("CAT001", "errada123", "novaSenha1"));
            assertMensagemContem(e, "senha atual está incorreta");

            assertTrue(sistema.autenticarUsuario("CAT001", "senha123"));
            assertFalse(sistema.autenticarUsuario("CAT001", "novaSenha1"));
            assertTrue(maria.isSenhaProvisoria(), "continua provisória");
        }

        @Test
        @DisplayName("senha atual nula ou vazia é rejeitada")
        void senhaAtualNulaOuVazia() {
            assertThrows(IllegalArgumentException.class, () -> sistema.alterarSenha("CAT001", null, "novaSenha1"));
            assertThrows(IllegalArgumentException.class, () -> sistema.alterarSenha("CAT001", "", "novaSenha1"));
            assertTrue(sistema.autenticarUsuario("CAT001", "senha123"));
        }

        @Test
        @DisplayName("nova senha fora da política é rejeitada e nada muda")
        void novaSenhaFraca() {
            assertThrows(IllegalArgumentException.class, () -> sistema.alterarSenha("CAT001", "senha123", "curta1"));
            assertThrows(IllegalArgumentException.class, () -> sistema.alterarSenha("CAT001", "senha123", "somenteletras"));
            assertThrows(IllegalArgumentException.class, () -> sistema.alterarSenha("CAT001", "senha123", null));

            assertTrue(sistema.autenticarUsuario("CAT001", "senha123"));
            assertTrue(maria.isSenhaProvisoria());
        }

        @Test
        @DisplayName("a nova senha precisa ser diferente da atual")
        void novaIgualAtual() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> sistema.alterarSenha("CAT001", "senha123", "senha123"));
            assertMensagemContem(e, "diferente da atual");
            assertTrue(maria.isSenhaProvisoria(), "continua provisória, pois nada mudou");
        }

        @Test
        @DisplayName("usuário inexistente, nulo ou em branco é rejeitado")
        void usuarioInexistente() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> sistema.alterarSenha("NAO-EXISTE", "senha123", "novaSenha1"));
            assertMensagemContem(e, "Usuário não encontrado");
            assertThrows(IllegalArgumentException.class, () -> sistema.alterarSenha(null, "senha123", "novaSenha1"));
            assertThrows(IllegalArgumentException.class, () -> sistema.alterarSenha("  ", "senha123", "novaSenha1"));
        }

        @Test
        @DisplayName("não afeta a senha de outros usuários, mesmo com a mesma senha")
        void naoAfetaOutros() {
            sistema.alterarSenha("CAT001", "senha123", "novaSenha1");

            assertTrue(sistema.autenticarUsuario("CQS001", "senha123"), "Ana tinha a mesma senha e continua com ela");
            assertTrue(sistema.autenticarUsuario("RESP-PAULA", "senha123"));
            assertTrue(sistema.autenticarUsuario("ADM001", "admin1234"));
        }
    }

    @Nested
    @DisplayName("Redefinir senha (administrador)")
    class RedefinirSenha {

        @Test
        @DisplayName("o administrador redefine: a antiga deixa de valer e a nova é provisória")
        void redefine() {
            sistema.alterarSenha("CAT001", "senha123", "minhaSenha9");
            assertFalse(maria.isSenhaProvisoria());

            sistema.redefinirSenha("CAT001", "temporaria", admin);

            assertFalse(sistema.autenticarUsuario("CAT001", "minhaSenha9"));
            assertTrue(sistema.autenticarUsuario("CAT001", "temporaria"));
            assertTrue(maria.isSenhaProvisoria());
            assertFalse(maria.getSenha().contains("temporaria"));
        }

        @Test
        @DisplayName("a senha provisória não precisa seguir a política, mas deve ser trocada por uma que siga")
        void provisoriaDepoisTroca() {
            sistema.redefinirSenha("CAT001", "abc", admin);
            assertTrue(sistema.autenticarUsuario("CAT001", "abc"));

            assertThrows(IllegalArgumentException.class, () -> sistema.alterarSenha("CAT001", "abc", "fraca"));
            sistema.alterarSenha("CAT001", "abc", "agoraForte1");

            assertTrue(sistema.autenticarUsuario("CAT001", "agoraForte1"));
            assertFalse(maria.isSenhaProvisoria());
        }

        @Test
        @DisplayName("funciona para catequisando e responsável")
        void outrosPerfis() {
            sistema.redefinirSenha("CQS001", "temporaria", admin);
            sistema.redefinirSenha("RESP-PAULA", "temporaria", admin);

            assertTrue(sistema.autenticarUsuario("CQS001", "temporaria"));
            assertTrue(sistema.autenticarUsuario("RESP-PAULA", "temporaria"));
        }

        @Test
        @DisplayName("só o administrador redefine senhas")
        void soAdministrador() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> sistema.redefinirSenha("RESP-PAULA", "temporaria", maria));
            assertMensagemContem(e, "Somente o administrador");
            assertThrows(IllegalArgumentException.class, () -> sistema.redefinirSenha("CAT001", "temporaria", ana));
            assertThrows(IllegalArgumentException.class, () -> sistema.redefinirSenha("CAT001", "temporaria", paula));
            assertThrows(IllegalArgumentException.class, () -> sistema.redefinirSenha("CAT001", "temporaria", null));

            assertTrue(sistema.autenticarUsuario("RESP-PAULA", "senha123"), "nada mudou");
            assertTrue(sistema.autenticarUsuario("CAT001", "senha123"));
        }

        @Test
        @DisplayName("um administrador que não está no sistema não redefine")
        void administradorNaoCadastrado() {
            Administrador intruso = new Administrador("ADM666", "Intruso", "83911110000", 30, "intruso123");
            Administrador copiaDoAdmin = new Administrador("ADM001", "Administrador", "83999999999", 30, "admin1234");

            assertThrows(IllegalArgumentException.class, () -> sistema.redefinirSenha("CAT001", "temporaria", intruso));
            assertThrows(IllegalArgumentException.class, () -> sistema.redefinirSenha("CAT001", "temporaria", copiaDoAdmin),
                    "um objeto igual, mas que não é o cadastrado, não vale");
            assertTrue(sistema.autenticarUsuario("CAT001", "senha123"));
        }

        @Test
        @DisplayName("usuário inexistente ou senha provisória vazia é rejeitado")
        void dadosInvalidos() {
            assertThrows(IllegalArgumentException.class, () -> sistema.redefinirSenha("NAO-EXISTE", "temporaria", admin));
            assertThrows(IllegalArgumentException.class, () -> sistema.redefinirSenha(null, "temporaria", admin));
            assertThrows(IllegalArgumentException.class, () -> sistema.redefinirSenha("CAT001", "", admin));
            assertThrows(IllegalArgumentException.class, () -> sistema.redefinirSenha("CAT001", "   ", admin));
            assertThrows(IllegalArgumentException.class, () -> sistema.redefinirSenha("CAT001", null, admin));
            assertTrue(sistema.autenticarUsuario("CAT001", "senha123"), "nada mudou");
        }

        @Test
        @DisplayName("o administrador não redefine a própria senha: deve usar Alterar minha senha")
        void naoRedefineAPropria() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> sistema.redefinirSenha("ADM001", "temporaria", admin));
            assertMensagemContem(e, "Alterar minha senha");
            assertTrue(sistema.autenticarUsuario("ADM001", "admin1234"));
            assertFalse(admin.isSenhaProvisoria());
        }
    }
}