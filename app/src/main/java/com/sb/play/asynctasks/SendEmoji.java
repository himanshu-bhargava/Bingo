package com.sb.play.asynctasks;

import android.os.AsyncTask;

import com.sb.play.bingo.MainActivity;
import com.sb.play.bingo.models.Emoji;
import com.sb.play.bingo.services.BackendService;

public class SendEmoji extends AsyncTask<String, String, String> {

    private final Long sender;
    private final Long receiver;
    private final String emoji;
    private MainActivity context;

    public SendEmoji(Long sender, Long receiver, String emoji, MainActivity context) {
        this.sender = sender;
        this.receiver = receiver;
        this.emoji = emoji;
        this.context = context;
    }

    @Override
    protected String doInBackground(String... strings) {
        context.getBackendService().sendEmoji(new Emoji(sender, receiver, emoji), context.getRoom().getId());
        return null;
    }
}
