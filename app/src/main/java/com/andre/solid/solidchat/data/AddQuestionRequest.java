package com.andre.solid.solidchat.data;

/**
 * Created by lantain on 09.04.17.
 */

public class AddQuestionRequest {
    private String questionName = "";
    private String questionAnswers = "";

    public String getQuestionName() {
        return questionName;
    }

    public void setQuestionName(String questionName) {
        this.questionName = questionName;
    }

    public String getQuestionAnswers() {
        return questionAnswers;
    }

    public void setQuestionAnswers(String questionAnswers) {
        this.questionAnswers = questionAnswers;
    }

    public boolean isValid() {
        return !questionName.trim().isEmpty() && !questionAnswers.trim().isEmpty();
    }
}
