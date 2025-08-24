package com.example.port_in_scan.domain.search.repository;

import com.example.port_in_scan.domain.search.entity.SearchHistory;
import com.example.port_in_scan.domain.search.entity.SearchType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    // 사용자별 검색 히스토리 조회
    Page<SearchHistory> findByUserIdxOrderBySearchedAtDesc(Long userIdx, Pageable pageable);
    
    // 사용자별 검색 타입으로 필터링
    Page<SearchHistory> findByUserIdxAndSearchTypeOrderBySearchedAtDesc(Long userIdx, SearchType searchType, Pageable pageable);
    
    // 특정 기간 내 검색 히스토리
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.searchedAt BETWEEN :startDate AND :endDate ORDER BY sh.searchedAt DESC")
    Page<SearchHistory> findBySearchedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate, 
                                               Pageable pageable);
    
    // 인기 검색어 통계 (최근 7일)
    @Query("SELECT sh.keyword, COUNT(sh) as searchCount FROM SearchHistory sh " +
           "WHERE sh.searchedAt >= :startDate " +
           "GROUP BY sh.keyword " +
           "ORDER BY searchCount DESC")
    List<Object[]> findPopularKeywords(@Param("startDate") LocalDateTime startDate, Pageable pageable);
    
    // 사용자별 최근 검색어 (중복 제거)
    @Query("SELECT DISTINCT sh.keyword FROM SearchHistory sh " +
           "WHERE sh.user.idx = :userIdx " +
           "ORDER BY sh.searchedAt DESC")
    List<String> findRecentKeywordsByUser(@Param("userIdx") Long userIdx, Pageable pageable);
    
    // 검색 타입별 통계
    @Query("SELECT sh.searchType, COUNT(sh) FROM SearchHistory sh " +
           "WHERE sh.searchedAt >= :startDate " +
           "GROUP BY sh.searchType")
    List<Object[]> findSearchTypeStatistics(@Param("startDate") LocalDateTime startDate);
}