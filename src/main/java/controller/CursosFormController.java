package controller;

import dao.CursoMongoDAO;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Curso;

public class CursosFormController {

    // --- Elementos FXML ---
    @FXML private TextField txtIdCurso;
    @FXML private TextField txtNome;
    @FXML private ComboBox<String> cbGrau;
    @FXML private ComboBox<String> cbTurno;
    @FXML private TextField txtCampus;
    @FXML private ComboBox<String> cbNivel;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;

    private Curso cursoEdicao; // Guarda a referência se for uma operação de EDIÇÃO
    private CursoMongoDAO cursoDao;

    @FXML
    public void initialize() {
        // Inicializa o DAO para persistência no MongoDB
        cursoDao = new CursoMongoDAO();

        // Popula os ComboBox com os mesmos valores dos ENUMs do banco (tipo_grau, tipo_turno, tipo_nivel)
        cbGrau.setItems(FXCollections.observableArrayList("Bacharelado", "Licenciatura Plena"));
        cbTurno.setItems(FXCollections.observableArrayList("Matutino", "Vespertino", "Noturno", "Turno Indefinido"));
        cbNivel.setItems(FXCollections.observableArrayList("Graduação", "Mestrado", "Doutorado", "Lato"));

        btnSalvar.setOnAction(event -> handleSalvar());
        btnCancelar.setOnAction(event -> handleCancelar());
    }

    public void setCursoParaEdicao(Curso curso) {
        this.cursoEdicao = curso;

        txtIdCurso.setText(String.valueOf(curso.getIdCurso()));

        txtNome.setText(curso.getNome());
        cbGrau.setValue(curso.getGrau());
        cbTurno.setValue(curso.getTurno());
        txtCampus.setText(curso.getCampus());
        cbNivel.setValue(curso.getNivel());
    }

    // --- Métodos de Ação ---

    @FXML
    private void handleSalvar() {
        if (!validarCampos()) {
            return;
        }

        try {
            String nome = txtNome.getText().trim();
            String grau = cbGrau.getValue();
            String turno = cbTurno.getValue();
            String campus = txtCampus.getText().trim();
            String nivel = cbNivel.getValue();

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    if (cursoEdicao == null) {
                        // idCurso é gerado automaticamente dentro do DAO
                        Curso novoCurso = new Curso(null, nome, grau, turno, campus, nivel);
                        cursoDao.inserir(novoCurso);
                    } else {
                        // Cenário: EDIÇÃO DE CURSO
                        cursoEdicao.setNome(nome);
                        cursoEdicao.setGrau(grau);
                        cursoEdicao.setTurno(turno);
                        cursoEdicao.setCampus(campus);
                        cursoEdicao.setNivel(nivel);

                        cursoDao.atualizar(cursoEdicao.getIdCurso(), cursoEdicao);
                    }
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
                mostrarAlerta("Erro ao Salvar", "Falha na persistência dos dados NoSQL", erro.getMessage());
            });

            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {
            mostrarAlerta("Erro ao Salvar", "Falha na persistência dos dados NoSQL", e.getMessage());
        }
    }

    @FXML
    private void handleCancelar() {
        fecharJanela();
    }

    private void fecharJanela() {
        Stage stage = (Stage) txtIdCurso.getScene().getWindow();
        stage.close();
    }

    private boolean validarCampos() {
        if (txtNome.getText() == null || txtNome.getText().trim().isEmpty() ||
                cbGrau.getValue() == null ||
                cbTurno.getValue() == null ||
                txtCampus.getText() == null || txtCampus.getText().trim().isEmpty() ||
                cbNivel.getValue() == null) {

            mostrarAlerta("Campos Obrigatórios", "Aviso de preenchimento", "Todos os campos marcados com asterisco (*) são de preenchimento obrigatório.");
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