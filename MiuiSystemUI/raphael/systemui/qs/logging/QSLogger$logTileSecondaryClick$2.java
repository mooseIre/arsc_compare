package com.android.systemui.qs.logging;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: QSLogger.kt */
public final class QSLogger$logTileSecondaryClick$2 extends Lambda implements Function1<LogMessage, String> {
    public static final QSLogger$logTileSecondaryClick$2 INSTANCE = new QSLogger$logTileSecondaryClick$2();

    QSLogger$logTileSecondaryClick$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return '[' + logMessage.getStr1() + "] Tile long clicked. StatusBarState=" + logMessage.getStr2() + ". TileState=" + logMessage.getStr3();
    }
}
