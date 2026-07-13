package main;

import dao.CursoMongoDAO;
import dao.EstudanteMongoDAO;
import model.Curso;
import model.Estudante;
import model.Usuario;
import model.Vinculo;

import com.mongodb.MongoException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

//Menu interativo de terminal para testar o CRUD

public class Main {
    private static final Scanner sc = new Scanner(System.in, java.nio.charset.StandardCharsets.UTF_8);
    private static final CursoMongoDAO cursoDAO = new CursoMongoDAO();
    private static final EstudanteMongoDAO estudanteDAO = new EstudanteMongoDAO();

    public static void main(String[] args) {
        int opcao;
        do {
            exibirMenu();
            opcao = lerInteiro("Escolha uma opção: ");
            try {
                executarOpcao(opcao);
            } catch (IllegalArgumentException e) {
                // Erros de validação de domínio (grau/turno/nivel/status inválido)
                System.out.println("[VALIDAÇÃO] " + e.getMessage());
            } catch (MongoException e) {
                // Erros vindos do MongoDB (duplicidade, falha de schema validator, etc.)
                System.out.println("[ERRO MONGODB] " + e.getMessage());
            } catch (Exception e) {
                System.out.println("[ERRO INESPERADO] " + e.getMessage());
            }
            System.out.println();
        } while (opcao != 0);

        System.out.println("Encerrando...");
        sc.close();
    }

    private static void exibirMenu() {
        System.out.println("======= MENU CRUD NoSQL =======");
        System.out.println("--- Curso ---");
        System.out.println(" 1) Cadastrar curso");
        System.out.println(" 2) Buscar curso por idCurso");
        System.out.println(" 3) Buscar cursos por nome (parcial)");
        System.out.println(" 4) Listar todos os cursos");
        System.out.println(" 5) Atualizar curso");
        System.out.println(" 6) Deletar curso");
        System.out.println("--- Estudante ---");
        System.out.println(" 7) Cadastrar estudante");
        System.out.println(" 8) Buscar estudante por matrícula");
        System.out.println(" 9) Listar todos os estudantes");
        System.out.println("10) Atualizar estudante");
        System.out.println("11) Adicionar vínculo a um estudante");
        System.out.println("12) Remover vínculo de um estudante");
        System.out.println("13) Deletar estudante");
        System.out.println(" 0) Sair");
    }

    private static void executarOpcao(int opcao) {
        switch (opcao) {
            case 1: cadastrarCurso(); break;
            case 2: buscarCursoPorId(); break;
            case 3: buscarCursosPorNome(); break;
            case 4: listarCursos(); break;
            case 5: atualizarCurso(); break;
            case 6: deletarCurso(); break;
            case 7: cadastrarEstudante(); break;
            case 8: buscarEstudante(); break;
            case 9: listarEstudantes(); break;
            case 10: atualizarEstudante(); break;
            case 11: adicionarVinculo(); break;
            case 12: removerVinculo(); break;
            case 13: deletarEstudante(); break;
            case 0: break;
            default: System.out.println("Opção inválida.");
        }
    }

    // ===================== CURSO =====================

    private static void cadastrarCurso() {
        System.out.println("--- Cadastrar curso ---");
        int idCurso = lerInteiroObrigatorio("idCurso (número): ");
        String nome = lerTextoObrigatorio("Nome: ");
        System.out.println("Graus aceitos: Bacharelado, Licenciatura Plena");
        String grau = lerTexto("Grau: ");
        System.out.println("Turnos aceitos: Matutino, Vespertino, Noturno, Turno Indefinido");
        String turno = lerTexto("Turno: ");
        String campus = lerTexto("Campus: ");
        System.out.println("Níveis aceitos: Graduação, Mestrado, Doutorado, Lato");
        String nivel = lerTexto("Nível: ");

        Curso curso = new Curso(idCurso, nome, grau, turno, campus, nivel);
        cursoDAO.inserir(curso);
    }

    private static void buscarCursoPorId() {
        int idCurso = lerInteiro("idCurso: ");
        Curso curso = cursoDAO.buscarPorId(idCurso);
        System.out.println(curso != null ? curso : "[AVISO] Nenhum curso encontrado com esse idCurso.");
    }

    private static void buscarCursosPorNome() {
        String nome = lerTexto("Trecho do nome a buscar: ");
        List<Curso> resultado = cursoDAO.buscarPorNome(nome);
        if (resultado.isEmpty()) {
            System.out.println("[AVISO] Nenhum curso encontrado.");
        } else {
            resultado.forEach(System.out::println);
        }
    }

    private static void listarCursos() {
        List<Curso> lista = cursoDAO.listarTodos();
        if (lista.isEmpty()) {
            System.out.println("[AVISO] Nenhum curso cadastrado.");
        } else {
            lista.forEach(System.out::println);
        }
    }

