package com.impal.gabungyuk.search.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.impal.gabungyuk.auth.entity.User;
import com.impal.gabungyuk.auth.respository.UserRepository;
import com.impal.gabungyuk.core.service.TokenService;
import com.impal.gabungyuk.core.service.TimezoneService;
import com.impal.gabungyuk.project.entity.Project;
import com.impal.gabungyuk.project.respository.ProjectRepository;
import com.impal.gabungyuk.search.model.response.SearchProjectResponse;
import com.impal.gabungyuk.search.model.response.SearchResponse;
import com.impal.gabungyuk.search.model.response.SearchUserResponse;

@Service
public class SearchServiceImpl implements SearchService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TokenService tokenService;
    private final TimezoneService timezoneService;

    public SearchServiceImpl(
            UserRepository userRepository,
            ProjectRepository projectRepository,
            TokenService tokenService,
            TimezoneService timezoneService
    ) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.tokenService = tokenService;
        this.timezoneService = timezoneService;
    }

    @Override
    public SearchResponse globalSearch(
            String authorizationHeader,
            String query
    ) {

        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(
                authorizationHeader
        );

        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
        String viewerTz = timezoneService.getUserTimezoneOrDefault(userId);

        List<User> users = userRepository
                .findByNamaLengkapContainingIgnoreCase(query);

        List<Project> projects = projectRepository
                .findActiveByTitleContaining(query);

        List<SearchUserResponse> userResponses = users.stream()
                .map(user -> SearchUserResponse.builder()
                        .userId(user.getIdPengguna())
                        .namaLengkap(user.getNamaLengkap())
                        .profilePicture(user.getProfilePicture())
                        .bio(user.getBio())
                        .institusi(user.getInstitusi())
                        .build())
                .toList();

        List<SearchProjectResponse> projectResponses = projects.stream()
                .map(project -> SearchProjectResponse.builder()
                        .id(project.getProjectId())
                        .title(project.getTitle())
                        .description(project.getDescription())
                        .category(project.getCategory())
                        .status(project.getStatus())
                        .repositoryLink(project.getRepositoryLink())
                        .projectPicture(project.getFileUrl())
                        .deadline(timezoneService.convertToUserZone(project.getDeadline(), viewerTz))
                        .build())
                .toList();

        return SearchResponse.builder()
                .users(userResponses)
                .projects(projectResponses)
                .build();
    }
}
