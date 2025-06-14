package com.example.librarymanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Entidade que representa um membro da biblioteca.
 * Armazena dados pessoais, informações de contato e controla o status da associação.
 */
@Entity
@Table(name = "members")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "loans") // Exclui loans do equals/hashCode para evitar loops infinitos
public class Member {

    /**
     * Identificador único do membro (chave primária)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Primeiro nome do membro (obrigatório)
     */
    @Column(nullable = false, length = 100)
    private String firstName;

    /**
     * Sobrenome do membro (obrigatório)
     */
    @Column(nullable = false, length = 100)
    private String lastName;

    /**
     * Email do membro (único e obrigatório)
     * Usado para identificação e comunicação
     */
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Telefone do membro (opcional)
     */
    @Column(length = 20)
    private String phone;

    /**
     * Endereço do membro (opcional)
     */
    @Column(length = 200)
    private String address;

    /**
     * Data de início da associação à biblioteca (obrigatório)
     */
    @Column(nullable = false)
    private LocalDate membershipDate;

    /**
     * Status atual da associação do membro (obrigatório)
     * Define se o membro pode realizar empréstimos
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MembershipStatus status;

    /**
     * Lista de empréstimos realizados por este membro
     * Relacionamento Um-para-Muitos com carregamento lazy
     */
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Loan> loans;

    /**
     * Enum que define os possíveis status de associação de um membro
     * ACTIVE: Pode realizar empréstimos normalmente
     * SUSPENDED: Temporariamente impedido de realizar empréstimos
     * EXPIRED: Associação expirada, necessita renovação
     */
    public enum MembershipStatus {
        ACTIVE, SUSPENDED, EXPIRED
    }
}