package ru.dashboardbattle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dashboardbattle.entity.TopNReport;

import java.util.List;

public interface TopNReportRepository extends JpaRepository<TopNReport, Long> {

    List<TopNReport> findByCompany_Id(Long companyId);

    List<TopNReport> findByCompany_IdAndStatus(Long companyId, String status);
}
