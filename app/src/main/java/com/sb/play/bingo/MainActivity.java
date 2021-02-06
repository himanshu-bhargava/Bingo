package com.sb.play.bingo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sb.play.bingo.models.BingoResponse;
import com.sb.play.bingo.models.Player;
import com.sb.play.bingo.models.Room;
import com.sb.play.bingo.models.Turn;
import com.sb.play.bingo.services.BackendService;
import com.sb.play.util.BingoUtil;
import com.sb.play.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.gridlayout.widget.GridLayout;


public class MainActivity extends AppCompatActivity {

    private final boolean crossed[][] = new boolean[5][5];
    private final Position[] positions = new Position[25];
    BingoResponse bingoResponse;
    MediaPlayer mediaPlayerClick;
    MediaPlayer mediaPlayerBubble;
    MediaPlayer mediaPlayerWin;
    MediaPlayer mediaPlayerLost;
    Vibrator v;
    Animation rotate;
    private BackendService backendService = new BackendService();
    private Set<Player> allPlayers = Collections.synchronizedSet(new HashSet<>());
    private String myName;
    private String gameType;
    volatile private Status localGameStatus = Status.loading;
    private Player mySelf;
    volatile private Room room;
    volatile private boolean isMyTurn = false;
    private int bingoCounter = 0;
    private int gameGridValueCounter;
    private int localStepCounter = 0;
    private GridLayout gameGrid;
    private GridLayout bingoGrid;
    private LinearLayout playerNameGrid;
    private Button startOrJoinGame;
    private Button customFill;
    private Button simpleFill;
    private Button randomFill;
    private Intent mainIntent;
    private Button roomIdToShareBottom;
    private TextView winnerAnnounce;
    private Button roomIdToShareTop;
    private ConstraintLayout fireWorksLayout;
    private ImageView fireworks;
    volatile private boolean isRemoteTurnCompleted = false;
    private boolean playerGettingUpdated = false;
    private boolean isWinnerAnnounced;

    private static int getRandom(int max) {
        return (int) (Math.random() * max);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        mainIntent = getIntent();
        joinGameViaLink();
        // declaring sound
        initializeSoundsAndEffects();
        //declaring vars
        initializeGameVariables();
        onRoomCreation(bingoResponse);
        initialize();
    }

