package com.impal.gabungyuk.Activitylog.entity;
import com.impal.gabungyuk.auth.entity.User;
import com.impal.gabungyuk.project.entity.Project;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "activity_log")
@Builder
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_log_id")
    private Integer activityLogId;

    @ManyToOne
    @JoinColumn(name = "id_pengguna", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name= "message", nullable = false)
    private String message;

    @Column(name= "is_read", nullable = false)
    private Boolean isRead;

    @Column(name= "timestamp", nullable = false)
    private LocalDateTime timestamp;

}
