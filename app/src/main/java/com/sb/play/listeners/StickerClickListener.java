package com.sb.play.listeners;

import android.view.View;
import android.widget.ImageView;

import com.sb.play.asynctasks.SendEmoji;
import com.sb.play.bingo.MainActivity;

public class StickerClickListener implements View.OnClickListener {

    private MainActivity context;

    public StickerClickListener(MainActivity context) {
        this.context = context;
    }

    @Override
    public void onClick(View view) {
        ImageView current = (ImageView) view;
        context.setVisibilityForStickerView(false);
        new SendEmoji(context.getMySelf().getId(), context.getSendingEmojiTo(), current.getTag().toString(), context).execute();
    }
}