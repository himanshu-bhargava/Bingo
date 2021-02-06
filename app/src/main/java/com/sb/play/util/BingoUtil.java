package com.sb.play.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class BingoUtil {
    public static SQLiteDatabase getDatabase(Context context) {
        SQLiteDatabase bingoDatabase = context.openOrCreateDatabase(Constants.DbConstants.DB_NAME, Context.MODE_PRIVATE, null);
        bingoDatabase.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s (%s VARCHAR, %s VARCHAR, %s VARCHAR, %s VARCHAR)",
                Constants.DbConstants.TABLE_NAME, Constants.DbConstants.ROOM_ID_COLUMN,
                Constants.DbConstants.PLAYERS_COLUMN, Constants.DbConstants.WINNER_COLUMN,
                Constants.DbConstants.END_TIME_COLUMN));
        return bingoDatabase;
    }
}
