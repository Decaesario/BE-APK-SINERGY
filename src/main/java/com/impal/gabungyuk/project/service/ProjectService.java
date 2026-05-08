package com.impal.gabungyuk.project.service;

import com.impal.gabungyuk.Activitylog.service.ActivityLogService;

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

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;



@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final ActivityLogService activityLogService;

    public ProjectService(
            ProjectRepository projectRepository,
            UserRepository userRepository,
            TokenService tokenService,
            ActivityLogService activityLogService //penambahan log aktivitas
    ) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.activityLogService = activityLogService; //penambahan log aktivitas
    }

    public ProjectResponse createProject(
            HttpServletRequest requestHttp,
            ProjectRequest projectRequest,
            String authorizationHeader,
            MultipartFile pictureProject
    ) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (projectRequest.getTitle() == null || projectRequest.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project title is required");
        }

        String fileUrl;

        if (pictureProject != null && !pictureProject.isEmpty()) {
            fileUrl = uploadProjectFile(requestHttp, pictureProject);
        } else {
            fileUrl = buildFullUrlIfNeeded(requestHttp, projectRequest.getFileUrl());
        }

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

        //penambahan log aktivitas
        activityLogService.log(user, savedProject, "Created project: " + savedProject.getTitle());
        return mapToResponse(savedProject);

        
    }

    public ProjectResponse updateProject(
            HttpServletRequest requestHttp,
            Integer projectId,
            ProjectRequest projectRequest,
            String authorizationHeader,
            MultipartFile pictureProject
    ) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        if (!project.getUser().getIdPengguna().equals(user.getIdPengguna())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to edit this project");
        }

        if (projectRequest.getTitle() != null) {
            project.setTitle(projectRequest.getTitle());
        }

        if (projectRequest.getDescription() != null) {
            project.setDescription(projectRequest.getDescription());
        }

        if (projectRequest.getCategory() != null) {
            project.setCategory(projectRequest.getCategory());
        }

        if (projectRequest.getStatus() != null) {
            project.setStatus(projectRequest.getStatus());
        }

        if (projectRequest.getRepositoryLink() != null) {
            project.setRepositoryLink(projectRequest.getRepositoryLink());
        }

        if (pictureProject != null && !pictureProject.isEmpty()) {
            project.setFileUrl(uploadProjectFile(requestHttp, pictureProject));
        } else if (projectRequest.getFileUrl() != null) {
            project.setFileUrl(buildFullUrlIfNeeded(requestHttp, projectRequest.getFileUrl()));
        }

        Project updatedProject = projectRepository.save(project);

        //penambahan log aktivitas
        activityLogService.log(user, updatedProject, "Updated project: " + updatedProject.getTitle());

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
         //penambahan log aktivitas
        activityLogService.log(user, project, "Deleted project: " + project.getTitle());
        projectRepository.delete(project);
    }

    private String uploadProjectFile(
            HttpServletRequest requestHttp,
            MultipartFile pictureProject
    ) {
        try {
            String uploadDir = "uploads/projects/";
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);

            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }

            String originalFilename = pictureProject.getOriginalFilename();

            if (originalFilename == null || originalFilename.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Project picture filename is invalid"
                );
            }

            String safeFileName = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
            String fileName = System.currentTimeMillis() + "_" + safeFileName;

            java.nio.file.Path filePath = uploadPath.resolve(fileName);

            java.nio.file.Files.copy(
                    pictureProject.getInputStream(),
                    filePath,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            return getBaseUrl(requestHttp) + "/uploads/projects/" + fileName;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to upload project picture"
            );
        }
    }

    private String buildFullUrlIfNeeded(HttpServletRequest requestHttp, String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return null;
        }

        if (fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) {
            return fileUrl;
        }

        if (fileUrl.startsWith("/")) {
            return getBaseUrl(requestHttp) + fileUrl;
        }

        return getBaseUrl(requestHttp) + "/" + fileUrl;
    }

    private String getBaseUrl(HttpServletRequest requestHttp) {
        return requestHttp.getScheme()
                + "://"
                + requestHttp.getServerName()
                + ":"
                + requestHttp.getServerPort();
    }

    private ProjectResponse mapToResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getProjectId())
                .title(project.getTitle())
                .description(project.getDescription())
                .category(project.getCategory())
                .status(project.getStatus())
                .repositoryLink(project.getRepositoryLink())
                .projectPicture(project.getFileUrl())
                .build();
    }
}