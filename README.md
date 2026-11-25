# BellaTrama

**API REST para gerenciamento de catálogo de bolsas artesanais.**

O BellaTrama é um projeto pessoal desenvolvido com Java e Spring Boot, com o objetivo de construir uma API REST para gerenciar o catálogo de bolsas de crochê artesanal de um pequeno negócio familiar.

A aplicação busca organizar o cadastro de produtos, o controle de estoque e o gerenciamento de fotografias, aplicando regras de negócio que garantam a consistência das informações.

O projeto está em desenvolvimento e também tem como objetivo aprofundar conhecimentos em desenvolvimento backend com Java, arquitetura de aplicações, persistência de dados e testes automatizados.

## Tecnologias utilizadas

- **Java 25**
- **Spring Boot 4.1.1**
- **Spring Web** — desenvolvimento de APIs REST.
- **Spring Data JPA** — persistência e acesso aos dados.
- **Hibernate** — implementação ORM.
- **PostgreSQL 18** — banco de dados relacional.
- **Bean Validation** — validação de dados de entrada.
- **Lombok** — redução de código repetitivo.
- **JUnit 5** — testes automatizados.
- **Mockito** — criação de mocks e testes unitários.
- **Maven** — gerenciamento de dependências e build.
- **AssertJ** — biblioteca para asserções mais expressivas nos testes automatizados.
- **Testcontainers** — execução de testes de integração com PostgreSQL em contêineres Docker.
- **Docker** — ambiente de contêineres utilizado pelos testes de integração.

## Funcionalidades implementadas

### Produtos

- Cadastro de produtos.
- Consulta de produtos.
- Consulta de produto por id.
- Atualização de informações.
- Desativação lógica de produtos (*soft delete*).
- Validação dos dados recebidos pela API.

### Fotos

- Cadastro de fotos vinculadas a produtos.
- Listagem de fotos de um produto.
- Consulta de fotos por id.
- Atualização de URL e definição da foto principal.
- Exclusão de fotos.
- Promoção automática de uma foto substituta quando a principal é excluída.

## Regras de negócio

O projeto possui regras específicas para manter a consistência do catálogo:

**Produtos**
- Produtos podem ser desativados sem serem removidos fisicamente do banco de dados.
- Produtos desativados não devem aparecer nas consultas públicas do catálogo.
- Produtos com estoque zerado continuam ativos e podem permanecer visíveis.
- Produtos desativados são tratados como inexistentes nas operações de consulta, atualização e desativação, retornando HTTP 404.

**Fotografias**
- Um produto pode possuir várias fotografias.
- A primeira fotografia cadastrada é definida automaticamente como principal.
- Cada produto deve possuir, no máximo, uma fotografia principal.
- Não é permitido desmarcar a fotografia principal sem definir outra como principal.
- Ao excluir a fotografia principal, a fotografia mais antiga entre as restantes é promovida automaticamente.
- A seleção da fotografia substituta considera a data de cadastro em ordem crescente e utiliza o identificador como critério de desempate.
- Caso a única fotografia principal seja excluída, o produto permanece sem fotografias.

## Estrutura do projeto

A aplicação está organizada por domínio, separando as responsabilidades de cada módulo.

```text
br.com.murilo.bellatrama
├── dominio
│   ├── produto
│   │   ├── controller
│   │   ├── dto
│   │   ├── exception
│   │   ├── model
│   │   ├── repository
│   │   └── service
│   │
│   └── foto
│       ├── controller
│       ├── dto
│       ├── exception
│       ├── model
│       ├── repository
│       └── service
│
└── exception
    ├── GlobalExceptionHandler.java
    └── ErrorResponse.java
```

Cada módulo concentra suas próprias entidades, DTOs, repositórios, serviços, controladores e exceções, favorecendo a organização e a manutenção do código.

## Endpoints

### Produtos

