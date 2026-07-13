package dao;

import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import model.Curso;
import model.Estudante;
import model.Usuario;
import model.Vinculo;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CursoMongoDAO extends BaseMonDao {
    // Domínios válidos — espelham os ENUMs do SQL e o validator JSON da coleção "curso"
    // Usamos List<String> em vez de enum Java porque o PojoCodecProvider (driver >= 4.5)
    // não serializa enums automaticamente e exigiria um codec extra
    private static final List<String> GRAUS_VALIDOS  = Arrays.asList("Bacharelado", "Licenciatura Plena");
    private static final List<String> TURNOS_VALIDOS = Arrays.asList("Matutino", "Vespertino", "Noturno", "Turno Indefinido");
    private static final List<String> NIVEIS_VALIDOS = Arrays.asList("Graduação", "Mestrado", "Doutorado", "Lato");
    // Ponto de entrada principal para as operações CRUD
    private final MongoCollection<Curso> collection;
    private final MongoCollection<Document> estudanteCollection;

    public CursoMongoDAO() {
        super();
        this.collection = database.getCollection("curso", Curso.class);
        this.estudanteCollection = database.getCollection("estudante");
    }

    /**
     * Validações aplicadas antes de chamar o MongoDB (o driver não expõe
     * erros de schema validator):
     *  - Domínios de grau, turno e nivel verificados aqui.
     *  - Unicidade (idCurso, nome+turno+campus+nivel) garantida pelos índices únicos.
     * @throws IllegalArgumentException se algum campo de domínio for inválido.
     * @throws MongoException           em duplicidade de índice único.
     */
    // ===== Método de Inserção (Create) ======
    public void inserir(Curso curso) {
        // Método auxiliar para validação dos enums
        validarDominios(curso);
        // idCurso é gerado aqui, imitando o comportamento do SERIAL no SQL
        curso.setIdCurso(proximoValor("curso"));
        try {
            // Método para inserir um na coleção
            collection.insertOne(curso);
            System.out.println("[OK] Curso inserido: " + curso);
        } catch (MongoWriteException e) {
            if (e.getError().getCode() == 11000) {
                // Código 11000 = duplicate key — violação de índice único
                throw new MongoException(
                        "Duplicidade: já existe um curso com o mesmo idCurso ou com a mesma combinação nome/turno/campus/nivel.", e);
            }
            throw e;
        }
    }

    // ====== Métodos de Leitura (Read) ======   
    // Busca pelo idCurso (PK). Retorna null se não encontrado
    public Curso buscarPorId(int idCurso) {
        return collection.find(Filters.eq("idCurso", idCurso)).first();
    }

    // Busca parcial pelo nome, sem diferenciar maiúsculas/minúsculas
    public List<Curso> buscarPorNome(String nome) {
        List<Curso> resultado = new ArrayList<>();
        // MongoCursor é uma interface que controla a iteração sobre resultados de uma consulta, navegando pelos documentos
        try (MongoCursor<Curso> cursor = collection.find(Filters.regex("nome", nome, "i")).iterator()) {
            while (cursor.hasNext()){
                resultado.add(cursor.next());
            }
        }
        return resultado;
    }

    // Lista todos os cursos.
    public List<Curso> listarTodos() {
        List<Curso> lista = new ArrayList<>();
        try (MongoCursor<Curso> cursor = collection.find().iterator()) {
            while (cursor.hasNext()){
                lista.add(cursor.next());
            }
        }
        return lista;
    }

    // Atualiza os campos não-nulos do Curso identificado por idCurso
    // Campos null no objeto passado são ignorados (patch parcial)
    // ===== Método de Atualizar (Update) ======
    public boolean atualizar(int idCurso, Curso cursoAtualizado) {
        // Método auxiliar
        validarDominios(cursoAtualizado);

        // Lista de Bson para poder dar update nos documentos
        List<Bson> updates = new ArrayList<>();
        if (cursoAtualizado.getNome()   != null) updates.add(Updates.set("nome",   cursoAtualizado.getNome()));
        if (cursoAtualizado.getGrau()   != null) updates.add(Updates.set("grau",   cursoAtualizado.getGrau()));
        if (cursoAtualizado.getTurno()  != null) updates.add(Updates.set("turno",  cursoAtualizado.getTurno()));
        if (cursoAtualizado.getCampus() != null) updates.add(Updates.set("campus", cursoAtualizado.getCampus()));
        if (cursoAtualizado.getNivel()  != null) updates.add(Updates.set("nivel",  cursoAtualizado.getNivel()));

        // Se não foram registrados updates
        if (updates.isEmpty()) {
            System.out.println("[AVISO] Nenhum campo fornecido para atualização.");
            return false;
        }

        // Variável para o resultado o update filtrando por idCurso e realização do updateOne, utilizado para verificação
        // Update.combine combina todos os updates passados anteriormente em uma única operação
        UpdateResult resultado = collection.updateOne(
                Filters.eq("idCurso", idCurso),
                Updates.combine(updates)
        );

        // Checagem se houve documentos modificados
        boolean ok = resultado.getModifiedCount() > 0;
        System.out.println(ok
                ? "[OK] Curso idCurso=" + idCurso + " atualizado."
                : "[AVISO] Nenhum curso encontrado com idCurso=" + idCurso + " ou sem alterações.");
        return ok;
    }

    /**
     * Integridade referencial manual:
     * Antes de remover, percorre os estudantes que têm este idCurso no array "vinculo" e remove o campo idCurso de cada elemento correspondente,
     * espelhando o comportamento ON DELETE SET NULL da FK fk_curso do SQL. Usamos arrayFilters para só tocar o elemento correto
     */
    // ====== Método de Deletar (Delete) ======
    public boolean deletar(int idCurso) {
        // Se não encontrar o idCurso passado
        if (buscarPorId(idCurso) == null) {
            System.out.println("[AVISO] Curso idCurso=" + idCurso + " não encontrado.");
            return false;
        }

        // Política ON DELETE SET NULL nos vínculos dos estudantes
        MongoCollection<Document> estudanteCol = database.getCollection("estudante");

        UpdateOptions opts = new UpdateOptions()
                .arrayFilters(List.of(Filters.eq("elem.idCurso", idCurso))); // Garante que apenas o item correto será excluido

        // Atualização dos vinculos dos estudantes (já que mongodb não garante FK)
        estudanteCol.updateMany(
                Filters.elemMatch("vinculo", Filters.eq("idCurso", idCurso)),
                Updates.unset("vinculo.$[elem].idCurso"), // unset remove especificamente o campo idCurso do vínculo
                opts
        );

        // Operação que de fato deleta o curso
        DeleteResult resultado = collection.deleteOne(Filters.eq("idCurso", idCurso));
        // Verificação
        boolean ok = resultado.getDeletedCount() > 0;
        System.out.println(ok
                ? "[OK] Curso idCurso=" + idCurso + " removido. Vínculos de estudantes atualizados (idCurso set null)."
                : "[ERRO] Falha ao remover curso idCurso=" + idCurso + ".");
        return ok;
    }

    // ====== Método auxiliar ======
    // Valida campos de domínio, campos nulos são ignorados (válidos em updates parciais)
    private void validarDominios(Curso curso) {
        if (curso == null) return;
        if (curso.getGrau()  != null && !GRAUS_VALIDOS.contains(curso.getGrau()))
            throw new IllegalArgumentException("Grau inválido: '" + curso.getGrau() + "'. Valores aceitos: " + GRAUS_VALIDOS);
        if (curso.getTurno() != null && !TURNOS_VALIDOS.contains(curso.getTurno()))
            throw new IllegalArgumentException("Turno inválido: '" + curso.getTurno() + "'. Valores aceitos: " + TURNOS_VALIDOS);
        if (curso.getNivel() != null && !NIVEIS_VALIDOS.contains(curso.getNivel()))
            throw new IllegalArgumentException("Nível inválido: '" + curso.getNivel() + "'. Valores aceitos: " + NIVEIS_VALIDOS);
    }

    public List<Estudante> buscarEstudantesPorCurso(Integer idCurso) {
        List<Estudante> estudantesMatriculados = new ArrayList<>();

        if (estudanteCollection == null || idCurso == null) {
            return estudantesMatriculados;
        }

        try {
            // NOTA: Ajuste "vinculos.codigo_curso" para o nome exato da chave gerada no JSON do seu MongoDB para o model Vinculo.
            var filtro = Filters.eq("vinculo.idCurso", idCurso);

            // Executa a busca e itera sobre os documentos encontrados
            for (Document doc : estudanteCollection.find(filtro)) {

                // Converte o Document do MongoDB de volta para o seu objeto Java (Model Estudante)
                Estudante estudante = converterDocumentParaEstudante(doc);

                if (estudante != null) {
                    estudantesMatriculados.add(estudante);
                }
            }

        } catch (Exception e) {
            System.err.println("Erro ao buscar estudantes vinculados ao curso: " + idCurso);
            e.printStackTrace();
        }
        return estudantesMatriculados;
    }

    private Estudante converterDocumentParaEstudante(Document doc) {
        if (doc == null) return null;

        try {
            Estudante est = new Estudante();
            est.setMat_estudante(doc.getString("mat_estudante"));

            // Tratamento do campo MC guardado como Decimal128 / BigDecimal
            if (doc.get("mc") != null) {
                est.setMc(BigDecimal.valueOf(doc.getInteger("mc")));
            }
            est.setAno_ingresso(doc.getInteger("ano_ingresso"));

            // Converte o Usuário Embutido
            Document docUser = (Document) doc.get("usuario");
            if (docUser != null) {
                Usuario user = new Usuario();
                user.setCpf(docUser.getLong("cpf"));
                user.setNome(docUser.getString("nome"));
                user.setLogin(docUser.getString("login"));

                // Conversão de java.util.Date do Mongo para java.time.LocalDate
                Date dataNasc = docUser.getDate("data_nascimento");
                if (dataNasc != null) {
                    user.setData_nascimento(dataNasc.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                }

                user.setEmail((List<String>) docUser.get("email"));
                user.setTelefone((List<String>) docUser.get("telefone"));
                est.setUsuario(user);
            }

            // Converte a lista de Vínculos Embutidos
            List<Document> docVinculos = (List<Document>) doc.get("vinculo");
            List<Vinculo> vinculos = new ArrayList<>();
            if (docVinculos != null) {
                for (Document vDoc : docVinculos) {
                    Vinculo v = new Vinculo();
                    v.setIdVinculo(vDoc.getInteger("idVinculo"));
                    v.setIdCurso(vDoc.getInteger("idCurso"));
                    v.setStatus(vDoc.getString("status"));

                    Date entrada = vDoc.getDate("data_entrada");
                    if (entrada != null) v.setData_entrada(entrada.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

                    Date saida = vDoc.getDate("data_saida");
                    if (saida != null) v.setData_saida(saida.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

                    vinculos.add(v);
                }
            }
            est.setVinculo(vinculos);

            return est;
        } catch (Exception e) {
            System.err.println("Erro ao mapear documento para Estudante.");
            e.printStackTrace();
            return null;
        }
    }
}
