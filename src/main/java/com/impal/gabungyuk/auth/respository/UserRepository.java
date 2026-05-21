package com.impal.gabungyuk.auth.respository;

import com.impal.gabungyuk.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
    
    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByFirebaseUid(String firebaseUid);

    List<User> findByNamaLengkapContainingIgnoreCase(String namaLengkap);
} 
