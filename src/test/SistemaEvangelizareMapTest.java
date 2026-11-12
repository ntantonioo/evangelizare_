import static org.junit.jupiter.api.Assertions.*;

import service.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import exception.CatequisandoNaoEncontradoException;
import exception.CatequistaNaoEncontradoException;
import exception.NoticiaNaoEncontradaException;
import exception.ResponsavelNaoEncontradoException;
import exception.TurmaNaoEncontradaException;
import model.*;

class SistemaEvangelizareMapTest {

    static {
        // Deixa o hash de senha rápido nos testes (só afeta esta execução).
        System.setProperty("evangelizare.hash.iteracoes", "1000");
    }

    private SistemaEvangelizareMap sistema;
    private Administrador admin;
    private Catequista maria, joao, rosa, pedro;
    private Turma infantil, jovem, adulto;

    @BeforeEach
    void setUp() {
        sistema = new SistemaEvangelizareMap();

        admin = new Administrador("ADM001", "Administrador", "83999999999", 30, "admin123");
        sistema.cadastrarAdministrador(admin);

        maria = novoCatequista("CAT001", "Maria Souza");
        joao = novoCatequista("CAT002", "João Pereira");
        rosa = novoCatequista("CAT003", "Rosa Lima");
        pedro = novoCatequista("CAT004", "Pedro Alves");
        for (Catequista c : List.of(maria, joao, rosa, pedro)) {
            sistema.cadastrarCatequista(c);
        }

        infantil = new Turma("INF-01", TipoTurma.CATEQUESE, maria);
        jovem = new Turma("CJ-01", TipoTurma.CRISMA_JOVEM, joao);
        adulto = new Turma("CA-01", TipoTurma.CRISMA_ADULTO, rosa);
        for (Turma t : List.of(infantil, jovem, adulto)) {
            sistema.cadastrarTurma(t);
        }
    }

    private static Catequista novoCatequista(String id, String nome) {
        return new Catequista(id, nome, "83988880000", 35, "senha123", "2018");
    }

    private static Responsavel novoResponsavel(String id) {
        return new Responsavel(id, "Responsável " + id, "83977770000", 40, "senha123");
    }

    private static Catequisando novoCatequisando(String id, Turma turma, boolean comResponsavel) {
        Catequisando c = new Catequisando(id, "Aluno " + id, "83966660000", 12, "senha123", turma);
        if (comResponsavel) {
            c.adicionarResponsavel(novoResponsavel("RESP-" + id), "Mãe");
        }
        return c;
    }

    private static Noticia novaNoticia(String id, Usuario autor, Turma turma) {
        return new Noticia(id, "Título " + id, "Conteúdo " + id, TipoNoticia.AVISO, autor, turma);
    }

    private static Set<String> ids(List<Noticia> noticias) {
        return noticias.stream().map(Noticia::getIdNoticia).collect(Collectors.toSet());
    }

    private static void assertMensagemContem(IllegalArgumentException e, String trecho) {
        assertTrue(e.getMessage().contains(trecho),
                "Mensagem esperada com \"" + trecho + "\", mas foi: " + e.getMessage());
    }

    @Nested
    @DisplayName("Usuários e autenticação")
    class UsuariosEAutenticacao {

        @Test
        @DisplayName("autentica com ID e senha corretos")
        void autenticaComCredenciaisCorretas() {
            assertTrue(sistema.autenticarUsuario("ADM001", "admin123"));
            assertTrue(sistema.autenticarUsuario("CAT001", "senha123"));
        }

        @Test
        @DisplayName("não autentica com senha errada, nula ou vazia")
        void naoAutenticaComSenhaErrada() {
            assertFalse(sistema.autenticarUsuario("ADM001", "errada"));
            assertFalse(sistema.autenticarUsuario("ADM001", "ADMIN123"));
            assertFalse(sistema.autenticarUsuario("ADM001", ""));
            assertFalse(sistema.autenticarUsuario("ADM001", null));
        }

        @Test
        @DisplayName("não autentica usuário inexistente")
        void naoAutenticaUsuarioInexistente() {
            assertFalse(sistema.autenticarUsuario("NAO-EXISTE", "admin123"));
            assertFalse(sistema.autenticarUsuario(null, "admin123"));
        }

        @Test
        @DisplayName("pesquisarUsuarioPorId devolve o usuário, ou null se não existir")
        void pesquisaUsuarioPorId() {
            assertSame(admin, sistema.pesquisarUsuarioPorId("ADM001"));
            assertSame(maria, sistema.pesquisarUsuarioPorId("CAT001"));
            assertNull(sistema.pesquisarUsuarioPorId("NAO-EXISTE"));
        }

        @Test
        @DisplayName("rejeita ID já usado, mesmo por outro perfil")
        void rejeitaIdDuplicadoEntrePerfis() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> sistema.cadastrarCatequista(novoCatequista("ADM001", "Impostor")));
            assertMensagemContem(e, "Já existe um usuário");

