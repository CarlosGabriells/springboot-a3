package com.example.librarymanagement.service;

import com.example.librarymanagement.dto.CategoryDto;
import com.example.librarymanagement.entity.Category;
import com.example.librarymanagement.exception.ResourceNotFoundException;
import com.example.librarymanagement.exception.BusinessException;
import com.example.librarymanagement.mapper.CategoryMapper;
import com.example.librarymanagement.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço responsável pela lógica de negócio relacionada às categorias.
 * Gerencia operações CRUD, consultas específicas e regras de negócio para entidade Category.
 * Inclui validação de nomes únicos e relacionamentos com livros.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true) // Por padrão, todas as operações são somente leitura
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    /**
     * Busca todas as categorias com paginação
     * @param pageable configurações de paginação
     * @return página com lista de categorias
     */
    public Page<CategoryDto> findAll(Pageable pageable) {
        log.debug("Finding all categories with pagination: {}", pageable);
        return categoryRepository.findAll(pageable)
                .map(categoryMapper::toDto);
    }

    /**
     * Busca todas as categorias sem paginação
     * @return lista completa de categorias
     */
    public List<CategoryDto> findAll() {
        log.debug("Finding all categories");
        return categoryMapper.toDtoList(categoryRepository.findAll());
    }

    /**
     * Busca uma categoria pelo ID
     * @param id identificador da categoria
     * @return dados da categoria encontrada
     * @throws ResourceNotFoundException se a categoria não for encontrada
     */
    public CategoryDto findById(Long id) {
        log.debug("Finding category by id: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return categoryMapper.toDto(category);
    }

    /**
     * Busca uma categoria pelo ID incluindo seus livros associados
     * @param id identificador da categoria
     * @return dados da categoria com lista de livros
     * @throws ResourceNotFoundException se a categoria não for encontrada
     */
    public CategoryDto findByIdWithBooks(Long id) {
        log.debug("Finding category by id with books: {}", id);
        Category category = categoryRepository.findByIdWithBooks(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return categoryMapper.toDto(category);
    }

    /**
     * Busca uma categoria pelo nome (ignora maiúsculas/minúsculas)
     * @param name nome da categoria
     * @return dados da categoria encontrada
     * @throws ResourceNotFoundException se a categoria não for encontrada
     */
    public CategoryDto findByName(String name) {
        log.debug("Finding category by name: {}", name);
        Category category = categoryRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with name: " + name));
        return categoryMapper.toDto(category);
    }

    /**
     * Cria uma nova categoria no sistema
     * @param categoryDto dados da categoria a ser criada
     * @return dados da categoria criada com ID gerado
     * @throws BusinessException se já existir uma categoria com o mesmo nome
     */
    @Transactional
    public CategoryDto create(CategoryDto categoryDto) {
        log.debug("Creating new category: {}", categoryDto);
        
        // Verifica se já existe uma categoria com o mesmo nome
        if (categoryRepository.existsByNameIgnoreCase(categoryDto.getName())) {
            throw new BusinessException("Category with name '" + categoryDto.getName() + "' already exists");
        }

        Category category = categoryMapper.toEntity(categoryDto);
        Category savedCategory = categoryRepository.save(category);
        log.info("Created category with id: {}", savedCategory.getId());
        return categoryMapper.toDto(savedCategory);
    }

    /**
     * Atualiza os dados de uma categoria existente
     * @param id identificador da categoria a ser atualizada
     * @param categoryDto novos dados da categoria
     * @return dados da categoria atualizada
     * @throws ResourceNotFoundException se a categoria não for encontrada
     * @throws BusinessException se tentar alterar para um nome que já existe
     */
    @Transactional
    public CategoryDto update(Long id, CategoryDto categoryDto) {
        log.debug("Updating category with id: {}", id);
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Verifica se o nome não está sendo usado por outra categoria
        if (!existingCategory.getName().equalsIgnoreCase(categoryDto.getName()) && 
            categoryRepository.existsByNameIgnoreCase(categoryDto.getName())) {
            throw new BusinessException("Category with name '" + categoryDto.getName() + "' already exists");
        }

        // Atualiza os campos da categoria existente
        existingCategory.setName(categoryDto.getName());
        existingCategory.setDescription(categoryDto.getDescription());

        Category updatedCategory = categoryRepository.save(existingCategory);
        log.info("Updated category with id: {}", updatedCategory.getId());
        return categoryMapper.toDto(updatedCategory);
    }

    /**
     * Remove uma categoria do sistema
     * @param id identificador da categoria a ser removida
     * @throws ResourceNotFoundException se a categoria não for encontrada
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Deleting category with id: {}", id);
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
        log.info("Deleted category with id: {}", id);
    }

    /**
     * Busca categorias por nome (busca parcial, ignora maiúsculas/minúsculas)
     * @param name nome ou parte do nome da categoria
     * @param pageable configurações de paginação
     * @return página com lista de categorias que correspondem ao critério
     */
    public Page<CategoryDto> searchByName(String name, Pageable pageable) {
        log.debug("Searching categories by name: {} with pagination: {}", name, pageable);
        return categoryRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(categoryMapper::toDto);
    }
}