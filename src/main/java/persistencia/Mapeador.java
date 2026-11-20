package persistencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.*;

/**
 * Converte os objetos do sistema em linhas de tabela, e as linhas de volta em
 * objetos. As ligações entre objetos (a turma de um catequisando, o autor de
 * uma notícia) viram IDs nas linhas e são refeitas ao carregar, de modo que
 * continuam sendo os mesmos objetos que o sistema usa na memória.
 */
final class Mapeador {

    private Mapeador() {
    }

    static Map<Tabela, Map<String, List<Object>>> linhasVazias() {
        Map<Tabela, Map<String, List<Object>>> linhas = new EnumMap<>(Tabela.class);

        for (Tabela tabela : Tabela.values()) {
            linhas.put(tabela, new LinkedHashMap<>());
        }

        return linhas;
    }

    // ---------- objetos -> linhas ----------

    static Map<Tabela, Map<String, List<Object>>> paraLinhas(Estado estado) {
        Map<Tabela, Map<String, List<Object>>> linhas = linhasVazias();

        for (Usuario usuario : estado.usuarios) {
            adicionar(linhas, Tabela.USUARIO, linhaDe(usuario));

            if (usuario instanceof Catequisando catequisando) {
                int ordem = 0;

                for (VinculoResponsavel vinculo : catequisando.getVinculos()) {
                    adicionar(linhas, Tabela.VINCULO_RESPONSAVEL, linha(
                            catequisando.getId(),
                            vinculo.getResponsavel().getId(),
                            vinculo.getParentesco(),
                            ordem++));
                }
            }
        }

        for (Turma turma : estado.turmas) {
            adicionar(linhas, Tabela.TURMA, linha(turma.getIdTurma(), turma.getTipoTurma().name()));

            int ordem = 0;

            for (Catequista catequista : turma.getCatequistas()) {
                adicionar(linhas, Tabela.TURMA_CATEQUISTA,
                        linha(turma.getIdTurma(), catequista.getId(), ordem++));
            }
        }

        for (Noticia noticia : estado.noticias) {
            adicionar(linhas, Tabela.NOTICIA, linha(
                    noticia.getIdNoticia(),
                    noticia.getTitulo(),
                    noticia.getConteudo(),
                    noticia.getTipoNoticia().name(),
                    noticia.getDataPublicacao().toString(),
                    noticia.getAutor().getId(),
                    noticia.getTurma() == null ? null : noticia.getTurma().getIdTurma()));
        }

        for (Atividade atividade : estado.atividades) {
            adicionar(linhas, Tabela.ATIVIDADE, linha(
                    atividade.getIdAtividade(),
                    atividade.getTitulo(),
                    atividade.getDescricao(),
                    atividade.getDataPublicacao().toString(),
                    atividade.getPrazo() == null ? null : atividade.getPrazo().toString(),
                    atividade.getAutor().getId(),
                    atividade.getTurma().getIdTurma()));
        }

        return linhas;
    }

    private static List<Object> linhaDe(Usuario usuario) {
        String tipo;
        String anoDeIngresso = null;
        String idTurma = null;

        if (usuario instanceof Catequista catequista) {
            tipo = "CATEQUISTA";
            anoDeIngresso = catequista.getAnoDeIngresso();
        } else if (usuario instanceof Catequisando catequisando) {
            tipo = "CATEQUISANDO";
            idTurma = catequisando.getTurma().getIdTurma();
        } else if (usuario instanceof Responsavel) {
            tipo = "RESPONSAVEL";
        } else {
            tipo = "ADMINISTRADOR";
        }

        return linha(
                usuario.getId(),
                tipo,
                usuario.getNome(),
                usuario.getTelefone(),
                usuario.getIdade(),
                usuario.getSenha(),
                usuario.isSenhaProvisoria() ? 1 : 0,
                anoDeIngresso,
                idTurma,
                usuario.getLogin(),
                usuario.getProvisoriaValidaAte() == null ? null : usuario.getProvisoriaValidaAte().toString());
    }

