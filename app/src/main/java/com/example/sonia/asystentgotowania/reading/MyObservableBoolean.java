package com.example.sonia.asystentgotowania.reading;

import java.util.Observable;


public class MyObservableBoolean extends Observable {
    private boolean value;

    MyObservableBoolean(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    void setValue(boolean value) {
        this.value = value;
        setChanged();
        notifyObservers(value);
    }
}