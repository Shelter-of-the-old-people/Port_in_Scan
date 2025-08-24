package com.example.port_in_scan.domain.search.entity;

import com.example.port_in_scan.domain.member.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "search_histories")
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "search_id")
    private Long searchId;

    @Column(name = "keyword", nullable = false)
    private String keyword;

    @Enumerated(EnumType.STRING)
    @Column(name = "search_type")
    private SearchType searchType;

    @Column(name = "result_count")
    private Long resultCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_idx", referencedColumnName = "idx")
    private User user;

    @CreationTimestamp
    @Column(name = "searched_at", nullable = false, updatable = false)
    private LocalDateTime searchedAt;

    @Column(name = "ip_address")
    private String ipAddress;

    public static SearchHistory createSearchHistory(String keyword, SearchType searchType, 
                                                  Long resultCount, User user, String ipAddress) {
        return SearchHistory.builder()
                .keyword(keyword)
                .searchType(searchType)
                .resultCount(resultCount)
                .user(user)
                .ipAddress(ipAddress)
                .build();
    }
}