package com.sb.play.bingo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.sb.play.bingo.models.BingoResponse;
import com.sb.play.bingo.services.BackendService;
import com.sb.play.util.BingoUtil;
import com.sb.play.util.Constants;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class FirstScreen extends AppCompatActivity implements View.OnKeyListener {

    private final BackendService backendService = new BackendService();
    private Button createRoomButton;
    private Button joinRoomButton;
    private EditText myNameEditText;
    private EditText roomId;
    private SharedPreferences sharedPreferences;
    private MediaPlayer mediaPlayer;
    public static String myName;

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_screen);
        createRoomButton = findViewById(R.id.createRoom);
        joinRoomButton = findViewById(R.id.joinRoom);
        roomId = findViewById(R.id.roomId);
        myNameEditText = findViewById(R.id.myNameEditText);
        sharedPreferences = getSharedPreferences(Constants.MY_APP_NAME, Context.MODE_PRIVATE);
        fetchAndAssignSavedName();
    }

    private void fetchAndAssignSavedName() {
        Log.i("fetchAndAssignSavedName", "fetching existing name");
        myName = sharedPreferences.getString(Constants.MY_NAME, Constants.DEFAULT_NAME);
        saveNameInMemory(myName);
        myNameEditText.setText(myName);
    }

    public void saveNameInMemory(String name) {
        sharedPreferences.edit().putString(Constants.MY_NAME, name).apply();
    }

    public void submitUserName(View view) {
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.click);
        mediaPlayer.start();
        String updatedName = myNameEditText.getText().toString().trim();
        if (updatedName.length() < 2) {
            buildAlert("Name is too short", "Please use at least 2 characters.", false);
        } else if (updatedName.length() > 10) {
            buildAlert("Name is too long", "Please use at most 10 characters.", false);
        } else if (Constants.YOU.equalsIgnoreCase(updatedName)) {
            buildAlert("Invalid Name", "This name is reserved and is not allowed to be used.", false);
        } else {
            myName = BingoUtil.capitalize(updatedName);
            myNameEditText.setText(myName);
            saveNameInMemory(myName);
            buildAlert("Saved your name",
                    "Your name is saved. This will be used in future games as well.", true);
        }
    }

    public void joinRoom(View view) {
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.click);
        mediaPlayer.start();
        String roomIdNumber = roomId.getText().toString();
        if (roomIdNumber.isEmpty()) {
            buildAlert("Empty room id",
                    "Please enter a room id to join the game.", false);
            return;
        }
        Intent idIntent = new Intent(getApplicationContext(), MainActivity.class);
        idIntent.putExtra(Constants.ROOM_ID, roomIdNumber);
        roomId.setText("");
        startActivity(idIntent);
    }

    public void createRoom(View view) {
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.click);
        mediaPlayer.start();
        Log.i("create room", "createRoom: here");
        new CreateRoom().execute();
    }

    private class CreateRoom extends AsyncTask<String, String, BingoResponse> {
        AlertDialog popUp;

        @Override
        protected BingoResponse doInBackground(String... params) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    popUp = createSimpleAlert("Room creation in progress!").show();
                }
            });
            BackendService.name = myName;
            BingoResponse response = backendService.createRoom();
            return response;
        }

        @Override
        protected void onPostExecute(final BingoResponse bingoResponse) {
            super.onPostExecute(bingoResponse);
            popUp.dismiss();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (bingoResponse == null) {
                        buildAlert("Could not create room",
                                "Please make sure you are connected to internet and then try again.", false);
                        return;
                    }
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("Response", bingoResponse);
                    intent.putExtra(Constants.TYPE_OF_GAME, Constants.CREATED_ROOM);
                    intent.putExtra(Constants.ROOM_ID, bingoResponse.getRoom().getId().toString());
                    startActivity(intent);
                }
            });
        }
    }

    public void launchStatActivity(View view) {
        Intent idIntent = new Intent(getApplicationContext(), GameStats.class);
        startActivity(idIntent);
    }

    public void about(View view) {
        Intent intent = new Intent(this, AboutGame.class);
        startActivity(intent);
    }

    private AlertDialog.Builder createSimpleAlert(String input) {
        return new AlertDialog.Builder(FirstScreen.this).setMessage(input);
    }

    private void buildAlert(String title, String message, boolean isInfo) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message)
                .setIcon(isInfo ? android.R.drawable.ic_dialog_info : android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .setPositiveButton("ok", null)
                .create().show();
    }
}