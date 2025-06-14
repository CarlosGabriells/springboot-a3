package com.example.librarymanagement.controller;

import com.example.librarymanagement.dto.CategoryDto;
import com.example.librarymanagement.service.CategoryService;
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
 * Controlador REST responsável pelos endpoints de gerenciamento de categorias.
 * Fornece operações CRUD completas e consultas específicas para categorias através de API REST.
 * Inclui funcionalidades de busca por nome e listagem com/sem paginação.
 * Todos os endpoints retornam dados no formato JSON e seguem as convenções REST.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management endpoints")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Endpoint para buscar todas as categorias com paginação.
     * @param pageable configurações de paginação (tamanho padrão: 10)
     * @return ResponseEntity com página de categorias e status HTTP 200 (OK)
     */
    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieve all categories with pagination")
    public ResponseEntity<Page<CategoryDto>> getAllCategories(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<CategoryDto> categories = categoryService.findAll(pageable);
        return ResponseEntity.ok(categories);
    }

    /**
     * Endpoint para buscar todas as categorias sem paginação.
     * Útil para população de dropdowns e seleção de categorias.
     * @return ResponseEntity com lista completa de categorias e status HTTP 200 (OK)
     */
    @GetMapping("/all")
    @Operation(summary = "Get all categories without pagination", description = "Retrieve all categories without pagination")
    public ResponseEntity<List<CategoryDto>> getAllCategoriesWithoutPagination() {
        List<CategoryDto> categories = categoryService.findAll();
        return ResponseEntity.ok(categories);
    }

    /**
     * Endpoint para buscar uma categoria específica pelo ID.
     * @param id identificador único da categoria
     * @return ResponseEntity com dados da categoria e status HTTP 200 (OK)
     * @throws ResourceNotFoundException se a categoria não for encontrada (HTTP 404)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieve a specific category by ID")
    public ResponseEntity<CategoryDto> getCategoryById(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        CategoryDto category = categoryService.findById(id);
        return ResponseEntity.ok(category);
    }

    /**
     * Endpoint para buscar uma categoria específica incluindo seus livros.
     * @param id identificador único da categoria
     * @return ResponseEntity com dados da categoria e lista de livros, status HTTP 200 (OK)
     * @throws ResourceNotFoundException se a categoria não for encontrada (HTTP 404)
     */
    @GetMapping("/{id}/with-books")
    @Operation(summary = "Get category with books", description = "Retrieve a specific category with its books")
    public ResponseEntity<CategoryDto> getCategoryWithBooks(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        CategoryDto category = categoryService.findByIdWithBooks(id);
        return ResponseEntity.ok(category);
    }

    /**
     * Endpoint para buscar uma categoria específica pelo nome.
     * @param name nome exato da categoria
     * @return ResponseEntity com dados da categoria e status HTTP 200 (OK)
     * @throws ResourceNotFoundException se a categoria não for encontrada (HTTP 404)
     */
    @GetMapping("/name/{name}")
    @Operation(summary = "Get category by name", description = "Retrieve a specific category by name")
    public ResponseEntity<CategoryDto> getCategoryByName(
            @Parameter(description = "Category name") @PathVariable String name) {
        CategoryDto category = categoryService.findByName(name);
        return ResponseEntity.ok(category);
    }

    /**
     * Endpoint para criar uma nova categoria no sistema.
     * @param categoryDto dados da categoria a ser criada (validados automaticamente)
     * @return ResponseEntity com dados da categoria criada e status HTTP 201 (CREATED)
     * @throws ValidationException se os dados fornecidos forem inválidos (HTTP 400)
     * @throws BusinessException se o nome da categoria já existir (HTTP 409)
     */
    @PostMapping
    @Operation(summary = "Create category", description = "Create a new category")
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto categoryDto) {
        CategoryDto createdCategory = categoryService.create(categoryDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    /**
     * Endpoint para atualizar uma categoria existente.
     * @param id identificador único da categoria a ser atualizada
     * @param categoryDto novos dados da categoria (validados automaticamente)
     * @return ResponseEntity com dados da categoria atualizada e status HTTP 200 (OK)
     * @throws ResourceNotFoundException se a categoria não for encontrada (HTTP 404)
     * @throws ValidationException se os dados fornecidos forem inválidos (HTTP 400)
     * @throws BusinessException se o nome já existir para outra categoria (HTTP 409)
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Update an existing category")
    public ResponseEntity<CategoryDto> updateCategory(
            @Parameter(description = "Category ID") @PathVariable Long id,
            @Valid @RequestBody CategoryDto categoryDto) {
        CategoryDto updatedCategory = categoryService.update(id, categoryDto);
        return ResponseEntity.ok(updatedCategory);
    }

    /**
     * Endpoint para remover uma categoria do sistema.
     * @param id identificador único da categoria a ser removida
     * @return ResponseEntity vazio com status HTTP 204 (NO CONTENT)
     * @throws ResourceNotFoundException se a categoria não for encontrada (HTTP 404)
     * @throws BusinessException se a categoria possui livros associados (HTTP 409)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Delete a category by ID")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint para buscar categorias por nome (busca parcial, ignora maiúsculas/minúsculas).
     * @param name nome ou parte do nome da categoria para busca
     * @param pageable configurações de paginação (tamanho padrão: 10)
     * @return ResponseEntity com página de categorias que correspondem ao critério e status HTTP 200 (OK)
     */
    @GetMapping("/search")
    @Operation(summary = "Search categories", description = "Search categories by name")
    public ResponseEntity<Page<CategoryDto>> searchCategories(
            @Parameter(description = "Name to search") @RequestParam String name,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<CategoryDto> categories = categoryService.searchByName(name, pageable);
        return ResponseEntity.ok(categories);
    }
}