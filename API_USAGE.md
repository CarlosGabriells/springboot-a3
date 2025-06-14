# Sistema de Gerenciamento de Biblioteca - Guia de Uso da API

Este guia demonstra como usar a API do Sistema de Gerenciamento de Biblioteca usando requisições HTTP. O sistema fornece endpoints REST abrangentes para gerenciar livros, autores, membros, categorias e empréstimos.

## URL Base

```
http://localhost:8080/api
```

## Autenticação

A versão atual não requer autenticação. Todos os endpoints são publicamente acessíveis.

## Formato de Resposta

Todas as respostas seguem um formato consistente:
- **Endpoints Paginados**: Retornam dados envolvidos em um objeto Page com `content`, `page`, `size`, `totalElements`, etc.
- **Endpoints de Item Único**: Retornam o item diretamente
- **Respostas de Erro**: Retornam detalhes do erro com timestamp, status, tipo de erro e mensagem

## Gerenciamento de Autores

### Buscar Todos os Autores

```http
GET /api/authors
```

**Parâmetros de Consulta:**
- `page` (opcional): Número da página (padrão: 0)
- `size` (opcional): Tamanho da página (padrão: 10)

**Exemplo de Requisição:**
```bash
curl -X GET "http://localhost:8080/api/authors?page=0&size=5"
```

**Exemplo de Resposta:**
```json
{
  "content": [
    {
      "id": 1,
      "firstName": "Joanne",
      "lastName": "Rowling",
      "biography": "Autora britânica, mais conhecida pela série Harry Potter.",
      "birthDate": "1965-07-31",
      "nationality": "Britânica"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 5
  },
  "totalElements": 8,
  "totalPages": 2
}
```

### Buscar Autor por ID

```http
GET /api/authors/{id}
```

**Exemplo de Requisição:**
```bash
curl -X GET "http://localhost:8080/api/authors/1"
```

### Criar Novo Autor

```http
POST /api/authors
Content-Type: application/json
```

**Corpo da Requisição:**
```json
{
  "firstName": "Jane",
  "lastName": "Austen",
  "biography": "Romancista inglesa conhecida por seu humor e comentário social.",
  "birthDate": "1775-12-16",
  "nationality": "Britânica"
}
```

**Exemplo de Requisição:**
```bash
curl -X POST "http://localhost:8080/api/authors" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Austen",
    "biography": "Romancista inglesa conhecida por seu humor e comentário social.",
    "birthDate": "1775-12-16",
    "nationality": "Britânica"
  }'
```

### Atualizar Autor

```http
PUT /api/authors/{id}
Content-Type: application/json
```

**Exemplo de Requisição:**
```bash
curl -X PUT "http://localhost:8080/api/authors/1" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Joanne",
    "lastName": "Rowling",
    "biography": "Autora britânica, filantropa e roteirista.",
    "birthDate": "1965-07-31",
    "nationality": "Britânica"
  }'
```

### Deletar Autor

```http
DELETE /api/authors/{id}
```

**Exemplo de Requisição:**
```bash
curl -X DELETE "http://localhost:8080/api/authors/1"
```

### Buscar Autores por Nome

```http
GET /api/authors/search?name={palavra-chave}
```

**Exemplo de Requisição:**
```bash
curl -X GET "http://localhost:8080/api/authors/search?name=Rowling"
```

## Gerenciamento de Categorias

### Buscar Todas as Categorias

```http
GET /api/categories
```

**Exemplo de Requisição:**
```bash
curl -X GET "http://localhost:8080/api/categories"
```

### Criar Nova Categoria

```http
POST /api/categories
Content-Type: application/json
```

**Corpo da Requisição:**
```json
{
  "name": "Ficção Científica",
  "description": "Livros apresentando conceitos futuristas e tecnologia."
}
```

**Exemplo de Requisição:**
```bash
curl -X POST "http://localhost:8080/api/categories" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ficção Científica",
    "description": "Livros apresentando conceitos futuristas e tecnologia."
  }'
```

### Buscar Categoria por ID

```http
GET /api/categories/{id}
```

### Atualizar Categoria

