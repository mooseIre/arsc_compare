package com.android.systemui.statusbar.notification.row.wrapper;

import android.content.Context;
import android.view.View;
import com.android.internal.graphics.ColorUtils;
import com.android.systemui.C0011R$color;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;

public class NotificationCustomViewWrapper extends NotificationViewWrapper {
    private boolean mIsLegacy;
    private int mLegacyColor;

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public boolean shouldClearBackgroundOnReapply() {
        return false;
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public boolean shouldClipToRounding(boolean z, boolean z2) {
        return true;
    }

    protected NotificationCustomViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
        this.mLegacyColor = expandableNotificationRow.getContext().getColor(C0011R$color.notification_legacy_background_color);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper, com.android.systemui.statusbar.TransformableView
    public void setVisible(boolean z) {
        super.setVisible(z);
        this.mView.setAlpha(z ? 1.0f : 0.0f);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public void onContentUpdated(ExpandableNotificationRow expandableNotificationRow) {
        super.onContentUpdated(expandableNotificationRow);
        if (needsInversion(this.mBackgroundColor, this.mView)) {
            invertViewLuminosity(this.mView);
            float[] fArr = {0.0f, 0.0f, 0.0f};
            ColorUtils.colorToHSL(this.mBackgroundColor, fArr);
            if (this.mBackgroundColor != 0 && ((double) fArr[2]) > 0.5d) {
                fArr[2] = 1.0f - fArr[2];
                this.mBackgroundColor = ColorUtils.HSLToColor(fArr);
            }
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public int getCustomBackgroundColor() {
        int customBackgroundColor = super.getCustomBackgroundColor();
        return (customBackgroundColor != 0 || !this.mIsLegacy) ? customBackgroundColor : this.mLegacyColor;
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public void setLegacy(boolean z) {
        super.setLegacy(z);
        this.mIsLegacy = z;
    }
}
