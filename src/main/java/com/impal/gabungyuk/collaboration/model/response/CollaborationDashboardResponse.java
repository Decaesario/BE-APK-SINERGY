package com.impal.gabungyuk.collaboration.model.response;

import java.util.List;

import com.impal.gabungyuk.project.model.response.ProjectResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CollaborationDashboardResponse {

    private List<CollaborationResponse> requestCollab;
    private List<ProjectResponse> ownedProjects;
}