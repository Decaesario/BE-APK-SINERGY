package com.impal.gabungyuk.Activitylog.repository;

import com.impal.gabungyuk.Activitylog.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Integer> {
    List<ActivityLog> findByUser_IdPenggunaOrderByTimestampDesc(Integer idPengguna);
}