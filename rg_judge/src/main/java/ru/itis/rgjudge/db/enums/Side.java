package ru.itis.rgjudge.db.enums;

public enum Side {
    RIGHT,
    LEFT;

    public Side getOpposite() {
        return this.equals(LEFT) ? RIGHT : LEFT;
    }
}
