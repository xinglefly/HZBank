package com.byteera.bank.domain;

import java.io.Serializable;

public class VisitorContent implements Serializable {

    private static final long serialVersionUID = 596595603634690611L;

    private int id;

    private String content;
    private int index;
    private String create_time;
    private String user_id;
    private String name;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "VisitorContent [id=" + id + ", content=" + content + ", index="
                + index + ", create_time=" + create_time + ", user_id="
                + user_id + ", name=" + name + "]";
    }

    public VisitorContent(String content, int index, String create_time,
            String user_id, String name) {
        super();
        this.content = content;
        this.index = index;
        this.create_time = create_time;
        this.user_id = user_id;
        this.name = name;
    }

    public VisitorContent() {
    }
}
