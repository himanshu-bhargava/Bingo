package com.sb.play.bingo.models;

public class Position {
    final private int row;
    final private int column;

    public Position(String tag) {
        String[] position = tag.split(",");
        this.row = Integer.parseInt(position[0]);
        this.column = Integer.parseInt(position[1]);
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }
}
