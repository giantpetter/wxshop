package com.huan.wxshop.entity;

public enum DataStatus {
    DELETE_STATUS("deleted"),
    OK("ok");

    private final String status;

    DataStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
