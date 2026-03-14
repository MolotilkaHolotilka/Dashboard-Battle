package ru.dashboardbattle.dto;

public class PublicationRequestDto {

    private Long topNReportId;
    private Long destinationId;

    public PublicationRequestDto() {
    }

    public PublicationRequestDto(Long topNReportId, Long destinationId) {
        this.topNReportId = topNReportId;
        this.destinationId = destinationId;
    }

    public Long getTopNReportId() {
        return topNReportId;
    }

    public void setTopNReportId(Long topNReportId) {
        this.topNReportId = topNReportId;
    }

    public Long getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(Long destinationId) {
        this.destinationId = destinationId;
    }
}
