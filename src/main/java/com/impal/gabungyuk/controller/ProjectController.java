package com.impal.gabungyuk.controller;

import com.impal.gabungyuk.model.*;
import com.impal.gabungyuk.service.ProjectService;
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
        List<ProjectResponse> response = projectService.getAllProjectsByUser(authorizationHeader);

        return SuccessResponse.<List<ProjectResponse>>builder()
                .status(200)
                .message("User projects retrieved successfully")
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