    // Arrays.asList aceita valores nulos (List.of não aceita).
    private static List<Object> linha(Object... valores) {
        return new ArrayList<>(java.util.Arrays.asList(valores));
    }

    private static void adicionar(
            Map<Tabela, Map<String, List<Object>>> linhas, Tabela tabela, List<Object> linha) {
        linhas.get(tabela).put(tabela.chaveDe(linha), linha);
    }

    // ---------- linhas -> objetos ----------

    static Estado paraObjetos(Map<Tabela, Map<String, List<Object>>> linhas) {
        try {
            return reconstruir(linhas);
        } catch (IllegalArgumentException | NullPointerException | ClassCastException
                 | java.time.format.DateTimeParseException e) {
            throw new PersistenciaException(
                    "Os dados do banco estão inconsistentes: " + e.getMessage(), e);
        }
    }

    private static Estado reconstruir(Map<Tabela, Map<String, List<Object>>> linhas) {
        Map<String, Usuario> usuarios = new LinkedHashMap<>();
        Map<String, Turma> turmas = new LinkedHashMap<>();
        List<Noticia> noticias = new ArrayList<>();
        List<Atividade> atividades = new ArrayList<>();

        // 1) quem não depende de ninguém
        for (List<Object> l : linhas.get(Tabela.USUARIO).values()) {
            Usuario usuario = switch (texto(l, 1)) {
                case "ADMINISTRADOR" -> new Administrador(
                        texto(l, 0), texto(l, 2), texto(l, 3), inteiro(l, 4), texto(l, 5));
                case "CATEQUISTA" -> new Catequista(
                        texto(l, 0), texto(l, 2), texto(l, 3), inteiro(l, 4), texto(l, 5), texto(l, 7));
                case "RESPONSAVEL" -> new Responsavel(
                        texto(l, 0), texto(l, 2), texto(l, 3), inteiro(l, 4), texto(l, 5));
                case "CATEQUISANDO" -> null; // depende da turma: vem no passo 3
                default -> throw new IllegalArgumentException("tipo de usuário desconhecido: " + texto(l, 1));
            };

            if (usuario != null) {
                aplicarAcesso(usuario, l);
                usuarios.put(usuario.getId(), usuario);
            }
        }

        // 2) turmas (dependem dos catequistas)
        Map<String, List<List<Object>>> catequistasPorTurma =
                agrupar(linhas.get(Tabela.TURMA_CATEQUISTA).values(), 0, 2);

        for (List<Object> l : linhas.get(Tabela.TURMA).values()) {
            String id = texto(l, 0);
            Turma turma = null;

            for (List<Object> vinculo : catequistasPorTurma.getOrDefault(id, List.of())) {
                if (!(usuarios.get(texto(vinculo, 1)) instanceof Catequista catequista)) {
                    throw new IllegalArgumentException("catequista inexistente: " + texto(vinculo, 1));
                }

                if (turma == null) {
                    turma = new Turma(id, TipoTurma.valueOf(texto(l, 1)), catequista);
                } else {
                    turma.adicionarCatequista(catequista);
                }
            }

            if (turma == null) {
                throw new IllegalArgumentException("turma sem catequista: " + id);
            }

            turmas.put(id, turma);
        }

        // 3) catequisandos (dependem das turmas e dos responsáveis)
        Map<String, List<List<Object>>> vinculosPorCatequisando =
                agrupar(linhas.get(Tabela.VINCULO_RESPONSAVEL).values(), 0, 3);

        for (List<Object> l : linhas.get(Tabela.USUARIO).values()) {
            if (!"CATEQUISANDO".equals(texto(l, 1))) {
                continue;
            }

            Turma turma = turmas.get(texto(l, 8));

            if (turma == null) {
                throw new IllegalArgumentException("turma inexistente: " + texto(l, 8));
            }

            Catequisando catequisando = new Catequisando(
                    texto(l, 0), texto(l, 2), texto(l, 3), inteiro(l, 4), texto(l, 5), turma);
            aplicarAcesso(catequisando, l);

            for (List<Object> vinculo : vinculosPorCatequisando.getOrDefault(texto(l, 0), List.of())) {
                if (!(usuarios.get(texto(vinculo, 1)) instanceof Responsavel responsavel)) {
                    throw new IllegalArgumentException("responsável inexistente: " + texto(vinculo, 1));
                }

                catequisando.adicionarResponsavel(responsavel, texto(vinculo, 2));
            }

            usuarios.put(catequisando.getId(), catequisando);
        }

        // 4) notícias (dependem de usuários e turmas)
        for (List<Object> l : linhas.get(Tabela.NOTICIA).values()) {
            Usuario autor = usuarios.get(texto(l, 5));

            if (autor == null) {
                throw new IllegalArgumentException("autor inexistente: " + texto(l, 5));
            }

            Turma turma = l.get(6) == null ? null : turmas.get(texto(l, 6));

            if (l.get(6) != null && turma == null) {
                throw new IllegalArgumentException("turma inexistente: " + texto(l, 6));
            }

            noticias.add(new Noticia(
                    texto(l, 0), texto(l, 1), texto(l, 2),
                    TipoNoticia.valueOf(texto(l, 3)),
                    autor, turma,
                    LocalDateTime.parse(texto(l, 4))));
        }

        // 5) atividades (dependem de catequistas e turmas)
        for (List<Object> l : linhas.get(Tabela.ATIVIDADE).values()) {
            if (!(usuarios.get(texto(l, 5)) instanceof Catequista autor)) {
                throw new IllegalArgumentException("autor inexistente: " + texto(l, 5));
            }

            Turma turma = turmas.get(texto(l, 6));

            if (turma == null) {
                throw new IllegalArgumentException("turma inexistente: " + texto(l, 6));
            }

            atividades.add(new Atividade(
                    texto(l, 0), texto(l, 1), texto(l, 2),
                    LocalDate.parse(texto(l, 3)),
                    l.get(4) == null ? null : LocalDate.parse(texto(l, 4)),
                    autor, turma));
        }

        return new Estado(
                new ArrayList<>(usuarios.values()),
                new ArrayList<>(turmas.values()),
                noticias,
                atividades);
    }

