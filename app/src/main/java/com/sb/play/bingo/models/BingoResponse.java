package com.sb.play.bingo.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BingoResponse implements Serializable {

    private List<Player> players = new ArrayList<>();
    private Room room;

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    @Override
    public String toString() {
        return "BingoResponse{" +
                "players=" + players +
                ", room=" + room +
                '}';
    }
}
