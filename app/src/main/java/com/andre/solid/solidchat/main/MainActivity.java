package com.andre.solid.solidchat.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.andre.solid.solidchat.R;
import com.andre.solid.solidchat.data.PartnerUserData;
import com.andre.solid.solidchat.events.FetchOfflineChatEvent;
import com.andre.solid.solidchat.events.P2PWifiConnectionEstabilishedEvent;
import com.andre.solid.solidchat.events.TryToConnectEvent;
import com.andre.solid.solidchat.receivers.WiFiBroadcastReceiver;
import com.andre.solid.solidchat.databinding.ActivityMainBinding;
import com.andre.solid.solidchat.user.User;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.InetAddress;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_ENTER_CHAT = 0;
    private final IntentFilter intentFilter = new IntentFilter();
    WifiP2pManager manager;
    WifiP2pManager.Channel mChannel;

    ActivityMainBinding binding;
    Realm realm;
    RealmResults<PartnerUserData> partnerUserDatas;
    String lastTriedMac;
    boolean connectionRequested = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate: ");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        realm = Realm.getDefaultInstance();

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = manager.initialize(this, getMainLooper(), null);
        receiver = new WiFiBroadcastReceiver(manager, mChannel, this);

        partnerUserDatas = realm.where(PartnerUserData.class).findAllAsync();
        partnerUserDatas.addChangeListener(partnersChangeListener);

        if (partnerUserDatas.isLoaded() && partnerUserDatas.isValid())
            binding.setData(partnerUserDatas);

        binding.setUser(User.getInstance());
    }

    BroadcastReceiver receiver;

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
        EventBus.getDefault().register(this);
        launchPeerDiscovering();
    }

    private void launchPeerDiscovering() {
        manager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "onSuccess: ");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "onFailure: ");
            }
        });
    }


    RealmChangeListener<RealmResults<PartnerUserData>> partnersChangeListener = new RealmChangeListener<RealmResults<PartnerUserData>>() {
        @Override
        public void onChange(RealmResults<PartnerUserData> element) {
            binding.setData(element);
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTryToConnectEvent(TryToConnectEvent event) {
        Log.e(TAG, "onTryToConnectEvent: ");
        if (event.getData().getConnectionStatus() == PartnerUserData.ConnectionStatus.available.toInt()) {
            lastTriedMac = event.getData().getAddress();

            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = event.getData().getAddress();
            config.wps.setup = WpsInfo.PBC;
            connectionRequested = true;
            manager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(int reason) {
                    Toast.makeText(MainActivity.this, "Connect failed. Retry.",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            lastTriedMac = null;
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = event.getData().getAddress();
            config.wps.setup = WpsInfo.PBC;
            connectionRequested = false;
            clearGroups();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        partnerUserDatas.removeChangeListener(partnersChangeListener);
        realm.close();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onP2PConnectionEstabilished(P2PWifiConnectionEstabilishedEvent event) {
        if (event.getInfo().groupFormed) {
//            if (lastTriedMac != null && connectionRequested)
            startChatActivity(event.getInfo().isGroupOwner, event.getInfo().groupOwnerAddress);
        } else
            lastTriedMac = null;
    }


    public void startChatActivity(boolean isOwner, InetAddress ownerId) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_IS_OWNER, isOwner);
        intent.putExtra(ChatActivity.EXTRA_OWNER_ID, ownerId);
        startActivityForResult(intent, REQUEST_ENTER_CHAT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENTER_CHAT) {
            lastTriedMac = null;
            connectionRequested = false;
            clearGroups();
        }
    }

    private void clearGroups() {
        manager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                launchPeerDiscovering();
            }

            @Override
            public void onFailure(int reason) {
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFetchOfflineChatEvent(FetchOfflineChatEvent event) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_MAC, event.getData().getAddress());
        startActivityForResult(intent, REQUEST_ENTER_CHAT);
    }
}
