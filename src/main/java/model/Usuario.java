package model;

import java.time.LocalDate;
import java.util.List;

public class Usuario {

    private long cpf; // NUMERIC(13) em Java vira long
    private String nome;
    private LocalDate data_nascimento; // Date vira Local Date
    private List<String> email;
    private List<String> telefone;
    private String login;
    private String senha;

    // Construtores (talvez seja necessária e criação de mais)
    public Usuario(){

    }

    public Usuario(long cpf, String nome, LocalDate data_nascimento, List<String> email, List<String> telefone, String login, String senha) {
        this.cpf = cpf;
        this.nome = nome;
        this.data_nascimento = data_nascimento;
        this.email = email;
        this.telefone = telefone;
        this.login = login;
        this.senha = senha;
    }

    // Metodos Getters e Setters

    public long getCpf() {
        return cpf;
    }

    public void setCpf(long cpf) {
        this.cpf = cpf;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getData_nascimento() {
        return data_nascimento;
    }

    public void setData_nascimento(LocalDate data_nascimento) {
        this.data_nascimento = data_nascimento;
    }

    public void setEmail(List<String> email) {
        this.email = email;
    }

    public List<String> getEmail() {
        return email;
    }

    public void setTelefone(List<String> telefone) {
        this.telefone = telefone;
    }

    public List<String> getTelefone() {
        return telefone;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

   @Override
    public String toString() {
        // Junta os elementos da lista em uma única String separada por vírgula para exibição, pois está em formato List
        String emailsStr = (email != null) ? String.join(", ", email) : "";
        String fonesStr = (telefone != null) ? String.join(", ", telefone) : "";

        // Corta os textos se forem grandes demais para não quebrar o alinhamento da tela
        if (emailsStr.length() > 30) emailsStr = emailsStr.substring(0, 27) + "...";
        if (fonesStr.length() > 20) fonesStr = fonesStr.substring(0, 17) + "...";
        String nomeCurto = (nome != null && nome.length() > 20) ? nome.substring(0, 17) + "..." : nome;

        // Retorna uma linha alinhada com espaçamentos fixos (%-20s = 20 caracteres alinhados à esquerda)
        return String.format("| %-12d | %-20s | %-12s | %-30s | %-20s | %-10s |", 
                cpf, nomeCurto, data_nascimento, emailsStr, fonesStr, login);
    }
}
