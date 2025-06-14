package com.example.librarymanagement.repository;

import com.example.librarymanagement.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositório responsável pelo acesso a dados da entidade Loan.
 * Estende JpaRepository para operações CRUD básicas e define consultas customizadas
 * para controle de empréstimos, verificação de atrasos e análise de histórico por período.
 */
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    /**
     * Busca um empréstimo pelo ID incluindo livro e membro (carregamento eager com JOIN FETCH).
     * Evita o problema N+1 ao carregar empréstimo com seus relacionamentos em uma única consulta.
     * @param id identificador único do empréstimo
     * @return Optional contendo o empréstimo com detalhes carregados, ou empty se não encontrado
     */
    @Query("SELECT l FROM Loan l JOIN FETCH l.book JOIN FETCH l.member WHERE l.id = :id")
    Optional<Loan> findByIdWithDetails(@Param("id") Long id);

    /**
     * Busca empréstimos por membro específico com paginação.
     * @param memberId identificador único do membro
     * @param pageable configurações de paginação
     * @return página de empréstimos do membro especificado
     */
    @Query("SELECT l FROM Loan l WHERE l.member.id = :memberId")
    Page<Loan> findByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    /**
     * Busca empréstimos por livro específico com paginação.
     * @param bookId identificador único do livro
     * @param pageable configurações de paginação
     * @return página de empréstimos do livro especificado
     */
    @Query("SELECT l FROM Loan l WHERE l.book.id = :bookId")
    Page<Loan> findByBookId(@Param("bookId") Long bookId, Pageable pageable);

    /**
     * Busca empréstimos por status específico com paginação.
     * @param status status do empréstimo (ACTIVE, RETURNED, OVERDUE)
     * @param pageable configurações de paginação
     * @return página de empréstimos com o status especificado
     */
    @Query("SELECT l FROM Loan l WHERE l.status = :status")
    Page<Loan> findByStatus(@Param("status") Loan.LoanStatus status, Pageable pageable);

    /**
     * Busca empréstimos em atraso (data de vencimento menor que a data fornecida e status ACTIVE).
     * @param date data de referência para verificação de atraso (normalmente a data atual)
     * @return lista de empréstimos em atraso
     */
    @Query("SELECT l FROM Loan l WHERE l.dueDate < :date AND l.status = 'ACTIVE'")
    List<Loan> findOverdueLoans(@Param("date") LocalDate date);

    /**
     * Busca empréstimos ativos (não devolvidos) de um membro específico.
     * @param memberId identificador único do membro
     * @return lista de empréstimos ativos do membro
     */
    @Query("SELECT l FROM Loan l WHERE l.member.id = :memberId AND l.status = 'ACTIVE'")
    List<Loan> findActiveLoansByMember(@Param("memberId") Long memberId);

    /**
     * Busca empréstimos ativos (não devolvidos) de um livro específico.
     * @param bookId identificador único do livro
     * @return lista de empréstimos ativos do livro
     */
    @Query("SELECT l FROM Loan l WHERE l.book.id = :bookId AND l.status = 'ACTIVE'")
    List<Loan> findActiveLoansByBook(@Param("bookId") Long bookId);

    /**
     * Busca empréstimos realizados em um período específico com paginação.
     * @param startDate data de início do período
     * @param endDate data de fim do período
     * @param pageable configurações de paginação
     * @return página de empréstimos realizados no período especificado
     */
    @Query("SELECT l FROM Loan l WHERE l.loanDate BETWEEN :startDate AND :endDate")
    Page<Loan> findByLoanDateBetween(@Param("startDate") LocalDate startDate, 
                                   @Param("endDate") LocalDate endDate, 
                                   Pageable pageable);

    /**
     * Conta o número de empréstimos ativos de um membro específico.
     * Útil para verificar o limite de empréstimos por membro (máximo 5).
     * @param memberId identificador único do membro
     * @return número de empréstimos ativos do membro
     */
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.member.id = :memberId AND l.status = 'ACTIVE'")
    Long countActiveLoansByMember(@Param("memberId") Long memberId);
}