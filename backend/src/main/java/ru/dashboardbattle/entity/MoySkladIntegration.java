package ru.dashboardbattle.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "moysklad_integrations")
public class MoySkladIntegration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "token_encrypted", nullable = false, columnDefinition = "TEXT")
    private String tokenEncrypted;

    @Column(length = 50)
    private String status = "ACTIVE";

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getTokenEncrypted() {
        return tokenEncrypted;
    }

    public void setTokenEncrypted(String tokenEncrypted) {
        this.tokenEncrypted = tokenEncrypted;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
