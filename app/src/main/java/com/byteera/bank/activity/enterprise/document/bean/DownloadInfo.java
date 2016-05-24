package com.byteera.bank.activity.enterprise.document.bean;



public class DownloadInfo {

    private int id;
    
    private String url;
    private String name;
    private String partment;
    private String size;
    
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPartment() {
        return partment;
    }
    public void setPartment(String partment) {
        this.partment = partment;
    }
    public String getSize() {
        return size;
    }
    public void setSize(String size) {
        this.size = size;
    }
    
    public DownloadInfo() {
        super();
    }
    
    public DownloadInfo(String name,String url) {
        super();
        this.url = url;
        this.name = name;
    }

    
    
   
}
