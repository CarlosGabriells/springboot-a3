package com.example.librarymanagement.service;

import com.example.librarymanagement.dto.AuthorDto;
import com.example.librarymanagement.entity.Author;
import com.example.librarymanagement.exception.ResourceNotFoundException;
import com.example.librarymanagement.mapper.AuthorMapper;
import com.example.librarymanagement.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço responsável pela lógica de negócio relacionada aos autores.
 * Gerencia operações CRUD e consultas específicas para entidade Author.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true) // Por padrão, todas as operações são somente leitura
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;

    /**
     * Busca todos os autores com paginação
     * @param pageable configurações de paginação
     * @return página com lista de autores
     */
    public Page<AuthorDto> findAll(Pageable pageable) {
        log.debug("Finding all authors with pagination: {}", pageable);
        return authorRepository.findAll(pageable)
                .map(authorMapper::toDto);
    }

    /**
     * Busca um autor pelo ID
     * @param id identificador do autor
     * @return dados do autor encontrado
     * @throws ResourceNotFoundException se o autor não for encontrado
     */
    public AuthorDto findById(Long id) {
        log.debug("Finding author by id: {}", id);
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
        return authorMapper.toDto(author);
    }

    /**
     * Busca um autor pelo ID incluindo seus livros
     * @param id identificador do autor
     * @return dados do autor com lista de livros
     * @throws ResourceNotFoundException se o autor não for encontrado
     */
    public AuthorDto findByIdWithBooks(Long id) {
        log.debug("Finding author by id with books: {}", id);
        Author author = authorRepository.findByIdWithBooks(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
        return authorMapper.toDto(author);
    }

    /**
     * Cria um novo autor no sistema
     * @param authorDto dados do autor a ser criado
     * @return dados do autor criado com ID gerado
     */
    @Transactional
    public AuthorDto create(AuthorDto authorDto) {
        log.debug("Creating new author: {}", authorDto);
        Author author = authorMapper.toEntity(authorDto);
        Author savedAuthor = authorRepository.save(author);
        log.info("Created author with id: {}", savedAuthor.getId());
        return authorMapper.toDto(savedAuthor);
    }

    /**
     * Atualiza os dados de um autor existente
     * @param id identificador do autor a ser atualizado
     * @param authorDto novos dados do autor
     * @return dados do autor atualizado
     * @throws ResourceNotFoundException se o autor não for encontrado
     */
    @Transactional
    public AuthorDto update(Long id, AuthorDto authorDto) {
        log.debug("Updating author with id: {}", id);
        Author existingAuthor = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));

        // Atualiza os campos do autor existente
        existingAuthor.setFirstName(authorDto.getFirstName());
        existingAuthor.setLastName(authorDto.getLastName());
        existingAuthor.setNationality(authorDto.getNationality());
        existingAuthor.setBirthDate(authorDto.getBirthDate());
        existingAuthor.setBiography(authorDto.getBiography());

        Author updatedAuthor = authorRepository.save(existingAuthor);
        log.info("Updated author with id: {}", updatedAuthor.getId());
        return authorMapper.toDto(updatedAuthor);
    }

    /**
     * Remove um autor do sistema
     * @param id identificador do autor a ser removido
     * @throws ResourceNotFoundException se o autor não for encontrado
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Deleting author with id: {}", id);
        if (!authorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Author not found with id: " + id);
        }
        authorRepository.deleteById(id);
        log.info("Deleted author with id: {}", id);
    }

    /**
     * Busca autores por nome (busca parcial, ignora maiúsculas/minúsculas)
     * @param name nome ou parte do nome do autor
     * @param pageable configurações de paginação
     * @return página com lista de autores que correspondem ao critério
     */
    public Page<AuthorDto> searchByName(String name, Pageable pageable) {
        log.debug("Searching authors by name: {} with pagination: {}", name, pageable);
        return authorRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(authorMapper::toDto);
    }

    /**
     * Busca autores por nacionalidade (ignora maiúsculas/minúsculas)
     * @param nationality nacionalidade dos autores
     * @return lista de autores da nacionalidade especificada
     */
    public List<AuthorDto> findByNationality(String nationality) {
        log.debug("Finding authors by nationality: {}", nationality);
        return authorMapper.toDtoList(authorRepository.findByNationalityIgnoreCase(nationality));
    }
}