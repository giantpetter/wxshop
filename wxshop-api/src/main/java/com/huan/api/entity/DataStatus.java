package com.huan.api.entity;

public enum DataStatus {
    DELETE_STATUS("deleted"),
    OK("ok"),
//    only for order
    PENDING("pending"),
    PAID("paid"),
    DELIVERED("delivered"),
    RECEIVED("received");



    private final String status;

    DataStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
