package com.android.systemui.recents;

import android.content.Context;
import com.android.systemui.C0021R$string;
import com.android.systemui.dagger.ContextComponentHelper;

public abstract class RecentsModule {
    public static RecentsImplementation provideRecentsImpl(Context context, ContextComponentHelper contextComponentHelper) {
        String string = context.getString(C0021R$string.config_recentsComponent);
        if (string == null || string.length() == 0) {
            throw new RuntimeException("No recents component configured", null);
        }
        RecentsImplementation resolveRecents = contextComponentHelper.resolveRecents(string);
        if (resolveRecents != null) {
            return resolveRecents;
        }
        try {
            try {
                return (RecentsImplementation) context.getClassLoader().loadClass(string).newInstance();
            } catch (Throwable th) {
                throw new RuntimeException("Error creating recents component: " + string, th);
            }
        } catch (Throwable th2) {
            throw new RuntimeException("Error loading recents component: " + string, th2);
        }
    }
}
