package com.android.systemui.statusbar.phone;

public class VelocityTrackerFactory {
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0032  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0050  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static com.android.systemui.statusbar.phone.VelocityTrackerInterface obtain(android.content.Context r3) {
        /*
            android.content.res.Resources r3 = r3.getResources()
            r0 = 2131822588(0x7f1107fc, float:1.9277952E38)
            java.lang.String r3 = r3.getString(r0)
            int r0 = r3.hashCode()
            r1 = 104998702(0x642272e, float:3.651613E-35)
            r2 = 1
            if (r0 == r1) goto L_0x0025
            r1 = 1874684019(0x6fbd6873, float:1.1723788E29)
            if (r0 == r1) goto L_0x001b
            goto L_0x002f
        L_0x001b:
            java.lang.String r0 = "platform"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x002f
            r0 = r2
            goto L_0x0030
        L_0x0025:
            java.lang.String r0 = "noisy"
            boolean r0 = r3.equals(r0)
            if (r0 == 0) goto L_0x002f
            r0 = 0
            goto L_0x0030
        L_0x002f:
            r0 = -1
        L_0x0030:
            if (r0 == 0) goto L_0x0050
            if (r0 != r2) goto L_0x0039
            com.android.systemui.statusbar.phone.PlatformVelocityTracker r3 = com.android.systemui.statusbar.phone.PlatformVelocityTracker.obtain()
            return r3
        L_0x0039:
            java.lang.IllegalStateException r0 = new java.lang.IllegalStateException
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "Invalid tracker: "
            r1.append(r2)
            r1.append(r3)
            java.lang.String r3 = r1.toString()
            r0.<init>(r3)
            throw r0
        L_0x0050:
            com.android.systemui.statusbar.phone.NoisyVelocityTracker r3 = com.android.systemui.statusbar.phone.NoisyVelocityTracker.obtain()
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.VelocityTrackerFactory.obtain(android.content.Context):com.android.systemui.statusbar.phone.VelocityTrackerInterface");
    }
}
