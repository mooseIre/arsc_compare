package com.android.systemui.statusbar.phone;

import android.graphics.Rect;
import android.view.View;
import android.widget.LinearLayout;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.MiuiStatusBarIconViewHelper;
import com.android.systemui.statusbar.StatusIconDisplayable;

public class MiuiLightDarkEndIconManager extends MiuiLightDarkIconManager {
    protected boolean mAttached;
    protected boolean mDripEnd;

    public MiuiLightDarkEndIconManager(LinearLayout linearLayout, CommandQueue commandQueue, boolean z, boolean z2, int i) {
        super(linearLayout, commandQueue, z2, i);
        this.mDripEnd = z;
    }

    public void setDripEnd(boolean z) {
        if (this.mDripEnd != z) {
            this.mDripEnd = z;
            updateViewConfig();
        }
    }

    /* access modifiers changed from: protected */
    public void updateViewConfig() {
        int childCount = this.mGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = this.mGroup.getChildAt(i);
            if (childAt instanceof StatusIconDisplayable) {
                StatusIconDisplayable statusIconDisplayable = (StatusIconDisplayable) childAt;
                statusIconDisplayable.setMiuiBlocked(this.mDripEnd && MiuiStatusBarIconViewHelper.DRIP_END_BLOCKED_LIST.contains(statusIconDisplayable.getSlot()));
                statusIconDisplayable.setDrip(this.mDripEnd);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.MiuiLightDarkIconManager, com.android.systemui.statusbar.phone.StatusBarIconController.IconManager
    public void onIconAdded(int i, String str, boolean z, StatusBarIconHolder statusBarIconHolder) {
        StatusIconDisplayable addHolder = addHolder(i, str, z, statusBarIconHolder);
        addHolder.setMiuiBlocked(this.mDripEnd && MiuiStatusBarIconViewHelper.DRIP_END_BLOCKED_LIST.contains(str));
        addHolder.setDrip(this.mDripEnd);
        Rect rect = new Rect();
        float f = this.mLight ? 0.0f : 1.0f;
        int i2 = this.mColor;
        addHolder.onDarkChanged(rect, f, i2, i2, i2, false);
    }

    public void attachToWindow() {
        if (!this.mAttached) {
            this.mAttached = true;
            ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(this);
        }
    }

    public void detachFromWindow() {
        if (this.mAttached) {
            this.mAttached = false;
            ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).removeIconGroup(this);
        }
    }
}
