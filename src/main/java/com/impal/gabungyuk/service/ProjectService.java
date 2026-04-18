package com.impal.gabungyuk.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.impal.gabungyuk.entity.Project;
import com.impal.gabungyuk.entity.User;
import com.impal.gabungyuk.model.ProjectRequest;
import com.impal.gabungyuk.model.ProjectResponse;
import com.impal.gabungyuk.repository.ProjectRepository;
import com.impal.gabungyuk.repository.UserRepository;

import java.time.LocalDateTime;

@Service
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository, TokenService tokenService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    public ProjectResponse createProject(ProjectRequest projectRequest, String authorizationHeader) {

        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Project project = Project.builder()
                .user(user)
                .title(projectRequest.getTitle())
                .description(projectRequest.getDescription())
                .repositoryLink(projectRequest.getRepositoryLink())
                .createdAt(LocalDateTime.now())
                .build();

        Project savedProject = projectRepository.save(project);
    
        return ProjectResponse.builder()
                .id(savedProject.getProjectId())
                .title(savedProject.getTitle())
                .description(savedProject.getDescription())
                .repositoryLink(savedProject.getRepositoryLink())
                .build();
                
    }
}
