package com.andre.solid.solidchat.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.andre.solid.solidchat.data.EventData;
import com.andre.solid.solidchat.data.EventType;
import com.andre.solid.solidchat.data.Message;
import com.andre.solid.solidchat.data.PartnerUserData;
import com.andre.solid.solidchat.events.ConnectionClosedEvent;
import com.andre.solid.solidchat.events.SendMessageEvent;
import com.andre.solid.solidchat.events.StopServiceEvent;
import com.andre.solid.solidchat.events.UserReceivedEvent;
import com.andre.solid.solidchat.user.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.realm.Realm;

import static com.andre.solid.solidchat.main.ChatActivity.EXTRA_OWNER_ID;

public class ChatServerService extends Service {
    private static final String TAG = ChatServerService.class.getSimpleName();
    private Selector selector;
    private Map<SocketChannel,List> dataMapper;
    private InetSocketAddress listenAddress;



    InetAddress ownerAddress;
    boolean stopService = false;
    private ServerSocketChannel serverChannel;

    public ChatServerService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        ownerAddress = (InetAddress) intent.getSerializableExtra(EXTRA_OWNER_ID);


        listenAddress = new InetSocketAddress(ownerAddress, 8000);
        dataMapper = new HashMap<>();

        new Thread(server).start();

        EventBus.getDefault().register(this);

        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.e(TAG, "onDestroy: ");
        EventBus.getDefault().unregister(this);

        try {
            serverChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    Runnable server = new Runnable() {
        @Override
        public void run() {
            try {
                startServer();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };

    private void startServer() throws IOException {
        this.selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // retrieve server socket and bind to port
        serverChannel.socket().bind(listenAddress);
        serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);

        while (!stopService) {
            // wait for events
            this.selector.select();

            //work on selected keys
            Iterator keys = this.selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = (SelectionKey) keys.next();

                // this is necessary to prevent the same key from coming up
                // again the next time around.
                keys.remove();

                if (!key.isValid()) {
                    continue;
                }

                if (key.isAcceptable()) {
                    this.accept(key);
                }
                else if (key.isReadable()) {
                    this.read(key);
                }
            }
        }
    }

    //accept a connection made to this channel's socket
    private void accept(SelectionKey key) throws IOException {
        serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        Socket socket = channel.socket();
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();
        System.out.println("Connected to: " + remoteAddr);

        String user = new GsonBuilder().create().toJson(new EventData(User.getInstance()));
        channel.write(ByteBuffer.wrap(user.getBytes()));

        // register channel with selector for further IO
        dataMapper.put(channel, new ArrayList());
        channel.register(this.selector, SelectionKey.OP_READ);
    }

    //read from the socket channel
    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int numRead = -1;
        numRead = channel.read(buffer);

        if (numRead == -1) {
            this.dataMapper.remove(channel);
            Socket socket = channel.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            Log.e(TAG,"Connection closed by client: " + remoteAddr);
            channel.close();
            key.cancel();
            EventBus.getDefault().post(new ConnectionClosedEvent());
            return;
        }

        byte[] data = new byte[numRead];
        System.arraycopy(buffer.array(), 0, data, 0, numRead);
        System.out.println("Got: " + new String(data));

        handleReceivedMessage(new String(data));

    }

    private void handleReceivedMessage(String data) {
        final EventData eventData = new GsonBuilder().create().fromJson(data, EventData.class);
        final Realm realm = Realm.getDefaultInstance();
        switch (eventData.getEventType()) {
            case EventType.TYPE_USER_DATA: {
                EventBus.getDefault().post(new UserReceivedEvent(eventData.getUserData().getMac()));
                realm.beginTransaction();
                PartnerUserData partnerUserData = realm.where(PartnerUserData.class).equalTo("address", eventData.getUserData().getMac()).findFirst();
                partnerUserData.setName(eventData.getUserData().getName());
                partnerUserData.setImage(eventData.getUserData().getImage());
                realm.commitTransaction();
                break;
            }
            case EventType.TYPE_MESSAGE: {
                Message message = eventData.getMessage();
                message.setIsMine(false);
                realm.beginTransaction();
                realm.insertOrUpdate(message);
                realm.commitTransaction();
                break;
            }
            case EventType.TYPE_DESTROY:{
                onCloseServiceEvent(null);
                break;
            }
        }
        realm.close();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onSendMessageEvent(SendMessageEvent event){
        try {
            for (SocketChannel clientChanngels :
                    dataMapper.keySet()) {
                clientChanngels.write(ByteBuffer.wrap(new Gson().toJson(new EventData(event.getMessage())).getBytes()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onCloseServiceEvent(StopServiceEvent event){
        stopService=true;

        Log.e(TAG, "onCloseServiceEvent: ");
        try {
            String eventJson = new Gson().toJson(EventData.getDestroyEvent());
            for (SocketChannel clientChanngels :
                    dataMapper.keySet()) {
                clientChanngels.write(ByteBuffer.wrap(eventJson.getBytes()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        stopSelf();
    }
}
