package com.android.keyguard.AwesomeLockScreenImp;

import miui.maml.data.VariableBinderVisitor;

public class BlockedColumnsSetter extends VariableBinderVisitor {
    private String[] mColumns;
    private boolean mPrefix;
    private String mUri;

    public BlockedColumnsSetter(String str, String... strArr) {
        this(str, false, strArr);
    }

    public BlockedColumnsSetter(String str, boolean z, String... strArr) {
        if (str != null) {
            this.mPrefix = z;
            this.mUri = str;
            this.mColumns = strArr;
            return;
        }
        throw new IllegalArgumentException("uri is null");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0004, code lost:
        r3 = (miui.maml.data.ContentProviderBinder) r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void visit(miui.maml.data.VariableBinder r3) {
        /*
            r2 = this;
            boolean r0 = r3 instanceof miui.maml.data.ContentProviderBinder
            if (r0 == 0) goto L_0x0026
            miui.maml.data.ContentProviderBinder r3 = (miui.maml.data.ContentProviderBinder) r3
            java.lang.String r0 = r3.getUriText()
            if (r0 != 0) goto L_0x000d
            return
        L_0x000d:
            boolean r1 = r2.mPrefix
            if (r1 == 0) goto L_0x0019
            java.lang.String r1 = r2.mUri
            boolean r1 = r0.startsWith(r1)
            if (r1 != 0) goto L_0x0021
        L_0x0019:
            java.lang.String r1 = r2.mUri
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x0026
        L_0x0021:
            java.lang.String[] r2 = r2.mColumns
            r3.setBlockedColumns(r2)
        L_0x0026:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.AwesomeLockScreenImp.BlockedColumnsSetter.visit(miui.maml.data.VariableBinder):void");
    }
}
