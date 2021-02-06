package com.sb.play.bingo.services;

import android.util.Log;

import com.sb.play.bingo.models.BingoResponse;
import com.sb.play.bingo.models.Request;
import com.sb.play.bingo.models.Room;
import com.sb.play.bingo.models.Turn;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class BackendService {

    public static final String URL = "http://bingobackendservice-env.eba-3ymgj4jd.us-east-2.elasticbeanstalk.com/v1/room";
    public static String name;
    RestTemplate restTemplate;

    public BackendService() {

        List<HttpMessageConverter<?>> httpMessageConverters = new ArrayList<>();
        httpMessageConverters.add(new ByteArrayHttpMessageConverter());
        httpMessageConverters.add(new StringHttpMessageConverter());
        httpMessageConverters.add(new ResourceHttpMessageConverter());

        httpMessageConverters.add(new MappingJackson2HttpMessageConverter());
        restTemplate = new RestTemplate(true);
    }

    public BingoResponse createRoom() {
        try {
            ResponseEntity<BingoResponse> response = restTemplate.postForEntity(URL, new Request(name), BingoResponse.class);
            return response.getBody();
        } catch (Exception e) {
            Log.e("Error creating room", "createRoom: failed");
            Log.e("error", "" + e);
            e.printStackTrace();
        }
        return null;
    }

    public BingoResponse getLatestRoomInfo(Room room) {
        ResponseEntity<BingoResponse> response = restTemplate.getForEntity(URL + "1" + room.getId(), BingoResponse.class);
        return response.getBody();
    }

    public BingoResponse joinRoom(String roomId) {
        try {
            ResponseEntity<BingoResponse> response = restTemplate.postForEntity(URL + "/" + roomId + "/join", new Request(name), BingoResponse.class);
            return response.getBody();
        } catch (Exception e) {
            Log.e("Error", "joinRoom:failed " + e.getLocalizedMessage());
            e.getLocalizedMessage();
        }
        return null;
    }

    public BingoResponse playTurn(Long roomId, Turn turn) {
        try {
            ResponseEntity<BingoResponse> response = restTemplate.postForEntity(URL + "/" + roomId + "/turn", turn, BingoResponse.class);
            return response.getBody();
        } catch (Exception e) {
            Log.e("failed play turn", "playTurn: " + e.getLocalizedMessage());
        }
        return null;
    }

    public BingoResponse polling(String roomId) {
        try {
            ResponseEntity<BingoResponse> response = restTemplate.getForEntity(URL + "/" + roomId, BingoResponse.class);
            return response.getBody();
        } catch (Exception e) {
        }
        return null;
    }

    public BingoResponse startGame(String roomId) {
        try {
            Log.i("startGame", "startGame: " + roomId);
            ResponseEntity<BingoResponse> response = restTemplate.postForEntity(URL + "/" + roomId + "/start",
                    null, BingoResponse.class);
            return response.getBody();
        } catch (Exception e) {
            Log.e("Error", "could not start game", e.getCause());
            e.printStackTrace();
        }
        return null;
    }
}
