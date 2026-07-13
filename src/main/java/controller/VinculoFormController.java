package controller;

import dao.EstudanteMongoDAO;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.Curso;
import model.Vinculo;

import java.time.LocalDate;
import java.util.List;

public class VinculoFormController {

    // --- Elementos FXML ---
    @FXML private ComboBox<Curso> cbCurso;
    @FXML private DatePicker dpDataEntrada;
    @FXML private ComboBox<String> cbStatus;
    @FXML private DatePicker dpDataSaida;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;

    private String matriculaEstudante;
    private EstudanteMongoDAO estDao;

    @FXML
    public void initialize() {
        estDao = new EstudanteMongoDAO();

        // Precisa espelhar o enum "status" do validator de estudante.json (vinculo.status)
        cbStatus.setItems(FXCollections.observableArrayList("Ativo", "Cancelada", "Formando", "Graduado"));
        dpDataEntrada.setValue(LocalDate.now());

        cbCurso.setConverter(new StringConverter<>() {
            @Override
            public String toString(Curso curso) {
                return curso == null ? "" : curso.getNome() + " (ID " + curso.getIdCurso() + ")";
            }

            @Override
            public Curso fromString(String s) {
                return null; // não é usado: o ComboBox não é editável
            }
        });

        btnSalvar.setOnAction(event -> handleSalvar());
        btnCancelar.setOnAction(event -> handleCancelar());
    }

    /**
     * Recebe a matrícula do estudante selecionado na tela principal e a lista de
     * cursos já carregada, para popular o ComboBox sem precisar buscar de novo no banco.
     */
    public void setContexto(String matriculaEstudante, List<Curso> cursosDisponiveis) {
        this.matriculaEstudante = matriculaEstudante;
        cbCurso.setItems(FXCollections.observableArrayList(cursosDisponiveis));
    }

    // --- Métodos de Ação ---

    @FXML
    private void handleSalvar() {
        if (!validarCampos()) {
            return;
        }

        Curso cursoSelecionado = cbCurso.getValue();
        Integer idCurso = cursoSelecionado != null ? cursoSelecionado.getIdCurso() : null;
        LocalDate dataEntrada = dpDataEntrada.getValue();
        String status = cbStatus.getValue();
        LocalDate dataSaida = dpDataSaida.getValue();

        // idVinculo NÃO é definido aqui: o DAO gera automaticamente (equivalente ao SERIAL do SQL)
        Vinculo novoVinculo = new Vinculo(idCurso, dataEntrada, status, dataSaida);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                estDao.adicionarVinculo(matriculaEstudante, novoVinculo);
                return null;
            }
        };

        btnSalvar.setDisable(true);
        btnCancelar.setDisable(true);

        task.setOnSucceeded(e -> fecharJanela());
        task.setOnFailed(e -> {
            btnSalvar.setDisable(false);
            btnCancelar.setDisable(false);
            Throwable erro = task.getException();
            erro.printStackTrace();
            mostrarAlerta("Erro ao Salvar", "Não foi possível adicionar o vínculo.", erro.getMessage());
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleCancelar() {
        fecharJanela();
    }

    private void fecharJanela() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    private boolean validarCampos() {
        if (dpDataEntrada.getValue() == null || cbStatus.getValue() == null) {
            mostrarAlerta("Campos Obrigatórios", "Aviso de preenchimento", "Data de Entrada e Status são obrigatórios.");
            return false;
        }
        if (dpDataSaida.getValue() != null && dpDataSaida.getValue().isBefore(dpDataEntrada.getValue())) {
            mostrarAlerta("Datas Inválidas", "Aviso de preenchimento", "A Data de Saída não pode ser anterior à Data de Entrada.");
            return false;
        }
        return true;
    }

    private void mostrarAlerta(String titulo, String cabecalho, String conteudo) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecalho);
        alert.setContentText(conteudo);
        alert.getDialogPane().setMinWidth(480);
        alert.getDialogPane().setPrefWidth(480);
        alert.setResizable(true);
        alert.showAndWait();
    }
}
