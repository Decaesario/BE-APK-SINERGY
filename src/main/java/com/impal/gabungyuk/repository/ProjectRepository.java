package com.impal.gabungyuk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.impal.gabungyuk.entity.Project;
import java.util.List;


public interface ProjectRepository extends JpaRepository<Project, Integer> {
    List<Project> findByUserIdPengguna(Integer idPengguna);
}
