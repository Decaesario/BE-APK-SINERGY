package com.impal.gabungyuk.controller;

import com.impal.gabungyuk.model.*;
import com.impal.gabungyuk.service.ProjectService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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

    
}