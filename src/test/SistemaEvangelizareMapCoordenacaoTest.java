import service.*;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import exception.AtividadeNaoEncontradaException;
import exception.CatequistaNaoEncontradoException;
import exception.NoticiaNaoEncontradaException;
import exception.TurmaNaoEncontradaException;
import model.*;
import service.atividade.AtividadeServiceMap;

class SistemaEvangelizareMapCoordenacaoTest {

    static {
        // Deixa o hash de senha rápido nos testes (só afeta esta execução).
        System.setProperty("evangelizare.hash.iteracoes", "1000");
    }

    private SistemaEvangelizareMap sistema;
    private AtividadeServiceMap atividades;
    private Administrador admin;
    private Catequista antonio, thereza, maria, rosa;
    private Turma jovem, infantil, adulto;
    private Responsavel paula, jose, rita;
    private Catequisando caio, beto, ana;

    @BeforeEach
    void setUp() {
        sistema = new SistemaEvangelizareMap();
        atividades = new AtividadeServiceMap(sistema);

        admin = new Administrador("ADM001", "Administrador", "83999999999", 30, "admin1234");
        sistema.cadastrarAdministradorInicial(admin);

        antonio = new Catequista("CAT001", "Antonio Silva", "83988880001", 45, "senha123", "2012");
        thereza = new Catequista("CAT002", "Thereza Costa", "83988880002", 50, "senha123", "2010");
        maria = new Catequista("CAT003", "Maria Souza", "83988880003", 40, "senha123", "2015");
        rosa = new Catequista("CAT004", "Rosa Lima", "83988880004", 38, "senha123", "2018");
        for (Catequista c : List.of(antonio, thereza, maria, rosa)) {
            sistema.cadastrarCatequista(c);
        }

        jovem = new Turma("CJ-01", TipoTurma.CRISMA_JOVEM, antonio);
        jovem.adicionarCatequista(thereza);
        infantil = new Turma("INF-01", TipoTurma.CATEQUESE, maria);
        adulto = new Turma("CA-01", TipoTurma.CRISMA_ADULTO, rosa);
        for (Turma t : List.of(jovem, infantil, adulto)) {
            sistema.cadastrarTurma(t);
        }

        paula = new Responsavel("RESP-PAULA", "Paula Lima", "83977770001", 40, "senha123");
        jose = new Responsavel("RESP-JOSE", "José Souza", "83977770002", 42, "senha123");
        rita = new Responsavel("RESP-RITA", "Rita Alves", "83977770003", 38, "senha123");

        caio = new Catequisando("CQS001", "Caio Souza", "83966660001", 15, "senha123", jovem);
        caio.adicionarResponsavel(paula, "Tia");
        caio.adicionarResponsavel(jose, "Pai");
        beto = new Catequisando("CQS002", "Beto Alves", "83966660002", 14, "senha123", jovem);
        beto.adicionarResponsavel(rita, "Mãe");
        ana = new Catequisando("CQS003", "Ana Clara", "83966660003", 9, "senha123", infantil);
        ana.adicionarResponsavel(paula, "Mãe");
        for (Catequisando c : List.of(caio, beto, ana)) {
            sistema.cadastrarCatequisando(c);
        }
    }

    private static void assertMensagemContem(IllegalArgumentException e, String trecho) {
        assertTrue(e.getMessage().contains(trecho),
                "Mensagem esperada com \"" + trecho + "\", mas foi: " + e.getMessage());
    }

    private static List<String> nomes(List<? extends Usuario> lista) {
        return lista.stream().map(Usuario::getNome).collect(Collectors.toList());
    }

    private Noticia noticia(String id, Usuario autor, Turma turma) {
        return new Noticia(id, "Título " + id, "Conteúdo " + id, TipoNoticia.values()[0], autor, turma);
    }

    @Nested
    @DisplayName("Catequista coordenador (também administrador)")
    class Coordenador {

        @Test
        @DisplayName("só o Administrador e o catequista coordenador têm permissão de administrador")
        void ehAdministrador() {
            assertTrue(admin.ehAdministrador());
            assertFalse(antonio.ehAdministrador(), "catequista comum não");
            assertFalse(caio.ehAdministrador());
            assertFalse(paula.ehAdministrador());

            sistema.definirCoordenador("CAT001", true, admin);

            assertTrue(antonio.isCoordenador());
            assertTrue(antonio.ehAdministrador());
            assertFalse(thereza.ehAdministrador(), "só quem foi escolhido");
        }

        @Test
        @DisplayName("o coordenador continua catequista: mantém a turma e o mesmo login")
        void continuaCatequista() {
            sistema.definirCoordenador("CAT001", true, admin);

            assertInstanceOf(Catequista.class, sistema.pesquisarUsuarioPorId("CAT001"));
            assertEquals(List.of(antonio, thereza), sistema.pesquisarTurmaPorId("CJ-01").get(0).getCatequistas());
            assertTrue(sistema.autenticarUsuario("CAT001", "senha123"));
        }

