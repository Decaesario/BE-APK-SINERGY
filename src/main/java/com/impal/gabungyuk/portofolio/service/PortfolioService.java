package com.impal.gabungyuk.portofolio.service;

import com.impal.gabungyuk.core.service.TokenService;
import com.impal.gabungyuk.core.service.TimezoneService;
import com.impal.gabungyuk.core.service.UrlService;
import com.impal.gabungyuk.portofolio.entity.Portfolio;
import com.impal.gabungyuk.portofolio.model.request.PortfolioRequest;
import com.impal.gabungyuk.portofolio.model.response.PortfolioResponse;
import com.impal.gabungyuk.portofolio.repository.PortfolioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final TokenService tokenService;
    private final TimezoneService timezoneService;
    private final UrlService urlService;

    public PortfolioService(
            PortfolioRepository portfolioRepository,
            TokenService tokenService,
            TimezoneService timezoneService,
            UrlService urlService
    ) {
        this.portfolioRepository = portfolioRepository;
        this.tokenService = tokenService;
        this.timezoneService = timezoneService;
        this.urlService = urlService;
    }

    // GET
    public List<PortfolioResponse> getMyPortfolios(String authorizationHeader) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);
        String viewerTz = timezoneService.getUserTimezoneOrDefault(userId);
        List<Portfolio> portfolios = portfolioRepository.findAllByIdPengguna(userId);
        return portfolios.stream().map(portfolio -> toResponse(portfolio, viewerTz)).collect(Collectors.toList());
    }

    public List<PortfolioResponse> getUsersPortoflio(String authorizationHeader, Integer idPengguna) {
        Integer viewerId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);
        String viewerTz = timezoneService.getUserTimezoneOrDefault(viewerId);
        List<Portfolio> portfolios = portfolioRepository.findAllByIdPengguna(idPengguna);
        return portfolios.stream().map(portfolio -> toResponse(portfolio, viewerTz)).collect(Collectors.toList());
    }

    // POST
    public PortfolioResponse createPortfolio(
            HttpServletRequest requestHttp,
            String authorizationHeader,
            PortfolioRequest request,
            MultipartFile image
    ) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = uploadPortfolioImage(image);
        } else if (request.getImage() != null) {
            imageUrl = request.getImage();
        }

        Portfolio portfolio = Portfolio.builder()
                .idPengguna(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .fileUrl(request.getFileUrl())
                .image(imageUrl)
                .uploadDate(LocalDateTime.now())
                .build();

        String viewerTz = timezoneService.getUserTimezoneOrDefault(userId);
        return toResponse(portfolioRepository.save(portfolio), viewerTz);
    }

    // PUT
    public PortfolioResponse updatePortfolio(
            HttpServletRequest requestHttp,
            String authorizationHeader,
            PortfolioRequest request,
            Integer portfolioId,
            MultipartFile image
    ) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found"));

        if (!portfolio.getIdPengguna().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        portfolio.setTitle(request.getTitle());
        portfolio.setDescription(request.getDescription());
        portfolio.setFileUrl(request.getFileUrl());

        if (image != null && !image.isEmpty()) {
            portfolio.setImage(uploadPortfolioImage(image));
        } else if (request.getImage() != null) {
            portfolio.setImage(request.getImage());
        }

        String viewerTz = timezoneService.getUserTimezoneOrDefault(userId);
        return toResponse(portfolioRepository.save(portfolio), viewerTz);
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

    private String uploadPortfolioImage(MultipartFile image) {
        try {
            String uploadDir = System.getProperty("user.home") + "/uploads/portfolios";
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);

            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }

            String originalFilename = image.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image filename is invalid");
            }

            String safeFileName = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
            String fileName = System.currentTimeMillis() + "_" + safeFileName;
            java.nio.file.Path filePath = uploadPath.resolve(fileName);

            java.nio.file.Files.copy(
                    image.getInputStream(),
                    filePath,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            return "/uploads/portfolios/" + fileName;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload portfolio image");
        }
    }

    private PortfolioResponse toResponse(Portfolio portfolio, String viewerTimezone) {
        PortfolioResponse response = new PortfolioResponse();
        response.setPortfolioId(portfolio.getPortfolioId());
        response.setIdPengguna(portfolio.getIdPengguna());
        response.setTitle(portfolio.getTitle());
        response.setDescription(portfolio.getDescription());
        response.setFileUrl(portfolio.getFileUrl());
        response.setImage(urlService.normalizeProfilePictureUrl(portfolio.getImage()));
        response.setUploadDate(timezoneService.convertToUserZone(portfolio.getUploadDate(), viewerTimezone));
        return response;
    }
}
