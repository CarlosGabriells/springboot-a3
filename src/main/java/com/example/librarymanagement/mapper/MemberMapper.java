package com.example.librarymanagement.mapper;

import com.example.librarymanagement.dto.MemberDto;
import com.example.librarymanagement.entity.Member;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper responsável pela conversão entre entidade Member e MemberDto.
 * Utiliza MapStruct para gerar automaticamente as implementações de mapeamento,
 * incluindo conversão de lista de empréstimos associados para resumo simplificado.
 * Implementa estratégias para evitar referências circulares no relacionamento Member-Loan.
 */
@Mapper(componentModel = "spring")
public interface MemberMapper {

    /**
     * Converte uma entidade Member para MemberDto.
     * Inclui mapeamento dos empréstimos associados para formato de resumo simplificado,
     * mostrando informações essenciais sobre os livros emprestados e seus status.
     * @param member entidade Member a ser convertida
     * @return MemberDto com dados do membro e resumo dos empréstimos associados
     */
    @Mapping(target = "loans", source = "loans", qualifiedByName = "mapLoanSummary")
    MemberDto toDto(Member member);

    /**
     * Converte um MemberDto para entidade Member.
     * Ignora a lista de empréstimos para evitar problemas de mapeamento circular,
     * já que o relacionamento Member-Loan é bidirecional (One-to-Many).
     * Os empréstimos devem ser gerenciados separadamente pelos serviços de negócio.
     * @param memberDto DTO do membro a ser convertido
     * @return entidade Member sem empréstimos associados
     */
    @Mapping(target = "loans", ignore = true)
    Member toEntity(MemberDto memberDto);

    /**
     * Converte uma lista de entidades Member para lista de MemberDto.
     * Aplica o mapeamento individual para cada membro da lista,
     * incluindo seus respectivos empréstimos em formato resumido.
     * @param members lista de entidades Member
     * @return lista de MemberDto correspondente
     */
    List<MemberDto> toDtoList(List<Member> members);

    /**
     * Método auxiliar para mapear lista de empréstimos para resumo simplificado.
     * Cria representações resumidas dos empréstimos do membro,
     * incluindo informações essenciais sobre o livro emprestado, datas e status.
     * Evita carregar dados complexos completos do livro e suas relações.
     * Converte o enum LoanStatus para string para facilitar a serialização.
     * @param loans lista de entidades Loan associadas ao membro
     * @return lista de LoanSummaryDto com informações básicas dos empréstimos
     */
    @Named("mapLoanSummary")
    default List<MemberDto.LoanSummaryDto> mapLoanSummary(List<com.example.librarymanagement.entity.Loan> loans) {
        if (loans == null) return null;
        return loans.stream()
                .map(loan -> MemberDto.LoanSummaryDto.builder()
                        .id(loan.getId())
                        .bookTitle(loan.getBook().getTitle())
                        .loanDate(loan.getLoanDate())
                        .dueDate(loan.getDueDate())
                        .status(loan.getStatus().name())
                        .build())
                .toList();
    }
}