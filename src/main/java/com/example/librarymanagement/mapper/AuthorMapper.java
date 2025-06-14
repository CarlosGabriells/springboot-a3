package com.example.librarymanagement.mapper;

import com.example.librarymanagement.dto.AuthorDto;
import com.example.librarymanagement.entity.Author;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper responsável pela conversão entre entidade Author e AuthorDto.
 * Utiliza MapStruct para gerar automaticamente as implementações de mapeamento,
 * incluindo conversão de lista de livros para resumo simplificado.
 */
@Mapper(componentModel = "spring")
public interface AuthorMapper {

    /**
     * Converte uma entidade Author para AuthorDto.
     * Inclui mapeamento dos livros para formato de resumo simplificado.
     * @param author entidade Author a ser convertida
     * @return AuthorDto com dados do autor e resumo dos livros
     */
    @Mapping(target = "books", source = "books", qualifiedByName = "mapBookSummary")
    AuthorDto toDto(Author author);

    /**
     * Converte um AuthorDto para entidade Author.
     * Ignora a lista de livros para evitar problemas de mapeamento circular.
     * @param authorDto DTO do autor a ser convertido
     * @return entidade Author sem livros associados
     */
    @Mapping(target = "books", ignore = true)
    Author toEntity(AuthorDto authorDto);

    /**
     * Converte uma lista de entidades Author para lista de AuthorDto.
     * @param authors lista de entidades Author
     * @return lista de AuthorDto correspondente
     */
    List<AuthorDto> toDtoList(List<Author> authors);

    /**
     * Método auxiliar para mapear lista de livros para resumo simplificado.
     * Evita carregar dados desnecessários ao retornar informações do autor.
     * @param books lista de entidades Book
     * @return lista de BookSummaryDto com informações básicas dos livros
     */
    @Named("mapBookSummary")
    default List<AuthorDto.BookSummaryDto> mapBookSummary(List<com.example.librarymanagement.entity.Book> books) {
        if (books == null) return null;
        return books.stream()
                .map(book -> AuthorDto.BookSummaryDto.builder()
                        .id(book.getId())
                        .title(book.getTitle())
                        .isbn(book.getIsbn())
                        .publicationDate(book.getPublicationDate())
                        .build())
                .toList();
    }
}