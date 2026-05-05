package com.impal.gabungyuk.collaboration.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.impal.gabungyuk.collaboration.entity.Collaboration;

public interface CollaborationRepository extends JpaRepository<Collaboration, Integer> {

    List<Collaboration> findByProjectIdAndStatus(Integer projectId, String status);

    List<Collaboration> findByIdPengguna(Integer idPengguna);

    boolean existsByProjectIdAndIdPenggunaAndStatusIn(
            Integer projectId,
            Integer idPengguna,
            List<String> statuses
    );

    Optional<Collaboration> findByProjectIdAndIdPenggunaAndStatus(
            Integer projectId,
            Integer idPengguna,
            String status
    );
}