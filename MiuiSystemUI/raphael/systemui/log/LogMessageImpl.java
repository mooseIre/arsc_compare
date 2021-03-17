package com.android.systemui.log;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: LogMessageImpl.kt */
public final class LogMessageImpl implements LogMessage {
    public static final Factory Factory = new Factory((DefaultConstructorMarker) null);
    private boolean bool1;
    private boolean bool2;
    private boolean bool3;
    private boolean bool4;
    private double double1;
    private int int1;
    private int int2;
    @NotNull
    private LogLevel level;
    private long long1;
    private long long2;
    @NotNull
    private Function1<? super LogMessage, String> printer;
    @Nullable
    private String str1;
    @Nullable
    private String str2;
    @Nullable
    private String str3;
    @NotNull
    private String tag;
    private long timestamp;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LogMessageImpl)) {
            return false;
        }
        LogMessageImpl logMessageImpl = (LogMessageImpl) obj;
        return Intrinsics.areEqual((Object) getLevel(), (Object) logMessageImpl.getLevel()) && Intrinsics.areEqual((Object) getTag(), (Object) logMessageImpl.getTag()) && getTimestamp() == logMessageImpl.getTimestamp() && Intrinsics.areEqual((Object) getPrinter(), (Object) logMessageImpl.getPrinter()) && Intrinsics.areEqual((Object) getStr1(), (Object) logMessageImpl.getStr1()) && Intrinsics.areEqual((Object) getStr2(), (Object) logMessageImpl.getStr2()) && Intrinsics.areEqual((Object) getStr3(), (Object) logMessageImpl.getStr3()) && getInt1() == logMessageImpl.getInt1() && getInt2() == logMessageImpl.getInt2() && getLong1() == logMessageImpl.getLong1() && getLong2() == logMessageImpl.getLong2() && Double.compare(getDouble1(), logMessageImpl.getDouble1()) == 0 && getBool1() == logMessageImpl.getBool1() && getBool2() == logMessageImpl.getBool2() && getBool3() == logMessageImpl.getBool3() && getBool4() == logMessageImpl.getBool4();
    }

    public int hashCode() {
        LogLevel level2 = getLevel();
        int i = 0;
        int hashCode = (level2 != null ? level2.hashCode() : 0) * 31;
        String tag2 = getTag();
        int hashCode2 = (((hashCode + (tag2 != null ? tag2.hashCode() : 0)) * 31) + Long.hashCode(getTimestamp())) * 31;
        Function1<LogMessage, String> printer2 = getPrinter();
        int hashCode3 = (hashCode2 + (printer2 != null ? printer2.hashCode() : 0)) * 31;
        String str12 = getStr1();
        int hashCode4 = (hashCode3 + (str12 != null ? str12.hashCode() : 0)) * 31;
        String str22 = getStr2();
        int hashCode5 = (hashCode4 + (str22 != null ? str22.hashCode() : 0)) * 31;
        String str32 = getStr3();
        if (str32 != null) {
            i = str32.hashCode();
        }
        int hashCode6 = (((((((((((hashCode5 + i) * 31) + Integer.hashCode(getInt1())) * 31) + Integer.hashCode(getInt2())) * 31) + Long.hashCode(getLong1())) * 31) + Long.hashCode(getLong2())) * 31) + Double.hashCode(getDouble1())) * 31;
        boolean bool12 = getBool1();
        boolean z = true;
        if (bool12) {
            bool12 = true;
        }
        int i2 = (hashCode6 + (bool12 ? 1 : 0)) * 31;
        boolean bool22 = getBool2();
        if (bool22) {
            bool22 = true;
        }
        int i3 = (i2 + (bool22 ? 1 : 0)) * 31;
        boolean bool32 = getBool3();
        if (bool32) {
            bool32 = true;
        }
        int i4 = (i3 + (bool32 ? 1 : 0)) * 31;
        boolean bool42 = getBool4();
        if (!bool42) {
            z = bool42;
        }
        return i4 + (z ? 1 : 0);
    }

    @NotNull
    public String toString() {
        return "LogMessageImpl(level=" + getLevel() + ", tag=" + getTag() + ", timestamp=" + getTimestamp() + ", printer=" + getPrinter() + ", str1=" + getStr1() + ", str2=" + getStr2() + ", str3=" + getStr3() + ", int1=" + getInt1() + ", int2=" + getInt2() + ", long1=" + getLong1() + ", long2=" + getLong2() + ", double1=" + getDouble1() + ", bool1=" + getBool1() + ", bool2=" + getBool2() + ", bool3=" + getBool3() + ", bool4=" + getBool4() + ")";
    }

    public LogMessageImpl(@NotNull LogLevel logLevel, @NotNull String str, long j, @NotNull Function1<? super LogMessage, String> function1, @Nullable String str4, @Nullable String str5, @Nullable String str6, int i, int i2, long j2, long j3, double d, boolean z, boolean z2, boolean z3, boolean z4) {
        Intrinsics.checkParameterIsNotNull(logLevel, "level");
        Intrinsics.checkParameterIsNotNull(str, "tag");
        Intrinsics.checkParameterIsNotNull(function1, "printer");
        this.level = logLevel;
        this.tag = str;
        this.timestamp = j;
        this.printer = function1;
        this.str1 = str4;
        this.str2 = str5;
        this.str3 = str6;
        this.int1 = i;
        this.int2 = i2;
        this.long1 = j2;
        this.long2 = j3;
        this.double1 = d;
        this.bool1 = z;
        this.bool2 = z2;
        this.bool3 = z3;
        this.bool4 = z4;
    }

    @NotNull
    public LogLevel getLevel() {
        return this.level;
    }

    public void setLevel(@NotNull LogLevel logLevel) {
        Intrinsics.checkParameterIsNotNull(logLevel, "<set-?>");
        this.level = logLevel;
    }

    @NotNull
    public String getTag() {
        return this.tag;
    }

    public void setTag(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "<set-?>");
        this.tag = str;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long j) {
        this.timestamp = j;
    }

    @NotNull
    public Function1<LogMessage, String> getPrinter() {
        return this.printer;
    }

    public void setPrinter(@NotNull Function1<? super LogMessage, String> function1) {
        Intrinsics.checkParameterIsNotNull(function1, "<set-?>");
        this.printer = function1;
    }

    @Nullable
    public String getStr1() {
        return this.str1;
    }

    public void setStr1(@Nullable String str) {
        this.str1 = str;
    }

    @Nullable
    public String getStr2() {
        return this.str2;
    }

    public void setStr2(@Nullable String str) {
        this.str2 = str;
    }

    @Nullable
    public String getStr3() {
        return this.str3;
    }

    public void setStr3(@Nullable String str) {
        this.str3 = str;
    }

    public int getInt1() {
        return this.int1;
    }

    public void setInt1(int i) {
        this.int1 = i;
    }

    public int getInt2() {
        return this.int2;
    }

    public void setInt2(int i) {
        this.int2 = i;
    }

    public long getLong1() {
        return this.long1;
    }

    public void setLong1(long j) {
        this.long1 = j;
    }

    public long getLong2() {
        return this.long2;
    }

    public void setLong2(long j) {
        this.long2 = j;
    }

    public double getDouble1() {
        return this.double1;
    }

    public void setDouble1(double d) {
        this.double1 = d;
    }

    public boolean getBool1() {
        return this.bool1;
    }

    public void setBool1(boolean z) {
        this.bool1 = z;
    }

    public boolean getBool2() {
        return this.bool2;
    }

    public void setBool2(boolean z) {
        this.bool2 = z;
    }

    public boolean getBool3() {
        return this.bool3;
    }

    public void setBool3(boolean z) {
        this.bool3 = z;
    }

    public boolean getBool4() {
        return this.bool4;
    }

    public void setBool4(boolean z) {
        this.bool4 = z;
    }

    public final void reset(@NotNull String str, @NotNull LogLevel logLevel, long j, @NotNull Function1<? super LogMessage, String> function1) {
        Intrinsics.checkParameterIsNotNull(str, "tag");
        Intrinsics.checkParameterIsNotNull(logLevel, "level");
        Intrinsics.checkParameterIsNotNull(function1, "renderer");
        setLevel(logLevel);
        setTag(str);
        setTimestamp(j);
        setPrinter(function1);
        setStr1((String) null);
        setStr2((String) null);
        setStr3((String) null);
        setInt1(0);
        setInt2(0);
        setLong1(0);
        setLong2(0);
        setDouble1(0.0d);
        setBool1(false);
        setBool2(false);
        setBool3(false);
        setBool4(false);
    }

    /* compiled from: LogMessageImpl.kt */
    public static final class Factory {
        private Factory() {
        }

        public /* synthetic */ Factory(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final LogMessageImpl create() {
            return new LogMessageImpl(LogLevel.DEBUG, "UnknownTag", 0, LogMessageImplKt.DEFAULT_RENDERER, (String) null, (String) null, (String) null, 0, 0, 0, 0, 0.0d, false, false, false, false);
        }
    }
}
