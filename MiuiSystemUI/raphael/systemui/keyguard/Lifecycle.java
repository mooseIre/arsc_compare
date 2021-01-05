package com.android.systemui.keyguard;

import java.util.ArrayList;
import java.util.function.Consumer;

public class Lifecycle<T> {
    private ArrayList<T> mObservers = new ArrayList<>();

    public void addObserver(T t) {
        this.mObservers.add(t);
    }

    public void removeObserver(T t) {
        this.mObservers.remove(t);
    }

    public void dispatch(Consumer<T> consumer) {
        for (int i = 0; i < this.mObservers.size(); i++) {
            consumer.accept(this.mObservers.get(i));
        }
    }
}
