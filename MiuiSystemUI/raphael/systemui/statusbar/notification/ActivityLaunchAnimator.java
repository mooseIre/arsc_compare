package com.android.systemui.statusbar.notification;

import android.util.MathUtils;
import android.view.RemoteAnimationAdapter;
import android.view.View;
import com.android.internal.policy.ScreenDecorationsUtils;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.NotificationShadeDepthController;
import com.android.systemui.statusbar.notification.stack.NotificationListContainer;
import com.android.systemui.statusbar.phone.NotificationPanelViewController;
import com.android.systemui.statusbar.phone.NotificationShadeWindowViewController;
import java.util.concurrent.Executor;

public class ActivityLaunchAnimator {
    private boolean mAnimationPending;
    protected boolean mAnimationRunning;
    private Callback mCallback;
    private boolean mIsLaunchForActivity;
    private final NotificationShadeWindowViewController mNotificationShadeWindowViewController;
    private final Runnable mTimeoutRunnable = new Runnable() {
        /* class com.android.systemui.statusbar.notification.$$Lambda$ActivityLaunchAnimator$l5Gj6YM2XO6z1WFQpGTriWePKVk */

        public final void run() {
            ActivityLaunchAnimator.this.lambda$new$0$ActivityLaunchAnimator();
        }
    };

    public interface Callback {
        boolean areLaunchAnimationsEnabled();

        void onExpandAnimationFinished(boolean z);

        void onExpandAnimationTimedOut();

        void onLaunchAnimationCancelled();
    }

    public abstract RemoteAnimationAdapter getLaunchAnimation(View view, boolean z);

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$ActivityLaunchAnimator() {
        setAnimationPending(false);
        this.mCallback.onExpandAnimationTimedOut();
    }

    public ActivityLaunchAnimator(NotificationShadeWindowViewController notificationShadeWindowViewController, Callback callback, NotificationPanelViewController notificationPanelViewController, NotificationShadeDepthController notificationShadeDepthController, NotificationListContainer notificationListContainer, Executor executor) {
        this.mNotificationShadeWindowViewController = notificationShadeWindowViewController;
        this.mCallback = callback;
        ScreenDecorationsUtils.getWindowCornerRadius(notificationShadeWindowViewController.getView().getResources());
    }

    public boolean isAnimationPending() {
        return this.mAnimationPending;
    }

    public void setLaunchResult(int i, boolean z) {
        this.mIsLaunchForActivity = z;
        setAnimationPending((i == 2 || i == 0) && this.mCallback.areLaunchAnimationsEnabled());
    }

    public boolean isLaunchForActivity() {
        return this.mIsLaunchForActivity;
    }

    /* access modifiers changed from: protected */
    public void setAnimationPending(boolean z) {
        this.mAnimationPending = z;
        this.mNotificationShadeWindowViewController.setExpandAnimationPending(z);
        if (z) {
            this.mNotificationShadeWindowViewController.getView().postDelayed(this.mTimeoutRunnable, 500);
        } else {
            this.mNotificationShadeWindowViewController.getView().removeCallbacks(this.mTimeoutRunnable);
        }
    }

    public boolean isAnimationRunning() {
        return this.mAnimationRunning;
    }

    public static class ExpandAnimationParameters {
        int bottom;
        int left;
        public float linearProgress;
        int parentStartClipTopAmount;
        int right;
        int startClipTopAmount;
        int[] startPosition;
        float startTranslationZ;
        int top;

        public int getTop() {
            return this.top;
        }

        public int getBottom() {
            return this.bottom;
        }

        public int getWidth() {
            return this.right - this.left;
        }

        public int getHeight() {
            return this.bottom - this.top;
        }

        public int getTopChange() {
            int i = this.startClipTopAmount;
            return Math.min((this.top - this.startPosition[1]) - (((float) i) != 0.0f ? (int) MathUtils.lerp(0.0f, (float) i, Interpolators.FAST_OUT_SLOW_IN.getInterpolation(this.linearProgress)) : 0), 0);
        }

        public float getProgress() {
            return this.linearProgress;
        }

        public float getProgress(long j, long j2) {
            return MathUtils.constrain(((this.linearProgress * 400.0f) - ((float) j)) / ((float) j2), 0.0f, 1.0f);
        }

        public int getStartClipTopAmount() {
            return this.startClipTopAmount;
        }

        public int getParentStartClipTopAmount() {
            return this.parentStartClipTopAmount;
        }

        public float getStartTranslationZ() {
            return this.startTranslationZ;
        }
    }
}
