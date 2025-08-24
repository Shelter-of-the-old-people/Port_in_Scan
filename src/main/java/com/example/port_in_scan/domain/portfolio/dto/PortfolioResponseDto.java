package com.example.port_in_scan.domain.portfolio.dto;

import com.example.port_in_scan.domain.portfolio.entity.Portfolio;
import com.example.port_in_scan.domain.portfolio.entity.PortfolioCategory;
import com.example.port_in_scan.domain.portfolio.entity.PortfolioStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioResponseDto {

    private Long portfolioId;
    private String title;
    private String description;
    private String githubUrl;
    private String demoUrl;
    private String thumbnailUrl;
    private PortfolioCategory category;
    private PortfolioStatus status;
    private Long viewCount;
    private Long likeCount;
    private List<String> techStacks;
    private List<String> tags;
    private String authorUsername;
    private String authorEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PortfolioResponseDto from(Portfolio portfolio) {
        return PortfolioResponseDto.builder()
                .portfolioId(portfolio.getPortfolioId())
                .title(portfolio.getTitle())
                .description(portfolio.getDescription())
                .githubUrl(portfolio.getGithubUrl())
                .demoUrl(portfolio.getDemoUrl())
                .thumbnailUrl(portfolio.getThumbnailUrl())
                .category(portfolio.getCategory())
                .status(portfolio.getStatus())
                .viewCount(portfolio.getViewCount())
                .likeCount(portfolio.getLikeCount())
                .techStacks(portfolio.getTechStacks())
                .tags(portfolio.getTags())
                .authorUsername(portfolio.getUser().getUsername())
                .authorEmail(portfolio.getUser().getEmail())
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .build();
    }
}