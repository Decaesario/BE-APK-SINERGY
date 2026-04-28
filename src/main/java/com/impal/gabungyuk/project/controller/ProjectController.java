package com.impal.gabungyuk.project.controller;

import com.impal.gabungyuk.core.model.SuccessResponse;
import com.impal.gabungyuk.project.model.request.ProjectDeleteRequest;
import com.impal.gabungyuk.project.model.request.ProjectRequest;
import com.impal.gabungyuk.project.model.response.ProjectResponse;
import com.impal.gabungyuk.project.service.ProjectService;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping(
            value = "/api/v1/create/projects",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<ProjectResponse> createProject(@RequestHeader("Authorization") String authorizationHeader, @RequestBody ProjectRequest request) {

        ProjectResponse response = projectService.createProject(request, authorizationHeader);

        return SuccessResponse.<ProjectResponse>builder()
                .status(200)
                .message("Project created successfully")
                .data(response)
                .build();
    }

    @GetMapping(
            value = "/api/v1/users/projects",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<List<ProjectResponse>> getUserProjects(@RequestHeader("Authorization") String authorizationHeader) {
        // Return all projects visible to any authenticated user (previous behavior returned only projects
        // belonging to the authenticated user). This allows all verified users to view projects.
        List<ProjectResponse> response = projectService.getAllProjectsForAuthenticatedUser(authorizationHeader);

        return SuccessResponse.<List<ProjectResponse>>builder()
                .status(200)
                .message("Projects retrieved successfully")
                .data(response)
                .build();
    }

    @PatchMapping(
            value = "/api/v1/projects/{projectId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<ProjectResponse> updateProject(@PathVariable Integer projectId,
                                                           @RequestHeader("Authorization") String authorizationHeader,
                                                           @RequestBody ProjectRequest request) {
        ProjectResponse response = projectService.updateProject(projectId, request, authorizationHeader);

        return SuccessResponse.<ProjectResponse>builder()
                .status(200)
                .message("Project updated successfully")
                .data(response)
                .build();
    }

    @GetMapping(
            value = "/api/v1/projects",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<List<ProjectResponse>> getAllProjects(@RequestHeader("Authorization") String authorizationHeader) {
        List<ProjectResponse> response = projectService.getAllProjectsForAuthenticatedUser(authorizationHeader);

        return SuccessResponse.<List<ProjectResponse>>builder()
                .status(200)
                .message("All projects retrieved successfully")
                .data(response)
                .build();
    }

    @GetMapping(
            value = "/api/v1/projects/{projectId}/view",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<ProjectResponse> viewProject(@PathVariable Integer projectId,
                                                         @RequestHeader("Authorization") String authorizationHeader) {
        ProjectResponse response = projectService.getProjectByIdForAuthenticatedUser(projectId, authorizationHeader);

        return SuccessResponse.<ProjectResponse>builder()
                .status(200)
                .message("Project retrieved successfully")
                .data(response)
                .build();
    }

   @DeleteMapping(
            value = "/api/v1/delete/projects",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<Void> deleteProject(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody ProjectDeleteRequest request
    ) {
        projectService.deleteProject(request.getProjectId(), authorizationHeader);

        return SuccessResponse.<Void>builder()
                .status(200)
                .message("Project deleted successfully")
                .data(null)
                .build();
    }
}