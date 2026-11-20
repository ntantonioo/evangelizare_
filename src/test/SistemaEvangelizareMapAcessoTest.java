import service.*;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import model.*;

class SistemaEvangelizareMapAcessoTest {

    static {
        // Deixa o hash de senha rápido nos testes (só afeta esta execução).
        System.setProperty("evangelizare.hash.iteracoes", "1000");
    }

    // Relógio que pode avançar, para testar a validade dos códigos.
    static class RelogioMutavel extends Clock {
        private Instant agora = Instant.parse("2026-11-04T12:00:00Z");

        void avancar(Duration tempo) {
            agora = agora.plus(tempo);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zona) {
            return this;
        }

        @Override
        public Instant instant() {
            return agora;
        }
    }

    private RelogioMutavel relogio;
    private SistemaEvangelizareMap sistema;
    private Administrador admin;
    private Turma infantil, jovem, adulto;

    @BeforeEach
    void setUp() {
        relogio = new RelogioMutavel();
        sistema = new SistemaEvangelizareMap(relogio);

        admin = new Administrador("ADM001", "Administrador", "83999999999", 30, "admin1234");
        sistema.cadastrarAdministradorInicial(admin);

        Catequista maria = new Catequista("CAT001", "Maria Souza", "83988880000", 40, "senha123", "2015");
        Catequista joao = new Catequista("CAT002", "João Pereira", "83988880001", 35, "senha123", "2019");
        Catequista rosa = new Catequista("CAT003", "Rosa Lima", "83988880002", 50, "senha123", "2010");
        sistema.cadastrarCatequista(maria);
        sistema.cadastrarCatequista(joao);
        sistema.cadastrarCatequista(rosa);

        infantil = new Turma("INF-01", TipoTurma.CATEQUESE, maria);
        jovem = new Turma("CJ-01", TipoTurma.CRISMA_JOVEM, joao);
        adulto = new Turma("CA-01", TipoTurma.CRISMA_ADULTO, rosa);
        sistema.cadastrarTurma(infantil);
        sistema.cadastrarTurma(jovem);
        sistema.cadastrarTurma(adulto);

        Responsavel paula = new Responsavel("RESP-PAULA", "Paula Lima", "83977770000", 40, "senha123");
        Catequisando ana = new Catequisando("CQS001", "Ana Clara", "83966660000", 9, "senha123", infantil);
        ana.adicionarResponsavel(paula, "Mãe");
        Catequisando caio = new Catequisando("CQS002", "Caio Souza", "83966660001", 15, "senha123", jovem);
        caio.adicionarResponsavel(paula, "Tia");
        Catequisando dani = new Catequisando("CQS003", "Dani Alves", "83966660002", 30, "senha123", adulto);
        sistema.cadastrarCatequisando(ana);
        sistema.cadastrarCatequisando(caio);
        sistema.cadastrarCatequisando(dani);
    }

    private static Catequista catequistaComLogin(String id, String login, String nome, String senha) {
        Catequista c = new Catequista(id, nome, "83900000000", 30, senha, "2020");
        c.setLogin(login);
        return c;
    }

    private static void assertMensagemContem(IllegalArgumentException e, String trecho) {
        assertTrue(e.getMessage().contains(trecho),
                "Mensagem esperada com \"" + trecho + "\", mas foi: " + e.getMessage());
    }

    @Nested
    @DisplayName("Nome de usuário (login)")
    class Login {

        @Test
        @DisplayName("entra pelo login, sem diferenciar maiúsculas, e não pelo ID interno")
        void entraPeloLogin() {
            sistema.cadastrarCatequista(catequistaComLogin("3f2a9c1e-id-interno", "lucia.reis", "Lúcia Reis", "K7M2-9QXA"));

            assertTrue(sistema.autenticarUsuario("lucia.reis", "K7M2-9QXA"));
            assertTrue(sistema.autenticarUsuario("Lucia.Reis", "K7M2-9QXA"));
            assertTrue(sistema.autenticarUsuario("  lucia.reis ", "K7M2-9QXA"));
            assertFalse(sistema.autenticarUsuario("3f2a9c1e-id-interno", "K7M2-9QXA"), "o ID interno não serve para entrar");
        }

