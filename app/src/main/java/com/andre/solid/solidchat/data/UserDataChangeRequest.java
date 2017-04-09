package com.andre.solid.solidchat.data;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;

import com.andre.solid.solidchat.BR;
import com.andre.solid.solidchat.user.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lantain on 09.04.17.
 */

public class UserDataChangeRequest extends BaseObservable {
    private String name = "";
    @Bindable
    private List<QuickQuestion> quickQuestions = new ArrayList<>();

    public UserDataChangeRequest(User user) {
        name = user.getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<QuickQuestion> getQuickQuestions() {
        return quickQuestions;
    }

    public void addQuestionRequest(QuickQuestion quickQuestion) {
        this.quickQuestions.add(quickQuestion);
        notifyPropertyChanged(BR.quickQuestions);
    }

    public void setQuickQuestions(List<QuickQuestion> quickQuestions) {
        this.quickQuestions = quickQuestions;
        notifyPropertyChanged(BR.quickQuestions);
    }


}
