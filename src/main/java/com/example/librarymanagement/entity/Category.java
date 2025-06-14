package com.example.librarymanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Entidade que representa uma categoria de livros no sistema de biblioteca.
 * Categorias são usadas para classificar e organizar os livros (ex: Ficção, Romance, Técnico).
 */
@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "books") // Exclui books do equals/hashCode para evitar loops infinitos
public class Category {

    /**
     * Identificador único da categoria (chave primária)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome da categoria (único e obrigatório)
     * Ex: "Ficção", "Romance", "Técnico", "História"
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * Descrição da categoria (opcional, até 500 caracteres)
     * Explica o tipo de livros que pertencem a esta categoria
     */
    @Column(length = 500)
    private String description;

    /**
     * Lista de livros que pertencem a esta categoria
     * Relacionamento Muitos-para-Muitos (lado inverso)
     */
    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    private List<Book> books;
}