package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import java.io.IOException;

public class MainController {
    //ATTRIBUTES
    @FXML
    private BorderPane mainPane;

    //METHODS
    private void trocarTela(String nomeFxml) {
        try {
            String caminhoCompleto = "/GUI/" + nomeFxml;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(caminhoCompleto));
            Parent novaTela = loader.load();
            mainPane.setCenter(novaTela);
        }
        catch (IOException e) {
            System.err.println("Erro ao carregar a tela: " + nomeFxml);
            e.printStackTrace();
        }
    }

    @FXML
    private void carregarEstudantes() {
        trocarTela("EstudantesView.fxml");
    }

    @FXML
    private void carregarCursos() {
        trocarTela("CursosView.fxml");
    }
}
