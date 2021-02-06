package com.sb.play.util;

public class Constants {
    public static final String TYPE_OF_GAME = "typeOfGame";
    public static final String CREATED_ROOM = "createdRoom";
    public static final String ROOM_ID = "roomId";
    public static final String COLOR_ACTIVE = "#7EA7DC";
    public static final String tagPattern = "%d,%d";
    public static final String MY_APP_NAME = "bingo";
    public static final long POLLING_CYCLE_TIME = 250;
    public static final String UNKNOWN = "unknown";
    public static final String MY_NAME = "myName";

    public static class DbConstants {
        public static final String DB_NAME = "bingoDB";
        public static final String TABLE_NAME = "stats";
        public static final String ROOM_ID_COLUMN = "room_id";
        public static final String PLAYERS_COLUMN = "players";
        public static final String WINNER_COLUMN = "winner";
        public static final String END_TIME_COLUMN = "endTime";
    }
}