```http
PUT /api/categories/{id}
Content-Type: application/json
```

### Deletar Categoria

```http
DELETE /api/categories/{id}
```

## Gerenciamento de Livros

### Buscar Todos os Livros

```http
GET /api/books
```

**Exemplo de Requisição:**
```bash
curl -X GET "http://localhost:8080/api/books?size=20"
```

**Exemplo de Resposta:**
```json
{
  "content": [
    {
      "id": 1,
      "isbn": "9780747532699",
      "title": "Harry Potter e a Pedra Filosofal",
      "description": "O primeiro livro da série Harry Potter.",
      "publicationDate": "1997-06-26",
      "totalCopies": 5,
      "availableCopies": 3,
      "authorId": 1,
      "author": {
        "id": 1,
        "firstName": "Joanne",
        "lastName": "Rowling"
      },
      "categoryIds": [1],
      "categories": [
        {
          "id": 1,
          "name": "Fantasia"
        }
      ]
    }
  ]
}
```

### Buscar Livro por ID

```http
GET /api/books/{id}
```

### Buscar Livro por ISBN

```http
GET /api/books/isbn/{isbn}
```

**Exemplo de Requisição:**
```bash
curl -X GET "http://localhost:8080/api/books/isbn/9780747532699"
```

### Criar Novo Livro

```http
POST /api/books
Content-Type: application/json
```

**Corpo da Requisição:**
```json
{
  "isbn": "9780451524935",
  "title": "1984",
  "description": "Um romance distópico de ficção científica social.",
  "publicationDate": "1949-06-08",
  "totalCopies": 6,
  "availableCopies": 4,
  "authorId": 2,
  "categoryIds": [2, 4]
}
```

**Exemplo de Requisição:**
```bash
curl -X POST "http://localhost:8080/api/books" \
  -H "Content-Type: application/json" \
  -d '{
    "isbn": "9780451524935",
    "title": "1984",
    "description": "Um romance distópico de ficção científica social.",
    "publicationDate": "1949-06-08",
    "totalCopies": 6,
    "availableCopies": 4,
    "authorId": 2,
    "categoryIds": [2, 4]
  }'
```

### Atualizar Livro

```http
PUT /api/books/{id}
Content-Type: application/json
```

### Deletar Livro

```http
DELETE /api/books/{id}
```

### Buscar Livros

```http
GET /api/books/search?keyword={termo}
```

**Exemplo de Requisição:**
```bash
curl -X GET "http://localhost:8080/api/books/search?keyword=Harry Potter"
```

### Buscar Livros Disponíveis

```http
GET /api/books/available
```

**Exemplo de Requisição:**
```bash
curl -X GET "http://localhost:8080/api/books/available"
```

### Buscar Livros por Categoria

```http
GET /api/books/category/{categoryId}
```

**Exemplo de Requisição:**
```bash
curl -X GET "http://localhost:8080/api/books/category/1"
```

## Gerenciamento de Membros

### Buscar Todos os Membros

```http
GET /api/members
```

**Exemplo de Resposta:**
```json
{
  "content": [
    {
      "id": 1,
      "firstName": "João",
      "lastName": "Silva",
      "email": "joao.silva@email.com",
      "phone": "+5511999000001",
      "address": "Rua Principal, 123, São Paulo, SP 01001-000",
      "membershipDate": "2023-01-15",
      "status": "ACTIVE"
    }
  ]
}
```

### Buscar Membro por ID

```http
GET /api/members/{id}
```

### Buscar Membro com Empréstimos

```http
GET /api/members/{id}/with-loans
```

**Exemplo de Requisição:**
```bash
curl -X GET "http://localhost:8080/api/members/1/with-loans"
```

### Buscar Membro por Email

```http
GET /api/members/email/{email}
```

**Exemplo de Requisição:**
```bash
curl -X GET "http://localhost:8080/api/members/email/joao.silva@email.com"
```

### Criar Novo Membro

```http
POST /api/members
Content-Type: application/json
```

