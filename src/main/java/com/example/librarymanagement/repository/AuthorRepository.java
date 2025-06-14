package com.example.librarymanagement.repository;

import com.example.librarymanagement.entity.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório responsável pelo acesso a dados da entidade Author.
 * Estende JpaRepository para operações CRUD básicas e define consultas customizadas
 * usando JPQL para busca de autores por critérios específicos.
 */
@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    /**
     * Busca autores por nome (primeiro nome ou sobrenome) com busca parcial e insensível a maiúsculas.
     * @param name nome ou parte do nome do autor para busca
     * @param pageable configurações de paginação
     * @return página de autores que correspondem ao critério de busca
     */
    @Query("SELECT a FROM Author a WHERE LOWER(a.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(a.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Author> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    /**
     * Busca autores pela nacionalidade (insensível a maiúsculas).
     * @param nationality nacionalidade dos autores
     * @return lista de autores da nacionalidade especificada
     */
    List<Author> findByNationalityIgnoreCase(String nationality);

    /**
     * Busca um autor pelo ID incluindo seus livros (carregamento eager com JOIN FETCH).
     * Evita o problema N+1 ao carregar autor com seus livros em uma única consulta.
     * @param id identificador único do autor
     * @return Optional contendo o autor com livros carregados, ou empty se não encontrado
     */
    @Query("SELECT a FROM Author a JOIN FETCH a.books WHERE a.id = :id")
    Optional<Author> findByIdWithBooks(@Param("id") Long id);

    /**
     * Busca autores que têm mais de X livros publicados.
     * Útil para identificar autores prolíficos na biblioteca.
     * @param bookCount número mínimo de livros que o autor deve ter
     * @return lista de autores com mais livros que o número especificado
     */
    @Query("SELECT a FROM Author a WHERE SIZE(a.books) > :bookCount")
    List<Author> findAuthorsWithMoreThanXBooks(@Param("bookCount") int bookCount);
}