package com.impal.gabungyuk.portofolio.controller;

import com.impal.gabungyuk.core.model.SuccessResponse;
import com.impal.gabungyuk.portofolio.model.request.PortfolioRequest;
import com.impal.gabungyuk.portofolio.model.request.PortfolioDeleteRequest;
import com.impal.gabungyuk.portofolio.model.response.PortfolioResponse;
import com.impal.gabungyuk.portofolio.service.PortfolioService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
            value = "/api/v1/portfolio",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<PortfolioResponse> createPortfolio(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody PortfolioRequest request) {
        return SuccessResponse.<PortfolioResponse>builder()
                .status(200)
                .message("Portfolio created successfully")
                .data(portfolioService.createPortfolio(authorizationHeader, request))
                .build();
        }

    @PutMapping(
        value = "/api/v1/portfolio",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
        )
        public SuccessResponse<PortfolioResponse> updatePortfolio(
                @RequestHeader("Authorization") String authorizationHeader,
                @RequestBody PortfolioRequest request) {

        return SuccessResponse.<PortfolioResponse>builder()
                .status(200)
                .message("Portfolio updated successfully")
                .data(portfolioService.updatePortfolio(authorizationHeader, request))
                .build();
        }

    @DeleteMapping(
        value = "/api/v1/portfolio/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
        )
        public SuccessResponse<Void> deletePortfolio(
                @RequestHeader("Authorization") String authorizationHeader,
                @PathVariable Integer id) {

        portfolioService.deletePortfolio(authorizationHeader, id);

        return SuccessResponse.<Void>builder()
                .status(200)
                .message("Portfolio deleted successfully")
                .data(null)
                .build();
        }
}               