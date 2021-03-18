package com.android.keyguard.AwesomeLockScreenImp;

import miui.maml.data.ContentProviderBinder;
import miui.maml.data.VariableBinder;
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

    public void visit(VariableBinder variableBinder) {
        ContentProviderBinder contentProviderBinder;
        String uriText;
        if ((variableBinder instanceof ContentProviderBinder) && (uriText = (contentProviderBinder = (ContentProviderBinder) variableBinder).getUriText()) != null) {
            if ((this.mPrefix && uriText.startsWith(this.mUri)) || uriText.equals(this.mUri)) {
                contentProviderBinder.setBlockedColumns(this.mColumns);
            }
        }
    }
}
