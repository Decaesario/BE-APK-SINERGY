package com.impal.gabungyuk.collaboration.model.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CollaborationResponse {

    private Integer collaborationId;
    private Integer projectId;
    private Integer idPengguna;
    private String role;
    private String status;

    private ProjectDetail project;
    private OwnerDetail owner;

    @Data
    @Builder
    public static class ProjectDetail {
        private Integer projectId;
        private String title;
        private String description;
        private String category;
        private String status;
        private String repositoryLink;
        private String projectPicture;
        private LocalDateTime deadline;
    }

    @Data
    @Builder
    public static class OwnerDetail {
        private Integer idPengguna;
        private String namaLengkap;
        private String email;
        private String profilePicture;
    }
}