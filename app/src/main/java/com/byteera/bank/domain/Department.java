package com.byteera.bank.domain;

public class Department {

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    private String departmentName;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    private String orderId;

    public String getDepartmentFullPath() {
        return departmentFullPath;
    }

    public void setDepartmentFullPath(String departmentFullPath) {
        this.departmentFullPath = departmentFullPath;
    }

    private String departmentFullPath;
}
