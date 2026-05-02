// package com.impal.gabungyuk.collaboration.service;

// import java.time.LocalDateTime;
// import java.util.List;

// import org.springframework.http.HttpStatus;
// import org.springframework.stereotype.Service;
// import org.springframework.web.server.ResponseStatusException;

// import com.impal.gabungyuk.auth.respository.UserRepository;
// import com.impal.gabungyuk.collaboration.entity.Collaboration;
// import com.impal.gabungyuk.collaboration.model.request.CollaborationRequest;
// import com.impal.gabungyuk.collaboration.model.response.CollaborationResponse;
// import com.impal.gabungyuk.collaboration.repository.CollaborationRepository;
// import com.impal.gabungyuk.core.service.TokenService;
// import com.impal.gabungyuk.project.entity.Project;
// import com.impal.gabungyuk.project.respository.ProjectRepository;

// @Service
// public class CollaborationService {

//     private final CollaborationRepository collaborationRepository;
//     private final TokenService tokenService;
//     private final UserRepository userRepository;
//     private final ProjectRepository projectRepository;

//     public CollaborationService(
//             CollaborationRepository collaborationRepository,
//             TokenService tokenService,
//             UserRepository userRepository,
//             ProjectRepository projectRepository
//     ) {
//         this.collaborationRepository = collaborationRepository;
//         this.tokenService = tokenService;
//         this.userRepository = userRepository;
//         this.projectRepository = projectRepository;
//     }

//     public CollaborationResponse requestCollaboration(CollaborationRequest request, String authorizationHeader) {
//         Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

//         userRepository.findById(userId)
//                 .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

//         Project project = projectRepository.findById(request.getProjectId())
//                 .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

//         if (project.getUser().getIdPengguna().equals(userId)) {
//             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot request collaboration on your own project");
//         }

//         boolean alreadyRequested = collaborationRepository.existsByProjectIdAndIdPenggunaAndStatusIn(
//                 project.getProjectId(),
//                 userId,
//                 List.of("PENDING", "ACCEPTED")
//         );

//         if (alreadyRequested) {
//             throw new ResponseStatusException(
//                     HttpStatus.CONFLICT,
//                     "You have already requested collaboration for this project"
//             );
//         }

//         Collaboration collaboration = Collaboration.builder()
//                 .projectId(project.getProjectId())
//                 .idPengguna(userId)
//                 .role(request.getRole())
//                 .status("PENDING")
//                 .joinDate(LocalDateTime.now())
//                 .build();

//         Collaboration saved = collaborationRepository.save(collaboration);

//         return mapToResponse(saved, project);
//     }

//     public CollaborationResponse getCollaborationById(String authorizationHeader, Integer collaborationId) {
//         Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

//         userRepository.findById(userId)
//                 .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

//         Collaboration collaboration = collaborationRepository.findById(collaborationId)
//                 .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collaboration not found"));

//         Project project = projectRepository.findById(collaboration.getProjectId())
//                 .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

//         return mapToResponse(collaboration, project);
//     }

//     public CollaborationResponse acceptOrDeclineCollaboration(
//             String authorizationHeader,
//             Integer collaborationId,
//             String action
//     ) {
//         Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

//         userRepository.findById(userId)
//                 .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

//         Collaboration collaboration = collaborationRepository.findById(collaborationId)
//                 .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collaboration not found"));

//         Project project = projectRepository.findById(collaboration.getProjectId())
//                 .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

//         if (!project.getUser().getIdPengguna().equals(userId)) {
//             throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only project owner can accept or decline");
//         }

//         if (action.equalsIgnoreCase("ACCEPT")) {
//             collaboration.setStatus("ACCEPTED");
//         } else if (action.equalsIgnoreCase("DECLINE")) {
//             collaboration.setStatus("DECLINED");
//         } else {
//             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid action");
//         }

//         Collaboration updated = collaborationRepository.save(collaboration);

