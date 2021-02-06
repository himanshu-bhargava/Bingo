package com.sb.play.bingo.models;

import java.io.Serializable;

public class Request implements Serializable {
    private String name;

    public Request(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
