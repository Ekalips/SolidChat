package com.andre.solid.solidchat.events;

import com.andre.solid.solidchat.data.PartnerUserData;
import com.andre.solid.solidchat.stuff.ClickAdapter;

/**
 * Created by lantain on 09.04.17.
 */

public class FetchOfflineChatEvent  {
    PartnerUserData data;
    public FetchOfflineChatEvent(PartnerUserData partnerUserData) {
        this.data = partnerUserData;
    }

    public PartnerUserData getData() {
        return data;
    }
}