| Método | Endpoint                | Descrição |
|---|-------------------------|---|
| POST | `/produtos`             | Cadastrar produto |
| GET | `/produtos`             | Listar produtos ativos |
| GET | `/produtos/{produtoId}` | Buscar produto por ID |
| PUT | `/produtos/{produtoId}` | Atualizar produto |
| PATCH | `/produtos/{produtoId}` | Desativar produto (exclusão lógica)|

### Fotografias

| Método | Endpoint                               | Descrição |
|---|----------------------------------------|---|
| POST | `/produtos/{produtoId}/fotos`          | Cadastrar fotografia |
| GET | `/produtos/{produtoId}/fotos`          | Listar fotografias do produto |
| GET | `/produtos/{produtoId}/fotos/{fotoId}` | Buscar fotografia por ID |
| PUT | `/produtos/{produtoId}/fotos/{fotoId}` | Atualizar fotografia |
| DELETE | `/produtos/{produtoId}/fotos/{fotoId}` | Excluir fotografia |

**Observação:** os endpoints de cadastro (`POST`) retornam HTTP 201 (Created), incluindo o header `Location` com o endereço do recurso criado.


## Tratamento de erros

A aplicação utiliza um tratamento global de exceções por meio do `GlobalExceptionHandler`, implementado com `@RestControllerAdvice`.

As respostas de erro seguem um formato padronizado, representado pela classe `ErrorResponse`:

| Campo | Descrição |
|---|---|
| `status` | Código HTTP da resposta |
| `mensagem` | Descrição do erro ocorrido |
| `erros` | Mapa com os campos inválidos e suas respectivas mensagens de validação. Pode ser `null` quando não se aplica. |

### Exemplo de erro de validação (HTTP 400)

    {
      "status": 400,
      "mensagem": "Existem campos inválidos na requisição.",
      "erros": {
        "nome": "não deve estar em branco",
        "preco": "deve ser maior que zero"
      }
    }

### Exemplo de recurso não encontrado (HTTP 404)

    {
      "status": 404,
      "mensagem": "Produto não encontrado.",
      "erros": null
    }

As mensagens apresentadas são exemplos ilustrativos. O conteúdo retornado depende da exceção e das validações acionadas pela requisição.


## Como executar o projeto

### Pré-requisitos

- Java 25 ou superior.
- PostgreSQL.
- Docker Desktop (necessário para executar os testes com Testcontainers).

O Maven Wrapper já está incluído no projeto, não sendo necessário instalar o Maven separadamente.

### Configuração do banco de dados

Crie um banco de dados PostgreSQL com o nome de sua preferência.

A conexão com o banco de dados é configurada por meio de variáveis de ambiente, evitando armazenar credenciais diretamente nos arquivos versionados.

O arquivo `.env.example`, disponível na raiz do projeto, apresenta um modelo das variáveis necessárias:

**Importante:** o arquivo `.env.example` serve apenas como modelo das variáveis necessárias. O Spring Boot não carrega arquivos `.env` automaticamente. Portanto, os valores devem ser definidos diretamente como variáveis de ambiente no sistema operacional ou na configuração de execução da IDE.

```properties
DB_URL=jdbc:postgresql://localhost:5432/bellatrama
DB_USERNAME=seu_usuario
DB_PASSWORD=sua_senha
SPRING_PROFILES_ACTIVE=dev
```
**Variáveis de ambiente:**

| Variável | Descrição |
|---|---|
| `DB_URL` | URL de conexão JDBC com o PostgreSQL |
| `DB_USERNAME` | Usuário do banco de dados |
| `DB_PASSWORD` | Senha do banco de dados |
| `SPRING_PROFILES_ACTIVE` | Perfil de execução (`dev` ou `prod`) |

### Perfis da aplicação

A aplicação possui dois perfis:

- **dev:** utilizado durante o desenvolvimento, com atualização automática do esquema pelo Hibernate (`ddl-auto: update`) e exibição de SQL.
- **prod:** destinado à produção, com validação do esquema pelo Hibernate (`ddl-auto: validate`) e sem exibição de SQL.

