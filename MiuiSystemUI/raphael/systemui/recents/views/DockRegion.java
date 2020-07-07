package com.android.systemui.recents.views;

import com.android.systemui.recents.model.TaskStack;

/* compiled from: RecentsViewTouchHandler */
class DockRegion {
    public static TaskStack.DockState[] PHONE_LANDSCAPE = {TaskStack.DockState.LEFT};
    public static TaskStack.DockState[] PHONE_PORTRAIT = {TaskStack.DockState.TOP};
    public static TaskStack.DockState[] PHONE_PORTRAIT_FORCE_BLACK = {TaskStack.DockState.TOP_FORCE_BLACK};
    public static TaskStack.DockState[] TABLET_LANDSCAPE = {TaskStack.DockState.LEFT};
    public static TaskStack.DockState[] TABLET_PORTRAIT = PHONE_PORTRAIT;
}
