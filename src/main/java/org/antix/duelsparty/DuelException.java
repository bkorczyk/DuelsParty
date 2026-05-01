package org.antix.duelsparty;

public class DuelException extends RuntimeException {
    private final String messageKey;

    public DuelException(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}