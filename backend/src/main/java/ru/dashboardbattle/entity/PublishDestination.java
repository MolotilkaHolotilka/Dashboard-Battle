package ru.dashboardbattle.entity;

import jakarta.persistence.*;
import java.time.Instant;

// куда конкретно публикуем (чат, группа и т.д.)
@Entity
@Table(name = "publish_destinations")
public class PublishDestination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private PublishChannel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "external_identifier", length = 255)
    private String externalIdentifier;

    @Column(length = 255)
    private String label;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PublishChannel getChannel() { return channel; }
    public void setChannel(PublishChannel channel) { this.channel = channel; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public String getExternalIdentifier() { return externalIdentifier; }
    public void setExternalIdentifier(String externalIdentifier) { this.externalIdentifier = externalIdentifier; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
