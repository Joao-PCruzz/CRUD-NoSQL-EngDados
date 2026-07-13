package controller;

import dao.CursoMongoDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Curso;

public class CursosFormController {

    // --- Elementos FXML ---
    @FXML private TextField txtIdCurso;
    @FXML private TextField txtNome;
    @FXML private TextField txtGrau;
    @FXML private TextField txtTurno;
    @FXML private TextField txtCampus;
    @FXML private TextField txtNivel;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;

    private Curso cursoEdicao; // Guarda a referência se for uma operação de EDIÇÃO
    private CursoMongoDAO cursoDao;

    @FXML
    public void initialize() {
        // Inicializa o DAO para persistência no MongoDB
        cursoDao = new CursoMongoDAO();

        btnSalvar.setOnAction(event -> handleSalvar());
        btnCancelar.setOnAction(event -> handleCancelar());
    }

    public void setCursoParaEdicao(Curso curso) {
        this.cursoEdicao = curso;

        txtIdCurso.setText(String.valueOf(curso.getIdCurso()));
        txtIdCurso.setDisable(true);

        txtNome.setText(curso.getNome());
        txtGrau.setText(curso.getGrau());
        txtTurno.setText(curso.getTurno());
        txtCampus.setText(curso.getCampus());
        txtNivel.setText(curso.getNivel());
    }

    // --- Métodos de Ação ---

    @FXML
    private void handleSalvar() {
        if (!validarCampos()) {
            return;
        }

        try {
            Integer idCurso = Integer.parseInt(txtIdCurso.getText().trim());
            String nome = txtNome.getText().trim();
            String grau = txtGrau.getText().trim();
            String turno = txtTurno.getText().trim();
            String campus = txtCampus.getText().trim();
            String nivel = txtNivel.getText().trim();

            if (cursoEdicao == null) {
                // Cenário: NOVO CURSO
                if (cursoDao.buscarPorId(idCurso) != null) {
                    mostrarAlerta("Erro de Duplicação", "ID de Curso já existente", "Já existe um curso registado com este ID. Introduza um identificador único.");
                    return;
                }

                Curso novoCurso = new Curso(idCurso, nome, grau, turno, campus, nivel);
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

            // Fecha a janela em caso de sucesso
            fecharJanela();

        } catch (NumberFormatException e) {
            mostrarAlerta("Erro de Formato", "ID do Curso Inválido", "O campo 'ID do Curso' deve conter apenas números inteiros.");
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
        if (txtIdCurso.getText() == null || txtIdCurso.getText().trim().isEmpty() ||
                txtNome.getText() == null || txtNome.getText().trim().isEmpty() ||
                txtGrau.getText() == null || txtGrau.getText().trim().isEmpty() ||
                txtTurno.getText() == null || txtTurno.getText().trim().isEmpty() ||
                txtCampus.getText() == null || txtCampus.getText().trim().isEmpty() ||
                txtNivel.getText() == null || txtNivel.getText().trim().isEmpty()) {

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
        alert.showAndWait();
    }
}