        @Test
        @DisplayName("a coordenação pode ser retirada")
        void retirar() {
            sistema.definirCoordenador("CAT001", true, admin);
            sistema.definirCoordenador("CAT001", false, admin);

            assertFalse(antonio.ehAdministrador());
        }

        @Test
        @DisplayName("só um administrador define coordenadores")
        void soAdministradorDefine() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> sistema.definirCoordenador("CAT002", true, antonio));
            assertMensagemContem(e, "Somente o administrador");

            assertThrows(IllegalArgumentException.class, () -> sistema.definirCoordenador("CAT002", true, caio));
            assertThrows(IllegalArgumentException.class, () -> sistema.definirCoordenador("CAT002", true, paula));
            assertThrows(IllegalArgumentException.class, () -> sistema.definirCoordenador("CAT002", true, null));
            assertFalse(thereza.isCoordenador());
        }

        @Test
        @DisplayName("um administrador que não está no sistema não define coordenadores")
        void administradorNaoCadastrado() {
            Administrador copia = new Administrador("ADM001", "Administrador", "83999999999", 30, "x1234567");

            assertThrows(IllegalArgumentException.class, () -> sistema.definirCoordenador("CAT002", true, copia));
            assertFalse(thereza.isCoordenador());
        }

        @Test
        @DisplayName("um coordenador também pode definir outros coordenadores")
        void coordenadorDefineCoordenador() {
            sistema.definirCoordenador("CAT001", true, admin);

            sistema.definirCoordenador("CAT002", true, antonio);

            assertTrue(thereza.isCoordenador());
        }

        @Test
        @DisplayName("catequista inexistente lança exceção")
        void inexistente() {
            assertThrows(CatequistaNaoEncontradoException.class,
                    () -> sistema.definirCoordenador("NAO-EXISTE", true, admin));
        }

        @Test
        @DisplayName("ninguém se torna coordenador no próprio cadastro")
        void naoSePromoveNoCadastro() {
            Catequista espertinho = new Catequista("CAT099", "Espertinho", "83900000000", 30, "senha123", "2024");
            espertinho.setCoordenador(true);

            sistema.cadastrarCatequista(espertinho);

            assertFalse(espertinho.isCoordenador());
            assertFalse(espertinho.ehAdministrador());
        }
    }

    @Nested
    @DisplayName("O que o coordenador pode como administrador")
    class PoderesDoCoordenador {

        @Test
        @DisplayName("publica notícia geral e remove notícias de outros")
        void noticias() {
            assertThrows(IllegalArgumentException.class,
                    () -> sistema.cadastrarNoticia(noticia("N1", antonio, null), antonio),
                    "catequista comum não publica notícia geral");

            sistema.definirCoordenador("CAT001", true, admin);

            sistema.cadastrarNoticia(noticia("N1", antonio, null), antonio);
            sistema.cadastrarNoticia(noticia("N2", maria, infantil), maria);
            sistema.removerNoticia("N2", antonio);

            assertThrows(NoticiaNaoEncontradaException.class, () -> sistema.pesquisarNoticiaPorId("N2"));
            assertNotNull(sistema.pesquisarNoticiaPorId("N1"));
        }

        @Test
        @DisplayName("vê as notícias de todas as turmas, e o catequista comum só as das suas")
        void veTodasAsNoticias() {
            sistema.cadastrarNoticia(noticia("N-INF", maria, infantil), maria);
            sistema.cadastrarNoticia(noticia("N-JOV", antonio, jovem), antonio);

            assertEquals(1, sistema.pesquisarNoticiasDisponiveisPara(thereza).size(), "só a da própria turma");

            sistema.definirCoordenador("CAT002", true, admin);

            assertEquals(2, sistema.pesquisarNoticiasDisponiveisPara(thereza).size(), "agora vê todas");
        }

        @Test
        @DisplayName("redefine a senha de outros usuários")
        void redefineSenha() {
            assertThrows(IllegalArgumentException.class, () -> sistema.redefinirSenha("CAT003", "CODE-1111", antonio));

            sistema.definirCoordenador("CAT001", true, admin);
            sistema.redefinirSenha("CAT003", "CODE-1111", antonio);

            assertTrue(sistema.autenticarUsuario("CAT003", "CODE-1111"));
        }

        @Test
        @DisplayName("vê e remove as atividades de qualquer turma")
        void atividadesDeTodas() {
            atividades.cadastrarAtividade("ATV-INF", "Da infantil", "d", null, "INF-01", maria);
            atividades.cadastrarAtividade("ATV-JOV", "Da jovem", "d", null, "CJ-01", thereza);

            assertEquals(1, atividades.pesquisarAtividadesDisponiveisPara(antonio).size());
            assertThrows(IllegalArgumentException.class, () -> atividades.removerAtividade("ATV-INF", antonio));

            sistema.definirCoordenador("CAT001", true, admin);

            assertEquals(2, atividades.pesquisarAtividadesDisponiveisPara(antonio).size());
            atividades.removerAtividade("ATV-INF", antonio);
            assertThrows(AtividadeNaoEncontradaException.class, () -> atividades.pesquisarAtividadePorId("ATV-INF"));
        }

        @Test
        @DisplayName("continua publicando atividades só para a própria turma, como catequista")
        void atividadesComoCatequista() {
            sistema.definirCoordenador("CAT001", true, admin);

            assertDoesNotThrow(() -> atividades.cadastrarAtividade("ATV-1", "t", "d", null, "CJ-01", antonio));
            assertThrows(IllegalArgumentException.class,
                    () -> atividades.cadastrarAtividade("ATV-2", "t", "d", null, "INF-01", antonio));
        }
    }

    @Nested
    @DisplayName("Último administrador")
    class UltimoAdministrador {

        // Sistema em que a única pessoa com permissão de administrador é um catequista coordenador.
        private SistemaEvangelizareMap soComCoordenador;
        private Catequista coordenador;

        @BeforeEach
        void montar() {
            soComCoordenador = new SistemaEvangelizareMap();
            coordenador = new Catequista("CAT001", "Antonio Silva", "83988880001", 45, "senha123", "2012");
            coordenador.setCoordenador(true);

            soComCoordenador.restaurar(List.of(coordenador), List.of(), List.of());
        }

        @Test
        @DisplayName("existeAdministrador conta o coordenador, então o administrador inicial não pode ser recriado")
        void existeAdministradorContaCoordenador() {
            assertTrue(soComCoordenador.existeAdministrador());

            assertThrows(IllegalArgumentException.class, () -> soComCoordenador.cadastrarAdministradorInicial(
                    new Administrador("ADM001", "Outro", "83911110000", 30, "outra1234")));
        }

        @Test
        @DisplayName("o último administrador não perde a coordenação nem é removido")
        void ultimoNaoSai() {
            IllegalArgumentException e1 = assertThrows(IllegalArgumentException.class,
                    () -> soComCoordenador.definirCoordenador("CAT001", false, coordenador));
            assertMensagemContem(e1, "último administrador");

            IllegalArgumentException e2 = assertThrows(IllegalArgumentException.class,
                    () -> soComCoordenador.removerCatequista("CAT001"));
            assertMensagemContem(e2, "último administrador");

            assertTrue(coordenador.isCoordenador());
            assertNotNull(soComCoordenador.pesquisarCatequistaPorId("CAT001"));
        }

        @Test
        @DisplayName("com outro administrador, a coordenação pode ser retirada e o catequista removido")
        void comOutroAdministrador() {
            Catequista segundo = new Catequista("CAT002", "Thereza Costa", "83988880002", 50, "senha123", "2010");
            segundo.setCoordenador(true);
            soComCoordenador.restaurar(List.of(coordenador, segundo), List.of(), List.of());

            soComCoordenador.definirCoordenador("CAT001", false, segundo);
            assertFalse(coordenador.isCoordenador());

            // agora Thereza é a única: não pode mais ser retirada
            assertThrows(IllegalArgumentException.class, () -> soComCoordenador.definirCoordenador("CAT002", false, segundo));

            soComCoordenador.removerCatequista("CAT001");
            assertThrows(CatequistaNaoEncontradoException.class, () -> soComCoordenador.pesquisarCatequistaPorId("CAT001"));
        }

        @Test
        @DisplayName("catequista comum (sem coordenação) pode ser removido normalmente")
        void catequistaComumRemove() {
            Catequista comum = new Catequista("CAT005", "Lúcia Reis", "83988880005", 30, "senha123", "2020");
            soComCoordenador.restaurar(List.of(coordenador, comum), List.of(), List.of());

            soComCoordenador.removerCatequista("CAT005");

            assertThrows(CatequistaNaoEncontradoException.class, () -> soComCoordenador.pesquisarCatequistaPorId("CAT005"));
        }
    }

    @Nested
    @DisplayName("Resumo da turma")
    class Resumo {

        @Test
        @DisplayName("reúne a turma, os catequistas, os catequisandos e os responsáveis de cada um")
        void resumoCompleto() {
            ResumoDaTurma r = sistema.resumirTurma("CJ-01", admin);

            assertSame(jovem, r.getTurma());
            assertEquals(List.of("Antonio Silva", "Thereza Costa"), nomes(r.getCatequistas()));
            assertEquals(List.of("Beto Alves", "Caio Souza"), nomes(r.getCatequisandos()), "em ordem alfabética");

            Catequisando primeiro = r.getCatequisandos().get(0);
            assertEquals(List.of("Rita Alves"), nomes(primeiro.getResponsaveis()));
            assertEquals("Mãe", primeiro.getParentescoDe("RESP-RITA"));

            Catequisando segundo = r.getCatequisandos().get(1);
            assertEquals(List.of("Paula Lima", "José Souza"), nomes(segundo.getResponsaveis()));
            assertEquals("Tia", segundo.getParentescoDe("RESP-PAULA"));
            assertEquals("Pai", segundo.getParentescoDe("RESP-JOSE"));
        }

        @Test
        @DisplayName("conta os catequisandos e os responsáveis diferentes")
        void totais() {
            ResumoDaTurma r = sistema.resumirTurma("CJ-01", admin);

            assertEquals(2, r.getTotalDeCatequisandos());
            assertEquals(3, r.getTotalDeResponsaveis(), "Paula, José e Rita");
        }

        @Test
        @DisplayName("responsável com dois filhos na mesma turma conta uma vez só")
        void responsavelRepetidoContaUmaVez() {
            Catequisando bia = new Catequisando("CQS004", "Bia Costa", "83966660004", 15, "senha123", jovem);
            bia.adicionarResponsavel(rita, "Mãe");
            sistema.cadastrarCatequisando(bia);

            ResumoDaTurma r = sistema.resumirTurma("CJ-01", admin);

            assertEquals(3, r.getTotalDeCatequisandos());
            assertEquals(3, r.getTotalDeResponsaveis(), "Rita aparece em dois catequisandos, mas é uma pessoa");
        }

        @Test
        @DisplayName("cada turma mostra só os seus próprios catequisandos")
        void cadaTurmaComOsSeus() {
            ResumoDaTurma infantilResumo = sistema.resumirTurma("INF-01", admin);

            assertEquals(List.of("Maria Souza"), nomes(infantilResumo.getCatequistas()));
            assertEquals(List.of("Ana Clara"), nomes(infantilResumo.getCatequisandos()));
            assertEquals(1, infantilResumo.getTotalDeResponsaveis());
        }

        @Test
        @DisplayName("turma sem catequisandos devolve listas vazias")
        void turmaVazia() {
            ResumoDaTurma r = sistema.resumirTurma("CA-01", admin);

            assertEquals(List.of("Rosa Lima"), nomes(r.getCatequistas()));
            assertTrue(r.getCatequisandos().isEmpty());
            assertEquals(0, r.getTotalDeResponsaveis());
        }

        @Test
        @DisplayName("acompanha as mudanças: depois de uma transferência, o resumo se atualiza")
        void acompanhaMudancas() {
            sistema.transferirCatequisando("CQS002", "CA-01");

            assertEquals(List.of("Caio Souza"), nomes(sistema.resumirTurma("CJ-01", admin).getCatequisandos()));
            assertEquals(List.of("Beto Alves"), nomes(sistema.resumirTurma("CA-01", admin).getCatequisandos()));
        }

        @Test
        @DisplayName("as listas devolvidas são cópias: alterá-las não muda o resumo")
        void listasSaoCopias() {
            ResumoDaTurma r = sistema.resumirTurma("CJ-01", admin);

            r.getCatequisandos().clear();
            r.getCatequistas().clear();

            assertEquals(2, r.getCatequisandos().size());
            assertEquals(2, r.getCatequistas().size());
        }

        @Test
        @DisplayName("o coordenador também vê o resumo; catequista comum, catequisando e responsável não")
        void quemPodeVer() {
            assertThrows(IllegalArgumentException.class, () -> sistema.resumirTurma("CJ-01", antonio));

            sistema.definirCoordenador("CAT001", true, admin);
            assertDoesNotThrow(() -> sistema.resumirTurma("CJ-01", antonio));

            assertThrows(IllegalArgumentException.class, () -> sistema.resumirTurma("CJ-01", caio));
            assertThrows(IllegalArgumentException.class, () -> sistema.resumirTurma("CJ-01", paula));
            assertThrows(IllegalArgumentException.class, () -> sistema.resumirTurma("CJ-01", null));
        }

        @Test
        @DisplayName("turma inexistente lança exceção; o ID ignora espaços")
        void turmaInexistente() {
            assertThrows(TurmaNaoEncontradaException.class, () -> sistema.resumirTurma("NAO-EXISTE", admin));
            assertThrows(TurmaNaoEncontradaException.class, () -> sistema.resumirTurma(null, admin));
            assertSame(jovem, sistema.resumirTurma("  CJ-01 ", admin).getTurma());
        }
    }
}