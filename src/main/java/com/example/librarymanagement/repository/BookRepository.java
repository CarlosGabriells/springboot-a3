package com.example.librarymanagement.repository;

import com.example.librarymanagement.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório responsável pelo acesso a dados da entidade Book.
 * Estende JpaRepository para operações CRUD básicas e define consultas customizadas
 * para busca de livros por diversos critérios como título, autor, categoria e disponibilidade.
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Busca um livro pelo código ISBN único.
     * @param isbn código ISBN do livro
     * @return Optional contendo o livro se encontrado, ou empty se não existir
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * Busca livros por título com busca parcial e insensível a maiúsculas.
     * @param title título ou parte do título do livro
     * @param pageable configurações de paginação
     * @return página de livros que correspondem ao critério de busca
     */
    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Book> findByTitleContainingIgnoreCase(@Param("title") String title, Pageable pageable);

    /**
     * Busca um livro pelo ID incluindo autor e categorias (carregamento eager com JOIN FETCH).
     * Evita o problema N+1 ao carregar livro com seus relacionamentos em uma única consulta.
     * @param id identificador único do livro
     * @return Optional contendo o livro com detalhes carregados, ou empty se não encontrado
     */
    @Query("SELECT b FROM Book b JOIN FETCH b.author JOIN FETCH b.categories WHERE b.id = :id")
    Optional<Book> findByIdWithDetails(@Param("id") Long id);

    /**
     * Busca todos os livros de um autor específico.
     * @param authorId identificador único do autor
     * @return lista de livros do autor especificado
     */
    @Query("SELECT b FROM Book b WHERE b.author.id = :authorId")
    List<Book> findByAuthorId(@Param("authorId") Long authorId);

    /**
     * Busca livros por categoria específica com paginação.
     * @param categoryId identificador único da categoria
     * @param pageable configurações de paginação
     * @return página de livros da categoria especificada
     */
    @Query("SELECT b FROM Book b JOIN b.categories c WHERE c.id = :categoryId")
    Page<Book> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * Busca livros disponíveis para empréstimo (com exemplares disponíveis > 0).
     * @param pageable configurações de paginação
     * @return página de livros disponíveis para empréstimo
     */
    @Query("SELECT b FROM Book b WHERE b.availableCopies > 0")
    Page<Book> findAvailableBooks(Pageable pageable);

    /**
     * Busca livros indisponíveis para empréstimo (sem exemplares disponíveis).
     * @return lista de livros sem exemplares disponíveis
     */
    @Query("SELECT b FROM Book b WHERE b.availableCopies = 0")
    List<Book> findUnavailableBooks();

    /**
     * Verifica se já existe um livro com o ISBN especificado.
     * @param isbn código ISBN a ser verificado
     * @return true se existir um livro com o ISBN, false caso contrário
     */
    boolean existsByIsbn(String isbn);

    /**
     * Busca livros por palavra-chave no título ou nome do autor.
     * Realiza busca parcial e insensível a maiúsculas nos campos título, primeiro nome e sobrenome do autor.
     * @param keyword palavra-chave para busca
     * @param pageable configurações de paginação
     * @return página de livros que correspondem ao critério de busca
     */
    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> searchBooks(@Param("keyword") String keyword, Pageable pageable);
}