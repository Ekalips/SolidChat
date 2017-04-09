package com.andre.solid.solidchat.main;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewTreeObserver;

import com.andre.solid.solidchat.R;
import com.andre.solid.solidchat.adapters.MessagesRecyclerViewAdapter;
import com.andre.solid.solidchat.data.Message;
import com.andre.solid.solidchat.data.PartnerUserData;
import com.andre.solid.solidchat.databinding.ActivityChatBinding;
import com.andre.solid.solidchat.events.ConnectionClosedEvent;
import com.andre.solid.solidchat.events.SendMessageEvent;
import com.andre.solid.solidchat.events.StopServiceEvent;
import com.andre.solid.solidchat.events.UserReceivedEvent;
import com.andre.solid.solidchat.services.ChatClientService;
import com.andre.solid.solidchat.services.ChatServerService;
import com.andre.solid.solidchat.stuff.Utils;
import com.andre.solid.solidchat.user.User;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.InetAddress;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_OWNER_ID = "owner_id";
    public static final String EXTRA_IS_OWNER = "is_owner";
    public static final String EXTRA_MAC = "mac";

    InetAddress ownerAddress;
    boolean isOwner;
    private String TAG = ChatActivity.class.getSimpleName();
    ActivityChatBinding binding;

    Intent serviceIntent;
    Realm realm;
    boolean partnerFetched;

    RealmResults<Message> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);
        realm = Realm.getDefaultInstance();

        isOwner = getIntent().getBooleanExtra(EXTRA_IS_OWNER, false);
        ownerAddress = (InetAddress) getIntent().getSerializableExtra(EXTRA_OWNER_ID);

        if (ownerAddress == null) {
            Utils.showToastMessage(R.string.error_init_chat);
            finish();
            return;
        }

        if (isOwner) {
            serviceIntent = new Intent(this, ChatServerService.class);
            serviceIntent.putExtra(ChatActivity.EXTRA_OWNER_ID, ownerAddress);
            startService(serviceIntent);
        } else {
            serviceIntent = new Intent(this, ChatClientService.class);
            serviceIntent.putExtra(ChatActivity.EXTRA_OWNER_ID, ownerAddress);
            startService(serviceIntent);
        }

        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryToSendMessage();
            }
        });


        binding.messageInputLayout.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        binding.recyclerView.setLayoutManager(manager);

    }

    private void tryToSendMessage() {
        if (binding.messageEt.getText().toString().trim().length() > 0) {
            if (partnerFetched) {
                Message messageToSend = new Message(User.getInstance().getMac(), System.currentTimeMillis(), binding.messageEt.getText().toString());
                Message messageToSave = new Message(currentPartner.getAddress(), System.currentTimeMillis(), binding.messageEt.getText().toString());
                EventBus.getDefault().post(new SendMessageEvent(messageToSend));
                addMessageToRealm(messageToSave);
                binding.messageEt.setText("");
            } else {
                Utils.showToastMessage(R.string.error_no_partner);
            }
        } else {
            Utils.showToastMessage(R.string.error_empty_message);
        }
    }

    ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            binding.recyclerView.setPadding(0, 0, 0, binding.messageInputLayout.getHeight());
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().post(new StopServiceEvent());
        binding.messageInputLayout.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectionClosedEvent(ConnectionClosedEvent event) {
        finish();
    }


    PartnerUserData currentPartner;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserReceivedEvent(UserReceivedEvent event) {
        currentPartner = realm.where(PartnerUserData.class).equalTo("address", event.getMac()).findFirstAsync();
        currentPartner.addChangeListener(partnerUserDataRealmChangeListener);

        if (messages != null)
            messages.removeAllChangeListeners();

        messages = realm.where(Message.class).equalTo("mac", event.getMac()).findAllSortedAsync("date", Sort.DESCENDING);
        messages.addChangeListener(realmResultsRealmChangeListener);

        if (currentPartner.isLoaded()) {
            partnerFetched = true;
        }
    }

    RealmChangeListener<PartnerUserData> partnerUserDataRealmChangeListener = new RealmChangeListener<PartnerUserData>() {
        @Override
        public void onChange(PartnerUserData element) {
            partnerFetched = true;
            binding.setPartner(element);

        }
    };

    private void addMessageToRealm(final Message message) {
        message.setIsMine(true);
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(message);
            }
        });
    }


    RealmChangeListener<RealmResults<Message>> realmResultsRealmChangeListener = new RealmChangeListener<RealmResults<Message>>() {
        @Override
        public void onChange(RealmResults<Message> element) {
            binding.setData(element);
        }
    };

}
