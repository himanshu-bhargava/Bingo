package com.sb.play.bingo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sb.play.asynctasks.CreateRoom;
import com.sb.play.bingo.services.BackendService;
import com.sb.play.media.MusicMedia;
import com.sb.play.util.BingoUtil;
import com.sb.play.util.Constants;

import androidx.appcompat.app.AppCompatActivity;

public class FirstScreen extends AppCompatActivity implements View.OnKeyListener {

    private static final String TAG = "First screen: ";

    private BackendService backendService;

    private Button createRoomButton;
    private Button joinRoomButton;
    private EditText myNameEditText;
    private EditText roomId;
    private SharedPreferences sharedPreferences;

    private MusicMedia musicMedia;

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
        initialize();
    }

    private void initialize() {
        sharedPreferences = getSharedPreferences(Constants.MY_APP_NAME, Context.MODE_PRIVATE);
        musicMedia = new MusicMedia(this);
        backendService = new BackendService(this);
        initializeUiContent();
        fetchAndAssignSavedName();
    }

    private void initializeUiContent() {
        createRoomButton = findViewById(R.id.createRoom);
        joinRoomButton = findViewById(R.id.joinRoom);
        roomId = findViewById(R.id.roomId);
        myNameEditText = findViewById(R.id.myNameEditText);
    }

    private void fetchAndAssignSavedName() {
        Log.i(TAG, "fetching existing name");
        String myName = sharedPreferences.getString(Constants.MY_NAME, Constants.DEFAULT_NAME);
        saveNameInMemory(myName);
        myNameEditText.setText(myName);
    }

    public void saveNameInMemory(String name) {
        sharedPreferences.edit().putString(Constants.MY_NAME, name).apply();
    }

    public void submitUserName(View view) {
        musicMedia.playClick();
        if (myNameEditText == null) {
            Toast.makeText(this, "Not initialized yet", Toast.LENGTH_SHORT).show();
        }
        Object updatedName = myNameEditText.getText().toString();
        if (updatedName == null || updatedName.toString().length() < 4) {
            Toast.makeText(this, "Please enter a name with length>4", Toast.LENGTH_SHORT).show();
            return;
        }
        saveNameInMemory(updatedName.toString());
        BingoUtil.createSimpleAlert(this, "Saved your name!").show();
    }

    public void joinRoom(View view) {
        musicMedia.playClick();
        String roomIdNumber = roomId.getText().toString();
        if (roomIdNumber.isEmpty()) {
            BingoUtil.createSimpleAlert(this, "Please enter a room id!!!").show();
            return;
        }
        //clear room id
        roomId.setText("");
        startActivity(new Intent(getApplicationContext(), MainActivity.class).putExtra(Constants.ROOM_ID, roomIdNumber));
    }

    public void createRoom(View view) {
        musicMedia.playClick();
        Log.i(TAG, "createRoom: creating room");
        new CreateRoom(this, backendService).execute();
    }

    public void launchStatActivity(View view) {
        Log.i(TAG, "launchStatActivity: launching stats activity");
        startActivity(new Intent(getApplicationContext(), GameStats.class));
    }

    public void launchAboutActivity(View view) {
        Log.i(TAG, "launchAboutActivity: launching about activity");
        startActivity(new Intent(this, AboutGame.class));
    }
}