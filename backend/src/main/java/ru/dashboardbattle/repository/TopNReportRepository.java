package ru.dashboardbattle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dashboardbattle.entity.TopNReport;

import java.util.List;

public interface TopNReportRepository extends JpaRepository<TopNReport, Long> {

    List<TopNReport> findByCompany_IdOrderByCreatedAtDesc(Long companyId);

    List<TopNReport> findByCompany_IdAndStatusNotOrderByCreatedAtDesc(Long companyId, String status);

    List<TopNReport> findByCompany_IdAndStatusOrderByCreatedAtDesc(Long companyId, String status);
}
