package com.android.keyguard;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Slog;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.systemui.util.QcomBoostFramework;
import java.util.List;
import java.util.Objects;

public final class LockPatternChecker {
    /* access modifiers changed from: private */
    public static String TAG = "miui_keyguard_password";
    private static final QcomBoostFramework sQcomBoostFramework = new QcomBoostFramework();

    public static AsyncTask<?, ?, ?> checkPatternForUsers(LockPatternUtils lockPatternUtils, List<LockPatternView.Cell> list, int i, int i2, Context context, OnCheckForUsersCallback onCheckForUsersCallback, OnCheckForUsersCallback onCheckForUsersCallback2) {
        sQcomBoostFramework.perfHint(4241, (String) null);
        final Context context2 = context;
        final int i3 = i;
        final List<LockPatternView.Cell> list2 = list;
        final OnCheckForUsersCallback onCheckForUsersCallback3 = onCheckForUsersCallback;
        final int i4 = i2;
        final LockPatternUtils lockPatternUtils2 = lockPatternUtils;
        final OnCheckForUsersCallback onCheckForUsersCallback4 = onCheckForUsersCallback2;
        AnonymousClass1 r3 = new AsyncTask<Void, Void, Boolean>() {
            private int mThrottleTimeout;
            private int mUserIdMatched = -10000;

            /* access modifiers changed from: protected */
            /* JADX WARNING: Removed duplicated region for block: B:17:0x0057  */
            /* JADX WARNING: Removed duplicated region for block: B:19:0x005c A[SYNTHETIC, Splitter:B:19:0x005c] */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public java.lang.Boolean doInBackground(java.lang.Void... r6) {
                /*
                    r5 = this;
                    java.lang.String r6 = "miui_keyguard"
                    java.lang.String r0 = "keyguard_check_password_failed"
                    android.content.Context r1 = r4
                    com.android.keyguard.analytics.AnalyticsHelper r1 = com.android.keyguard.analytics.AnalyticsHelper.getInstance(r1)
                    java.lang.String r2 = "pw_verify_time"
                    r1.trackPageStart(r2)
                    int r1 = r5
                    r5.mUserIdMatched = r1
                    r1 = 1
                    java.util.List r2 = r6     // Catch:{ RequestThrottledException -> 0x0049, Exception -> 0x0035 }
                    if (r2 == 0) goto L_0x002f
                    java.util.List r2 = r6     // Catch:{ RequestThrottledException -> 0x0049, Exception -> 0x0035 }
                    boolean r2 = r2.isEmpty()     // Catch:{ RequestThrottledException -> 0x0049, Exception -> 0x0035 }
                    if (r2 != 0) goto L_0x002f
                    int r2 = r5     // Catch:{ RequestThrottledException -> 0x0049, Exception -> 0x0035 }
                    com.android.keyguard.OnCheckForUsersCallback r3 = r7     // Catch:{ RequestThrottledException -> 0x0049, Exception -> 0x0035 }
                    boolean r2 = r5.checkPattern(r2, r3)     // Catch:{ RequestThrottledException -> 0x0049, Exception -> 0x0035 }
                    if (r2 == 0) goto L_0x0050
                    java.lang.Boolean r5 = java.lang.Boolean.valueOf(r1)     // Catch:{ RequestThrottledException -> 0x0049, Exception -> 0x0035 }
                    return r5
                L_0x002f:
                    java.lang.String r2 = "pattern is null when check pattern for currentUserId"
                    android.util.Log.e(r6, r2)     // Catch:{ RequestThrottledException -> 0x0049, Exception -> 0x0035 }
                    goto L_0x0050
                L_0x0035:
                    r2 = move-exception
                    java.lang.String r3 = com.android.keyguard.LockPatternChecker.TAG
                    java.lang.String r4 = "checkPatternForUsers failed"
                    android.util.Slog.e(r3, r4, r2)
                    android.content.Context r2 = r4
                    com.android.keyguard.analytics.AnalyticsHelper r2 = com.android.keyguard.analytics.AnalyticsHelper.getInstance(r2)
                    r2.record(r0)
                    goto L_0x0050
                L_0x0049:
                    r2 = move-exception
                    int r2 = r2.getTimeoutMs()
                    r5.mThrottleTimeout = r2
                L_0x0050:
                    int r2 = r8
                    int r3 = r5
                    r4 = 0
                    if (r2 != r3) goto L_0x005c
                    java.lang.Boolean r5 = java.lang.Boolean.valueOf(r4)
                    return r5
                L_0x005c:
                    java.util.List r2 = r6     // Catch:{ RequestThrottledException -> 0x009a, Exception -> 0x0087 }
                    if (r2 == 0) goto L_0x0081
                    java.util.List r2 = r6     // Catch:{ RequestThrottledException -> 0x009a, Exception -> 0x0087 }
                    boolean r2 = r2.isEmpty()     // Catch:{ RequestThrottledException -> 0x009a, Exception -> 0x0087 }
                    if (r2 != 0) goto L_0x0081
                    com.android.internal.widget.LockPatternUtils r6 = r9     // Catch:{ RequestThrottledException -> 0x009a, Exception -> 0x0087 }
                    int r2 = r8     // Catch:{ RequestThrottledException -> 0x009a, Exception -> 0x0087 }
                    boolean r6 = com.android.keyguard.LockPatternChecker.isPatternPasswordEnable(r6, r2)     // Catch:{ RequestThrottledException -> 0x009a, Exception -> 0x0087 }
                    if (r6 == 0) goto L_0x009a
                    int r6 = r8     // Catch:{ RequestThrottledException -> 0x009a, Exception -> 0x0087 }
                    com.android.keyguard.OnCheckForUsersCallback r2 = r10     // Catch:{ RequestThrottledException -> 0x009a, Exception -> 0x0087 }
                    boolean r6 = r5.checkPattern(r6, r2)     // Catch:{ RequestThrottledException -> 0x009a, Exception -> 0x0087 }
                    if (r6 == 0) goto L_0x009a
                    java.lang.Boolean r5 = java.lang.Boolean.valueOf(r1)     // Catch:{ RequestThrottledException -> 0x009a, Exception -> 0x0087 }
                    return r5
                L_0x0081:
                    java.lang.String r1 = "pattern is null when check pattern foe other user"
                    android.util.Log.e(r6, r1)     // Catch:{ RequestThrottledException -> 0x009a, Exception -> 0x0087 }
                    goto L_0x009a
                L_0x0087:
                    r6 = move-exception
                    java.lang.String r1 = com.android.keyguard.LockPatternChecker.TAG
                    java.lang.String r2 = "checkPatternForUsers other users failed"
                    android.util.Slog.e(r1, r2, r6)
                    android.content.Context r5 = r4
                    com.android.keyguard.analytics.AnalyticsHelper r5 = com.android.keyguard.analytics.AnalyticsHelper.getInstance(r5)
                    r5.record(r0)
                L_0x009a:
                    java.lang.Boolean r5 = java.lang.Boolean.valueOf(r4)
                    return r5
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.LockPatternChecker.AnonymousClass1.doInBackground(java.lang.Void[]):java.lang.Boolean");
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Boolean bool) {
                onCheckForUsersCallback3.onChecked(bool.booleanValue(), this.mUserIdMatched, this.mThrottleTimeout);
            }

            private boolean checkPattern(int i, OnCheckForUsersCallback onCheckForUsersCallback) throws LockPatternUtils.RequestThrottledException {
                LockPatternUtils lockPatternUtils = lockPatternUtils2;
                List list = list2;
                Objects.requireNonNull(onCheckForUsersCallback);
                if (!lockPatternUtils.checkPattern(list, i, new LockPatternUtils.CheckCredentialProgressCallback() {
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
        r3.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        return r3;
    }

    public static AsyncTask<?, ?, ?> checkPasswordForUsers(LockPatternUtils lockPatternUtils, String str, int i, int i2, Context context, OnCheckForUsersCallback onCheckForUsersCallback, OnCheckForUsersCallback onCheckForUsersCallback2) {
        sQcomBoostFramework.perfHint(4241, (String) null);
        final Context context2 = context;
        final int i3 = i;
        final OnCheckForUsersCallback onCheckForUsersCallback3 = onCheckForUsersCallback;
        final int i4 = i2;
        final LockPatternUtils lockPatternUtils2 = lockPatternUtils;
        final OnCheckForUsersCallback onCheckForUsersCallback4 = onCheckForUsersCallback2;
        final String str2 = str;
        AnonymousClass2 r3 = new AsyncTask<Void, Void, Boolean>() {
            private int mThrottleTimeout;
            private int mUserIdMatched = -10000;

            /* access modifiers changed from: protected */
            public Boolean doInBackground(Void... voidArr) {
                AnalyticsHelper.getInstance(context2).trackPageStart("pw_verify_time");
                int i = i3;
                this.mUserIdMatched = i;
                try {
                    if (checkPassword(i, onCheckForUsersCallback3)) {
                        return true;
                    }
                } catch (LockPatternUtils.RequestThrottledException e) {
                    this.mThrottleTimeout = e.getTimeoutMs();
                } catch (Exception e2) {
                    Slog.e(LockPatternChecker.TAG, "checkPasswordForUsers failed", e2);
                    AnalyticsHelper.getInstance(context2).record("keyguard_check_password_failed");
                }
                int i2 = i4;
                if (i2 == i3) {
                    return false;
                }
                try {
                    if (LockPatternChecker.isPasswordEnable(lockPatternUtils2, i2) && checkPassword(i4, onCheckForUsersCallback4)) {
                        return true;
                    }
                } catch (LockPatternUtils.RequestThrottledException unused) {
                } catch (Exception e3) {
                    Slog.e(LockPatternChecker.TAG, "checkPasswordForUsers other users failed", e3);
                    AnalyticsHelper.getInstance(context2).record("keyguard_check_password_failed");
                }
                return false;
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Boolean bool) {
                onCheckForUsersCallback3.onChecked(bool.booleanValue(), this.mUserIdMatched, this.mThrottleTimeout);
            }

            private boolean checkPassword(int i, OnCheckForUsersCallback onCheckForUsersCallback) throws LockPatternUtils.RequestThrottledException {
                LockPatternUtils lockPatternUtils = lockPatternUtils2;
                String str = str2;
                Objects.requireNonNull(onCheckForUsersCallback);
                if (!lockPatternUtils.checkPassword(str, i, new LockPatternUtils.CheckCredentialProgressCallback() {
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
        r3.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        return r3;
    }

    /* access modifiers changed from: private */
    public static boolean isPatternPasswordEnable(LockPatternUtils lockPatternUtils, int i) {
        boolean z = lockPatternUtils.getActivePasswordQuality(i) == 65536;
        String str = TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("check pattern password enable for userId : ");
        sb.append(i);
        sb.append(z ? "   enable" : "   disable");
        Log.d(str, sb.toString());
        return z;
    }

    /* access modifiers changed from: private */
    public static boolean isPasswordEnable(LockPatternUtils lockPatternUtils, int i) {
        int activePasswordQuality = lockPatternUtils.getActivePasswordQuality(i);
        boolean z = activePasswordQuality == 262144 || activePasswordQuality == 327680 || activePasswordQuality == 393216 || activePasswordQuality == 131072 || activePasswordQuality == 196608;
        String str = TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("check password enable for userId : ");
        sb.append(i);
        sb.append(z ? "   enable" : "   disable");
        Log.d(str, sb.toString());
        return z;
    }
}
