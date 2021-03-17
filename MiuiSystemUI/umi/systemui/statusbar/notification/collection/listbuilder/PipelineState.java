package com.android.systemui.statusbar.notification.collection.listbuilder;

public class PipelineState {
    private int mState = 0;

    public boolean is(int i) {
        return i == this.mState;
    }

    public int getState() {
        return this.mState;
    }

    public void setState(int i) {
        this.mState = i;
    }

    public void incrementTo(int i) {
        if (this.mState == i - 1) {
            this.mState = i;
            return;
        }
        throw new IllegalStateException("Cannot increment from state " + this.mState + " to state " + i);
    }

    public void requireState(int i) {
        if (i != this.mState) {
            throw new IllegalStateException("Required state is <" + i + " but actual state is " + this.mState);
        }
    }

    public void requireIsBefore(int i) {
        if (this.mState >= i) {
            throw new IllegalStateException("Required state is <" + i + " but actual state is " + this.mState);
        }
    }
}