O perfil padrão é `dev`. Para selecionar outro perfil, configure a variável `SPRING_PROFILES_ACTIVE`.

**Atenção:** em ambiente de produção, configure explicitamente `SPRING_PROFILES_ACTIVE=prod`. Atualmente, o perfil `prod` depende de um esquema de banco de dados previamente criado, pois as migrações com Flyway ainda não foram implementadas.

### Executando a aplicação

Clone o repositório:

```bash
git clone https://github.com/MuriloPizolito/BellaTrama.git
```

Acesse a pasta:

```bash
cd BellaTrama
```

Configure as variáveis de ambiente necessárias antes de iniciar a aplicação.

**Windows (PowerShell):**

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/bellatrama"
$env:DB_USERNAME="seu_usuario"
$env:DB_PASSWORD="sua_senha"
$env:SPRING_PROFILES_ACTIVE="dev"
```

**Linux e macOS:**

```bash
export DB_URL="jdbc:postgresql://localhost:5432/bellatrama"
export DB_USERNAME="seu_usuario"
export DB_PASSWORD="sua_senha"
export SPRING_PROFILES_ACTIVE="dev"
```

Execute a aplicação:

**Windows (PowerShell):**

```powershell
.\mvnw.cmd spring-boot:run
```

**Linux e macOS:**

```bash
./mvnw spring-boot:run
```

## Testes

O projeto utiliza JUnit 5, Mockito e AssertJ para a implementação de testes automatizados, organizados em três camadas:

- **Testes unitários:** validam as regras de negócio dos serviços, utilizando Mockito para simulação de dependências e AssertJ para asserções.
- **Testes da camada web:** utilizam `@WebMvcTest` para verificar os endpoints, os códigos HTTP, as validações e o tratamento global de exceções.
- **Testes de integração:** utilizam Testcontainers com PostgreSQL 18 para validar o comportamento dos repositórios e a inicialização do contexto da aplicação.

Os testes de integração utilizam contêineres Docker, sendo necessário que o Docker esteja em execução.

Os testes são organizados com `@Nested`, agrupando os cenários de acordo com os métodos testados.

Para executar a suíte de testes:

**Windows (PowerShell):**

```powershell
.\mvnw.cmd test
```

**Linux e macOS:**

```bash
./mvnw test
```

## Roadmap

O desenvolvimento do projeto está organizado em etapas, desde a
implementação da API até a publicação da aplicação.

Confira o planejamento e o progresso das funcionalidades em
[ROADMAP.md](./ROADMAP.md).

## Coleção do Postman

A coleção de requisições da API BellaTrama está disponível no arquivo:

[ProjetoBellaTrama.postman_collection.json](docs/postman/ProjetoBellaTrama.postman_collection.json)

A coleção contempla os endpoints de **Produtos e Fotos**, incluindo cenários de sucesso e de erros HTTP 400 e 404.

### Como utilizar

1. Importe o arquivo JSON no Postman.
2. A URL base já está configurada como `http://localhost:8080`.
3. Execute a requisição **Cadastrar produto**, na pasta Produtos. O identificador do produto será armazenado automaticamente na variável `produtoId`.
4. Execute a requisição **Cadastrar foto**, na pasta Fotos. O identificador da foto também será armazenado automaticamente na variável `fotoId`.
5. Execute as demais requisições disponíveis nas pastas Produtos e Fotos.

### Preenchimento automático de variáveis

A coleção utiliza scripts na aba **Tests** das requisições de cadastro para capturar os identificadores retornados pela API e armazená-los nas variáveis de coleção:

- `produtoId`: preenchida automaticamente após o cadastro de um produto.
- `fotoId`: preenchida automaticamente após o cadastro de uma foto.

As variáveis `produtoIdInexistente` e `fotoIdInexistente` possuem identificadores previamente configurados para testar os cenários de erro HTTP 404.

---

**Status:** Em desenvolvimento.

**Desenvolvido por [Murilo Pizolito](https://github.com/MuriloPizolito).**