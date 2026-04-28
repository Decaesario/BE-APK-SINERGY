package com.impal.gabungyuk.project.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.impal.gabungyuk.auth.entity.User;
import com.impal.gabungyuk.project.entity.Project;
import com.impal.gabungyuk.project.model.request.ProjectRequest;
import com.impal.gabungyuk.project.model.response.ProjectResponse;
import com.impal.gabungyuk.project.respository.ProjectRepository;
import com.impal.gabungyuk.auth.respository.UserRepository;
import com.impal.gabungyuk.core.service.TokenService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<ProjectResponse> getAllProjectsByUser(String authorizationHeader) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<Project> projects = projectRepository.findByUser_IdPengguna(userId);

        return projects.stream()
                .map(project -> ProjectResponse.builder()
                        .id(project.getProjectId())
                        .title(project.getTitle())
                        .description(project.getDescription())
                        .category(project.getCategory())
                        .status(project.getStatus())
                        .repositoryLink(project.getRepositoryLink())
                        .fileUrl(project.getFileUrl())
                        .build())
                .collect(Collectors.toList());
    }

    public ProjectResponse updateProject(Integer projectId, ProjectRequest projectRequest, String authorizationHeader) {
        Integer token = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);
        User user = userRepository.findById(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        if (!project.getUser().getIdPengguna().equals(user.getIdPengguna())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to edit this project");
        }

        project.setTitle(projectRequest.getTitle());
        project.setDescription(projectRequest.getDescription());
        project.setCategory(projectRequest.getCategory());
        project.setStatus(projectRequest.getStatus());
        project.setRepositoryLink(projectRequest.getRepositoryLink());
        project.setFileUrl(projectRequest.getFileUrl());

        Project updatedProject = projectRepository.save(project);

        return ProjectResponse.builder()
                .id(updatedProject.getProjectId())
                .title(updatedProject.getTitle())
                .description(updatedProject.getDescription())
                .category(updatedProject.getCategory())
                .status(updatedProject.getStatus())
                .repositoryLink(updatedProject.getRepositoryLink())
                .fileUrl(updatedProject.getFileUrl())
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
    }

    public ProjectResponse getProjectByIdForAuthenticatedUser(Integer projectId, String authorizationHeader) {
        // validate token (only ensure the user is authenticated)
        tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        return ProjectResponse.builder()
                .id(project.getProjectId())
                .title(project.getTitle())
                .description(project.getDescription())
                .category(project.getCategory())
                .status(project.getStatus())
                .repositoryLink(project.getRepositoryLink())
                .fileUrl(project.getFileUrl())
                .build();
    }

    public List<ProjectResponse> getAllProjectsForAuthenticatedUser(String authorizationHeader) {
        // validate token (only ensure the user is authenticated)
        tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        List<Project> projects = projectRepository.findAll();

        return projects.stream()
                .map(project -> ProjectResponse.builder()
                        .id(project.getProjectId())
                        .title(project.getTitle())
                        .description(project.getDescription())
                        .category(project.getCategory())
                        .status(project.getStatus())
                        .repositoryLink(project.getRepositoryLink())
                        .fileUrl(project.getFileUrl())
                        .build())
                .collect(Collectors.toList());
    }
}
