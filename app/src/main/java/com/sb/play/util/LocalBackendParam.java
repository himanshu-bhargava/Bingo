package com.sb.play.util;

public class LocalBackendParam extends BackendParam {

    private final String URL = "http://192.168.1.3:8080/";

    @Override
    public String getRoomUrl() {
        return URL + VERSION + "/room";
    }

    @Override
    public String getEmojiUrl() {
        return URL + VERSION + "/emoji";
    }
}
