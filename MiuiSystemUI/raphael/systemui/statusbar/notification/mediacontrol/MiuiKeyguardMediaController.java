package com.android.systemui.statusbar.notification.mediacontrol;

import android.view.MotionEvent;
import com.android.systemui.media.KeyguardMediaController;
import com.android.systemui.media.MediaHost;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.notification.stack.MediaHeaderView;
import com.android.systemui.statusbar.notification.stack.MiuiMediaHeaderView;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiKeyguardMediaController.kt */
public final class MiuiKeyguardMediaController extends KeyguardMediaController {
    private float mInitialTouchX;
    private float mInitialTouchY;
    private boolean mIsDownInMediaHeaderView;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiKeyguardMediaController(@NotNull MediaHost mediaHost, @NotNull KeyguardBypassController keyguardBypassController, @NotNull SysuiStatusBarStateController sysuiStatusBarStateController, @NotNull NotificationLockscreenUserManager notificationLockscreenUserManager) {
        super(mediaHost, keyguardBypassController, sysuiStatusBarStateController, notificationLockscreenUserManager);
        Intrinsics.checkParameterIsNotNull(mediaHost, "mediaHost");
        Intrinsics.checkParameterIsNotNull(keyguardBypassController, "bypassController");
        Intrinsics.checkParameterIsNotNull(sysuiStatusBarStateController, "statusBarStateController");
        Intrinsics.checkParameterIsNotNull(notificationLockscreenUserManager, "notifLockscreenUserManager");
    }

    public final boolean onMediaControlIntercept(@NotNull MotionEvent motionEvent) {
        Intrinsics.checkParameterIsNotNull(motionEvent, "ev");
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.mInitialTouchX = motionEvent.getRawX();
            this.mInitialTouchY = motionEvent.getRawY();
            this.mIsDownInMediaHeaderView = false;
            if (isDownEventInMediaHeaderView()) {
                this.mIsDownInMediaHeaderView = true;
            }
        } else if (actionMasked != 1) {
            if (actionMasked == 2 && shouldInterceptTouchEvent(motionEvent)) {
                return true;
            }
        } else if (shouldInterceptTouchEvent(motionEvent)) {
            return true;
        }
        return false;
    }

    private final boolean isDownEventInMediaHeaderView() {
        if (getView() == null) {
            return false;
        }
        int[] iArr = {0, 0};
        MediaHeaderView view = getView();
        if (view != null) {
            view.getLocationOnScreen(iArr);
            float f = this.mInitialTouchX;
            if (f < ((float) iArr[0])) {
                return false;
            }
            int i = iArr[0];
            MediaHeaderView view2 = getView();
            if (view2 == null) {
                Intrinsics.throwNpe();
                throw null;
            } else if (f > ((float) (i + view2.getWidth()))) {
                return false;
            } else {
                float f2 = this.mInitialTouchY;
                if (f2 < ((float) iArr[1])) {
                    return false;
                }
                int i2 = iArr[1];
                MediaHeaderView view3 = getView();
                if (view3 == null) {
                    Intrinsics.throwNpe();
                    throw null;
                } else if (f2 <= ((float) (i2 + view3.getHeight()))) {
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            Intrinsics.throwNpe();
            throw null;
        }
    }

    private final boolean shouldInterceptTouchEvent(MotionEvent motionEvent) {
        if (this.mIsDownInMediaHeaderView) {
            int i = this.mInitialTouchX - motionEvent.getRawX() >= ((float) 0) ? 1 : -1;
            if (getView() instanceof MiuiMediaHeaderView) {
                MediaHeaderView view = getView();
                if (view == null) {
                    throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.statusbar.notification.stack.MiuiMediaHeaderView");
                } else if (((MiuiMediaHeaderView) view).canMediaScrollHorizontally(i)) {
                    return true;
                }
            }
        }
        return false;
    }
}
