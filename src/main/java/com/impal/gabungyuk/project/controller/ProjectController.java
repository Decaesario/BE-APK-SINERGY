package com.impal.gabungyuk.project.controller;

import com.impal.gabungyuk.core.model.SuccessResponse;
import com.impal.gabungyuk.project.model.request.ProjectDeleteRequest;
import com.impal.gabungyuk.project.model.request.ProjectRequest;
import com.impal.gabungyuk.project.model.response.ProjectResponse;
import com.impal.gabungyuk.project.service.ProjectService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.databind.ObjectMapper;

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
            HttpServletRequest requestHttp,
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestPart(value = "data", required = false) String dataJson,
            @RequestPart(value = "pictureProject", required = false) MultipartFile pictureProject
    ) {
        ProjectRequest request = parseProjectRequest(dataJson);

        ProjectResponse response = projectService.createProject(
                requestHttp,
                request,
                authorizationHeader,
                pictureProject
        );

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
            HttpServletRequest requestHttp,
            @PathVariable Integer projectId,
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestPart(value = "data", required = false) String dataJson,
            @RequestPart(value = "pictureProject", required = false) MultipartFile pictureProject
    ) {
        ProjectRequest request = parseProjectRequest(dataJson);

        ProjectResponse response = projectService.updateProject(
                requestHttp,
                projectId,
                request,
                authorizationHeader,
                pictureProject
        );

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

    private ProjectRequest parseProjectRequest(String dataJson) {
        if (dataJson == null || dataJson.isBlank()) {
            return new ProjectRequest();
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(dataJson, ProjectRequest.class);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid project data format"
            );
        }
    }
}