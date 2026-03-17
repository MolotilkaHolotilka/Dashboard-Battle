package ru.dashboardbattle;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.dashboardbattle.entity.Company;
import ru.dashboardbattle.entity.User;
import ru.dashboardbattle.repository.CompanyRepository;
import ru.dashboardbattle.repository.UserRepository;
import ru.dashboardbattle.service.DashboardBattleService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DashboardBattleApplicationTests {

    @Autowired
    private DashboardBattleService dashboardBattleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    void contextLoads() {
        assertThat(dashboardBattleService).isNotNull();
    }

    @Test
    void userRepository_saveFindAndDelete() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hash");
        user = userRepository.save(user);
        assertThat(user.getId()).isNotNull();

        var found = userRepository.findById(user.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");

        userRepository.delete(user);
        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @Test
    void companyRepository_saveFindAndDelete() {
        User user = new User();
        user.setEmail("owner@example.com");
        user.setPasswordHash("hash");
        user = userRepository.save(user);

        Company company = new Company();
        company.setName("Test Company");
        company.setUser(user);
        company = companyRepository.save(company);
        assertThat(company.getId()).isNotNull();

        var found = companyRepository.findById(company.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Company");

        companyRepository.delete(company);
        userRepository.delete(user);
        assertThat(companyRepository.findById(company.getId())).isEmpty();
    }
}
