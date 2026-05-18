package com.impal.gabungyuk.rating.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.impal.gabungyuk.rating.entity.ProjectRating;

public interface ProjectRatingRepository extends JpaRepository<ProjectRating, Long> {

    boolean existsByProjectIdAndRatedUserId(Integer projectId, Integer ratedUserId);

    Optional<ProjectRating> findByProjectIdAndRatedUserId(Integer projectId, Integer ratedUserId);

    List<ProjectRating> findByRatedUserIdOrderByCreatedAtDesc(Integer ratedUserId);

    List<ProjectRating> findByProjectIdOrderByCreatedAtDesc(Integer projectId);

    @Query("SELECT AVG(r.ratingValue) FROM ProjectRating r WHERE r.ratedUserId = :ratedUserId")
    Double getAverageRatingByRatedUserId(Integer ratedUserId);

    Long countByRatedUserId(Integer ratedUserId);
}