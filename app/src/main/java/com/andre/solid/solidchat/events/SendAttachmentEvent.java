package com.andre.solid.solidchat.events;

import com.andre.solid.solidchat.data.Message;

import java.io.File;

/**
 * Created by lantain on 09.04.17.
 */

public class SendAttachmentEvent {
    Message message;
    File file;

    public SendAttachmentEvent(Message message, File file){
        this.message = message;
        this.file = file;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public File getFile() {
        return file;
    }
}
