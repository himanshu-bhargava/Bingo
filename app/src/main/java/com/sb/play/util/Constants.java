package com.sb.play.util;

import java.util.ArrayList;
import java.util.List;

public class Constants {
    protected static final List<String> emojis = new ArrayList<>();
    public static final String TYPE_OF_GAME = "typeOfGame";
    public static final String CREATED_ROOM = "createdRoom";
    public static final String ROOM_ID = "roomId";
    public static final String COLOR_ACTIVE = "#7EA7DC";
    public static final String tagPattern = "%d,%d";
    public static final String MY_APP_NAME = "bingo";
    public static final String DEFAULT_NAME = "unknown";
    public static final String RESPONSE = "Response";
    public static final String MY_NAME = "myName";
    public static final String YOU = "You";
    public static final long POLLING_CYCLE_TIME = 250;

    public static class DbConstants {
        public static final String DB_NAME = "bingoDB";
        public static final String TABLE_NAME = "new_stats";
        public static final String OLD_TABLE_NAME = "stats";
        public static final String ROOM_ID_COLUMN = "room_id";
        public static final String PLAYERS_COLUMN = "players";
        public static final String WINNER_COLUMN = "winner";
        public static final String END_TIME_COLUMN = "endTime";
        public static final String IS_IMPORTED = "isImported";
    }


    public static class LocalBackendParam extends BackendParam {

        private final String URL = "http://192.168.1.2:8080/";

        @Override
        public String getRoomUrl() {
            return URL + VERSION + "/room";
        }

        @Override
        public String getEmojiUrl() {
            return URL + VERSION + "/emoji";
        }
    }

    public static class AwsBackendParam extends BackendParam {

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
}
