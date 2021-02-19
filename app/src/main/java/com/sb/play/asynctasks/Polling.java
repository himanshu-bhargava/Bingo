package com.sb.play.asynctasks;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sb.play.bingo.MainActivity;
import com.sb.play.bingo.R;
import com.sb.play.bingo.models.BingoResponse;
import com.sb.play.bingo.models.Emoji;
import com.sb.play.bingo.models.Player;
import com.sb.play.bingo.models.Position;
import com.sb.play.bingo.models.Status;
import com.sb.play.bingo.models.Turn;
import com.sb.play.util.Constants;

import java.util.Iterator;
import java.util.List;

import androidx.annotation.RequiresApi;

public class Polling extends Thread {

    private MainActivity context;

    public Polling(MainActivity context) {
        super();
        this.context = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void run() {
        Looper.prepare();
        while (!context.getLocalGameStatus().equals(Status.completed)) {
            goToSleepMode();
            BingoResponse response = context.getBackendService().polling(context.getRoom().getId().toString(),
                    context.getMySelf().getId());
            context.setRoom(response.getRoom());
            displayEmoji(response.getEmoji());
            updatePlayerInfo(response.getPlayers());
            alertGameStarted();
            if (checkIfSomebodyWon()) break;
            updateColorForTurn();
            updateGamePlay();
        }
        Log.i("Polling", "stopped" + context.getLocalGameStatus().toString());
    }

    private void displayEmoji(Emoji emoji) {
        if (emoji == null || !(context.getLocalGameStatus().equals(Status.started) || context.getLocalGameStatus().equals(Status.playingTurn))) {
            return;
        }
        context.runOnUiThread(() -> {
            context.showReceivedEmoji(emoji);
        });
    }

    private void alertGameStarted() {
        if (context.getLocalGameStatus().equals(Status.joining) && context.getRoom().getStatus().equals("started")) {
            context.setLocalGameStatus(Status.started);
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, R.string.game_started, Toast.LENGTH_SHORT).show();
                    context.getVibrationMedia().vibrate();
                    context.getRoomIdToShareBottom().setVisibility(View.GONE);
                }
            });
        }
    }

    private void goToSleepMode() {
        try {
            while (context.getRoom() == null) {
                sleep(Constants.POLLING_CYCLE_TIME);
            }
            sleep(Constants.POLLING_CYCLE_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void updateGamePlay() {
        Log.i("polling response", String.format("current step counter: %d, updated step counter: %d",
                context.getLocalStepCounter(), context.getRoom().getStepCount()));
        updateGridAfterOtherPlayedTurn();
        if (context.getLocalGameStatus().equals(Status.started)
                && context.getRoom().getTurn().equals(context.getMySelf().getId())
                && !context.isMyTurn()) {
            context.setMyTurn(true);
            context.unFreeze();
            context.getVibrationMedia().vibrate();
        }
    }

    private void updateGridAfterOtherPlayedTurn() {
        if (context.getRoom().getStepCount() > context.getLocalStepCounter()) {
            Log.i("Someone played", "Updated the server counter: " + context.getRoom().getStepCount());
            Position position = context.getPositions()[context.getRoom().getLatestStep() - 1];
            Button currentButton = context.getGameGrid().findViewWithTag(String.format(Constants.tagPattern,
                    position.getRow(), position.getColumn()));
            context.setRemoteTurnCompleted(false);
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    context.playSingleTurn(currentButton, position);
                }
            });
            while (!context.isRemoteTurnCompleted()) {
                Log.i("isRemoteTurnCompleted ", "Waiting for winner update");
                goToSleepMode();
            }
            if (context.isBingoCompleted()) {
                //Handle draw conditions as well on the server side!!!
                //update the server when somebody else played and i won!!!
                new PlayTurn(context).execute(context.getRoom().getId(), new Turn(context.getMySelf().getId(), alreadyPlayedButton(),
                        true));
            }
        }
    }

    public void updateColorForTurn() {
        if (context.isPlayerGettingUpdated() || context.getLocalGameStatus().equals(Status.initializing)) {
            return;
        }

        String turn = context.getRoom().getTurn().toString();
        Iterator<Player> iterator = context.getAllPlayers().iterator();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            if (player.getId().toString().equals(turn)) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Button thisButton = context.getPlayerNameGrid().findViewWithTag(player.getId().toString());
                        thisButton.setBackgroundTintMode(PorterDuff.Mode.DARKEN);
                        thisButton.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.RED, Color.CYAN}));
                    }
                });
            } else {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Button thisButton = context.getPlayerNameGrid().findViewWithTag(player.getId().toString());
                        thisButton.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                        thisButton.setTextColor(Color.BLACK);
                    }
                });
            }
        }
    }

    private int alreadyPlayedButton() {
        for (int i = 0; i < 25; i++) {
            Position position = context.getPositions()[i];
            if (context.getCrossed()[position.getRow()][position.getColumn()]) {
                return Integer.parseInt(((Button) context.getGameGrid()
                        .findViewWithTag(String.format(Constants.tagPattern, position.getRow(), position.getColumn())))
                        .getText().toString());
            }
        }
        return 0;
    }

    private boolean checkIfSomebodyWon() {
        if (context.getRoom().getWinner() != null) {
            Log.i("daemon", "Somebody won checking who: " + context.getAllPlayers().size());
            context.setLocalGameStatus(Status.completed);
            for (final Player player : context.getAllPlayers()) {
                Log.i("Polling", "checking winner: " + player.getId() + " " + (context.getRoom().getWinner()));
                if (player.getId().equals(context.getRoom().getWinner())) {
                    Log.i("Polling", player.getName() + " Won");
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            context.popWinnerAlert(player.equals(context.getMySelf()) ?
                                    context.getString(R.string.you) : player.getName());
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
        if (!context.isPlayerGettingUpdated() && context.getRoom().getStatus().equals("created")
                && players != null) {
            context.setPlayerGettingUpdated(true);
            Log.i("polling", "updating players");
            context.getAllPlayers().addAll(players);
            Thread thread = new Thread() {
                @Override
                public void run() {
                    Log.i("polling", "Updating player grid");
                    context.updatePlayersGrid();
                }
            };
            thread.setPriority(Thread.MAX_PRIORITY);
            context.runOnUiThread(thread);
        }
    }
}

