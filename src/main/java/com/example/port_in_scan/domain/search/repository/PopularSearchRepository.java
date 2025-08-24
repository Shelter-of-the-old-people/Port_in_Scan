package com.example.port_in_scan.domain.search.repository;

import com.example.port_in_scan.domain.search.entity.PopularSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PopularSearchRepository extends JpaRepository<PopularSearch, Long> {

    // 키워드로 인기 검색어 찾기
    Optional<PopularSearch> findByKeyword(String keyword);
    
    // 검색 횟수 상위 키워드 조회
    Page<PopularSearch> findAllByOrderBySearchCountDesc(Pageable pageable);
    
    // 최근 검색된 키워드 조회
    Page<PopularSearch> findAllByOrderByLastSearchedAtDesc(Pageable pageable);
    
    // 검색 횟수 증가
    @Modifying
    @Query("UPDATE PopularSearch p SET p.searchCount = p.searchCount + 1 WHERE p.keyword = :keyword")
    int incrementSearchCount(@Param("keyword") String keyword);
    
    // 키워드 검색 (LIKE 검색)
    @Query("SELECT p FROM PopularSearch p WHERE p.keyword LIKE %:keyword% ORDER BY p.searchCount DESC")
    Page<PopularSearch> findByKeywordContaining(@Param("keyword") String keyword, Pageable pageable);
}