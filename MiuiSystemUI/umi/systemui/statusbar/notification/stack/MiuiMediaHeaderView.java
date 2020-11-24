package com.android.systemui.statusbar.notification.stack;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import com.android.systemui.C0012R$id;
import com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin;
import com.android.systemui.statusbar.notification.mediacontrol.MiuiMediaScrollView;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiMediaHeaderView.kt */
public final class MiuiMediaHeaderView extends MediaHeaderView implements SwipeableView {
    private MiuiMediaScrollView mScrollView;

    @Nullable
    public NotificationMenuRowPlugin createMenu() {
        return null;
    }

    public boolean hasFinishedInitialization() {
        return true;
    }

    public MiuiMediaHeaderView(@Nullable Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void setContentView(@Nullable ViewGroup viewGroup) {
        super.setContentView(viewGroup);
        this.mScrollView = (MiuiMediaScrollView) findViewById(C0012R$id.media_carousel_scroller);
    }

    public final boolean canMediaScrollHorizontally(int i) {
        MiuiMediaScrollView miuiMediaScrollView = this.mScrollView;
        if (miuiMediaScrollView != null) {
            return miuiMediaScrollView.canScrollHorizontally(i);
        }
        return false;
    }

    public void setVisibility(int i) {
        int visibility = getVisibility();
        super.setVisibility(i);
        if (i == 0 && i != visibility) {
            setTransitionAlpha(1.0f);
            resetTranslation();
        }
    }

    public void resetTranslation() {
        setTranslation(0.0f);
    }
}
