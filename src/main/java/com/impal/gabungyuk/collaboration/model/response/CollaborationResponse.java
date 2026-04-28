
// package com.impal.gabungyuk.collaboration.model.response;

// import lombok.Builder;
// import lombok.Data;

// @Data
// @Builder
// public class CollaborationResponse {

//     private Integer collaborationId;
//     private Integer projectId;
//     private Integer idPengguna;
//     private String role;
//     private String status;

//     private ProjectDetail project;
//     private OwnerDetail owner;

//     @Data
//     @Builder
//     public static class ProjectDetail {
//         private Integer projectId;
//         private String title;
//         private String description;
//         private String category;
//         private String status;
//         private String repositoryLink;
//         private String fileUrl;
//     }

//     @Data
//     @Builder
//     public static class OwnerDetail {
//         private Integer idPengguna;
//         private String username;
//         private String email;
//     }
// }
package com.impal.gabungyuk.collaboration.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CollaborationResponse {

    private Integer collaborationId;
    private Integer projectId;
    private Integer idPengguna; // user yang request
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
        private String fileUrl;
    }

    @Data
    @Builder
    public static class OwnerDetail {
        private Integer idPengguna;
        private String username;
        private String email;
    }
}