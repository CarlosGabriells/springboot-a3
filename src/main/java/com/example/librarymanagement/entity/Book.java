package com.example.librarymanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Entidade que representa um livro no sistema de gerenciamento de biblioteca.
 * Contém informações do livro, controle de exemplares e relacionamentos com autor, categorias e empréstimos.
 */
@Entity
@Table(name = "books")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"loans", "categories"}) // Exclui coleções do equals/hashCode para evitar loops
public class Book {

    /**
     * Identificador único do livro (chave primária)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Código ISBN do livro (único e obrigatório)
     */
    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    /**
     * Título do livro (obrigatório)
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * Descrição do livro (opcional, até 1000 caracteres)
     */
    @Column(length = 1000)
    private String description;

    /**
     * Data de publicação do livro (obrigatório)
     */
    @Column(nullable = false)
    private LocalDate publicationDate;

    /**
     * Número total de exemplares deste livro (obrigatório)
     */
    @Column(nullable = false)
    private Integer totalCopies;

    /**
     * Número de exemplares disponíveis para empréstimo (obrigatório)
     */
    @Column(nullable = false)
    private Integer availableCopies;

    /**
     * Autor do livro - relacionamento Muitos-para-Um
     * Carregamento lazy para melhor performance
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    /**
     * Categorias do livro - relacionamento Muitos-para-Muitos
     * Um livro pode ter várias categorias e uma categoria pode ter vários livros
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "book_categories",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories;

    /**
     * Lista de empréstimos deste livro
     * Relacionamento Um-para-Muitos com carregamento lazy
     */
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Loan> loans;
}