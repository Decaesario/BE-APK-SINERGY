package com.impal.gabungyuk.collaboration.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.impal.gabungyuk.Activitylog.service.ActivityLogService;
import com.impal.gabungyuk.auth.entity.User;
import com.impal.gabungyuk.auth.respository.UserRepository;
import com.impal.gabungyuk.collaboration.entity.Collaboration;
import com.impal.gabungyuk.collaboration.model.request.CollaborationRequest;
import com.impal.gabungyuk.collaboration.model.response.CollaborationDashboardResponse;
import com.impal.gabungyuk.collaboration.model.response.CollaborationResponse;
import com.impal.gabungyuk.collaboration.model.response.PendingCollaborationResponse;
import com.impal.gabungyuk.collaboration.model.response.PendingCollaborationUserResponse;
import com.impal.gabungyuk.collaboration.repository.CollaborationRepository;
import com.impal.gabungyuk.core.service.TokenService;
import com.impal.gabungyuk.notification.service.NotificationService;
import com.impal.gabungyuk.profile.entitiy.Profile;
import com.impal.gabungyuk.profile.repository.ProfileRepository;
import com.impal.gabungyuk.project.entity.Project;
import com.impal.gabungyuk.project.model.response.ProjectResponse;
import com.impal.gabungyuk.project.respository.ProjectRepository;

@Service
public class CollaborationService {

    private final CollaborationRepository collaborationRepository;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProfileRepository profileRepository;
    private final ActivityLogService activityLogService;

    // untuk notification
    private final NotificationService notificationService;

    public CollaborationService(
            CollaborationRepository collaborationRepository,
            TokenService tokenService,
            UserRepository userRepository,
            ProjectRepository projectRepository,
            ProfileRepository profileRepository,
            ActivityLogService activityLogService,
            NotificationService notificationService
    ) {
        this.collaborationRepository = collaborationRepository;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.profileRepository = profileRepository;
        this.activityLogService = activityLogService;
        this.notificationService = notificationService;
    }

