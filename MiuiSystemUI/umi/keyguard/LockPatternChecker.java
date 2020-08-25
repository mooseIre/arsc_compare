package com.android.keyguard;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Slog;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.settings.LockPatternUtilsCompat;
import com.android.systemui.util.QcomBoostFramework;
import java.util.List;
import java.util.Objects;

public final class LockPatternChecker {
    /* access modifiers changed from: private */
    public static String TAG = "miui_keyguard_password";
    private static final QcomBoostFramework sQcomBoostFramework = new QcomBoostFramework();

    public static AsyncTask<?, ?, ?> checkPatternForUsers(LockPatternUtils lockPatternUtils, List<LockPatternView.Cell> list, int i, int i2, Context context, OnCheckForUsersCallback onCheckForUsersCallback, OnCheckForUsersCallback onCheckForUsersCallback2) {
        BoostFrameworkHelper.setBoost(3);
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
            /* JADX WARNING: Removed duplicated region for block: B:16:0x0055 A[RETURN] */
            /* JADX WARNING: Removed duplicated region for block: B:17:0x0056 A[SYNTHETIC, Splitter:B:17:0x0056] */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public java.lang.Boolean doInBackground(java.lang.Void... r7) {
                /*
                    r6 = this;
                    java.lang.Boolean r7 = java.lang.Boolean.TRUE
                    java.lang.Boolean r0 = java.lang.Boolean.FALSE
                    java.lang.String r1 = "miui_keyguard"
                    java.lang.String r2 = "keyguard_check_password_failed"
                    android.content.Context r3 = r4
                    com.android.keyguard.analytics.AnalyticsHelper r3 = com.android.keyguard.analytics.AnalyticsHelper.getInstance(r3)
                    java.lang.String r4 = "pw_verify_time"
                    r3.trackPageStart(r4)
                    int r3 = r5
                    r6.mUserIdMatched = r3
                    java.util.List r3 = r6     // Catch:{ RequestThrottledException -> 0x0048, Exception -> 0x0034 }
                    if (r3 == 0) goto L_0x002e
                    java.util.List r3 = r6     // Catch:{ RequestThrottledException -> 0x0048, Exception -> 0x0034 }
                    boolean r3 = r3.isEmpty()     // Catch:{ RequestThrottledException -> 0x0048, Exception -> 0x0034 }
                    if (r3 != 0) goto L_0x002e
                    int r3 = r5     // Catch:{ RequestThrottledException -> 0x0048, Exception -> 0x0034 }
                    com.android.keyguard.OnCheckForUsersCallback r4 = r7     // Catch:{ RequestThrottledException -> 0x0048, Exception -> 0x0034 }
                    boolean r3 = r6.checkPattern(r3, r4)     // Catch:{ RequestThrottledException -> 0x0048, Exception -> 0x0034 }
                    if (r3 == 0) goto L_0x004f
                    return r7
                L_0x002e:
                    java.lang.String r3 = "pattern is null when check pattern for currentUserId"
                    android.util.Log.e(r1, r3)     // Catch:{ RequestThrottledException -> 0x0048, Exception -> 0x0034 }
                    goto L_0x004f
                L_0x0034:
                    r3 = move-exception
                    java.lang.String r4 = com.android.keyguard.LockPatternChecker.TAG
                    java.lang.String r5 = "checkPatternForUsers failed"
                    android.util.Slog.e(r4, r5, r3)
                    android.content.Context r3 = r4
                    com.android.keyguard.analytics.AnalyticsHelper r3 = com.android.keyguard.analytics.AnalyticsHelper.getInstance(r3)
                    r3.record(r2)
                    goto L_0x004f
                L_0x0048:
                    r3 = move-exception
                    int r3 = r3.getTimeoutMs()
                    r6.mThrottleTimeout = r3
                L_0x004f:
                    int r3 = r8
                    int r4 = r5
                    if (r3 != r4) goto L_0x0056
                    return r0
                L_0x0056:
                    java.util.List r3 = r6     // Catch:{ RequestThrottledException -> 0x0090, Exception -> 0x007d }
                    if (r3 == 0) goto L_0x0077
                    java.util.List r3 = r6     // Catch:{ RequestThrottledException -> 0x0090, Exception -> 0x007d }
                    boolean r3 = r3.isEmpty()     // Catch:{ RequestThrottledException -> 0x0090, Exception -> 0x007d }
                    if (r3 != 0) goto L_0x0077
                    com.android.internal.widget.LockPatternUtils r1 = r9     // Catch:{ RequestThrottledException -> 0x0090, Exception -> 0x007d }
                    int r3 = r8     // Catch:{ RequestThrottledException -> 0x0090, Exception -> 0x007d }
                    boolean r1 = com.android.keyguard.LockPatternChecker.isPatternPasswordEnable(r1, r3)     // Catch:{ RequestThrottledException -> 0x0090, Exception -> 0x007d }
                    if (r1 == 0) goto L_0x0090
                    int r1 = r8     // Catch:{ RequestThrottledException -> 0x0090, Exception -> 0x007d }
                    com.android.keyguard.OnCheckForUsersCallback r3 = r10     // Catch:{ RequestThrottledException -> 0x0090, Exception -> 0x007d }
                    boolean r6 = r6.checkPattern(r1, r3)     // Catch:{ RequestThrottledException -> 0x0090, Exception -> 0x007d }
                    if (r6 == 0) goto L_0x0090
                    return r7
                L_0x0077:
                    java.lang.String r7 = "pattern is null when check pattern foe other user"
                    android.util.Log.e(r1, r7)     // Catch:{ RequestThrottledException -> 0x0090, Exception -> 0x007d }
                    goto L_0x0090
                L_0x007d:
                    r7 = move-exception
                    java.lang.String r1 = com.android.keyguard.LockPatternChecker.TAG
                    java.lang.String r3 = "checkPatternForUsers other users failed"
                    android.util.Slog.e(r1, r3, r7)
                    android.content.Context r6 = r4
                    com.android.keyguard.analytics.AnalyticsHelper r6 = com.android.keyguard.analytics.AnalyticsHelper.getInstance(r6)
                    r6.record(r2)
                L_0x0090:
                    return r0
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
                if (!LockPatternUtilsCompat.checkPattern(lockPatternUtils, list, i, new LockPatternUtils.CheckCredentialProgressCallback() {
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
        BoostFrameworkHelper.setBoost(3);
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
                Boolean bool = Boolean.TRUE;
                Boolean bool2 = Boolean.FALSE;
                AnalyticsHelper.getInstance(context2).trackPageStart("pw_verify_time");
                int i = i3;
                this.mUserIdMatched = i;
                try {
                    if (checkPassword(i, onCheckForUsersCallback3)) {
                        return bool;
                    }
                } catch (LockPatternUtils.RequestThrottledException e) {
                    this.mThrottleTimeout = e.getTimeoutMs();
                } catch (Exception e2) {
                    Slog.e(LockPatternChecker.TAG, "checkPasswordForUsers failed", e2);
                    AnalyticsHelper.getInstance(context2).record("keyguard_check_password_failed");
                }
                int i2 = i4;
                if (i2 == i3) {
                    return bool2;
                }
                try {
                    if (!LockPatternChecker.isPasswordEnable(lockPatternUtils2, i2) || !checkPassword(i4, onCheckForUsersCallback4)) {
                        return bool2;
                    }
                    return bool;
                } catch (LockPatternUtils.RequestThrottledException unused) {
                } catch (Exception e3) {
                    Slog.e(LockPatternChecker.TAG, "checkPasswordForUsers other users failed", e3);
                    AnalyticsHelper.getInstance(context2).record("keyguard_check_password_failed");
                }
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Boolean bool) {
                onCheckForUsersCallback3.onChecked(bool.booleanValue(), this.mUserIdMatched, this.mThrottleTimeout);
            }

            private boolean checkPassword(int i, OnCheckForUsersCallback onCheckForUsersCallback) throws LockPatternUtils.RequestThrottledException {
                LockPatternUtils lockPatternUtils = lockPatternUtils2;
                String str = str2;
                Objects.requireNonNull(onCheckForUsersCallback);
                if (!LockPatternUtilsCompat.checkPassword(lockPatternUtils, str, i, new LockPatternUtils.CheckCredentialProgressCallback() {
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
