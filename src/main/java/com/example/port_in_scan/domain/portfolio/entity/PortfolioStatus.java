package com.example.port_in_scan.domain.portfolio.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PortfolioStatus {
    
    ACTIVE("활성"),
    INACTIVE("비활성"),
    DRAFT("임시저장"),
    DELETED("삭제됨");

    private final String displayName;
}