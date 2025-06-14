package com.example.librarymanagement.mapper;

import com.example.librarymanagement.dto.CategoryDto;
import com.example.librarymanagement.entity.Category;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper responsável pela conversão entre entidade Category e CategoryDto.
 * Utiliza MapStruct para gerar automaticamente as implementações de mapeamento,
 * incluindo conversão de lista de livros associados para resumo simplificado.
 * Implementa estratégias para evitar referências circulares em relacionamentos bidirecionais.
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    /**
     * Converte uma entidade Category para CategoryDto.
     * Inclui mapeamento dos livros associados para formato de resumo simplificado,
     * evitando carregar dados desnecessários dos relacionamentos complexos.
     * @param category entidade Category a ser convertida
     * @return CategoryDto com dados da categoria e resumo dos livros associados
     */
    @Mapping(target = "books", source = "books", qualifiedByName = "mapBookSummary")
    CategoryDto toDto(Category category);

    /**
     * Converte um CategoryDto para entidade Category.
     * Ignora a lista de livros para evitar problemas de mapeamento circular,
     * já que o relacionamento Category-Book é bidirecional (Many-to-Many).
     * @param categoryDto DTO da categoria a ser convertido
     * @return entidade Category sem livros associados
     */
    @Mapping(target = "books", ignore = true)
    Category toEntity(CategoryDto categoryDto);

    /**
     * Converte uma lista de entidades Category para lista de CategoryDto.
     * Aplica o mapeamento individual para cada categoria da lista.
     * @param categories lista de entidades Category
     * @return lista de CategoryDto correspondente
     */
    List<CategoryDto> toDtoList(List<Category> categories);

    /**
     * Método auxiliar para mapear lista de livros para resumo simplificado.
     * Cria representações resumidas dos livros associados à categoria,
     * incluindo apenas informações essenciais (ID, título, ISBN).
     * Evita carregar dados complexos como autor completo, outras categorias e empréstimos.
     * @param books lista de entidades Book associadas à categoria
     * @return lista de BookSummaryDto com informações básicas dos livros
     */
    @Named("mapBookSummary")
    default List<CategoryDto.BookSummaryDto> mapBookSummary(List<com.example.librarymanagement.entity.Book> books) {
        if (books == null) return null;
        return books.stream()
                .map(book -> CategoryDto.BookSummaryDto.builder()
                        .id(book.getId())
                        .title(book.getTitle())
                        .isbn(book.getIsbn())
                        .build())
                .toList();
    }
}