        @Test
        @DisplayName("sem login definido, vale o ID (para quem foi criado de forma simples)")
        void loginPadraoEhOId() {
            assertEquals("CAT001", sistema.pesquisarCatequistaPorId("CAT001").getLogin());
            assertTrue(sistema.autenticarUsuario("CAT001", "senha123"));
        }

        @Test
        @DisplayName("rejeita login curto, longo ou com caracteres que não são permitidos")
        void loginInvalido() {
            for (String invalido : List.of("ab", "com espaço", "acentuação", "a/b/c", "x".repeat(41), "")) {
                IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                        () -> sistema.cadastrarCatequista(catequistaComLogin("ID-" + invalido.hashCode(), invalido, "Nome", "senha123")),
                        "login: \"" + invalido + "\"");
                assertTrue(e.getMessage().contains("nome de usuário") || e.getMessage().contains("ID"), e.getMessage());
            }

            assertTrue(sistema.pesquisarCatequistaPorNome("Nome").isEmpty(), "nenhum cadastro inválido pode ter sido salvo");
        }

        @Test
        @DisplayName("rejeita login repetido, mesmo com letras diferentes, e o primeiro usuário continua intacto")
        void loginRepetido() {
            sistema.cadastrarCatequista(catequistaComLogin("ID-A", "lucia.reis", "Lúcia Reis", "K7M2-9QXA"));

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> sistema.cadastrarCatequista(catequistaComLogin("ID-B", "LUCIA.REIS", "Outra Lúcia", "ZZZZ-2222")));
            assertMensagemContem(e, "nome de usuário");

