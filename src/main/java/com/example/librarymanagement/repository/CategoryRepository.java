package com.example.librarymanagement.repository;

import com.example.librarymanagement.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório responsável pelo acesso a dados da entidade Category.
 * Estende JpaRepository para operações CRUD básicas e define consultas customizadas
 * para busca de categorias por nome e análise de popularidade por quantidade de livros.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Busca uma categoria pelo nome exato (insensível a maiúsculas).
     * @param name nome da categoria
     * @return Optional contendo a categoria se encontrada, ou empty se não existir
     */
    Optional<Category> findByNameIgnoreCase(String name);

    /**
     * Busca categorias por nome com busca parcial e insensível a maiúsculas.
     * @param name nome ou parte do nome da categoria
     * @param pageable configurações de paginação
     * @return página de categorias que correspondem ao critério de busca
     */
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Category> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    /**
     * Busca uma categoria pelo ID incluindo seus livros (carregamento eager com JOIN FETCH).
     * Evita o problema N+1 ao carregar categoria com seus livros em uma única consulta.
     * @param id identificador único da categoria
     * @return Optional contendo a categoria com livros carregados, ou empty se não encontrada
     */
    @Query("SELECT c FROM Category c JOIN FETCH c.books WHERE c.id = :id")
    Optional<Category> findByIdWithBooks(@Param("id") Long id);

    /**
     * Busca categorias que têm mais de X livros associados.
     * Útil para identificar categorias mais populares na biblioteca.
     * @param bookCount número mínimo de livros que a categoria deve ter
     * @return lista de categorias com mais livros que o número especificado
     */
    @Query("SELECT c FROM Category c WHERE SIZE(c.books) > :bookCount")
    List<Category> findCategoriesWithMoreThanXBooks(@Param("bookCount") int bookCount);

    /**
     * Verifica se já existe uma categoria com o nome especificado (insensível a maiúsculas).
     * @param name nome da categoria a ser verificado
     * @return true se existir uma categoria com o nome, false caso contrário
     */
    boolean existsByNameIgnoreCase(String name);
}