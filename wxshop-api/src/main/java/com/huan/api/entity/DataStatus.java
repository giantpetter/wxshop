package com.huan.api.entity;

public enum DataStatus {
    DELETE_STATUS("deleted"),
    OK("ok"),
    //    only for order
    PENDING("pending"),
    PAID("paid"),
    DELIVERED("delivered"),
    RECEIVED("received");

    public static DataStatus fromStatus(String status) {
        try {
            return DataStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    private final String status;

    DataStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
