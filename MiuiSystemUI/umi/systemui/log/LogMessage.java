package com.android.systemui.log;

import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: LogMessage.kt */
public interface LogMessage {
    boolean getBool1();

    boolean getBool2();

    boolean getBool3();

    boolean getBool4();

    double getDouble1();

    int getInt1();

    int getInt2();

    @NotNull
    LogLevel getLevel();

    long getLong1();

    long getLong2();

    @NotNull
    Function1<LogMessage, String> getPrinter();

    @Nullable
    String getStr1();

    @Nullable
    String getStr2();

    @Nullable
    String getStr3();

    @NotNull
    String getTag();

    long getTimestamp();

    void setBool1(boolean z);

    void setBool2(boolean z);

    void setBool3(boolean z);

    void setBool4(boolean z);

    void setDouble1(double d);

    void setInt1(int i);

    void setInt2(int i);

    void setLong1(long j);

    void setLong2(long j);

    void setStr1(@Nullable String str);

    void setStr2(@Nullable String str);

    void setStr3(@Nullable String str);
}
