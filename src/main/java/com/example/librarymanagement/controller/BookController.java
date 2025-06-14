package com.example.librarymanagement.controller;

import com.example.librarymanagement.dto.BookDto;
import com.example.librarymanagement.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST responsável pelos endpoints de gerenciamento de livros.
 * Fornece operações CRUD completas e consultas específicas para livros através de API REST.
 * Inclui funcionalidades de busca, filtragem por categoria e verificação de disponibilidade.
 * Todos os endpoints retornam dados no formato JSON e seguem as convenções REST.
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "Book management endpoints")
public class BookController {

    private final BookService bookService;

    /**
     * Endpoint para buscar todos os livros com paginação.
     * @param pageable configurações de paginação (tamanho padrão: 10)
     * @return ResponseEntity com página de livros e status HTTP 200 (OK)
     */
    @GetMapping
    @Operation(summary = "Get all books", description = "Retrieve all books with pagination")
    public ResponseEntity<Page<BookDto>> getAllBooks(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<BookDto> books = bookService.findAll(pageable);
        return ResponseEntity.ok(books);
    }

    /**
     * Endpoint para buscar um livro específico pelo ID.
     * @param id identificador único do livro
     * @return ResponseEntity com dados do livro e status HTTP 200 (OK)
     * @throws ResourceNotFoundException se o livro não for encontrado (HTTP 404)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID", description = "Retrieve a specific book by ID")
    public ResponseEntity<BookDto> getBookById(
            @Parameter(description = "Book ID") @PathVariable Long id) {
        BookDto book = bookService.findById(id);
        return ResponseEntity.ok(book);
    }

    /**
     * Endpoint para buscar um livro específico pelo ISBN.
     * @param isbn código ISBN único do livro
     * @return ResponseEntity com dados do livro e status HTTP 200 (OK)
     * @throws ResourceNotFoundException se o livro não for encontrado (HTTP 404)
     */
    @GetMapping("/isbn/{isbn}")
    @Operation(summary = "Get book by ISBN", description = "Retrieve a specific book by ISBN")
    public ResponseEntity<BookDto> getBookByIsbn(
            @Parameter(description = "Book ISBN") @PathVariable String isbn) {
        BookDto book = bookService.findByIsbn(isbn);
        return ResponseEntity.ok(book);
    }

    /**
     * Endpoint para criar um novo livro no sistema.
     * @param bookDto dados do livro a ser criado (validados automaticamente)
     * @return ResponseEntity com dados do livro criado e status HTTP 201 (CREATED)
     * @throws ValidationException se os dados fornecidos forem inválidos (HTTP 400)
     * @throws BusinessException se o ISBN já existir no sistema (HTTP 409)
     */
    @PostMapping
    @Operation(summary = "Create book", description = "Create a new book")
    public ResponseEntity<BookDto> createBook(@Valid @RequestBody BookDto bookDto) {
        BookDto createdBook = bookService.create(bookDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    /**
     * Endpoint para atualizar um livro existente.
     * @param id identificador único do livro a ser atualizado
     * @param bookDto novos dados do livro (validados automaticamente)
     * @return ResponseEntity com dados do livro atualizado e status HTTP 200 (OK)
     * @throws ResourceNotFoundException se o livro não for encontrado (HTTP 404)
     * @throws ValidationException se os dados fornecidos forem inválidos (HTTP 400)
     * @throws BusinessException se o ISBN já existir para outro livro (HTTP 409)
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update book", description = "Update an existing book")
    public ResponseEntity<BookDto> updateBook(
            @Parameter(description = "Book ID") @PathVariable Long id,
            @Valid @RequestBody BookDto bookDto) {
        BookDto updatedBook = bookService.update(id, bookDto);
        return ResponseEntity.ok(updatedBook);
    }

    /**
     * Endpoint para remover um livro do sistema.
     * @param id identificador único do livro a ser removido
     * @return ResponseEntity vazio com status HTTP 204 (NO CONTENT)
     * @throws ResourceNotFoundException se o livro não for encontrado (HTTP 404)
     * @throws BusinessException se o livro possui empréstimos ativos (HTTP 409)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete book", description = "Delete a book by ID")
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "Book ID") @PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint para buscar livros por palavra-chave (título ou autor).
     * Realiza busca parcial e ignora maiúsculas/minúsculas.
     * @param keyword palavra-chave para busca no título ou nome do autor
     * @param pageable configurações de paginação (tamanho padrão: 10)
     * @return ResponseEntity com página de livros que correspondem ao critério e status HTTP 200 (OK)
     */
    @GetMapping("/search")
    @Operation(summary = "Search books", description = "Search books by keyword (title, author)")
    public ResponseEntity<Page<BookDto>> searchBooks(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<BookDto> books = bookService.searchBooks(keyword, pageable);
        return ResponseEntity.ok(books);
    }

    /**
     * Endpoint para buscar todos os livros disponíveis para empréstimo.
     * Retorna apenas livros que não estão atualmente emprestados.
     * @param pageable configurações de paginação (tamanho padrão: 10)
     * @return ResponseEntity com página de livros disponíveis e status HTTP 200 (OK)
     */
    @GetMapping("/available")
    @Operation(summary = "Get available books", description = "Retrieve all available books")
    public ResponseEntity<Page<BookDto>> getAvailableBooks(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<BookDto> books = bookService.findAvailableBooks(pageable);
        return ResponseEntity.ok(books);
    }

    /**
     * Endpoint para buscar livros por categoria específica.
     * @param categoryId identificador único da categoria
     * @param pageable configurações de paginação (tamanho padrão: 10)
     * @return ResponseEntity com página de livros da categoria especificada e status HTTP 200 (OK)
     * @throws ResourceNotFoundException se a categoria não for encontrada (HTTP 404)
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get books by category", description = "Retrieve books by category")
    public ResponseEntity<Page<BookDto>> getBooksByCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<BookDto> books = bookService.findByCategory(categoryId, pageable);
        return ResponseEntity.ok(books);
    }
}