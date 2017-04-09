package com.andre.solid.solidchat.events;

/**
 * Created by lantain on 09.04.17.
 */

public class MessageReceivedEvent {
    private String message;
    public MessageReceivedEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
