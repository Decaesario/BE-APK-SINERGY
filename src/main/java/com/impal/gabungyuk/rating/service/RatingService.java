package com.impal.gabungyuk.rating.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.impal.gabungyuk.auth.entity.User;
import com.impal.gabungyuk.auth.respository.UserRepository;
import com.impal.gabungyuk.collaboration.repository.CollaborationRepository;
import com.impal.gabungyuk.core.service.TokenService;
import com.impal.gabungyuk.core.service.TimezoneService;
import com.impal.gabungyuk.profile.entitiy.Profile;
import com.impal.gabungyuk.profile.repository.ProfileRepository;
import com.impal.gabungyuk.project.entity.Project;
import com.impal.gabungyuk.project.respository.ProjectRepository;
import com.impal.gabungyuk.rating.entity.ProjectRating;
import com.impal.gabungyuk.rating.model.request.RatingRequest;
import com.impal.gabungyuk.rating.model.response.AverageRatingResponse;
import com.impal.gabungyuk.rating.model.response.RatingResponse;
import com.impal.gabungyuk.rating.model.response.UserRatingSummaryResponse;
import com.impal.gabungyuk.rating.repository.ProjectRatingRepository;

@Service
public class RatingService {

    private final ProjectRatingRepository projectRatingRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final CollaborationRepository collaborationRepository;
    private final TokenService tokenService;
    private final TimezoneService timezoneService;

    public RatingService(
            ProjectRatingRepository projectRatingRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            ProfileRepository profileRepository,
            CollaborationRepository collaborationRepository,
            TokenService tokenService,
            TimezoneService timezoneService
    ) {
        this.projectRatingRepository = projectRatingRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.collaborationRepository = collaborationRepository;
        this.tokenService = tokenService;
        this.timezoneService = timezoneService;
    }

    public RatingResponse createRating(
            String authorizationHeader,
            RatingRequest request
    ) {
        Integer ownerUserId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        User owner = userRepository.findById(ownerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner user not found"));

        if (request.getProjectId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project id is required");
        }

        if (request.getRatedUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rated user id is required");
        }

        if (request.getRatingValue() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating value is required");
        }

        if (request.getRatingValue() < 1 || request.getRatingValue() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating value must be between 1 and 5");
        }

        Project project = findActiveProjectById(request.getProjectId());

        if (!project.getUser().getIdPengguna().equals(ownerUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only project owner can give rating");
        }

        if (project.getStatus() == null || !project.getStatus().equalsIgnoreCase("COMPLETED")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Project must be completed before giving rating"
            );
        }

        if (request.getRatedUserId().equals(ownerUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner cannot rate themselves");
        }

        userRepository.findById(request.getRatedUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rated user not found"));

        boolean isAcceptedCollaborator = collaborationRepository
                .findByProjectIdAndIdPenggunaAndStatus(
                        request.getProjectId(),
                        request.getRatedUserId(),
                        "ACCEPTED"
                )
                .isPresent();

        if (!isAcceptedCollaborator) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Rated user must be an accepted collaborator in this project"
            );
        }

        boolean alreadyRated = projectRatingRepository.existsByProjectIdAndRatedUserId(
                request.getProjectId(),
                request.getRatedUserId()
        );

        if (alreadyRated) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This user has already been rated for this project"
            );
        }

        ProjectRating rating = ProjectRating.builder()
                .projectId(request.getProjectId())
                .ratedUserId(request.getRatedUserId())
                .ownerUserId(ownerUserId)
                .ratingValue(request.getRatingValue())
                .review(request.getReview())
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .build();

        ProjectRating savedRating = projectRatingRepository.save(rating);

        String viewerTz = timezoneService.getUserTimezoneOrDefault(ownerUserId);
        return mapToRatingResponse(savedRating, viewerTz);
    }

    public UserRatingSummaryResponse getUserRatings(Integer userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Profile profile = profileRepository.findByIdPengguna(userId)
                .orElse(null);
        String viewerTz = timezoneService.getUserTimezoneOrDefault(userId);

        List<RatingResponse> ratings = projectRatingRepository.findByRatedUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(rating -> mapToRatingResponse(rating, viewerTz))
                .toList();

        Double averageRating = projectRatingRepository.getAverageRatingByRatedUserId(userId);
        Long totalRatings = projectRatingRepository.countByRatedUserId(userId);

        return UserRatingSummaryResponse.builder()
                .userId(userId)
                .namaLengkap(profile != null ? profile.getNamaLengkap() : null)
                .profilePicture(profile != null ? profile.getProfilePicture() : null)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalRatings(totalRatings)
                .ratings(ratings)
                .build();
    }

    public AverageRatingResponse getUserAverageRating(Integer userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Profile profile = profileRepository.findByIdPengguna(userId)
                .orElse(null);

        Double averageRating = projectRatingRepository.getAverageRatingByRatedUserId(userId);
        Long totalRatings = projectRatingRepository.countByRatedUserId(userId);

        return AverageRatingResponse.builder()
                .userId(userId)
                .namaLengkap(profile != null ? profile.getNamaLengkap() : null)
                .profilePicture(profile != null ? profile.getProfilePicture() : null)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalRatings(totalRatings)
                .build();
    }

    public List<RatingResponse> getProjectRatings(Integer projectId) {
        findActiveProjectById(projectId);
        String viewerTz = timezoneService.getUserTimezoneOrDefault(null);

        return projectRatingRepository.findByProjectIdOrderByCreatedAtDesc(projectId)
                .stream()
                .map(rating -> mapToRatingResponse(rating, viewerTz))
                .toList();
    }

    private RatingResponse mapToRatingResponse(ProjectRating rating, String viewerTimezone) {
        Project project = projectRepository.findById(rating.getProjectId())
                .orElse(null);

        Profile ratedUserProfile = profileRepository.findByIdPengguna(rating.getRatedUserId())
                .orElse(null);

        Profile ownerProfile = profileRepository.findByIdPengguna(rating.getOwnerUserId())
                .orElse(null);

        return RatingResponse.builder()
                .ratingId(rating.getRatingId())
                .projectId(rating.getProjectId())
                .projectTitle(project != null ? project.getTitle() : null)
                .ratedUserId(rating.getRatedUserId())
                .ratedUserName(ratedUserProfile != null ? ratedUserProfile.getNamaLengkap() : null)
                .ratedUserProfilePicture(ratedUserProfile != null ? ratedUserProfile.getProfilePicture() : null)
                .ownerUserId(rating.getOwnerUserId())
                .ownerName(ownerProfile != null ? ownerProfile.getNamaLengkap() : null)
                .ownerProfilePicture(ownerProfile != null ? ownerProfile.getProfilePicture() : null)
                .ratingValue(rating.getRatingValue())
                .review(rating.getReview())
                .createdAt(timezoneService.convertToUserZone(rating.getCreatedAt(), viewerTimezone))
                .updatedAt(timezoneService.convertToUserZone(rating.getUpdatedAt(), viewerTimezone))
                .build();
    }

    private Project findActiveProjectById(Integer projectId) {
        return projectRepository.findActiveById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }
}
