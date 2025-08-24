package com.example.port_in_scan.domain.search.dto;

import com.example.port_in_scan.domain.portfolio.entity.PortfolioCategory;
import com.example.port_in_scan.domain.search.entity.SearchType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequestDto {

    @NotBlank(message = "검색어는 필수입니다.")
    @Size(min = 1, max = 100, message = "검색어는 1자 이상 100자 이하여야 합니다.")
    private String keyword;

    private SearchType searchType;

    private PortfolioCategory category;

    private List<String> techStacks;

    private List<String> tags;

    private String author;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 10;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String sortDir = "desc";
}