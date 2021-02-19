package com.sb.play.asynctasks;

import android.content.Intent;
import android.os.AsyncTask;

import com.sb.play.bingo.MainActivity;
import com.sb.play.bingo.R;
import com.sb.play.bingo.models.BingoResponse;
import com.sb.play.bingo.services.BackendService;
import com.sb.play.util.BingoUtil;
import com.sb.play.util.Constants;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class CreateRoom extends AsyncTask<String, String, BingoResponse> {
    private AlertDialog popUp;
    private AppCompatActivity context;
    private BackendService backendService;

    public CreateRoom(AppCompatActivity context, BackendService backendService) {
        this.context = context;
        this.backendService = backendService;
    }

    @Override
    protected BingoResponse doInBackground(String... params) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                popUp = BingoUtil.createSimpleAlert(context, context.getString(R.string.room_creation_in_progress)).show();
            }
        });
        BingoResponse response = backendService.createRoom();
        return response;
    }

    @Override
    protected void onPostExecute(final BingoResponse bingoResponse) {
        super.onPostExecute(bingoResponse);
        popUp.dismiss();
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (bingoResponse == null) {
                    BingoUtil.createSimpleAlert(context, context.getString(R.string.could_not_create_room_try_later));
                    return;
                }
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra(Constants.RESPONSE, bingoResponse);
                intent.putExtra(Constants.TYPE_OF_GAME, Constants.CREATED_ROOM);
                intent.putExtra(Constants.ROOM_ID, bingoResponse.getRoom().getId().toString());
                context.startActivity(intent);
            }
        });
    }
}