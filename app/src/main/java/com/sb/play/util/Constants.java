package com.sb.play.util;

import java.util.ArrayList;
import java.util.List;

public class Constants {
    protected static final List<String> emojis = new ArrayList<>();
    public static final String YOU = "you";
    public static final String TYPE_OF_GAME = "typeOfGame";
    public static final String CREATED_ROOM = "createdRoom";
    public static final String ROOM_ID = "roomId";
    public static final String COLOR_ACTIVE = "#7EA7DC";
    public static final String tagPattern = "%d,%d";
    public static final String MY_APP_NAME = "bingo";
    public static final String DEFAULT_NAME = "unknown";
    public static final String MY_NAME = "myName";
    public static final long POLLING_CYCLE_TIME = 250;
    public static final String RESPONSE = "Response";

    public static class DbConstants {
        public static final String DB_NAME = "bingoDB";
        public static final String TABLE_NAME = "stats";
        public static final String ROOM_ID_COLUMN = "room_id";
        public static final String PLAYERS_COLUMN = "players";
        public static final String WINNER_COLUMN = "winner";
        public static final String END_TIME_COLUMN = "endTime";
    }

}
