package com.dog.mobprocessor.item;

public enum UpgradeType {
    LOOTING("Looting"),
    SPEED("Speed"),
    STORAGE("Storage"),
    PLAYER_KILL("Player Kill");

    private final String displayName;

    UpgradeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
