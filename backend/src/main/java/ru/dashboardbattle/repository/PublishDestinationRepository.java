package ru.dashboardbattle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dashboardbattle.entity.PublishDestination;

import java.util.List;

public interface PublishDestinationRepository extends JpaRepository<PublishDestination, Long> {

    List<PublishDestination> findByCompany_Id(Long companyId);

    List<PublishDestination> findByCompany_IdAndChannel_Id(Long companyId, Long channelId);
}
