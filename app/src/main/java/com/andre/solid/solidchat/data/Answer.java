package com.andre.solid.solidchat.data;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by lantain on 09.04.17.
 */

public class Answer extends RealmObject {
    @PrimaryKey
    private String answer;

    public Answer() {
    }

    public Answer(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Answer answer1 = (Answer) o;

        return answer != null ? answer.equals(answer1.answer) : answer1.answer == null;

    }

    @Override
    public int hashCode() {
        return answer != null ? answer.hashCode() : 0;
    }
}
