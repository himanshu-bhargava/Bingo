package com.sb.play.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sb.play.bingo.models.About;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import androidx.appcompat.app.AlertDialog;

public class BingoUtil {
    public static SQLiteDatabase getDatabase(Context context) {
        SQLiteDatabase bingoDatabase = context.openOrCreateDatabase(Constants.DbConstants.DB_NAME, Context.MODE_PRIVATE, null);
        bingoDatabase.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s (%s VARCHAR, %s VARCHAR, %s VARCHAR, %s VARCHAR)",
                Constants.DbConstants.TABLE_NAME, Constants.DbConstants.ROOM_ID_COLUMN,
                Constants.DbConstants.PLAYERS_COLUMN, Constants.DbConstants.WINNER_COLUMN,
                Constants.DbConstants.END_TIME_COLUMN));
        return bingoDatabase;
    }

    public static int getResourceIdForImage(String name, Context context) {
        return context.getResources().getIdentifier(name, "drawable", context.getPackageName());
    }

    public static List<String> getEmojiNames(Context context) {
        if (Constants.emojis.isEmpty()) {
            try {
                Constants.emojis.addAll(Arrays.asList(new ObjectMapper().readValue(
                        context.getAssets()
                                .open("emojis.json"), String[].class)));
            } catch (Exception e) {
                Log.e("Reading error", e.toString());
            }
        }
        return Constants.emojis;
    }

    public static List<About> readQuesAnswer(Context context) throws IOException {
        return Arrays.asList(new ObjectMapper().readValue(
                context.getAssets()
                        .open("about.json"), About[].class));
    }

    public static AlertDialog.Builder createSimpleAlert(Context context, String message) {
        return new AlertDialog.Builder(context).setMessage(message);
    }

    public static int getRandom(int max) {
        return (int) (Math.random() * max);
    }
}
