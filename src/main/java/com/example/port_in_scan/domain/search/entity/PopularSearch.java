package com.example.port_in_scan.domain.search.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "popular_searches")
public class PopularSearch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "popular_search_id")
    private Long popularSearchId;

    @Column(name = "keyword", nullable = false, unique = true)
    private String keyword;

    @Column(name = "search_count")
    @Builder.Default
    private Long searchCount = 1L;

    @UpdateTimestamp
    @Column(name = "last_searched_at")
    private LocalDateTime lastSearchedAt;

    public void incrementSearchCount() {
        this.searchCount++;
    }

    public static PopularSearch createPopularSearch(String keyword) {
        return PopularSearch.builder()
                .keyword(keyword)
                .build();
    }
}