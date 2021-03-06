package com.andre.solid.solidchat.main;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;

import com.andre.solid.solidchat.R;
import com.andre.solid.solidchat.adapters.MessagesRecyclerViewAdapter;
import com.andre.solid.solidchat.data.Answer;
import com.andre.solid.solidchat.data.Attachment;
import com.andre.solid.solidchat.data.Message;
import com.andre.solid.solidchat.data.PartnerUserData;
import com.andre.solid.solidchat.data.QuickQuestion;
import com.andre.solid.solidchat.databinding.ActivityChatBinding;
import com.andre.solid.solidchat.events.ConnectionClosedEvent;
import com.andre.solid.solidchat.events.MessageReceivedEvent;
import com.andre.solid.solidchat.events.OnQuickAnswerClick;
import com.andre.solid.solidchat.events.SendAttachmentEvent;
import com.andre.solid.solidchat.events.SendMessageEvent;
import com.andre.solid.solidchat.events.StopServiceEvent;
import com.andre.solid.solidchat.events.UserReceivedEvent;
import com.andre.solid.solidchat.services.ChatClientService;
import com.andre.solid.solidchat.services.ChatServerService;
import com.andre.solid.solidchat.stuff.UriFileUtil;
import com.andre.solid.solidchat.stuff.Utils;
import com.andre.solid.solidchat.user.User;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_OWNER_ID = "owner_id";
    public static final String EXTRA_IS_OWNER = "is_owner";
    public static final String EXTRA_MAC = "mac";
    private static final int REQUEST_FILE = 0;

    InetAddress ownerAddress;
    boolean isOwner;
    private String TAG = ChatActivity.class.getSimpleName();
    ActivityChatBinding binding;

    String mac;
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
        mac = getIntent().getStringExtra(EXTRA_MAC);
        if (ownerAddress == null && mac == null) {
            Utils.showToastMessage(R.string.error_init_chat);
            finish();
            return;
        }

        if (mac != null && !mac.isEmpty()) {
            onUserReceivedEvent(new UserReceivedEvent(mac));
        }

        if (ownerAddress != null) {

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
                    tryToSendMessage(binding.messageEt.getText().toString());
                }
            });

            binding.attachBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.showFileChooser(ChatActivity.this, REQUEST_FILE);
                }
            });

        } else {
            binding.sendBtn.setEnabled(false);
            binding.attachBtn.setEnabled(false);
        }


        binding.messageInputLayout.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        binding.recyclerView.setLayoutManager(manager);

        setSupportActionBar(binding.includeToolbar.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    private boolean tryToSendMessage(String message) {
        if (message.trim().length() > 0) {
            if (partnerFetched) {
                Message messageToSend = new Message(User.getInstance().getMac(), System.currentTimeMillis(), message);
                Message messageToSave = new Message(currentPartner.getAddress(), System.currentTimeMillis(), message);
                EventBus.getDefault().post(new SendMessageEvent(messageToSend));
                addMessageToRealm(messageToSave);
                binding.messageEt.setText("");
                return true;
            } else {
                Utils.showToastMessage(R.string.error_no_partner);
                return false;
            }
        } else {
            Utils.showToastMessage(R.string.error_empty_message);
            return false;
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
            List<String> answer = new ArrayList<>();
            for (QuickQuestion q :
                    element.getQuickQuestions()) {
                answer.add(q.getQuestion());
            }
            binding.setQickAnswers(answer);
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_FILE) {
            boolean isSucceed = true;
            for (int res :
                    grantResults) {
                isSucceed = isSucceed && res == PackageManager.PERMISSION_GRANTED;
            }

            if (isSucceed)
                Utils.showFileChooser(this, REQUEST_FILE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_FILE) {
                Uri uri = data.getData();
                try {
                    String path = UriFileUtil.getFilePath(this, uri);
                    if (path != null) {
                        File file = new File(path);
                        Message messageToSend = new Message(User.getInstance().getMac(), System.currentTimeMillis(), file.getName());
                        Message messageToSave = new Message(currentPartner.getAddress(), System.currentTimeMillis(), file.getName());
                        messageToSave.setFilePath(file.getPath());
                        EventBus.getDefault().post(new SendAttachmentEvent(messageToSend, file));
                        addMessageToRealm(messageToSave);
                    } else {
                        Utils.showToastMessage(R.string.error_file_open);
                    }

                } catch (URISyntaxException | NullPointerException exception) {
                    Utils.showToastMessage(R.string.error_file_open);
                    exception.printStackTrace();
                }
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onQuickAnswerClick(OnQuickAnswerClick answerClick) {
        if (tryToSendMessage(answerClick.getAnswer())) {
            binding.setQickAnswers(new ArrayList());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageReceivedEvent(MessageReceivedEvent event) {
        for (QuickQuestion q :
                User.getInstance().getQuickQuestions()) {
            if (TextUtils.equals(q.getQuestion(), event.getMessage())) {
                List<String> answers = new ArrayList<>();
                for (Answer a :
                        q.getAnswers()) {
                    answers.add(a.getAnswer());
                }
                binding.setQickAnswers(answers);
                return;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
