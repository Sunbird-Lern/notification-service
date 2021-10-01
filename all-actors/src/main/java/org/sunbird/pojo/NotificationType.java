package org.sunbird.pojo;

public enum NotificationType {
    EMAIL("email"),
    PHONE("phone"),
    FEED("feed"),
    DEVICE("device");

    private String value;

    /**
     * constructor
     *
     * @param value String
     */
    NotificationType(String value) {
        this.value = value;
    }

    /**
     * returns the enum value
     *
     * @return String
     */
    public String getValue() {
        return this.value;
    }
}
