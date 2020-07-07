package com.android.systemui;

import android.os.Handler;
import android.view.Choreographer;
import com.android.systemui.util.Assert;
import java.util.ArrayList;

public class DejankUtils {
    private static final Runnable sAnimationCallbackRunnable = new Runnable() {
        public void run() {
            for (int i = 0; i < DejankUtils.sPendingRunnables.size(); i++) {
                DejankUtils.sHandler.post((Runnable) DejankUtils.sPendingRunnables.get(i));
            }
            DejankUtils.sPendingRunnables.clear();
        }
    };
    private static final Choreographer sChoreographer = Choreographer.getInstance();
    /* access modifiers changed from: private */
    public static final Handler sHandler = new Handler();
    /* access modifiers changed from: private */
    public static final ArrayList<Runnable> sPendingRunnables = new ArrayList<>();

    public static void postAfterTraversal(Runnable runnable) {
        Assert.isMainThread();
        sPendingRunnables.add(runnable);
        postAnimationCallback();
    }

    public static void removeCallbacks(Runnable runnable) {
        Assert.isMainThread();
        sPendingRunnables.remove(runnable);
        sHandler.removeCallbacks(runnable);
    }

    private static void postAnimationCallback() {
        sChoreographer.postCallback(1, sAnimationCallbackRunnable, (Object) null);
    }
}
