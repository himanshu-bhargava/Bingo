package com.sb.play.asynctasks;

import android.os.AsyncTask;
import android.util.Log;

import com.sb.play.bingo.MainActivity;
import com.sb.play.bingo.models.BingoResponse;
import com.sb.play.bingo.models.Turn;

public class PlayTurn extends AsyncTask<Object, String, BingoResponse> {

    private MainActivity context;

    public PlayTurn(MainActivity context) {
        this.context = context;
    }

    @Override
    protected BingoResponse doInBackground(Object... params) {
        Log.e("PlayTurn", "sending request");
        return context.getBackendService().playTurn((Long) params[0], (Turn) params[1]);
    }

    @Override
    protected void onPostExecute(BingoResponse s) {
        super.onPostExecute(s);
        Log.i("PlayTurn", "next Turn: " + s);
        context.setLocalGameStatus(com.sb.play.bingo.models.Status.started);
        Log.i("PlayTurn", "changing status after playing the turn " + context.getLocalGameStatus().toString());
    }
}