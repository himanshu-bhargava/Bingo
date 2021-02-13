package com.sb.play.asynctasks;

import android.os.AsyncTask;
import android.widget.Toast;

import com.sb.play.bingo.MainActivity;
import com.sb.play.bingo.models.BingoResponse;

public class JoinRoom extends AsyncTask<String, String, BingoResponse> {

    private MainActivity context;

    public JoinRoom(MainActivity context) {
        this.context = context;
    }

    @Override
    protected BingoResponse doInBackground(String... params) {
        BingoResponse response = context.getBackendService().joinRoom(params[0]);
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
                    Toast.makeText(context, "Could not join the room!", Toast.LENGTH_SHORT).show();
                    context.finish();
                }
            });
        } else {
            context.setRoom(response.getRoom());
            context.updateMySelfInPlayerGrid(response.getPlayers().get(response.getPlayers().size() - 1));
            context.getAllPlayers().addAll(response.getPlayers());
            context.setPlayerGettingUpdated(true);
            context.updatePlayersGrid();
            context.setLocalGameStatus(com.sb.play.bingo.models.Status.joining);
            Toast.makeText(context, "Game joined successfully", Toast.LENGTH_SHORT).show();
        }
    }
}