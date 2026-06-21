package dao;

import com.mongodb.ConnectionString; //Imports principais do mongoDB driver para Java
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import org.bson.codecs.configuration.CodecRegistry; //Imports auxiliares para trabalhar com os POJO's e etc
import org.bson.codecs.pojo.PojoCodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class BaseMonDao {
    private static MongoClient mongoClient = null;
    protected final MongoDatabase database;

    public BaseMonDao() {
        // Se o cliente ainda não foi criado, inicializa a conexão (Padrão Singleton)
        if (mongoClient == null) {
            // Obtém a URI de conexão das variáveis de ambiente
            String mongoUri = System.getenv("MONGO_URI");
            
            if (mongoUri == null || mongoUri.isEmpty()) {
                // Para caso esqueça de mudar o IPV4 público no json
                throw new IllegalStateException("ERRO: A variável de ambiente MONGO_URI não foi configurada no launch.json!");
            }

            // Configura o Driver para mapear seus POJOs (Curso, Estudante, etc.) automaticamente
            CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
            CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);

            // Aplica as configurações de conexão
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(mongoUri))
                    .codecRegistry(codecRegistry)
                    .build();

            // Cria a instância única do cliente do MongoDB
            mongoClient = MongoClients.create(settings);
            System.out.println("Conexão com o MongoDB na AWS inicializada com sucesso.");
        }
        
        // Define o nome do banco de dados que você vai usar
        this.database = mongoClient.getDatabase("universidade");
    }
}
