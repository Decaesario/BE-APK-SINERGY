package com.impal.gabungyuk.project.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.impal.gabungyuk.project.entity.Project;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Integer> {

    @Query("SELECT p FROM Project p WHERE p.projectId = :projectId AND (p.status IS NULL OR UPPER(p.status) <> 'DELETED')")
    Optional<Project> findActiveById(@Param("projectId") Integer projectId);

    @Query("SELECT p FROM Project p WHERE p.user.idPengguna = :idPengguna AND (p.status IS NULL OR UPPER(p.status) <> 'DELETED')")
    List<Project> findActiveByUserId(@Param("idPengguna") Integer idPengguna);

    @Query("SELECT p FROM Project p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')) AND (p.status IS NULL OR UPPER(p.status) <> 'DELETED')")
    List<Project> findActiveByTitleContaining(@Param("title") String title);

    @Query("SELECT p FROM Project p WHERE p.status IS NULL OR UPPER(p.status) <> 'DELETED'")
    List<Project> findAllActive();
}