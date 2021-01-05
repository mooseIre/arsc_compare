package com.android.systemui.plugins.qs;

import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.plugins.FragmentBase;
import com.android.systemui.plugins.annotations.DependsOn;
import com.android.systemui.plugins.annotations.ProvidesInterface;

@DependsOn(target = HeightListener.class)
@ProvidesInterface(action = "com.android.systemui.action.PLUGIN_QS", version = 8)
public interface QS extends FragmentBase {
    public static final String ACTION = "com.android.systemui.action.PLUGIN_QS";
    public static final String TAG = "QS";
    public static final int VERSION = 8;

    @ProvidesInterface(version = 1)
    public interface HeightListener {
        public static final int VERSION = 1;

        void onQsHeightChanged();
    }

    void animateAppearDisappear(boolean z);

    void animateHeaderSlidingIn(long j);

    void animateHeaderSlidingOut();

    void closeDetail();

    int getDesiredHeight();

    View getHeader();

    int getQsMinExpansionHeight();

    void hideImmediately();

    boolean isCustomizing();

    boolean isShowingDetail();

    void notifyCustomizeChanged();

    void setContainer(ViewGroup viewGroup);

    void setDetailAnimatedViews(View... viewArr);

    void setExpandClickListener(View.OnClickListener onClickListener);

    void setExpanded(boolean z);

    void setHasNotifications(boolean z) {
    }

    void setHeaderClickable(boolean z);

    void setHeaderListening(boolean z);

    void setHeightOverride(int i);

    void setListening(boolean z);

    void setOverscrolling(boolean z);

    void setPanelView(HeightListener heightListener);

    void setQsExpansion(float f, float f2);

    void setShowCollapsedOnKeyguard(boolean z) {
    }

    boolean disallowPanelTouches() {
        return isShowingDetail();
    }
}
