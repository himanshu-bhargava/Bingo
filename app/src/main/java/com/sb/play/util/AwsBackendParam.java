package com.sb.play.util;

public class AwsBackendParam extends BackendParam {

    private static final String URL = "http://bingobackendservice-env.eba-3ymgj4jd.us-east-2.elasticbeanstalk.com/";

    @Override
    public String getRoomUrl() {
        return URL + VERSION + "/room";
    }

    @Override
    public String getEmojiUrl() {
        return URL + VERSION + "/emoji";
    }
}
