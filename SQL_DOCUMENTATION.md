# Sistema de Gerenciamento de Biblioteca - Documentação SQL do Banco de Dados

## Visão Geral

Este documento fornece documentação SQL abrangente para o banco de dados do Sistema de Gerenciamento de Biblioteca, incluindo definições de esquema, relacionamentos, restrições, dados de amostra e todas as consultas utilizadas na aplicação.

## Esquema do Banco de Dados

### 1. Tabela Authors (Autores)

```sql
CREATE TABLE authors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    nationality VARCHAR(100),
    birth_date DATE,
    biography VARCHAR(1000)
);
```

**Índices:**
- Chave Primária: `id` (AUTO_INCREMENT)

**Restrições:**
- `first_name` e `last_name` são obrigatórios (NOT NULL)

### 2. Tabela Categories (Categorias)

```sql
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500)
);
```

**Índices:**
- Chave Primária: `id` (AUTO_INCREMENT)
- Índice Único: `name`

**Restrições:**
- `name` é obrigatório e único

### 3. Tabela Books (Livros)

```sql
CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    publication_date DATE NOT NULL,
    total_copies INT NOT NULL,
    available_copies INT NOT NULL,
    author_id BIGINT NOT NULL,
    FOREIGN KEY (author_id) REFERENCES authors(id)
);
```

**Índices:**
- Chave Primária: `id` (AUTO_INCREMENT)
- Índice Único: `isbn`
- Índice de Chave Estrangeira: `author_id`

**Restrições:**
- `isbn` é único e obrigatório
- `title`, `publication_date`, `total_copies`, `available_copies` são obrigatórios
- `author_id` referencia `authors.id`

### 4. Tabela de Junção Book-Category (Muitos-para-Muitos)

```sql
CREATE TABLE book_categories (
    book_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, category_id),
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);
```

**Índices:**
- Chave Primária Composta: `(book_id, category_id)`
- Índices de Chave Estrangeira: `book_id`, `category_id`

### 5. Tabela Members (Membros)

```sql
CREATE TABLE members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20),
    address VARCHAR(200),
    membership_date DATE NOT NULL,
    status ENUM('ACTIVE', 'SUSPENDED', 'EXPIRED') NOT NULL
);
```

**Índices:**
- Chave Primária: `id` (AUTO_INCREMENT)
- Índice Único: `email`

**Restrições:**
- `first_name`, `last_name`, `email`, `membership_date`, `status` são obrigatórios
- `email` deve ser único
- `status` deve ser um de: ACTIVE, SUSPENDED, EXPIRED

### 6. Tabela Loans (Empréstimos)

```sql
CREATE TABLE loans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    status ENUM('ACTIVE', 'RETURNED', 'OVERDUE') NOT NULL,
    notes VARCHAR(500),
    book_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    FOREIGN KEY (book_id) REFERENCES books(id),
    FOREIGN KEY (member_id) REFERENCES members(id)
);
```

**Índices:**
- Chave Primária: `id` (AUTO_INCREMENT)
- Índices de Chave Estrangeira: `book_id`, `member_id`

**Restrições:**
- `loan_date`, `due_date`, `status`, `book_id`, `member_id` são obrigatórios
- `status` deve ser um de: ACTIVE, RETURNED, OVERDUE
- `book_id` referencia `books.id`
- `member_id` referencia `members.id`

## Relacionamentos entre Entidades

### Diagrama de Relacionamentos (Textual)

```
Authors (1) ←→ (∞) Books
Books (∞) ←→ (∞) Categories (via book_categories)
Books (1) ←→ (∞) Loans
Members (1) ←→ (∞) Loans
```

### Relacionamentos Detalhados

1. **Author → Books**: Um-para-Muitos
   - Um autor pode escrever múltiplos livros
   - Cada livro tem exatamente um autor
   - Cascata: ALL (quando autor é deletado, todos seus livros são deletados)

2. **Book ↔ Categories**: Muitos-para-Muitos
   - Um livro pode pertencer a múltiplas categorias
   - Uma categoria pode conter múltiplos livros
   - Tabela de junção: `book_categories`

