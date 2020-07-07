package com.android.systemui.miui.statusbar.analytics;

public class Analytics$StatusBarPromptEvent extends Analytics$Event {
    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String getPromptState(java.lang.String r6) {
        /*
            int r0 = r6.hashCode()
            r1 = 5
            r2 = 4
            r3 = 3
            r4 = 2
            r5 = 1
            switch(r0) {
                case -956163980: goto L_0x003f;
                case 345429025: goto L_0x0035;
                case 1231041940: goto L_0x002b;
                case 1239445827: goto L_0x0021;
                case 2117874964: goto L_0x0017;
                case 2118351427: goto L_0x000d;
                default: goto L_0x000c;
            }
        L_0x000c:
            goto L_0x0049
        L_0x000d:
            java.lang.String r0 = "legacy_safe"
            boolean r6 = r6.equals(r0)
            if (r6 == 0) goto L_0x0049
            r6 = r3
            goto L_0x004a
        L_0x0017:
            java.lang.String r0 = "legacy_call"
            boolean r6 = r6.equals(r0)
            if (r6 == 0) goto L_0x0049
            r6 = r1
            goto L_0x004a
        L_0x0021:
            java.lang.String r0 = "legacy_multi"
            boolean r6 = r6.equals(r0)
            if (r6 == 0) goto L_0x0049
            r6 = 0
            goto L_0x004a
        L_0x002b:
            java.lang.String r0 = "legacy_drive"
            boolean r6 = r6.equals(r0)
            if (r6 == 0) goto L_0x0049
            r6 = r5
            goto L_0x004a
        L_0x0035:
            java.lang.String r0 = "legacy_sos"
            boolean r6 = r6.equals(r0)
            if (r6 == 0) goto L_0x0049
            r6 = r2
            goto L_0x004a
        L_0x003f:
            java.lang.String r0 = "legacy_recorder"
            boolean r6 = r6.equals(r0)
            if (r6 == 0) goto L_0x0049
            r6 = r4
            goto L_0x004a
        L_0x0049:
            r6 = -1
        L_0x004a:
            if (r6 == 0) goto L_0x0068
            if (r6 == r5) goto L_0x0065
            if (r6 == r4) goto L_0x0062
            if (r6 == r3) goto L_0x005f
            if (r6 == r2) goto L_0x005c
            if (r6 == r1) goto L_0x0059
            java.lang.String r6 = "NORMAL_MODE"
            goto L_0x006a
        L_0x0059:
            java.lang.String r6 = "CALL_MODE"
            goto L_0x006a
        L_0x005c:
            java.lang.String r6 = "SOS_MODE"
            goto L_0x006a
        L_0x005f:
            java.lang.String r6 = "SAFE_MODE"
            goto L_0x006a
        L_0x0062:
            java.lang.String r6 = "RECORDER_MODE"
            goto L_0x006a
        L_0x0065:
            java.lang.String r6 = "DRIVE_MODE"
            goto L_0x006a
        L_0x0068:
            java.lang.String r6 = "MULTI_MODE"
        L_0x006a:
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.miui.statusbar.analytics.Analytics$StatusBarPromptEvent.getPromptState(java.lang.String):java.lang.String");
    }
}
