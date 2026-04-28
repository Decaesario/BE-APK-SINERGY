// package com.impal.gabungyuk.collaboration.controller;

// import org.springframework.http.MediaType;
// import org.springframework.web.bind.annotation.*;

// import com.impal.gabungyuk.collaboration.model.request.CollaborationRequest;
// import com.impal.gabungyuk.collaboration.model.response.CollaborationResponse;
// import com.impal.gabungyuk.collaboration.service.CollaborationService;
// import com.impal.gabungyuk.core.model.SuccessResponse;

// @RestController
// public class CollaborationController {

//     private final CollaborationService collaborationService;

//     public CollaborationController(CollaborationService collaborationService) {
//         this.collaborationService = collaborationService;
//     }

//     @PostMapping(
//             value = "/api/v1/collaboration/request/{projectId}",
//             consumes = MediaType.APPLICATION_JSON_VALUE,
//             produces = MediaType.APPLICATION_JSON_VALUE
//     )
//     public SuccessResponse<CollaborationResponse> requestCollaboration(
//             @RequestHeader("Authorization") String authorizationHeader,
//             @PathVariable Integer projectId,
//             @RequestBody CollaborationRequest request
//     ) {
//         request.setProjectId(projectId);

//         CollaborationResponse response = collaborationService.requestCollaboration(request, authorizationHeader);

//         return SuccessResponse.<CollaborationResponse>builder()
//                 .status(200)
//                 .message("Collaboration request created successfully")
//                 .data(response)
//                 .build();
//     }

//     @GetMapping(
//             value = "/api/v1/collaboration/{collaborationId}",
//             produces = MediaType.APPLICATION_JSON_VALUE
//     )
//     public SuccessResponse<CollaborationResponse> getCollaboration(
//             @RequestHeader("Authorization") String authorizationHeader,
//             @PathVariable Integer collaborationId
//     ) {
//         CollaborationResponse response = collaborationService.getCollaborationById(
//                 authorizationHeader,
//                 collaborationId
//         );

//         return SuccessResponse.<CollaborationResponse>builder()
//                 .status(200)
//                 .message("Collaboration found successfully")
//                 .data(response)
//                 .build();
//     }

//     @GetMapping(
//             value = "/api/v1/collaboration/{collaborationId}/action",
//             produces = MediaType.APPLICATION_JSON_VALUE
//     )
//     public SuccessResponse<CollaborationResponse> acceptOrDeclineCollaboration(
//             @RequestHeader("Authorization") String authorizationHeader,
//             @PathVariable Integer collaborationId,
//             @RequestParam String action
//     ) {
//         CollaborationResponse response = collaborationService.acceptOrDeclineCollaboration(
//                 authorizationHeader,
//                 collaborationId,
//                 action
//         );

//         return SuccessResponse.<CollaborationResponse>builder()
//                 .status(200)
//                 .message("Collaboration " + action.toLowerCase() + " successfully")
//                 .data(response)
//                 .build();
//     }
// }
package com.impal.gabungyuk.collaboration.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.impal.gabungyuk.collaboration.model.request.CollaborationRequest;
import com.impal.gabungyuk.collaboration.model.response.CollaborationDashboardResponse;
import com.impal.gabungyuk.collaboration.model.response.CollaborationResponse;
import com.impal.gabungyuk.collaboration.service.CollaborationService;
import com.impal.gabungyuk.core.model.SuccessResponse;

@RestController
public class CollaborationController {

    private final CollaborationService collaborationService;

    public CollaborationController(CollaborationService collaborationService) {
        this.collaborationService = collaborationService;
    }

    @PostMapping(
            value = "/api/v1/collaboration/request/{projectId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<CollaborationResponse> requestCollaboration(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Integer projectId,
            @RequestBody CollaborationRequest request
    ) {
        request.setProjectId(projectId);

        CollaborationResponse response = collaborationService.requestCollaboration(request, authorizationHeader);

        return SuccessResponse.<CollaborationResponse>builder()
                .status(200)
                .message("Collaboration request created successfully")
                .data(response)
                .build();
    }

    @GetMapping(
            value = "/api/v1/collaboration/{collaborationId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<CollaborationResponse> getCollaboration(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Integer collaborationId
    ) {
        CollaborationResponse response = collaborationService.getCollaborationById(
                authorizationHeader,
                collaborationId
        );

        return SuccessResponse.<CollaborationResponse>builder()
                .status(200)
                .message("Collaboration found successfully")
                .data(response)
                .build();
    }

    @GetMapping(
            value = "/api/v1/collaboration/{collaborationId}/action",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<CollaborationResponse> acceptOrDeclineCollaboration(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Integer collaborationId,
            @RequestParam String action
    ) {
        CollaborationResponse response = collaborationService.acceptOrDeclineCollaboration(
                authorizationHeader,
                collaborationId,
                action
        );

        return SuccessResponse.<CollaborationResponse>builder()
                .status(200)
                .message("Collaboration " + action.toLowerCase() + " successfully")
                .data(response)
                .build();
    }

    @GetMapping(
            value = "/api/v1/collaboration/dashboard",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<CollaborationDashboardResponse> getDashboard(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        CollaborationDashboardResponse response = collaborationService.getDashboard(authorizationHeader);

        return SuccessResponse.<CollaborationDashboardResponse>builder()
                .status(200)
                .message("Collaboration dashboard retrieved successfully")
                .data(response)
                .build();
    }
}