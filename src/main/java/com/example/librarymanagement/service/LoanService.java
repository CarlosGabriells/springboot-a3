package com.example.librarymanagement.service;

import com.example.librarymanagement.dto.LoanDto;
import com.example.librarymanagement.entity.Book;
import com.example.librarymanagement.entity.Loan;
import com.example.librarymanagement.entity.Member;
import com.example.librarymanagement.exception.ResourceNotFoundException;
import com.example.librarymanagement.exception.BusinessException;
import com.example.librarymanagement.mapper.LoanMapper;
import com.example.librarymanagement.repository.BookRepository;
import com.example.librarymanagement.repository.LoanRepository;
import com.example.librarymanagement.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Serviço responsável pela lógica de negócio relacionada aos empréstimos de livros.
 * Gerencia todo o ciclo de vida dos empréstimos: criação, devolução, renovação e controle de atrasos.
 * Implementa regras de negócio como limite de empréstimos por membro (5), período de empréstimo (14 dias),
 * validação de status do membro, disponibilidade de livros e controle automático de atrasos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true) // Por padrão, todas as operações são somente leitura
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BookService bookService;
    private final LoanMapper loanMapper;

    // Constantes de regras de negócio
    private static final int MAX_LOANS_PER_MEMBER = 5; // Máximo de empréstimos ativos por membro
    private static final int LOAN_PERIOD_DAYS = 14; // Período padrão de empréstimo em dias

    /**
     * Busca todos os empréstimos com paginação
     * @param pageable configurações de paginação
     * @return página com lista de empréstimos
     */
    public Page<LoanDto> findAll(Pageable pageable) {
        log.debug("Finding all loans with pagination: {}", pageable);
        return loanRepository.findAll(pageable)
                .map(loanMapper::toDto);
    }

    /**
     * Busca um empréstimo pelo ID incluindo detalhes completos (livro, membro)
     * @param id identificador do empréstimo
     * @return dados do empréstimo encontrado com relacionamentos
     * @throws ResourceNotFoundException se o empréstimo não for encontrado
     */
    public LoanDto findById(Long id) {
        log.debug("Finding loan by id: {}", id);
        Loan loan = loanRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + id));
        return loanMapper.toDto(loan);
    }

    /**
     * Cria um novo empréstimo no sistema aplicando todas as regras de negócio
     * Validações realizadas:
     * - Verifica se o membro existe e está ativo
     * - Verifica se o membro não excedeu o limite de empréstimos ativos (5)
     * - Verifica se o livro existe e tem exemplares disponíveis
     * - Define data de empréstimo (hoje) e data de devolução (14 dias)
     * - Atualiza disponibilidade do livro (decrementa 1 exemplar)
     * 
     * @param loanDto dados do empréstimo a ser criado
     * @return dados do empréstimo criado com ID gerado
     * @throws ResourceNotFoundException se o membro ou livro não forem encontrados
     * @throws BusinessException se o membro não estiver ativo, exceder limite de empréstimos ou livro não disponível
     */
    @Transactional
    public LoanDto create(LoanDto loanDto) {
        log.debug("Creating new loan: {}", loanDto);
        
        // Valida se membro existe e está ativo
        Member member = memberRepository.findById(loanDto.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + loanDto.getMemberId()));
        
        if (member.getStatus() != Member.MembershipStatus.ACTIVE) {
            throw new BusinessException("Member is not active and cannot borrow books");
        }

        // Verifica se membro atingiu o limite de empréstimos ativos
        Long activeLoanCount = loanRepository.countActiveLoansByMember(loanDto.getMemberId());
        if (activeLoanCount >= MAX_LOANS_PER_MEMBER) {
            throw new BusinessException("Member has reached the maximum number of active loans (" + MAX_LOANS_PER_MEMBER + ")");
        }

        // Valida se livro existe e está disponível
        Book book = bookRepository.findById(loanDto.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + loanDto.getBookId()));
        
        if (book.getAvailableCopies() <= 0) {
            throw new BusinessException("Book is not available for loan");
        }

        // Cria o empréstimo com datas automáticas
        Loan loan = loanMapper.toEntity(loanDto);
        loan.setBook(book);
        loan.setMember(member);
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(LOAN_PERIOD_DAYS));
        loan.setStatus(Loan.LoanStatus.ACTIVE);

        // Atualiza disponibilidade do livro (decrementa exemplares)
        bookService.updateAvailableCopies(book.getId(), -1);

        Loan savedLoan = loanRepository.save(loan);
        log.info("Created loan with id: {}", savedLoan.getId());
        return loanMapper.toDto(savedLoan);
    }

    /**
     * Processa a devolução de um livro emprestado
     * Validações realizadas:
     * - Verifica se o empréstimo existe
     * - Verifica se o empréstimo está ativo (não pode devolver livro já devolvido)
     * - Define data de devolução (hoje) e altera status para RETURNED
     * - Atualiza disponibilidade do livro (incrementa 1 exemplar)
     * 
     * @param loanId identificador do empréstimo a ser devolvido
     * @return dados do empréstimo atualizado com devolução
     * @throws ResourceNotFoundException se o empréstimo não for encontrado
     * @throws BusinessException se o empréstimo não estiver ativo
     */
    @Transactional
    public LoanDto returnBook(Long loanId) {
        log.debug("Returning book for loan id: {}", loanId);
        Loan loan = loanRepository.findByIdWithDetails(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + loanId));

        if (loan.getStatus() != Loan.LoanStatus.ACTIVE) {
            throw new BusinessException("Loan is not active and cannot be returned");
        }

        loan.setReturnDate(LocalDate.now());
        loan.setStatus(Loan.LoanStatus.RETURNED);

        // Atualiza disponibilidade do livro (incrementa exemplares)
        bookService.updateAvailableCopies(loan.getBook().getId(), 1);

        Loan updatedLoan = loanRepository.save(loan);
        log.info("Returned book for loan id: {}", loanId);
        return loanMapper.toDto(updatedLoan);
    }

    /**
     * Atualiza dados de um empréstimo existente
     * Permite alterar: data de devolução, observações e status
     * Não permite alterar: livro, membro, datas de empréstimo
     * 
     * @param id identificador do empréstimo
     * @param loanDto novos dados do empréstimo
     * @return dados do empréstimo atualizado
     * @throws ResourceNotFoundException se o empréstimo não for encontrado
     */
    @Transactional
    public LoanDto update(Long id, LoanDto loanDto) {
        log.debug("Updating loan with id: {}", id);
        Loan existingLoan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + id));

        existingLoan.setDueDate(loanDto.getDueDate());
        existingLoan.setNotes(loanDto.getNotes());

        if (loanDto.getStatus() != null) {
            existingLoan.setStatus(loanDto.getStatus());
        }

        Loan updatedLoan = loanRepository.save(existingLoan);
        log.info("Updated loan with id: {}", updatedLoan.getId());
        return loanMapper.toDto(updatedLoan);
    }

    /**
     * Remove um empréstimo do sistema
     * Atenção: operação irreversível, remover apenas registros de teste
     * 
     * @param id identificador do empréstimo a ser removido
     * @throws ResourceNotFoundException se o empréstimo não for encontrado
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Deleting loan with id: {}", id);
        if (!loanRepository.existsById(id)) {
            throw new ResourceNotFoundException("Loan not found with id: " + id);
        }
        loanRepository.deleteById(id);
        log.info("Deleted loan with id: {}", id);
    }

    /**
     * Busca empréstimos de um membro específico com paginação
     * @param memberId identificador do membro
     * @param pageable configurações de paginação
     * @return página com empréstimos do membro
     */
    public Page<LoanDto> findByMember(Long memberId, Pageable pageable) {
        log.debug("Finding loans by member id: {} with pagination: {}", memberId, pageable);
        return loanRepository.findByMemberId(memberId, pageable)
                .map(loanMapper::toDto);
    }

    /**
     * Busca empréstimos de um livro específico com paginação
     * @param bookId identificador do livro
     * @param pageable configurações de paginação
     * @return página com empréstimos do livro
     */
    public Page<LoanDto> findByBook(Long bookId, Pageable pageable) {
        log.debug("Finding loans by book id: {} with pagination: {}", bookId, pageable);
        return loanRepository.findByBookId(bookId, pageable)
                .map(loanMapper::toDto);
    }

    /**
     * Busca empréstimos por status específico com paginação
     * @param status status do empréstimo (ACTIVE, RETURNED, OVERDUE)
     * @param pageable configurações de paginação
     * @return página com empréstimos do status especificado
     */
    public Page<LoanDto> findByStatus(Loan.LoanStatus status, Pageable pageable) {
        log.debug("Finding loans by status: {} with pagination: {}", status, pageable);
        return loanRepository.findByStatus(status, pageable)
                .map(loanMapper::toDto);
    }

    /**
     * Busca todos os empréstimos em atraso (data de devolução vencida)
     * Retorna empréstimos com status ACTIVE cuja data de devolução é anterior a hoje
     * 
     * @return lista de empréstimos em atraso
     */
    public List<LoanDto> findOverdueLoans() {
        log.debug("Finding overdue loans");
        List<Loan> overdueLoans = loanRepository.findOverdueLoans(LocalDate.now());
        return loanMapper.toDtoList(overdueLoans);
    }

    /**
     * Atualiza automaticamente o status de empréstimos em atraso
     * Processo batch que identifica empréstimos ativos vencidos e altera status para OVERDUE
     * Deve ser executado periodicamente (ex: job diário)
     */
    @Transactional
    public void updateOverdueLoans() {
        log.debug("Updating overdue loan statuses");
        List<Loan> overdueLoans = loanRepository.findOverdueLoans(LocalDate.now());
        for (Loan loan : overdueLoans) {
            loan.setStatus(Loan.LoanStatus.OVERDUE);
        }
        loanRepository.saveAll(overdueLoans);
        log.info("Updated {} loans to overdue status", overdueLoans.size());
    }

    /**
     * Busca empréstimos criados em um período específico com paginação
     * @param startDate data inicial do período
     * @param endDate data final do período
     * @param pageable configurações de paginação
     * @return página com empréstimos do período especificado
     */
    public Page<LoanDto> findByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.debug("Finding loans between {} and {} with pagination: {}", startDate, endDate, pageable);
        return loanRepository.findByLoanDateBetween(startDate, endDate, pageable)
                .map(loanMapper::toDto);
    }
}