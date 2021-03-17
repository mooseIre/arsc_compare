package com.android.systemui.qs.logging;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: QSLogger.kt */
public final class QSLogger$logTileUpdated$2 extends Lambda implements Function1<LogMessage, String> {
    public static final QSLogger$logTileUpdated$2 INSTANCE = new QSLogger$logTileUpdated$2();

    QSLogger$logTileUpdated$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        String str;
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(logMessage.getStr1());
        sb.append("] Tile updated. Label=");
        sb.append(logMessage.getStr2());
        sb.append(". State=");
        sb.append(logMessage.getInt1());
        sb.append(". Icon=");
        sb.append(logMessage.getStr3());
        sb.append('.');
        if (logMessage.getBool1()) {
            str = " Activity in/out=" + logMessage.getBool2() + '/' + logMessage.getBool3();
        } else {
            str = "";
        }
        sb.append(str);
        return sb.toString();
    }
}
