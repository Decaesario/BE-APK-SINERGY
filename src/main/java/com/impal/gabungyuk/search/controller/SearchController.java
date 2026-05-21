package com.impal.gabungyuk.search.controller;

import com.impal.gabungyuk.core.model.SuccessResponse;
import com.impal.gabungyuk.search.model.response.SearchResponse;
import com.impal.gabungyuk.search.service.SearchService;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping(
            value = "/api/v1/search",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<SearchResponse> globalSearch(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam("query") String query
    ) {

        SearchResponse response = searchService.globalSearch(
                authorizationHeader,
                query
        );

        return SuccessResponse.<SearchResponse>builder()
                .status(200)
                .message("Search results found")
                .data(response)
                .build();
    }
}