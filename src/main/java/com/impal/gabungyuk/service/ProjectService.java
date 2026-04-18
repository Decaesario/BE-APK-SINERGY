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
                .category(projectRequest.getCategory())
                .status(projectRequest.getStatus())
                .repositoryLink(projectRequest.getRepositoryLink())
                .fileUrl(projectRequest.getFileUrl())
                .createdAt(LocalDateTime.now())
                .build();

        Project savedProject = projectRepository.save(project);
    
        return ProjectResponse.builder()
                .id(savedProject.getProjectId())
                .title(savedProject.getTitle())
                .category(savedProject.getCategory())
                .status(savedProject.getStatus())
                .description(savedProject.getDescription())
                .repositoryLink(savedProject.getRepositoryLink())
                .fileUrl(savedProject.getFileUrl())
                .build();
                
    }
    public void deleteProject(Integer projectId, String authorizationHeader) {

    Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

    if (!project.getUser().getIdPengguna().equals(user.getIdPengguna())) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this project");
    }

    projectRepository.delete(project);
    }
}
