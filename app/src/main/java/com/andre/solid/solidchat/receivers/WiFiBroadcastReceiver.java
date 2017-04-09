package com.andre.solid.solidchat.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.text.TextUtils;
import android.util.Log;

import com.andre.solid.solidchat.data.PartnerUserData;
import com.andre.solid.solidchat.events.P2PWifiConnectionEstabilishedEvent;
import com.andre.solid.solidchat.main.MainActivity;
import com.andre.solid.solidchat.user.User;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class WiFiBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = WiFiBroadcastReceiver.class.getSimpleName();
    Realm realm;
    WifiP2pManager manager;
    WifiP2pManager.Channel channel;

    public WiFiBroadcastReceiver() {
    }

    public WiFiBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel mChannel, MainActivity mainActivity) {
        this.manager = manager;
        this.channel = mChannel;
        realm = Realm.getDefaultInstance();

        if (manager != null) {
            manager.requestPeers(channel, peerListListener);
        }
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.e(TAG, "onReceive: " + action);

        switch (action) {
            case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION: {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Log.e(TAG, "onReceive: wifiP2P enabled!");
                } else {
                    Log.e(TAG, "onReceive: wifiP2P disabled!");
                }
                break;
            }
            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION: {
                Log.e(TAG, "onReceive: The peer list has changed!  We should probably do something about\n" +
                        "            // that.");

                if (manager != null) {
                    manager.requestPeers(channel, peerListListener);
                }
                break;
            }
            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION: {
                Log.e(TAG, "onReceive:             // Connection state changed!  We should probably do something about\n" +
                        "            // that.");
                if (manager == null) {
                    return;
                }

                NetworkInfo networkInfo = intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if (networkInfo.isConnected()) {
                    // We are connected with the other device, request connection
                    // info to find group owner IP

                    manager.requestConnectionInfo(channel, connectionListener);
                }
                break;
            }
            case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION: {
                Log.e(TAG, "onReceive: wifi device attached action" + ((WifiP2pDevice) intent.getParcelableExtra(
                        WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)).deviceName);
                User.getInstance().setMac(((WifiP2pDevice) intent.getParcelableExtra(
                        WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)).deviceAddress);
                if (manager != null) {
                    manager.requestPeers(channel, peerListListener);
                }
                break;
            }
        }
    }


    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peers) {

            List<PartnerUserData> partnerUserDatas = new ArrayList<>();
            for (WifiP2pDevice device :
                    peers.getDeviceList()) {
                Log.e(TAG, "onPeersAvailable: " + device.deviceName);
                partnerUserDatas.add(new PartnerUserData(device.deviceAddress, device.deviceName, device.status));
            }
            writeDetectedDevicesToRealm(partnerUserDatas);
        }
    };


    public void writeDetectedDevicesToRealm(final List<PartnerUserData> datas) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<PartnerUserData> savedDatas = realm.where(PartnerUserData.class).findAll();
                for (PartnerUserData savedItem :
                        savedDatas) {
                    savedItem.setInNetwork(false);
                }
                for (PartnerUserData receivedItem :
                        datas) {
                    receivedItem.setInNetwork(true);
                    for (PartnerUserData savedItem :
                            savedDatas) {
                        if (TextUtils.equals(savedItem.getAddress(), receivedItem.getAddress())) {
                            receivedItem.setName(savedItem.getName());
                            receivedItem.setImage(savedItem.getImage());
                        }
                    }
                }
                realm.insertOrUpdate(datas);
            }
        });
    }

    private WifiP2pManager.ConnectionInfoListener connectionListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            Log.e(TAG, "onConnectionInfoAvailable: " + info.toString());
            EventBus.getDefault().post(new P2PWifiConnectionEstabilishedEvent(info));
        }
    };


}
