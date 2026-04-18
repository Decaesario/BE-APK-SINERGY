package com.impal.gabungyuk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.impal.gabungyuk.entity.Project;


public interface ProjectRepository extends JpaRepository<Project, Integer> {
    
}
