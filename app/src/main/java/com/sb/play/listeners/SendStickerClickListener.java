package com.sb.play.listeners;

import android.view.View;
import android.widget.Toast;

import com.sb.play.bingo.MainActivity;
import com.sb.play.bingo.models.Status;

public class SendStickerClickListener implements View.OnClickListener {
    private MainActivity context;

    public SendStickerClickListener(MainActivity context) {
        this.context = context;
    }

    @Override
    public void onClick(View view) {
        if (view.getTag().toString().equals(context.getMySelf().getId().toString())) {
            return;
        }
        if (!(context.getLocalGameStatus().equals(Status.started) || context.getLocalGameStatus().equals(Status.playingTurn))) {
            Toast.makeText(context, "Cannot send sticker now!", Toast.LENGTH_SHORT).show();
            return;
        }
        context.getReceivedEmojiLayout().setVisibility(View.INVISIBLE);
        context.setVisibilityForStickerView(true);
        context.setSendingEmojiTo(new Long(view.getTag().toString()));
    }
}
