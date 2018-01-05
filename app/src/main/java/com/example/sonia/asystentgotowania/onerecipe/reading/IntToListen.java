package com.example.sonia.asystentgotowania.onerecipe.reading;

import java.util.Observable;

public class IntToListen extends Observable {
    private int value;

    IntToListen(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    void setValue(int value) {
        this.value = value;
        setChanged();
        notifyObservers(value);
    }
}
