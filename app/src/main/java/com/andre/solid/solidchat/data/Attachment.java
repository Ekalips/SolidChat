package com.andre.solid.solidchat.data;

/**
 * Created by lantain on 09.04.17.
 */

public class Attachment {
    private String attachmentName;
    private long date;
    private String mac;


    public Attachment(String attachmentName, long date, String mac) {
        this.attachmentName = attachmentName;
        this.date = date;
        this.mac = mac;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public long getDate() {
        return date;
    }

    public String getMac() {
        return mac;
    }
}
