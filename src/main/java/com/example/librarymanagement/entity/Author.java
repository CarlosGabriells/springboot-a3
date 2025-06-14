package com.example.librarymanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Entidade que representa um autor no sistema de gerenciamento de biblioteca.
 * Armazena informações básicas do autor e mantém relacionamento com seus livros.
 */
@Entity
@Table(name = "authors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "books") // Exclui books do equals/hashCode para evitar loops infinitos
public class Author {

    /**
     * Identificador único do autor (chave primária)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Primeiro nome do autor (obrigatório)
     */
    @Column(nullable = false, length = 100)
    private String firstName;

    /**
     * Sobrenome do autor (obrigatório)
     */
    @Column(nullable = false, length = 100)
    private String lastName;

    /**
     * Nacionalidade do autor (opcional)
     */
    @Column(length = 100)
    private String nationality;

    /**
     * Data de nascimento do autor (opcional)
     */
    @Column
    private LocalDate birthDate;

    /**
     * Biografia do autor (opcional, até 1000 caracteres)
     */
    @Column(length = 1000)
    private String biography;

    /**
     * Lista de livros escritos por este autor
     * Relacionamento Um-para-Muitos com carregamento lazy
     */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Book> books;
}