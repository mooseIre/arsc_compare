package com.android.keyguard.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.IContentProvider;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class ContentProviderUtils {
    public static void updateData(Context context, Uri uri, ContentValues contentValues) {
        IContentProvider iContentProvider = null;
        try {
            iContentProvider = context.getContentResolver().acquireUnstableProvider(uri);
            if (iContentProvider != null) {
                iContentProvider.update(context.getPackageName(), uri, contentValues, (String) null, (String[]) null);
                if (iContentProvider == null) {
                    return;
                }
                context.getContentResolver().releaseUnstableProvider(iContentProvider);
            } else if (iContentProvider != null) {
                context.getContentResolver().releaseUnstableProvider(iContentProvider);
            }
        } catch (Exception e) {
            Log.d("ContentProviderUtils", "updateData", e);
            if (iContentProvider == null) {
            }
        } catch (Throwable th) {
            if (iContentProvider != null) {
                context.getContentResolver().releaseUnstableProvider(iContentProvider);
            }
            throw th;
        }
    }

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

    /* JADX WARNING: Removed duplicated region for block: B:21:0x002b  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0032  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static android.os.Bundle getResultFromProvider(android.content.Context r1, android.net.Uri r2, java.lang.String r3, java.lang.String r4, android.os.Bundle r5) {
        /*
            r0 = 0
            android.content.ContentResolver r1 = r1.getContentResolver()     // Catch:{ Exception -> 0x0020, all -> 0x001d }
            android.content.ContentProviderClient r1 = r1.acquireUnstableContentProviderClient(r2)     // Catch:{ Exception -> 0x0020, all -> 0x001d }
            if (r1 != 0) goto L_0x0011
            if (r1 == 0) goto L_0x0010
            r1.close()
        L_0x0010:
            return r0
        L_0x0011:
            android.os.Bundle r2 = r1.call(r3, r4, r5)     // Catch:{ Exception -> 0x001b }
            if (r1 == 0) goto L_0x001a
            r1.close()
        L_0x001a:
            return r2
        L_0x001b:
            r2 = move-exception
            goto L_0x0022
        L_0x001d:
            r2 = move-exception
            r1 = r0
            goto L_0x0030
        L_0x0020:
            r2 = move-exception
            r1 = r0
        L_0x0022:
            java.lang.String r3 = "ContentProviderUtils"
            java.lang.String r4 = "getResultFromProvider"
            android.util.Log.e(r3, r4, r2)     // Catch:{ all -> 0x002f }
            if (r1 == 0) goto L_0x002e
            r1.close()
        L_0x002e:
            return r0
        L_0x002f:
            r2 = move-exception
        L_0x0030:
            if (r1 == 0) goto L_0x0035
            r1.close()
        L_0x0035:
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.utils.ContentProviderUtils.getResultFromProvider(android.content.Context, android.net.Uri, java.lang.String, java.lang.String, android.os.Bundle):android.os.Bundle");
    }
}