    private static void atualizarCurso() {
        int idCurso = lerInteiro("idCurso do curso a atualizar: ");
        System.out.println("Deixe em branco (Enter) para não alterar um campo.");

        Curso atualizacao = new Curso();
        String nome = lerTextoOpcional("Novo nome: ");
        String grau = lerTextoOpcional("Novo grau: ");
        String turno = lerTextoOpcional("Novo turno: ");
        String campus = lerTextoOpcional("Novo campus: ");
        String nivel = lerTextoOpcional("Novo nível: ");

        atualizacao.setNome(nome);
        atualizacao.setGrau(grau);
        atualizacao.setTurno(turno);
        atualizacao.setCampus(campus);
        atualizacao.setNivel(nivel);

        cursoDAO.atualizar(idCurso, atualizacao);
    }

    private static void deletarCurso() {
        int idCurso = lerInteiro("idCurso do curso a deletar: ");
        System.out.println("Atenção: isso também vai remover a referência (idCurso) do vínculo de estudantes matriculados nesse curso.");
        if (confirmar("Confirma a exclusão? (s/n): ")) {
            cursoDAO.deletar(idCurso);
        } else {
            System.out.println("Operação cancelada.");
        }
    }

    // ===================== ESTUDANTE =====================

    private static void cadastrarEstudante() {
        System.out.println("--- Cadastrar estudante ---");
        String matricula = lerTextoObrigatorio("Matrícula (mat_estudante): ");
        BigDecimal mc = lerBigDecimalOpcional("MC (média, pode deixar em branco): ");
        Integer anoIngresso = lerInteiroOpcional("Ano de ingresso: ");

        System.out.println("-- Dados do usuário --");
        Long cpf = lerLongObrigatorio("CPF (somente números): ");
        String nome = lerTextoObrigatorio("Nome completo: ");
        LocalDate dataNascimento = lerDataOpcional("Data de nascimento (AAAA-MM-DD, opcional): ");
        List<String> emails = lerListaOpcional("E-mails (separados por vírgula, opcional): ");
        List<String> telefones = lerListaOpcional("Telefones (separados por vírgula, opcional): ");
        String login = lerTextoObrigatorio("Login: ");
        String senha = lerTextoObrigatorio("Senha: ");

        Usuario usuario = new Usuario(cpf != null ? cpf : 0L, nome, dataNascimento, emails, telefones, login, senha);

        List<Vinculo> vinculos = new ArrayList<>();
        if (confirmar("Deseja adicionar um vínculo (curso) agora? (s/n): ")) {
            vinculos.add(lerVinculo());
        }

        Estudante estudante = new Estudante(matricula, mc, anoIngresso, usuario, vinculos);
        estudanteDAO.inserir(estudante);
    }

    private static void buscarEstudante() {
        String matricula = lerTexto("Matrícula: ");
        Estudante estudante = estudanteDAO.buscarPorMatricula(matricula);
        System.out.println(estudante != null ? estudante : "[AVISO] Nenhum estudante encontrado com essa matrícula.");
    }

    private static void listarEstudantes() {
        List<Estudante> lista = estudanteDAO.listarTodos();
        if (lista.isEmpty()) {
            System.out.println("[AVISO] Nenhum estudante cadastrado.");
        } else {
            lista.forEach(System.out::println);
        }
    }

    private static void atualizarEstudante() {
        String matricula = lerTexto("Matrícula do estudante a atualizar: ");
        System.out.println("Deixe em branco (Enter) para não alterar um campo.");

        BigDecimal mc = lerBigDecimalOpcional("Nova MC: ");
        Integer anoIngresso = lerInteiroOpcional("Novo ano de ingresso: ");
        String nome = lerTextoOpcional("Novo nome: ");
        String login = lerTextoOpcional("Novo login: ");
        String senha = lerTextoOpcional("Nova senha: ");
        List<String> emails = lerListaOpcional("Novos e-mails (separados por vírgula): ");
        List<String> telefones = lerListaOpcional("Novos telefones (separados por vírgula): ");
        LocalDate dataNascimento = lerDataOpcional("Nova data de nascimento (AAAA-MM-DD): ");

        Usuario usuarioAtualizado = new Usuario();
        usuarioAtualizado.setNome(nome);
        usuarioAtualizado.setLogin(login);
        usuarioAtualizado.setSenha(senha);
        usuarioAtualizado.setEmail(emails);
        usuarioAtualizado.setTelefone(telefones);
        usuarioAtualizado.setData_nascimento(dataNascimento);

        Estudante estudanteAtualizado = new Estudante();
        estudanteAtualizado.setMc(mc);
        estudanteAtualizado.setAno_ingresso(anoIngresso);
        estudanteAtualizado.setUsuario(usuarioAtualizado);

        estudanteDAO.atualizar(matricula, estudanteAtualizado);
    }

    private static void adicionarVinculo() {
        String matricula = lerTexto("Matrícula do estudante: ");
        Vinculo vinculo = lerVinculo();
        boolean ok = estudanteDAO.adicionarVinculo(matricula, vinculo);
        System.out.println(ok ? "[OK] Vínculo adicionado." : "[AVISO] Nenhum estudante encontrado com essa matrícula, nada foi alterado.");
    }

