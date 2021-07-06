package com.android.systemui.statusbar.phone;

import android.view.View;
import android.widget.LinearLayout;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.MiuiStatusBarIconViewHelper;
import com.android.systemui.statusbar.StatusIconDisplayable;
import com.android.systemui.statusbar.phone.StatusBarIconController;

public class MiuiEndIconManager extends StatusBarIconController.DarkIconManager {
    protected boolean mAttached;
    protected boolean mDripEnd;

    public MiuiEndIconManager(LinearLayout linearLayout, CommandQueue commandQueue, boolean z) {
        super(linearLayout, commandQueue);
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
    @Override // com.android.systemui.statusbar.phone.StatusBarIconController.IconManager, com.android.systemui.statusbar.phone.StatusBarIconController.DarkIconManager
    public void onIconAdded(int i, String str, boolean z, StatusBarIconHolder statusBarIconHolder) {
        StatusIconDisplayable addHolder = addHolder(i, str, z, statusBarIconHolder);
        addHolder.setMiuiBlocked(this.mDripEnd && MiuiStatusBarIconViewHelper.DRIP_END_BLOCKED_LIST.contains(str));
        addHolder.setDrip(this.mDripEnd);
        this.mDarkIconDispatcher.addDarkReceiver(addHolder);
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
