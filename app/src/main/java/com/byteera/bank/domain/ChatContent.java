package com.byteera.bank.domain;


import java.io.Serializable;
import java.util.List;

public class ChatContent implements Serializable {

    private static final long serialVersionUID = -6578508030928664092L;

    private int id;

    private String name;
    private String head_photo;
    private String content;
    private String publisher;
    private String publish_time;
    private String chat_id;
    private List<VisitorContent> listVisitorContents;
    private List<String> imgLists;

    public List<VisitorContent> getListVisitorContents() {
        return listVisitorContents;
    }

    public void setListVisitorContents(List<VisitorContent> listVisitorContents) {
        this.listVisitorContents = listVisitorContents;
    }

    public String getHead_photo() {
        return head_photo;
    }

    public void setHead_photo(String head_photo) {
        this.head_photo = head_photo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublish_time() {
        return publish_time;
    }

    public void setPublish_time(String publish_time) {
        this.publish_time = publish_time;
    }

    public String getChat_id() {
        return chat_id;
    }

    public void setChat_id(String chat_id) {
        this.chat_id = chat_id;
    }

    public List<String> getImgLists() {
        return imgLists;
    }

    public void setImgLists(List<String> imgLists) {
        this.imgLists = imgLists;
    }

    @Override
    public String toString() {
        return "ChatContent [content=" + content + ", publisher=" + publisher
                + ", publish_time=" + publish_time + ", chat_id=" + chat_id
                + "]";
    }

    public ChatContent(String name,String head_photo, String content, String publisher, String publish_time,
            String chat_id,List<VisitorContent> listVisitorContents,List<String> imgLists) {
        super();
        this.name = name;
        this.head_photo = head_photo;
        this.content = content;
        this.publisher = publisher;
        this.publish_time = publish_time;
        this.chat_id = chat_id;
        this.listVisitorContents = listVisitorContents;
        this.imgLists = imgLists;
    }

}