**Corpo da Requisição:**
```json
{
  "firstName": "João",
  "lastName": "Silva",
  "email": "joao.silva@email.com",
  "phone": "+5511999000001",
  "address": "Rua Principal, 123, São Paulo, SP 01001-000",
  "membershipDate": "2023-01-15",
  "status": "ACTIVE"
}
```

### Atualizar Membro

```http
PUT /api/members/{id}
Content-Type: application/json
```

### Deletar Membro

```http
DELETE /api/members/{id}
```

### Buscar Membros por Nome

```http
GET /api/members/search?name={nome}
```

**Exemplo de Requisição:**
```bash
curl -X GET "http://localhost:8080/api/members/search?name=João"
```

### Buscar Membros por Status

```http
GET /api/members/status/{status}
```

**Status disponíveis:** `ACTIVE`, `SUSPENDED`, `EXPIRED`

**Exemplo de Requisição:**
```bash
curl -X GET "http://localhost:8080/api/members/status/ACTIVE"
```

### Atualizar Status do Membro

```http
PATCH /api/members/{id}/status?status={novoStatus}
```

**Exemplo de Requisição:**
```bash
curl -X PATCH "http://localhost:8080/api/members/1/status?status=SUSPENDED"
```

## Gerenciamento de Empréstimos

### Buscar Todos os Empréstimos

```http
GET /api/loans
```

**Exemplo de Resposta:**
```json
{
  "content": [
    {
      "id": 1,
      "loanDate": "2025-05-01",
      "dueDate": "2025-12-15",
      "returnDate": null,
      "status": "ACTIVE",
      "notes": null,
      "bookId": 1,
      "memberId": 1,
      "book": {
        "id": 1,
        "title": "Harry Potter e a Pedra Filosofal",
        "isbn": "9780747532699",
        "authorName": "Joanne Rowling"
      },
      "member": {
        "id": 1,
        "firstName": "João",
        "lastName": "Silva",
        "email": "joao.silva@email.com"
      }
    }
  ]
}
```

### Buscar Empréstimo por ID

```http
GET /api/loans/{id}
```

### Criar Novo Empréstimo

```http
POST /api/loans
Content-Type: application/json
```

**Corpo da Requisição:**
```json
{
  "loanDate": "2025-06-10",
  "dueDate": "2025-06-24",
  "status": "ACTIVE",
  "bookId": 1,
  "memberId": 1,
  "notes": "Primeira vez emprestando este livro"
}
```

**Exemplo de Requisição:**
```bash
curl -X POST "http://localhost:8080/api/loans" \
  -H "Content-Type: application/json" \
  -d '{
    "loanDate": "2025-06-10",
    "dueDate": "2025-06-24",
    "status": "ACTIVE",
    "bookId": 1,
    "memberId": 1,
    "notes": "Primeira vez emprestando este livro"
  }'
```

### Atualizar Empréstimo

```http
PUT /api/loans/{id}
Content-Type: application/json
```

### Deletar Empréstimo

```http
DELETE /api/loans/{id}
```

### Buscar Empréstimos por Status

```http
GET /api/loans/status/{status}
```

**Status disponíveis:** `ACTIVE`, `RETURNED`, `OVERDUE`

**Exemplo de Requisição:**
```bash
curl -X GET "http://localhost:8080/api/loans/status/ACTIVE"
```

### Buscar Empréstimos por Membro

```http
GET /api/loans/member/{memberId}
```

**Exemplo de Requisição:**
```bash
curl -X GET "http://localhost:8080/api/loans/member/1"
```

### Buscar Empréstimos por Livro

```http
GET /api/loans/book/{bookId}
```

### Devolver um Livro

```http
PATCH /api/loans/{id}/return
```

**Exemplo de Requisição:**
```bash
curl -X PATCH "http://localhost:8080/api/loans/1/return"
```

### Renovar um Empréstimo

```http
PATCH /api/loans/{id}/renew
```

**Exemplo de Requisição:**
```bash
curl -X PATCH "http://localhost:8080/api/loans/1/renew"
```

## Tratamento de Erros

### Códigos de Status HTTP Comuns