            assertThrows(IllegalArgumentException.class, () -> sistema.cadastrarAdministrador(
                    new Administrador("CAT001", "Outro", "83911110000", 40, "x12345")));
        }

        @Test
        @DisplayName("cadastro duplicado não altera a senha do usuário original")
        void duplicadoNaoAlteraSenhaOriginal() {
            assertThrows(IllegalArgumentException.class, () -> sistema.cadastrarAdministrador(
                    new Administrador("ADM001", "Outro", "83911110000", 40, "novaSenha")));

            assertTrue(sistema.autenticarUsuario("ADM001", "admin123"));
            assertFalse(sistema.autenticarUsuario("ADM001", "novaSenha"));
        }

        @Test
        @DisplayName("rejeita usuário nulo e ID em branco")
        void rejeitaUsuarioNuloEIdEmBranco() {
            assertThrows(IllegalArgumentException.class, () -> sistema.cadastrarAdministrador(null));
            assertThrows(IllegalArgumentException.class, () -> sistema.cadastrarCatequista(null));
            assertThrows(IllegalArgumentException.class, () -> sistema.cadastrarAdministrador(
                    new Administrador("  ", "Sem ID", "83911110000", 40, "x12345")));
        }

        @Test
        @DisplayName("exige nome, telefone, senha e idade positiva")
        void exigeCamposObrigatorios() {
            assertThrows(IllegalArgumentException.class, () -> sistema.cadastrarAdministrador(
                    new Administrador("ADM002", " ", "83911110000", 40, "x12345")), "nome em branco");
            assertThrows(IllegalArgumentException.class, () -> sistema.cadastrarAdministrador(
                    new Administrador("ADM002", "Nome", "", 40, "x12345")), "telefone em branco");
            assertThrows(IllegalArgumentException.class, () -> sistema.cadastrarAdministrador(
                    new Administrador("ADM002", "Nome", "83911110000", 40, "")), "senha em branco");
            assertThrows(IllegalArgumentException.class, () -> sistema.cadastrarAdministrador(
                    new Administrador("ADM002", "Nome", "83911110000", 0, "x12345")), "idade zero");
            assertThrows(IllegalArgumentException.class, () -> sistema.cadastrarAdministrador(
                    new Administrador("ADM002", "Nome", "83911110000", -5, "x12345")), "idade negativa");
            assertNull(sistema.pesquisarUsuarioPorId("ADM002"), "nenhum cadastro inválido pode ter sido salvo");
        }

        @Test
        @DisplayName("um segundo administrador cadastrado consegue entrar")
        void segundoAdministradorConsegueEntrar() {
            sistema.cadastrarAdministrador(
                    new Administrador("ADM002", "Segundo Admin", "83911110000", 41, "outraSenha1"));

            assertTrue(sistema.autenticarUsuario("ADM002", "outraSenha1"));
            assertFalse(sistema.autenticarUsuario("ADM002", "admin123"));
            assertInstanceOf(Administrador.class, sistema.pesquisarUsuarioPorId("ADM002"));
        }
    }

    @Nested
    @DisplayName("Catequistas")
    class Catequistas {

        @Test
        @DisplayName("cadastra e pesquisa por ID")
        void cadastraEPesquisaPorId() {
            assertSame(maria, sistema.pesquisarCatequistaPorId("CAT001"));
        }

        @Test
        @DisplayName("pesquisa por ID inexistente lança CatequistaNaoEncontradoException")
        void pesquisaInexistente() {
            assertThrows(CatequistaNaoEncontradoException.class,
                    () -> sistema.pesquisarCatequistaPorId("NAO-EXISTE"));
        }

        @Test
        @DisplayName("pesquisa por nome ignora maiúsculas e aceita trecho")
        void pesquisaPorNome() {
            List<Catequista> r = sistema.pesquisarCatequistaPorNome("soUZA");
            assertEquals(1, r.size());
            assertSame(maria, r.get(0));
            assertTrue(sistema.pesquisarCatequistaPorNome("zzz").isEmpty());
        }

        @Test
        @DisplayName("exige o ano de ingresso")
        void exigeAnoDeIngresso() {
            assertThrows(IllegalArgumentException.class, () -> sistema.cadastrarCatequista(
                    new Catequista("CAT099", "Sem Ano", "83911110000", 30, "x12345", " ")));
            assertNull(sistema.pesquisarUsuarioPorId("CAT099"));
        }

        @Test
        @DisplayName("atualiza telefone, idade e ano de ingresso")
        void atualiza() {
            sistema.atualizarCatequista("CAT001", "83900001111", 50, "2010");

            Catequista c = sistema.pesquisarCatequistaPorId("CAT001");
            assertEquals("83900001111", c.getTelefone());
            assertEquals(50, c.getIdade());
            assertEquals("2010", c.getAnoDeIngresso());
        }

        @Test
        @DisplayName("atualização inválida não altera nenhum dado")
        void atualizacaoInvalidaNaoAlteraNada() {
            String telefoneAntes = maria.getTelefone();
            int idadeAntes = maria.getIdade();
            String anoAntes = maria.getAnoDeIngresso();

            assertThrows(IllegalArgumentException.class,
                    () -> sistema.atualizarCatequista("CAT001", "83900001111", 0, "2010"));
            assertThrows(IllegalArgumentException.class,
                    () -> sistema.atualizarCatequista("CAT001", "83900001111", 50, " "));

            assertEquals(telefoneAntes, maria.getTelefone(), "o telefone não pode ter mudado");
            assertEquals(idadeAntes, maria.getIdade());
            assertEquals(anoAntes, maria.getAnoDeIngresso());
        }

        @Test
        @DisplayName("atualizar catequista inexistente lança exceção")
        void atualizarInexistente() {
            assertThrows(CatequistaNaoEncontradoException.class,
                    () -> sistema.atualizarCatequista("NAO-EXISTE", "83900001111", 50, "2010"));
        }

        @Test
        @DisplayName("não remove catequista que ainda tem turma")
        void naoRemoveCatequistaComTurma() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> sistema.removerCatequista("CAT001"));
            assertMensagemContem(e, "turmas associadas");
            assertSame(maria, sistema.pesquisarCatequistaPorId("CAT001"));
        }

        @Test
        @DisplayName("remove catequista sem turma, e ele deixa de entrar no sistema")
        void removeCatequistaSemTurma() {
            sistema.removerCatequista("CAT004");

            assertThrows(CatequistaNaoEncontradoException.class,
                    () -> sistema.pesquisarCatequistaPorId("CAT004"));
            assertNull(sistema.pesquisarUsuarioPorId("CAT004"));
            assertFalse(sistema.autenticarUsuario("CAT004", "senha123"));
        }

        @Test
        @DisplayName("depois de remover a turma, o catequista pode ser removido")
        void removeAposRemoverTurma() {
            sistema.removerTurma("INF-01");
            sistema.removerCatequista("CAT001");

            assertNull(sistema.pesquisarUsuarioPorId("CAT001"));
        }

        @Test
        @DisplayName("remover catequista inexistente lança exceção")
        void removerInexistente() {
            assertThrows(CatequistaNaoEncontradoException.class,
                    () -> sistema.removerCatequista("NAO-EXISTE"));
        }
    }

    @Nested
    @DisplayName("Turmas")
    class Turmas {

        @Test
        @DisplayName("cadastra turma com um catequista e pesquisa por ID")
        void cadastraComUmCatequista() {
            Turma t = new Turma("NOVA-01", TipoTurma.CATEQUESE, pedro);
            sistema.cadastrarTurma(t);

            assertSame(t, sistema.pesquisarTurmaPorId("NOVA-01").get(0));
        }

        @Test
        @DisplayName("aceita turma com dois catequistas")
        void cadastraComDoisCatequistas() {
            Catequista extra = novoCatequista("CAT005", "Lúcia Reis");
            sistema.cadastrarCatequista(extra);

            Turma t = new Turma("NOVA-01", TipoTurma.CATEQUESE, pedro);
            t.adicionarCatequista(extra);
            sistema.cadastrarTurma(t);

            assertEquals(2, sistema.pesquisarTurmaPorId("NOVA-01").get(0).getCatequistas().size());
        }

        @Test
        @DisplayName("rejeita turma nula, sem ID, sem tipo ou sem catequista")
        void rejeitaTurmaIncompleta() {
            assertThrows(IllegalArgumentException.class, () -> sistema.cadastrarTurma(null));
            assertThrows(IllegalArgumentException.class,
                    () -> sistema.cadastrarTurma(new Turma(" ", TipoTurma.CATEQUESE, pedro)), "ID em branco");
            assertThrows(IllegalArgumentException.class,
                    () -> sistema.cadastrarTurma(new Turma("NOVA-01", null, pedro)), "sem tipo");
            assertThrows(IllegalArgumentException.class,
                    () -> sistema.cadastrarTurma(new Turma("NOVA-01", TipoTurma.CATEQUESE, null)), "sem catequista");
            assertThrows(TurmaNaoEncontradaException.class, () -> sistema.pesquisarTurmaPorId("NOVA-01"));
        }

        @Test
        @DisplayName("rejeita ID de turma já existente")
        void rejeitaIdDuplicado() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> sistema.cadastrarTurma(new Turma("INF-01", TipoTurma.CATEQUESE, pedro)));
            assertMensagemContem(e, "Já existe uma turma");
        }

        @Test
        @DisplayName("rejeita catequista não cadastrado")
        void rejeitaCatequistaNaoCadastrado() {
            Catequista fantasma = novoCatequista("CAT999", "Fantasma");

            assertThrows(CatequistaNaoEncontradoException.class,
                    () -> sistema.cadastrarTurma(new Turma("NOVA-01", TipoTurma.CATEQUESE, fantasma)));
        }

        @Test
        @DisplayName("um catequista não pode estar em duas turmas")
        void catequistaEmUmaSoTurma() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> sistema.cadastrarTurma(new Turma("NOVA-01", TipoTurma.CATEQUESE, maria)));
            assertMensagemContem(e, "outra turma");
        }

        @Test
        @DisplayName("pesquisa por tipo ignora maiúsculas; tipo sem turma lança exceção")
        void pesquisaPorTipo() {
            assertEquals(List.of(jovem), sistema.pesquisarTurmaPorTipo("crisma_jovem"));
            assertEquals(List.of(infantil), sistema.pesquisarTurmaPorTipo("CATEQUESE"));
            assertThrows(TurmaNaoEncontradaException.class, () -> sistema.pesquisarTurmaPorTipo("INEXISTENTE"));
        }

        @Test
        @DisplayName("pesquisa por catequista (e erro se o catequista não existe)")
        void pesquisaPorCatequista() {
            assertEquals(List.of(infantil), sistema.pesquisarTurmaPorCatequista("CAT001"));
            assertTrue(sistema.pesquisarTurmaPorCatequista("CAT004").isEmpty());
            assertThrows(CatequistaNaoEncontradoException.class,
                    () -> sistema.pesquisarTurmaPorCatequista("NAO-EXISTE"));
        }

        @Test
        @DisplayName("pesquisa por catequisando")
        void pesquisaPorCatequisando() {
            sistema.cadastrarCatequisando(novoCatequisando("CQS001", infantil, true));

            assertEquals(List.of(infantil), sistema.pesquisarTurmaPorCatequisando("CQS001"));
            assertThrows(CatequisandoNaoEncontradoException.class,
                    () -> sistema.pesquisarTurmaPorCatequisando("NAO-EXISTE"));
        }

        @Test
        @DisplayName("pesquisa por ID inexistente lança TurmaNaoEncontradaException")
        void pesquisaInexistente() {
            assertThrows(TurmaNaoEncontradaException.class, () -> sistema.pesquisarTurmaPorId("NAO-EXISTE"));
        }

        @Test
        @DisplayName("remove turma sem catequisandos")
        void removeTurmaVazia() {
            sistema.removerTurma("CA-01");

            assertThrows(TurmaNaoEncontradaException.class, () -> sistema.pesquisarTurmaPorId("CA-01"));
        }

        @Test
        @DisplayName("não remove turma que ainda tem catequisandos")
        void naoRemoveTurmaComCatequisandos() {
            sistema.cadastrarCatequisando(novoCatequisando("CQS001", infantil, true));

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> sistema.removerTurma("INF-01"));
            assertMensagemContem(e, "catequisandos vinculados");
            assertSame(infantil, sistema.pesquisarTurmaPorId("INF-01").get(0));
        }

        @Test
        @DisplayName("remover turma inexistente lança exceção")
        void removerInexistente() {
            assertThrows(TurmaNaoEncontradaException.class, () -> sistema.removerTurma("NAO-EXISTE"));
        }

        @Test
        @DisplayName("edição pode mudar o tipo da turma")
        void editaTipo() {
            sistema.editarTurma(new Turma("INF-01", TipoTurma.CRISMA_ADULTO, maria));

            assertEquals(TipoTurma.CRISMA_ADULTO, sistema.pesquisarTurmaPorId("INF-01").get(0).getTipoTurma());
        }

        @Test
        @DisplayName("edição pode trocar o catequista, liberando o anterior")
        void editaCatequista() {
            sistema.editarTurma(new Turma("INF-01", TipoTurma.CATEQUESE, pedro));

            assertEquals(List.of(pedro), sistema.pesquisarTurmaPorId("INF-01").get(0).getCatequistas());
            assertTrue(sistema.pesquisarTurmaPorCatequista("CAT001").isEmpty());
        }

        @Test
        @DisplayName("edição pode manter um catequista e acrescentar outro")
        void editaAcrescentandoSegundoCatequista() {
            Turma editada = new Turma("INF-01", TipoTurma.CATEQUESE, maria);
            editada.adicionarCatequista(pedro);
            sistema.editarTurma(editada);

            assertEquals(2, sistema.pesquisarTurmaPorId("INF-01").get(0).getCatequistas().size());
        }

        @Test
        @DisplayName("edição rejeita catequista que já está em outra turma")
        void editaRejeitaCatequistaDeOutraTurma() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> sistema.editarTurma(new Turma("INF-01", TipoTurma.CATEQUESE, joao)));
            assertMensagemContem(e, "outra turma");
            assertEquals(List.of(maria), sistema.pesquisarTurmaPorId("INF-01").get(0).getCatequistas());
        }

        @Test
        @DisplayName("edição rejeita turma inexistente, sem tipo ou sem catequista")
        void editaRejeitaDadosInvalidos() {
            assertThrows(TurmaNaoEncontradaException.class,
                    () -> sistema.editarTurma(new Turma("NAO-EXISTE", TipoTurma.CATEQUESE, maria)));
            assertThrows(IllegalArgumentException.class,
                    () -> sistema.editarTurma(new Turma("INF-01", null, maria)));
            assertThrows(IllegalArgumentException.class,
                    () -> sistema.editarTurma(new Turma("INF-01", TipoTurma.CATEQUESE, null)));
            assertThrows(IllegalArgumentException.class, () -> sistema.editarTurma(null));
        }

        @Test
        @DisplayName("não permite mudar para tipo que exige responsável se há catequisando sem responsável")
        void naoMudaTipoSeFaltaResponsavel() {
            sistema.cadastrarCatequisando(novoCatequisando("CQS001", adulto, false));

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> sistema.editarTurma(new Turma("CA-01", TipoTurma.CATEQUESE, rosa)));
            assertMensagemContem(e, "não possui responsável");
            assertEquals(TipoTurma.CRISMA_ADULTO, sistema.pesquisarTurmaPorId("CA-01").get(0).getTipoTurma());
        }
    }

    @Nested
    @DisplayName("Catequisandos")
    class Catequisandos {

        @Test
        @DisplayName("cadastra na Catequese Infantil com responsável")
        void cadastraNaCatequese() {
            Catequisando c = novoCatequisando("CQS001", infantil, true);
            sistema.cadastrarCatequisando(c);

            assertSame(c, sistema.pesquisarCatequisandoPorId("CQS001"));
            assertTrue(sistema.autenticarUsuario("CQS001", "senha123"));
        }

        @Test
        @DisplayName("Catequese Infantil e Crisma Jovem exigem responsável")
        void exigeResponsavelEmInfantilEJovem() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> sistema.cadastrarCatequisando(novoCatequisando("CQS001", infantil, false)));
            assertMensagemContem(e, "exige pelo menos um responsável");

            assertThrows(IllegalArgumentException.class,
                    () -> sistema.cadastrarCatequisando(novoCatequisando("CQS002", jovem, false)));

            assertNull(sistema.pesquisarUsuarioPorId("CQS001"), "cadastro recusado não pode ser salvo");
            assertNull(sistema.pesquisarUsuarioPorId("CQS002"));
        }

        @Test
        @DisplayName("Crisma Adulto não exige responsável")
        void adultoNaoExigeResponsavel() {
            sistema.cadastrarCatequisando(novoCatequisando("CQS001", adulto, false));

            assertTrue(sistema.pesquisarCatequisandoPorId("CQS001").getResponsaveis().isEmpty());
        }

        @Test
        @DisplayName("rejeita turma não cadastrada")
        void rejeitaTurmaNaoCadastrada() {
            Turma fantasma = new Turma("FANTASMA", TipoTurma.CRISMA_ADULTO, pedro);

            assertThrows(TurmaNaoEncontradaException.class,
                    () -> sistema.cadastrarCatequisando(novoCatequisando("CQS001", fantasma, false)));
        }

        @Test
        @DisplayName("rejeita ID já usado por outro usuário")
        void rejeitaIdDuplicado() {
            assertThrows(IllegalArgumentException.class,
                    () -> sistema.cadastrarCatequisando(novoCatequisando("ADM001", adulto, false)));
            assertThrows(IllegalArgumentException.class,
                    () -> sistema.cadastrarCatequisando(null));
        }

        @Test
        @DisplayName("fica ligado à turma registrada no sistema")
        void ligadoATurmaRegistrada() {
            Catequisando c = novoCatequisando("CQS001", infantil, true);
            sistema.cadastrarCatequisando(c);

            assertSame(infantil, c.getTurma());
        }

        @Test
        @DisplayName("pesquisas por ID, nome e turma")
        void pesquisas() {
            Catequisando a = novoCatequisando("CQS001", infantil, true);
            Catequisando b = novoCatequisando("CQS002", infantil, true);
            Catequisando c = novoCatequisando("CQS003", adulto, false);
            sistema.cadastrarCatequisando(a);
            sistema.cadastrarCatequisando(b);
            sistema.cadastrarCatequisando(c);

            assertEquals(2, sistema.pesquisarCatequisandosPorTurma("INF-01").size());
            assertEquals(List.of(c), sistema.pesquisarCatequisandosPorTurma("CA-01"));
            assertTrue(sistema.pesquisarCatequisandosPorTurma("CJ-01").isEmpty());
            assertEquals(List.of(b), sistema.pesquisarCatequisandoPorNome("aluno cqs002"));
            assertThrows(CatequisandoNaoEncontradoException.class,
                    () -> sistema.pesquisarCatequisandoPorId("NAO-EXISTE"));
        }

        @Test
        @DisplayName("atualiza telefone, idade e responsáveis")
        void atualiza() {
            sistema.cadastrarCatequisando(novoCatequisando("CQS001", infantil, true));
            VinculoResponsavel novo = new VinculoResponsavel(novoResponsavel("RESP-JOSE"), "Pai");

            sistema.atualizarCatequisando("CQS001", "83900002222", 13, List.of(novo));

            Catequisando c = sistema.pesquisarCatequisandoPorId("CQS001");
            assertEquals("83900002222", c.getTelefone());
            assertEquals(13, c.getIdade());
            assertEquals(1, c.getResponsaveis().size());
            assertEquals("Responsável RESP-JOSE", c.getResponsaveis().get(0).getNome());
            assertEquals("Pai", c.getParentescoDe("RESP-JOSE"));
        }

        @Test
        @DisplayName("atualização que deixaria turma exigente sem responsável é recusada e nada muda")
        void atualizacaoSemResponsavelNaoAlteraNada() {
            Catequisando c = novoCatequisando("CQS001", infantil, true);
            sistema.cadastrarCatequisando(c);
            String telefoneAntes = c.getTelefone();

            assertThrows(IllegalArgumentException.class,
                    () -> sistema.atualizarCatequisando("CQS001", "83900002222", 13, List.of()));
            assertThrows(IllegalArgumentException.class,
                    () -> sistema.atualizarCatequisando("CQS001", "83900002222", 13, null));

            assertEquals(telefoneAntes, c.getTelefone());
            assertEquals(1, c.getResponsaveis().size());
        }

        @Test
        @DisplayName("atualização com dado inválido não altera nenhum dado")
        void atualizacaoInvalidaNaoAlteraNada() {
            Catequisando c = novoCatequisando("CQS001", infantil, true);
            sistema.cadastrarCatequisando(c);
            String telefoneAntes = c.getTelefone();
            int idadeAntes = c.getIdade();

            assertThrows(IllegalArgumentException.class,
                    () -> sistema.atualizarCatequisando("CQS001", "83900002222", 0, List.of(new VinculoResponsavel(novoResponsavel("RESP-X"), "Mãe"))));
            assertThrows(IllegalArgumentException.class,
                    () -> sistema.atualizarCatequisando("CQS001", " ", 13, List.of(new VinculoResponsavel(novoResponsavel("RESP-X"), "Mãe"))));

            assertEquals(telefoneAntes, c.getTelefone(), "o telefone não pode ter mudado");
            assertEquals(idadeAntes, c.getIdade());
        }

        @Test
        @DisplayName("atualizar catequisando inexistente lança exceção")
        void atualizarInexistente() {
            assertThrows(CatequisandoNaoEncontradoException.class,
                    () -> sistema.atualizarCatequisando("NAO-EXISTE", "83900002222", 13, List.of()));
        }

        @Test
        @DisplayName("remove catequisando, que deixa de entrar no sistema")
        void remove() {
            sistema.cadastrarCatequisando(novoCatequisando("CQS001", infantil, true));

            sistema.removerCatequisando("CQS001");

            assertThrows(CatequisandoNaoEncontradoException.class,
                    () -> sistema.pesquisarCatequisandoPorId("CQS001"));
            assertFalse(sistema.autenticarUsuario("CQS001", "senha123"));
            sistema.removerTurma("INF-01"); // a turma ficou livre
        }

        @Test
        @DisplayName("remover catequisando inexistente lança exceção")
        void removerInexistente() {
            assertThrows(CatequisandoNaoEncontradoException.class,
                    () -> sistema.removerCatequisando("NAO-EXISTE"));
        }

        @Test
        @DisplayName("transfere para outra turma")
        void transfere() {
            sistema.cadastrarCatequisando(novoCatequisando("CQS001", infantil, true));

            sistema.transferirCatequisando("CQS001", "CJ-01");

            assertSame(jovem, sistema.pesquisarCatequisandoPorId("CQS001").getTurma());
            assertTrue(sistema.pesquisarCatequisandosPorTurma("INF-01").isEmpty());
        }

        @Test
        @DisplayName("não transfere para a mesma turma")
        void naoTransferePraMesmaTurma() {
            sistema.cadastrarCatequisando(novoCatequisando("CQS001", infantil, true));

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> sistema.transferirCatequisando("CQS001", "INF-01"));
            assertMensagemContem(e, "já está associado");
        }

        @Test
        @DisplayName("não transfere sem responsável para turma que exige responsável")
        void naoTransfereSemResponsavel() {
            sistema.cadastrarCatequisando(novoCatequisando("CQS001", adulto, false));

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> sistema.transferirCatequisando("CQS001", "INF-01"));
            assertMensagemContem(e, "exige pelo menos um responsável");
            assertSame(adulto, sistema.pesquisarCatequisandoPorId("CQS001").getTurma());
        }

        @Test
        @DisplayName("transferência rejeita IDs em branco, turma ou catequisando inexistentes")
        void transferenciaRejeitaDadosInvalidos() {
            sistema.cadastrarCatequisando(novoCatequisando("CQS001", infantil, true));

            assertThrows(IllegalArgumentException.class, () -> sistema.transferirCatequisando(" ", "CJ-01"));
            assertThrows(IllegalArgumentException.class, () -> sistema.transferirCatequisando("CQS001", null));
            assertThrows(TurmaNaoEncontradaException.class,
                    () -> sistema.transferirCatequisando("CQS001", "NAO-EXISTE"));
            assertThrows(CatequisandoNaoEncontradoException.class,
                    () -> sistema.transferirCatequisando("NAO-EXISTE", "CJ-01"));
        }
    }

    @Nested
    @DisplayName("Responsáveis")
    class Responsaveis {

        private Catequisando filho(String id, Turma turma, Responsavel responsavel, String parentesco) {
            Catequisando c = new Catequisando(id, "Aluno " + id, "83966660000", 12, "senha123", turma);
            c.adicionarResponsavel(responsavel, parentesco);
            return c;
        }

        @Test
        @DisplayName("responsável novo é cadastrado junto com o catequisando e consegue entrar")
        void cadastraJuntoEEntra() {
            Responsavel paula = novoResponsavel("RESP-PAULA");
            sistema.cadastrarCatequisando(filho("CQS001", infantil, paula, "Mãe"));

            assertSame(paula, sistema.pesquisarResponsavelPorId("RESP-PAULA"));
            assertSame(paula, sistema.pesquisarUsuarioPorId("RESP-PAULA"));
            assertTrue(sistema.autenticarUsuario("RESP-PAULA", "senha123"));
            assertFalse(sistema.autenticarUsuario("RESP-PAULA", "outra"));
            assertNotEquals("senha123", paula.getSenha(), "a senha não pode ficar guardada em texto puro");
        }

        @Test
        @DisplayName("o mesmo responsável pode ter vários filhos, com parentescos diferentes")
        void variosFilhos() {
            Responsavel paula = novoResponsavel("RESP-PAULA");
            Catequisando ana = filho("CQS001", infantil, paula, "Mãe");
            Catequisando caio = filho("CQS002", jovem, paula, "Tia");
            sistema.cadastrarCatequisando(ana);
            sistema.cadastrarCatequisando(caio);

            assertEquals(List.of(ana, caio), sistema.pesquisarCatequisandosPorResponsavel("RESP-PAULA"));
            assertEquals("Mãe", ana.getParentescoDe("RESP-PAULA"));
            assertEquals("Tia", caio.getParentescoDe("RESP-PAULA"));
        }

        @Test
        @DisplayName("um catequisando pode ter mais de um responsável")
        void maisDeUmResponsavel() {
            Catequisando ana = filho("CQS001", infantil, novoResponsavel("RESP-PAULA"), "Mãe");
            ana.adicionarResponsavel(novoResponsavel("RESP-JOSE"), "Pai");
            sistema.cadastrarCatequisando(ana);

            assertEquals(2, ana.getResponsaveis().size());
            assertEquals(List.of(ana), sistema.pesquisarCatequisandosPorResponsavel("RESP-JOSE"));
        }

        @Test
        @DisplayName("outro objeto com o ID de um responsável já cadastrado é rejeitado")
        void idDeResponsavelExistenteComOutroObjeto() {
            sistema.cadastrarCatequisando(filho("CQS001", infantil, novoResponsavel("RESP-PAULA"), "Mãe"));
            Catequisando outro = filho("CQS002", infantil, novoResponsavel("RESP-PAULA"), "Mãe");

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> sistema.cadastrarCatequisando(outro));
            assertMensagemContem(e, "já pertence a outro usuário");
            assertNull(sistema.pesquisarUsuarioPorId("CQS002"));
        }

        @Test
        @DisplayName("o ID de um responsável não pode ser o de outro usuário")
        void idDeOutroUsuario() {
            assertThrows(IllegalArgumentException.class,
                    () -> sistema.cadastrarCatequisando(filho("CQS001", infantil, novoResponsavel("CAT001"), "Mãe")));
            assertThrows(IllegalArgumentException.class,
                    () -> sistema.cadastrarCatequisando(filho("CQS002", infantil, novoResponsavel("ADM001"), "Mãe")));

            assertNull(sistema.pesquisarUsuarioPorId("CQS001"));
            assertSame(maria, sistema.pesquisarUsuarioPorId("CAT001"), "o usuário original não pode ser afetado");
            assertTrue(sistema.autenticarUsuario("ADM001", "admin123"));
        }

        @Test
        @DisplayName("responsável novo precisa ser maior de idade")
        void maiorDeIdade() {
            Responsavel menor = new Responsavel("RESP-MENOR", "Menor", "83977770000", 17, "senha123");

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> sistema.cadastrarCatequisando(filho("CQS001", infantil, menor, "Irmão")));
            assertMensagemContem(e, "maior de idade");
            assertNull(sistema.pesquisarUsuarioPorId("CQS001"));
            assertNull(sistema.pesquisarUsuarioPorId("RESP-MENOR"));

            Responsavel dezoito = new Responsavel("RESP-18", "Dezoito", "83977770000", 18, "senha123");
            assertDoesNotThrow(() -> sistema.cadastrarCatequisando(filho("CQS002", infantil, dezoito, "Irmã")));
        }

        @Test
        @DisplayName("cadastro recusado não deixa o responsável cadastrado")
        void cadastroRecusadoNaoCadastraResponsavel() {
            Turma fantasma = new Turma("FANTASMA", TipoTurma.CATEQUESE, pedro);

            assertThrows(TurmaNaoEncontradaException.class, () -> sistema.cadastrarCatequisando(
                    filho("CQS001", fantasma, novoResponsavel("RESP-PAULA"), "Mãe")));

            assertThrows(ResponsavelNaoEncontradoException.class,
                    () -> sistema.pesquisarResponsavelPorId("RESP-PAULA"));
            assertNull(sistema.pesquisarUsuarioPorId("RESP-PAULA"));
        }

        @Test
        @DisplayName("pesquisa por ID ignora espaços; inexistente ou nulo lança exceção")
        void pesquisa() {
            Responsavel paula = novoResponsavel("RESP-PAULA");
            sistema.cadastrarCatequisando(filho("CQS001", infantil, paula, "Mãe"));

            assertSame(paula, sistema.pesquisarResponsavelPorId("  RESP-PAULA "));
            assertThrows(ResponsavelNaoEncontradoException.class, () -> sistema.pesquisarResponsavelPorId("NAO-EXISTE"));
            assertThrows(ResponsavelNaoEncontradoException.class, () -> sistema.pesquisarResponsavelPorId(null));
            assertThrows(ResponsavelNaoEncontradoException.class,
                    () -> sistema.pesquisarResponsavelPorId("CAT001"), "um catequista não é responsável");
        }

        @Test
        @DisplayName("pesquisar filhos de responsável inexistente lança exceção")
        void filhosDeInexistente() {
            assertThrows(ResponsavelNaoEncontradoException.class,
                    () -> sistema.pesquisarCatequisandosPorResponsavel("NAO-EXISTE"));
        }

        @Test
        @DisplayName("remover o catequisando não remove a conta do responsável")
        void removerCatequisandoMantemResponsavel() {
            sistema.cadastrarCatequisando(filho("CQS001", infantil, novoResponsavel("RESP-PAULA"), "Mãe"));

            sistema.removerCatequisando("CQS001");

            assertTrue(sistema.autenticarUsuario("RESP-PAULA", "senha123"));
            assertTrue(sistema.pesquisarCatequisandosPorResponsavel("RESP-PAULA").isEmpty());
        }

        @Test
        @DisplayName("em Crisma Adulto o responsável é opcional, mas se informado também é cadastrado")
        void adultoComResponsavelOpcional() {
            sistema.cadastrarCatequisando(filho("CQS001", adulto, novoResponsavel("RESP-PAULA"), "Mãe"));

            assertTrue(sistema.autenticarUsuario("RESP-PAULA", "senha123"));
        }

        @Test
        @DisplayName("atualizar: troca de responsáveis cadastra os novos e mantém os existentes")
        void atualizaResponsaveis() {
            Responsavel paula = novoResponsavel("RESP-PAULA");
            Catequisando ana = filho("CQS001", infantil, paula, "Mãe");
            sistema.cadastrarCatequisando(ana);
            Responsavel jose = novoResponsavel("RESP-JOSE");

            sistema.atualizarCatequisando("CQS001", "83900002222", 13,
                    List.of(new VinculoResponsavel(paula, "Mãe"), new VinculoResponsavel(jose, "Pai")));

            assertEquals(2, ana.getResponsaveis().size());
            assertSame(jose, sistema.pesquisarResponsavelPorId("RESP-JOSE"));
            assertTrue(sistema.autenticarUsuario("RESP-JOSE", "senha123"));
        }

        @Test
        @DisplayName("atualizar: responsável repetido na lista é rejeitado e nada muda")
        void atualizaComRepetido() {
            Responsavel paula = novoResponsavel("RESP-PAULA");
            Catequisando ana = filho("CQS001", infantil, paula, "Mãe");
            sistema.cadastrarCatequisando(ana);
            String telefoneAntes = ana.getTelefone();
            Responsavel jose = novoResponsavel("RESP-JOSE");

            assertThrows(IllegalArgumentException.class, () -> sistema.atualizarCatequisando("CQS001", "83900002222", 13,
                    List.of(new VinculoResponsavel(jose, "Pai"), new VinculoResponsavel(jose, "Tio"))));

            assertEquals(telefoneAntes, ana.getTelefone());
            assertEquals(List.of(paula), ana.getResponsaveis());
            assertThrows(ResponsavelNaoEncontradoException.class,
                    () -> sistema.pesquisarResponsavelPorId("RESP-JOSE"),
                    "responsável de uma atualização recusada não pode ser cadastrado");
        }

        @Test
        @DisplayName("atualizar: responsável menor de idade é rejeitado e nada muda")
        void atualizaComMenorDeIdade() {
            Responsavel paula = novoResponsavel("RESP-PAULA");
            Catequisando ana = filho("CQS001", infantil, paula, "Mãe");
            sistema.cadastrarCatequisando(ana);
            Responsavel menor = new Responsavel("RESP-MENOR", "Menor", "83977770000", 15, "senha123");

            assertThrows(IllegalArgumentException.class, () -> sistema.atualizarCatequisando("CQS001", "83900002222", 13,
                    List.of(new VinculoResponsavel(menor, "Irmão"))));

            assertEquals(List.of(paula), ana.getResponsaveis());
            assertNull(sistema.pesquisarUsuarioPorId("RESP-MENOR"));
        }
    }

    @Nested
    @DisplayName("Notícias")
    class Noticias {

        @Test
        @DisplayName("administrador publica notícia geral e notícia de turma")
        void adminPublica() {
            sistema.cadastrarNoticia(novaNoticia("N1", admin, null), admin);
            sistema.cadastrarNoticia(novaNoticia("N2", admin, infantil), admin);

            assertNull(sistema.pesquisarNoticiaPorId("N1").getTurma());
            assertSame(infantil, sistema.pesquisarNoticiaPorId("N2").getTurma());
        }

        @Test
        @DisplayName("catequista publica para a própria turma")
        void catequistaPublicaParaSuaTurma() {
            sistema.cadastrarNoticia(novaNoticia("N1", maria, infantil), maria);

            assertSame(maria, sistema.pesquisarNoticiaPorId("N1").getAutor());
        }

        @Test
        @DisplayName("catequista não publica para turma de outro catequista")
        void catequistaNaoPublicaParaTurmaAlheia() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> sistema.cadastrarNoticia(novaNoticia("N1", maria, jovem), maria));
            assertMensagemContem(e, "só pode publicar notícias para sua turma");
        }

        @Test
        @DisplayName("catequista não publica notícia geral")
        void catequistaNaoPublicaGeral() {
            assertThrows(IllegalArgumentException.class,
                    () -> sistema.cadastrarNoticia(novaNoticia("N1", maria, null), maria));
        }

        @Test
        @DisplayName("catequisando não publica notícias")
        void catequisandoNaoPublica() {
            Catequisando c = novoCatequisando("CQS001", infantil, true);
            sistema.cadastrarCatequisando(c);

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> sistema.cadastrarNoticia(novaNoticia("N1", c, infantil), c));
            assertMensagemContem(e, "não tem permissão");
        }

        @Test
        @DisplayName("o autor precisa ser o usuário logado")
        void autorPrecisaSerOLogado() {
            assertThrows(IllegalArgumentException.class,
                    () -> sistema.cadastrarNoticia(novaNoticia("N1", admin, null), maria));
        }

        @Test
        @DisplayName("usuário que não está no sistema não publica")
        void usuarioNaoCadastradoNaoPublica() {
            Administrador intruso = new Administrador("ADM666", "Intruso", "83911110000", 30, "x12345");

            assertThrows(IllegalArgumentException.class,
                    () -> sistema.cadastrarNoticia(novaNoticia("N1", intruso, null), intruso));
            assertThrows(IllegalArgumentException.class,
                    () -> sistema.cadastrarNoticia(novaNoticia("N1", admin, null), null));
        }

        @Test
        @DisplayName("rejeita notícia nula, sem ID, título, conteúdo ou categoria")
        void rejeitaNoticiaIncompleta() {
            assertThrows(IllegalArgumentException.class, () -> sistema.cadastrarNoticia(null, admin));
            assertThrows(IllegalArgumentException.class, () -> sistema.cadastrarNoticia(
                    new Noticia(" ", "Título", "Conteúdo", TipoNoticia.AVISO, admin, null), admin), "ID");
            assertThrows(IllegalArgumentException.class, () -> sistema.cadastrarNoticia(
                    new Noticia("N1", " ", "Conteúdo", TipoNoticia.AVISO, admin, null), admin), "título");
            assertThrows(IllegalArgumentException.class, () -> sistema.cadastrarNoticia(
                    new Noticia("N1", "Título", "", TipoNoticia.AVISO, admin, null), admin), "conteúdo");
            assertThrows(IllegalArgumentException.class, () -> sistema.cadastrarNoticia(
                    new Noticia("N1", "Título", "Conteúdo", null, admin, null), admin), "categoria");
        }

        @Test
        @DisplayName("rejeita ID de notícia já existente")
        void rejeitaIdDuplicado() {
            sistema.cadastrarNoticia(novaNoticia("N1", admin, null), admin);

            assertThrows(IllegalArgumentException.class,
                    () -> sistema.cadastrarNoticia(novaNoticia("N1", admin, null), admin));
        }

        @Test
        @DisplayName("rejeita turma que não é a cadastrada no sistema")
        void rejeitaTurmaNaoCadastrada() {
            Turma fantasma = new Turma("FANTASMA", TipoTurma.CATEQUESE, pedro);
            Turma copiaDeInfantil = new Turma("INF-01", TipoTurma.CATEQUESE, maria);

            assertThrows(IllegalArgumentException.class,
                    () -> sistema.cadastrarNoticia(novaNoticia("N1", admin, fantasma), admin));
            assertThrows(IllegalArgumentException.class,
                    () -> sistema.cadastrarNoticia(novaNoticia("N2", admin, copiaDeInfantil), admin));
        }

        @Test
        @DisplayName("só o administrador remove notícias")
        void somenteAdminRemove() {
            sistema.cadastrarNoticia(novaNoticia("N1", maria, infantil), maria);

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> sistema.removerNoticia("N1", maria));
            assertMensagemContem(e, "Somente o administrador");
            assertNotNull(sistema.pesquisarNoticiaPorId("N1"));

            sistema.removerNoticia("N1", admin);
            assertThrows(NoticiaNaoEncontradaException.class, () -> sistema.pesquisarNoticiaPorId("N1"));
        }

        @Test
        @DisplayName("remover notícia inexistente lança exceção")
        void removerInexistente() {
            assertThrows(NoticiaNaoEncontradaException.class, () -> sistema.removerNoticia("NAO-EXISTE", admin));
        }

        @Test
        @DisplayName("pesquisa por tipo de notícia")
        void pesquisaPorTipo() {
            sistema.cadastrarNoticia(novaNoticia("N1", admin, null), admin);

            assertEquals(Set.of("N1"), ids(sistema.pesquisarNoticiaPorTipo(TipoNoticia.AVISO)));
            assertThrows(NoticiaNaoEncontradaException.class,
                    () -> sistema.pesquisarNoticiaPorTipo(TipoNoticia.EVENTO));
            assertThrows(IllegalArgumentException.class, () -> sistema.pesquisarNoticiaPorTipo(null));
        }

        @Test
        @DisplayName("pesquisa por ID inexistente lança exceção")
        void pesquisaInexistente() {
            assertThrows(NoticiaNaoEncontradaException.class, () -> sistema.pesquisarNoticiaPorId("NAO-EXISTE"));
        }

        @Nested
        @DisplayName("quem vê o quê")
        class Visibilidade {

            private Catequisando alunoInfantil;

            @BeforeEach
            void publicaNoticias() {
                alunoInfantil = novoCatequisando("CQS001", infantil, true);
                sistema.cadastrarCatequisando(alunoInfantil);

                sistema.cadastrarNoticia(novaNoticia("GERAL", admin, null), admin);
                sistema.cadastrarNoticia(novaNoticia("INF", maria, infantil), maria);
                sistema.cadastrarNoticia(novaNoticia("JOV", joao, jovem), joao);
            }

            @Test
            @DisplayName("administrador vê todas")
            void adminVeTodas() {
                assertEquals(Set.of("GERAL", "INF", "JOV"), ids(sistema.pesquisarNoticiasDisponiveisPara(admin)));
            }

            @Test
            @DisplayName("catequista vê as gerais e as das suas turmas")
            void catequistaVeSuasTurmas() {
                assertEquals(Set.of("GERAL", "INF"), ids(sistema.pesquisarNoticiasDisponiveisPara(maria)));
                assertEquals(Set.of("GERAL", "JOV"), ids(sistema.pesquisarNoticiasDisponiveisPara(joao)));
            }

            @Test
            @DisplayName("catequisando vê as gerais e as da própria turma")
            void catequisandoVeSuaTurma() {
                assertEquals(Set.of("GERAL", "INF"), ids(sistema.pesquisarNoticiasDisponiveisPara(alunoInfantil)));
            }

            @Test
            @DisplayName("depois de transferido, passa a ver as notícias da nova turma")
            void transferidoVeNovaTurma() {
                sistema.transferirCatequisando("CQS001", "CJ-01");

                assertEquals(Set.of("GERAL", "JOV"), ids(sistema.pesquisarNoticiasDisponiveisPara(alunoInfantil)));
            }

            @Test
            @DisplayName("sem notícias disponíveis lança exceção")
            void semNoticiasLancaExcecao() {
                sistema.removerNoticia("GERAL", admin);

                // Rosa só vê gerais e as da turma dela; não há nenhuma.
                assertThrows(NoticiaNaoEncontradaException.class,
                        () -> sistema.pesquisarNoticiasDisponiveisPara(rosa));
            }

            @Test
            @DisplayName("exige usuário autenticado no sistema")
            void exigeUsuarioAutenticado() {
                assertThrows(IllegalArgumentException.class, () -> sistema.pesquisarNoticiasDisponiveisPara(null));
                assertThrows(IllegalArgumentException.class, () -> sistema.pesquisarNoticiasDisponiveisPara(
                        new Administrador("ADM666", "Intruso", "83911110000", 30, "x12345")));
            }
        }
    }
}