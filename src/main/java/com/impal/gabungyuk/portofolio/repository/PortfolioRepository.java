package com.impal.gabungyuk.portofolio.repository;

import com.impal.gabungyuk.portofolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Integer> {
    List<Portfolio> findAllByIdPengguna(Integer idPengguna);
}