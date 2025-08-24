package com.example.port_in_scan.domain.portfolio.service;

import com.example.port_in_scan.domain.member.entity.User;
import com.example.port_in_scan.domain.member.repository.UserRepository;
import com.example.port_in_scan.domain.portfolio.dto.PortfolioCreateRequestDto;
import com.example.port_in_scan.domain.portfolio.dto.PortfolioResponseDto;
import com.example.port_in_scan.domain.portfolio.dto.PortfolioUpdateRequestDto;
import com.example.port_in_scan.domain.portfolio.entity.Portfolio;
import com.example.port_in_scan.domain.portfolio.entity.PortfolioCategory;
import com.example.port_in_scan.domain.portfolio.entity.PortfolioStatus;
import com.example.port_in_scan.domain.portfolio.repository.PortfolioRepository;
import com.example.port_in_scan.exception.AppException;
import com.example.port_in_scan.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;

    @Transactional
    public PortfolioResponseDto createPortfolio(String userEmail, PortfolioCreateRequestDto requestDto) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND.getMessage(), ErrorCode.USER_NOT_FOUND));

        Portfolio portfolio = Portfolio.builder()
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .githubUrl(requestDto.getGithubUrl())
                .demoUrl(requestDto.getDemoUrl())
                .category(requestDto.getCategory())
                .techStacks(requestDto.getTechStacks() != null ? requestDto.getTechStacks() : List.of())
                .tags(requestDto.getTags() != null ? requestDto.getTags() : List.of())
                .user(user)
                .build();

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        log.info("포트폴리오 생성 완료: {}", savedPortfolio.getPortfolioId());
        
        return PortfolioResponseDto.from(savedPortfolio);
    }

    public PortfolioResponseDto getPortfolio(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new AppException("포트폴리오를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND));

        return PortfolioResponseDto.from(portfolio);
    }

    @Transactional
    public PortfolioResponseDto getPortfolioWithViewCount(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new AppException("포트폴리오를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND));

        portfolio.increaseViewCount();
        Portfolio updatedPortfolio = portfolioRepository.save(portfolio);
        
        return PortfolioResponseDto.from(updatedPortfolio);
    }

    @Transactional
    public PortfolioResponseDto updatePortfolio(Long portfolioId, String userEmail, PortfolioUpdateRequestDto requestDto) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new AppException("포트폴리오를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND));

        // 작성자 확인
        if (!portfolio.getUser().getEmail().equals(userEmail)) {
            throw new AppException("포트폴리오 수정 권한이 없습니다.", ErrorCode.USER_EMAIL_UNMATCHED);
        }

        if (requestDto.getTitle() != null || requestDto.getDescription() != null || 
            requestDto.getGithubUrl() != null || requestDto.getDemoUrl() != null || 
            requestDto.getCategory() != null) {
            
            portfolio.updatePortfolio(
                requestDto.getTitle() != null ? requestDto.getTitle() : portfolio.getTitle(),
                requestDto.getDescription() != null ? requestDto.getDescription() : portfolio.getDescription(),
                requestDto.getGithubUrl() != null ? requestDto.getGithubUrl() : portfolio.getGithubUrl(),
                requestDto.getDemoUrl() != null ? requestDto.getDemoUrl() : portfolio.getDemoUrl(),
                requestDto.getCategory() != null ? requestDto.getCategory() : portfolio.getCategory()
            );
        }

        if (requestDto.getTechStacks() != null) {
            portfolio.updateTechStacks(requestDto.getTechStacks());
        }

        if (requestDto.getTags() != null) {
            portfolio.updateTags(requestDto.getTags());
        }

        Portfolio updatedPortfolio = portfolioRepository.save(portfolio);
        log.info("포트폴리오 수정 완료: {}", updatedPortfolio.getPortfolioId());
        
        return PortfolioResponseDto.from(updatedPortfolio);
    }

    @Transactional
    public void deletePortfolio(Long portfolioId, String userEmail) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new AppException("포트폴리오를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND));

        // 작성자 확인
        if (!portfolio.getUser().getEmail().equals(userEmail)) {
            throw new AppException("포트폴리오 삭제 권한이 없습니다.", ErrorCode.USER_EMAIL_UNMATCHED);
        }

        portfolio.updateStatus(PortfolioStatus.DELETED);
        portfolioRepository.save(portfolio);
        log.info("포트폴리오 삭제 완료: {}", portfolioId);
    }

    public Page<PortfolioResponseDto> getAllPortfolios(Pageable pageable) {
        return portfolioRepository.findByStatus(PortfolioStatus.ACTIVE, pageable)
                .map(PortfolioResponseDto::from);
    }

    public Page<PortfolioResponseDto> getPortfoliosByCategory(PortfolioCategory category, Pageable pageable) {
        return portfolioRepository.findByStatusAndCategory(PortfolioStatus.ACTIVE, category, pageable)
                .map(PortfolioResponseDto::from);
    }

    public Page<PortfolioResponseDto> getMyPortfolios(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND.getMessage(), ErrorCode.USER_NOT_FOUND));

        return portfolioRepository.findByUserIdx(user.getIdx(), pageable)
                .map(PortfolioResponseDto::from);
    }

    public Page<PortfolioResponseDto> searchPortfolios(String keyword, Pageable pageable) {
        return portfolioRepository.findByTitleOrDescriptionContainingAndStatus(
                keyword, PortfolioStatus.ACTIVE, pageable)
                .map(PortfolioResponseDto::from);
    }

    public Page<PortfolioResponseDto> searchByTechStacks(List<String> techStacks, Pageable pageable) {
        return portfolioRepository.findByTechStacksInAndStatus(techStacks, PortfolioStatus.ACTIVE, pageable)
                .map(PortfolioResponseDto::from);
    }

    public Page<PortfolioResponseDto> searchByTags(List<String> tags, Pageable pageable) {
        return portfolioRepository.findByTagsInAndStatus(tags, PortfolioStatus.ACTIVE, pageable)
                .map(PortfolioResponseDto::from);
    }

    public Page<PortfolioResponseDto> getPopularPortfolios(Pageable pageable) {
        return portfolioRepository.findByStatusOrderByViewCountDesc(PortfolioStatus.ACTIVE, pageable)
                .map(PortfolioResponseDto::from);
    }

    public Page<PortfolioResponseDto> getMostLikedPortfolios(Pageable pageable) {
        return portfolioRepository.findByStatusOrderByLikeCountDesc(PortfolioStatus.ACTIVE, pageable)
                .map(PortfolioResponseDto::from);
    }

    public Page<PortfolioResponseDto> getLatestPortfolios(Pageable pageable) {
        return portfolioRepository.findByStatusOrderByCreatedAtDesc(PortfolioStatus.ACTIVE, pageable)
                .map(PortfolioResponseDto::from);
    }

    @Transactional
    public void likePortfolio(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new AppException("포트폴리오를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND));

        portfolio.increaseLikeCount();
        portfolioRepository.save(portfolio);
        log.info("포트폴리오 좋아요 증가: {}", portfolioId);
    }

    @Transactional
    public void unlikePortfolio(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new AppException("포트폴리오를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND));

        portfolio.decreaseLikeCount();
        portfolioRepository.save(portfolio);
        log.info("포트폴리오 좋아요 감소: {}", portfolioId);
    }
}