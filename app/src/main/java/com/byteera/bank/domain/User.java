package com.byteera.bank.domain;

import com.easemob.chat.EMContact;
import com.google.gson.Gson;

import java.io.Serializable;

public class User extends EMContact implements Serializable {
    private int unreadMsgCount;
    private String header;
    private String userId;
    private String opnum;
    private String mobile;
    private String nickName;
    private String sex;
    private String avatar;
    private String email;
    private String easemobId;
    private String tel;
    private String depart;
    private String firstLetter;
    private String pinYin;

    public User() {
    }

    public User(String username) {
        this.username = username;
    }


    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public int getUnreadMsgCount() {
        return unreadMsgCount;
    }

    public void setUnreadMsgCount(int unreadMsgCount) {
        this.unreadMsgCount = unreadMsgCount;
    }

    @Override
    public int hashCode() {
        return 17 * getUsername().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof User)) {
            return false;
        }

        return getUserId().equals(((User) o).getUserId());
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOpnum() {
        return opnum;
    }

    public void setOpnum(String opnum) {
        this.opnum = opnum;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEasemobId() {
        return easemobId;
    }

    public void setEasemobId(String easemobId) {
        this.easemobId = easemobId;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getDepart() {
        return depart;
    }

    public void setDepart(String depart) {
        this.depart = depart;
    }


    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public static User loadFromJson(String json)
    {
        return new Gson().fromJson(json, User.class);
    }

    public User(String userId, String opnum, String mobile, String nickName, String sex,
                String avatar, String email, String easemobId, String tel, String depart,
                String firstLetter, String pinYin) {
        this.userId = userId;
        this.opnum = opnum;
        this.mobile = mobile;
        this.nickName = nickName;
        this.sex = sex;
        this.avatar = avatar;
        this.email = email;
        this.easemobId = easemobId;
        this.tel = tel;
        this.depart = depart;
        this.firstLetter = firstLetter;
        this.pinYin = pinYin;
    }

    public String getFirstLetter() {
        return firstLetter;
    }

    public void setFirstLetter(String firstLetter) {
        this.firstLetter = firstLetter;
    }

    public String getPinYin() {
        return pinYin;
    }

    public void setPinYin(String pinYin) {
        this.pinYin = pinYin;
    }
}
