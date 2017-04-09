package com.andre.solid.solidchat.events;

import android.net.wifi.p2p.WifiP2pInfo;

/**
 * Created by lantain on 08.04.17.
 */

public class P2PWifiConnectionEstabilishedEvent {
    private WifiP2pInfo info;

    public P2PWifiConnectionEstabilishedEvent(WifiP2pInfo info) {
        this.info = info;
    }

    public WifiP2pInfo getInfo() {
        return info;
    }
}
