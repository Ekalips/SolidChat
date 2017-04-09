package com.andre.solid.solidchat.data;

import com.andre.solid.solidchat.user.User;

/**
 * Created by lantain on 08.04.17.
 */

public class EventData {


    @EventType.EventTypeDef
    private int eventType;
    private User userData;
    private Message message;
    private Attachment attachment;

    public EventData(Message message) {
        this.message = message;
        eventType = EventType.TYPE_MESSAGE;
    }

    public EventData(User userData) {
        this.userData = userData;
        eventType = EventType.TYPE_USER_DATA;
    }

    public EventData() {
    }

    public static EventData getDestroyEvent(){
        EventData data = new EventData();
        data.eventType = EventType.TYPE_DESTROY;
        return data;
    }

    public EventData(Attachment attachment) {
        this.attachment = attachment;
        this.eventType = EventType.TYPE_ATTACHMENT;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public User getUserData() {
        return userData;
    }

    public void setUserData(User userData) {
        this.userData = userData;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Attachment getAttachment() {
        return attachment;
    }
}
