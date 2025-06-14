package com.example.librarymanagement.controller;

import com.example.librarymanagement.dto.AuthorDto;
import com.example.librarymanagement.service.AuthorService;
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

import java.util.List;

/**
 * Controlador REST responsável pelos endpoints de gerenciamento de autores.
 * Fornece operações CRUD completas e consultas específicas para autores através de API REST.
 * Todos os endpoints retornam dados no formato JSON e seguem as convenções REST.
 */
@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
@Tag(name = "Authors", description = "Author management endpoints")
public class AuthorController {

    private final AuthorService authorService;

    /**
     * Endpoint para buscar todos os autores com paginação.
     * @param pageable configurações de paginação (tamanho padrão: 10)
     * @return ResponseEntity com página de autores e status HTTP 200 (OK)
     */
    @GetMapping
    @Operation(summary = "Get all authors", description = "Retrieve all authors with pagination")
    public ResponseEntity<Page<AuthorDto>> getAllAuthors(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<AuthorDto> authors = authorService.findAll(pageable);
        return ResponseEntity.ok(authors);
    }

    /**
     * Endpoint para buscar um autor específico pelo ID.
     * @param id identificador único do autor
     * @return ResponseEntity com dados do autor e status HTTP 200 (OK)
     * @throws ResourceNotFoundException se o autor não for encontrado (HTTP 404)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get author by ID", description = "Retrieve a specific author by ID")
    public ResponseEntity<AuthorDto> getAuthorById(
            @Parameter(description = "Author ID") @PathVariable Long id) {
        AuthorDto author = authorService.findById(id);
        return ResponseEntity.ok(author);
    }

    /**
     * Endpoint para buscar um autor específico incluindo seus livros.
     * @param id identificador único do autor
     * @return ResponseEntity com dados do autor e lista de livros, status HTTP 200 (OK)
     * @throws ResourceNotFoundException se o autor não for encontrado (HTTP 404)
     */
    @GetMapping("/{id}/with-books")
    @Operation(summary = "Get author with books", description = "Retrieve a specific author with their books")
    public ResponseEntity<AuthorDto> getAuthorWithBooks(
            @Parameter(description = "Author ID") @PathVariable Long id) {
        AuthorDto author = authorService.findByIdWithBooks(id);
        return ResponseEntity.ok(author);
    }

    /**
     * Endpoint para criar um novo autor no sistema.
     * @param authorDto dados do autor a ser criado (validados automaticamente)
     * @return ResponseEntity com dados do autor criado e status HTTP 201 (CREATED)
     * @throws ValidationException se os dados fornecidos forem inválidos (HTTP 400)
     */
    @PostMapping
    @Operation(summary = "Create author", description = "Create a new author")
    public ResponseEntity<AuthorDto> createAuthor(@Valid @RequestBody AuthorDto authorDto) {
        AuthorDto createdAuthor = authorService.create(authorDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAuthor);
    }

    /**
     * Endpoint para atualizar um autor existente.
     * @param id identificador único do autor a ser atualizado
     * @param authorDto novos dados do autor (validados automaticamente)
     * @return ResponseEntity com dados do autor atualizado e status HTTP 200 (OK)
     * @throws ResourceNotFoundException se o autor não for encontrado (HTTP 404)
     * @throws ValidationException se os dados fornecidos forem inválidos (HTTP 400)
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update author", description = "Update an existing author")
    public ResponseEntity<AuthorDto> updateAuthor(
            @Parameter(description = "Author ID") @PathVariable Long id,
            @Valid @RequestBody AuthorDto authorDto) {
        AuthorDto updatedAuthor = authorService.update(id, authorDto);
        return ResponseEntity.ok(updatedAuthor);
    }

    /**
     * Endpoint para remover um autor do sistema.
     * @param id identificador único do autor a ser removido
     * @return ResponseEntity vazio com status HTTP 204 (NO CONTENT)
     * @throws ResourceNotFoundException se o autor não for encontrado (HTTP 404)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete author", description = "Delete an author by ID")
    public ResponseEntity<Void> deleteAuthor(
            @Parameter(description = "Author ID") @PathVariable Long id) {
        authorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint para buscar autores por nome (busca parcial, ignora maiúsculas/minúsculas).
     * @param name nome ou parte do nome do autor para busca
     * @param pageable configurações de paginação (tamanho padrão: 10)
     * @return ResponseEntity com página de autores que correspondem ao critério e status HTTP 200 (OK)
     */
    @GetMapping("/search")
    @Operation(summary = "Search authors", description = "Search authors by name")
    public ResponseEntity<Page<AuthorDto>> searchAuthors(
            @Parameter(description = "Name to search") @RequestParam String name,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<AuthorDto> authors = authorService.searchByName(name, pageable);
        return ResponseEntity.ok(authors);
    }

    /**
     * Endpoint para buscar autores por nacionalidade (ignora maiúsculas/minúsculas).
     * @param nationality nacionalidade dos autores a serem buscados
     * @return ResponseEntity com lista de autores da nacionalidade especificada e status HTTP 200 (OK)
     */
    @GetMapping("/nationality/{nationality}")
    @Operation(summary = "Get authors by nationality", description = "Retrieve authors by nationality")
    public ResponseEntity<List<AuthorDto>> getAuthorsByNationality(
            @Parameter(description = "Nationality") @PathVariable String nationality) {
        List<AuthorDto> authors = authorService.findByNationality(nationality);
        return ResponseEntity.ok(authors);
    }
}