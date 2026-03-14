package ru.dashboardbattle.mapper;

import ru.dashboardbattle.dto.TopNEntryDto;
import ru.dashboardbattle.dto.TopNReportDto;
import ru.dashboardbattle.entity.Company;
import ru.dashboardbattle.entity.TopNEntry;
import ru.dashboardbattle.entity.TopNReport;

import java.util.List;
import java.util.stream.Collectors;


// маппер Entity <-> DTO для TopN
public final class TopNMapper {

    private TopNMapper() {
    }

    // --- TopNReport ---

    public static TopNReportDto toDto(TopNReport entity) {
        if (entity == null) return null;

        TopNReportDto dto = new TopNReportDto();
        dto.setId(entity.getId());
        dto.setCompanyId(entity.getCompany() != null ? entity.getCompany().getId() : null);
        dto.setPeriodStart(entity.getPeriodStart());
        dto.setPeriodEnd(entity.getPeriodEnd());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setPublishedAt(entity.getPublishedAt());

        if (entity.getEntries() != null) {
            dto.setEntries(entity.getEntries().stream()
                    .map(TopNMapper::toDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public static TopNReport toEntity(TopNReportDto dto, Company company) {
        if (dto == null) return null;

        TopNReport entity = new TopNReport();
        entity.setCompany(company);
        entity.setPeriodStart(dto.getPeriodStart());
        entity.setPeriodEnd(dto.getPeriodEnd());
        entity.setStatus(dto.getStatus());

        if (dto.getEntries() != null) {
            List<TopNEntry> entries = dto.getEntries().stream()
                    .map(entryDto -> toEntity(entryDto, entity))
                    .collect(Collectors.toList());
            entity.setEntries(entries);
        }
        return entity;
    }

    // --- TopNEntry ---

    public static TopNEntryDto toDto(TopNEntry entity) {
        if (entity == null) return null;

        TopNEntryDto dto = new TopNEntryDto();
        dto.setId(entity.getId());
        dto.setEmployeeName(entity.getEmployeeName());
        dto.setEmployeeIdMs(entity.getEmployeeIdMs());
        dto.setRevenue(entity.getRevenue());
        dto.setMargin(entity.getMargin());
        dto.setFavoriteProduct(entity.getFavoriteProduct());
        dto.setRank(entity.getRank());
        return dto;
    }

    public static TopNEntry toEntity(TopNEntryDto dto, TopNReport report) {
        if (dto == null) return null;

        TopNEntry entity = new TopNEntry();
        entity.setReport(report);
        entity.setEmployeeName(dto.getEmployeeName());
        entity.setEmployeeIdMs(dto.getEmployeeIdMs());
        entity.setRevenue(dto.getRevenue());
        entity.setMargin(dto.getMargin());
        entity.setFavoriteProduct(dto.getFavoriteProduct());
        entity.setRank(dto.getRank());
        return entity;
    }
}