            assertNull(sistema.pesquisarUsuarioPorId("ID-B"));
            assertTrue(sistema.autenticarUsuario("lucia.reis", "K7M2-9QXA"));
        }

        @Test
        @DisplayName("pesquisa por login ignora maiúsculas e espaços; inexistente ou nulo devolve null")
        void pesquisaPorLogin() {
            assertSame(sistema.pesquisarUsuarioPorId("CAT001"), sistema.pesquisarUsuarioPorLogin(" cat001 "));
            assertNull(sistema.pesquisarUsuarioPorLogin("nao.existe"));
            assertNull(sistema.pesquisarUsuarioPorLogin(null));
        }

        @Test
        @DisplayName("sugere o login a partir do nome, e acrescenta número se já estiver em uso")
        void sugerirLogin() {
            assertEquals("joana.silva", sistema.sugerirLogin("Joana da Silva"));

            sistema.cadastrarCatequista(catequistaComLogin("ID-1", "joana.silva", "Joana da Silva", "K7M2-9QXA"));
            assertEquals("joana.silva2", sistema.sugerirLogin("Joana da Silva"));

            sistema.cadastrarCatequista(catequistaComLogin("ID-2", "joana.silva2", "Joana Silva", "K7M2-9QXB"));
            assertEquals("joana.silva3", sistema.sugerirLogin("JOANA SILVA"));

            assertEquals("usuario", sistema.sugerirLogin("   "));
        }

        @Test
        @DisplayName("pesquisa de usuários e responsáveis por parte do nome, em ordem alfabética")
        void pesquisaPorNome() {
            assertEquals(List.of("Paula Lima"), sistema.pesquisarResponsavelPorNome("paula").stream()
                    .map(Responsavel::getNome).toList());
            assertTrue(sistema.pesquisarResponsavelPorNome("zzz").isEmpty());

            List<String> comLima = sistema.pesquisarUsuarioPorNome("lima").stream().map(Usuario::getNome).toList();
            assertEquals(List.of("Paula Lima", "Rosa Lima"), comLima);
            assertTrue(sistema.pesquisarUsuarioPorNome("").size() >= 8, "texto vazio lista todos");
        }
    }

    @Nested
    @DisplayName("Código de primeiro acesso")
    class Codigo {

        private void cadastrarLucia() {
            sistema.cadastrarCatequista(catequistaComLogin("ID-L", "lucia.reis", "Lúcia Reis", "K7M2-9QXA"));
        }

        @Test
        @DisplayName("vale por 7 dias, e não depois disso")
        void validadeDeSeteDias() {
            cadastrarLucia();

            assertTrue(sistema.autenticarUsuario("lucia.reis", "K7M2-9QXA"));

            relogio.avancar(Duration.ofDays(7));
            assertTrue(sistema.autenticarUsuario("lucia.reis", "K7M2-9QXA"), "no último instante ainda vale");

            relogio.avancar(Duration.ofSeconds(1));
            assertFalse(sistema.autenticarUsuario("lucia.reis", "K7M2-9QXA"), "venceu");
        }

        @Test
        @DisplayName("código vencido não impede o administrador de gerar um novo")
        void redefinirRenova() {
            cadastrarLucia();
            relogio.avancar(Duration.ofDays(10));
            assertFalse(sistema.autenticarUsuario("lucia.reis", "K7M2-9QXA"));

            sistema.redefinirSenha("ID-L", "NOVO-CODE", admin);

            assertTrue(sistema.autenticarUsuario("lucia.reis", "NOVO-CODE"));
            assertFalse(sistema.autenticarUsuario("lucia.reis", "K7M2-9QXA"), "o código antigo foi substituído");

            relogio.avancar(Duration.ofDays(8));
            assertFalse(sistema.autenticarUsuario("lucia.reis", "NOVO-CODE"), "o novo também vence em 7 dias");
        }

        @Test
        @DisplayName("quando a pessoa cria a própria senha, ela nunca mais vence")
        void senhaDefinitivaNaoVence() {
            cadastrarLucia();

            sistema.alterarSenha("ID-L", "K7M2-9QXA", "minhaSenha9");
            assertNull(sistema.pesquisarUsuarioPorLogin("lucia.reis").getProvisoriaValidaAte());

            relogio.avancar(Duration.ofDays(400));
            assertTrue(sistema.autenticarUsuario("lucia.reis", "minhaSenha9"));
        }

        @Test
        @DisplayName("o código aceita letras minúsculas e espaços, mas a senha definitiva não")
        void codigoAceitaMinusculas() {
            cadastrarLucia();

            assertTrue(sistema.autenticarUsuario("lucia.reis", "k7m2-9qxa"));
            assertTrue(sistema.autenticarUsuario("lucia.reis", "  K7M2-9QXA "));

            sistema.alterarSenha("ID-L", "k7m2-9qxa", "MinhaSenha9");

            assertTrue(sistema.autenticarUsuario("lucia.reis", "MinhaSenha9"));
            assertFalse(sistema.autenticarUsuario("lucia.reis", "MINHASENHA9"), "senha definitiva é exata");
            assertFalse(sistema.autenticarUsuario("lucia.reis", "k7m2-9qxa"), "o código não vale mais");
        }

        @Test
        @DisplayName("o administrador inicial, que escolheu a própria senha, nunca tem a senha vencida")
        void administradorInicialNaoVence() {
            relogio.avancar(Duration.ofDays(100));

            assertTrue(sistema.autenticarUsuario("ADM001", "admin1234"));
        }

        @Test
        @DisplayName("cadastrar grava a validade do código")
        void gravaValidade() {
            cadastrarLucia();

            assertEquals(java.time.LocalDateTime.of(2026, 11, 11, 12, 0),
                    sistema.pesquisarUsuarioPorLogin("lucia.reis").getProvisoriaValidaAte());
        }
    }

    @Nested
    @DisplayName("Logins repetidos dentro do mesmo cadastro")
    class LoginsNoMesmoCadastro {

        private Responsavel responsavel(String id, String login, String nome) {
            Responsavel r = new Responsavel(id, nome, "83977770009", 40, "K7M2-9QXA");
            r.setLogin(login);
            return r;
        }

        private Catequisando crianca(String id, String login) {
            Catequisando c = new Catequisando(id, "Criança " + id, "83966660009", 9, "ZZZZ-2222", infantil);
            c.setLogin(login);
            return c;
        }

        @Test
        @DisplayName("dois responsáveis novos com o mesmo login são recusados, e nada é cadastrado")
        void doisResponsaveisMesmoLogin() {
            Catequisando c = crianca("CQS100", "crianca.cem");
            c.adicionarResponsavel(responsavel("R-A", "mae.silva", "Mãe Silva"), "Mãe");
            c.adicionarResponsavel(responsavel("R-B", "MAE.SILVA", "Mãe Silva"), "Tia");

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> sistema.cadastrarCatequisando(c));
            assertMensagemContem(e, "repete");

            assertNull(sistema.pesquisarUsuarioPorId("CQS100"));
            assertNull(sistema.pesquisarUsuarioPorId("R-A"));
            assertNull(sistema.pesquisarUsuarioPorId("R-B"));
        }

        @Test
        @DisplayName("responsável novo com o mesmo login da criança é recusado")
        void mesmoLoginDaCrianca() {
            Catequisando c = crianca("CQS101", "ana.lima");
            c.adicionarResponsavel(responsavel("R-C", "Ana.Lima", "Ana Lima"), "Mãe");

            assertThrows(IllegalArgumentException.class, () -> sistema.cadastrarCatequisando(c));

            assertNull(sistema.pesquisarUsuarioPorId("CQS101"));
            assertNull(sistema.pesquisarUsuarioPorId("R-C"));
        }

        @Test
        @DisplayName("dois responsáveis novos com o mesmo ID são recusados")
        void mesmoId() {
            Catequisando c = crianca("CQS102", "crianca.cento");
            c.adicionarResponsavel(responsavel("R-D", "pai.um", "Pai Um"), "Pai");
            c.adicionarResponsavel(responsavel("r-d", "pai.dois", "Pai Dois"), "Tio");

            assertThrows(IllegalArgumentException.class, () -> sistema.cadastrarCatequisando(c));
            assertNull(sistema.pesquisarUsuarioPorId("R-D"));
        }

        @Test
        @DisplayName("com logins diferentes, o cadastro funciona e todos entram")
        void loginsDiferentesFuncionam() {
            Catequisando c = crianca("CQS103", "crianca.tres");
            c.adicionarResponsavel(responsavel("R-E", "mae.lima", "Mãe Lima"), "Mãe");
            c.adicionarResponsavel(responsavel("R-F", "pai.lima", "Pai Lima"), "Pai");

            sistema.cadastrarCatequisando(c);

            assertTrue(sistema.autenticarUsuario("mae.lima", "K7M2-9QXA"));
            assertTrue(sistema.autenticarUsuario("pai.lima", "K7M2-9QXA"));
            assertFalse(sistema.autenticarUsuario("crianca.tres", "ZZZZ-2222"), "criança da Catequese Infantil não entra");
        }
    }

    @Nested
    @DisplayName("Quem tem acesso próprio")
    class AcessoProprio {

        @Test
        @DisplayName("criança da Catequese Infantil não entra, mesmo com a senha certa")
        void criancaInfantilNaoEntra() {
            assertFalse(sistema.autenticarUsuario("CQS001", "senha123"));
        }

        @Test
        @DisplayName("o responsável da criança entra e acompanha por ela")
        void responsavelEntra() {
            assertTrue(sistema.autenticarUsuario("RESP-PAULA", "senha123"));
        }

        @Test
        @DisplayName("catequisandos da Crisma Jovem e da Crisma Adulto têm acesso próprio")
        void jovemEAdultoEntram() {
            assertTrue(sistema.autenticarUsuario("CQS002", "senha123"));
            assertTrue(sistema.autenticarUsuario("CQS003", "senha123"));
        }

        @Test
        @DisplayName("quando a criança passa para a Crisma Jovem, ganha acesso com um novo código")
        void criancaCresce() {
            sistema.transferirCatequisando("CQS001", "CJ-01");
            sistema.redefinirSenha("CQS001", "CODE-2222", admin);

            assertTrue(sistema.autenticarUsuario("CQS001", "CODE-2222"));
            assertTrue(sistema.pesquisarUsuarioPorId("CQS001").isSenhaProvisoria(), "terá de criar a própria senha");
        }

        @Test
        @DisplayName("quando o jovem volta para a Catequese Infantil, perde o acesso")
        void perdeAcesso() {
            assertTrue(sistema.autenticarUsuario("CQS002", "senha123"));

            sistema.transferirCatequisando("CQS002", "INF-01");

            assertFalse(sistema.autenticarUsuario("CQS002", "senha123"));
        }

        @Test
        @DisplayName("administrador, catequista e responsável não são afetados por essa regra")
        void outrosPerfis() {
            assertTrue(sistema.autenticarUsuario("ADM001", "admin1234"));
            assertTrue(sistema.autenticarUsuario("CAT001", "senha123"));
            assertTrue(sistema.autenticarUsuario("CAT002", "senha123"));
        }
    }
}