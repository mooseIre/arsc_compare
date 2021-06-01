package com.android.systemui.statusbar.phone;

import android.widget.LinearLayout;
import com.android.systemui.C0008R$array;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.StatusBarMobileView;
import java.util.ArrayList;
import java.util.Arrays;

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
            updateController();
        }
    }

    /* access modifiers changed from: protected */
    public void updateController() {
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).removeIconGroup(this);
        if (!this.mAttached) {
            return;
        }
        if (this.mDripEnd) {
            ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(this, new ArrayList(Arrays.asList(this.mContext.getResources().getStringArray(C0008R$array.config_drip_right_block_statusBarIcons))));
            return;
        }
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(this);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.StatusBarIconController.IconManager
    public StatusBarMobileView onCreateStatusBarMobileView(String str) {
        StatusBarMobileView onCreateStatusBarMobileView = super.onCreateStatusBarMobileView(str);
        onCreateStatusBarMobileView.setDrip(this.mDripEnd);
        return onCreateStatusBarMobileView;
    }

    public void attachToWindow() {
        this.mAttached = true;
        updateController();
    }

    public void detachFromWindow() {
        this.mAttached = false;
        updateController();
    }
}
