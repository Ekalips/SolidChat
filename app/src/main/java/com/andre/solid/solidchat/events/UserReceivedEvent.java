package com.andre.solid.solidchat.events;

import com.andre.solid.solidchat.data.PartnerUserData;
import com.andre.solid.solidchat.user.User;

/**
 * Created by lantain on 09.04.17.
 */

public class UserReceivedEvent {
    String mac;

    public UserReceivedEvent(String mac) {
        this.mac = mac;
    }

    public String getMac() {
        return mac;
    }
}