    // Dados de acesso: senha provisória, validade do código e nome de usuário.
    private static void aplicarAcesso(Usuario usuario, List<Object> l) {
        usuario.setSenhaProvisoria(inteiro(l, 6) == 1);

        if (l.get(10) != null) {
            usuario.setProvisoriaValidaAte(LocalDateTime.parse(texto(l, 10)));
        }

        // Se por algum motivo não houver login gravado, vale o ID.
        usuario.setLogin(l.get(9) == null || texto(l, 9).isBlank() ? usuario.getId() : texto(l, 9));
    }

    // Agrupa linhas pela coluna "porColuna" e ordena cada grupo pela coluna "ordem".
    private static Map<String, List<List<Object>>> agrupar(
            Iterable<List<Object>> linhas, int porColuna, int colunaOrdem) {

        Map<String, List<List<Object>>> grupos = new LinkedHashMap<>();

        for (List<Object> linha : linhas) {
            grupos.computeIfAbsent(texto(linha, porColuna), k -> new ArrayList<>()).add(linha);
        }

        for (List<List<Object>> grupo : grupos.values()) {
            grupo.sort(Comparator.comparingInt(l -> inteiro(l, colunaOrdem)));
        }

        return grupos;
    }

    private static String texto(List<Object> linha, int posicao) {
        return (String) linha.get(posicao);
    }

    private static int inteiro(List<Object> linha, int posicao) {
        return (Integer) linha.get(posicao);
    }
}