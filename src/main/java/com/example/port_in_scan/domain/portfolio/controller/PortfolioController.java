package com.example.port_in_scan.domain.portfolio.controller;

import com.example.port_in_scan.domain.portfolio.dto.PortfolioCreateRequestDto;
import com.example.port_in_scan.domain.portfolio.dto.PortfolioResponseDto;
import com.example.port_in_scan.domain.portfolio.dto.PortfolioUpdateRequestDto;
import com.example.port_in_scan.domain.portfolio.entity.PortfolioCategory;
import com.example.port_in_scan.domain.portfolio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Portfolio", description = "포트폴리오 관리 API")
@RestController
@RequestMapping("/v1/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @Operation(summary = "포트폴리오 생성", description = "새로운 포트폴리오를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "포트폴리오 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping
    public ResponseEntity<PortfolioResponseDto> createPortfolio(
            @Valid @RequestBody PortfolioCreateRequestDto requestDto,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        PortfolioResponseDto response = portfolioService.createPortfolio(userEmail, requestDto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "포트폴리오 상세 조회", description = "포트폴리오 ID로 상세 정보를 조회합니다. (조회수 증가)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "포트폴리오를 찾을 수 없음")
    })
    @GetMapping("/{portfolioId}")
    public ResponseEntity<PortfolioResponseDto> getPortfolio(
            @Parameter(description = "포트폴리오 ID", required = true)
            @PathVariable Long portfolioId) {
        
        PortfolioResponseDto response = portfolioService.getPortfolioWithViewCount(portfolioId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "포트폴리오 수정", description = "기존 포트폴리오를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음"),
            @ApiResponse(responseCode = "404", description = "포트폴리오를 찾을 수 없음")
    })
    @PutMapping("/{portfolioId}")
    public ResponseEntity<PortfolioResponseDto> updatePortfolio(
            @Parameter(description = "포트폴리오 ID", required = true)
            @PathVariable Long portfolioId,
            @Valid @RequestBody PortfolioUpdateRequestDto requestDto,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        PortfolioResponseDto response = portfolioService.updatePortfolio(portfolioId, userEmail, requestDto);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "포트폴리오 삭제", description = "포트폴리오를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "포트폴리오를 찾을 수 없음")
    })
    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<Void> deletePortfolio(
            @Parameter(description = "포트폴리오 ID", required = true)
            @PathVariable Long portfolioId,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        portfolioService.deletePortfolio(portfolioId, userEmail);
        
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "전체 포트폴리오 조회", description = "활성화된 모든 포트폴리오를 페이징 조회합니다.")
    @GetMapping
    public ResponseEntity<Page<PortfolioResponseDto>> getAllPortfolios(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 기준", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "정렬 방향", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<PortfolioResponseDto> portfolios = portfolioService.getAllPortfolios(pageable);
        return ResponseEntity.ok(portfolios);
    }

    @Operation(summary = "카테고리별 포트폴리오 조회", description = "특정 카테고리의 포트폴리오를 조회합니다.")
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<PortfolioResponseDto>> getPortfoliosByCategory(
            @Parameter(description = "포트폴리오 카테고리", required = true)
            @PathVariable PortfolioCategory category,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PortfolioResponseDto> portfolios = portfolioService.getPortfoliosByCategory(category, pageable);
        
        return ResponseEntity.ok(portfolios);
    }

    @Operation(summary = "내 포트폴리오 조회", description = "로그인한 사용자의 포트폴리오를 조회합니다.")
    @GetMapping("/my")
    public ResponseEntity<Page<PortfolioResponseDto>> getMyPortfolios(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<PortfolioResponseDto> portfolios = portfolioService.getMyPortfolios(userEmail, pageable);
        return ResponseEntity.ok(portfolios);
    }

    @Operation(summary = "포트폴리오 검색", description = "제목 또는 설명으로 포트폴리오를 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<Page<PortfolioResponseDto>> searchPortfolios(
            @Parameter(description = "검색 키워드", required = true)
            @RequestParam String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PortfolioResponseDto> portfolios = portfolioService.searchPortfolios(keyword, pageable);
        
        return ResponseEntity.ok(portfolios);
    }

    @Operation(summary = "기술 스택으로 검색", description = "기술 스택으로 포트폴리오를 검색합니다.")
    @GetMapping("/search/tech-stacks")
    public ResponseEntity<Page<PortfolioResponseDto>> searchByTechStacks(
            @Parameter(description = "기술 스택 목록", required = true)
            @RequestParam List<String> techStacks,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PortfolioResponseDto> portfolios = portfolioService.searchByTechStacks(techStacks, pageable);
        
        return ResponseEntity.ok(portfolios);
    }

    @Operation(summary = "태그로 검색", description = "태그로 포트폴리오를 검색합니다.")
    @GetMapping("/search/tags")
    public ResponseEntity<Page<PortfolioResponseDto>> searchByTags(
            @Parameter(description = "태그 목록", required = true)
            @RequestParam List<String> tags,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PortfolioResponseDto> portfolios = portfolioService.searchByTags(tags, pageable);
        
        return ResponseEntity.ok(portfolios);
    }

    @Operation(summary = "인기 포트폴리오 조회", description = "조회수가 높은 포트폴리오를 조회합니다.")
    @GetMapping("/popular")
    public ResponseEntity<Page<PortfolioResponseDto>> getPopularPortfolios(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PortfolioResponseDto> portfolios = portfolioService.getPopularPortfolios(pageable);
        
        return ResponseEntity.ok(portfolios);
    }

    @Operation(summary = "좋아요 많은 포트폴리오 조회", description = "좋아요가 많은 포트폴리오를 조회합니다.")
    @GetMapping("/most-liked")
    public ResponseEntity<Page<PortfolioResponseDto>> getMostLikedPortfolios(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PortfolioResponseDto> portfolios = portfolioService.getMostLikedPortfolios(pageable);
        
        return ResponseEntity.ok(portfolios);
    }

    @Operation(summary = "최신 포트폴리오 조회", description = "최근에 등록된 포트폴리오를 조회합니다.")
    @GetMapping("/latest")
    public ResponseEntity<Page<PortfolioResponseDto>> getLatestPortfolios(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PortfolioResponseDto> portfolios = portfolioService.getLatestPortfolios(pageable);
        
        return ResponseEntity.ok(portfolios);
    }

    @Operation(summary = "포트폴리오 좋아요", description = "포트폴리오에 좋아요를 추가합니다.")
    @PostMapping("/{portfolioId}/like")
    public ResponseEntity<Void> likePortfolio(
            @Parameter(description = "포트폴리오 ID", required = true)
            @PathVariable Long portfolioId) {
        
        portfolioService.likePortfolio(portfolioId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "포트폴리오 좋아요 취소", description = "포트폴리오 좋아요를 취소합니다.")
    @DeleteMapping("/{portfolioId}/like")
    public ResponseEntity<Void> unlikePortfolio(
            @Parameter(description = "포트폴리오 ID", required = true)
            @PathVariable Long portfolioId) {
        
        portfolioService.unlikePortfolio(portfolioId);
        return ResponseEntity.ok().build();
    }
}