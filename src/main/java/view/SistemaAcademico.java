package view;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import dao.BaseMonDao;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SistemaAcademico extends Application {

    private BaseMonDao mongoDao;

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/Janela.fxml"));

            Parent root = loader.load();

            Scene scene = new Scene(root, 1050, 650);

            primaryStage.setTitle("Sistema Acadêmico - Ufs");
            primaryStage.setScene(scene);

            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Erro: Não foi possível carregar o arquivo FXML da interface");
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        try {
            BaseMonDao.fecharConexao();
        } catch (Exception e) {
            System.err.println("Erro ao fechar conexões com o banco de dados: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
