package com.android.systemui.statusbar.notification.row;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.C0015R$id;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.icon.IconPack;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: DungeonRow.kt */
public final class DungeonRow extends LinearLayout {
    @Nullable
    private NotificationEntry entry;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public DungeonRow(@NotNull Context context, @NotNull AttributeSet attributeSet) {
        super(context, attributeSet);
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(attributeSet, "attrs");
    }

    @Nullable
    public final NotificationEntry getEntry() {
        return this.entry;
    }

    public final void setEntry(@Nullable NotificationEntry notificationEntry) {
        this.entry = notificationEntry;
        update();
    }

    private final void update() {
        IconPack icons;
        StatusBarIconView statusBarIcon;
        ExpandableNotificationRow row;
        View findViewById = findViewById(C0015R$id.app_name);
        if (findViewById != null) {
            TextView textView = (TextView) findViewById;
            NotificationEntry notificationEntry = this.entry;
            StatusBarIcon statusBarIcon2 = null;
            textView.setText((notificationEntry == null || (row = notificationEntry.getRow()) == null) ? null : row.getAppName());
            View findViewById2 = findViewById(C0015R$id.icon);
            if (findViewById2 != null) {
                StatusBarIconView statusBarIconView = (StatusBarIconView) findViewById2;
                NotificationEntry notificationEntry2 = this.entry;
                if (!(notificationEntry2 == null || (icons = notificationEntry2.getIcons()) == null || (statusBarIcon = icons.getStatusBarIcon()) == null)) {
                    statusBarIcon2 = statusBarIcon.getStatusBarIcon();
                }
                statusBarIconView.set(statusBarIcon2);
                return;
            }
            throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.statusbar.StatusBarIconView");
        }
        throw new TypeCastException("null cannot be cast to non-null type android.widget.TextView");
    }
}
