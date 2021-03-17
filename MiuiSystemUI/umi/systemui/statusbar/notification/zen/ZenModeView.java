package com.android.systemui.statusbar.notification.zen;

import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.stack.SwipeableView;

public class ZenModeView extends ExpandableView implements SwipeableView {
    private final String CONTENT_ALL_TIME = getResources().getString(C0021R$string.zen_mode_warnings_all_time_content);
    private final String CONTENT_KEYGUARD = getResources().getString(C0021R$string.zen_mode_warnings_keyguard_content);
    private ViewGroup mContent;
    public ZenModeViewController mController;

    @Override // com.android.systemui.statusbar.notification.stack.SwipeableView
    public NotificationMenuRowPlugin createMenu() {
        return null;
    }

    public boolean getCanSwipe() {
        return true;
    }

    @Override // com.android.systemui.statusbar.notification.stack.SwipeableView
    public boolean hasFinishedInitialization() {
        return true;
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public void performAddAnimation(long j, long j2, boolean z) {
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public long performRemoveAnimation(long j, long j2, float f, boolean z, float f2, Runnable runnable, AnimatorListenerAdapter animatorListenerAdapter) {
        return 0;
    }

    public ZenModeView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        ViewGroup viewGroup = (ViewGroup) findViewById(C0015R$id.content);
        this.mContent = viewGroup;
        viewGroup.setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.statusbar.notification.zen.$$Lambda$ZenModeView$_6oqLxuLrd6dpOtYMGRgCl0UadQ */

            public final void onClick(View view) {
                ZenModeView.this.lambda$onFinishInflate$0$ZenModeView(view);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onFinishInflate$0 */
    public /* synthetic */ void lambda$onFinishInflate$0$ZenModeView(View view) {
        ZenModeViewController zenModeViewController = this.mController;
        if (zenModeViewController != null) {
            zenModeViewController.jump2Settings();
        }
    }

    public void setController(ZenModeViewController zenModeViewController) {
        this.mController = zenModeViewController;
    }

    public void loadOrReleaseContent(int i) {
        ViewGroup viewGroup = this.mContent;
        if (viewGroup != null) {
            if (i == 0) {
                if (viewGroup.getChildCount() == 0) {
                    loadContentViews();
                }
                resetContentText();
            } else if (viewGroup.getChildCount() != 0) {
                this.mContent.removeAllViews();
            }
        }
    }

    private void loadContentViews() {
        ((TextView) LayoutInflater.from(getContext()).inflate(C0017R$layout.item_zen_mode, this.mContent, true).findViewById(C0015R$id.zen_quit)).setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.statusbar.notification.zen.$$Lambda$ZenModeView$IX1IT9wvL5wir1_ziDkIeeeOlqA */

            public final void onClick(View view) {
                ZenModeView.this.lambda$loadContentViews$1$ZenModeView(view);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$loadContentViews$1 */
    public /* synthetic */ void lambda$loadContentViews$1$ZenModeView(View view) {
        ZenModeViewController zenModeViewController = this.mController;
        if (zenModeViewController != null) {
            zenModeViewController.setZenOff();
        }
    }

    public void resetContentText() {
        int i = Settings.System.getInt(getContext().getContentResolver(), "zen_mode_intercepted_when_unlocked", -1);
        if (i == -1) {
            Log.e("ZenModeView", "resetContentText: unable to get KEY_ZEN_MODE_INTERCEPT_SCENE");
            return;
        }
        TextView textView = (TextView) findViewById(C0015R$id.zen_content);
        if (textView != null) {
            textView.setText(i == 1 ? this.CONTENT_ALL_TIME : this.CONTENT_KEYGUARD);
        }
    }

    public void reInflate() {
        ViewGroup viewGroup = this.mContent;
        if (viewGroup != null && viewGroup.getChildCount() != 0) {
            this.mContent.removeAllViews();
            loadContentViews();
            resetContentText();
        }
    }

    @Override // com.android.systemui.statusbar.notification.stack.SwipeableView
    public void resetTranslation() {
        setTranslation(0.0f);
        setTransitionAlpha(1.0f);
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView, com.android.systemui.statusbar.notification.stack.SwipeableView
    public void setTranslation(float f) {
        setTranslationX(f);
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView, com.android.systemui.statusbar.notification.stack.SwipeableView
    public float getTranslation() {
        return getTranslationX();
    }
}
