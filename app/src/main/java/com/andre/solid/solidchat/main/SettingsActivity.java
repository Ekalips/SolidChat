package com.andre.solid.solidchat.main;

import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.andre.solid.solidchat.R;
import com.andre.solid.solidchat.data.AddQuestionRequest;
import com.andre.solid.solidchat.data.Answer;
import com.andre.solid.solidchat.data.QuickQuestion;
import com.andre.solid.solidchat.data.UserDataChangeRequest;
import com.andre.solid.solidchat.databinding.ActivitySettingsBinding;
import com.andre.solid.solidchat.databinding.DialogAddQuestionBinding;
import com.andre.solid.solidchat.stuff.Utils;
import com.andre.solid.solidchat.user.User;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;

public class SettingsActivity extends AppCompatActivity {

    Realm realm;
    ActivitySettingsBinding binding;
    RealmResults<QuickQuestion> quickQuestions;
    UserDataChangeRequest request = new UserDataChangeRequest(User.getInstance());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        realm = Realm.getDefaultInstance();


        binding.setUser(request);

        binding.addQuestionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddQuestionDialog();
            }
        });

        quickQuestions = realm.where(QuickQuestion.class).equalTo("author", User.getInstance().getMac()).findAllAsync();
        quickQuestions.addChangeListener(questionsChangeListener);

        if (quickQuestions.isLoaded())
            setQuickQuestionsToBinding(quickQuestions);

        setSupportActionBar(binding.includeToolbar.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setQuickQuestionsToBinding(RealmResults<QuickQuestion> quickQuestions) {
        List<QuickQuestion> newQuickQuestionList = new ArrayList<>();
        for (QuickQuestion q :
                quickQuestions) {
            newQuickQuestionList.add(new QuickQuestion(q));
        }
        request.setQuickQuestions(newQuickQuestionList);
    }

    RealmChangeListener<RealmResults<QuickQuestion>> questionsChangeListener = new RealmChangeListener<RealmResults<QuickQuestion>>() {
        @Override
        public void onChange(RealmResults<QuickQuestion> element) {
            setQuickQuestionsToBinding(element);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                break;
            }
            case R.id.menu_save: {
                tryToSaveData();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void tryToSaveData() {
        if (request.getName() == null || request.getName().isEmpty()) {
            Utils.showToastMessage(R.string.error_enter_name);
            return;
        }

        User.getInstance().setName(request.getName());

        realm.beginTransaction();
        realm.insertOrUpdate(request.getQuickQuestions());
        realm.commitTransaction();

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    private void showAddQuestionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DialogAddQuestionBinding binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.dialog_add_question, null, false);
        final AddQuestionRequest addQuestionRequest = new AddQuestionRequest();
        binding.setRequest(addQuestionRequest);
        builder.setView(binding.getRoot());
        builder.setCancelable(false);
        builder.setTitle(R.string.dialog_add_question_title);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button button = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (addQuestionRequest.isValid()) {
                            addQuestion(addQuestionRequest);
                        }
                        dialog.dismiss();
                    }
                });
            }
        });
        dialog.show();
    }

    private void addQuestion(AddQuestionRequest addQuestionRequest) {
        RealmList<Answer> answers = new RealmList<>();
        for (String s :
                addQuestionRequest.getQuestionAnswers().trim().split(System.lineSeparator())) {
            answers.add(new Answer(s));
        }
        request.addQuestionRequest(new QuickQuestion(addQuestionRequest.getQuestionName(), answers, User.getInstance().getMac()));
    }
}
