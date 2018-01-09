package com.example.sonia.asystentgotowania.onerecipe.reading;

import java.util.Observable;

public class MyObservableInteger extends Observable {
    private int value;

    MyObservableInteger(int value) {
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
