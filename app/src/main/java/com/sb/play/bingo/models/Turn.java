package com.sb.play.bingo.models;

public class Turn {

    private Long id;
    private int number;
    private boolean won;

    public Turn(Long id, int number, boolean won) {
        this.id = id;
        this.number = number;
        this.won = won;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isWon() {
        return won;
    }

    public void setWon(boolean won) {
        this.won = won;
    }
}
