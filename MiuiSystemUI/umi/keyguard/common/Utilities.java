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

    /* JADX WARNING: Code restructure failed: missing block: B:40:0x006c, code lost:
        if (r9 <= 8) goto L_0x00e0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x006e, code lost:
        r2 = new byte[r9];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0074, code lost:
        if (read(r12, r2, r9) != false) goto L_0x0077;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0076, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0077, code lost:
        r12 = pack(r2, 0, 4, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x007e, code lost:
        if (r12 == 1229531648) goto L_0x008b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0083, code lost:
        if (r12 == 1296891946) goto L_0x008b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0085, code lost:
        android.util.Log.e("Utilities", "Invalid byte order");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x008a, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x008b, code lost:
        if (r12 != 1229531648) goto L_0x008f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x008d, code lost:
        r12 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x008f, code lost:
        r12 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0090, code lost:
        r4 = pack(r2, 4, 4, r12) + 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0097, code lost:
        if (r4 < 10) goto L_0x00da;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0099, code lost:
        if (r4 <= r9) goto L_0x009c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x009c, code lost:
        r7 = r4 + 0;
        r9 = r9 - r4;
        r4 = pack(r2, r7 - 2, 2, r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00a5, code lost:
        r10 = r4 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00a7, code lost:
        if (r4 <= 0) goto L_0x00e0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00ab, code lost:
        if (r9 < 12) goto L_0x00e0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00b3, code lost:
        if (pack(r2, r7, 2, r12) != 274) goto L_0x00d4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00b5, code lost:
        r4 = pack(r2, r7 + 8, 2, r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00bb, code lost:
        if (r4 == 1) goto L_0x00d3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x00be, code lost:
        if (r4 == 3) goto L_0x00d0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x00c0, code lost:
        if (r4 == 6) goto L_0x00cd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x00c2, code lost:
        if (r4 == 8) goto L_0x00ca;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x00c4, code lost:
        android.util.Log.i("Utilities", "Unsupported orientation");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x00ca, code lost:
        return 270;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x00cd, code lost:
        return 90;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x00d0, code lost:
        return 180;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x00d3, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x00d4, code lost:
        r7 = r7 + 12;
        r9 = r9 - 12;
        r4 = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x00da, code lost:
        android.util.Log.e("Utilities", "Invalid offset");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x00df, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x00e0, code lost:
        android.util.Log.i("Utilities", "Orientation not found");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x00e5, code lost:
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
            if (r4 == 0) goto L_0x006b
            byte r4 = r2[r0]
            r9 = 255(0xff, float:3.57E-43)
            r4 = r4 & r9
            if (r4 != r9) goto L_0x006b
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
            if (r4 == r9) goto L_0x006a
            r9 = 218(0xda, float:3.05E-43)
            if (r4 != r9) goto L_0x0031
            goto L_0x006a
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
            int r9 = r9 + -6
            int r4 = pack(r2, r0, r7, r0)
            r10 = 1165519206(0x45786966, float:3974.5874)
            if (r4 != r10) goto L_0x0065
            int r4 = pack(r2, r7, r3, r0)
            if (r4 != 0) goto L_0x0065
            goto L_0x006c
        L_0x0065:
            long r3 = (long) r9
            r12.skip(r3)     // Catch:{ IOException -> 0x006a }
            goto L_0x0008
        L_0x006a:
            return r0
        L_0x006b:
            r9 = r0
        L_0x006c:
            if (r9 <= r1) goto L_0x00e0
            byte[] r2 = new byte[r9]
            boolean r12 = read(r12, r2, r9)
            if (r12 != 0) goto L_0x0077
            return r0
        L_0x0077:
            int r12 = pack(r2, r0, r7, r0)
            r4 = 1229531648(0x49492a00, float:823968.0)
            if (r12 == r4) goto L_0x008b
            r10 = 1296891946(0x4d4d002a, float:2.14958752E8)
            if (r12 == r10) goto L_0x008b
            java.lang.String r12 = "Invalid byte order"
            android.util.Log.e(r8, r12)
            return r0
        L_0x008b:
            if (r12 != r4) goto L_0x008f
            r12 = r6
            goto L_0x0090
        L_0x008f:
            r12 = r0
        L_0x0090:
            int r4 = pack(r2, r7, r7, r12)
            int r4 = r4 + r3
            r7 = 10
            if (r4 < r7) goto L_0x00da
            if (r4 <= r9) goto L_0x009c
            goto L_0x00da
        L_0x009c:
            int r7 = r4 + 0
            int r9 = r9 - r4
            int r4 = r7 + -2
            int r4 = pack(r2, r4, r3, r12)
        L_0x00a5:
            int r10 = r4 + -1
            if (r4 <= 0) goto L_0x00e0
            r4 = 12
            if (r9 < r4) goto L_0x00e0
            int r4 = pack(r2, r7, r3, r12)
            r11 = 274(0x112, float:3.84E-43)
            if (r4 != r11) goto L_0x00d4
            int r4 = r7 + 8
            int r4 = pack(r2, r4, r3, r12)
            if (r4 == r6) goto L_0x00d3
            r11 = 3
            if (r4 == r11) goto L_0x00d0
            if (r4 == r5) goto L_0x00cd
            if (r4 == r1) goto L_0x00ca
            java.lang.String r4 = "Unsupported orientation"
            android.util.Log.i(r8, r4)
            goto L_0x00d4
        L_0x00ca:
            r12 = 270(0x10e, float:3.78E-43)
            return r12
        L_0x00cd:
            r12 = 90
            return r12
        L_0x00d0:
            r12 = 180(0xb4, float:2.52E-43)
            return r12
        L_0x00d3:
            return r0
        L_0x00d4:
            int r7 = r7 + 12
            int r9 = r9 + -12
            r4 = r10
            goto L_0x00a5
        L_0x00da:
            java.lang.String r12 = "Invalid offset"
            android.util.Log.e(r8, r12)
            return r0
        L_0x00e0:
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
