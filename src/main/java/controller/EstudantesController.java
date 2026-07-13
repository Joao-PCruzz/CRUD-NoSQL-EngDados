package controller;

import dao.EstudanteMongoDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Estudante;
import model.Usuario;
import model.Vinculo;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class EstudantesController {
    //ATTRIBUTES
    // --- Elementos FXML ---
    @FXML private TextField txtBusca;
    @FXML private Button btnNovo;
    @FXML private Button btnAtualizar;
    @FXML private Button btnDeletar;
    @FXML private Button btnPesquisar;

    // Tabela Principal (Estudantes)
    @FXML private TableView<Estudante> tabelaEstudantes;
    @FXML private TableColumn<Estudante, String> colMatricula;
    @FXML private TableColumn<Estudante, Double> colMc;
    @FXML private TableColumn<Estudante, Integer> colAnoIngresso;

    // Aba de Vínculos
    @FXML private TableView<Vinculo> tabelaVinculos;
    @FXML private TableColumn<Vinculo, String> colIdVinculo;
    @FXML private TableColumn<Vinculo, String> colDataEntrada;
    @FXML private TableColumn<Vinculo, String> colDataSaida;
    @FXML private TableColumn<Vinculo, String> colStatusVinculo;
    @FXML private TableColumn<Vinculo, String> colCursoVinculo;

    // Aba de Usuário
    @FXML private TableView<Usuario> tabelaUsuario;
    @FXML private TableColumn<Usuario, String> colCpf;
    @FXML private TableColumn<Usuario, String> colNome;
    @FXML private TableColumn<Usuario, String> colLogin;
    @FXML private TableColumn<Usuario, String> colDataNascimento;
    @FXML private TableColumn<Usuario, String> colEmails;
    @FXML private TableColumn<Usuario, String> colTelefones;

    private EstudanteMongoDAO estDao;

    //METHODS

    @FXML
    public void initialize() {
        estDao = new EstudanteMongoDAO();

        btnAtualizar.setOnAction(event -> handleAtualizarEstudante());
        btnNovo.setOnAction(event -> handleNovoEstudante());
        btnPesquisar.setOnAction(event -> handlePesquisarEstudante());
        btnDeletar.setOnAction(event -> handleDeletarEstudante());

        configurarColunas();
        carregarDadosEstudantes();

        tabelaEstudantes.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novoSelecao) -> {
            if (novoSelecao != null) {
                mostrarDetalhesEstudante(novoSelecao);
            } else {
                // Limpa as tabelas de detalhes caso nenhum estudante esteja selecionado
                tabelaVinculos.getItems().clear();
                tabelaUsuario.getItems().clear();
            }
        });
    }

    private void configurarColunas() {
        // Colunas da tabela de Estudantes
        colMatricula.setCellValueFactory(new PropertyValueFactory<>("mat_estudante"));
        colMc.setCellValueFactory(new PropertyValueFactory<>("mc"));
        colAnoIngresso.setCellValueFactory(new PropertyValueFactory<>("ano_ingresso"));

        // Colunas da tabela de Vínculos
        colIdVinculo.setCellValueFactory(new PropertyValueFactory<>("idVinculo"));
        colDataEntrada.setCellValueFactory(new PropertyValueFactory<>("data_entrada"));
        colDataSaida.setCellValueFactory(new PropertyValueFactory<>("data_saida"));
        colStatusVinculo.setCellValueFactory(new PropertyValueFactory<>("status"));
        colCursoVinculo.setCellValueFactory(new PropertyValueFactory<>("curso"));

        // Colunas da tabela de Usuário
        colCpf.setCellValueFactory(new PropertyValueFactory<>("cpf"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colLogin.setCellValueFactory(new PropertyValueFactory<>("login"));
        colDataNascimento.setCellValueFactory(new PropertyValueFactory<>("data_nascimento"));
        colEmails.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelefones.setCellValueFactory(new PropertyValueFactory<>("telefone"));
    }

    private void carregarDadosEstudantes() {
        Task<List<Estudante>> task = new Task<>() {
            @Override
            protected List<Estudante> call() {
                return estDao.listarTodos();
            }
        };

        task.setOnSucceeded(e -> tabelaEstudantes.setItems(FXCollections.observableArrayList(task.getValue())));
        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            exibirErro("Não foi possível carregar os estudantes.", task.getException());
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

    private void mostrarDetalhesEstudante(Estudante estudante) {
        // Popula os Vínculos do estudante selecionado
        if (estudante.getVinculo() != null) {
            ObservableList<Vinculo> obsVinculos = FXCollections.observableArrayList(estudante.getVinculo());
            tabelaVinculos.setItems(obsVinculos);
        } else {
            tabelaVinculos.getItems().clear();
        }

        // Popula os dados do Usuário
        if (estudante.getUsuario() != null) {
            ObservableList<Usuario> obsUsuario = FXCollections.observableArrayList(estudante.getUsuario());
            tabelaUsuario.setItems(obsUsuario);
        } else {
            tabelaUsuario.getItems().clear();
        }
    }

    // --- Métodos de Ação ---

    @FXML
    private void handlePesquisarEstudante() {
        String termoBusca = txtBusca.getText();

        if (termoBusca == null || termoBusca.trim().isEmpty()) {
            carregarDadosEstudantes(); // Se campo vazio, recarrega todos
            return;
        }

        String matricula = termoBusca.trim();
        Task<Estudante> task = new Task<>() {
            @Override
            protected Estudante call() {
                return estDao.buscarPorMatricula(matricula);
            }
        };

        task.setOnSucceeded(e -> {
            Estudante encontrado = task.getValue();
            if (encontrado != null) {
                tabelaEstudantes.setItems(FXCollections.observableArrayList(encontrado));
                tabelaEstudantes.getSelectionModel().select(encontrado);
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Busca");
                alert.setHeaderText(null);
                alert.setContentText("Nenhum estudante encontrado com a matrícula informada.");
                alert.showAndWait();
            }
        });
        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            exibirErro("Não foi possível pesquisar o estudante.", task.getException());
        });

        executarEmBackground(task);
    }

    @FXML
    private void handleNovoEstudante() {
        abrirFormularioEstudante(null);
    }

    @FXML
    private void handleAtualizarEstudante() {
        Estudante selecionado = tabelaEstudantes.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            abrirFormularioEstudante(selecionado);
        } else {
            mostrarAlertaSelecao();
        }
    }

    @FXML
    private void handleDeletarEstudante() {
        Estudante selecionado = tabelaEstudantes.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Exclusão");
            alert.setHeaderText("Excluir Estudante");
            alert.setContentText("Tem certeza que deseja deletar o estudante " + selecionado.getUsuario().getNome() + "?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Task<Boolean> task = new Task<>() {
                    @Override
                    protected Boolean call() {
                        return estDao.deletar(selecionado.getMat_estudante());
                    }
                };
                task.setOnSucceeded(e -> carregarDadosEstudantes());
                task.setOnFailed(e -> {
                    task.getException().printStackTrace();
                    exibirErro("Não foi possível deletar o estudante.", task.getException());
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
        alert.setContentText("Por favor, selecione um estudante na tabela primeiro.");
        alert.showAndWait();
    }

    private void abrirFormularioEstudante(Estudante estudante) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/EstudantesFormView.fxml"));
            Parent root = loader.load();

            EstudantesFormController formController = loader.getController();

            // Se for edição, injeta o estudante selecionado no controller do formulário
            if (estudante != null) {
                formController.setEstudanteParaEdicao(estudante);
            }

            Stage stage = new Stage();
            stage.setTitle(estudante == null ? "Cadastrar Estudante" : "Editar Estudante");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tabelaEstudantes.getScene().getWindow());
            stage.showAndWait();

            carregarDadosEstudantes();

        } catch (IOException e) {
            System.err.println("Erro ao carregar o formulário de estudantes.");
            e.printStackTrace();
        }
    }
}