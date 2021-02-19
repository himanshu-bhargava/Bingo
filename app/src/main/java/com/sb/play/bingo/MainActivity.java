package com.sb.play.bingo;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sb.play.asynctasks.JoinRoom;
import com.sb.play.asynctasks.PlayTurn;
import com.sb.play.asynctasks.Polling;
import com.sb.play.asynctasks.StartGame;
import com.sb.play.bingo.models.BingoResponse;
import com.sb.play.bingo.models.Emoji;
import com.sb.play.bingo.models.Player;
import com.sb.play.bingo.models.Position;
import com.sb.play.bingo.models.Room;
import com.sb.play.bingo.models.Status;
import com.sb.play.bingo.models.Turn;
import com.sb.play.bingo.services.BackendService;
import com.sb.play.listeners.SendStickerClickListener;
import com.sb.play.listeners.StickerClickListener;
import com.sb.play.media.AnimationMedia;
import com.sb.play.media.MusicMedia;
import com.sb.play.media.VibrationMedia;
import com.sb.play.util.BingoUtil;
import com.sb.play.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.gridlayout.widget.GridLayout;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity: ";

    private final boolean[][] crossed = new boolean[5][5];
    private final Position[] positions = new Position[25];

    private MusicMedia musicMedia;
    private AnimationMedia animationMedia;
    private VibrationMedia vibrationMedia;

    private BingoResponse bingoResponse;
    private BackendService backendService;

    private int bingoCounter = 0;
    private int gameGridValueCounter = 0;
    private int localStepCounter = 0;
    private Long sendingEmojiTo;
    private String gameType;
    private Player mySelf;
    private volatile Room room;
    private volatile boolean isMyTurn = false;
    private volatile Status localGameStatus = Status.loading;
    private volatile boolean isRemoteTurnCompleted = false;
    private boolean playerGettingUpdated = false;
    private boolean isWinnerAnnounced;
    private Set<Player> allPlayers = Collections.synchronizedSet(new HashSet<>());

    private ConstraintLayout fireWorksLayout;
    private GridLayout gameGrid;
    private GridLayout bingoGrid;
    private LinearLayout playerNameGrid;
    private LinearLayout stickerHolderView;
    private LinearLayout receivedEmojiLayout;
    private Button startOrJoinGame;
    private Button customFill;
    private Button simpleFill;
    private Button randomFill;
    private Button roomIdToShareTop;
    private Intent mainIntent;
    private Button roomIdToShareBottom;
    private TextView winnerAnnounce;
    private TextView senderNameView;
    private ImageView fireworks;
    private ImageView receivedEmojiImageView;
    private HorizontalScrollView stickerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        initialize();
    }

    private void initialize() {
        mainIntent = getIntent();
        initializePrivateVars();
        initializeUiContents();
        initializeSoundsAndEffects();
        initializeStickerView();
        joinGameViaLink();
        onRoomCreation(bingoResponse);
        initiateGrid();
    }

    private void initializePrivateVars() {
        backendService = new BackendService(this);
        bingoResponse = (BingoResponse) mainIntent.getSerializableExtra(Constants.RESPONSE);
        gameType = mainIntent.getStringExtra(Constants.TYPE_OF_GAME);
    }

    private void initializeUiContents() {
        fireworks = findViewById(R.id.fireworks);
        fireWorksLayout = findViewById(R.id.fireWorksLayout);
        receivedEmojiLayout = findViewById(R.id.recievedEmojiLayout);
        stickerView = findViewById(R.id.stickersView);
        senderNameView = findViewById(R.id.senderNameView);
        receivedEmojiImageView = findViewById(R.id.recievedEmojiView);
        stickerHolderView = findViewById(R.id.stickerHolderView);
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
            Toast.makeText(this, R.string.invalid_room_id, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeStickerView() {
        StickerClickListener stickerClickListener = new StickerClickListener(this);
        for (String emojiName : BingoUtil.getEmojiNames(this)) {
            ImageView current = new ImageView(this);
            current.setTag(emojiName);
            current.setImageResource(BingoUtil.getResourceIdForImage(emojiName, this));
            current.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
            current.setScaleType(ImageView.ScaleType.FIT_XY);
            current.setOnClickListener(stickerClickListener);
            stickerHolderView.addView(current);
        }
    }

    private void initializeSoundsAndEffects() {
        musicMedia = new MusicMedia(this);
        vibrationMedia = new VibrationMedia(this);
        animationMedia = new AnimationMedia(this);
    }

    private void initiateGrid() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                Button currentButton = gameGrid.findViewWithTag(String.format("%d,%d", i, j));
                currentButton.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                currentButton.setText("");
                currentButton.setClickable(false);
            }
        }
        startOrJoinGame.setText(Constants.CREATED_ROOM.equals(gameType) ? getString(R.string.start_game) : getString(R.string.join_game));
    }

    private void onRoomCreation(BingoResponse bingoResponse) {
        if (Constants.CREATED_ROOM.equals(gameType)) {
            playerGettingUpdated = true;
            updateMySelfInPlayerGrid(bingoResponse.getPlayers().get(0));
            room = bingoResponse.getRoom();
            allPlayers.add(mySelf);
            startPolling();
        }
        roomIdToShareTop.setText(getString(R.string.room_id) + mainIntent.getStringExtra(Constants.ROOM_ID) + getString(R.string.share));
        roomIdToShareBottom.setText(getString(R.string.room_id) + mainIntent.getStringExtra(Constants.ROOM_ID) + getString(R.string.share));
    }

    public void updateMySelfInPlayerGrid(Player player) {
        mySelf = player;
        mySelf.setName(getString(R.string.you));
        playerNameGrid.addView(createPlayerButton(mySelf));
        playerGettingUpdated = false;
    }

    public void updatePlayersGrid() {
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
        button.setOnClickListener(new SendStickerClickListener(this));
        return button;
    }

    public void startGame(View view) {
        musicMedia.playClick();
        if (Constants.CREATED_ROOM.equals(gameType)) {
            if (allPlayers.size() < 2) {
                Toast.makeText(this, R.string.wait_for_others_to_join, Toast.LENGTH_SHORT).show();
                return;
            }
            new StartGame(this).execute(room.getId().toString());
        } else {
            new JoinRoom(this).execute(mainIntent.getStringExtra(Constants.ROOM_ID));
            startPolling();
        }
        startOrJoinGame.setVisibility(View.INVISIBLE);
        roomIdToShareBottom.setVisibility(View.INVISIBLE);
    }

    public void popWinnerAlert(String name) {
        if (isWinnerAnnounced) return;
        updateStats(name);
        if (getString(R.string.you).equalsIgnoreCase(name)) {
            musicMedia.playWin();
        } else {
            musicMedia.playLost();
        }
        isWinnerAnnounced = true;
        Glide.with(this).load(R.raw.sparkles).into(fireworks);
        fireWorksLayout.setVisibility(View.VISIBLE);
        winnerAnnounce.setText(name + getString(R.string.won));
    }

    private void updateStats(String winner) {
        SQLiteDatabase bingoDatabase = BingoUtil.getDatabase(this);
        try {
            bingoDatabase.execSQL(String.format("INSERT INTO %s VALUES('%s', '%s', '%s', '%s')",
                    Constants.DbConstants.TABLE_NAME,
                    mainIntent.getStringExtra(Constants.ROOM_ID),
                    getAllPlayersNameAsString(),
                    winner, System.currentTimeMillis()));
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
        Thread polling = new Polling(this);
        polling.setDaemon(true);
        polling.start();
        Log.i("starting Polling method", "started polling with daemon");
    }

    public void regularInitialize(View view) {
        musicMedia.playClick();
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
        musicMedia.playClick();
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
        int number = BingoUtil.getRandom(24);
        while (positions[number] != null) {
            number = (number + 1) % 25;
        }
        return number;
    }

    public void userInitialize(View view) {
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
                Toast.makeText(this, R.string.you_cannot_play_turn_now, Toast.LENGTH_SHORT).show();
            } else {
                localGameStatus = Status.playingTurn;
                isMyTurn = false;
                freeze();
                Log.i("onGameGridButtonClick", "Status changed to" + localGameStatus);
                playSingleTurn(thisButton, position);
                new PlayTurn(this).execute(
                        room.getId(),
                        new Turn(mySelf.getId(), Integer.parseInt(thisButton.getText().toString()), isBingoCompleted()));
            }
        }
    }

    public void playSingleTurn(Button thisButton, Position position) {
        musicMedia.playBubble();
        if (crossed[position.getRow()][position.getColumn()]) {
            return;
        }
        localStepCounter++;
        crossed[position.getRow()][position.getColumn()] = true;
        thisButton.setBackgroundTintMode(PorterDuff.Mode.DARKEN);
        animationMedia.animateRotation(thisButton);
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
        if (position.getRow() + position.getColumn() == 4) {
            for (int i = 0; i < 5; i++) {
                if (!isLineCompleted(i, 4 - i)) return;
            }
            checkAndIncrementBingoCounter();
        }
    }

    private void checkDiagonal(Position position) {
        if (position.getRow() == position.getColumn()) {
            for (int i = 0; i < 5; i++) {
                if (!isLineCompleted(i, i)) return;
            }
            checkAndIncrementBingoCounter();
        }
    }

    private void checkColumn(Position position) {
        for (int i = 0; i < 5; i++) {
            if (!isLineCompleted(i, position.getColumn())) return;
        }
        checkAndIncrementBingoCounter();
    }

    private void checkRow(Position position) {
        for (int i = 0; i < 5; i++) {
            if (!isLineCompleted(position.getRow(), i)) return;
        }
        checkAndIncrementBingoCounter();
    }

    public boolean isBingoCompleted() {
        return bingoCounter >= 5;
    }

    private boolean isLineCompleted(int row, int column) {
        return crossed[row][column];
    }

    private void checkAndIncrementBingoCounter() {
        if (!isBingoCompleted()) {
            bingoCounter++;
            Button thisButton = bingoGrid.findViewWithTag(String.valueOf(bingoCounter));
            thisButton.setBackgroundTintMode(PorterDuff.Mode.DARKEN);
            animationMedia.animateRotation(thisButton);
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
        musicMedia.playClick();
        String roomId = mainIntent.getStringExtra(Constants.ROOM_ID);
        Intent sharingIntent = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, String.format(String.valueOf(getString(R.string.room_share_message_format)),
                        "http://com.sb.bingo/joinroom?roomId=", roomId, roomId))
                .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.click_to_join_bingo));
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_via)));
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
        builder.setMessage(R.string.do_you_want_to_exit);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void showReceivedEmoji(Emoji emoji) {
        new CountDownTimer(3000, 3000) {
            public void onTick(long millisUntilFinished) {
                receivedEmojiImageView.setImageResource(
                        BingoUtil.getResourceIdForImage(emoji.getEmoji(), MainActivity.this));
                senderNameView.setText(String.format("%s sent::", getPlayerNameFromId(emoji.getSender())));
                receivedEmojiLayout.setVisibility(View.VISIBLE);
            }

            public void onFinish() {
                receivedEmojiLayout.setVisibility(View.INVISIBLE);
            }
        }.start();
    }

    private String getPlayerNameFromId(Long playerId) {
        String name = null;
        for (Player player : allPlayers) {
            if (player.getId().equals(playerId)) {
                name = player.getName();
                break;
            }
        }
        return (name == null ? "someone" : name).toUpperCase();
    }

    public void setVisibilityForStickerView(boolean show) {
        this.stickerView.setVisibility(show ? View.VISIBLE : View.GONE);
    }


    public synchronized int getLocalStepCounter() {
        return localStepCounter;
    }

    public synchronized Status getLocalGameStatus() {
        return localGameStatus;
    }

    public synchronized void setLocalGameStatus(Status localGameStatus) {
        this.localGameStatus = localGameStatus;
    }

    public synchronized Player getMySelf() {
        return mySelf;
    }

    public LinearLayout getReceivedEmojiLayout() {
        return receivedEmojiLayout;
    }

    public void setSendingEmojiTo(Long sendingEmojiTo) {
        this.sendingEmojiTo = sendingEmojiTo;
    }

    public Long getSendingEmojiTo() {
        return sendingEmojiTo;
    }

    public BackendService getBackendService() {
        return backendService;
    }

    public synchronized Room getRoom() {
        return room;
    }

    public void setMyTurn(boolean myTurn) {
        isMyTurn = myTurn;
    }

    public synchronized void setRoom(Room room) {
        this.room = room;
    }

    public synchronized Set<Player> getAllPlayers() {
        return allPlayers;
    }

    public void setPlayerGettingUpdated(boolean playerGettingUpdated) {
        this.playerGettingUpdated = playerGettingUpdated;
    }

    public VibrationMedia getVibrationMedia() {
        return vibrationMedia;
    }

    public Button getRoomIdToShareBottom() {
        return roomIdToShareBottom;
    }

    public boolean isMyTurn() {
        return isMyTurn;
    }

    public Position[] getPositions() {
        return positions;
    }

    public GridLayout getGameGrid() {
        return gameGrid;
    }

    public void setRemoteTurnCompleted(boolean remoteTurnCompleted) {
        isRemoteTurnCompleted = remoteTurnCompleted;
    }

    public boolean isRemoteTurnCompleted() {
        return isRemoteTurnCompleted;
    }

    public boolean isPlayerGettingUpdated() {
        return playerGettingUpdated;
    }

    public LinearLayout getPlayerNameGrid() {
        return playerNameGrid;
    }

    public boolean[][] getCrossed() {
        return crossed;
    }
}