package com.android.systemui.controls;

import android.content.ComponentName;
import android.graphics.drawable.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlStatus.kt */
public interface ControlInterface {

    /* compiled from: ControlStatus.kt */
    public static final class DefaultImpls {
        public static boolean getRemoved(ControlInterface controlInterface) {
            return false;
        }
    }

    @NotNull
    ComponentName getComponent();

    @NotNull
    String getControlId();

    @Nullable
    Icon getCustomIcon();

    int getDeviceType();

    boolean getFavorite();

    boolean getRemoved();

    @NotNull
    CharSequence getSubtitle();

    @NotNull
    CharSequence getTitle();
}
