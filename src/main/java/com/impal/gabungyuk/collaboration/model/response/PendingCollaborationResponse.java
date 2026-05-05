package com.impal.gabungyuk.collaboration.model.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingCollaborationResponse {

    private String status;
    private ProjectDetail project;
    private List<PendingCollaborationUserResponse> collaborators;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectDetail {
        private Integer projectId;
        private String title;
        private String description;
        private String category;
        private String status;
        private String repositoryLink;
        private String fileUrl;
    }
}