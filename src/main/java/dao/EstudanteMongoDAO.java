package dao;

import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import model.Estudante;
import model.Vinculo;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EstudanteMongoDAO extends BaseMonDao {
    //ATTRIBUTES
    private final MongoCollection<Estudante> collection;
    private static final List<String> STATUS_VALIDOS = Arrays.asList("Ativo", "Trancado", "Formado", "Cancelado");

    //CONSTRUCTOR
    public EstudanteMongoDAO() {
        super();
        this.collection = database.getCollection("estudante", Estudante.class);
    }

    //METHODS

    //CREATE
    public void inserir(Estudante estudante) {
        validarDominios(estudante);
        try {
            collection.insertOne(estudante);
            System.out.println("[OK] Estudante inserido: " + estudante.getMat_estudante());
        } catch (MongoWriteException e) {
            if (e.getError().getCode() == 11000) {
                throw new MongoException("Duplicidade: Já existe um estudante com esta matrícula ou CPF.", e);
            }
            throw e;
        }
    }

    //READ
    public Estudante buscarPorMatricula(String matricula) {
        return collection.find(Filters.eq("mat_estudante", matricula)).first();
    }

    public List<Estudante> listarTodos() {
        List<Estudante> lista = new ArrayList<>();
        try (MongoCursor<Estudante> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                lista.add(cursor.next());
            }
        }
        return lista;
    }

    //UPDATE
    public boolean atualizar(String matricula, Estudante estudanteAtualizado) {
        validarDominios(estudanteAtualizado);

        List<Bson> updates = new ArrayList<>();

        if (estudanteAtualizado.getMc() != null) updates.add(Updates.set("MC", estudanteAtualizado.getMc()));
        if (estudanteAtualizado.getAno_ingresso() != null) updates.add(Updates.set("ano_ingresso", estudanteAtualizado.getAno_ingresso()));

        if (estudanteAtualizado.getUsuario() != null) {
            var user = estudanteAtualizado.getUsuario();
            if (user.getNome() != null) updates.add(Updates.set("usuario.nome", user.getNome()));
            if (user.getLogin() != null) updates.add(Updates.set("usuario.login", user.getLogin()));
            if (user.getSenha() != null) updates.add(Updates.set("usuario.senha", user.getSenha()));
            if (user.getEmail() != null) updates.add(Updates.set("usuario.email", user.getEmail()));
            if (user.getTelefone() != null) updates.add(Updates.set("usuario.telefone", user.getTelefone()));
        }

        if (updates.isEmpty()) return false;

        UpdateResult resultado = collection.updateOne(Filters.eq("mat_estudante", matricula), Updates.combine(updates));
        return resultado.getModifiedCount() > 0;
    }

    public boolean adicionarVinculo(String matricula, Vinculo novoVinculo) {
        if (novoVinculo.getStatus() != null && !STATUS_VALIDOS.contains(novoVinculo.getStatus())) {
            throw new IllegalArgumentException("Status de vínculo inválido: " + novoVinculo.getStatus());
        }

        UpdateResult resultado = collection.updateOne(
                Filters.eq("mat_estudante", matricula),
                Updates.push("vinculo", novoVinculo)
        );
        return resultado.getModifiedCount() > 0;
    }

    //DELETE
    public boolean deletar(String matricula) {
        DeleteResult resultado = collection.deleteOne(Filters.eq("mat_estudante", matricula));
        return resultado.getDeletedCount() > 0;
    }

    //Método auxiliar
    private void validarDominios(Estudante estudante) {
        if (estudante == null || estudante.getVinculo() == null) return;
        for (Vinculo v : estudante.getVinculo()) {
            if (v.getStatus() != null && !STATUS_VALIDOS.contains(v.getStatus())) {
                throw new IllegalArgumentException("Status inválido encontrado no vínculo: '" + v.getStatus() + "'. Valores aceitos: " + STATUS_VALIDOS);
            }
        }
    }
}