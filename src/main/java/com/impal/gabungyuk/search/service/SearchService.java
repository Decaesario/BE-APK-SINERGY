package com.impal.gabungyuk.search.service;

import com.impal.gabungyuk.search.model.response.SearchResponse;

public interface SearchService {

    SearchResponse globalSearch(
            String authorizationHeader,
            String query
    );
}