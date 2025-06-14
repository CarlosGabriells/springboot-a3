package com.example.librarymanagement.repository;

import com.example.librarymanagement.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório responsável pelo acesso a dados da entidade Member.
 * Estende JpaRepository para operações CRUD básicas e define consultas customizadas
 * para busca de membros por critérios específicos como nome, email, status e histórico de empréstimos.
 */
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * Busca um membro pelo email único.
     * @param email endereço de email do membro
     * @return Optional contendo o membro se encontrado, ou empty se não existir
     */
    Optional<Member> findByEmail(String email);

    /**
     * Busca membros por nome (primeiro nome ou sobrenome) com busca parcial e insensível a maiúsculas.
     * @param name nome ou parte do nome do membro
     * @param pageable configurações de paginação
     * @return página de membros que correspondem ao critério de busca
     */
    @Query("SELECT m FROM Member m WHERE LOWER(m.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(m.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Member> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    /**
     * Busca membros por status de associação específico (sem paginação).
     * @param status status de associação (ACTIVE, SUSPENDED, EXPIRED)
     * @return lista de membros com o status especificado
     */
    List<Member> findByStatus(Member.MembershipStatus status);

    /**
     * Busca um membro pelo ID incluindo seu histórico de empréstimos (carregamento eager com JOIN FETCH).
     * Evita o problema N+1 ao carregar membro com seus empréstimos em uma única consulta.
     * @param id identificador único do membro
     * @return Optional contendo o membro com empréstimos carregados, ou empty se não encontrado
     */
    @Query("SELECT m FROM Member m JOIN FETCH m.loans WHERE m.id = :id")
    Optional<Member> findByIdWithLoans(@Param("id") Long id);

    /**
     * Verifica se já existe um membro com o email especificado.
     * @param email endereço de email a ser verificado
     * @return true se existir um membro com o email, false caso contrário
     */
    boolean existsByEmail(String email);

    /**
     * Busca membros por status de associação específico com paginação.
     * @param status status de associação (ACTIVE, SUSPENDED, EXPIRED)
     * @param pageable configurações de paginação
     * @return página de membros com o status especificado
     */
    @Query("SELECT m FROM Member m WHERE m.status = :status")
    Page<Member> findByStatus(@Param("status") Member.MembershipStatus status, Pageable pageable);

    /**
     * Busca membros que têm mais de X empréstimos no histórico.
     * Útil para identificar membros mais ativos na biblioteca.
     * @param loanCount número mínimo de empréstimos que o membro deve ter
     * @return lista de membros com mais empréstimos que o número especificado
     */
    @Query("SELECT m FROM Member m WHERE SIZE(m.loans) > :loanCount")
    List<Member> findMembersWithMoreThanXLoans(@Param("loanCount") int loanCount);
}