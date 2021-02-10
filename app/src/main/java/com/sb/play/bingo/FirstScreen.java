package com.sb.play.bingo;

import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.sb.play.bingo.models.BingoResponse;
import com.sb.play.bingo.services.BackendService;
import com.sb.play.util.Constants;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class FirstScreen extends AppCompatActivity implements View.OnKeyListener {

    private static final String DEFAULT_NAME = "unknown";
    public static String myName;
    private final BackendService backendService = new BackendService();
    Button createRoomButton;
    Button joinRoomButton;
    EditText myNameEditText;
    EditText roomId;
    SharedPreferences sharedPreferences;
    MediaPlayer mediaPlayer;

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
        myNameEditText = (EditText) findViewById(R.id.myNameEditText);
        sharedPreferences = getSharedPreferences(Constants.MY_APP_NAME, Context.MODE_PRIVATE);
        Log.i("Test", "" + myNameEditText.getText());
        fetchAndAssignSavedName();
    }

    private void fetchAndAssignSavedName() {
        Log.i("fetchAndAssignSavedName", "fetching existing name");
        myName = sharedPreferences.getString(Constants.MY_NAME, DEFAULT_NAME);
        saveNameInMemory(myName);
        saveNameInMemory(myName);
        myNameEditText.setText(myName);
    }

    public void saveNameInMemory(String name) {
        sharedPreferences.edit().putString(Constants.MY_NAME, name).apply();
    }

    public void submitUserName(View view) {
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.click);
        mediaPlayer.start();
        if (myNameEditText == null) {
            Toast.makeText(this, "Not initialized yet", Toast.LENGTH_SHORT).show();
        }
        Object updatedName = myNameEditText.getText().toString();
        if (updatedName == null || updatedName.toString().length() < 4) {
            Toast.makeText(this, "Please enter a name with length>4", Toast.LENGTH_SHORT).show();
            return;
        }
        myName = updatedName.toString();
        saveNameInMemory(updatedName.toString());
        new AlertDialog.Builder(this).setMessage("Saved your name!").create().show();
    }

    public void joinRoom(View view) {
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.click);
        mediaPlayer.start();
        String roomIdNumber = roomId.getText().toString();
        if (roomIdNumber.isEmpty()) {
            new AlertDialog.Builder(this).setMessage("Please enter a room id!!!").show();
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

    private AlertDialog.Builder createSimpleAlert(String input) {
        return new AlertDialog.Builder(FirstScreen.this).setMessage(input);
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
                        createSimpleAlert("Could not create the room please try again later!!!");
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
    public void launchStatActivity(View view){
        Intent idIntent = new Intent(getApplicationContext(), GameStats.class);
        startActivity(idIntent);
    }
    public void about(View view){
        Intent intent=new Intent(this,AboutGame.class);
        startActivity(intent);
    }
}