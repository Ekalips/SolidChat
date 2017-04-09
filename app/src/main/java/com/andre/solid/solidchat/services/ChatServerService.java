package com.andre.solid.solidchat.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.andre.solid.solidchat.R;
import com.andre.solid.solidchat.data.Attachment;
import com.andre.solid.solidchat.data.EventData;
import com.andre.solid.solidchat.data.EventType;
import com.andre.solid.solidchat.data.Message;
import com.andre.solid.solidchat.data.PartnerUserData;
import com.andre.solid.solidchat.data.QuickQuestion;
import com.andre.solid.solidchat.events.ConnectionClosedEvent;
import com.andre.solid.solidchat.events.MessageReceivedEvent;
import com.andre.solid.solidchat.events.SendAttachmentEvent;
import com.andre.solid.solidchat.events.SendMessageEvent;
import com.andre.solid.solidchat.events.StopServiceEvent;
import com.andre.solid.solidchat.events.UserReceivedEvent;
import com.andre.solid.solidchat.stuff.Const;
import com.andre.solid.solidchat.user.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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
import io.realm.RealmList;

import static com.andre.solid.solidchat.main.ChatActivity.EXTRA_OWNER_ID;

public class ChatServerService extends Service {
    private static final String TAG = ChatServerService.class.getSimpleName();
    private Selector selector;
    private Map<SocketChannel, List> dataMapper;
    private InetSocketAddress listenAddress;


    InetAddress ownerAddress;
    boolean stopService = false;
    boolean nextIsAttachment = false;
    private Attachment lastAttachment;

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
                } else if (key.isReadable()) {
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

        User user = User.getInstance();
        Realm realm = Realm.getDefaultInstance();
        user.getQuickQuestions().clear();
        for (QuickQuestion q :
                realm.where(QuickQuestion.class).equalTo("author", User.getInstance().getMac()).findAll()) {
            user.getQuickQuestions().add(new QuickQuestion(q));
        }
        Log.e(TAG, "startServer: " + user.getQuickQuestions().size());

        String userJson = new GsonBuilder().create().toJson(new EventData(user));
        channel.write(ByteBuffer.wrap(userJson.getBytes()));

        // register channel with selector for further IO
        dataMapper.put(channel, new ArrayList());
        channel.register(this.selector, SelectionKey.OP_READ);
    }

    //read from the socket channel
    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        if (nextIsAttachment && lastAttachment != null) {
            handleReceivedAttachment(channel);
            return;
        }
        nextIsAttachment = false;
        lastAttachment = null;

        ByteBuffer buffer = ByteBuffer.allocate(2048);
        int numRead = -1;
        numRead = channel.read(buffer);

        if (numRead == -1) {
            this.dataMapper.remove(channel);
            Socket socket = channel.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            Log.e(TAG, "Connection closed by client: " + remoteAddr);
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

    private void handleReceivedAttachment(SocketChannel channel) throws IOException {
        File file = new File(Const.DEFAULT_STORAGE_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(file, lastAttachment.getAttachmentName());
        ByteBuffer bb = ByteBuffer.allocate(10000000);
        int bytesRead = channel.read(bb);
        FileOutputStream bout = new FileOutputStream(file);
        FileChannel sbc = bout.getChannel();

        while (bytesRead != -1) {
            bb.flip();
            sbc.write(bb);
            bb.compact();
            bytesRead = channel.read(bb);
        }

        Realm realm = Realm.getDefaultInstance();
        Message mess = realm.where(Message.class).equalTo("date", lastAttachment.getDate()).findFirst();
        realm.beginTransaction();
        mess.setFilePath(file.getPath());
        realm.commitTransaction();
        realm.close();
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
                RealmList<QuickQuestion> qqs = new RealmList<>();
                for (QuickQuestion q :
                        eventData.getUserData().getQuickQuestions()) {
                    if (q.getAuthor().equals(partnerUserData.getAddress()) && !qqs.contains(q))
                        qqs.add(q);
                }
                partnerUserData.setQuickQuestions(qqs);
                realm.commitTransaction();
                break;
            }
            case EventType.TYPE_MESSAGE: {
                Message message = eventData.getMessage();
                message.setIsMine(false);
                realm.beginTransaction();
                realm.insertOrUpdate(message);
                realm.commitTransaction();
                EventBus.getDefault().post(new MessageReceivedEvent(message.getMessage()));

                break;
            }
            case EventType.TYPE_DESTROY: {
                onCloseServiceEvent(null);
                break;
            }
            case EventType.TYPE_ATTACHMENT: {
                nextIsAttachment = true;
                lastAttachment = eventData.getAttachment();
                Message message = new Message();
                message.setDate(eventData.getAttachment().getDate());
                message.setMessage(getString(R.string.attachment_text, eventData.getAttachment().getAttachmentName()));
                message.setMac(eventData.getAttachment().getMac());
                realm.beginTransaction();
                realm.insertOrUpdate(message);
                realm.commitTransaction();
                break;
            }
        }
        realm.close();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onSendMessageEvent(SendMessageEvent event) {
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
    public void onCloseServiceEvent(StopServiceEvent event) {
        stopService = true;

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

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onSendAttachmentEvent(SendAttachmentEvent event) {
        try {
            ByteBuffer nameBuffer = ByteBuffer.wrap(new Gson().toJson(new EventData(new Attachment(event.getFile().getName(), event.getMessage().getDate(), event.getMessage().getMac()))).getBytes());
            SocketChannel client = null;
            for (SocketChannel ch :
                    dataMapper.keySet()) {
                client = ch;
            }
            if (client == null)
                return;
            client.write(nameBuffer);
            nameBuffer.flip();

            Thread.sleep(1000);

            FileInputStream fout = new FileInputStream(event.getFile());
            FileChannel sbc = fout.getChannel();


            ByteBuffer buff = ByteBuffer.allocate((int) event.getFile().length());
            int bytesread = sbc.read(buff);

            while (bytesread != -1) {
                buff.flip();
                client.write(buff);
                buff.compact();
                bytesread = sbc.read(buff);
            }


        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