    private void joinGameViaLink() {
        String action = mainIntent.getAction();
        if (action == null) {
            return;
        }
        Uri data = mainIntent.getData();
        Log.i("Opening from url: ", data.toString());
        try {
            int id = Integer.parseInt(data.getQueryParameter(Constants.ROOM_ID));
            mainIntent.putExtra(Constants.ROOM_ID, String.valueOf(id));
        } catch (Exception e) {
            Log.e("Error ", "Could not join the game via link");
            Toast.makeText(this, "Invalid room id!!!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeGameVariables() {
        fireworks = findViewById(R.id.fireworks);
        fireWorksLayout = findViewById(R.id.fireWorksLayout);
        gameGrid = findViewById(R.id.gameGrid);
        bingoGrid = findViewById(R.id.bingoGrid);
        playerNameGrid = findViewById(R.id.playersNameGrid);
        customFill = findViewById(R.id.customFill);
        simpleFill = findViewById(R.id.simpleFill);
        randomFill = findViewById(R.id.randomFill);
        winnerAnnounce = findViewById(R.id.winnerAnnounce);
        startOrJoinGame = findViewById(R.id.goGame);
        roomIdToShareBottom = findViewById(R.id.roomIdToShareBottom);
        roomIdToShareTop = findViewById(R.id.roomIdToShareTop);
        bingoResponse = (BingoResponse) mainIntent.getSerializableExtra("Response");
        gameType = mainIntent.getStringExtra(Constants.TYPE_OF_GAME);
        myName = getSharedPreferences(Constants.MY_APP_NAME, MODE_PRIVATE).getString(Constants.MY_NAME, Constants.UNKNOWN);
    }

    private void initializeSoundsAndEffects() {
        mediaPlayerBubble = MediaPlayer.create(getApplicationContext(), R.raw.bubble);
        mediaPlayerClick = MediaPlayer.create(getApplicationContext(), R.raw.click);
        mediaPlayerWin = MediaPlayer.create(getApplicationContext(), R.raw.win);
        mediaPlayerLost = MediaPlayer.create(getApplicationContext(), R.raw.lost);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        rotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
    }

    private void onRoomCreation(BingoResponse bingoResponse) {
        if (Constants.CREATED_ROOM.equals(gameType)) {
            playerGettingUpdated = true;
            updateMySelfInPlayerGrid(bingoResponse.getPlayers().get(0));
            room = bingoResponse.getRoom();
            allPlayers.add(mySelf);
            startPolling();
        }
        roomIdToShareTop.setText("Room: " + mainIntent.getStringExtra(Constants.ROOM_ID) + " share?");
        roomIdToShareBottom.setText("Room: " + mainIntent.getStringExtra(Constants.ROOM_ID) + " share?");
    }

    private void updateMySelfInPlayerGrid(Player player) {
        mySelf = player;
        mySelf.setName("You");
        playerNameGrid.addView(createPlayerButton(mySelf));
        playerGettingUpdated = false;
    }

    private void updatePlayersGrid() {
        Iterator<Player> iterator = allPlayers.iterator();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            Button existingPlayerButton = playerNameGrid.findViewWithTag(player.getId().toString());
            if (existingPlayerButton == null) {
                Log.i("Update player grid ", player.toString());
                playerNameGrid.addView(createPlayerButton(player));
            }
        }
        playerGettingUpdated = false;
    }

    private Button createPlayerButton(Player player) {
        Log.i("Create player button", "New player joined: " + player.getName() + "(" + player.getId() + ")");
        Button button = new Button(this);
        button.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.player_button));
        button.setText(player.getName());
        button.setTag(player.getId().toString());
        button.setClickable(false);
        button.setTextColor(Color.WHITE);
        return button;
    }

    public void startGame(View view) {
        mediaPlayerClick.start();
        if (Constants.CREATED_ROOM.equals(gameType)) {
            if (allPlayers.size() < 2) {
                Toast.makeText(this, "Wait for others to join!!", Toast.LENGTH_SHORT).show();
                return;
            }
            new StartGame().execute(room.getId().toString());
        } else {
            new JoinRoom().execute(mainIntent.getStringExtra(Constants.ROOM_ID));
            startPolling();
        }
        startOrJoinGame.setVisibility(View.INVISIBLE);
        roomIdToShareBottom.setVisibility(View.INVISIBLE);
    }

    private void vibrateAlert() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(100);
        }
    }

    private void popWinnerAlert(String name) {
        if (isWinnerAnnounced) return;
        updateStats(name);
        if ("you".equalsIgnoreCase(name)) {
            mediaPlayerWin.start();
        } else {
            mediaPlayerLost.start();
        }
        isWinnerAnnounced = true;
        Glide.with(this).load(R.raw.sparkles).into(fireworks);
        fireWorksLayout.setVisibility(View.VISIBLE);
        winnerAnnounce.setText(name + " won!!!");
    }

    private void updateStats(String winner) {
        SQLiteDatabase bingoDatabase = BingoUtil.getDatabase(this);
        try {
            bingoDatabase.execSQL(String.format("INSERT INTO %s VALUES('%s', '%s', '%s', '%s')",
                    Constants.DbConstants.TABLE_NAME,
                    mainIntent.getStringExtra(Constants.ROOM_ID),
                    getAllPlayersNameAsString(),
                    winner, String.valueOf(System.currentTimeMillis())));
            Log.i("stat", "updateStats: stats updated successfully");
        } catch (Exception e) {
            Log.e("error", "Could not update stats", e);
        }
    }

    private String getAllPlayersNameAsString() {
        StringBuilder s = new StringBuilder();
        List<Player> tempSet = new ArrayList(allPlayers);
        int size = tempSet.size();
        for (int i = 0; i < tempSet.size(); i++) {
            s.append(tempSet.get(i).getName());
            if (i != size - 1) {
                s.append(",");
            }
        }
        return s.toString();
    }

    public void closeActivity(View view) {
        finish();
    }

    public void startPolling() {
        Thread polling = new Polling();
        polling.setDaemon(true);
        polling.start();
        Log.i("starting Polling method", "started polling with daemon");
    }

    private void initialize() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                Button currentButton = gameGrid.findViewWithTag(String.format("%d,%d", i, j));
                currentButton.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                currentButton.setText("");
                currentButton.setClickable(false);
            }
        }
        startOrJoinGame.setText(Constants.CREATED_ROOM.equals(gameType) ? "Start Game" : "Join Game");
    }

    public void regularInitialize(View view) {
        mediaPlayerClick.start();
        roomIdToShareTop.setVisibility(View.GONE);
        roomIdToShareBottom.setVisibility(View.VISIBLE);
        localGameStatus = Status.initializing;
        int temp = 1;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                String tag = String.format("%d,%d", i, j);
                Button currentButton = gameGrid.findViewWithTag(tag);
                currentButton.setText(String.valueOf(temp));
                currentButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(Constants.COLOR_ACTIVE)));
                positions[temp - 1] = new Position(tag);
                temp++;
            }
        }
        disableFillButtons();
        startOrJoinGame.setVisibility(View.VISIBLE);
    }

    public void randomInitialize(View view) {
        roomIdToShareTop.setVisibility(View.GONE);
        roomIdToShareBottom.setVisibility(View.VISIBLE);
        mediaPlayerClick.start();
        localGameStatus = Status.initializing;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                String tag = String.format("%d,%d", i, j);
                Button currentButton = gameGrid.findViewWithTag(tag);
                int temp = randomNumber();
                currentButton.setText(String.valueOf(temp + 1));
                currentButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(Constants.COLOR_ACTIVE)));
                positions[temp] = new Position(tag);
            }
        }
        disableFillButtons();
        startOrJoinGame.setVisibility(View.VISIBLE);
    }

    private int randomNumber() {
        int number = getRandom(24);
        while (positions[number] != null) {
            number = (number + 1) % 25;
        }
        return number;
    }

    public void userInitialize(View view) {
        mediaPlayerClick.start();
        roomIdToShareTop.setVisibility(View.GONE);
        roomIdToShareBottom.setVisibility(View.VISIBLE);
        localGameStatus = Status.initializing;
        gameGridValueCounter = 0;
        unFreeze();
        disableFillButtons();
    }

    private void disableFillButtons() {
        simpleFill.setVisibility(View.GONE);
        randomFill.setVisibility(View.GONE);
        customFill.setVisibility(View.GONE);
        gameGrid.setVisibility(View.VISIBLE);
        findViewById(R.id.gridSelectMessage).setVisibility(View.GONE);
    }

    public void onGameGridButtonClick(View pressedButton) {
        Button thisButton = (Button) pressedButton;
        Position position = new Position(thisButton.getTag().toString());
        Log.i("Turn", localGameStatus + " " + isMyTurn + " " + thisButton.getText());
        if (localGameStatus.equals(Status.initializing)) {
            initializeGameGridButton(thisButton, position);
        } else {
            if (!localGameStatus.equals(Status.started) || !isMyTurn) {
                Toast.makeText(this, "You cannot play turn now!", Toast.LENGTH_SHORT).show();
            } else {
                localGameStatus = Status.playingTurn;
                isMyTurn = false;
                freeze();
                Log.i("onGameGridButtonClick", "Status changed to" + localGameStatus);
                playSingleTurn(thisButton, position);
                new PlayTurn().execute(
                        room.getId(),
                        new Turn(mySelf.getId(), Integer.parseInt(thisButton.getText().toString()), isBingoCompleted()));
            }
        }
    }

    public void rotateView(View view) {
        view.startAnimation(rotate);
    }

    private void playSingleTurn(Button thisButton, Position position) {
        mediaPlayerBubble.start();
        if (crossed[position.row][position.column]) {
            return;
        }
        localStepCounter++;
        crossed[position.row][position.column] = true;
        thisButton.setBackgroundTintMode(PorterDuff.Mode.DARKEN);
        rotateView(thisButton);
        thisButton.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.YELLOW, Color.GRAY}));
        thisButton.setClickable(false);
        checkAndUpdateCompletedLineInfo(position);
        Log.i("playSingleTurn: ", "pressed:" + thisButton.getText());
        isRemoteTurnCompleted = true;
    }

    private void initializeGameGridButton(Button pressedButton, Position position) {
        positions[gameGridValueCounter] = position;
        gameGridValueCounter++;
        pressedButton.setText(String.valueOf(gameGridValueCounter));
        pressedButton.setClickable(false);
        pressedButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(Constants.COLOR_ACTIVE)));
        if (gameGridValueCounter == 25) {
            startOrJoinGame.setVisibility(View.VISIBLE);
        }
    }

    public void checkAndUpdateCompletedLineInfo(Position position) {
        //check row
        checkRow(position);
        //check column
        checkColumn(position);
        //check diagonals
        checkDiagonal(position);
        //check reverse diagonal
        checkReverseDiagonal(position);
    }

    private void checkReverseDiagonal(Position position) {
        if (position.row + position.column == 4) {
            for (int i = 0; i < 5; i++) {
                if (!isLineCompleted(i, 4 - i)) return;
            }
            checkAndIncrementBingoCounter();
        }
    }

    private void checkDiagonal(Position position) {
        if (position.row == position.column) {
            for (int i = 0; i < 5; i++) {
                if (!isLineCompleted(i, i)) return;
            }
            checkAndIncrementBingoCounter();
        }
    }

    private void checkColumn(Position position) {
        for (int i = 0; i < 5; i++) {
            if (!isLineCompleted(i, position.column)) return;
        }
        checkAndIncrementBingoCounter();
    }

    private void checkRow(Position position) {
        for (int i = 0; i < 5; i++) {
            if (!isLineCompleted(position.row, i)) return;
        }
        checkAndIncrementBingoCounter();
    }

    private boolean isBingoCompleted() {
        return bingoCounter >= 5;
    }

    private boolean isLineCompleted(int row, int column) {
        return crossed[row][column];
    }

    private void checkAndIncrementBingoCounter() {
        if (!isBingoCompleted()) {
            bingoCounter++;
            Button thisButton = (Button) bingoGrid.findViewWithTag(String.valueOf(bingoCounter));
            thisButton.setBackgroundTintMode(PorterDuff.Mode.DARKEN);
            rotateView(thisButton);
            thisButton.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.RED, Color.CYAN}));
        }
    }

    public void freeze() {
        Log.i("Freeze", "freezing all the buttons");
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                Button currentButton = gameGrid.findViewWithTag(String.format("%d,%d", i, j));
                currentButton.setClickable(false);
            }
        }
        Log.i("Freeze", "done freezing all the buttons");
    }

    public void unFreeze() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (!crossed[i][j]) {
                    Button currentButton = gameGrid.findViewWithTag(String.format("%d,%d", i, j));
                    currentButton.setClickable(true);
                }
            }
        }
    }

    public void shareRoomId(View view) {
        mediaPlayerClick.start();
        String roomId = mainIntent.getStringExtra(Constants.ROOM_ID);
        Intent sharingIntent = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, "Click http://com.sb.bingo/joinroom?roomId=" + roomId + " to join!" +
                        "\n Or enter: " + roomId+ " to join the room")
                .putExtra(Intent.EXTRA_SUBJECT, "Click to join bingo:");
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startPolling();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        localGameStatus = Status.completed;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Do you want to Exit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    static private enum Status {
        loading, initializing, joining, started, playingTurn, completed;
    }

    private static class Position {
        final private int row;
        final private int column;

        Position(String tag) {
            String[] position = tag.split(",");
            this.row = Integer.parseInt(position[0]);
            this.column = Integer.parseInt(position[1]);
        }
    }

    private class PlayTurn extends AsyncTask<Object, String, BingoResponse> {
        @Override
        protected BingoResponse doInBackground(Object... params) {
            Log.e("PlayTurn", "sending request");
            return backendService.playTurn((Long) params[0], (Turn) params[1]);
        }

        @Override
        protected void onPostExecute(BingoResponse s) {
            super.onPostExecute(s);
            Log.i("PlayTurn", "next Turn: " + s);
            localGameStatus = MainActivity.Status.started;
            Log.i("PlayTurn", "changing status after playing the turn " + localGameStatus.toString());
        }
    }

    private class JoinRoom extends AsyncTask<String, String, BingoResponse> {
        @Override
        protected BingoResponse doInBackground(String... params) {
            BackendService.name = myName;
            BingoResponse response = backendService.joinRoom(params[0]);
            return response;
        }

        @Override
        protected void onPostExecute(BingoResponse response) {
            super.onPostExecute(response);
            if (response == null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        localGameStatus = MainActivity.Status.completed;
                        Toast.makeText(MainActivity.this, "Could not join the room!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            } else {
                room = response.getRoom();
                updateMySelfInPlayerGrid(response.getPlayers().get(response.getPlayers().size() - 1));
                allPlayers.addAll(response.getPlayers());
                playerGettingUpdated = true;
                updatePlayersGrid();
                localGameStatus = MainActivity.Status.joining;
                Toast.makeText(MainActivity.this, "Game joined successfully", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class StartGame extends AsyncTask<String, String, BingoResponse> {
        @Override
        protected BingoResponse doInBackground(String... params) {
            BingoResponse response = backendService.startGame(params[0]);
            return response;
        }

        @Override
        protected void onPostExecute(BingoResponse response) {
            super.onPostExecute(response);
            if (response == null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        localGameStatus = MainActivity.Status.completed;
                        Toast.makeText(MainActivity.this, "Could not start the game!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Game started!!!", Toast.LENGTH_SHORT).show();
                }
            });
            isMyTurn = true;
            localGameStatus = MainActivity.Status.started;
            unFreeze();
        }
    }

    private class Polling extends Thread {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void run() {
            Looper.prepare();
            while (!localGameStatus.equals(Status.completed)) {
                goToSleepMode();
                BingoResponse response = backendService.polling(room.getId().toString());
                room = response.getRoom();
                updatePlayerInfo(response.getPlayers());
                alertGameStarted();
                if (checkIfSomebodyWon()) break;
                updateColorForTurn();
                updateGamePlay();
            }
            Log.i("Polling", "stopped" + localGameStatus.toString());
        }

        private void alertGameStarted() {
            if (localGameStatus.equals(Status.joining) && room.getStatus().equals("started")) {
                localGameStatus = Status.started;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Game started!!!", Toast.LENGTH_SHORT).show();
                        vibrateAlert();
                        roomIdToShareBottom.setVisibility(View.GONE);
                    }
                });
            }
        }

        private void goToSleepMode() {
            try {
                while (room == null) {
                    sleep(Constants.POLLING_CYCLE_TIME);
                }
                sleep(Constants.POLLING_CYCLE_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void updateGamePlay() {
            Log.i("polling response", String.format("current step counter: %d, updated step counter: %d",
                    localStepCounter, room.getStepCount()));
            updateGridAfterOtherPlayedTurn();
            if (localGameStatus.equals(Status.started)
                    && room.getTurn().equals(mySelf.getId())
                    && !isMyTurn) {
                isMyTurn = true;
                unFreeze();
                vibrateAlert();
            }
        }

        private void updateGridAfterOtherPlayedTurn() {
            if (room.getStepCount() > localStepCounter) {
                Log.i("Someone played", "Updated the server counter: " + room.getStepCount());
                Position position = positions[room.getLatestStep() - 1];
                Button currentButton = gameGrid.findViewWithTag(String.format(Constants.tagPattern, position.row, position.column));
                isRemoteTurnCompleted = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        playSingleTurn(currentButton, position);
                    }
                });
                while (!isRemoteTurnCompleted) {
                    Log.i("isRemoteTurnCompleted ", "Waiting for winner update");
                    goToSleepMode();
                }
                if (isBingoCompleted()) {
                    //Handle draw conditions as well on the server side!!!
                    //update the server when somebody else played and i won!!!
                    new PlayTurn().execute(room.getId(), new Turn(mySelf.getId(), alreadyPlayedButton(),
                            true));
                }
            }
        }

        public void updateColorForTurn() {
            if (playerGettingUpdated || localGameStatus.equals(Status.initializing)) {
                return;
            }

            String turn = room.getTurn().toString();
            Iterator<Player> iterator = allPlayers.iterator();
            while (iterator.hasNext()) {
                Player player = iterator.next();
                if (player.getId().toString().equals(turn)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Button thisButton = (Button) playerNameGrid.findViewWithTag(player.getId().toString());
                            thisButton.setBackgroundTintMode(PorterDuff.Mode.DARKEN);
                            thisButton.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.RED, Color.CYAN}));
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Button thisButton = (Button) playerNameGrid.findViewWithTag(player.getId().toString());
                            thisButton.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                            thisButton.setTextColor(Color.BLACK);
                        }
                    });
                }
            }
        }

        private int alreadyPlayedButton() {
            for (int i = 0; i < 25; i++) {
                Position position = positions[i];
                if (crossed[position.row][position.column]) {
                    return Integer.parseInt(((Button) gameGrid.findViewWithTag(String.format(Constants.tagPattern, position.row, position.column)))
                            .getText().toString());
                }
            }
            return 0;
        }

        private boolean checkIfSomebodyWon() {
            if (room.getWinner() != null) {
                Log.i("daemon", "Somebody won checking who: " + allPlayers.size());
                localGameStatus = Status.completed;
                for (final Player player : allPlayers) {
                    Log.i("Polling", "checking winner: " + player.getId() + " " + (room.getWinner()));
                    if (player.getId().equals(room.getWinner())) {
                        Log.i("Polling", player.getName() + " Won");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                popWinnerAlert(player.equals(mySelf) ? "You" : player.getName());
                            }
                        });
                        break;
                    }
                }
                return true;
            }
            return false;
        }

        private void updatePlayerInfo(List<Player> players) {
            if (!playerGettingUpdated && room.getStatus().equals("created")
                    && players != null) {
                playerGettingUpdated = true;
                Log.i("polling", "updating players");
                allPlayers.addAll(players);
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        Log.i("polling", "Updating player grid");
                        updatePlayersGrid();
                    }
                };
                thread.setPriority(Thread.MAX_PRIORITY);
                runOnUiThread(thread);
            }
        }
    }
}