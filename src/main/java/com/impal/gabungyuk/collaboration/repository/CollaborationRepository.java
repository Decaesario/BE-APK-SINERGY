// package com.impal.gabungyuk.collaboration.repository;

// import org.springframework.data.jpa.repository.JpaRepository;
// import com.impal.gabungyuk.collaboration.entity.Collaboration;

// import java.util.List;

// public interface CollaborationRepository extends JpaRepository<Collaboration, Integer> {
//     // Define custom query methods if needed     
//     List<Collaboration> findByProjectId(Integer projectId);
//     List<Collaboration> findByIdPengguna(Integer idPengguna);
//     List<Collaboration> findByProjectIdAndIdPengguna(Integer projectId, Integer idPengguna);
// }

// package com.impal.gabungyuk.collaboration.repository;

// import java.util.List;

// import org.springframework.data.jpa.repository.JpaRepository;

// import com.impal.gabungyuk.collaboration.entity.Collaboration;

// public interface CollaborationRepository extends JpaRepository<Collaboration, Integer> {

//     List<Collaboration> findByIdPengguna(Integer idPengguna);

//     boolean existsByProjectIdAndIdPenggunaAndStatusIn(
//             Integer projectId,
//             Integer idPengguna,
//             List<String> statuses
//     );
// }

package com.impal.gabungyuk.collaboration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.impal.gabungyuk.collaboration.entity.Collaboration;

public interface CollaborationRepository extends JpaRepository<Collaboration, Integer> {

    List<Collaboration> findByIdPengguna(Integer idPengguna);

    boolean existsByProjectIdAndIdPenggunaAndStatusIn(
            Integer projectId,
            Integer idPengguna,
            List<String> statuses
    );
}