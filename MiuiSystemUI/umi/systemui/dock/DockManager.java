package com.android.systemui.dock;

public interface DockManager {

    public interface AlignmentStateListener {
    }

    public interface DockEventListener {
    }

    void addAlignmentStateListener(AlignmentStateListener alignmentStateListener);

    void addListener(DockEventListener dockEventListener);

    boolean isDocked();

    boolean isHidden();

    void removeListener(DockEventListener dockEventListener);
}
