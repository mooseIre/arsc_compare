package com.android.keyguard.utils;

import android.content.Context;
import android.content.IContentProvider;
import android.net.Uri;
import android.os.Bundle;

public class ContentProviderUtils {
    public static boolean isProviderExists(Context context, Uri uri) {
        IContentProvider acquireUnstableProvider = context.getContentResolver().acquireUnstableProvider(uri);
        if (acquireUnstableProvider == null) {
            return false;
        }
        context.getContentResolver().releaseUnstableProvider(acquireUnstableProvider);
        return true;
    }

    public static Bundle getResultFromProvider(Context context, String str, String str2, String str3, Bundle bundle) {
        return getResultFromProvider(context, Uri.parse(str), str2, str3, bundle);
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x004f  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0057  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static android.os.Bundle getResultFromProvider(android.content.Context r2, android.net.Uri r3, java.lang.String r4, java.lang.String r5, android.os.Bundle r6) {
        /*
            java.lang.String r0 = "ContentProviderUtils"
            r1 = 0
            android.content.ContentResolver r2 = r2.getContentResolver()     // Catch:{ Exception -> 0x0046, all -> 0x0044 }
            android.content.ContentProviderClient r2 = r2.acquireUnstableContentProviderClient(r3)     // Catch:{ Exception -> 0x0046, all -> 0x0044 }
            if (r2 != 0) goto L_0x0038
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0042 }
            r5.<init>()     // Catch:{ Exception -> 0x0042 }
            java.lang.String r6 = "getResultFromProvider provider == null callMethod:"
            r5.append(r6)     // Catch:{ Exception -> 0x0042 }
            r5.append(r4)     // Catch:{ Exception -> 0x0042 }
            java.lang.String r4 = " providerUri:"
            r5.append(r4)     // Catch:{ Exception -> 0x0042 }
            java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0042 }
            r5.append(r3)     // Catch:{ Exception -> 0x0042 }
            java.lang.String r3 = r5.toString()     // Catch:{ Exception -> 0x0042 }
            java.lang.Throwable r4 = new java.lang.Throwable     // Catch:{ Exception -> 0x0042 }
            r4.<init>()     // Catch:{ Exception -> 0x0042 }
            android.util.Log.e(r0, r3, r4)     // Catch:{ Exception -> 0x0042 }
            if (r2 == 0) goto L_0x0037
            r2.close()
        L_0x0037:
            return r1
        L_0x0038:
            android.os.Bundle r3 = r2.call(r4, r5, r6)
            if (r2 == 0) goto L_0x0041
            r2.close()
        L_0x0041:
            return r3
        L_0x0042:
            r3 = move-exception
            goto L_0x0048
        L_0x0044:
            r3 = move-exception
            goto L_0x0055
        L_0x0046:
            r3 = move-exception
            r2 = r1
        L_0x0048:
            java.lang.String r4 = "getResultFromProvider"
            android.util.Log.e(r0, r4, r3)     // Catch:{ all -> 0x0053 }
            if (r2 == 0) goto L_0x0052
            r2.close()
        L_0x0052:
            return r1
        L_0x0053:
            r3 = move-exception
            r1 = r2
        L_0x0055:
            if (r1 == 0) goto L_0x005a
            r1.close()
        L_0x005a:
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.utils.ContentProviderUtils.getResultFromProvider(android.content.Context, android.net.Uri, java.lang.String, java.lang.String, android.os.Bundle):android.os.Bundle");
    }
}
