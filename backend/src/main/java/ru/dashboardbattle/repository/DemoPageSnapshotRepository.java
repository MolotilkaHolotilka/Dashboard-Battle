package ru.dashboardbattle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dashboardbattle.entity.DemoPageSnapshot;

import java.util.Optional;

public interface DemoPageSnapshotRepository extends JpaRepository<DemoPageSnapshot, Long> {

    Optional<DemoPageSnapshot> findByShareToken(String shareToken);

    void deleteByPublicationId(Long publicationId);
}
