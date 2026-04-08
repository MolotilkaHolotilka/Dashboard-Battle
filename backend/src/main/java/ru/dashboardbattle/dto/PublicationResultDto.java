package ru.dashboardbattle.dto;

public class PublicationResultDto {

    private Long publicationId;
    private Long channelId;
    private Long destinationId;
    private String status;
    private String externalId;
    /** Относительный путь для просмотра демо-страницы, например /api/public/demo/view/{token} */
    private String viewerPath;
    private String channelCode;
    private String channelName;
    private String destinationLabel;

    public PublicationResultDto() {
    }

    public Long getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(Long publicationId) {
        this.publicationId = publicationId;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public Long getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(Long destinationId) {
        this.destinationId = destinationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getViewerPath() {
        return viewerPath;
    }

    public void setViewerPath(String viewerPath) {
        this.viewerPath = viewerPath;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getDestinationLabel() {
        return destinationLabel;
    }

    public void setDestinationLabel(String destinationLabel) {
        this.destinationLabel = destinationLabel;
    }
}
