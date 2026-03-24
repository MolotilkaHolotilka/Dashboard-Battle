package ru.dashboardbattle.exception;

public class UnsupportedChannelException extends RuntimeException {
    public UnsupportedChannelException(String channel) {
        super("Канал " + channel + " пока не поддерживается");
    }
}
