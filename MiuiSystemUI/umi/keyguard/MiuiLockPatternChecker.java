package com.android.keyguard;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Slog;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockscreenCredential;
import com.android.keyguard.analytics.AnalyticsHelper;
import java.util.Objects;

public final class MiuiLockPatternChecker {
    /* access modifiers changed from: private */
    public static String TAG = "miui_keyguard_password";

    public static AsyncTask<?, ?, ?> checkCredentialForUsers(LockPatternUtils lockPatternUtils, LockscreenCredential lockscreenCredential, int i, int i2, Context context, OnCheckForUsersCallback onCheckForUsersCallback, OnCheckForUsersCallback onCheckForUsersCallback2) {
        final Context context2 = context;
        final int i3 = i;
        final OnCheckForUsersCallback onCheckForUsersCallback3 = onCheckForUsersCallback;
        final int i4 = i2;
        final LockPatternUtils lockPatternUtils2 = lockPatternUtils;
        final OnCheckForUsersCallback onCheckForUsersCallback4 = onCheckForUsersCallback2;
        final LockscreenCredential lockscreenCredential2 = lockscreenCredential;
        AnonymousClass1 r0 = new AsyncTask<Void, Void, Boolean>() {
            private int mThrottleTimeout;
            private int mUserIdMatched = -10000;

            /* access modifiers changed from: protected */
            public Boolean doInBackground(Void... voidArr) {
                Boolean bool = Boolean.TRUE;
                Boolean bool2 = Boolean.FALSE;
                AnalyticsHelper.getInstance(context2).trackPageStart("pw_verify_time");
                int i = i3;
                this.mUserIdMatched = i;
                try {
                    if (checkCredential(i, onCheckForUsersCallback3)) {
                        return bool;
                    }
                } catch (LockPatternUtils.RequestThrottledException e) {
                    this.mThrottleTimeout = e.getTimeoutMs();
                } catch (Exception e2) {
                    Slog.e(MiuiLockPatternChecker.TAG, "checkPasswordForUsers failed", e2);
                    AnalyticsHelper.getInstance(context2).record("keyguard_check_password_failed");
                }
                int i2 = i4;
                if (i2 == i3) {
                    return bool2;
                }
                try {
                    if (!MiuiLockPatternChecker.isCredentialEnable(lockPatternUtils2, i2) || !checkCredential(i4, onCheckForUsersCallback4)) {
                        return bool2;
                    }
                    return bool;
                } catch (LockPatternUtils.RequestThrottledException unused) {
                } catch (Exception e3) {
                    Slog.e(MiuiLockPatternChecker.TAG, "checkPasswordForUsers other users failed", e3);
                    AnalyticsHelper.getInstance(context2).record("keyguard_check_password_failed");
                }
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Boolean bool) {
                onCheckForUsersCallback3.onChecked(bool.booleanValue(), this.mUserIdMatched, this.mThrottleTimeout);
            }

            private boolean checkCredential(int i, OnCheckForUsersCallback onCheckForUsersCallback) throws LockPatternUtils.RequestThrottledException {
                LockPatternUtils lockPatternUtils = lockPatternUtils2;
                LockscreenCredential lockscreenCredential = lockscreenCredential2;
                Objects.requireNonNull(onCheckForUsersCallback);
                if (!lockPatternUtils.checkCredential(lockscreenCredential, i, new LockPatternUtils.CheckCredentialProgressCallback() {
                    public final void onEarlyMatched() {
                        OnCheckForUsersCallback.this.onEarlyMatched();
                    }
                })) {
                    return false;
                }
                this.mUserIdMatched = i;
                return true;
            }
        };
        r0.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        return r0;
    }

    /* access modifiers changed from: private */
    public static boolean isCredentialEnable(LockPatternUtils lockPatternUtils, int i) {
        int activePasswordQuality = lockPatternUtils.getActivePasswordQuality(i);
        boolean z = activePasswordQuality == 65536 || activePasswordQuality == 262144 || activePasswordQuality == 327680 || activePasswordQuality == 393216 || activePasswordQuality == 131072 || activePasswordQuality == 196608;
        String str = TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("check credential enable for userId : ");
        sb.append(i);
        sb.append(z ? "   enable" : "   disable");
        Log.d(str, sb.toString());
        return z;
    }
}
