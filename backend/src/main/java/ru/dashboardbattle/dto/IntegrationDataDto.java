package ru.dashboardbattle.dto;

import java.util.List;

public class IntegrationDataDto {

    private Long companyId;
    private List<MoySkladInfoDto> moySkladIntegrations;
    private List<TelegramInfoDto> telegramIntegrations;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public List<MoySkladInfoDto> getMoySkladIntegrations() {
        return moySkladIntegrations;
    }

    public void setMoySkladIntegrations(List<MoySkladInfoDto> moySkladIntegrations) {
        this.moySkladIntegrations = moySkladIntegrations;
    }

    public List<TelegramInfoDto> getTelegramIntegrations() {
        return telegramIntegrations;
    }

    public void setTelegramIntegrations(List<TelegramInfoDto> telegramIntegrations) {
        this.telegramIntegrations = telegramIntegrations;
    }

    public static class MoySkladInfoDto {
        private Long id;
        private String status;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class TelegramInfoDto {
        private Long id;
        private String channelChatId;
        private String status;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getChannelChatId() { return channelChatId; }
        public void setChannelChatId(String channelChatId) { this.channelChatId = channelChatId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
