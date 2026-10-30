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
- **PostgreSQL** — banco de dados relacional.
- **Bean Validation** — validação de dados de entrada.
- **Lombok** — redução de código repetitivo.
- **JUnit 5** — testes automatizados.
- **Mockito** — criação de mocks e testes unitários.
- **Maven** — gerenciamento de dependências e build.

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
└── ...
```

Cada módulo concentra suas próprias entidades, DTOs, repositórios, serviços, controladores e exceções, favorecendo a organização e a manutenção do código.

## Endpoints

### Produtos

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/produto` | Cadastrar produto |
| GET | `/produto` | Listar produtos ativos |
| GET | `/produto/{produtoId}` | Buscar produto por ID |
| PUT | `/produto/{produtoId}` | Atualizar produto |
| PATCH | `/produto/{produtoId}` | Desativar produto |

### Fotografias

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/produto/{produtoId}/fotos` | Cadastrar fotografia |
| GET | `/produto/{produtoId}/fotos` | Listar fotografias do produto |
| GET | `/produto/{produtoId}/fotos/{fotoId}` | Buscar fotografia por ID |
| PUT | `/produto/{produtoId}/fotos/{fotoId}` | Atualizar fotografia |
| DELETE | `/produto/{produtoId}/fotos/{fotoId}` | Excluir fotografia |

## Como executar o projeto

### Pré-requisitos

- Java 25 ou superior.
- Maven.
- PostgreSQL.

### Configuração do banco de dados

Crie um banco de dados PostgreSQL chamado:

```sql
CREATE DATABASE db_bellatrama;
```

Configure as credenciais e a conexão com o banco no arquivo `application.yml`, de acordo com seu ambiente.

### Executando a aplicação

Clone o repositório:

```bash
git clone https://github.com/MuriloPizolito/BellaTrama.git
```

Acesse a pasta:

```bash
cd BellaTrama
```

Execute a aplicação com Maven:

```bash
./mvnw spring-boot:run
```

No Windows, utilize:

```bash
mvnw.cmd spring-boot:run
```

## Testes

O projeto utiliza JUnit 5 e Mockito para desenvolver testes unitários, validando as regras de negócio implementadas nos serviços.

Os testes são organizados com `@Nested`, agrupando os cenários de acordo com os métodos testados.

Para executar os testes:

```bash
./mvnw test
```

## Próximos passos

- Finalizar e ampliar a cobertura dos testes unitários.
- Implementar testes de integração com o banco de dados.
- Avaliar uma solução de armazenamento externo para as fotografias.
- Implementar autenticação e autorização.
- Desenvolver a integração com uma interface frontend.
- Avaliar a implantação da aplicação em ambiente de produção.

---

**Status:** Em desenvolvimento.

**Desenvolvido por [Murilo Pizolito](https://github.com/MuriloPizolito).**