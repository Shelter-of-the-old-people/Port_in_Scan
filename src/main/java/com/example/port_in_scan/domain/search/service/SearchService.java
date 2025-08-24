package com.example.port_in_scan.domain.search.service;

import com.example.port_in_scan.domain.member.entity.User;
import com.example.port_in_scan.domain.member.repository.UserRepository;
import com.example.port_in_scan.domain.portfolio.dto.PortfolioResponseDto;
import com.example.port_in_scan.domain.portfolio.entity.PortfolioStatus;
import com.example.port_in_scan.domain.portfolio.repository.PortfolioRepository;
import com.example.port_in_scan.domain.search.dto.SearchRequestDto;
import com.example.port_in_scan.domain.search.dto.SearchResponseDto;
import com.example.port_in_scan.domain.search.entity.PopularSearch;
import com.example.port_in_scan.domain.search.entity.SearchHistory;
import com.example.port_in_scan.domain.search.entity.SearchType;
import com.example.port_in_scan.domain.search.repository.PopularSearchRepository;
import com.example.port_in_scan.domain.search.repository.SearchHistoryRepository;
import com.example.port_in_scan.exception.AppException;
import com.example.port_in_scan.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SearchService {

    private final PortfolioRepository portfolioRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final PopularSearchRepository popularSearchRepository;
    private final UserRepository userRepository;

    @Transactional
    public SearchResponseDto search(SearchRequestDto requestDto, String userEmail, String ipAddress) {
        long startTime = System.currentTimeMillis();
        
        // 페이징 설정
        Sort sort = requestDto.getSortDir().equalsIgnoreCase("desc") ? 
                   Sort.by(requestDto.getSortBy()).descending() : Sort.by(requestDto.getSortBy()).ascending();
        Pageable pageable = PageRequest.of(requestDto.getPage(), requestDto.getSize(), sort);

        // 검색 실행
        Page<PortfolioResponseDto> portfolioPage = executeSearch(requestDto, pageable);
        
        // 관련 키워드 생성
        List<String> relatedKeywords = generateRelatedKeywords(requestDto.getKeyword());
        
        long searchTime = System.currentTimeMillis() - startTime;
        
        // 검색 히스토리 저장 (비동기적으로 처리 가능)
        saveSearchHistory(requestDto, userEmail, ipAddress, portfolioPage.getTotalElements());
        
        // 인기 검색어 업데이트
        updatePopularSearch(requestDto.getKeyword());
        
        return SearchResponseDto.from(requestDto.getKeyword(), portfolioPage, relatedKeywords, searchTime);
    }

    private Page<PortfolioResponseDto> executeSearch(SearchRequestDto requestDto, Pageable pageable) {
        // 검색 타입에 따른 분기 처리
        switch (requestDto.getSearchType() != null ? requestDto.getSearchType() : SearchType.KEYWORD) {
            case TECH_STACK:
                if (requestDto.getTechStacks() != null && !requestDto.getTechStacks().isEmpty()) {
                    return portfolioRepository.findByTechStacksInAndStatus(
                            requestDto.getTechStacks(), PortfolioStatus.ACTIVE, pageable)
                            .map(PortfolioResponseDto::from);
                }
                break;
            case TAG:
                if (requestDto.getTags() != null && !requestDto.getTags().isEmpty()) {
                    return portfolioRepository.findByTagsInAndStatus(
                            requestDto.getTags(), PortfolioStatus.ACTIVE, pageable)
                            .map(PortfolioResponseDto::from);
                }
                break;
            case CATEGORY:
                if (requestDto.getCategory() != null) {
                    return portfolioRepository.findByStatusAndCategory(
                            PortfolioStatus.ACTIVE, requestDto.getCategory(), pageable)
                            .map(PortfolioResponseDto::from);
                }
                break;
            case COMPLEX:
                return portfolioRepository.findByComplexSearch(
                        requestDto.getKeyword(),
                        requestDto.getCategory(),
                        requestDto.getTechStacks() != null && !requestDto.getTechStacks().isEmpty() ? 
                                requestDto.getTechStacks().get(0) : null,
                        requestDto.getTags() != null && !requestDto.getTags().isEmpty() ? 
                                requestDto.getTags().get(0) : null,
                        PortfolioStatus.ACTIVE,
                        pageable)
                        .map(PortfolioResponseDto::from);
            default:
                // 기본 키워드 검색
                return portfolioRepository.findByTitleOrDescriptionContainingAndStatus(
                        requestDto.getKeyword(), PortfolioStatus.ACTIVE, pageable)
                        .map(PortfolioResponseDto::from);
        }
        
        // 기본 키워드 검색으로 폴백
        return portfolioRepository.findByTitleOrDescriptionContainingAndStatus(
                requestDto.getKeyword(), PortfolioStatus.ACTIVE, pageable)
                .map(PortfolioResponseDto::from);
    }

    @Transactional
    private void saveSearchHistory(SearchRequestDto requestDto, String userEmail, 
                                 String ipAddress, Long resultCount) {
        try {
            User user = null;
            if (userEmail != null) {
                user = userRepository.findByEmail(userEmail).orElse(null);
            }
            
            SearchHistory searchHistory = SearchHistory.createSearchHistory(
                    requestDto.getKeyword(),
                    requestDto.getSearchType() != null ? requestDto.getSearchType() : SearchType.KEYWORD,
                    resultCount,
                    user,
                    ipAddress
            );
            
            searchHistoryRepository.save(searchHistory);
        } catch (Exception e) {
            log.warn("검색 히스토리 저장 실패: {}", e.getMessage());
        }
    }

    @Transactional
    private void updatePopularSearch(String keyword) {
        try {
            Optional<PopularSearch> existingPopularSearch = popularSearchRepository.findByKeyword(keyword);
            
            if (existingPopularSearch.isPresent()) {
                existingPopularSearch.get().incrementSearchCount();
                popularSearchRepository.save(existingPopularSearch.get());
            } else {
                PopularSearch newPopularSearch = PopularSearch.createPopularSearch(keyword);
                popularSearchRepository.save(newPopularSearch);
            }
        } catch (Exception e) {
            log.warn("인기 검색어 업데이트 실패: {}", e.getMessage());
        }
    }

    private List<String> generateRelatedKeywords(String keyword) {
        // 간단한 관련 키워드 생성 로직 (실제로는 더 복잡한 알고리즘 사용)
        List<String> commonTechKeywords = Arrays.asList(
                "React", "Vue", "Angular", "Node.js", "Express", 
                "Spring Boot", "Django", "Flask", "Java", "Python",
                "JavaScript", "TypeScript", "Go", "Kotlin", "Swift"
        );
        
        return commonTechKeywords.stream()
                .filter(tech -> tech.toLowerCase().contains(keyword.toLowerCase()) || 
                               keyword.toLowerCase().contains(tech.toLowerCase()))
                .limit(5)
                .collect(Collectors.toList());
    }

    public List<String> getPopularKeywords(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return popularSearchRepository.findAllByOrderBySearchCountDesc(pageable)
                .getContent()
                .stream()
                .map(PopularSearch::getKeyword)
                .collect(Collectors.toList());
    }

    public List<String> getRecentSearchKeywords(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return popularSearchRepository.findAllByOrderByLastSearchedAtDesc(pageable)
                .getContent()
                .stream()
                .map(PopularSearch::getKeyword)
                .collect(Collectors.toList());
    }

    public Page<SearchHistory> getUserSearchHistory(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND.getMessage(), ErrorCode.USER_NOT_FOUND));
        
        return searchHistoryRepository.findByUserIdxOrderBySearchedAtDesc(user.getIdx(), pageable);
    }

    public List<String> getUserRecentKeywords(String userEmail, int limit) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND.getMessage(), ErrorCode.USER_NOT_FOUND));
        
        Pageable pageable = PageRequest.of(0, limit);
        return searchHistoryRepository.findRecentKeywordsByUser(user.getIdx(), pageable);
    }

    public List<String> getKeywordSuggestions(String query, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return popularSearchRepository.findByKeywordContaining(query, pageable)
                .getContent()
                .stream()
                .map(PopularSearch::getKeyword)
                .collect(Collectors.toList());
    }

    // 검색 통계 조회
    public List<Object[]> getSearchStatistics(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return searchHistoryRepository.findPopularKeywords(startDate, PageRequest.of(0, 10));
    }
}