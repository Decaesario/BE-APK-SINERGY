package com.impal.gabungyuk.calender.service;

import com.impal.gabungyuk.calender.entity.Calendar;
import com.impal.gabungyuk.calender.model.response.CalendarResponse;
import com.impal.gabungyuk.calender.repository.CalendarRepository;
import com.impal.gabungyuk.collaboration.entity.Collaboration;
import com.impal.gabungyuk.collaboration.repository.CollaborationRepository;
import com.impal.gabungyuk.core.service.TokenService;
import com.impal.gabungyuk.core.service.TimezoneService;
import com.impal.gabungyuk.project.entity.Project;
import com.impal.gabungyuk.project.respository.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CalendarService {

    private final CalendarRepository calendarRepository;
    private final TokenService tokenService;
    private final ProjectRepository projectRepository;
    private final CollaborationRepository collaborationRepository;
    private final TimezoneService timezoneService;

    public CalendarService(
            CalendarRepository calendarRepository,
            TokenService tokenService,
            ProjectRepository projectRepository,
            CollaborationRepository collaborationRepository,
            TimezoneService timezoneService
    ) {
        this.calendarRepository = calendarRepository;
        this.tokenService = tokenService;
        this.projectRepository = projectRepository;
        this.collaborationRepository = collaborationRepository;
        this.timezoneService = timezoneService;
    }

    // GET semua deadline (exclude yang is_done = true)
    public List<CalendarResponse> getMyCalendar(String authorizationHeader) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);
        String viewerTz = timezoneService.getUserTimezoneOrDefault(userId);

        List<Project> ownedProjects = projectRepository.findActiveByUserId(userId);

        List<Integer> collaboratedProjectIds = collaborationRepository
                .findByIdPengguna(userId)
                .stream()
                .filter(c -> c.getStatus().equalsIgnoreCase("ACCEPTED"))
                .map(Collaboration::getProjectId)
                .collect(Collectors.toList());

        List<Project> collaboratedProjects = collaboratedProjectIds.stream()
                .map(projectId -> projectRepository.findActiveById(projectId).orElse(null))
                .filter(p -> p != null)
                .collect(Collectors.toList());

        List<Project> allProjects = new ArrayList<>();
        allProjects.addAll(ownedProjects);
        allProjects.addAll(collaboratedProjects);

        return allProjects.stream()
                .filter(p -> p.getDeadline() != null)
                .filter(p -> {
                    // Cek apakah user sudah mark as done project ini
                    return calendarRepository
                            .findByProjectIdAndIdPengguna(p.getProjectId(), userId)
                            .map(c -> !c.getIsDone())
                            .orElse(true); // kalau belum ada record, berarti belum done
                })
                .map(p -> projectToCalendarResponse(p, viewerTz))
                .collect(Collectors.toList());
    }

    // GET filter by bulan & tahun
    public List<CalendarResponse> getMyCalendarByMonthAndYear(
            String authorizationHeader, int month, int year) {
        return getMyCalendar(authorizationHeader)
                .stream()
                .filter(c -> c.getDeadline() != null
                        && c.getDeadline().getMonthValue() == month
                        && c.getDeadline().getYear() == year)
                .collect(Collectors.toList());
    }

    // PATCH mark as done
    public void markAsDone(String authorizationHeader, Integer projectId) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        // Cek project ada tidak
        projectRepository.findActiveById(projectId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Project not found"));

        // Cek sudah ada record calendar atau belum
        Calendar calendar = calendarRepository
                .findByProjectIdAndIdPengguna(projectId, userId)
                .orElse(Calendar.builder()
                        .projectId(projectId)
                        .idPengguna(userId)
                        .isDone(false)
                        .build());

        calendar.setIsDone(true);
        calendarRepository.save(calendar);
    }

    private CalendarResponse projectToCalendarResponse(Project project, String viewerTimezone) {
        CalendarResponse response = new CalendarResponse();
        response.setEventId(project.getProjectId());
        response.setProjectId(project.getProjectId());
        response.setTitle(project.getTitle());
        response.setDescription(project.getDescription());
        response.setDeadline(timezoneService.convertToUserZone(project.getDeadline(), viewerTimezone));
        response.setIsDone(false);
        return response;
    }
}
