package model;

public class Curso{

    private Integer idCurso;
    private String nome;
    private String grau; //Grau está em um domínio "tipo_grau" precisa-se ver como implementar isso no java
    private String turno; //mesma coisa, está no "tipo_turno"
    private String campus;
    private String nivel; //está no "tipo_nivel"

    //Constrututores
    public Curso() {
    }
    public Curso(Integer idCurso, String nome, String grau, String turno, String campus, String nivel) {
        this.idCurso = idCurso;
        this.nome = nome;
        this.grau = grau;
        this.turno = turno;
        this.campus = campus;
        this.nivel = nivel;
    }

    //Metodos Getters and Setters
    public Integer getIdCurso() {
        return idCurso;
    }
    public void setIdCurso(Integer idCurso) {
        this.idCurso = idCurso;
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getGrau() {
        return grau;
    }
    public void setGrau(String grau) {
        this.grau = grau;
    }
    public String getTurno() {
        return turno;
    }
    public void setTurno(String turno) {
        this.turno = turno;
    }
    public String getCampus() {
        return campus;
    }
    public void setCampus(String campus) {
        this.campus = campus;
    }
    public String getNivel() {
        return nivel;
    }
    public void setNivel(String nível) {
        this.nivel = nível;
    }

    @Override
    public String toString() {
        return "Curso{" +
                "idCurso=" + idCurso +
                ", nome='" + nome + '\'' +
                ", grau='" + grau + '\'' +
                ", turno='" + turno + '\'' +
                ", campus='" + campus + '\'' +
                ", nivel='" + nivel + '\'' +
                '}';
    }
}