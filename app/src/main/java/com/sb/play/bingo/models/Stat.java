package com.sb.play.bingo.models;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Stat {
    private String room;
    private String winner;
    private List<String> players;
    private Date time;

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Stat(String room, String players, String winner, Long time) {
        this.room = room;
        this.players = Arrays.asList(players.split(","));
        this.winner = winner;
        this.time = new Date(time);
    }

    @Override
    public String toString() {
        return "Stat{" +
                "room='" + room + '\'' +
                ", winner='" + winner + '\'' +
                ", players=" + players +
                ", time=" + time +
                '}';
    }
}
