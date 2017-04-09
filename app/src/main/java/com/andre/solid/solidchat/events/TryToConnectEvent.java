package com.andre.solid.solidchat.events;

import com.andre.solid.solidchat.data.PartnerUserData;

/**
 * Created by lantain on 08.04.17.
 */

public class TryToConnectEvent {
    private PartnerUserData data;

    public TryToConnectEvent(PartnerUserData data) {
        this.data = data;
    }

    public PartnerUserData getData() {
        return data;
    }
}
