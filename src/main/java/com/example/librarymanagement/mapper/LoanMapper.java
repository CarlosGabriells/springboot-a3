package com.example.librarymanagement.mapper;

import com.example.librarymanagement.dto.LoanDto;
import com.example.librarymanagement.entity.Loan;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper responsável pela conversão entre entidade Loan e LoanDto.
 * Utiliza MapStruct para gerar automaticamente as implementações de mapeamento,
 * incluindo conversão de relacionamentos com Book e Member para resumos simplificados.
 * Gerencia múltiplas estratégias de mapeamento para evitar referências circulares.
 */
@Mapper(componentModel = "spring")
public interface LoanMapper {

    /**
     * Converte uma entidade Loan para LoanDto.
     * Mapeia os IDs dos relacionamentos (bookId, memberId) e cria resumos
     * simplificados das entidades relacionadas para evitar carregamento excessivo.
     * Utiliza métodos auxiliares especializados para cada tipo de relacionamento.
     * @param loan entidade Loan a ser convertida
     * @return LoanDto com dados completos do empréstimo e resumos das entidades relacionadas
     */
    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "book", source = "book", qualifiedByName = "mapBookSummary")
    @Mapping(target = "member", source = "member", qualifiedByName = "mapMemberSummary")
    LoanDto toDto(Loan loan);

    /**
     * Converte um LoanDto para entidade Loan.
     * Ignora os relacionamentos complexos (book, member) para evitar problemas
     * de mapeamento circular, já que estes relacionamentos são bidirecionais.
     * As entidades relacionadas devem ser definidas separadamente pelos serviços.
     * @param loanDto DTO do empréstimo a ser convertido
     * @return entidade Loan sem relacionamentos associados
     */
    @Mapping(target = "book", ignore = true)
    @Mapping(target = "member", ignore = true)
    Loan toEntity(LoanDto loanDto);

    /**
     * Converte uma lista de entidades Loan para lista de LoanDto.
     * Aplica o mapeamento individual para cada empréstimo da lista,
     * incluindo seus respectivos resumos de livro e membro.
     * @param loans lista de entidades Loan
     * @return lista de LoanDto correspondente
     */
    List<LoanDto> toDtoList(List<Loan> loans);

    /**
     * Método auxiliar para mapear livro para resumo simplificado.
     * Cria uma representação resumida do livro emprestado,
     * incluindo informações essenciais como título, ISBN e nome do autor.
     * Constrói o nome completo do autor concatenando firstName e lastName,
     * com tratamento seguro para casos onde o autor pode ser nulo.
     * @param book entidade Book a ser mapeada
     * @return BookSummaryDto com informações básicas do livro e autor
     */
    @Named("mapBookSummary")
    default LoanDto.BookSummaryDto mapBookSummary(com.example.librarymanagement.entity.Book book) {
        if (book == null) return null;
        String authorName = book.getAuthor() != null ? 
            book.getAuthor().getFirstName() + " " + book.getAuthor().getLastName() : null;
        return LoanDto.BookSummaryDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .authorName(authorName)
                .build();
    }

    /**
     * Método auxiliar para mapear membro para resumo simplificado.
     * Cria uma representação resumida do membro que realizou o empréstimo,
     * incluindo informações básicas de identificação e contato.
     * Evita carregar dados desnecessários como lista completa de empréstimos.
     * @param member entidade Member a ser mapeada
     * @return MemberSummaryDto com informações básicas do membro
     */
    @Named("mapMemberSummary")
    default LoanDto.MemberSummaryDto mapMemberSummary(com.example.librarymanagement.entity.Member member) {
        if (member == null) return null;
        return LoanDto.MemberSummaryDto.builder()
                .id(member.getId())
                .firstName(member.getFirstName())
                .lastName(member.getLastName())
                .email(member.getEmail())
                .build();
    }
}