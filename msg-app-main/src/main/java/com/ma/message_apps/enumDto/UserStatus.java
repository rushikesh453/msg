package com.ma.message_apps.enumDto;

public enum UserStatus {
    ONLINE("Online"),
    OFFLINE("Offline"),
    AWAY("Away");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getStatus() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
