package com.impal.gabungyuk.collaboration.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.impal.gabungyuk.Activitylog.service.ActivityLogService;
import com.impal.gabungyuk.auth.entity.User;
import com.impal.gabungyuk.auth.respository.UserRepository;
import com.impal.gabungyuk.collaboration.entity.Collaboration;
import com.impal.gabungyuk.collaboration.model.request.CollaborationRequest;
import com.impal.gabungyuk.collaboration.model.response.PendingCollaborationResponse;
import com.impal.gabungyuk.collaboration.repository.CollaborationRepository;
import com.impal.gabungyuk.core.service.TokenService;
import com.impal.gabungyuk.core.service.UrlService;
import com.impal.gabungyuk.core.service.TimezoneService;
import com.impal.gabungyuk.notification.service.NotificationService;
import com.impal.gabungyuk.profile.entitiy.Profile;
import com.impal.gabungyuk.profile.repository.ProfileRepository;
import com.impal.gabungyuk.project.entity.Project;
import com.impal.gabungyuk.project.respository.ProjectRepository;

@ExtendWith(MockitoExtension.class)
public class CollaborationServiceTest {

    @Mock
    private CollaborationRepository collaborationRepository;
    @Mock
    private TokenService tokenService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private ActivityLogService activityLogService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private TimezoneService timezoneService;
    @Mock
    private UrlService urlService;

    private CollaborationService collaborationService;

    @BeforeEach
    void setUp() {
        collaborationService = new CollaborationService(
                collaborationRepository,
                tokenService,
                userRepository,
                projectRepository,
                profileRepository,
                activityLogService,
                notificationService,
                timezoneService,
                urlService
        );
    }

    @Test
    void testAcceptOrDecline_UserNotFound() {
        when(tokenService.extractUserIdFromAuthorizationHeader(anyString())).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            collaborationService.acceptOrDeclineCollaboration("Bearer token", new CollaborationRequest());
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }

    @Test
    void testAcceptOrDecline_ProjectIdNull() {
        when(tokenService.extractUserIdFromAuthorizationHeader(anyString())).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(new User()));

        CollaborationRequest request = new CollaborationRequest();
        request.setProjectId(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            collaborationService.acceptOrDeclineCollaboration("Bearer token", request);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Project id is required", exception.getReason());
    }

    @Test
    void testAcceptOrDecline_UserIdNull() {
        when(tokenService.extractUserIdFromAuthorizationHeader(anyString())).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(new User()));

        CollaborationRequest request = new CollaborationRequest();
        request.setProjectId(1);
        request.setUserId(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            collaborationService.acceptOrDeclineCollaboration("Bearer token", request);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("User id is required", exception.getReason());
    }

    @Test
    void testAcceptOrDecline_ActionNull() {
        when(tokenService.extractUserIdFromAuthorizationHeader(anyString())).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(new User()));

        CollaborationRequest request = new CollaborationRequest();
        request.setProjectId(1);
        request.setUserId(2);
        request.setAction(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            collaborationService.acceptOrDeclineCollaboration("Bearer token", request);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Action is required", exception.getReason());
    }

    @Test
    void testAcceptOrDecline_ProjectNotFound() {
        when(tokenService.extractUserIdFromAuthorizationHeader(anyString())).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(new User()));
        when(projectRepository.findActiveById(1)).thenReturn(Optional.empty());