3. **Book → Loans**: Um-para-Muitos
   - Um livro pode ter múltiplos registros de empréstimo
   - Cada empréstimo é para exatamente um livro
   - Cascata: ALL (quando livro é deletado, todos registros de empréstimo são deletados)

4. **Member → Loans**: Um-para-Muitos
   - Um membro pode ter múltiplos empréstimos
   - Cada empréstimo pertence a exatamente um membro
   - Cascata: ALL (quando membro é deletado, todos seus registros de empréstimo são deletados)

## Consultas dos Repositórios

### Consultas do AuthorRepository

#### 1. Buscar Autores por Nome (Busca Insensível a Maiúsculas)
**JPQL:** `SELECT a FROM Author a WHERE LOWER(a.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(a.lastName) LIKE LOWER(CONCAT('%', :name, '%'))`

#### 2. Buscar Autores por Nacionalidade (Insensível a Maiúsculas)
**Método Spring Data JPA:** `findByNationalityIgnoreCase`

#### 3. Buscar Autor com Livros (Fetch Join)
**JPQL:** `SELECT a FROM Author a JOIN FETCH a.books WHERE a.id = :id`

#### 4. Buscar Autores com Mais de X Livros
**JPQL:** `SELECT a FROM Author a WHERE SIZE(a.books) > :bookCount`

### Consultas do BookRepository

#### 1. Buscar Livro por ISBN
**Método Spring Data JPA:** `findByIsbn`

**SQL:**
```sql
SELECT b.* FROM books b WHERE b.isbn = ?;
```

#### 2. Buscar Livros por Título (Busca Insensível a Maiúsculas)
**JPQL:** `SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))`

#### 3. Buscar Livro com Detalhes (Autor e Categorias)
**JPQL:** `SELECT b FROM Book b JOIN FETCH b.author JOIN FETCH b.categories WHERE b.id = :id`

#### 4. Buscar Livros por ID do Autor
**JPQL:** `SELECT b FROM Book b WHERE b.author.id = :authorId`

#### 5. Buscar Livros por ID da Categoria
**JPQL:** `SELECT b FROM Book b JOIN b.categories c WHERE c.id = :categoryId`

#### 6. Buscar Livros Disponíveis
**JPQL:** `SELECT b FROM Book b WHERE b.availableCopies > 0`

#### 7. Buscar Livros Indisponíveis
**JPQL:** `SELECT b FROM Book b WHERE b.availableCopies = 0`

#### 8. Verificar se ISBN Existe
**Método Spring Data JPA:** `existsByIsbn`

#### 9. Buscar Livros (Título ou Nome do Autor)
**JPQL:** `SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))`

### Consultas do CategoryRepository

#### 1. Buscar Categoria por Nome (Insensível a Maiúsculas)
**Método Spring Data JPA:** `findByNameIgnoreCase`

**SQL:**
```sql
SELECT c.* FROM categories c WHERE LOWER(c.name) = LOWER(?);
```

#### 2. Buscar Categorias por Nome (Busca Insensível a Maiúsculas)
**JPQL:** `SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))`

#### 3. Buscar Categoria com Livros (Fetch Join)
**JPQL:** `SELECT c FROM Category c JOIN FETCH c.books WHERE c.id = :id`

#### 4. Buscar Categorias com Mais de X Livros
**JPQL:** `SELECT c FROM Category c WHERE SIZE(c.books) > :bookCount`

#### 5. Verificar se Nome da Categoria Existe (Insensível a Maiúsculas)
**Método Spring Data JPA:** `existsByNameIgnoreCase`

**SQL:**
```sql
SELECT COUNT(*) > 0 FROM categories WHERE LOWER(name) = LOWER(?);
```

### Consultas do LoanRepository

#### 1. Buscar Empréstimo com Detalhes (Livro e Membro)
**JPQL:** `SELECT l FROM Loan l JOIN FETCH l.book JOIN FETCH l.member WHERE l.id = :id`

#### 2. Buscar Empréstimos por ID do Membro
**JPQL:** `SELECT l FROM Loan l WHERE l.member.id = :memberId`

