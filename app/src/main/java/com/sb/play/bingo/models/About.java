package com.sb.play.bingo.models;

import java.util.ArrayList;
import java.util.List;

public class About {
    private String question;
    private Answer answer;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Answer getAnswer() {
        return answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }

    @Override
    public String toString() {
        return "About{" +
                "question='" + question + '\'' +
                ", answer=" + answer +
                '}';
    }

    public static class Answer {
        private String main;
        private List<Answer> sub=new ArrayList<>();

        public String getMain() {
            return main;
        }

        public void setMain(String main) {
            this.main = main;
        }

        public List<Answer> getSub() {
            return sub;
        }

        public void setSub(List<Answer> sub) {
            this.sub = sub;
        }

        @Override
        public String toString() {
            return "Answer{" +
                    "main='" + main + '\'' +
                    ", sub=" + sub +
                    '}';
        }
    }
}
