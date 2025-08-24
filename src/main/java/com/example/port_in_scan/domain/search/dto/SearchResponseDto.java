package com.example.port_in_scan.domain.search.dto;

import com.example.port_in_scan.domain.portfolio.dto.PortfolioResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponseDto {

    private String keyword;
    private Long totalResults;
    private Integer totalPages;
    private Integer currentPage;
    private Integer pageSize;
    private List<PortfolioResponseDto> portfolios;
    private List<String> relatedKeywords;
    private Long searchTime; // 검색 소요 시간 (ms)

    public static SearchResponseDto from(String keyword, Page<PortfolioResponseDto> portfolioPage, 
                                        List<String> relatedKeywords, Long searchTime) {
        return SearchResponseDto.builder()
                .keyword(keyword)
                .totalResults(portfolioPage.getTotalElements())
                .totalPages(portfolioPage.getTotalPages())
                .currentPage(portfolioPage.getNumber())
                .pageSize(portfolioPage.getSize())
                .portfolios(portfolioPage.getContent())
                .relatedKeywords(relatedKeywords)
                .searchTime(searchTime)
                .build();
    }
}