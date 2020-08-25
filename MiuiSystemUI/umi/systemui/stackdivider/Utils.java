package com.android.systemui.stackdivider;

import android.app.ActivityManager;
import java.lang.reflect.Field;

public class Utils {
    public static boolean isResizable(ActivityManager.RunningTaskInfo runningTaskInfo) {
        Class<?> cls = runningTaskInfo.getClass();
        try {
            return ((Boolean) cls.getMethod("isResizable", new Class[0]).invoke(runningTaskInfo, new Object[0])).booleanValue();
        } catch (Exception unused) {
            try {
                Field declaredField = cls.getDeclaredField("isResizeable");
                declaredField.setAccessible(true);
                return ((Boolean) declaredField.get(runningTaskInfo)).booleanValue();
            } catch (Exception unused2) {
                return true;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:8:?, code lost:
        return java.lang.Class.forName("com.android.internal.policy.DividerSnapAlgorithm").getConstructor(new java.lang.Class[]{android.content.res.Resources.class, java.lang.Integer.TYPE, java.lang.Integer.TYPE, java.lang.Integer.TYPE, r2, android.graphics.Rect.class, java.lang.Integer.TYPE, r2, r2}).newInstance(new java.lang.Object[]{r17.getResources(), java.lang.Integer.valueOf(r18), java.lang.Integer.valueOf(r19), java.lang.Integer.valueOf(r20), java.lang.Boolean.valueOf(r21), r0, java.lang.Integer.valueOf(r23), java.lang.Boolean.valueOf(r24), java.lang.Boolean.valueOf(r25)});
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:?, code lost:
        return r4;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing exception handler attribute for start block: B:4:0x0072 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static com.android.internal.policy.DividerSnapAlgorithm getDividerSnapAlgorithm(android.content.Context r17, int r18, int r19, int r20, boolean r21, android.graphics.Rect r22, int r23, boolean r24, boolean r25) {
        /*
            r0 = r22
            java.lang.String r1 = "com.android.internal.policy.DividerSnapAlgorithm"
            java.lang.Class r2 = java.lang.Boolean.TYPE
            r3 = r17
            com.android.internal.policy.DividerSnapAlgorithm r4 = com.android.internal.policy.DividerSnapAlgorithm.create(r3, r0)
            r5 = 7
            r6 = 6
            r7 = 5
            r8 = 4
            r9 = 3
            r10 = 2
            r11 = 1
            r12 = 0
            r13 = 8
            java.lang.Class r14 = java.lang.Class.forName(r1)     // Catch:{ Exception -> 0x0072 }
            java.lang.Class[] r15 = new java.lang.Class[r13]     // Catch:{ Exception -> 0x0072 }
            java.lang.Class<android.content.res.Resources> r16 = android.content.res.Resources.class
            r15[r12] = r16     // Catch:{ Exception -> 0x0072 }
            java.lang.Class r16 = java.lang.Integer.TYPE     // Catch:{ Exception -> 0x0072 }
            r15[r11] = r16     // Catch:{ Exception -> 0x0072 }
            java.lang.Class r16 = java.lang.Integer.TYPE     // Catch:{ Exception -> 0x0072 }
            r15[r10] = r16     // Catch:{ Exception -> 0x0072 }
            java.lang.Class r16 = java.lang.Integer.TYPE     // Catch:{ Exception -> 0x0072 }
            r15[r9] = r16     // Catch:{ Exception -> 0x0072 }
            r15[r8] = r2     // Catch:{ Exception -> 0x0072 }
            java.lang.Class<android.graphics.Rect> r16 = android.graphics.Rect.class
            r15[r7] = r16     // Catch:{ Exception -> 0x0072 }
            java.lang.Class r16 = java.lang.Integer.TYPE     // Catch:{ Exception -> 0x0072 }
            r15[r6] = r16     // Catch:{ Exception -> 0x0072 }
            r15[r5] = r2     // Catch:{ Exception -> 0x0072 }
            java.lang.reflect.Constructor r14 = r14.getConstructor(r15)     // Catch:{ Exception -> 0x0072 }
            java.lang.Object[] r15 = new java.lang.Object[r13]     // Catch:{ Exception -> 0x0072 }
            android.content.res.Resources r16 = r17.getResources()     // Catch:{ Exception -> 0x0072 }
            r15[r12] = r16     // Catch:{ Exception -> 0x0072 }
            java.lang.Integer r16 = java.lang.Integer.valueOf(r18)     // Catch:{ Exception -> 0x0072 }
            r15[r11] = r16     // Catch:{ Exception -> 0x0072 }
            java.lang.Integer r16 = java.lang.Integer.valueOf(r19)     // Catch:{ Exception -> 0x0072 }
            r15[r10] = r16     // Catch:{ Exception -> 0x0072 }
            java.lang.Integer r16 = java.lang.Integer.valueOf(r20)     // Catch:{ Exception -> 0x0072 }
            r15[r9] = r16     // Catch:{ Exception -> 0x0072 }
            java.lang.Boolean r16 = java.lang.Boolean.valueOf(r21)     // Catch:{ Exception -> 0x0072 }
            r15[r8] = r16     // Catch:{ Exception -> 0x0072 }
            r15[r7] = r0     // Catch:{ Exception -> 0x0072 }
            java.lang.Integer r16 = java.lang.Integer.valueOf(r23)     // Catch:{ Exception -> 0x0072 }
            r15[r6] = r16     // Catch:{ Exception -> 0x0072 }
            java.lang.Boolean r16 = java.lang.Boolean.valueOf(r24)     // Catch:{ Exception -> 0x0072 }
            r15[r5] = r16     // Catch:{ Exception -> 0x0072 }
            java.lang.Object r14 = r14.newInstance(r15)     // Catch:{ Exception -> 0x0072 }
            com.android.internal.policy.DividerSnapAlgorithm r14 = (com.android.internal.policy.DividerSnapAlgorithm) r14     // Catch:{ Exception -> 0x0072 }
            r4 = r14
            goto L_0x00d7
        L_0x0072:
            java.lang.Class r1 = java.lang.Class.forName(r1)     // Catch:{ Exception -> 0x00d7 }
            r14 = 9
            java.lang.Class[] r15 = new java.lang.Class[r14]     // Catch:{ Exception -> 0x00d7 }
            java.lang.Class<android.content.res.Resources> r16 = android.content.res.Resources.class
            r15[r12] = r16     // Catch:{ Exception -> 0x00d7 }
            java.lang.Class r16 = java.lang.Integer.TYPE     // Catch:{ Exception -> 0x00d7 }
            r15[r11] = r16     // Catch:{ Exception -> 0x00d7 }
            java.lang.Class r16 = java.lang.Integer.TYPE     // Catch:{ Exception -> 0x00d7 }
            r15[r10] = r16     // Catch:{ Exception -> 0x00d7 }
            java.lang.Class r16 = java.lang.Integer.TYPE     // Catch:{ Exception -> 0x00d7 }
            r15[r9] = r16     // Catch:{ Exception -> 0x00d7 }
            r15[r8] = r2     // Catch:{ Exception -> 0x00d7 }
            java.lang.Class<android.graphics.Rect> r16 = android.graphics.Rect.class
            r15[r7] = r16     // Catch:{ Exception -> 0x00d7 }
            java.lang.Class r16 = java.lang.Integer.TYPE     // Catch:{ Exception -> 0x00d7 }
            r15[r6] = r16     // Catch:{ Exception -> 0x00d7 }
            r15[r5] = r2     // Catch:{ Exception -> 0x00d7 }
            r15[r13] = r2     // Catch:{ Exception -> 0x00d7 }
            java.lang.reflect.Constructor r1 = r1.getConstructor(r15)     // Catch:{ Exception -> 0x00d7 }
            java.lang.Object[] r2 = new java.lang.Object[r14]     // Catch:{ Exception -> 0x00d7 }
            android.content.res.Resources r3 = r17.getResources()     // Catch:{ Exception -> 0x00d7 }
            r2[r12] = r3     // Catch:{ Exception -> 0x00d7 }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r18)     // Catch:{ Exception -> 0x00d7 }
            r2[r11] = r3     // Catch:{ Exception -> 0x00d7 }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r19)     // Catch:{ Exception -> 0x00d7 }
            r2[r10] = r3     // Catch:{ Exception -> 0x00d7 }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r20)     // Catch:{ Exception -> 0x00d7 }
            r2[r9] = r3     // Catch:{ Exception -> 0x00d7 }
            java.lang.Boolean r3 = java.lang.Boolean.valueOf(r21)     // Catch:{ Exception -> 0x00d7 }
            r2[r8] = r3     // Catch:{ Exception -> 0x00d7 }
            r2[r7] = r0     // Catch:{ Exception -> 0x00d7 }
            java.lang.Integer r0 = java.lang.Integer.valueOf(r23)     // Catch:{ Exception -> 0x00d7 }
            r2[r6] = r0     // Catch:{ Exception -> 0x00d7 }
            java.lang.Boolean r0 = java.lang.Boolean.valueOf(r24)     // Catch:{ Exception -> 0x00d7 }
            r2[r5] = r0     // Catch:{ Exception -> 0x00d7 }
            java.lang.Boolean r0 = java.lang.Boolean.valueOf(r25)     // Catch:{ Exception -> 0x00d7 }
            r2[r13] = r0     // Catch:{ Exception -> 0x00d7 }
            java.lang.Object r0 = r1.newInstance(r2)     // Catch:{ Exception -> 0x00d7 }
            com.android.internal.policy.DividerSnapAlgorithm r0 = (com.android.internal.policy.DividerSnapAlgorithm) r0     // Catch:{ Exception -> 0x00d7 }
            r4 = r0
        L_0x00d7:
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.stackdivider.Utils.getDividerSnapAlgorithm(android.content.Context, int, int, int, boolean, android.graphics.Rect, int, boolean, boolean):com.android.internal.policy.DividerSnapAlgorithm");
    }
}
