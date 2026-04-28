package com.impal.gabungyuk.project.respository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.impal.gabungyuk.project.entity.Project;

import java.util.List;


public interface ProjectRepository extends JpaRepository<Project, Integer> {
    // Use explicit property navigation for clarity and to avoid derived query issues
    List<Project> findByUser_IdPengguna(Integer idPengguna);
}
