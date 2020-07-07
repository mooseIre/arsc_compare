package com.android.keyguard.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class Utilities {
    public static boolean isUriFileExists(Context context, Uri uri) {
        assertNoUIThread();
        if (uri == null) {
            return false;
        }
        try {
            closeFileSafely(context.getContentResolver().openInputStream(uri));
            return true;
        } catch (Exception e) {
            Log.e("Utilities", "isUriFileExists", e);
            return false;
        }
    }

    private static void assertNoUIThread() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("function cannot run no UI thread!");
        }
    }

    private static void closeFileSafely(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Log.e("Utilities", "closeFileSafely", e);
            }
        }
    }

    public static Bitmap createBitmapSafely(int i, int i2, Bitmap.Config config) {
        try {
            return Bitmap.createBitmap(i, i2, config);
        } catch (OutOfMemoryError e) {
            Log.e("Utilities", "createBitmapSafely", e);
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:41:0x006d, code lost:
        if (r4 <= 8) goto L_0x00e1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x006f, code lost:
        r2 = new byte[r4];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0075, code lost:
        if (read(r12, r2, r4) != false) goto L_0x0078;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0077, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0078, code lost:
        r12 = pack(r2, 0, 4, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x007f, code lost:
        if (r12 == 1229531648) goto L_0x008c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0084, code lost:
        if (r12 == 1296891946) goto L_0x008c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0086, code lost:
        android.util.Log.e("Utilities", "Invalid byte order");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x008b, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x008c, code lost:
        if (r12 != 1229531648) goto L_0x0090;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x008e, code lost:
        r12 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0090, code lost:
        r12 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0091, code lost:
        r7 = pack(r2, 4, 4, r12) + 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0098, code lost:
        if (r7 < 10) goto L_0x00db;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x009a, code lost:
        if (r7 <= r4) goto L_0x009d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x009d, code lost:
        r9 = r7 + 0;
        r4 = r4 - r7;
        r7 = pack(r2, r9 - 2, 2, r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00a6, code lost:
        r10 = r7 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00a8, code lost:
        if (r7 <= 0) goto L_0x00e1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00ac, code lost:
        if (r4 < 12) goto L_0x00e1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00b4, code lost:
        if (pack(r2, r9, 2, r12) != 274) goto L_0x00d5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00b6, code lost:
        r7 = pack(r2, r9 + 8, 2, r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x00bc, code lost:
        if (r7 == 1) goto L_0x00d4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x00bf, code lost:
        if (r7 == 3) goto L_0x00d1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x00c1, code lost:
        if (r7 == 6) goto L_0x00ce;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x00c3, code lost:
        if (r7 == 8) goto L_0x00cb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x00c5, code lost:
        android.util.Log.i("Utilities", "Unsupported orientation");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x00cb, code lost:
        return 270;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x00ce, code lost:
        return 90;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x00d1, code lost:
        return 180;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x00d4, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x00d5, code lost:
        r9 = r9 + 12;
        r4 = r4 - 12;
        r7 = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x00db, code lost:
        android.util.Log.e("Utilities", "Invalid offset");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x00e0, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x00e1, code lost:
        android.util.Log.i("Utilities", "Orientation not found");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x00e6, code lost:
        return 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int getImageRotation(java.io.InputStream r12) {
        /*
            r0 = 0
            if (r12 != 0) goto L_0x0004
            return r0
        L_0x0004:
            r1 = 8
            byte[] r2 = new byte[r1]
        L_0x0008:
            r3 = 2
            boolean r4 = read(r12, r2, r3)
            r5 = 6
            r6 = 1
            r7 = 4
            java.lang.String r8 = "Utilities"
            if (r4 == 0) goto L_0x006c
            byte r4 = r2[r0]
            r9 = 255(0xff, float:3.57E-43)
            r4 = r4 & r9
            if (r4 != r9) goto L_0x006c
            byte r4 = r2[r6]
            r4 = r4 & r9
            if (r4 != r9) goto L_0x0021
            goto L_0x0008
        L_0x0021:
            r9 = 216(0xd8, float:3.03E-43)
            if (r4 == r9) goto L_0x0008
            if (r4 != r6) goto L_0x0028
            goto L_0x0008
        L_0x0028:
            r9 = 217(0xd9, float:3.04E-43)
            if (r4 == r9) goto L_0x006b
            r9 = 218(0xda, float:3.05E-43)
            if (r4 != r9) goto L_0x0031
            goto L_0x006b
        L_0x0031:
            boolean r9 = read(r12, r2, r3)
            if (r9 != 0) goto L_0x0038
            return r0
        L_0x0038:
            int r9 = pack(r2, r0, r3, r0)
            if (r9 >= r3) goto L_0x0044
            java.lang.String r12 = "Invalid length"
            android.util.Log.e(r8, r12)
            return r0
        L_0x0044:
            int r9 = r9 + -2
            r10 = 225(0xe1, float:3.15E-43)
            if (r4 != r10) goto L_0x0065
            if (r9 < r5) goto L_0x0065
            boolean r4 = read(r12, r2, r5)
            if (r4 != 0) goto L_0x0053
            return r0
        L_0x0053:
            int r4 = r9 + -6
            int r9 = pack(r2, r0, r7, r0)
            r10 = 1165519206(0x45786966, float:3974.5874)
            if (r9 != r10) goto L_0x0066
            int r9 = pack(r2, r7, r3, r0)
            if (r9 != 0) goto L_0x0066
            goto L_0x006d
        L_0x0065:
            r4 = r9
        L_0x0066:
            long r3 = (long) r4
            r12.skip(r3)     // Catch:{ IOException -> 0x006b }
            goto L_0x0008
        L_0x006b:
            return r0
        L_0x006c:
            r4 = r0
        L_0x006d:
            if (r4 <= r1) goto L_0x00e1
            byte[] r2 = new byte[r4]
            boolean r12 = read(r12, r2, r4)
            if (r12 != 0) goto L_0x0078
            return r0
        L_0x0078:
            int r12 = pack(r2, r0, r7, r0)
            r9 = 1229531648(0x49492a00, float:823968.0)
            if (r12 == r9) goto L_0x008c
            r10 = 1296891946(0x4d4d002a, float:2.14958752E8)
            if (r12 == r10) goto L_0x008c
            java.lang.String r12 = "Invalid byte order"
            android.util.Log.e(r8, r12)
            return r0
        L_0x008c:
            if (r12 != r9) goto L_0x0090
            r12 = r6
            goto L_0x0091
        L_0x0090:
            r12 = r0
        L_0x0091:
            int r7 = pack(r2, r7, r7, r12)
            int r7 = r7 + r3
            r9 = 10
            if (r7 < r9) goto L_0x00db
            if (r7 <= r4) goto L_0x009d
            goto L_0x00db
        L_0x009d:
            int r9 = r7 + 0
            int r4 = r4 - r7
            int r7 = r9 + -2
            int r7 = pack(r2, r7, r3, r12)
        L_0x00a6:
            int r10 = r7 + -1
            if (r7 <= 0) goto L_0x00e1
            r7 = 12
            if (r4 < r7) goto L_0x00e1
            int r7 = pack(r2, r9, r3, r12)
            r11 = 274(0x112, float:3.84E-43)
            if (r7 != r11) goto L_0x00d5
            int r7 = r9 + 8
            int r7 = pack(r2, r7, r3, r12)
            if (r7 == r6) goto L_0x00d4
            r11 = 3
            if (r7 == r11) goto L_0x00d1
            if (r7 == r5) goto L_0x00ce
            if (r7 == r1) goto L_0x00cb
            java.lang.String r7 = "Unsupported orientation"
            android.util.Log.i(r8, r7)
            goto L_0x00d5
        L_0x00cb:
            r12 = 270(0x10e, float:3.78E-43)
            return r12
        L_0x00ce:
            r12 = 90
            return r12
        L_0x00d1:
            r12 = 180(0xb4, float:2.52E-43)
            return r12
        L_0x00d4:
            return r0
        L_0x00d5:
            int r9 = r9 + 12
            int r4 = r4 + -12
            r7 = r10
            goto L_0x00a6
        L_0x00db:
            java.lang.String r12 = "Invalid offset"
            android.util.Log.e(r8, r12)
            return r0
        L_0x00e1:
            java.lang.String r12 = "Orientation not found"
            android.util.Log.i(r8, r12)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.common.Utilities.getImageRotation(java.io.InputStream):int");
    }

    private static int pack(byte[] bArr, int i, int i2, boolean z) {
        int i3;
        if (z) {
            i += i2 - 1;
            i3 = -1;
        } else {
            i3 = 1;
        }
        byte b = 0;
        while (true) {
            int i4 = i2 - 1;
            if (i2 <= 0) {
                return b;
            }
            b = (bArr[i] & 255) | (b << 8);
            i += i3;
            i2 = i4;
        }
    }

    private static boolean read(InputStream inputStream, byte[] bArr, int i) {
        try {
            return inputStream.read(bArr, 0, i) == i;
        } catch (IOException unused) {
            return false;
        }
    }
}
