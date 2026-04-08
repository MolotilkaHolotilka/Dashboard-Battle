package ru.dashboardbattle.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class TopNReportDto {

    private Long id;
    private Long companyId;
    private String companyName;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String status;
    private Instant createdAt;
    private Instant publishedAt;
    private List<TopNEntryDto> entries;

    public TopNReportDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
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

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public List<TopNEntryDto> getEntries() {
        return entries;
    }

    public void setEntries(List<TopNEntryDto> entries) {
        this.entries = entries;
    }
}
