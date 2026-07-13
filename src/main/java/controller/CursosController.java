package controller;

import dao.CursoMongoDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Curso;
import model.Estudante;
import model.Vinculo;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class CursosController {

    // --- Elementos FXML ---
    @FXML private TextField txtBusca;
    @FXML private Button btnNovo;
    @FXML private Button btnAtualizar;
    @FXML private Button btnDeletar;
    @FXML private Button btnPesquisar;

    // Tabela Principal (Cursos)
    @FXML private TableView<Curso> tabelaCursos;
    @FXML private TableColumn<Curso, Integer> colIdCurso;
    @FXML private TableColumn<Curso, String> colNomeCurso;
    @FXML private TableColumn<Curso, String> colGrau;
    @FXML private TableColumn<Curso, String> colTurno;
    @FXML private TableColumn<Curso, String> colCampus;
    @FXML private TableColumn<Curso, String> colNivel;

    // Aba: Estudantes Matriculados (Definida no CursosView.fxml)
    @FXML private TableView<Estudante> tabelaEstudantesCurso;
    @FXML private TableColumn<Estudante, String> colMatriculaEstudante;
    @FXML private TableColumn<Estudante, String> colNomeEstudante;
    @FXML private TableColumn<Estudante, String> colStatusEstudante;

    private CursoMongoDAO cursoDao;

    @FXML
    public void initialize() {
        // Inicializa o DAO de Cursos
        cursoDao = new CursoMongoDAO();

        btnAtualizar.setOnAction(event -> handleAtualizarCurso());
        btnNovo.setOnAction(event -> handleNovoCurso());
        btnPesquisar.setOnAction(event -> handlePesquisarCurso());
        btnDeletar.setOnAction(event -> handleDeletarCurso());

        configurarColunas();
        carregarDadosCursos();

        // Listener para monitorar a seleção de cursos na tabela principal
        tabelaCursos.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novoSelecao) -> {
            if (novoSelecao != null) {
                mostrarEstudantesDoCurso(novoSelecao);
            } else {
                tabelaEstudantesCurso.getItems().clear();
            }
        });
    }

    private void configurarColunas() {
        // Configuração da tabela principal (Cursos) mapeada com Curso.java
        colIdCurso.setCellValueFactory(new PropertyValueFactory<>("idCurso"));
        colNomeCurso.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colGrau.setCellValueFactory(new PropertyValueFactory<>("grau"));
        colTurno.setCellValueFactory(new PropertyValueFactory<>("turno"));
        colCampus.setCellValueFactory(new PropertyValueFactory<>("campus"));
        colNivel.setCellValueFactory(new PropertyValueFactory<>("nivel"));

        // Configuração da sub-tabela (Estudantes Matriculados) mapeada com Estudante.java
        colMatriculaEstudante.setCellValueFactory(new PropertyValueFactory<>("mat_estudante"));

        // Extrai o nome de dentro do documento Usuario embutido no Estudante de forma segura
        colNomeEstudante.setCellValueFactory(cellData -> {
            Estudante est = cellData.getValue();
            if (est != null && est.getUsuario() != null) {
                return new SimpleStringProperty(est.getUsuario().getNome());
            }
            return new SimpleStringProperty("");
        });

        // Varre a lista de vínculos do estudante selecionado para encontrar o status correspondente a este curso
        colStatusEstudante.setCellValueFactory(cellData -> {
            Estudante est = cellData.getValue();
            Curso cursoSelecionado = tabelaCursos.getSelectionModel().getSelectedItem();

            if (est != null && cursoSelecionado != null && est.getVinculo() != null) {
                for (Vinculo v : est.getVinculo()) {
                    if (cursoSelecionado.getIdCurso().equals(v.getIdCurso())) {
                        return new SimpleStringProperty(v.getStatus());
                    }
                }
            }
            return new SimpleStringProperty("N/A");
        });
    }

    private void carregarDadosCursos() {
        Task<List<Curso>> task = new Task<>() {
            @Override
            protected List<Curso> call() {
                return cursoDao.listarTodos();
            }
        };

        task.setOnSucceeded(e -> tabelaCursos.setItems(FXCollections.observableArrayList(task.getValue())));
        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            exibirErro("Não foi possível carregar os cursos.", task.getException());
        });

        executarEmBackground(task);
    }

    private void mostrarEstudantesDoCurso(Curso curso) {
        Task<List<Estudante>> task = new Task<>() {
            @Override
            protected List<Estudante> call() {
                return cursoDao.buscarEstudantesPorCurso(curso.getIdCurso());
            }
        };

        task.setOnSucceeded(e -> {
            List<Estudante> estudantes = task.getValue();
            if (estudantes != null && !estudantes.isEmpty()) {
                tabelaEstudantesCurso.setItems(FXCollections.observableArrayList(estudantes));
            } else {
                tabelaEstudantesCurso.getItems().clear();
            }
        });
        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            exibirErro("Não foi possível carregar os estudantes do curso.", task.getException());
        });

        executarEmBackground(task);
    }

    private void executarEmBackground(Task<?> task) {
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void exibirErro(String contexto, Throwable erro) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro de comunicação com o banco");
        alert.setHeaderText(contexto);
        alert.setContentText(erro != null ? erro.getMessage() : "Erro desconhecido.");
        alert.showAndWait();
    }

    // --- Métodos de Ação ---

    @FXML
    private void handlePesquisarCurso() {
        String termoBusca = txtBusca.getText();

        if (termoBusca == null || termoBusca.trim().isEmpty()) {
            carregarDadosCursos();
            return;
        }

        try {
            Integer idBusca = Integer.parseInt(termoBusca.trim());

            Task<Curso> task = new Task<>() {
                @Override
                protected Curso call() {
                    return cursoDao.buscarPorId(idBusca);
                }
            };

            task.setOnSucceeded(e -> {
                Curso encontrado = task.getValue();
                if (encontrado != null) {
                    tabelaCursos.setItems(FXCollections.observableArrayList(encontrado));
                    tabelaCursos.getSelectionModel().select(encontrado);
                } else {
                    exibirMensagemInformativa("Nenhum curso encontrado com o ID informado.");
                }
            });
            task.setOnFailed(e -> {
                task.getException().printStackTrace();
                exibirErro("Não foi possível pesquisar o curso.", task.getException());
            });

            executarEmBackground(task);
        } catch (NumberFormatException e) {
            exibirMensagemInformativa("Por favor, insira um ID numérico válido para a pesquisa.");
        }
    }

    @FXML
    private void handleNovoCurso() {
        abrirFormularioCurso(null);
    }

    @FXML
    private void handleAtualizarCurso() {
        Curso selecionado = tabelaCursos.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            abrirFormularioCurso(selecionado);
        } else {
            mostrarAlertaSelecao();
        }
    }

    @FXML
    private void handleDeletarCurso() {
        Curso selecionado = tabelaCursos.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Exclusão");
            alert.setHeaderText("Excluir Curso");
            alert.setContentText("Tem certeza que deseja deletar o curso " + selecionado.getNome() + "?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Task<Boolean> task = new Task<>() {
                    @Override
                    protected Boolean call() {
                        return cursoDao.deletar(selecionado.getIdCurso());
                    }
                };
                task.setOnSucceeded(e -> carregarDadosCursos());
                task.setOnFailed(e -> {
                    task.getException().printStackTrace();
                    exibirErro("Não foi possível deletar o curso.", task.getException());
                });
                executarEmBackground(task);
            }
        } else {
            mostrarAlertaSelecao();
        }
    }

    private void mostrarAlertaSelecao() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText("Por favor, selecione um curso na tabela primeiro.");
        alert.showAndWait();
    }

    private void exibirMensagemInformativa(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Busca de Cursos");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void abrirFormularioCurso(Curso curso) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/CursosFormView.fxml"));
            Parent root = loader.load();

            CursosFormController formController = loader.getController();

            if (curso != null) {
                formController.setCursoParaEdicao(curso);
            }

            Stage stage = new Stage();
            stage.setTitle(curso == null ? "Cadastrar Curso" : "Editar Curso");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tabelaCursos.getScene().getWindow());
            stage.showAndWait();

            // Sincroniza a tabela principal ao fechar a tela de formulário
            carregarDadosCursos();

        } catch (IOException e) {
            System.err.println("Erro ao carregar CursosFormView.fxml");
            e.printStackTrace();
        }
    }
}