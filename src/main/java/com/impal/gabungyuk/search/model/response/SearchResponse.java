package com.impal.gabungyuk.search.model.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResponse {

    private List<SearchUserResponse> users;

    private List<SearchProjectResponse> projects;
}