package com.android.systemui.statusbar.notification.stack;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin;
import com.android.systemui.statusbar.notification.mediacontrol.MiuiMediaScrollView;
import com.miui.internal.vip.utils.Utils;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiMediaHeaderView.kt */
public final class MiuiMediaHeaderView extends MediaHeaderView implements SwipeableView {
    public static final Companion Companion = new Companion(null);
    private static final int mSidePaddings;
    private MiuiMediaScrollView mScrollView;

    @Override // com.android.systemui.statusbar.notification.stack.SwipeableView
    @Nullable
    public NotificationMenuRowPlugin createMenu() {
        return null;
    }

    @Override // com.android.systemui.statusbar.notification.stack.SwipeableView
    public boolean hasFinishedInitialization() {
        return true;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiMediaHeaderView(@NotNull Context context, @NotNull AttributeSet attributeSet) {
        super(context, attributeSet);
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(attributeSet, "attrs");
    }

    /* compiled from: MiuiMediaHeaderView.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        public final int getMSidePaddings() {
            return MiuiMediaHeaderView.mSidePaddings;
        }
    }

    static {
        Context context = Utils.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context, "Utils.getContext()");
        mSidePaddings = context.getResources().getDimensionPixelSize(C0012R$dimen.notification_side_paddings);
    }

    @Override // com.android.systemui.statusbar.notification.stack.MediaHeaderView
    public void setContentView(@Nullable ViewGroup viewGroup) {
        super.setContentView(viewGroup);
        this.mScrollView = (MiuiMediaScrollView) findViewById(C0015R$id.media_carousel_scroller);
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

    @Override // com.android.systemui.statusbar.notification.stack.SwipeableView
    public void resetTranslation() {
        setTranslation(0.0f);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public void onMeasure(int i, int i2) {
        super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(i) + (mSidePaddings * 2), View.MeasureSpec.getMode(i)), i2);
    }
}
