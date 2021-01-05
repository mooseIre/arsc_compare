package com.android.systemui.statusbar.notification.collection.listbuilder;

import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogLevel;
import com.android.systemui.log.LogMessageImpl;
import com.android.systemui.statusbar.notification.collection.GroupEntry;
import com.android.systemui.statusbar.notification.collection.ListEntry;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifFilter;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifPromoter;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifSection;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ShadeListBuilderLogger.kt */
public final class ShadeListBuilderLogger {
    private final LogBuffer buffer;

    public ShadeListBuilderLogger(@NotNull LogBuffer logBuffer) {
        Intrinsics.checkParameterIsNotNull(logBuffer, "buffer");
        this.buffer = logBuffer;
    }

    public final void logOnBuildList() {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        ShadeListBuilderLogger$logOnBuildList$2 shadeListBuilderLogger$logOnBuildList$2 = ShadeListBuilderLogger$logOnBuildList$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            logBuffer.push(logBuffer.obtain("ShadeListBuilder", logLevel, shadeListBuilderLogger$logOnBuildList$2));
        }
    }

    public final void logEndBuildList(int i, int i2, int i3) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        ShadeListBuilderLogger$logEndBuildList$2 shadeListBuilderLogger$logEndBuildList$2 = ShadeListBuilderLogger$logEndBuildList$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("ShadeListBuilder", logLevel, shadeListBuilderLogger$logEndBuildList$2);
            obtain.setLong1((long) i);
            obtain.setInt1(i2);
            obtain.setInt2(i3);
            logBuffer.push(obtain);
        }
    }

    public final void logPreGroupFilterInvalidated(@NotNull String str, int i) {
        Intrinsics.checkParameterIsNotNull(str, "filterName");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        ShadeListBuilderLogger$logPreGroupFilterInvalidated$2 shadeListBuilderLogger$logPreGroupFilterInvalidated$2 = ShadeListBuilderLogger$logPreGroupFilterInvalidated$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("ShadeListBuilder", logLevel, shadeListBuilderLogger$logPreGroupFilterInvalidated$2);
            obtain.setStr1(str);
            obtain.setInt1(i);
            logBuffer.push(obtain);
        }
    }

    public final void logPromoterInvalidated(@NotNull String str, int i) {
        Intrinsics.checkParameterIsNotNull(str, "name");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        ShadeListBuilderLogger$logPromoterInvalidated$2 shadeListBuilderLogger$logPromoterInvalidated$2 = ShadeListBuilderLogger$logPromoterInvalidated$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("ShadeListBuilder", logLevel, shadeListBuilderLogger$logPromoterInvalidated$2);
            obtain.setStr1(str);
            obtain.setInt1(i);
            logBuffer.push(obtain);
        }
    }

    public final void logNotifSectionInvalidated(@NotNull String str, int i) {
        Intrinsics.checkParameterIsNotNull(str, "name");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        ShadeListBuilderLogger$logNotifSectionInvalidated$2 shadeListBuilderLogger$logNotifSectionInvalidated$2 = ShadeListBuilderLogger$logNotifSectionInvalidated$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("ShadeListBuilder", logLevel, shadeListBuilderLogger$logNotifSectionInvalidated$2);
            obtain.setStr1(str);
            obtain.setInt1(i);
            logBuffer.push(obtain);
        }
    }

    public final void logFinalizeFilterInvalidated(@NotNull String str, int i) {
        Intrinsics.checkParameterIsNotNull(str, "name");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        ShadeListBuilderLogger$logFinalizeFilterInvalidated$2 shadeListBuilderLogger$logFinalizeFilterInvalidated$2 = ShadeListBuilderLogger$logFinalizeFilterInvalidated$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("ShadeListBuilder", logLevel, shadeListBuilderLogger$logFinalizeFilterInvalidated$2);
            obtain.setStr1(str);
            obtain.setInt1(i);
            logBuffer.push(obtain);
        }
    }

    public final void logDuplicateSummary(int i, @NotNull String str, @NotNull String str2, @NotNull String str3) {
        Intrinsics.checkParameterIsNotNull(str, "groupKey");
        Intrinsics.checkParameterIsNotNull(str2, "existingKey");
        Intrinsics.checkParameterIsNotNull(str3, "newKey");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.WARNING;
        ShadeListBuilderLogger$logDuplicateSummary$2 shadeListBuilderLogger$logDuplicateSummary$2 = ShadeListBuilderLogger$logDuplicateSummary$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("ShadeListBuilder", logLevel, shadeListBuilderLogger$logDuplicateSummary$2);
            obtain.setInt1(i);
            obtain.setStr1(str);
            obtain.setStr2(str2);
            obtain.setStr3(str3);
            logBuffer.push(obtain);
        }
    }

    public final void logDuplicateTopLevelKey(int i, @NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "topLevelKey");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.WARNING;
        ShadeListBuilderLogger$logDuplicateTopLevelKey$2 shadeListBuilderLogger$logDuplicateTopLevelKey$2 = ShadeListBuilderLogger$logDuplicateTopLevelKey$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("ShadeListBuilder", logLevel, shadeListBuilderLogger$logDuplicateTopLevelKey$2);
            obtain.setInt1(i);
            obtain.setStr1(str);
            logBuffer.push(obtain);
        }
    }

    public final void logEntryAttachStateChanged(int i, @NotNull String str, @Nullable GroupEntry groupEntry, @Nullable GroupEntry groupEntry2) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        ShadeListBuilderLogger$logEntryAttachStateChanged$2 shadeListBuilderLogger$logEntryAttachStateChanged$2 = ShadeListBuilderLogger$logEntryAttachStateChanged$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("ShadeListBuilder", logLevel, shadeListBuilderLogger$logEntryAttachStateChanged$2);
            obtain.setInt1(i);
            obtain.setStr1(str);
            String str2 = null;
            obtain.setStr2(groupEntry != null ? groupEntry.getKey() : null);
            if (groupEntry2 != null) {
                str2 = groupEntry2.getKey();
            }
            obtain.setStr3(str2);
            logBuffer.push(obtain);
        }
    }

    public final void logParentChanged(int i, @Nullable GroupEntry groupEntry, @Nullable GroupEntry groupEntry2) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        ShadeListBuilderLogger$logParentChanged$2 shadeListBuilderLogger$logParentChanged$2 = ShadeListBuilderLogger$logParentChanged$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("ShadeListBuilder", logLevel, shadeListBuilderLogger$logParentChanged$2);
            obtain.setInt1(i);
            String str = null;
            obtain.setStr1(groupEntry != null ? groupEntry.getKey() : null);
            if (groupEntry2 != null) {
                str = groupEntry2.getKey();
            }
            obtain.setStr2(str);
            logBuffer.push(obtain);
        }
    }

    public final void logFilterChanged(int i, @Nullable NotifFilter notifFilter, @Nullable NotifFilter notifFilter2) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        ShadeListBuilderLogger$logFilterChanged$2 shadeListBuilderLogger$logFilterChanged$2 = ShadeListBuilderLogger$logFilterChanged$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("ShadeListBuilder", logLevel, shadeListBuilderLogger$logFilterChanged$2);
            obtain.setInt1(i);
            String str = null;
            obtain.setStr1(notifFilter != null ? notifFilter.getName() : null);
            if (notifFilter2 != null) {
                str = notifFilter2.getName();
            }
            obtain.setStr2(str);
            logBuffer.push(obtain);
        }
    }

    public final void logPromoterChanged(int i, @Nullable NotifPromoter notifPromoter, @Nullable NotifPromoter notifPromoter2) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        ShadeListBuilderLogger$logPromoterChanged$2 shadeListBuilderLogger$logPromoterChanged$2 = ShadeListBuilderLogger$logPromoterChanged$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("ShadeListBuilder", logLevel, shadeListBuilderLogger$logPromoterChanged$2);
            obtain.setInt1(i);
            String str = null;
            obtain.setStr1(notifPromoter != null ? notifPromoter.getName() : null);
            if (notifPromoter2 != null) {
                str = notifPromoter2.getName();
            }
            obtain.setStr2(str);
            logBuffer.push(obtain);
        }
    }

    public final void logSectionChanged(int i, @Nullable NotifSection notifSection, int i2, @Nullable NotifSection notifSection2, int i3) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        ShadeListBuilderLogger$logSectionChanged$2 shadeListBuilderLogger$logSectionChanged$2 = ShadeListBuilderLogger$logSectionChanged$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("ShadeListBuilder", logLevel, shadeListBuilderLogger$logSectionChanged$2);
            obtain.setLong1((long) i);
            String str = null;
            obtain.setStr1(notifSection != null ? notifSection.getName() : null);
            obtain.setInt1(i2);
            if (notifSection2 != null) {
                str = notifSection2.getName();
            }
            obtain.setStr2(str);
            obtain.setInt2(i3);
            logBuffer.push(obtain);
        }
    }

    public final void logFinalList(@NotNull List<? extends ListEntry> list) {
        Intrinsics.checkParameterIsNotNull(list, "entries");
        if (list.isEmpty()) {
            LogBuffer logBuffer = this.buffer;
            LogLevel logLevel = LogLevel.DEBUG;
            ShadeListBuilderLogger$logFinalList$2 shadeListBuilderLogger$logFinalList$2 = ShadeListBuilderLogger$logFinalList$2.INSTANCE;
            if (!logBuffer.getFrozen()) {
                logBuffer.push(logBuffer.obtain("ShadeListBuilder", logLevel, shadeListBuilderLogger$logFinalList$2));
            }
        }
        int size = list.size();
        for (int i = 0; i < size; i++) {
            ListEntry listEntry = (ListEntry) list.get(i);
            LogBuffer logBuffer2 = this.buffer;
            LogLevel logLevel2 = LogLevel.DEBUG;
            ShadeListBuilderLogger$logFinalList$4 shadeListBuilderLogger$logFinalList$4 = ShadeListBuilderLogger$logFinalList$4.INSTANCE;
            if (!logBuffer2.getFrozen()) {
                LogMessageImpl obtain = logBuffer2.obtain("ShadeListBuilder", logLevel2, shadeListBuilderLogger$logFinalList$4);
                obtain.setInt1(i);
                obtain.setStr1(listEntry.getKey());
                logBuffer2.push(obtain);
            }
            if (listEntry instanceof GroupEntry) {
                GroupEntry groupEntry = (GroupEntry) listEntry;
                NotificationEntry summary = groupEntry.getSummary();
                if (summary != null) {
                    LogBuffer logBuffer3 = this.buffer;
                    LogLevel logLevel3 = LogLevel.DEBUG;
                    ShadeListBuilderLogger$logFinalList$5$2 shadeListBuilderLogger$logFinalList$5$2 = ShadeListBuilderLogger$logFinalList$5$2.INSTANCE;
                    if (!logBuffer3.getFrozen()) {
                        LogMessageImpl obtain2 = logBuffer3.obtain("ShadeListBuilder", logLevel3, shadeListBuilderLogger$logFinalList$5$2);
                        Intrinsics.checkExpressionValueIsNotNull(summary, "it");
                        obtain2.setStr1(summary.getKey());
                        logBuffer3.push(obtain2);
                    }
                }
                List<NotificationEntry> children = groupEntry.getChildren();
                Intrinsics.checkExpressionValueIsNotNull(children, "entry.children");
                int size2 = children.size();
                for (int i2 = 0; i2 < size2; i2++) {
                    NotificationEntry notificationEntry = groupEntry.getChildren().get(i2);
                    LogBuffer logBuffer4 = this.buffer;
                    LogLevel logLevel4 = LogLevel.DEBUG;
                    ShadeListBuilderLogger$logFinalList$7 shadeListBuilderLogger$logFinalList$7 = ShadeListBuilderLogger$logFinalList$7.INSTANCE;
                    if (!logBuffer4.getFrozen()) {
                        LogMessageImpl obtain3 = logBuffer4.obtain("ShadeListBuilder", logLevel4, shadeListBuilderLogger$logFinalList$7);
                        obtain3.setInt1(i2);
                        Intrinsics.checkExpressionValueIsNotNull(notificationEntry, "child");
                        obtain3.setStr1(notificationEntry.getKey());
                        logBuffer4.push(obtain3);
                    }
                }
            }
        }
    }
}