#### 3. Buscar Empréstimos por ID do Livro
**JPQL:** `SELECT l FROM Loan l WHERE l.book.id = :bookId`

#### 4. Buscar Empréstimos por Status
**JPQL:** `SELECT l FROM Loan l WHERE l.status = :status`

#### 5. Buscar Empréstimos em Atraso
**JPQL:** `SELECT l FROM Loan l WHERE l.dueDate < :date AND l.status = 'ACTIVE'`

#### 6. Buscar Empréstimos Ativos por Membro
**JPQL:** `SELECT l FROM Loan l WHERE l.member.id = :memberId AND l.status = 'ACTIVE'`

#### 7. Buscar Empréstimos Ativos por Livro
**JPQL:** `SELECT l FROM Loan l WHERE l.book.id = :bookId AND l.status = 'ACTIVE'`

#### 8. Buscar Empréstimos por Intervalo de Datas
**JPQL:** `SELECT l FROM Loan l WHERE l.loanDate BETWEEN :startDate AND :endDate`

#### 9. Contar Empréstimos Ativos por Membro
**JPQL:** `SELECT COUNT(l) FROM Loan l WHERE l.member.id = :memberId AND l.status = 'ACTIVE'`

### Consultas do MemberRepository

#### 1. Buscar Membro por Email
**Método Spring Data JPA:** `findByEmail`

**SQL:**
```sql
SELECT m.* FROM members m WHERE m.email = ?;
```

#### 2. Buscar Membros por Nome (Busca Insensível a Maiúsculas)
**JPQL:** `SELECT m FROM Member m WHERE LOWER(m.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(m.lastName) LIKE LOWER(CONCAT('%', :name, '%'))`

#### 3. Buscar Membros por Status
**Método Spring Data JPA:** `findByStatus`

**SQL:**
```sql
SELECT m.* FROM members m WHERE m.status = ?;
```

#### 4. Buscar Membro com Empréstimos (Fetch Join)
**JPQL:** `SELECT m FROM Member m JOIN FETCH m.loans WHERE m.id = :id`

#### 5. Verificar se Email Existe
**Método Spring Data JPA:** `existsByEmail`

**SQL:**
```sql
SELECT COUNT(*) > 0 FROM members WHERE email = ?;
```

#### 6. Buscar Membros por Status (Paginado)
**JPQL:** `SELECT m FROM Member m WHERE m.status = :status`

#### 7. Buscar Membros com Mais de X Empréstimos
**JPQL:** `SELECT m FROM Member m WHERE SIZE(m.loans) > :loanCount`

## Regras de Negócio e Restrições

### 1. Regras de Gerenciamento de Empréstimos
- **Máximo de Empréstimos Ativos**: Cada membro pode ter no máximo 5 empréstimos ativos simultaneamente
- **Período de Empréstimo**: Período padrão de empréstimo é de 14 dias
- **Status do Membro**: Apenas membros ATIVOS podem pegar livros emprestados
- **Disponibilidade do Livro**: Livros só podem ser emprestados se `available_copies > 0`
- **Detecção de Atraso**: Empréstimos tornam-se EM ATRASO quando `due_date < data_atual` e status é ATIVO

### 2. Regras de Integridade de Dados
- **Restrições Únicas**: 
  - ISBN do livro deve ser único
  - Email do membro deve ser único
  - Nome da categoria deve ser único (insensível a maiúsculas)
- **Operações em Cascata**: 
  - Deletar um autor deleta todos seus livros
  - Deletar um livro deleta todos seus registros de empréstimo
  - Deletar um membro deleta todos seus registros de empréstimo
- **Integridade Referencial**: Todas as chaves estrangeiras são enforçadas

### 3. Regras de Validação
- **Campos Obrigatórios**: Todas as restrições NOT NULL devem ser satisfeitas
- **Formato de Email**: Endereços de email devem seguir formato válido de email
- **Validação de Data**: 
  - `due_date` deve ser posterior a `loan_date`
  - `return_date` (se fornecida) deve ser posterior a `loan_date`
- **Gerenciamento de Exemplares**: `available_copies` deve ser ≤ `total_copies`