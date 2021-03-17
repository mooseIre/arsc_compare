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
    private static String TAG = "miui_keyguard_password";

    public static AsyncTask<?, ?, ?> checkCredentialForUsers(final LockPatternUtils lockPatternUtils, final LockscreenCredential lockscreenCredential, final int i, final int i2, final Context context, final OnCheckForUsersCallback onCheckForUsersCallback, final OnCheckForUsersCallback onCheckForUsersCallback2) {
        AnonymousClass1 r8 = new AsyncTask<Void, Void, Boolean>() {
            /* class com.android.keyguard.MiuiLockPatternChecker.AnonymousClass1 */
            private int mThrottleTimeout;
            private int mUserIdMatched = -10000;

            /* access modifiers changed from: protected */
            public Boolean doInBackground(Void... voidArr) {
                Boolean bool = Boolean.TRUE;
                Boolean bool2 = Boolean.FALSE;
                AnalyticsHelper.getInstance(context).trackPageStart("pw_verify_time");
                int i = i;
                this.mUserIdMatched = i;
                try {
                    if (checkCredential(i, onCheckForUsersCallback)) {
                        return bool;
                    }
                } catch (LockPatternUtils.RequestThrottledException e) {
                    this.mThrottleTimeout = e.getTimeoutMs();
                } catch (Exception e2) {
                    Slog.e(MiuiLockPatternChecker.TAG, "checkPasswordForUsers failed", e2);
                    AnalyticsHelper.getInstance(context).record("keyguard_check_password_failed");
                }
                int i2 = i2;
                if (i2 == i) {
                    return bool2;
                }
                try {
                    if (!MiuiLockPatternChecker.isCredentialEnable(lockPatternUtils, i2) || !checkCredential(i2, onCheckForUsersCallback2)) {
                        return bool2;
                    }
                    return bool;
                } catch (LockPatternUtils.RequestThrottledException unused) {
                } catch (Exception e3) {
                    Slog.e(MiuiLockPatternChecker.TAG, "checkPasswordForUsers other users failed", e3);
                    AnalyticsHelper.getInstance(context).record("keyguard_check_password_failed");
                }
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Boolean bool) {
                onCheckForUsersCallback.onChecked(bool.booleanValue(), this.mUserIdMatched, this.mThrottleTimeout);
            }

            private boolean checkCredential(int i, OnCheckForUsersCallback onCheckForUsersCallback) throws LockPatternUtils.RequestThrottledException {
                LockPatternUtils lockPatternUtils = lockPatternUtils;
                LockscreenCredential lockscreenCredential = lockscreenCredential;
                Objects.requireNonNull(onCheckForUsersCallback);
                if (!lockPatternUtils.checkCredential(lockscreenCredential, i, new LockPatternUtils.CheckCredentialProgressCallback() {
                    /* class com.android.keyguard.$$Lambda$wZIIIDk2gN019CXZ9W64R7NWx4 */

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
        r8.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        return r8;
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
