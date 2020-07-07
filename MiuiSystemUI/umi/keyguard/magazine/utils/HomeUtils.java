package com.android.keyguard.magazine.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.SystemProperties;
import android.os.UserHandle;

public class HomeUtils {
    public static final String HOME_LAUNCHER_SETTINGS_AUTHORITY = (SystemProperties.get("ro.miui.product.home", "com.miui.home") + ".launcher.settings");

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0053  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x005a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String getCurrentWallpaperInfo(android.content.Context r9) {
        /*
            r0 = 0
            android.os.UserHandle r1 = android.os.UserHandle.OWNER     // Catch:{ Exception -> 0x0057, all -> 0x004d }
            android.content.ContentResolver r2 = r9.getContentResolverForUser(r1)     // Catch:{ Exception -> 0x0057, all -> 0x004d }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0057, all -> 0x004d }
            r9.<init>()     // Catch:{ Exception -> 0x0057, all -> 0x004d }
            java.lang.String r1 = "content://"
            r9.append(r1)     // Catch:{ Exception -> 0x0057, all -> 0x004d }
            java.lang.String r1 = HOME_LAUNCHER_SETTINGS_AUTHORITY     // Catch:{ Exception -> 0x0057, all -> 0x004d }
            r9.append(r1)     // Catch:{ Exception -> 0x0057, all -> 0x004d }
            java.lang.String r1 = "/"
            r9.append(r1)     // Catch:{ Exception -> 0x0057, all -> 0x004d }
            java.lang.String r1 = "preference"
            r9.append(r1)     // Catch:{ Exception -> 0x0057, all -> 0x004d }
            java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x0057, all -> 0x004d }
            android.net.Uri r3 = android.net.Uri.parse(r9)     // Catch:{ Exception -> 0x0057, all -> 0x004d }
            java.lang.String r9 = "currentWallpaperInfo"
            java.lang.String[] r4 = new java.lang.String[]{r9}     // Catch:{ Exception -> 0x0057, all -> 0x004d }
            r5 = 0
            r6 = 0
            r7 = 0
            android.database.Cursor r9 = r2.query(r3, r4, r5, r6, r7)     // Catch:{ Exception -> 0x0057, all -> 0x004d }
            if (r9 == 0) goto L_0x0047
            r9.moveToFirst()     // Catch:{ Exception -> 0x0058, all -> 0x0045 }
            r1 = 0
            java.lang.String r0 = r9.getString(r1)     // Catch:{ Exception -> 0x0058, all -> 0x0045 }
            if (r9 == 0) goto L_0x0044
            r9.close()
        L_0x0044:
            return r0
        L_0x0045:
            r0 = move-exception
            goto L_0x0051
        L_0x0047:
            if (r9 == 0) goto L_0x004c
            r9.close()
        L_0x004c:
            return r0
        L_0x004d:
            r9 = move-exception
            r8 = r0
            r0 = r9
            r9 = r8
        L_0x0051:
            if (r9 == 0) goto L_0x0056
            r9.close()
        L_0x0056:
            throw r0
        L_0x0057:
            r9 = r0
        L_0x0058:
            if (r9 == 0) goto L_0x005d
            r9.close()
        L_0x005d:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.magazine.utils.HomeUtils.getCurrentWallpaperInfo(android.content.Context):java.lang.String");
    }

    public static void updateCurrentWallpaperInfo(Context context, String str) {
        try {
            ContentResolver contentResolverForUser = context.getContentResolverForUser(UserHandle.OWNER);
            ContentValues contentValues = new ContentValues();
            contentValues.put("currentWallpaperInfo", str);
            contentResolverForUser.update(Uri.parse("content://" + HOME_LAUNCHER_SETTINGS_AUTHORITY + "/" + "preference"), contentValues, (String) null, (String[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
