package controller;

import dao.EstudanteMongoDAO;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Estudante;
import model.Usuario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EstudantesFormController {

    // --- Elementos FXML ---
    @FXML
    private TextField txtMatricula;
    @FXML
    private TextField txtAnoIngresso;
    @FXML
    private TextField txtMc;
    @FXML
    private Button btnSalvar;
    @FXML
    private Button btnCancelar;

    //Cadastro de Usuário
    @FXML
    private TextField txtNome;
    @FXML
    private TextField txtCpf;
    @FXML
    private TextField txtLogin;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtTelefone;
    @FXML
    private DatePicker dataNascimento;

    private Estudante estudanteEdicao; // Guarda a referência caso seja edição
    private EstudanteMongoDAO estDao;

    @FXML
    public void initialize() {
        estDao = new EstudanteMongoDAO();
    }

    public void setEstudanteParaEdicao(Estudante estudante) {
        this.estudanteEdicao = estudante;

        btnSalvar.setOnAction(event -> handleSalvar());
        btnCancelar.setOnAction(event -> handleCancelar());

        //Dados do estudante
        txtMatricula.setText(estudante.getMat_estudante());
        txtMatricula.setDisable(true);
        txtAnoIngresso.setText(String.valueOf(estudante.getAno_ingresso()));
        txtMc.setText(estudante.getMc() != null ? estudante.getMc().toString() : "0.0");

        //Dados do usuário
        if (estudante.getUsuario() != null) {
            txtCpf.setText(String.valueOf(estudante.getUsuario().getCpf()));
            txtCpf.setDisable(true);
            txtNome.setText(estudante.getUsuario().getNome());
            txtLogin.setText(estudante.getUsuario().getLogin());
            txtEmail.setText(estudante.getUsuario().emailsFormatados());
            txtTelefone.setText(estudante.getUsuario().telefonesFormatados());

            if (estudante.getUsuario().getData_nascimento() != null) {
                dataNascimento.setValue(estudante.getUsuario().getData_nascimento());
            }
        }
    }

    // --- Métodos de Ação ---

    @FXML
    private void handleSalvar() {
        if (!validarCampos()) {
            return;
        }

        try {
            String matricula = txtMatricula.getText().trim();
            int anoIngresso = Integer.parseInt(txtAnoIngresso.getText().trim());
            double mc = Double.parseDouble(txtMc.getText().trim());

            String cpfLimpo = txtCpf.getText().replaceAll("[^0-9]", "").trim();
            long cpf = Long.parseLong(cpfLimpo);

            String nome = txtNome.getText().trim();
            String login = txtLogin.getText().trim();
            LocalDate dataNasc = dataNascimento.getValue();

            List<String> listaEmails = converterStringParaLista(txtEmail.getText());
            List<String> listaTelefones = converterStringParaLista(txtTelefone.getText());

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    if (estudanteEdicao == null) {
                        //NOVO ESTUDANTE
                        Usuario novoUsuario = new Usuario(cpf, nome, dataNasc, listaEmails, listaTelefones, login);
                        Estudante novoEstudante = new Estudante(matricula, BigDecimal.valueOf(mc), anoIngresso, novoUsuario, new ArrayList<>());
                        estDao.inserir(novoEstudante);
                    } else {
                        //ATUALIZAR ESTUDANTE
                        estudanteEdicao.setAno_ingresso(anoIngresso);
                        estudanteEdicao.setMc(BigDecimal.valueOf(mc));

                        Usuario usr = estudanteEdicao.getUsuario();
                        if (usr == null) {
                            usr = new Usuario();
                            estudanteEdicao.setUsuario(usr);
                        }
                        usr.setNome(nome);
                        usr.setLogin(login);
                        usr.setData_nascimento(dataNasc);
                        usr.setEmail(listaEmails);
                        usr.setTelefone(listaTelefones);

                        estDao.atualizar(estudanteEdicao.getMat_estudante(), estudanteEdicao);
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
                mostrarAlerta("Erro ao Salvar", "Não foi possível salvar os dados.", erro.getMessage());
            });

            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();

        } catch (NumberFormatException e) {
            mostrarAlerta("Erro de Formato", "Ano de Ingresso ou MC inválidos", "Certifique-se de usar números válidos (ex: MC usar ponto final '0.0').");
        } catch (Exception e) {
            mostrarAlerta("Erro ao Salvar", "Não foi possível salvar os dados.", e.getMessage());
        }
    }

    @FXML
    private void handleCancelar() {
        fecharJanela();
    }

    private void fecharJanela() {
        Stage stage = (Stage) txtMatricula.getScene().getWindow();
        stage.close();
    }

    private List<String> converterStringParaLista(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(texto.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private boolean validarCampos() {
        if (txtMatricula.getText() == null || txtMatricula.getText().trim().isEmpty() ||
                txtNome.getText() == null || txtNome.getText().trim().isEmpty() ||
                txtCpf.getText() == null || txtCpf.getText().trim().isEmpty()) {
            mostrarAlerta("Campos Obrigatórios", "Aviso de preenchimento", "Matrícula, Nome e CPF são campos obrigatórios.");
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