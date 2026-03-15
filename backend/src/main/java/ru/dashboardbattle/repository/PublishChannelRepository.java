package ru.dashboardbattle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dashboardbattle.entity.PublishChannel;

import java.util.Optional;

public interface PublishChannelRepository extends JpaRepository<PublishChannel, Long> {

    Optional<PublishChannel> findByCode(String code);
}
