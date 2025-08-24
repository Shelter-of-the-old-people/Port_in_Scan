package com.example.port_in_scan.domain.portfolio.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PortfolioCategory {
    
    WEB("웹 개발"),
    MOBILE("모바일 앱"),
    DESKTOP("데스크톱 애플리케이션"),
    GAME("게임 개발"),
    AI_ML("인공지능/머신러닝"),
    DATA_SCIENCE("데이터 사이언스"),
    BACKEND("백엔드 개발"),
    FRONTEND("프론트엔드 개발"),
    FULLSTACK("풀스택 개발"),
    DEVOPS("데브옵스/인프라"),
    BLOCKCHAIN("블록체인"),
    IOT("사물인터넷"),
    EMBEDDED("임베디드"),
    DESIGN("UI/UX 디자인"),
    OTHER("기타");

    private final String displayName;
}