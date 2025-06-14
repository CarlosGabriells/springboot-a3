package com.example.librarymanagement.controller;

import com.example.librarymanagement.dto.MemberDto;
import com.example.librarymanagement.entity.Member;
import com.example.librarymanagement.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST responsável pelos endpoints de gerenciamento de membros.
 * Fornece operações CRUD completas e consultas específicas para membros através de API REST.
 * Inclui funcionalidades de busca por nome, filtro por status e gestão de associação.
 * Todos os endpoints retornam dados no formato JSON e seguem as convenções REST.
 */
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Members", description = "Member management endpoints")
public class MemberController {

    private final MemberService memberService;

    /**
     * Endpoint para buscar todos os membros com paginação.
     * @param pageable configurações de paginação (tamanho padrão: 10)
     * @return ResponseEntity com página de membros e status HTTP 200 (OK)
     */
    @GetMapping
    @Operation(summary = "Get all members", description = "Retrieve all members with pagination")
    public ResponseEntity<Page<MemberDto>> getAllMembers(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<MemberDto> members = memberService.findAll(pageable);
        return ResponseEntity.ok(members);
    }

    /**
     * Endpoint para buscar um membro específico pelo ID.
     * @param id identificador único do membro
     * @return ResponseEntity com dados do membro e status HTTP 200 (OK)
     * @throws ResourceNotFoundException se o membro não for encontrado (HTTP 404)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get member by ID", description = "Retrieve a specific member by ID")
    public ResponseEntity<MemberDto> getMemberById(
            @Parameter(description = "Member ID") @PathVariable Long id) {
        MemberDto member = memberService.findById(id);
        return ResponseEntity.ok(member);
    }

    /**
     * Endpoint para buscar um membro específico incluindo seu histórico de empréstimos.
     * @param id identificador único do membro
     * @return ResponseEntity com dados do membro e lista de empréstimos, status HTTP 200 (OK)
     * @throws ResourceNotFoundException se o membro não for encontrado (HTTP 404)
     */
    @GetMapping("/{id}/with-loans")
    @Operation(summary = "Get member with loans", description = "Retrieve a specific member with their loans")
    public ResponseEntity<MemberDto> getMemberWithLoans(
            @Parameter(description = "Member ID") @PathVariable Long id) {
        MemberDto member = memberService.findByIdWithLoans(id);
        return ResponseEntity.ok(member);
    }

    /**
     * Endpoint para buscar um membro específico pelo email.
     * @param email endereço de email único do membro
     * @return ResponseEntity com dados do membro e status HTTP 200 (OK)
     * @throws ResourceNotFoundException se o membro não for encontrado (HTTP 404)
     */
    @GetMapping("/email/{email}")
    @Operation(summary = "Get member by email", description = "Retrieve a specific member by email")
    public ResponseEntity<MemberDto> getMemberByEmail(
            @Parameter(description = "Member email") @PathVariable String email) {
        MemberDto member = memberService.findByEmail(email);
        return ResponseEntity.ok(member);
    }

    /**
     * Endpoint para criar um novo membro no sistema.
     * @param memberDto dados do membro a ser criado (validados automaticamente)
     * @return ResponseEntity com dados do membro criado e status HTTP 201 (CREATED)
     * @throws ValidationException se os dados fornecidos forem inválidos (HTTP 400)
     * @throws BusinessException se o email já existir no sistema (HTTP 409)
     */
    @PostMapping
    @Operation(summary = "Create member", description = "Create a new member")
    public ResponseEntity<MemberDto> createMember(@Valid @RequestBody MemberDto memberDto) {
        MemberDto createdMember = memberService.create(memberDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMember);
    }

    /**
     * Endpoint para atualizar um membro existente.
     * @param id identificador único do membro a ser atualizado
     * @param memberDto novos dados do membro (validados automaticamente)
     * @return ResponseEntity com dados do membro atualizado e status HTTP 200 (OK)
     * @throws ResourceNotFoundException se o membro não for encontrado (HTTP 404)
     * @throws ValidationException se os dados fornecidos forem inválidos (HTTP 400)
     * @throws BusinessException se o email já existir para outro membro (HTTP 409)
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update member", description = "Update an existing member")
    public ResponseEntity<MemberDto> updateMember(
            @Parameter(description = "Member ID") @PathVariable Long id,
            @Valid @RequestBody MemberDto memberDto) {
        MemberDto updatedMember = memberService.update(id, memberDto);
        return ResponseEntity.ok(updatedMember);
    }

    /**
     * Endpoint para remover um membro do sistema.
     * @param id identificador único do membro a ser removido
     * @return ResponseEntity vazio com status HTTP 204 (NO CONTENT)
     * @throws ResourceNotFoundException se o membro não for encontrado (HTTP 404)
     * @throws BusinessException se o membro possui empréstimos ativos (HTTP 409)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete member", description = "Delete a member by ID")
    public ResponseEntity<Void> deleteMember(
            @Parameter(description = "Member ID") @PathVariable Long id) {
        memberService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint para buscar membros por nome (busca parcial, ignora maiúsculas/minúsculas).
     * @param name nome ou parte do nome do membro para busca
     * @param pageable configurações de paginação (tamanho padrão: 10)
     * @return ResponseEntity com página de membros que correspondem ao critério e status HTTP 200 (OK)
     */
    @GetMapping("/search")
    @Operation(summary = "Search members", description = "Search members by name")
    public ResponseEntity<Page<MemberDto>> searchMembers(
            @Parameter(description = "Name to search") @RequestParam String name,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<MemberDto> members = memberService.searchByName(name, pageable);
        return ResponseEntity.ok(members);
    }

    /**
     * Endpoint para buscar membros por status de associação específico.
     * @param status status de associação (ACTIVE, SUSPENDED, EXPIRED)
     * @param pageable configurações de paginação (tamanho padrão: 10)
     * @return ResponseEntity com página de membros do status especificado e status HTTP 200 (OK)
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get members by status", description = "Retrieve members by membership status")
    public ResponseEntity<Page<MemberDto>> getMembersByStatus(
            @Parameter(description = "Membership status") @PathVariable Member.MembershipStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<MemberDto> members = memberService.findByStatus(status, pageable);
        return ResponseEntity.ok(members);
    }

    /**
     * Endpoint para atualizar apenas o status de associação de um membro.
     * Usado para ativar, suspender ou expirar a associação do membro.
     * @param id identificador único do membro
     * @param status novo status de associação (ACTIVE, SUSPENDED, EXPIRED)
     * @return ResponseEntity com dados do membro atualizado e status HTTP 200 (OK)
     * @throws ResourceNotFoundException se o membro não for encontrado (HTTP 404)
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update member status", description = "Update a member's status")
    public ResponseEntity<MemberDto> updateMemberStatus(
            @Parameter(description = "Member ID") @PathVariable Long id,
            @Parameter(description = "New status") @RequestParam Member.MembershipStatus status) {
        MemberDto updatedMember = memberService.updateStatus(id, status);
        return ResponseEntity.ok(updatedMember);
    }
}