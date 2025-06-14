package com.example.librarymanagement.controller;

import com.example.librarymanagement.dto.LoanDto;
import com.example.librarymanagement.entity.Loan;
import com.example.librarymanagement.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST responsável pelos endpoints de gerenciamento de empréstimos.
 * Fornece operações CRUD completas e consultas específicas para empréstimos através de API REST.
 * Inclui funcionalidades para processar empréstimos, devoluções, consultas por status e controle de atrasos.
 * Todos os endpoints retornam dados no formato JSON e seguem as convenções REST.
 */
@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Tag(name = "Loans", description = "Loan management endpoints")
public class LoanController {

    private final LoanService loanService;

    /**
     * Endpoint para buscar todos os empréstimos com paginação.
     * @param pageable configurações de paginação (tamanho padrão: 10)
     * @return ResponseEntity com página de empréstimos e status HTTP 200 (OK)
     */
    @GetMapping
    @Operation(summary = "Get all loans", description = "Retrieve all loans with pagination")
    public ResponseEntity<Page<LoanDto>> getAllLoans(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<LoanDto> loans = loanService.findAll(pageable);
        return ResponseEntity.ok(loans);
    }

    /**
     * Endpoint para buscar um empréstimo específico pelo ID.
     * @param id identificador único do empréstimo
     * @return ResponseEntity com dados do empréstimo e status HTTP 200 (OK)
     * @throws ResourceNotFoundException se o empréstimo não for encontrado (HTTP 404)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get loan by ID", description = "Retrieve a specific loan by ID")
    public ResponseEntity<LoanDto> getLoanById(
            @Parameter(description = "Loan ID") @PathVariable Long id) {
        LoanDto loan = loanService.findById(id);
        return ResponseEntity.ok(loan);
    }

    /**
     * Endpoint para criar um novo empréstimo no sistema.
     * Aplica regras de negócio como limite de 5 empréstimos por membro e verificação de disponibilidade.
     * @param loanDto dados do empréstimo a ser criado (validados automaticamente)
     * @return ResponseEntity com dados do empréstimo criado e status HTTP 201 (CREATED)
     * @throws ValidationException se os dados fornecidos forem inválidos (HTTP 400)
     * @throws BusinessException se violar regras de negócio (HTTP 409)
     */
    @PostMapping
    @Operation(summary = "Create loan", description = "Create a new loan")
    public ResponseEntity<LoanDto> createLoan(@Valid @RequestBody LoanDto loanDto) {
        LoanDto createdLoan = loanService.create(loanDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLoan);
    }

    /**
     * Endpoint para atualizar um empréstimo existente.
     * @param id identificador único do empréstimo a ser atualizado
     * @param loanDto novos dados do empréstimo (validados automaticamente)
     * @return ResponseEntity com dados do empréstimo atualizado e status HTTP 200 (OK)
     * @throws ResourceNotFoundException se o empréstimo não for encontrado (HTTP 404)
     * @throws ValidationException se os dados fornecidos forem inválidos (HTTP 400)
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update loan", description = "Update an existing loan")
    public ResponseEntity<LoanDto> updateLoan(
            @Parameter(description = "Loan ID") @PathVariable Long id,
            @Valid @RequestBody LoanDto loanDto) {
        LoanDto updatedLoan = loanService.update(id, loanDto);
        return ResponseEntity.ok(updatedLoan);
    }

    /**
     * Endpoint para remover um empréstimo do sistema.
     * ATENÇÃO: Operação irreversível que pode afetar o histórico de empréstimos.
     * @param id identificador único do empréstimo a ser removido
     * @return ResponseEntity vazio com status HTTP 204 (NO CONTENT)
     * @throws ResourceNotFoundException se o empréstimo não for encontrado (HTTP 404)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete loan", description = "Delete a loan by ID")
    public ResponseEntity<Void> deleteLoan(
            @Parameter(description = "Loan ID") @PathVariable Long id) {
        loanService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint para processar a devolução de um livro.
     * Atualiza o status do empréstimo para 'RETURNED' e incrementa exemplares disponíveis.
     * @param id identificador único do empréstimo
     * @return ResponseEntity com dados do empréstimo atualizado e status HTTP 200 (OK)
     * @throws ResourceNotFoundException se o empréstimo não for encontrado (HTTP 404)
     * @throws BusinessException se o livro já foi devolvido (HTTP 409)
     */
    @PatchMapping("/{id}/return")
    @Operation(summary = "Return book", description = "Mark a loan as returned")
    public ResponseEntity<LoanDto> returnBook(
            @Parameter(description = "Loan ID") @PathVariable Long id) {
        LoanDto returnedLoan = loanService.returnBook(id);
        return ResponseEntity.ok(returnedLoan);
    }

