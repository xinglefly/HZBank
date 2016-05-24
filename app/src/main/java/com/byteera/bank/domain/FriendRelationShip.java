package com.byteera.bank.domain;

import java.io.Serializable;

public class FriendRelationShip implements Serializable{
    private String srcUser;
    private int mode;
    private int result;

    public String getSrcUser() {
        return srcUser;
    }

    public void setSrcUser(String srcuser) {
        this.srcUser = srcuser;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "FriendRelationShip{" +
                "srcUser='" + srcUser + '\'' +
                ", mode=" + mode +
                ", result=" + result +
                '}';
    }

    public FriendRelationShip(String srcUser, int mode, int result) {
        this.srcUser = srcUser;
        this.mode = mode;
        this.result = result;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
}

