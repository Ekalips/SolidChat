package com.andre.solid.solidchat.events;

/**
 * Created by lantain on 09.04.17.
 */

public class OnQuickAnswerClick {
    private String answer;

    public OnQuickAnswerClick(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }
}
