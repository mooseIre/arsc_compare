package com.android.systemui.statusbar.notification.mediacontrol;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.android.systemui.media.MediaScrollView;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiMediaScrollView.kt */
public final class MiuiMediaScrollView extends MediaScrollView {
    private int mDownX;

    public MiuiMediaScrollView(@NotNull Context context) {
        this(context, null, 0, 6, null);
    }

    public MiuiMediaScrollView(@NotNull Context context, @Nullable AttributeSet attributeSet) {
        this(context, attributeSet, 0, 4, null);
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public /* synthetic */ MiuiMediaScrollView(Context context, AttributeSet attributeSet, int i, int i2, DefaultConstructorMarker defaultConstructorMarker) {
        this(context, (i2 & 2) != 0 ? null : attributeSet, (i2 & 4) != 0 ? 0 : i);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiMediaScrollView(@NotNull Context context, @Nullable AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        Intrinsics.checkParameterIsNotNull(context, "context");
    }

    @Override // com.android.systemui.media.MediaScrollView
    public boolean onInterceptTouchEvent(@Nullable MotionEvent motionEvent) {
        boolean processTouchEvent = processTouchEvent(motionEvent);
        if (!processTouchEvent) {
            return processTouchEvent;
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    private final boolean processTouchEvent(MotionEvent motionEvent) {
        Integer valueOf = motionEvent != null ? Integer.valueOf(motionEvent.getAction()) : null;
        if (valueOf == null || valueOf.intValue() != 0) {
            return valueOf == null || valueOf.intValue() != 2 || canScrollHorizontally(this.mDownX - ((int) motionEvent.getX()));
        }
        this.mDownX = (int) motionEvent.getX();
        return true;
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int scrollX = getScrollX();
        super.onLayout(z, i, i2, i3, i4);
        if (scrollX != getScrollX()) {
            scrollTo(scrollX, 0);
        }
    }
}
