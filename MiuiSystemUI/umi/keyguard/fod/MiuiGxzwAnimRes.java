package com.android.keyguard.fod;

import android.content.Context;
import android.content.res.Resources;

/* access modifiers changed from: package-private */
public class MiuiGxzwAnimRes {
    final int mFrameInterval;
    private final IGeneralRes mGeneralRes;
    final boolean mRepeat;
    private int[] mRes = null;

    private interface IGeneralRes {
        int[] generalRes(Context context);
    }

    MiuiGxzwAnimRes(int[] iArr, boolean z, int i) {
        this.mGeneralRes = new ArrayGeneraRes(iArr);
        this.mRepeat = z;
        this.mFrameInterval = i;
    }

    MiuiGxzwAnimRes(int i, String str, boolean z, int i2, boolean z2) {
        this.mGeneralRes = new DrawableGeneraRes(i, str, z2);
        this.mRepeat = z;
        this.mFrameInterval = i2;
    }

    public int[] getAnimRes(Context context) {
        if (this.mRes == null) {
            this.mRes = this.mGeneralRes.generalRes(context);
        }
        return this.mRes;
    }

    private static class DrawableGeneraRes implements IGeneralRes {
        final boolean clean;
        final int len;
        final String prefix;

        DrawableGeneraRes(int i, String str, boolean z) {
            this.len = i;
            this.prefix = str;
            this.clean = z;
        }

        @Override // com.android.keyguard.fod.MiuiGxzwAnimRes.IGeneralRes
        public int[] generalRes(Context context) {
            int i;
            String packageName = context.getPackageName();
            int[] iArr = new int[(this.clean ? this.len + 1 : this.len)];
            int i2 = 0;
            while (true) {
                i = this.len;
                if (i2 >= i) {
                    break;
                }
                Resources resources = context.getResources();
                StringBuilder sb = new StringBuilder();
                sb.append(this.prefix);
                int i3 = i2 + 1;
                sb.append(i3);
                iArr[i2] = resources.getIdentifier(sb.toString(), "drawable", packageName);
                i2 = i3;
            }
            if (this.clean) {
                iArr[i] = 0;
            }
            return iArr;
        }
    }

    private static class ArrayGeneraRes implements IGeneralRes {
        final int[] res;

        ArrayGeneraRes(int[] iArr) {
            this.res = iArr;
        }

        @Override // com.android.keyguard.fod.MiuiGxzwAnimRes.IGeneralRes
        public int[] generalRes(Context context) {
            return this.res;
        }
    }
}
