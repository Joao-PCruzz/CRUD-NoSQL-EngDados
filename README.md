# CRUD NoSQL - UFS 🍃

Aplicação em Java (JavaFX + MongoDB) para gerenciamento acadêmico de uma universidade, com persistência em banco de dados NoSQL MongoDB. Projeto desenvolvido para a disciplina de Engenharia de Dados.

## Funcionalidades 🚀

Baseado no modelo acadêmico disponibilizado pelo professor, desenvolvemos uma aplicação CRUD utilizando MongoDB como mecanismo de persistência, explorando conceitos de bancos de dados orientados a documentos.

* **Gestão de Estudantes**: cadastro, consulta, atualização e remoção de estudantes.
* **Gestão de Cursos**: gerenciamento completo dos cursos ofertados pela universidade.
* **Gestão de Vínculos**: associação entre estudantes e cursos.
* **Validação de Dados**: utilização de validadores JSON Schema no MongoDB para garantir integridade dos documentos.
* **Controle de Identificadores**: implementação de auto incremento através de coleção auxiliar (`counters`).

## Tecnologias 💻

| Tecnologia          | Versão  |
| ------------------- | ------- |
| Java                | 17      |
| JavaFX              | 21      |
| MongoDB             | 7+      |
| MongoDB Java Driver | 5.1.0   |
| Maven               | -       |
| Banco de Dados      | MongoDB |

## Arquitetura 🗂️

O projeto segue uma separação em camadas para facilitar manutenção e organização do código:

```text
src/main/java/
├── view/              → Inicialização da aplicação JavaFX
├── controller/        → Controladores das telas FXML
├── model/             → Classes de domínio (Curso, Estudante, Matrícula, etc.)
└── dao/               → Camada de acesso ao MongoDB

src/main/resources/
├── GUI/               → Interfaces gráficas FXML
└── Imagens/           → Recursos visuais da aplicação

mongodb-validators/
├── cursos.json
├── estudantes.json
└── turmas.json
```

## Camada de Persistência 🗄️

A comunicação com o MongoDB é centralizada na camada DAO.

A classe base responsável pela conexão cria um único cliente MongoDB compartilhado por toda a aplicação, evitando múltiplas conexões desnecessárias e reduzindo o consumo de recursos.

Cada entidade possui seu próprio DAO especializado para:

* Inserção de documentos;
* Consultas;
* Atualizações;
* Remoções;
* Validações específicas de negócio.

## Modelagem NoSQL 📄

O sistema utiliza coleções MongoDB para armazenar os dados acadêmicos.

Exemplos de coleções:

* `cursos`
* `estudantes`
* `turmas`
* `counters`

A coleção `counters` é utilizada para simular auto incremento de identificadores, funcionalidade comum em bancos relacionais mas inexistente nativamente no MongoDB.

Sempre que um novo curso é inserido, o valor é incrementado automaticamente.

## Validação de Dados ✅

O projeto utiliza validadores JSON Schema configurados diretamente no MongoDB.

Esses validadores garantem:

* Existência dos campos obrigatórios;
* Tipos corretos dos atributos;
* Restrições de domínio;
* Integridade dos documentos armazenados.

Os arquivos de validação encontram-se na pasta:

```text
mongodb-validators/
```

## Configuração do Banco de Dados ⚙️

A aplicação espera uma instância MongoDB em execução.

Exemplo de configuração:

| Variável         | Descrição                    |
| ---------------- | ---------------------------- |
| `MONGO_URI`      | String de conexão do MongoDB |
| `MONGO_DATABASE` | Nome do banco utilizado      |

Exemplo:

```text
mongodb://localhost:27017
```

Banco:

```text
universidade
```

Caso as configurações estejam definidas diretamente no código, ajuste os parâmetros de conexão antes da execução.

## Como Executar 💡

### 1. Inicie o MongoDB

Certifique-se de que o serviço MongoDB esteja em execução.

### 2. Crie o banco de dados

Crie o banco:

```text
universidade
```

### 3. Configure os validadores

Execute os scripts JSON presentes na pasta:

```text
mongodb-validators/
```

para criar as coleções com suas respectivas regras de validação.

### 4. Compile o projeto

```bash
mvn clean compile
```

### 5. Execute a aplicação

```bash
mvn javafx:run
```

ou execute a classe principal diretamente pela IDE.

## Estrutura das Coleções 🛠️
As coleções foram criadas usando do método de orientação a docuemntos, que consiste em utilizar arquivos .json, onde cada documento possuem seus campos e valores definidos. 

Para o mapeamento, empregamos as lógicas da incorporação (Embedding) e da Referência (Referencing).

A logica da incorporação consiste em incluir documentos relacionados dentro de um único documento, facilitando operações que dependem de FK's. Já a lógica da referência consiste em armazenar apenas o identificador (ID) de um documento relacionado, o que dificulda opeações que necessitam de consultas constantes, entretanto deixa as tabelas mais autônomas.

### Cursos

Armazena informações dos cursos oferecidos pela universidade.

Exemplos de atributos:

* idCurso
* nome
* grau
* turno
* campus
* nível

### Estudantes

Armazena informações dos estudantes cadastrados.

Exemplos de atributos:

* matricula estuadnte
* cpf
* MC
* data de ingresso

### Usuário

Armazena informações de usuários que se ligam a estudantes ou professores (tabela desnormalizada no mapeamento).

* cpf
* nome
* data_nascimento
* email
* telefone
* login
* senha

### Vínculos

Representa a relação entre estudantes e cursos (tabela desnormalizada no mapeamento).

Exemplos de atributos:

* idVinculo
* mat_estudante
* curso
* status
* data_entrada
* data_saida

## Autores 👥

* Bernardo Abrahão Nóbrega;
* João Pedro Costa Cruz;
* Lucas Antônio Araújo Santos.