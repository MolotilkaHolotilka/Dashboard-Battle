package ru.dashboardbattle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dashboardbattle.entity.Company;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    List<Company> findByUser_Id(Long userId);
}
