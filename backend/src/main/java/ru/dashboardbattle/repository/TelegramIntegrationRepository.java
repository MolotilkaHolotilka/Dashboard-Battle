package ru.dashboardbattle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dashboardbattle.entity.TelegramIntegration;

import java.util.List;
import java.util.Optional;

public interface TelegramIntegrationRepository extends JpaRepository<TelegramIntegration, Long> {

    Optional<TelegramIntegration> findByCompany_Id(Long companyId);

    List<TelegramIntegration> findAllByCompany_Id(Long companyId);
}
