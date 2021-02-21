package com.sb.play.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sb.play.bingo.models.About;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class BingoUtil {

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

    public static String capitalize(String input) {
        char[] array = input.toLowerCase().toCharArray();
        array[0] = Character.toUpperCase(array[0]);
        return String.valueOf(array);
    }
}
