package com.sb.play.media;

import android.content.Context;
import android.media.MediaPlayer;

import com.sb.play.bingo.R;

public class MusicMedia {
    private MediaPlayer mediaPlayerClick;
    private MediaPlayer mediaPlayerBubble;
    private MediaPlayer mediaPlayerWin;
    private MediaPlayer mediaPlayerLost;

    public MusicMedia(Context context) {
        mediaPlayerClick = MediaPlayer.create(context, R.raw.click);
        mediaPlayerBubble = MediaPlayer.create(context, R.raw.bubble);
        mediaPlayerWin = MediaPlayer.create(context, R.raw.win);
        mediaPlayerLost = MediaPlayer.create(context, R.raw.lost);
    }

    public void playClick() {
        mediaPlayerClick.start();
    }

    public void playBubble() {
        mediaPlayerBubble.start();
    }

    public void playWin() {
        mediaPlayerWin.start();
    }

    public void playLost() {
        mediaPlayerLost.start();
    }
}
