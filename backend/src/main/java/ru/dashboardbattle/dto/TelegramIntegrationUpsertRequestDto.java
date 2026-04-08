package ru.dashboardbattle.dto;

public class TelegramIntegrationUpsertRequestDto {

    private Long companyId;
    private String botToken;
    private String channelChatId;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getBotToken() {
        return botToken;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public String getChannelChatId() {
        return channelChatId;
    }

    public void setChannelChatId(String channelChatId) {
        this.channelChatId = channelChatId;
    }
}