//         return mapToResponse(updated, project);
//     }

//     private CollaborationResponse mapToResponse(Collaboration collaboration, Project project) {
//         return CollaborationResponse.builder()
//                 .collaborationId(collaboration.getCollaborationId())
//                 .projectId(collaboration.getProjectId())
//                 .idPengguna(collaboration.getIdPengguna())
//                 .role(collaboration.getRole())
//                 .status(collaboration.getStatus())
//                 .project(CollaborationResponse.ProjectDetail.builder()
//                         .projectId(project.getProjectId())
//                         .title(project.getTitle())
//                         .description(project.getDescription())
//                         .category(project.getCategory())
//                         .status(project.getStatus())
//                         .repositoryLink(project.getRepositoryLink())
//                         .fileUrl(project.getFileUrl())
//                         .build())
//                 .owner(CollaborationResponse.OwnerDetail.builder()
//                         .idPengguna(project.getUser().getIdPengguna())
//                         .username(project.getUser().getUsername())
//                         .email(project.getUser().getEmail())
//                         .build())
//                 .build();
//     }
// }
package com.impal.gabungyuk.collaboration.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.impal.gabungyuk.auth.respository.UserRepository;
import com.impal.gabungyuk.collaboration.entity.Collaboration;
import com.impal.gabungyuk.collaboration.model.request.CollaborationRequest;
import com.impal.gabungyuk.collaboration.model.response.CollaborationDashboardResponse;
import com.impal.gabungyuk.collaboration.model.response.CollaborationResponse;
import com.impal.gabungyuk.collaboration.repository.CollaborationRepository;
import com.impal.gabungyuk.core.service.TokenService;
import com.impal.gabungyuk.project.entity.Project;
import com.impal.gabungyuk.project.model.response.ProjectResponse;
import com.impal.gabungyuk.project.respository.ProjectRepository;

@Service
public class CollaborationService {

    private final CollaborationRepository collaborationRepository;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public CollaborationService(
            CollaborationRepository collaborationRepository,
            TokenService tokenService,
            UserRepository userRepository,
            ProjectRepository projectRepository
    ) {
        this.collaborationRepository = collaborationRepository;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    public CollaborationResponse requestCollaboration(CollaborationRequest request, String authorizationHeader) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Project project = projectRepository.findById(request.getProjectId())
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
                .role(request.getRole())
                .status("PENDING")
                .joinDate(LocalDateTime.now())
                .build();

        Collaboration saved = collaborationRepository.save(collaboration);

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

    public CollaborationResponse acceptOrDeclineCollaboration(
            String authorizationHeader,
            Integer collaborationId,
            String action
    ) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Collaboration collaboration = collaborationRepository.findById(collaborationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collaboration not found"));

        Project project = projectRepository.findById(collaboration.getProjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        if (!project.getUser().getIdPengguna().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only project owner can accept or decline");
        }

        if (action.equalsIgnoreCase("ACCEPT")) {
            collaboration.setStatus("ACCEPTED");
        } else if (action.equalsIgnoreCase("DECLINE")) {
            collaboration.setStatus("DECLINED");
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid action");
        }

        Collaboration updated = collaborationRepository.save(collaboration);

        return mapToResponse(updated, project);
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
                        .fileUrl(project.getFileUrl())
                        .build())
                .toList();

        return CollaborationDashboardResponse.builder()
                .requestCollab(requestCollab)
                .ownedProjects(ownedProjects)
                .build();
    }

    private CollaborationResponse mapToResponse(Collaboration collaboration, Project project) {
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
                        .fileUrl(project.getFileUrl())
                        .build())
                .owner(CollaborationResponse.OwnerDetail.builder()
                        .idPengguna(project.getUser().getIdPengguna())
                        .namaLengkap(project.getUser().getNamaLengkap())
                        .email(project.getUser().getEmail())
                        .build())
                .build();
    }
}