        CollaborationRequest request = new CollaborationRequest();
        request.setProjectId(1);
        request.setUserId(2);
        request.setAction("ACCEPT");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            collaborationService.acceptOrDeclineCollaboration("Bearer token", request);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Project not found", exception.getReason());
    }

    @Test
    void testAcceptOrDecline_NotOwner() {
        User owner = new User();
        owner.setIdPengguna(1);

        User projectUser = new User();
        projectUser.setIdPengguna(2);

        Project project = new Project();
        project.setProjectId(1);
        project.setUser(projectUser);

        when(tokenService.extractUserIdFromAuthorizationHeader(anyString())).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(owner));
        when(projectRepository.findActiveById(1)).thenReturn(Optional.of(project));

        CollaborationRequest request = new CollaborationRequest();
        request.setProjectId(1);
        request.setUserId(3);
        request.setAction("ACCEPT");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            collaborationService.acceptOrDeclineCollaboration("Bearer token", request);
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Only project owner can accept or decline", exception.getReason());
    }

    @Test
    void testAcceptOrDecline_CollaborationNotFound() {
        User owner = new User();
        owner.setIdPengguna(1);

        Project project = new Project();
        project.setProjectId(1);
        project.setUser(owner);

        when(tokenService.extractUserIdFromAuthorizationHeader(anyString())).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(owner));
        when(projectRepository.findActiveById(1)).thenReturn(Optional.of(project));
        when(collaborationRepository.findByProjectIdAndIdPenggunaAndStatus(1, 2, "PENDING")).thenReturn(Optional.empty());

        CollaborationRequest request = new CollaborationRequest();
        request.setProjectId(1);
        request.setUserId(2);
        request.setAction("ACCEPT");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            collaborationService.acceptOrDeclineCollaboration("Bearer token", request);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Pending collaboration request not found", exception.getReason());
    }

    @Test
    void testAcceptOrDecline_InvalidAction() {
        User owner = new User();
        owner.setIdPengguna(1);

        Project project = new Project();
        project.setProjectId(1);
        project.setUser(owner);

        Collaboration collaboration = new Collaboration();
        collaboration.setProjectId(1);
        collaboration.setIdPengguna(2);
        collaboration.setStatus("PENDING");

        when(tokenService.extractUserIdFromAuthorizationHeader(anyString())).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(owner));
        when(projectRepository.findActiveById(1)).thenReturn(Optional.of(project));
        when(collaborationRepository.findByProjectIdAndIdPenggunaAndStatus(1, 2, "PENDING")).thenReturn(Optional.of(collaboration));

        CollaborationRequest request = new CollaborationRequest();
        request.setProjectId(1);
        request.setUserId(2);
        request.setAction("INVALID");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            collaborationService.acceptOrDeclineCollaboration("Bearer token", request);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Invalid action. Use ACCEPT or DECLINE", exception.getReason());
    }

    @Test
    void testAcceptOrDecline_SuccessAccept() {
        User owner = new User();
        owner.setIdPengguna(1);

        Project project = new Project();
        project.setProjectId(1);
        project.setUser(owner);
        project.setTitle("Project Title");

        Collaboration collaboration = new Collaboration();
        collaboration.setCollaborationId(100);
        collaboration.setProjectId(1);
        collaboration.setIdPengguna(2);
        collaboration.setStatus("PENDING");
        collaboration.setJoinDate(LocalDateTime.now());

        Profile profile = new Profile();
        profile.setIdPengguna(2);
        profile.setNamaLengkap("Collaborator");

        when(tokenService.extractUserIdFromAuthorizationHeader(anyString())).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(owner));
        when(projectRepository.findActiveById(1)).thenReturn(Optional.of(project));
        when(collaborationRepository.findByProjectIdAndIdPenggunaAndStatus(1, 2, "PENDING")).thenReturn(Optional.of(collaboration));
        when(collaborationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(profileRepository.findByIdPengguna(2)).thenReturn(Optional.of(profile));

        CollaborationRequest request = new CollaborationRequest();
        request.setProjectId(1);
        request.setUserId(2);
        request.setAction("ACCEPT");

        PendingCollaborationResponse response = collaborationService.acceptOrDeclineCollaboration("Bearer token", request);

        assertEquals("accepted", response.getStatus());
        verify(notificationService).notifyCollaborationAccepted(anyInt(), anyInt(), anyInt(), anyInt(), anyString());
    }

    @Test
    void testAcceptOrDecline_SuccessDecline() {
        User owner = new User();
        owner.setIdPengguna(1);

        Project project = new Project();
        project.setProjectId(1);
        project.setUser(owner);
        project.setTitle("Project Title");

        Collaboration collaboration = new Collaboration();
        collaboration.setCollaborationId(100);
        collaboration.setProjectId(1);
        collaboration.setIdPengguna(2);
        collaboration.setStatus("PENDING");
        collaboration.setJoinDate(LocalDateTime.now());

        Profile profile = new Profile();
        profile.setIdPengguna(2);
        profile.setNamaLengkap("Collaborator");

        when(tokenService.extractUserIdFromAuthorizationHeader(anyString())).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(owner));
        when(projectRepository.findActiveById(1)).thenReturn(Optional.of(project));
        when(collaborationRepository.findByProjectIdAndIdPenggunaAndStatus(1, 2, "PENDING")).thenReturn(Optional.of(collaboration));
        when(collaborationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(profileRepository.findByIdPengguna(2)).thenReturn(Optional.of(profile));

        CollaborationRequest request = new CollaborationRequest();
        request.setProjectId(1);
        request.setUserId(2);
        request.setAction("DECLINE");

        PendingCollaborationResponse response = collaborationService.acceptOrDeclineCollaboration("Bearer token", request);

        assertEquals("declined", response.getStatus());
        verify(notificationService).notifyCollaborationDeclined(anyInt(), anyInt(), anyInt(), anyInt(), anyString());
    }

    @Test
    void testAcceptOrDecline_ProfileNotFound() {
        User owner = new User();
        owner.setIdPengguna(1);

        Project project = new Project();
        project.setProjectId(1);
        project.setUser(owner);
        project.setTitle("Project Title");

        Collaboration collaboration = new Collaboration();
        collaboration.setCollaborationId(100);
        collaboration.setProjectId(1);
        collaboration.setIdPengguna(2);
        collaboration.setStatus("PENDING");
        collaboration.setJoinDate(LocalDateTime.now());

        when(tokenService.extractUserIdFromAuthorizationHeader(anyString())).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(owner));
        when(projectRepository.findActiveById(1)).thenReturn(Optional.of(project));
        when(collaborationRepository.findByProjectIdAndIdPenggunaAndStatus(1, 2, "PENDING")).thenReturn(Optional.of(collaboration));
        when(collaborationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(profileRepository.findByIdPengguna(2)).thenReturn(Optional.empty());

        CollaborationRequest request = new CollaborationRequest();
        request.setProjectId(1);
        request.setUserId(2);
        request.setAction("ACCEPT");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            collaborationService.acceptOrDeclineCollaboration("Bearer token", request);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Profile not found", exception.getReason());
    }
}
