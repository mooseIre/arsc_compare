package com.android.systemui.miui.statusbar.analytics;

import com.android.systemui.statusbar.ExpandableNotificationRow;
import java.util.function.Function;

/* renamed from: com.android.systemui.miui.statusbar.analytics.-$$Lambda$SystemUIStat$J9bUnh-0618MhdBwwnfyV94jOEo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$SystemUIStat$J9bUnh0618MhdBwwnfyV94jOEo implements Function {
    public static final /* synthetic */ $$Lambda$SystemUIStat$J9bUnh0618MhdBwwnfyV94jOEo INSTANCE = new $$Lambda$SystemUIStat$J9bUnh0618MhdBwwnfyV94jOEo();

    private /* synthetic */ $$Lambda$SystemUIStat$J9bUnh0618MhdBwwnfyV94jOEo() {
    }

    public final Object apply(Object obj) {
        return Long.valueOf(((ExpandableNotificationRow) obj).getEntry().notification.getPostTime());
    }
}
