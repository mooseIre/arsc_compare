package com.android.systemui.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.view.View;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.statusbar.CommandQueue;
import java.util.List;
import java.util.function.Consumer;

public class Utils {
    public static boolean useQsMediaPlayer(Context context) {
        return true;
    }

    public static <T> void safeForeach(List<T> list, Consumer<T> consumer) {
        for (int size = list.size() - 1; size >= 0; size--) {
            T t = list.get(size);
            if (t != null) {
                consumer.accept(t);
            }
        }
    }

    public static class DisableStateTracker implements CommandQueue.Callbacks, View.OnAttachStateChangeListener {
        private final CommandQueue mCommandQueue;
        private boolean mDisabled;
        private final int mMask1;
        private final int mMask2;
        private View mView;

        public DisableStateTracker(int i, int i2, CommandQueue commandQueue) {
            this.mMask1 = i;
            this.mMask2 = i2;
            this.mCommandQueue = commandQueue;
        }

        public void onViewAttachedToWindow(View view) {
            this.mView = view;
            this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
        }

        public void onViewDetachedFromWindow(View view) {
            this.mCommandQueue.removeCallback((CommandQueue.Callbacks) this);
            this.mView = null;
        }

        @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
        public void disable(int i, int i2, int i3, boolean z) {
            if (i == this.mView.getDisplay().getDisplayId()) {
                int i4 = this.mMask1 & i2;
                int i5 = 0;
                boolean z2 = (i4 == 0 && (this.mMask2 & i3) == 0) ? false : true;
                if (z2 != this.mDisabled) {
                    this.mDisabled = z2;
                    View view = this.mView;
                    if (z2) {
                        i5 = 8;
                    }
                    view.setVisibility(i5);
                }
            }
        }
    }

    public static boolean isHeadlessRemoteDisplayProvider(PackageManager packageManager, String str) {
        if (packageManager.checkPermission("android.permission.REMOTE_DISPLAY_PROVIDER", str) != 0) {
            return false;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setPackage(str);
        return packageManager.queryIntentActivities(intent, 0).isEmpty();
    }

    public static boolean isGesturalModeOnDefaultDisplay(Context context, int i) {
        return context.getDisplayId() == 0 && QuickStepContract.isGesturalMode(i);
    }

    public static boolean useMediaResumption(Context context) {
        int i = Settings.Secure.getInt(context.getContentResolver(), "qs_media_resumption", 1);
        if (!useQsMediaPlayer(context) || i <= 0) {
            return false;
        }
        return true;
    }
}
