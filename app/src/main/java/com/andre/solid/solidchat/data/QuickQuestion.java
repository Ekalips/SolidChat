package com.andre.solid.solidchat.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by lantain on 09.04.17.
 */

public class QuickQuestion extends RealmObject {
    @PrimaryKey
    private String question;
    private RealmList<Answer> answers;

    public QuickQuestion() {
    }

    public QuickQuestion(QuickQuestion quickQuestion) {
        this.question = quickQuestion.getQuestion();
        this.answers = new RealmList<>();
        for (Answer q :
                quickQuestion.getAnswers()) {
            answers.add(new Answer(q.getAnswer()));
        }
    }

    public QuickQuestion(String question, RealmList<Answer> answers) {
        this.question = question;
        this.answers = answers;
    }


    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public RealmList<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(RealmList<Answer> answers) {
        this.answers = answers;
    }

    public String getStrAnswers() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0;i<answers.size();i++) {
            builder.append(answers.get(i).getAnswer());
            if (i!=answers.size()-1)
                builder.append(", ");
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QuickQuestion that = (QuickQuestion) o;

        return question != null ? question.equals(that.question) : that.question == null;

    }

    @Override
    public int hashCode() {
        return question != null ? question.hashCode() : 0;
    }
}