    /**
     * Endpoint para buscar empréstimos por membro específico.
     * Útil para visualizar histórico de empréstimos de um membro.
     * @param memberId identificador único do membro
     * @param pageable configurações de paginação (tamanho padrão: 10)
     * @return ResponseEntity com página de empréstimos do membro e status HTTP 200 (OK)
     */
    @GetMapping("/member/{memberId}")
    @Operation(summary = "Get loans by member", description = "Retrieve loans by member ID")
    public ResponseEntity<Page<LoanDto>> getLoansByMember(
            @Parameter(description = "Member ID") @PathVariable Long memberId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<LoanDto> loans = loanService.findByMember(memberId, pageable);
        return ResponseEntity.ok(loans);
    }

    /**
     * Endpoint para buscar empréstimos por livro específico.
     * Útil para verificar histórico de empréstimos de um livro.
     * @param bookId identificador único do livro
     * @param pageable configurações de paginação (tamanho padrão: 10)
     * @return ResponseEntity com página de empréstimos do livro e status HTTP 200 (OK)
     */
    @GetMapping("/book/{bookId}")
    @Operation(summary = "Get loans by book", description = "Retrieve loans by book ID")
    public ResponseEntity<Page<LoanDto>> getLoansByBook(
            @Parameter(description = "Book ID") @PathVariable Long bookId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<LoanDto> loans = loanService.findByBook(bookId, pageable);
        return ResponseEntity.ok(loans);
    }

    /**
     * Endpoint para buscar empréstimos por status específico.
     * @param status status do empréstimo (ACTIVE, RETURNED, OVERDUE)
     * @param pageable configurações de paginação (tamanho padrão: 10)
     * @return ResponseEntity com página de empréstimos do status especificado e status HTTP 200 (OK)
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get loans by status", description = "Retrieve loans by status")
    public ResponseEntity<Page<LoanDto>> getLoansByStatus(
            @Parameter(description = "Loan status") @PathVariable Loan.LoanStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<LoanDto> loans = loanService.findByStatus(status, pageable);
        return ResponseEntity.ok(loans);
    }

    /**
     * Endpoint para buscar todos os empréstimos em atraso.
     * Útil para geração de relatórios e controle administrativo.
     * @return ResponseEntity com lista de empréstimos em atraso e status HTTP 200 (OK)
     */
    @GetMapping("/overdue")
    @Operation(summary = "Get overdue loans", description = "Retrieve all overdue loans")
    public ResponseEntity<List<LoanDto>> getOverdueLoans() {
        List<LoanDto> overdueLoans = loanService.findOverdueLoans();
        return ResponseEntity.ok(overdueLoans);
    }

    /**
     * Endpoint para atualizar em lote o status de empréstimos em atraso.
     * Processo administrativo que verifica e atualiza automaticamente empréstimos vencidos.
     * @return ResponseEntity vazio com status HTTP 200 (OK)
     */
    @PatchMapping("/update-overdue")
    @Operation(summary = "Update overdue loans", description = "Update status of overdue loans")
    public ResponseEntity<Void> updateOverdueLoans() {
        loanService.updateOverdueLoans();
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint para buscar empréstimos por período de datas.
     * Útil para geração de relatórios e análises por período.
     * @param startDate data de início do período (formato: YYYY-MM-DD)
     * @param endDate data de fim do período (formato: YYYY-MM-DD)
     * @param pageable configurações de paginação (tamanho padrão: 10)
     * @return ResponseEntity com página de empréstimos do período e status HTTP 200 (OK)
     */
    @GetMapping("/date-range")
    @Operation(summary = "Get loans by date range", description = "Retrieve loans within a date range")
    public ResponseEntity<Page<LoanDto>> getLoansByDateRange(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<LoanDto> loans = loanService.findByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(loans);
    }
}