- **200 OK**: Operações GET, PUT bem-sucedidas
- **201 Created**: Operações POST bem-sucedidas
- **204 No Content**: Operações DELETE bem-sucedidas
- **400 Bad Request**: Dados de requisição inválidos ou violação de regra de negócio
- **404 Not Found**: Recurso não encontrado
- **500 Internal Server Error**: Erro do servidor

### Formato de Resposta de Erro

```json
{
  "timestamp": "2025-06-10T01:53:16.910511526",
  "status": 400,
  "error": "Erro de Lógica de Negócio",
  "message": "Livro com ISBN 9780747532699 já existe",
  "path": "/api/books",
  "validationErrors": null
}
```

### Resposta de Erro de Validação

```json
{
  "timestamp": "2025-06-10T01:53:16.910511526",
  "status": 400,
  "error": "Erro de Validação",
  "message": "Falha na validação",
  "path": "/api/books",
  "validationErrors": {
    "isbn": "ISBN é obrigatório",
    "title": "Título não deve exceder 200 caracteres"
  }
}
```

## Regras de Negócio

### Gerenciamento de Livros
- ISBN deve ser único em todo o sistema
- Livros devem ter pelo menos 1 cópia total
- Cópias disponíveis não podem exceder cópias totais
- Cópias disponíveis não podem ser negativas

### Gerenciamento de Membros
- Endereços de email devem ser únicos
- Apenas membros ATIVOS podem emprestar livros
- Membros não podem ser deletados se tiverem empréstimos ativos

### Gerenciamento de Empréstimos
- Membros podem ter no máximo 5 empréstimos ativos
- Período de empréstimo é automaticamente definido para 14 dias a partir da data do empréstimo
- Livros devem estar disponíveis (availableCopies > 0) para serem emprestados
- Apenas membros ATIVOS podem criar novos empréstimos
- Empréstimos em atraso são automaticamente detectados com base na data de vencimento

### Gerenciamento de Categorias e Autores
- Nomes de categorias devem ser únicos
- Autores não podem ser deletados se tiverem livros
- Categorias não podem ser deletadas se tiverem livros

## Testando a API

### Usando Exemplos com cURL

1. **Buscar todos os livros:**
```bash
curl -X GET "http://localhost:8080/api/books?size=20"
```

2. **Criar um novo membro:**
```bash
curl -X POST "http://localhost:8080/api/members" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Alice",
    "lastName": "Santos",
    "email": "alice.santos@email.com",
    "phone": "+5511999000123",
    "address": "Avenida das Flores, 456, Rio de Janeiro, RJ 20001-000",
    "membershipDate": "2025-06-10",
    "status": "ACTIVE"
  }'
```

3. **Buscar livros por palavra-chave:**
```bash
curl -X GET "http://localhost:8080/api/books/search?keyword=Harry"
```

4. **Buscar empréstimos ativos:**
```bash
curl -X GET "http://localhost:8080/api/loans/status/ACTIVE"
```

5. **Criar um novo empréstimo:**
```bash
curl -X POST "http://localhost:8080/api/loans" \
  -H "Content-Type: application/json" \
  -d '{
    "loanDate": "2025-06-10",
    "dueDate": "2025-06-24",
    "status": "ACTIVE",
    "bookId": 1,
    "memberId": 1
  }'
```

### Documentação da API

Para documentação interativa da API, visite:
```
http://localhost:8080/swagger-ui.html
```

Isso fornece uma interface abrangente para testar todos os endpoints com exemplos apropriados de requisição/resposta.

## Paginação

A maioria dos endpoints de lista suporta paginação:
- `page`: Número da página (baseado em 0, padrão: 0)
- `size`: Número de itens por página (padrão: 10, máx: 100)
- `sort`: Critério de ordenação (ex: `sort=title,asc` ou `sort=createdDate,desc`)

**Exemplo:**
```bash
curl -X GET "http://localhost:8080/api/books?page=1&size=5&sort=title,asc"
```

## Formatos de Data

Todas as datas devem ser fornecidas no formato ISO 8601: `YYYY-MM-DD`

**Exemplos:**
- `"2025-06-10"`
- `"1997-06-26"`
- `"1965-07-31"`