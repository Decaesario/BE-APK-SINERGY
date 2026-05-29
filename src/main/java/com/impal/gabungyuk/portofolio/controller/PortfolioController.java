package com.impal.gabungyuk.portofolio.controller;

import com.impal.gabungyuk.core.model.SuccessResponse;
import com.impal.gabungyuk.portofolio.model.request.PortfolioDeleteRequest;
import com.impal.gabungyuk.portofolio.model.request.PortfolioRequest;
import com.impal.gabungyuk.portofolio.model.response.PortfolioResponse;
import com.impal.gabungyuk.portofolio.service.PortfolioService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping(
            value = "/api/v1/portfolio",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<List<PortfolioResponse>> getMyPortfolios(
            @RequestHeader("Authorization") String authorizationHeader) {
        return SuccessResponse.<List<PortfolioResponse>>builder()
                .status(200)
                .message("Portfolios retrieved successfully")
                .data(portfolioService.getMyPortfolios(authorizationHeader))
                .build();
    }

    @PostMapping(
            value = "/api/v1/create/portfolio",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<PortfolioResponse> createPortfolio(
            HttpServletRequest requestHttp,
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestPart(value = "data", required = false) String dataJson,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        PortfolioRequest request = parseRequest(dataJson);
        return SuccessResponse.<PortfolioResponse>builder()
                .status(200)
                .message("Portfolio created successfully")
                .data(portfolioService.createPortfolio(requestHttp, authorizationHeader, request, image))
                .build();
    }

    @PutMapping(
            value = "/api/v1/edit/portfolio/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<PortfolioResponse> updatePortfolio(
            HttpServletRequest requestHttp,
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Integer id,
            @RequestPart(value = "data", required = false) String dataJson,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        PortfolioRequest request = parseRequest(dataJson);
        return SuccessResponse.<PortfolioResponse>builder()
                .status(200)
                .message("Portfolio updated successfully")
                .data(portfolioService.updatePortfolio(requestHttp, authorizationHeader, request, id, image))
                .build();
    }

    @DeleteMapping(
            value = "/api/v1/delete/portfolio",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<Void> deletePortfolio(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody PortfolioDeleteRequest request) {
        portfolioService.deletePortfolio(authorizationHeader, request.getPortfolioId());
        return SuccessResponse.<Void>builder()
                .status(200)
                .message("Portfolio deleted successfully")
                .data(null)
                .build();
    }

    private PortfolioRequest parseRequest(String dataJson) {
        if (dataJson == null || dataJson.isBlank()) {
            return new PortfolioRequest();
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(dataJson, PortfolioRequest.class);
        } catch (Exception e) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Invalid portfolio data format"
            );
        }
    }
}