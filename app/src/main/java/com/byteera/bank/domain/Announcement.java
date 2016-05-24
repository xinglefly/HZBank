package com.byteera.bank.domain;

import java.io.Serializable;

public class Announcement implements Serializable {

    private String title;
    private long time;
    private String content;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Message{" +
                "title='" + title + '\'' +
                ", time=" + time +
                ", content='" + content + '\'' +
                '}';
    }


    public Announcement(String title, long time, String content) {
        this.title = title;
        this.time = time;
        this.content = content;
    }
}
