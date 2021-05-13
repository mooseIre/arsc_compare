package com.android.systemui.statusbar.notification.row.wrapper;

import android.content.Context;
import android.view.View;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;

public class MiuiNotificationOneLineViewWrapper extends MiuiNotificationViewWrapper {
    private final boolean mIsTransparentBg;

    protected MiuiNotificationOneLineViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
        this.mIsTransparentBg = "oneLine_transparent".equals(view.getTag());
    }

    public static boolean match(Object obj) {
        return "oneLine".equals(obj) || "oneLine_transparent".equals(obj);
    }

    public boolean isTransparentBg() {
        return this.mIsTransparentBg;
    }
}
