package model;

import java.time.LocalDate;

import org.bson.codecs.pojo.annotations.BsonIgnore;


public class Vinculo {
    private Integer idVinculo; // PK gerada automaticamente (equivalente ao SERIAL idVinculo do SQL)
    private Integer idCurso;
    private LocalDate data_entrada;
    private String status; //Faz parte do domínio "status_estudante"
    private LocalDate data_saida;

    // Campo apenas de exibição (não é persistido no MongoDB): guarda o nome do
    // curso resolvido a partir do idCurso, para preencher a coluna "Curso" na tabela.
    @BsonIgnore
    private String nomeCurso;

    // Construtores
    public Vinculo() {
    }

    public Vinculo(Integer idCurso, LocalDate data_entrada, String status, LocalDate data_saida) {
        this.idCurso = idCurso;
        this.data_entrada = data_entrada;
        this.status = status;
        this.data_saida = data_saida;
    }

    public Vinculo(Integer idVinculo, Integer idCurso, LocalDate data_entrada, String status, LocalDate data_saida) {
        this.idVinculo = idVinculo;
        this.idCurso = idCurso;
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

    public String getNomeCurso() {
        return nomeCurso;
    }

    public void setNomeCurso(String nomeCurso) {
        this.nomeCurso = nomeCurso;
    }

    public Integer getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(Integer idCurso) {
        this.idCurso = idCurso;
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
                ", idCurso=" + idCurso +
                ", data_entrada=" + data_entrada +
                ", status='" + status + '\'' +
                ", data_saida=" + data_saida +
                '}';
    }
}
