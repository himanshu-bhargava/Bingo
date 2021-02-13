package com.sb.play.asynctasks;

import android.os.AsyncTask;
import android.widget.Toast;

import com.sb.play.bingo.MainActivity;
import com.sb.play.bingo.models.BingoResponse;

public class StartGame extends AsyncTask<String, String, BingoResponse> {

    private MainActivity context;

    public StartGame(MainActivity context) {
        this.context = context;
    }

    @Override
    protected BingoResponse doInBackground(String... params) {
        BingoResponse response = context.getBackendService().startGame(params[0]);
        return response;
    }

    @Override
    protected void onPostExecute(BingoResponse response) {
        super.onPostExecute(response);
        if (response == null) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    context.setLocalGameStatus(com.sb.play.bingo.models.Status.completed);
                    Toast.makeText(context, "Could not start the game!", Toast.LENGTH_SHORT).show();
                    context.finish();
                }
            });
        }
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "Game started!!!", Toast.LENGTH_SHORT).show();
            }
        });
        context.setMyTurn(true);
        context.setLocalGameStatus(com.sb.play.bingo.models.Status.started);
        context.unFreeze();
    }
}
