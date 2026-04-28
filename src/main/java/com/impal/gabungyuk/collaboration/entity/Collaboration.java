package com.impal.gabungyuk.collaboration.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.impal.gabungyuk.project.entity.Project;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "collaboration")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Collaboration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "collaboration_id")
    private Integer collaborationId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

    @Column(name = "project_id", nullable = false)
    private Integer projectId;

    @Column(name = "id_pengguna", nullable = false)
    private Integer idPengguna;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "join_date", nullable = false)
    private LocalDateTime joinDate;
}
