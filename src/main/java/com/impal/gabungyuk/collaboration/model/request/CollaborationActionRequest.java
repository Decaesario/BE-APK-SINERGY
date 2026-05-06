package com.impal.gabungyuk.collaboration.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollaborationActionRequest {

    private Integer projectId;
    private Integer idPengguna;
    private String action;
}