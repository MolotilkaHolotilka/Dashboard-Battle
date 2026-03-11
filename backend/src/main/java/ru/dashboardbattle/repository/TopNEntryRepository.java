package ru.dashboardbattle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dashboardbattle.entity.TopNEntry;

import java.util.List;

public interface TopNEntryRepository extends JpaRepository<TopNEntry, Long> {

    List<TopNEntry> findByReport_Id(Long reportId);
}
