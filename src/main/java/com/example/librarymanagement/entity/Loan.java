package com.example.librarymanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Entidade que representa um empréstimo de livro na biblioteca.
 * Controla o processo de empréstimo, devolução e status dos livros emprestados.
 */
@Entity
@Table(name = "loans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Loan {

    /**
     * Identificador único do empréstimo (chave primária)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Data em que o empréstimo foi realizado (obrigatório)
     */
    @Column(nullable = false)
    private LocalDate loanDate;

    /**
     * Data limite para devolução do livro (obrigatório)
     * Normalmente 14 dias após a data do empréstimo
     */
    @Column(nullable = false)
    private LocalDate dueDate;

    /**
     * Data em que o livro foi devolvido (opcional)
     * Fica null enquanto o livro não for devolvido
     */
    @Column
    private LocalDate returnDate;

    /**
     * Status atual do empréstimo (obrigatório)
     * Controla se está ativo, devolvido ou em atraso
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    /**
     * Observações sobre o empréstimo (opcional)
     * Pode conter informações sobre condições especiais ou problemas
     */
    @Column(length = 500)
    private String notes;

    /**
     * Livro que foi emprestado - relacionamento Muitos-para-Um
     * Carregamento lazy para melhor performance
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    /**
     * Membro que realizou o empréstimo - relacionamento Muitos-para-Um
     * Carregamento lazy para melhor performance
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /**
     * Enum que define os possíveis status de um empréstimo
     * ACTIVE: Empréstimo em andamento, livro ainda não devolvido
     * RETURNED: Livro foi devolvido dentro do prazo
     * OVERDUE: Empréstimo em atraso, passou da data limite
     */
    public enum LoanStatus {
        ACTIVE, RETURNED, OVERDUE
    }
}