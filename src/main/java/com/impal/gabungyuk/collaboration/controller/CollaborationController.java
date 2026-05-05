package com.impal.gabungyuk.collaboration.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.impal.gabungyuk.collaboration.model.request.CollaborationRequest;
import com.impal.gabungyuk.collaboration.model.response.PendingCollaborationResponse;
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
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<CollaborationResponse> requestCollaboration(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Integer projectId
    ) {
        CollaborationResponse response = collaborationService.requestCollaboration(projectId, authorizationHeader);

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

    @PostMapping(
            value = "/api/v1/collaboration/action",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<PendingCollaborationResponse> acceptOrDeclineCollaboration(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody CollaborationRequest request
    ) {
        PendingCollaborationResponse response = collaborationService.acceptOrDeclineCollaboration(
                authorizationHeader,
                request
        );

        String actionMessage = request.getAction().equalsIgnoreCase("ACCEPT")
                ? "accepted"
                : "declined";

        return SuccessResponse.<PendingCollaborationResponse>builder()
                .status(200)
                .message("Collaboration " + actionMessage + " successfully")
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

    @GetMapping(
            value = "/api/v1/collaboration/project/{projectId}/pending",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<PendingCollaborationResponse> getPendingCollaborationUsers(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Integer projectId
    ) {
        PendingCollaborationResponse response = collaborationService.getPendingCollaborationUsers(
                authorizationHeader,
                projectId
        );

        return SuccessResponse.<PendingCollaborationResponse>builder()
                .status(200)
                .message("Pending collaboration requests found successfully")
                .data(response)
                .build();
    }

    @GetMapping(
            value = "/api/v1/collaboration/detail/project/{projectId}/",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<PendingCollaborationResponse> getProjectCollaborators(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Integer projectId
    ) {
        PendingCollaborationResponse response = collaborationService.getProjectCollaborators(
                authorizationHeader,
                projectId
        );

        return SuccessResponse.<PendingCollaborationResponse>builder()
                .status(200)
                .message("Project collaborators found successfully")
                .data(response)
                .build();
    }
}