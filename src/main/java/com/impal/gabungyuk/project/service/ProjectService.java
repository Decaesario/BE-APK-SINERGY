package com.impal.gabungyuk.project.service;

import com.impal.gabungyuk.Activitylog.service.ActivityLogService;
import com.impal.gabungyuk.auth.entity.User;
import com.impal.gabungyuk.auth.respository.UserRepository;
import com.impal.gabungyuk.collaboration.entity.Collaboration;
import com.impal.gabungyuk.collaboration.repository.CollaborationRepository;
import com.impal.gabungyuk.core.service.TokenService;
import com.impal.gabungyuk.notification.service.NotificationService;
import com.impal.gabungyuk.project.entity.Project;
import com.impal.gabungyuk.project.model.request.ProjectRequest;
import com.impal.gabungyuk.project.model.response.ProjectResponse;
import com.impal.gabungyuk.project.model.response.UserOwnerResponse;
import com.impal.gabungyuk.project.respository.ProjectRepository;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private static final String PROJECT_STATUS_DELETED = "DELETED";

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final ActivityLogService activityLogService;

    // untuk notification
    private final NotificationService notificationService;
    private final CollaborationRepository collaborationRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            UserRepository userRepository,
            TokenService tokenService,
            ActivityLogService activityLogService,
            NotificationService notificationService,
            CollaborationRepository collaborationRepository
    ) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.activityLogService = activityLogService;
        this.notificationService = notificationService;
        this.collaborationRepository = collaborationRepository;
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

        if (projectRequest.getDeadline() != null && projectRequest.getDeadline().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Deadline cannot be before current date and time"
            );
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
                .deadline(projectRequest.getDeadline())
                .createdAt(LocalDateTime.now())
                .build();

        Project savedProject = projectRepository.save(project);

        // penambahan log aktivitas
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

        Project project = findActiveProjectById(projectId);

        if (!project.getUser().getIdPengguna().equals(user.getIdPengguna())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to edit this project");
        }

        // untuk notification
        String oldDescription = project.getDescription();
        LocalDateTime oldDeadline = project.getDeadline();

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
            if (PROJECT_STATUS_DELETED.equalsIgnoreCase(projectRequest.getStatus())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid project status");
            }
            project.setStatus(projectRequest.getStatus());
        }

        if (projectRequest.getRepositoryLink() != null) {
            project.setRepositoryLink(projectRequest.getRepositoryLink());
        }

        if (projectRequest.getDeadline() != null) {
            if (projectRequest.getDeadline().isBefore(LocalDateTime.now())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Deadline cannot be before current date and time"
                );
            }

            project.setDeadline(projectRequest.getDeadline());
        }

        if (pictureProject != null && !pictureProject.isEmpty()) {
            project.setFileUrl(uploadProjectFile(requestHttp, pictureProject));
        } else if (projectRequest.getFileUrl() != null) {
            project.setFileUrl(buildFullUrlIfNeeded(requestHttp, projectRequest.getFileUrl()));
        }

        Project updatedProject = projectRepository.save(project);

        // untuk notification
        if (!Objects.equals(oldDescription, updatedProject.getDescription())) {
            notifyAcceptedCollaboratorsProjectDescriptionUpdated(
                    user,
                    updatedProject
            );
        }

        // untuk notification
        if (!Objects.equals(oldDeadline, updatedProject.getDeadline())) {
            notifyAcceptedCollaboratorsProjectDeadlineUpdated(
                    user,
                    updatedProject
            );
        }

        // penambahan log aktivitas
        activityLogService.log(user, updatedProject, "Updated project: " + updatedProject.getTitle());

        return mapToResponse(updatedProject);
    }

    public List<ProjectResponse> getAllProjectsByUser(String authorizationHeader) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<Project> projects = projectRepository.findActiveByUserId(userId);

        return projects.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProjectByIdForAuthenticatedUser(
            Integer projectId,
            String authorizationHeader
    ) {
        tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        Project project = findActiveProjectById(projectId);

        return mapToResponse(project);
    }

    public List<ProjectResponse> getAllProjectsForAuthenticatedUser(String authorizationHeader) {
        tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        List<Project> projects = projectRepository.findAllActive();

        return projects.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteProject(Integer projectId, String authorizationHeader) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Project project = findActiveProjectById(projectId);

        if (!project.getUser().getIdPengguna().equals(user.getIdPengguna())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this project");
        }

        project.setStatus(PROJECT_STATUS_DELETED);
        Project deletedProject = projectRepository.save(project);

        // penambahan log aktivitas
        activityLogService.log(user, deletedProject, "Deleted project: " + deletedProject.getTitle());
    }

    // untuk notification
    private void notifyAcceptedCollaboratorsProjectDescriptionUpdated(
            User owner,
            Project project
    ) {
        List<Integer> collaboratorUserIds = getAcceptedCollaboratorUserIds(project.getProjectId());

        for (Integer collaboratorUserId : collaboratorUserIds) {
            notificationService.notifyProjectDescriptionUpdated(
                    collaboratorUserId,
                    owner.getIdPengguna(),
                    project.getProjectId(),
                    project.getTitle()
            );
        }
    }

    // untuk notification
    private void notifyAcceptedCollaboratorsProjectDeadlineUpdated(
            User owner,
            Project project
    ) {
        List<Integer> collaboratorUserIds = getAcceptedCollaboratorUserIds(project.getProjectId());

        for (Integer collaboratorUserId : collaboratorUserIds) {
            notificationService.notifyProjectDeadlineUpdated(
                    collaboratorUserId,
                    owner.getIdPengguna(),
                    project.getProjectId(),
                    project.getTitle(),
                    project.getDeadline()
            );
        }
    }

    // untuk notification
    private List<Integer> getAcceptedCollaboratorUserIds(Integer projectId) {
        return collaborationRepository.findByProjectIdAndStatus(projectId, "ACCEPTED")
                .stream()
                .map(Collaboration::getIdPengguna)
                .toList();
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
        UserOwnerResponse owner = null;

        if (project.getUser() != null) {
            owner = UserOwnerResponse.builder()
                    .id(project.getUser().getIdPengguna())
                    .fullName(project.getUser().getNamaLengkap())
                    .email(project.getUser().getEmail())
                    .profilePicture(project.getUser().getProfilePicture())
                    .build();
        }

        return ProjectResponse.builder()
                .id(project.getProjectId())
                .title(project.getTitle())
                .description(project.getDescription())
                .category(project.getCategory())
                .status(project.getStatus())
                .repositoryLink(project.getRepositoryLink())
                .projectPicture(project.getFileUrl())
                .deadline(project.getDeadline())
                .owner(owner)
                .build();
    }

    private Project findActiveProjectById(Integer projectId) {
        return projectRepository.findActiveById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }
}