package com.sb.play.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class BingoDbUtil {
    private static final String TAG = "BingoDbUtil";

    public static SQLiteDatabase getDatabase(Context context) {
        SQLiteDatabase bingoDatabase = context
                .openOrCreateDatabase(Constants.DbConstants.DB_NAME, Context.MODE_PRIVATE, null);
        createTable(bingoDatabase);
        return bingoDatabase;
    }

    private static void createTable(SQLiteDatabase bingoDatabase) {
        bingoDatabase.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s (%s VARCHAR, %s VARCHAR, %s VARCHAR, %s BigInt)",
                Constants.DbConstants.TABLE_NAME, Constants.DbConstants.ROOM_ID_COLUMN,
                Constants.DbConstants.PLAYERS_COLUMN, Constants.DbConstants.WINNER_COLUMN,
                Constants.DbConstants.END_TIME_COLUMN));
    }

    public static void importOldStats(Context context, SQLiteDatabase bingoDatabase) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.MY_APP_NAME, Context.MODE_PRIVATE);
        if (!sharedPreferences.getBoolean(Constants.DbConstants.IS_IMPORTED, false)) {
            Log.i(TAG, "importOldStats: importing old data to new table");
            try {
                Cursor c = bingoDatabase.rawQuery(String.format("SELECT * FROM %s ORDER BY %s DESC LIMIT 200",
                        Constants.DbConstants.OLD_TABLE_NAME,
                        Constants.DbConstants.ROOM_ID_COLUMN), null);
                int roomIndex = c.getColumnIndex(Constants.DbConstants.ROOM_ID_COLUMN);
                int playersIndex = c.getColumnIndex(Constants.DbConstants.PLAYERS_COLUMN);
                int winnerIndex = c.getColumnIndex(Constants.DbConstants.WINNER_COLUMN);
                int timeIndex = c.getColumnIndex(Constants.DbConstants.END_TIME_COLUMN);
                createTable(bingoDatabase);
                while (c.moveToNext()) {
                    insertRecord(bingoDatabase, c.getString(roomIndex), c.getString(playersIndex), c.getString(winnerIndex),
                            Long.parseLong(c.getString(timeIndex)));
                }
            } catch (Exception e) {
                Log.e("error", "GameStats on import: ", e);
            } finally {
                bingoDatabase.execSQL(String.format("DROP TABLE IF EXISTS %s", Constants.DbConstants.OLD_TABLE_NAME));
            }
            sharedPreferences.edit().putBoolean(Constants.DbConstants.IS_IMPORTED, true).apply();
        }
    }

    public static void insertRecord(SQLiteDatabase bingoDatabase, String roomId, String players, String winner) {
        bingoDatabase.execSQL(String.format("INSERT INTO %s VALUES('%s', '%s', '%s', %d)",
                Constants.DbConstants.TABLE_NAME, roomId, players, winner, System.currentTimeMillis()));
    }

    private static void insertRecord(SQLiteDatabase bingoDatabase, String roomId, String players, String winner, Long time) {
        bingoDatabase.execSQL(String.format("INSERT INTO %s VALUES('%s', '%s', '%s', %d )",
                Constants.DbConstants.TABLE_NAME, roomId, players, winner, time));
    }

    public static Cursor getTotalGamesCursor(SQLiteDatabase bingoDatabase) {
        return bingoDatabase.rawQuery(String.format("SELECT COUNT(*) FROM %s", Constants.DbConstants.TABLE_NAME), null);
    }

    public static Cursor getWinCountCursor(SQLiteDatabase bingoDatabase) {
        return bingoDatabase.rawQuery(String.format("SELECT COUNT(*) FROM %s where winner='%s'",
                Constants.DbConstants.TABLE_NAME, "You"), null);
    }

    public static Cursor getAllGameDetailsCursor(SQLiteDatabase bingoDatabase) {
        return bingoDatabase.rawQuery(String.format("SELECT * FROM %s ORDER BY %s DESC LIMIT 200",
                Constants.DbConstants.TABLE_NAME,
                Constants.DbConstants.END_TIME_COLUMN), null);
    }
}
