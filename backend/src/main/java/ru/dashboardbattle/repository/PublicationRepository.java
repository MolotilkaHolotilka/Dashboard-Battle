package ru.dashboardbattle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dashboardbattle.entity.Publication;

import java.util.List;

public interface PublicationRepository extends JpaRepository<Publication, Long> {

    List<Publication> findByReport_Id(Long reportId);

    List<Publication> findByDestination_Company_Id(Long companyId);

    List<Publication> findByDestination_Company_IdAndDestination_Channel_Id(Long companyId, Long channelId);

    List<Publication> findByDestination_Id(Long destinationId);
}
