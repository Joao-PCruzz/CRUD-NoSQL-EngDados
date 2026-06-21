package model;

import java.time.LocalDate;


public class Vinculo {
    
    private Integer idVinculo;
    private LocalDate data_entrada;
    private String status; //Faz parte do domínio "status_estudante"
    private LocalDate data_saida;
    private Estudante estudante;
    private Curso curso;

    // Construtores
    public Vinculo() {
    }

    public Vinculo(Integer idVinculo, Estudante estudante, Curso curso, LocalDate data_entrada, String status, LocalDate data_saida) {
        this.idVinculo = idVinculo;
        this.estudante = estudante;
        this.curso = curso;
        this.data_entrada = data_entrada;
        this.status = status;
        this.data_saida = data_saida;
    }

    // Metodo Getters and Setters

    public Integer getIdVinculo() {
        return idVinculo;
    }

    public void setIdVinculo(Integer idVinculo) {
        this.idVinculo = idVinculo;
    }

    public Estudante getEstudante() {
        return estudante;
    }

    public void setEstudante(Estudante estudante) {
        this.estudante = estudante;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public LocalDate getData_entrada() {
        return data_entrada;
    }

    public void setData_entrada(LocalDate data_entrada) {
        this.data_entrada = data_entrada;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getData_saida() {
        return data_saida;
    }

    public void setData_saida(LocalDate data_saida) {
        this.data_saida = data_saida;
    }

   @Override
    public String toString() {
        return "Vinculo{" +
                "idVinculo=" + idVinculo +
                ", mat_estudante='" + (estudante != null ? estudante.getMat_estudante() : "null") + '\'' +
                ", id_curso=" + (curso != null ? curso.getIdCurso() : "null") +
                ", data_entrada=" + data_entrada +
                ", status='" + status + '\'' +
                ", data_saida=" + data_saida +
                '}';
    }
}
