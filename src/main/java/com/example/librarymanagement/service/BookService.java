package com.example.librarymanagement.service;

import com.example.librarymanagement.dto.BookDto;
import com.example.librarymanagement.entity.Author;
import com.example.librarymanagement.entity.Book;
import com.example.librarymanagement.entity.Category;
import com.example.librarymanagement.exception.ResourceNotFoundException;
import com.example.librarymanagement.exception.BusinessException;
import com.example.librarymanagement.mapper.BookMapper;
import com.example.librarymanagement.repository.AuthorRepository;
import com.example.librarymanagement.repository.BookRepository;
import com.example.librarymanagement.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço responsável pela lógica de negócio relacionada aos livros.
 * Gerencia operações CRUD, consultas específicas, e regras de negócio para entidade Book.
 * Inclui controle de disponibilidade de exemplares e relacionamentos com autores e categorias.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true) // Por padrão, todas as operações são somente leitura
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final BookMapper bookMapper;

    /**
     * Busca todos os livros com paginação
     * @param pageable configurações de paginação
     * @return página com lista de livros
     */
    public Page<BookDto> findAll(Pageable pageable) {
        log.debug("Finding all books with pagination: {}", pageable);
        return bookRepository.findAll(pageable)
                .map(bookMapper::toDto);
    }

    /**
     * Busca um livro pelo ID incluindo detalhes completos
     * @param id identificador do livro
     * @return dados do livro encontrado com autor e categorias
     * @throws ResourceNotFoundException se o livro não for encontrado
     */
    public BookDto findById(Long id) {
        log.debug("Finding book by id: {}", id);
        Book book = bookRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        return bookMapper.toDto(book);
    }

    /**
     * Busca um livro pelo código ISBN
     * @param isbn código ISBN do livro
     * @return dados do livro encontrado
     * @throws ResourceNotFoundException se o livro não for encontrado
     */
    public BookDto findByIsbn(String isbn) {
        log.debug("Finding book by isbn: {}", isbn);
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with isbn: " + isbn));
        return bookMapper.toDto(book);
    }

    /**
     * Cria um novo livro no sistema
     * @param bookDto dados do livro a ser criado
     * @return dados do livro criado com ID gerado
     * @throws BusinessException se já existir um livro com o mesmo ISBN
     * @throws ResourceNotFoundException se o autor ou categorias não forem encontrados
     */
    @Transactional
    public BookDto create(BookDto bookDto) {
        log.debug("Creating new book: {}", bookDto);
        
        // Verifica se já existe um livro com o mesmo ISBN
        if (bookRepository.existsByIsbn(bookDto.getIsbn())) {
            throw new BusinessException("Book with ISBN " + bookDto.getIsbn() + " already exists");
        }

        // Busca o autor pelo ID fornecido
        Author author = authorRepository.findById(bookDto.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + bookDto.getAuthorId()));

        Book book = bookMapper.toEntity(bookDto);
        book.setAuthor(author);

        // Associa categorias se foram fornecidas
        if (bookDto.getCategoryIds() != null && !bookDto.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(bookDto.getCategoryIds());
            if (categories.size() != bookDto.getCategoryIds().size()) {
                throw new ResourceNotFoundException("One or more categories not found");
            }
            book.setCategories(categories);
        }

        Book savedBook = bookRepository.save(book);
        log.info("Created book with id: {}", savedBook.getId());
        return bookMapper.toDto(savedBook);
    }

    /**
     * Atualiza os dados de um livro existente
     * @param id identificador do livro a ser atualizado
     * @param bookDto novos dados do livro
     * @return dados do livro atualizado
     * @throws ResourceNotFoundException se o livro, autor ou categorias não forem encontrados
     * @throws BusinessException se tentar alterar para um ISBN que já existe
     */
    @Transactional
    public BookDto update(Long id, BookDto bookDto) {
        log.debug("Updating book with id: {}", id);
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        // Verifica se o ISBN não está sendo usado por outro livro
        if (!existingBook.getIsbn().equals(bookDto.getIsbn()) && 
            bookRepository.existsByIsbn(bookDto.getIsbn())) {
            throw new BusinessException("Book with ISBN " + bookDto.getIsbn() + " already exists");
        }

        // Busca o autor pelo ID fornecido
        Author author = authorRepository.findById(bookDto.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + bookDto.getAuthorId()));

        // Atualiza os campos do livro existente
        existingBook.setIsbn(bookDto.getIsbn());
        existingBook.setTitle(bookDto.getTitle());
        existingBook.setDescription(bookDto.getDescription());
        existingBook.setPublicationDate(bookDto.getPublicationDate());
        existingBook.setTotalCopies(bookDto.getTotalCopies());
        existingBook.setAvailableCopies(bookDto.getAvailableCopies());
        existingBook.setAuthor(author);

        // Atualiza categorias se foram fornecidas
        if (bookDto.getCategoryIds() != null) {
            List<Category> categories = categoryRepository.findAllById(bookDto.getCategoryIds());
            existingBook.setCategories(categories);
        }

        Book updatedBook = bookRepository.save(existingBook);
        log.info("Updated book with id: {}", updatedBook.getId());
        return bookMapper.toDto(updatedBook);
    }

    /**
     * Remove um livro do sistema
     * @param id identificador do livro a ser removido
     * @throws ResourceNotFoundException se o livro não for encontrado
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Deleting book with id: {}", id);
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
        log.info("Deleted book with id: {}", id);
    }

    /**
     * Busca livros por palavra-chave no título, descrição ou nome do autor
     * @param keyword palavra-chave para busca
     * @param pageable configurações de paginação
     * @return página com lista de livros que correspondem ao critério
     */
    public Page<BookDto> searchBooks(String keyword, Pageable pageable) {
        log.debug("Searching books with keyword: {} and pagination: {}", keyword, pageable);
        return bookRepository.searchBooks(keyword, pageable)
                .map(bookMapper::toDto);
    }

    /**
     * Busca livros disponíveis para empréstimo (com exemplares disponíveis)
     * @param pageable configurações de paginação
     * @return página com lista de livros disponíveis
     */
    public Page<BookDto> findAvailableBooks(Pageable pageable) {
        log.debug("Finding available books with pagination: {}", pageable);
        return bookRepository.findAvailableBooks(pageable)
                .map(bookMapper::toDto);
    }

    /**
     * Busca livros por categoria específica
     * @param categoryId identificador da categoria
     * @param pageable configurações de paginação
     * @return página com lista de livros da categoria especificada
     */
    public Page<BookDto> findByCategory(Long categoryId, Pageable pageable) {
        log.debug("Finding books by category id: {} with pagination: {}", categoryId, pageable);
        return bookRepository.findByCategoryId(categoryId, pageable)
                .map(bookMapper::toDto);
    }

    /**
     * Atualiza a quantidade de exemplares disponíveis de um livro
     * Usado principalmente pelo sistema de empréstimos
     * @param bookId identificador do livro
     * @param change quantidade a ser adicionada ou subtraída (pode ser negativa)
     * @throws ResourceNotFoundException se o livro não for encontrado
     * @throws BusinessException se a operação resultar em valores inválidos
     */
    @Transactional
    public void updateAvailableCopies(Long bookId, int change) {
        log.debug("Updating available copies for book id: {} by: {}", bookId, change);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        
        int newAvailableCopies = book.getAvailableCopies() + change;
        
        // Valida se a nova quantidade não é negativa
        if (newAvailableCopies < 0) {
            throw new BusinessException("Cannot reduce available copies below 0");
        }
        
        // Valida se não excede o total de exemplares
        if (newAvailableCopies > book.getTotalCopies()) {
            throw new BusinessException("Available copies cannot exceed total copies");
        }
        
        book.setAvailableCopies(newAvailableCopies);
        bookRepository.save(book);
        log.info("Updated available copies for book id: {} to: {}", bookId, newAvailableCopies);
    }
}