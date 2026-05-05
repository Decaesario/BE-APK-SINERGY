package com.impal.gabungyuk.project.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.impal.gabungyuk.auth.entity.User;
import com.impal.gabungyuk.auth.respository.UserRepository;
import com.impal.gabungyuk.core.service.TokenService;
import com.impal.gabungyuk.project.entity.Project;
import com.impal.gabungyuk.project.model.request.ProjectRequest;
import com.impal.gabungyuk.project.model.response.ProjectResponse;
import com.impal.gabungyuk.project.respository.ProjectRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public ProjectService(
            ProjectRepository projectRepository,
            UserRepository userRepository,
            TokenService tokenService
    ) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    public ProjectResponse createProject(
            ProjectRequest projectRequest,
            String authorizationHeader,
            MultipartFile file
    ) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String fileUrl = uploadProjectFile(file, projectRequest.getFileUrl());

        Project project = Project.builder()
                .user(user)
                .title(projectRequest.getTitle())
                .description(projectRequest.getDescription())
                .category(projectRequest.getCategory())
                .status(projectRequest.getStatus())
                .repositoryLink(projectRequest.getRepositoryLink())
                .fileUrl(fileUrl)
                .createdAt(LocalDateTime.now())
                .build();

        Project savedProject = projectRepository.save(project);

        return mapToResponse(savedProject);
    }

    public ProjectResponse updateProject(
            Integer projectId,
            ProjectRequest projectRequest,
            String authorizationHeader,
            MultipartFile file
    ) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        if (!project.getUser().getIdPengguna().equals(user.getIdPengguna())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to edit this project");
        }

        String fileUrl = project.getFileUrl();

        if (file != null && !file.isEmpty()) {
            fileUrl = uploadProjectFile(file, project.getFileUrl());
        } else if (projectRequest.getFileUrl() != null) {
            fileUrl = projectRequest.getFileUrl();
        }

        project.setTitle(projectRequest.getTitle());
        project.setDescription(projectRequest.getDescription());
        project.setCategory(projectRequest.getCategory());
        project.setStatus(projectRequest.getStatus());
        project.setRepositoryLink(projectRequest.getRepositoryLink());
        project.setFileUrl(fileUrl);

        Project updatedProject = projectRepository.save(project);

        return mapToResponse(updatedProject);
    }

    public List<ProjectResponse> getAllProjectsByUser(String authorizationHeader) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<Project> projects = projectRepository.findByUser_IdPengguna(userId);

        return projects.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProjectByIdForAuthenticatedUser(
            Integer projectId,
            String authorizationHeader
    ) {
        tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        return mapToResponse(project);
    }

    public List<ProjectResponse> getAllProjectsForAuthenticatedUser(String authorizationHeader) {
        tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        List<Project> projects = projectRepository.findAll();

        return projects.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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

    private String uploadProjectFile(MultipartFile file, String currentFileUrl) {
        if (file == null || file.isEmpty()) {
            return currentFileUrl;
        }

        try {
            String uploadDir = "uploads/projects/";
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);

            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String fileName = System.currentTimeMillis() + "_" + originalFilename;

            java.nio.file.Path filePath = uploadPath.resolve(fileName);

            java.nio.file.Files.copy(
                    file.getInputStream(),
                    filePath,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            return "/" + uploadDir + fileName;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to upload project file"
            );
        }
    }

    private ProjectResponse mapToResponse(Project project) {
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
}