    private static void removerVinculo() {
        String matricula = lerTexto("Matrícula do estudante: ");
        int idCurso = lerInteiro("idCurso do vínculo a remover: ");
        boolean ok = estudanteDAO.removerVinculo(matricula, idCurso);
        System.out.println(ok ? "[OK] Vínculo removido." : "[AVISO] Nenhum estudante encontrado com essa matrícula, nada foi alterado.");
    }

    private static void deletarEstudante() {
        String matricula = lerTexto("Matrícula do estudante a deletar: ");
        if (confirmar("Confirma a exclusão? (s/n): ")) {
            estudanteDAO.deletar(matricula);
        } else {
            System.out.println("Operação cancelada.");
        }
    }

    private static Vinculo lerVinculo() {
        Integer idCurso = lerInteiroOpcional("idCurso: ");
        LocalDate dataEntrada = lerDataObrigatoria("Data de entrada (AAAA-MM-DD): ");
        System.out.println("Status aceitos: Ativo, Cancelada, Formando, Graduado");
        String status = lerTextoOpcional("Status (opcional): ");
        LocalDate dataSaida = lerDataOpcional("Data de saída (AAAA-MM-DD, opcional): ");
        return new Vinculo(idCurso, dataEntrada, status, dataSaida);
    }

    // ===================== HELPERS DE LEITURA =====================

    private static String lerTexto(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private static String lerTextoOpcional(String prompt) {
        System.out.print(prompt);
        String linha = sc.nextLine().trim();
        return linha.isEmpty() ? null : linha;
    }

    private static String lerTextoObrigatorio(String prompt) {
        while (true) {
            System.out.print(prompt);
            String linha = sc.nextLine().trim();
            if (!linha.isEmpty()) return linha;
            System.out.println("Este campo é obrigatório, não pode ficar em branco.");
        }
    }

    private static int lerInteiro(String prompt) {
        while (true) {
            System.out.print(prompt);
            String linha = sc.nextLine().trim();
            try {
                return Integer.parseInt(linha);
            } catch (NumberFormatException e) {
                System.out.println("Digite um número inteiro válido.");
            }
        }
    }

    private static Integer lerInteiroOpcional(String prompt) {
        System.out.print(prompt);
        String linha = sc.nextLine().trim();
        if (linha.isEmpty()) return null;
        try {
            return Integer.parseInt(linha);
        } catch (NumberFormatException e) {
            System.out.println("Valor inválido, ignorando campo.");
            return null;
        }
    }

    private static Integer lerInteiroObrigatorio(String prompt) {
        while (true) {
            System.out.print(prompt);
            String linha = sc.nextLine().trim();
            try {
                return Integer.parseInt(linha);
            } catch (NumberFormatException e) {
                System.out.println("Digite um idCurso válido (somente números).");
            }
        }
    }

    private static Long lerLongOpcional(String prompt) {
        System.out.print(prompt);
        String linha = sc.nextLine().trim();
        if (linha.isEmpty()) return null;
        try {
            return Long.parseLong(linha);
        } catch (NumberFormatException e) {
            System.out.println("Valor inválido, ignorando campo.");
            return null;
        }
    }

    private static long lerLongObrigatorio(String prompt) {
        while (true) {
            System.out.print(prompt);
            String linha = sc.nextLine().trim();
            try {
                return Long.parseLong(linha);
            } catch (NumberFormatException e) {
                System.out.println("Digite um CPF válido (somente números).");
            }
        }
    }

    private static BigDecimal lerBigDecimalOpcional(String prompt) {
        System.out.print(prompt);
        String linha = sc.nextLine().trim();
        if (linha.isEmpty()) return null;
        try {
            return new BigDecimal(linha.replace(",", "."));
        } catch (NumberFormatException e) {
            System.out.println("Valor inválido, ignorando campo.");
            return null;
        }
    }

    private static LocalDate lerDataOpcional(String prompt) {
        System.out.print(prompt);
        String linha = sc.nextLine().trim();
        if (linha.isEmpty()) return null;
        try {
            return LocalDate.parse(linha);
        } catch (DateTimeParseException e) {
            System.out.println("Data inválida (use AAAA-MM-DD), ignorando campo.");
            return null;
        }
    }

    private static LocalDate lerDataObrigatoria(String prompt) {
        while (true) {
            System.out.print(prompt);
            String linha = sc.nextLine().trim();
            try {
                return LocalDate.parse(linha);
            } catch (DateTimeParseException e) {
                System.out.println("Data inválida, use o formato AAAA-MM-DD.");
            }
        }
    }

    private static List<String> lerListaOpcional(String prompt) {
        System.out.print(prompt);
        String linha = sc.nextLine().trim();
        if (linha.isEmpty()) return null;
        return Arrays.stream(linha.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private static boolean confirmar(String prompt) {
        System.out.print(prompt);
        String linha = sc.nextLine().trim().toLowerCase();
        return linha.equals("s") || linha.equals("sim");
    }
}
