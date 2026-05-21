package com.impal.gabungyuk.search.model.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchProjectResponse {

    private Integer id;
    private String title;
    private String description;
    private List<String> category;
    private String status;
    private String repositoryLink;
    private String projectPicture;
    private LocalDateTime deadline;
}