package com.impal.gabungyuk.project.controller;

import com.impal.gabungyuk.core.model.SuccessResponse;
import com.impal.gabungyuk.project.model.request.ProjectDeleteRequest;
import com.impal.gabungyuk.project.model.request.ProjectRequest;
import com.impal.gabungyuk.project.model.response.ProjectResponse;
import com.impal.gabungyuk.project.service.ProjectService;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping(
            value = "/api/v1/create/projects",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<ProjectResponse> createProject(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "repositoryLink", required = false) String repositoryLink,
            @RequestParam(value = "fileUrl", required = false) String fileUrl,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        ProjectRequest request = ProjectRequest.builder()
                .title(title)
                .description(description)
                .category(category)
                .status(status)
                .repositoryLink(repositoryLink)
                .fileUrl(fileUrl)
                .build();

        ProjectResponse response = projectService.createProject(request, authorizationHeader, file);

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
    public SuccessResponse<List<ProjectResponse>> getUserProjects(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        List<ProjectResponse> response = projectService.getAllProjectsForAuthenticatedUser(authorizationHeader);

        return SuccessResponse.<List<ProjectResponse>>builder()
                .status(200)
                .message("Projects retrieved successfully")
                .data(response)
                .build();
    }

    @PatchMapping(
            value = "/api/v1/projects/{projectId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<ProjectResponse> updateProject(
            @PathVariable Integer projectId,
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "repositoryLink", required = false) String repositoryLink,
            @RequestParam(value = "fileUrl", required = false) String fileUrl,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        ProjectRequest request = ProjectRequest.builder()
                .title(title)
                .description(description)
                .category(category)
                .status(status)
                .repositoryLink(repositoryLink)
                .fileUrl(fileUrl)
                .build();

        ProjectResponse response = projectService.updateProject(projectId, request, authorizationHeader, file);

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
    public SuccessResponse<List<ProjectResponse>> getAllProjects(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
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
    public SuccessResponse<ProjectResponse> viewProject(
            @PathVariable Integer projectId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
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