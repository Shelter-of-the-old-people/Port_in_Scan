package com.example.port_in_scan.domain.portfolio.dto;

import com.example.port_in_scan.domain.portfolio.entity.PortfolioCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class PortfolioCreateRequestDto {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다.")
    private String title;

    @Size(max = 5000, message = "설명은 5000자 이하여야 합니다.")
    private String description;

    private String githubUrl;

    private String demoUrl;

    @NotNull(message = "카테고리는 필수입니다.")
    private PortfolioCategory category;

    private List<String> techStacks;

    private List<String> tags;
}