package com.impal.gabungyuk.project.respository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.impal.gabungyuk.project.entity.Project;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Integer> {

    List<Project> findByUser_IdPengguna(Integer idPengguna);
}