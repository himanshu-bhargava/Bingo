package com.sb.play.bingo.services;

import android.util.Log;

import com.sb.play.bingo.models.BingoResponse;
import com.sb.play.bingo.models.Emoji;
import com.sb.play.bingo.models.Request;
import com.sb.play.bingo.models.Room;
import com.sb.play.bingo.models.Turn;
import com.sb.play.util.BackendParam;
import com.sb.play.util.Constants;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class BackendService {

    public static String name;
    private RestTemplate restTemplate;
    private BackendParam backendParam = new Constants.AwsBackendParam();

    public BackendService() {
        restTemplate = new RestTemplate(true);
    }

    public BingoResponse createRoom() {
        try {
            ResponseEntity<BingoResponse> response = restTemplate
                    .postForEntity(backendParam.getRoomUrl(), new Request(name), BingoResponse.class);
            return response.getBody();
        } catch (Exception e) {
            Log.e("Error creating room", "createRoom: failed");
            Log.e("error", "" + e);
            e.printStackTrace();
        }
        return null;
    }

    public BingoResponse getLatestRoomInfo(Room room) {
        ResponseEntity<BingoResponse> response = restTemplate
                .getForEntity(backendParam.getRoomUrl() + "/" + room.getId(), BingoResponse.class);
        return response.getBody();
    }

    public BingoResponse joinRoom(String roomId) {
        try {
            ResponseEntity<BingoResponse> response = restTemplate
                    .postForEntity(backendParam.getRoomUrl() + "/" + roomId + "/join", new Request(name), BingoResponse.class);
            return response.getBody();
        } catch (Exception e) {
            Log.e("Error", "joinRoom:failed " + e.getLocalizedMessage());
            e.getLocalizedMessage();
        }
        return null;
    }

    public BingoResponse playTurn(Long roomId, Turn turn) {
        try {
            ResponseEntity<BingoResponse> response = restTemplate
                    .postForEntity(backendParam.getRoomUrl() + "/" + roomId + "/turn", turn, BingoResponse.class);
            return response.getBody();
        } catch (Exception e) {
            Log.e("failed play turn", "playTurn: " + e.getLocalizedMessage());
        }
        return null;
    }

    public BingoResponse polling(String roomId, Long myId) {
        try {
            ResponseEntity<BingoResponse> response = restTemplate
                    .getForEntity(backendParam.getRoomUrl() + "/" + roomId +
                            "?myId=" + myId, BingoResponse.class);
            return response.getBody();
        } catch (Exception e) {
            Log.e("Backend error", "Polling failed", e);
        }
        return null;
    }

    public BingoResponse startGame(String roomId) {
        try {
            Log.i("startGame", "startGame: " + roomId);
            ResponseEntity<BingoResponse> response = restTemplate
                    .postForEntity(backendParam.getRoomUrl() + "/" + roomId + "/start", null, BingoResponse.class);
            return response.getBody();
        } catch (Exception e) {
            Log.e("Error", "could not start game", e.getCause());
            e.printStackTrace();
        }
        return null;
    }

    public void sendEmoji(Emoji emoji, long roomId) {
        try {
            restTemplate.postForEntity(backendParam.getEmojiUrl() + "?roomId=" + roomId, emoji, String.class);
        } catch (Exception e) {
            Log.e("Backend Error", "could not send the emoji", e);
        }
    }
}
