package com.impal.gabungyuk.collaboration.model.response;

import java.time.LocalDateTime;

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
public class PendingCollaborationUserResponse {

    private Integer collaborationId;
    private Integer idPengguna;

    private String namaLengkap;
    private String email;
    private String profilePicture;
    private String institusi;
    private String bio;
    private String keahlian;
    private String lokasi;
    private String whatsapp;

    private String role;
    private String status;

    private String requestStatus;
    private LocalDateTime requestedAt;
}