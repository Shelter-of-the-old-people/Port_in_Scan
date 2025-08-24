package com.example.port_in_scan.domain.portfolio.repository;

import com.example.port_in_scan.domain.portfolio.entity.Portfolio;
import com.example.port_in_scan.domain.portfolio.entity.PortfolioCategory;
import com.example.port_in_scan.domain.portfolio.entity.PortfolioStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    // 상태별 포트폴리오 조회
    Page<Portfolio> findByStatus(PortfolioStatus status, Pageable pageable);
    
    // 카테고리별 포트폴리오 조회
    Page<Portfolio> findByCategory(PortfolioCategory category, Pageable pageable);
    
    // 상태와 카테고리로 포트폴리오 조회
    Page<Portfolio> findByStatusAndCategory(PortfolioStatus status, PortfolioCategory category, Pageable pageable);
    
    // 사용자별 포트폴리오 조회
    Page<Portfolio> findByUserIdx(Long userIdx, Pageable pageable);
    
    // 사용자별 활성 포트폴리오 조회
    Page<Portfolio> findByUserIdxAndStatus(Long userIdx, PortfolioStatus status, Pageable pageable);
    
    // 제목으로 검색 (LIKE 검색)
    @Query("SELECT p FROM Portfolio p WHERE p.title LIKE %:title% AND p.status = :status")
    Page<Portfolio> findByTitleContainingAndStatus(@Param("title") String title, 
                                                  @Param("status") PortfolioStatus status, 
                                                  Pageable pageable);
    
    // 설명으로 검색 (LIKE 검색)
    @Query("SELECT p FROM Portfolio p WHERE p.description LIKE %:description% AND p.status = :status")
    Page<Portfolio> findByDescriptionContainingAndStatus(@Param("description") String description, 
                                                        @Param("status") PortfolioStatus status, 
                                                        Pageable pageable);
    
    // 제목 또는 설명으로 검색
    @Query("SELECT p FROM Portfolio p WHERE (p.title LIKE %:keyword% OR p.description LIKE %:keyword%) AND p.status = :status")
    Page<Portfolio> findByTitleOrDescriptionContainingAndStatus(@Param("keyword") String keyword, 
                                                              @Param("status") PortfolioStatus status, 
                                                              Pageable pageable);
    
    // 기술 스택으로 검색
    @Query("SELECT DISTINCT p FROM Portfolio p JOIN p.techStacks ts WHERE ts IN :techStacks AND p.status = :status")
    Page<Portfolio> findByTechStacksInAndStatus(@Param("techStacks") List<String> techStacks, 
                                               @Param("status") PortfolioStatus status, 
                                               Pageable pageable);
    
    // 태그로 검색
    @Query("SELECT DISTINCT p FROM Portfolio p JOIN p.tags t WHERE t IN :tags AND p.status = :status")
    Page<Portfolio> findByTagsInAndStatus(@Param("tags") List<String> tags, 
                                         @Param("status") PortfolioStatus status, 
                                         Pageable pageable);
    
    // 조회수 상위 포트폴리오
    Page<Portfolio> findByStatusOrderByViewCountDesc(PortfolioStatus status, Pageable pageable);
    
    // 좋아요 수 상위 포트폴리오
    Page<Portfolio> findByStatusOrderByLikeCountDesc(PortfolioStatus status, Pageable pageable);
    
    // 최신 포트폴리오
    Page<Portfolio> findByStatusOrderByCreatedAtDesc(PortfolioStatus status, Pageable pageable);
    
    // 조회수 증가
    @Modifying
    @Query("UPDATE Portfolio p SET p.viewCount = p.viewCount + 1 WHERE p.portfolioId = :portfolioId")
    void incrementViewCount(@Param("portfolioId") Long portfolioId);
    
    // 좋아요 수 증가
    @Modifying
    @Query("UPDATE Portfolio p SET p.likeCount = p.likeCount + 1 WHERE p.portfolioId = :portfolioId")
    void incrementLikeCount(@Param("portfolioId") Long portfolioId);
    
    // 좋아요 수 감소
    @Modifying
    @Query("UPDATE Portfolio p SET p.likeCount = p.likeCount - 1 WHERE p.portfolioId = :portfolioId AND p.likeCount > 0")
    void decrementLikeCount(@Param("portfolioId") Long portfolioId);
    
    // 복합 검색 (제목, 설명, 기술스택, 태그)
    @Query("SELECT DISTINCT p FROM Portfolio p " +
           "LEFT JOIN p.techStacks ts " +
           "LEFT JOIN p.tags t " +
           "WHERE p.status = :status " +
           "AND (:keyword IS NULL OR p.title LIKE %:keyword% OR p.description LIKE %:keyword%) " +
           "AND (:category IS NULL OR p.category = :category) " +
           "AND (:techStack IS NULL OR ts = :techStack) " +
           "AND (:tag IS NULL OR t = :tag)")
    Page<Portfolio> findByComplexSearch(@Param("keyword") String keyword,
                                       @Param("category") PortfolioCategory category,
                                       @Param("techStack") String techStack,
                                       @Param("tag") String tag,
                                       @Param("status") PortfolioStatus status,
                                       Pageable pageable);
}