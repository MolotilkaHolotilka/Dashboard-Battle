package ru.dashboardbattle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dashboardbattle.entity.MoySkladIntegration;

import java.util.List;
import java.util.Optional;

public interface MoySkladIntegrationRepository extends JpaRepository<MoySkladIntegration, Long> {

    Optional<MoySkladIntegration> findByCompany_Id(Long companyId);

    List<MoySkladIntegration> findAllByCompany_Id(Long companyId);
}
