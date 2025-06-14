package com.example.librarymanagement.mapper;

import com.example.librarymanagement.dto.BookDto;
import com.example.librarymanagement.entity.Book;
import com.example.librarymanagement.entity.Category;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper responsável pela conversão entre entidade Book e BookDto.
 * Utiliza MapStruct para gerar automaticamente as implementações de mapeamento,
 * incluindo conversão de relacionamentos complexos como autor e categorias.
 * Implementa estratégias específicas para evitar referências circulares.
 */
@Mapper(componentModel = "spring")
public interface BookMapper {

    /**
     * Converte uma entidade Book para BookDto.
     * Mapeia o ID do autor, informações resumidas do autor e categorias.
     * Utiliza métodos auxiliares para criar representações simplificadas.
     * @param book entidade Book a ser convertida
     * @return BookDto com dados completos do livro, incluindo autor e categorias
     */
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "author", source = "author", qualifiedByName = "mapAuthorSummary")
    @Mapping(target = "categoryIds", source = "categories", qualifiedByName = "mapCategoryIds")
    @Mapping(target = "categories", source = "categories", qualifiedByName = "mapCategorySummary")
    BookDto toDto(Book book);

    /**
     * Converte um BookDto para entidade Book.
     * Ignora relacionamentos complexos (autor, categorias, empréstimos) para evitar
     * problemas de mapeamento circular. Estes devem ser definidos separadamente.
     * @param bookDto DTO do livro a ser convertido
     * @return entidade Book sem relacionamentos associados
     */
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "loans", ignore = true)
    Book toEntity(BookDto bookDto);

    /**
     * Converte uma lista de entidades Book para lista de BookDto.
     * @param books lista de entidades Book
     * @return lista de BookDto correspondente
     */
    List<BookDto> toDtoList(List<Book> books);

    /**
     * Método auxiliar para mapear autor para resumo simplificado.
     * Cria um DTO resumido com informações básicas do autor,
     * evitando carregar dados desnecessários como lista de livros.
     * @param author entidade Author a ser mapeada
     * @return AuthorSummaryDto com informações básicas do autor
     */
    @Named("mapAuthorSummary")
    default BookDto.AuthorSummaryDto mapAuthorSummary(com.example.librarymanagement.entity.Author author) {
        if (author == null) return null;
        return BookDto.AuthorSummaryDto.builder()
                .id(author.getId())
                .firstName(author.getFirstName())
                .lastName(author.getLastName())
                .build();
    }

    /**
     * Método auxiliar para extrair IDs das categorias.
     * Converte lista de entidades Category em lista de IDs,
     * útil para operações que precisam apenas dos identificadores.
     * @param categories lista de entidades Category
     * @return lista de IDs das categorias
     */
    @Named("mapCategoryIds")
    default List<Long> mapCategoryIds(List<Category> categories) {
        if (categories == null) return null;
        return categories.stream().map(Category::getId).toList();
    }

    /**
     * Método auxiliar para mapear categorias para resumo simplificado.
     * Cria uma lista de DTOs resumidos com informações básicas das categorias,
     * evitando carregar dados desnecessários como lista de livros.
     * @param categories lista de entidades Category
     * @return lista de CategorySummaryDto com informações básicas das categorias
     */
    @Named("mapCategorySummary")
    default List<BookDto.CategorySummaryDto> mapCategorySummary(List<Category> categories) {
        if (categories == null) return null;
        return categories.stream()
                .map(category -> BookDto.CategorySummaryDto.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build())
                .toList();
    }
}