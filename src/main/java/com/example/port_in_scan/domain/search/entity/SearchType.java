package com.example.port_in_scan.domain.search.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SearchType {
    
    KEYWORD("키워드 검색"),
    TECH_STACK("기술 스택 검색"),
    TAG("태그 검색"),
    CATEGORY("카테고리 검색"),
    AUTHOR("작성자 검색"),
    COMPLEX("복합 검색");

    private final String displayName;
}