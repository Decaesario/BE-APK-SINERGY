package com.impal.gabungyuk.portofolio.service;

import com.impal.gabungyuk.core.service.TokenService;
import com.impal.gabungyuk.portofolio.entity.Portfolio;
import com.impal.gabungyuk.portofolio.model.request.PortfolioRequest;
import com.impal.gabungyuk.portofolio.model.response.PortfolioResponse;
import com.impal.gabungyuk.portofolio.repository.PortfolioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final TokenService tokenService;

    public PortfolioService(PortfolioRepository portfolioRepository, TokenService tokenService) {
        this.portfolioRepository = portfolioRepository;
        this.tokenService = tokenService;
    }

    // GET 
    public List<PortfolioResponse> getMyPortfolios(String authorizationHeader) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);
        List<Portfolio> portfolios = portfolioRepository.findAllByIdPengguna(userId);
        return portfolios.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // POST 
    public PortfolioResponse createPortfolio(String authorizationHeader, PortfolioRequest request) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        Portfolio portfolio = Portfolio.builder()
                .idPengguna(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .fileUrl(request.getFileUrl())
                .image(request.getImage())
                .uploadDate(LocalDateTime.now())
                .build();

        return toResponse(portfolioRepository.save(portfolio));
    }

    // EDIT
    public PortfolioResponse updatePortfolio(String authorizationHeader, PortfolioRequest request) {

        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        Portfolio portfolio = portfolioRepository.findById(request.getPortfolioId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Portfolio not found"
                ));

        if (!portfolio.getIdPengguna().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        portfolio.setTitle(request.getTitle());
        portfolio.setDescription(request.getDescription());
        portfolio.setFileUrl(request.getFileUrl());
        portfolio.setImage(request.getImage());

        return toResponse(portfolioRepository.save(portfolio));
    }

    // DELETE 
    public void deletePortfolio(String authorizationHeader, Integer portfolioId) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found"));

        if (!portfolio.getIdPengguna().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        portfolioRepository.delete(portfolio);
    }

    private PortfolioResponse toResponse(Portfolio portfolio) {
        PortfolioResponse response = new PortfolioResponse();
        response.setPortfolioId(portfolio.getPortfolioId());
        response.setIdPengguna(portfolio.getIdPengguna());
        response.setTitle(portfolio.getTitle());
        response.setDescription(portfolio.getDescription());
        response.setFileUrl(portfolio.getFileUrl());
        response.setImage(portfolio.getImage());
        response.setUploadDate(portfolio.getUploadDate());
        return response;
    }
}