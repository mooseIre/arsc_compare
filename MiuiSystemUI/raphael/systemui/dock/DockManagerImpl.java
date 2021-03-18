package com.android.systemui.dock;

import com.android.systemui.dock.DockManager;

public class DockManagerImpl implements DockManager {
    @Override // com.android.systemui.dock.DockManager
    public void addAlignmentStateListener(DockManager.AlignmentStateListener alignmentStateListener) {
    }

    @Override // com.android.systemui.dock.DockManager
    public void addListener(DockManager.DockEventListener dockEventListener) {
    }

    @Override // com.android.systemui.dock.DockManager
    public boolean isDocked() {
        return false;
    }

    @Override // com.android.systemui.dock.DockManager
    public boolean isHidden() {
        return false;
    }

    @Override // com.android.systemui.dock.DockManager
    public void removeListener(DockManager.DockEventListener dockEventListener) {
    }
}
