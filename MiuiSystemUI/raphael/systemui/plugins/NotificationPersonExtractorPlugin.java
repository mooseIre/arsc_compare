package com.android.systemui.plugins;

import android.graphics.drawable.Drawable;
import android.service.notification.StatusBarNotification;
import com.android.systemui.plugins.annotations.DependsOn;
import com.android.systemui.plugins.annotations.ProvidesInterface;

@DependsOn(target = PersonData.class)
@ProvidesInterface(action = "com.android.systemui.action.PEOPLE_HUB_PERSON_EXTRACTOR", version = 1)
public interface NotificationPersonExtractorPlugin extends Plugin {
    public static final String ACTION = "com.android.systemui.action.PEOPLE_HUB_PERSON_EXTRACTOR";
    public static final int VERSION = 1;

    PersonData extractPerson(StatusBarNotification statusBarNotification);

    String extractPersonKey(StatusBarNotification statusBarNotification) {
        return extractPerson(statusBarNotification).key;
    }

    boolean isPersonNotification(StatusBarNotification statusBarNotification) {
        return extractPersonKey(statusBarNotification) != null;
    }

    @ProvidesInterface(version = 0)
    public static final class PersonData {
        public static final int VERSION = 0;
        public final Drawable avatar;
        public final Runnable clickRunnable;
        public final String key;
        public final CharSequence name;

        public PersonData(String str, CharSequence charSequence, Drawable drawable, Runnable runnable) {
            this.key = str;
            this.name = charSequence;
            this.avatar = drawable;
            this.clickRunnable = runnable;
        }
    }
}
