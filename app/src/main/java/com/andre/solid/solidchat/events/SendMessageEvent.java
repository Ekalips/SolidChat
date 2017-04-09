package com.andre.solid.solidchat.events;

import com.andre.solid.solidchat.data.Message;

/**
 * Created by lantain on 08.04.17.
 */

public class SendMessageEvent {
    Message message;

    public SendMessageEvent(Message message){
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