    public CollaborationResponse requestCollaboration(Integer projectId, String authorizationHeader) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Profile profile = profileRepository.findByIdPengguna(user.getIdPengguna())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        if (profile.getKeahlian() == null || profile.getKeahlian().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please set your skill/role in profile first"
            );
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        if (project.getUser().getIdPengguna().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You cannot request collaboration on your own project"
            );
        }

        boolean alreadyRequested = collaborationRepository.existsByProjectIdAndIdPenggunaAndStatusIn(
                project.getProjectId(),
                userId,
                List.of("PENDING", "ACCEPTED")
        );

        if (alreadyRequested) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You have already requested collaboration for this project"
            );
        }

        Collaboration collaboration = Collaboration.builder()
                .projectId(project.getProjectId())
                .idPengguna(userId)
                .role(profile.getKeahlian())
                .status("PENDING")
                .joinDate(LocalDateTime.now())
                .build();

        Collaboration saved = collaborationRepository.save(collaboration);

        // penambahan log aktivitas
        activityLogService.log(user, project, "Requested collaboration: " + saved.getRole());

        // untuk notification
        notificationService.notifyCollaborationRequest(
                project.getUser().getIdPengguna(),
                userId,
                project.getProjectId(),
                saved.getCollaborationId(),
                profile.getNamaLengkap(),
                project.getTitle()
        );

        return mapToResponse(saved, project);
    }

    public CollaborationResponse getCollaborationById(String authorizationHeader, Integer collaborationId) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Collaboration collaboration = collaborationRepository.findById(collaborationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collaboration not found"));

        Project project = projectRepository.findById(collaboration.getProjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        return mapToResponse(collaboration, project);
    }

    public PendingCollaborationResponse acceptOrDeclineCollaboration(
            String authorizationHeader,
            CollaborationRequest request
    ) {
        Integer ownerId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (request.getProjectId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project id is required");
        }

        if (request.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User id is required");
        }

        if (request.getAction() == null || request.getAction().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Action is required");
        }

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        if (!project.getUser().getIdPengguna().equals(ownerId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only project owner can accept or decline"
            );
        }

        Collaboration collaboration = collaborationRepository
                .findByProjectIdAndIdPenggunaAndStatus(
                        request.getProjectId(),
                        request.getUserId(),
                        "PENDING"
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pending collaboration request not found"
                ));

        if (request.getAction().equalsIgnoreCase("ACCEPT")) {
            collaboration.setStatus("ACCEPTED");
        } else if (request.getAction().equalsIgnoreCase("DECLINE")) {
            collaboration.setStatus("DECLINED");
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid action. Use ACCEPT or DECLINE"
            );
        }

        Collaboration updated = collaborationRepository.save(collaboration);

        // penambahan log aktivitas
        activityLogService.log(
                owner,
                project,
                request.getAction() + " collaboration on project: " + project.getTitle()
        );

        // untuk notification
        if (updated.getStatus().equalsIgnoreCase("ACCEPTED")) {
            notificationService.notifyCollaborationAccepted(
                    updated.getIdPengguna(),
                    ownerId,
                    project.getProjectId(),
                    updated.getCollaborationId(),
                    project.getTitle()
            );
        } else if (updated.getStatus().equalsIgnoreCase("DECLINED")) {
            notificationService.notifyCollaborationDeclined(
                    updated.getIdPengguna(),
                    ownerId,
                    project.getProjectId(),
                    updated.getCollaborationId(),
                    project.getTitle()
            );
        }

        Profile profile = profileRepository.findByIdPengguna(updated.getIdPengguna())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        PendingCollaborationUserResponse collaborator = buildPendingUserResponse(updated, profile);

        return PendingCollaborationResponse.builder()
                .status(updated.getStatus().toLowerCase())
                .project(mapProjectDetail(project))
                .collaborators(List.of(collaborator))
                .build();
    }

    public PendingCollaborationResponse getPendingCollaborationUsers(
            String authorizationHeader,
            Integer projectId
    ) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        if (!project.getUser().getIdPengguna().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only project owner can view pending collaboration requests"
            );
        }

        List<PendingCollaborationUserResponse> pendingUsers = collaborationRepository
                .findByProjectIdAndStatus(projectId, "PENDING")
                .stream()
                .map(collaboration -> {
                    Profile profile = profileRepository.findByIdPengguna(collaboration.getIdPengguna())
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Profile not found"
                            ));

                    return buildPendingUserResponse(collaboration, profile);
                })
                .toList();

        return PendingCollaborationResponse.builder()
                .status("pending")
                .project(mapProjectDetail(project))
                .collaborators(pendingUsers)
                .build();
    }

    public PendingCollaborationResponse getProjectCollaborators(
            String authorizationHeader,
            Integer projectId
    ) {
        tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        List<PendingCollaborationUserResponse> collaborators = collaborationRepository
                .findByProjectIdAndStatus(projectId, "ACCEPTED")
                .stream()
                .map(collaboration -> {
                    Profile profile = profileRepository.findByIdPengguna(collaboration.getIdPengguna())
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Profile not found"
                            ));

                    return buildPendingUserResponse(collaboration, profile);
                })
                .toList();

        return PendingCollaborationResponse.builder()
                .status("accepted")
                .project(mapProjectDetail(project))
                .collaborators(collaborators)
                .build();
    }

    public CollaborationDashboardResponse getDashboard(String authorizationHeader) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<CollaborationResponse> requestCollab = collaborationRepository.findByIdPengguna(userId)
                .stream()
                .map(collaboration -> {
                    Project project = projectRepository.findById(collaboration.getProjectId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

                    return mapToResponse(collaboration, project);
                })
                .toList();

        List<ProjectResponse> ownedProjects = projectRepository.findByUser_IdPengguna(userId)
                .stream()
                .map(project -> ProjectResponse.builder()
                        .id(project.getProjectId())
                        .title(project.getTitle())
                        .description(project.getDescription())
                        .category(project.getCategory())
                        .status(project.getStatus())
                        .repositoryLink(project.getRepositoryLink())
                        .projectPicture(project.getFileUrl())
                        .deadline(project.getDeadline())
                        .build())
                .toList();

        return CollaborationDashboardResponse.builder()
                .requestCollab(requestCollab)
                .ownedProjects(ownedProjects)
                .build();
    }

    private PendingCollaborationUserResponse buildPendingUserResponse(
            Collaboration collaboration,
            Profile profile
    ) {
        return PendingCollaborationUserResponse.builder()
                .collaborationId(collaboration.getCollaborationId())
                .idPengguna(collaboration.getIdPengguna())
                .namaLengkap(profile.getNamaLengkap())
                .email(profile.getEmail())
                .profilePicture(profile.getProfilePicture())
                .institusi(profile.getInstitusi())
                .bio(profile.getBio())
                .keahlian(profile.getKeahlian())
                .lokasi(profile.getLokasi())
                .whatsapp(profile.getWhatsapp())
                .instagram(profile.getInstagram())
                .facebook(profile.getFacebook())
                .linkedin(profile.getLinkedin())
                .role(collaboration.getRole())
                .status(collaboration.getStatus())
                .requestStatus(collaboration.getStatus())
                .requestedAt(collaboration.getJoinDate())
                .build();
    }

    private PendingCollaborationResponse.ProjectDetail mapProjectDetail(Project project) {
        return PendingCollaborationResponse.ProjectDetail.builder()
                .projectId(project.getProjectId())
                .title(project.getTitle())
                .description(project.getDescription())
                .category(project.getCategory())
                .status(project.getStatus())
                .repositoryLink(project.getRepositoryLink())
                .projectPicture(project.getFileUrl())
                .deadline(project.getDeadline())
                .build();
    }

    private CollaborationResponse mapToResponse(Collaboration collaboration, Project project) {
        Profile ownerProfile = profileRepository.findByIdPengguna(project.getUser().getIdPengguna())
                .orElse(null);

        return CollaborationResponse.builder()
                .collaborationId(collaboration.getCollaborationId())
                .projectId(collaboration.getProjectId())
                .idPengguna(collaboration.getIdPengguna())
                .role(collaboration.getRole())
                .status(collaboration.getStatus())
                .project(CollaborationResponse.ProjectDetail.builder()
                        .projectId(project.getProjectId())
                        .title(project.getTitle())
                        .description(project.getDescription())
                        .category(project.getCategory())
                        .status(project.getStatus())
                        .repositoryLink(project.getRepositoryLink())
                        .projectPicture(project.getFileUrl())
                        .deadline(project.getDeadline())
                        .build())
                .owner(CollaborationResponse.OwnerDetail.builder()
                        .idPengguna(project.getUser().getIdPengguna())
                        .namaLengkap(project.getUser().getNamaLengkap())
                        .email(project.getUser().getEmail())
                        .profilePicture(ownerProfile != null ? ownerProfile.getProfilePicture() : null)
                        .build())
                .build();
    }
}