package com.android.systemui.statusbar.phone;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.StatusIconDisplayable;
import com.android.systemui.statusbar.phone.StatusBarIconController;

public class MiuiLightDarkIconManager extends StatusBarIconController.IconManager {
    private int mColor;
    private boolean mLight;

    public MiuiLightDarkIconManager(ViewGroup viewGroup, CommandQueue commandQueue, boolean z, int i) {
        super(viewGroup, commandQueue);
        this.mLight = z;
        this.mColor = i;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.StatusBarIconController.IconManager
    public void onIconAdded(int i, String str, boolean z, StatusBarIconHolder statusBarIconHolder) {
        StatusIconDisplayable addHolder = addHolder(i, str, z, statusBarIconHolder);
        Rect rect = new Rect();
        float f = this.mLight ? 0.0f : 1.0f;
        int i2 = this.mColor;
        addHolder.onDarkChanged(rect, f, i2, i2, i2, false);
    }

    public void setLight(boolean z, int i) {
        this.mLight = z;
        this.mColor = i;
        Rect rect = new Rect();
        for (int i2 = 0; i2 < this.mGroup.getChildCount(); i2++) {
            View childAt = this.mGroup.getChildAt(i2);
            if (childAt instanceof StatusIconDisplayable) {
                ((StatusIconDisplayable) childAt).onDarkChanged(rect, z ? 0.0f : 1.0f, i, i, i, false);
            }
        }
    }
}
