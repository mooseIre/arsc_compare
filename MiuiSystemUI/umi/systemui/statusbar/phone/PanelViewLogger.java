package com.android.systemui.statusbar.phone;

import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogLevel;
import com.android.systemui.log.LogMessageImpl;
import kotlin.jvm.internal.Intrinsics;

public final class PanelViewLogger {
    private final LogBuffer buffer;

    public PanelViewLogger(LogBuffer logBuffer) {
        Intrinsics.checkParameterIsNotNull(logBuffer, "buffer");
        this.buffer = logBuffer;
    }

    public final void logBlurAnimStart(float f) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        PanelViewLogger$logBlurAnimStart$2 panelViewLogger$logBlurAnimStart$2 = PanelViewLogger$logBlurAnimStart$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("PanelView", logLevel, panelViewLogger$logBlurAnimStart$2);
            obtain.setStr1(String.valueOf(f));
            logBuffer.push(obtain);
        }
    }

    public final void logBlurAnimUpdate(float f, float f2) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        PanelViewLogger$logBlurAnimUpdate$2 panelViewLogger$logBlurAnimUpdate$2 = PanelViewLogger$logBlurAnimUpdate$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("PanelView", logLevel, panelViewLogger$logBlurAnimUpdate$2);
            obtain.setStr1(String.valueOf(f));
            obtain.setDouble1((double) f2);
            logBuffer.push(obtain);
        }
    }

    public final void logBlurAnimEnd(boolean z, float f) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        PanelViewLogger$logBlurAnimEnd$2 panelViewLogger$logBlurAnimEnd$2 = PanelViewLogger$logBlurAnimEnd$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("PanelView", logLevel, panelViewLogger$logBlurAnimEnd$2);
            obtain.setBool1(z);
            obtain.setStr1(String.valueOf(f));
            logBuffer.push(obtain);
        }
    }

    public static /* synthetic */ void logBlurRequest$default(PanelViewLogger panelViewLogger, String str, float f, float f2, boolean z, int i, Object obj) {
        if ((i & 8) != 0) {
            z = true;
        }
        panelViewLogger.logBlurRequest(str, f, f2, z);
    }

    public final void logBlurRequest(String str, float f, float f2, boolean z) {
        Intrinsics.checkParameterIsNotNull(str, "reason");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        PanelViewLogger$logBlurRequest$2 panelViewLogger$logBlurRequest$2 = new PanelViewLogger$logBlurRequest$2(z);
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("PanelView", logLevel, panelViewLogger$logBlurRequest$2);
            obtain.setBool1(z);
            obtain.setStr1(str);
            obtain.setStr2(String.valueOf(f));
            obtain.setStr3(String.valueOf(f2));
            logBuffer.push(obtain);
        }
    }

    public final void logUpdateBlurPanelState(boolean z, boolean z2, boolean z3, boolean z4, int i, boolean z5, boolean z6, float f, boolean z7, boolean z8, boolean z9, boolean z10, float f2, float f3) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(z ? "Br-" : "br-");
        stringBuffer.append(z2 ? "Big-" : "big-");
        stringBuffer.append(z3 ? "Sw-" : "sw-");
        stringBuffer.append(z4 ? "Act-" : "act-");
        stringBuffer.append(z5 ? "Bsig-" : "bsig-");
        stringBuffer.append(z6 ? "Ko-" : "ko-");
        stringBuffer.append(i);
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        PanelViewLogger$logUpdateBlurPanelState$2 panelViewLogger$logUpdateBlurPanelState$2 = PanelViewLogger$logUpdateBlurPanelState$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("PanelView", logLevel, panelViewLogger$logUpdateBlurPanelState$2);
            obtain.setStr1(stringBuffer.toString());
            obtain.setDouble1((double) f);
            obtain.setBool1(z7);
            obtain.setBool2(z8);
            obtain.setBool3(z9);
            obtain.setBool4(z10);
            obtain.setInt1((int) f2);
            obtain.setInt2((int) f3);
            logBuffer.push(obtain);
        }
    }

    public final void logNcSwitch(boolean z) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        PanelViewLogger$logNcSwitch$2 panelViewLogger$logNcSwitch$2 = PanelViewLogger$logNcSwitch$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("PanelView", logLevel, panelViewLogger$logNcSwitch$2);
            obtain.setBool1(z);
            logBuffer.push(obtain);
        }
    }

    public final void logNcSwitchError(boolean z, boolean z2) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        PanelViewLogger$logNcSwitchError$2 panelViewLogger$logNcSwitchError$2 = PanelViewLogger$logNcSwitchError$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("PanelView", logLevel, panelViewLogger$logNcSwitchError$2);
            obtain.setBool1(z);
            obtain.setBool2(z2);
            logBuffer.push(obtain);
        }
    }
}
