package com.impal.gabungyuk.project.entity;

import java.time.LocalDateTime;

import com.impal.gabungyuk.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "project")
@Builder
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Integer projectId;

    //foregein key 
    @ManyToOne
    @JoinColumn(name = "id_pengguna")
    private User user;
    
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "category", nullable = true)
    private String category;

    @Column(name = "status", nullable = true)
    private String status;
    
    @Column(name = "repository_link", nullable = true)
    private String repositoryLink;

     @Column(name = "file_url", nullable = true)
    private String fileUrl;

     @Column(name = "created_at", nullable = true)
    private LocalDateTime createdAt;



}