package com.andre.solid.solidchat.data;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by lantain on 08.04.17.
 */

public class Message extends RealmObject{


    private String mac;
    @PrimaryKey
    private
    long date;
    private String message;
    private boolean isMine;


    public Message() {
    }

    public Message(String mac, long date, String message) {
        this.mac = mac;
        this.date = date;
        this.message = message;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setIsMine(boolean isMine) {
        this.isMine = isMine;
    }

    public boolean isMine() {
        return isMine;
    }
}
