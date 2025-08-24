package com.example.port_in_scan.domain.search.controller;

import com.example.port_in_scan.domain.search.dto.SearchRequestDto;
import com.example.port_in_scan.domain.search.dto.SearchResponseDto;
import com.example.port_in_scan.domain.search.entity.SearchHistory;
import com.example.port_in_scan.domain.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Search", description = "검색 관리 API")
@RestController
@RequestMapping("/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "포트폴리오 검색", description = "다양한 조건으로 포트폴리오를 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 검색 조건")
    })
    @PostMapping
    public ResponseEntity<SearchResponseDto> searchPortfolios(
            @Valid @RequestBody SearchRequestDto requestDto,
            HttpServletRequest request,
            Authentication authentication) {
        
        String userEmail = authentication != null ? authentication.getName() : null;
        String ipAddress = getClientIpAddress(request);
        
        SearchResponseDto response = searchService.search(requestDto, userEmail, ipAddress);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "간단 검색", description = "키워드로 간단하게 포트폴리오를 검색합니다.")
    @GetMapping
    public ResponseEntity<SearchResponseDto> simpleSearch(
            @Parameter(description = "검색 키워드", required = true)
            @RequestParam String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 기준", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "정렬 방향", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request,
            Authentication authentication) {
        
        SearchRequestDto requestDto = SearchRequestDto.builder()
                .keyword(keyword)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build();
        
        String userEmail = authentication != null ? authentication.getName() : null;
        String ipAddress = getClientIpAddress(request);
        
        SearchResponseDto response = searchService.search(requestDto, userEmail, ipAddress);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "인기 검색어 조회", description = "인기 검색어 목록을 조회합니다.")
    @GetMapping("/popular-keywords")
    public ResponseEntity<List<String>> getPopularKeywords(
            @Parameter(description = "조회할 키워드 개수", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        
        List<String> popularKeywords = searchService.getPopularKeywords(limit);
        return ResponseEntity.ok(popularKeywords);
    }

    @Operation(summary = "최근 검색어 조회", description = "최근 검색어 목록을 조회합니다.")
    @GetMapping("/recent-keywords")
    public ResponseEntity<List<String>> getRecentKeywords(
            @Parameter(description = "조회할 키워드 개수", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        
        List<String> recentKeywords = searchService.getRecentSearchKeywords(limit);
        return ResponseEntity.ok(recentKeywords);
    }

    @Operation(summary = "검색어 자동완성", description = "입력된 쿼리를 기반으로 검색어를 추천합니다.")
    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getKeywordSuggestions(
            @Parameter(description = "검색 쿼리", required = true)
            @RequestParam String query,
            @Parameter(description = "추천할 키워드 개수", example = "5")
            @RequestParam(defaultValue = "5") int limit) {
        
        List<String> suggestions = searchService.getKeywordSuggestions(query, limit);
        return ResponseEntity.ok(suggestions);
    }

    @Operation(summary = "내 검색 히스토리 조회", description = "로그인한 사용자의 검색 히스토리를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/history")
    public ResponseEntity<Page<SearchHistory>> getMySearchHistory(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by("searchedAt").descending());
        
        Page<SearchHistory> searchHistory = searchService.getUserSearchHistory(userEmail, pageable);
        return ResponseEntity.ok(searchHistory);
    }

    @Operation(summary = "내 최근 검색어 조회", description = "로그인한 사용자의 최근 검색어를 조회합니다.")
    @GetMapping("/history/recent-keywords")
    public ResponseEntity<List<String>> getMyRecentKeywords(
            @Parameter(description = "조회할 키워드 개수", example = "10")
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        List<String> recentKeywords = searchService.getUserRecentKeywords(userEmail, limit);
        return ResponseEntity.ok(recentKeywords);
    }

    @Operation(summary = "검색 통계 조회", description = "검색 통계 정보를 조회합니다. (관리자 전용)")
    @GetMapping("/statistics")
    public ResponseEntity<List<Object[]>> getSearchStatistics(
            @Parameter(description = "통계 기간 (일)", example = "7")
            @RequestParam(defaultValue = "7") int days,
            Authentication authentication) {
        
        // TODO: 관리자 권한 체크 로직 추가
        List<Object[]> statistics = searchService.getSearchStatistics(days);
        return ResponseEntity.ok(statistics);
    }

    // IP 주소 추출 유틸리티 메서드
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
}