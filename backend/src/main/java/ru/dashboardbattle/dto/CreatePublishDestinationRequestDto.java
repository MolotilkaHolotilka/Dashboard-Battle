package ru.dashboardbattle.dto;

public class CreatePublishDestinationRequestDto {

    private Long companyId;
    /** TELEGRAM, DEMO_PAGE, WEBHOOK */
    private String channelCode;
    /** Понятное имя места публикации в интерфейсе */
    private String label;
    /**
     * Для Telegram — chat id (если не задан, используется из интеграции).
     * Для Webhook — полный URL.
     * Для Демо-страницы можно оставить пустым.
     */
    private String externalIdentifier;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getExternalIdentifier() {
        return externalIdentifier;
    }

    public void setExternalIdentifier(String externalIdentifier) {
        this.externalIdentifier = externalIdentifier;
    }
}
