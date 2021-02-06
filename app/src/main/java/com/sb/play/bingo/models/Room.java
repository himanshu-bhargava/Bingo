package com.sb.play.bingo.models;

import java.io.Serializable;

public class Room implements Serializable {

    private Long id;
    private Integer latestStep;
    private Integer stepCount;
    private Long turn;
    private String status;
    private Long winner;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getLatestStep() {
        return latestStep;
    }

    public void setLatestStep(Integer latestStep) {
        this.latestStep = latestStep;
    }

    public Integer getStepCount() {
        return stepCount;
    }

    public void setStepCount(Integer stepCount) {
        this.stepCount = stepCount;
    }

    public Long getTurn() {
        return turn;
    }

    public void setTurn(Long turn) {
        this.turn = turn;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getWinner() {
        return winner;
    }

    public void setWinner(Long winner) {
        this.winner = winner;
    }

}
