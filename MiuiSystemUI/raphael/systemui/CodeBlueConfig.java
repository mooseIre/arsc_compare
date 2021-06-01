package com.android.systemui;

import android.content.Context;
import java.util.concurrent.TimeUnit;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: CodeBlue.kt */
public final class CodeBlueConfig {
    private static final int CODE_BLUE_TRIGGER_COUNT = 3;
    private static final long CODE_BLUE_TRIGGER_WINDOW_MS = TimeUnit.SECONDS.toMillis(10);
    public static final Companion Companion = new Companion(null);
    private static final int FIX_CRASH_TRIGGER_COUNT = 2;
    private static final long NOTIFICATION_TRIGGER_WINDOW_MS = TimeUnit.SECONDS.toMillis(1);

    /* compiled from: CodeBlue.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        public final int getFIX_CRASH_TRIGGER_COUNT() {
            return CodeBlueConfig.FIX_CRASH_TRIGGER_COUNT;
        }

        public final int getCODE_BLUE_TRIGGER_COUNT() {
            return CodeBlueConfig.CODE_BLUE_TRIGGER_COUNT;
        }

        public final long getCODE_BLUE_TRIGGER_WINDOW_MS() {
            return CodeBlueConfig.CODE_BLUE_TRIGGER_WINDOW_MS;
        }

        public final long getNOTIFICATION_TRIGGER_WINDOW_MS() {
            return CodeBlueConfig.NOTIFICATION_TRIGGER_WINDOW_MS;
        }

        public final void setCrashCount(@NotNull Context context, int i) {
            Intrinsics.checkParameterIsNotNull(context, "context");
            context.getSharedPreferences("code_blue", 0).edit().putInt("crash_count", i).commit();
        }

        public final int getCrashCount(@NotNull Context context) {
            Intrinsics.checkParameterIsNotNull(context, "context");
            return context.getSharedPreferences("code_blue", 0).getInt("crash_count", 0);
        }

        public final void setLastCrashTimestamp(@NotNull Context context, long j) {
            Intrinsics.checkParameterIsNotNull(context, "context");
            context.getSharedPreferences("code_blue", 0).edit().putLong("last_crash_timestamp", j).commit();
        }

        public final long getLastCrashTimestamp(@NotNull Context context) {
            Intrinsics.checkParameterIsNotNull(context, "context");
            return context.getSharedPreferences("code_blue", 0).getLong("last_crash_timestamp", CodeBlueConfig.CODE_BLUE_TRIGGER_WINDOW_MS);
        }

        public final void setExceptionHandler(@NotNull Context context, @NotNull String str) {
            Intrinsics.checkParameterIsNotNull(context, "context");
            Intrinsics.checkParameterIsNotNull(str, "handler");
            context.getSharedPreferences("code_blue", 0).edit().putString("exception_handler", str).commit();
        }

        @Nullable
        public final String getExceptionHandler(@NotNull Context context) {
            Intrinsics.checkParameterIsNotNull(context, "context");
            return context.getSharedPreferences("code_blue", 0).getString("exception_handler", null);
        }

        public final void setExceptionClues(@NotNull Context context, @NotNull String str) {
            Intrinsics.checkParameterIsNotNull(context, "context");
            Intrinsics.checkParameterIsNotNull(str, "pkg_name");
            context.getSharedPreferences("code_blue", 0).edit().putString("exception_clues", str).commit();
        }

        @Nullable
        public final String getExceptionClues(@NotNull Context context) {
            Intrinsics.checkParameterIsNotNull(context, "context");
            return context.getSharedPreferences("code_blue", 0).getString("exception_clues", null);
        }

        public final boolean isFirstTimeFixCrash(@NotNull Context context) {
            Intrinsics.checkParameterIsNotNull(context, "context");
            return getCrashCount(context) == getFIX_CRASH_TRIGGER_COUNT();
        }

        public final void setTrackCodeBlue(@NotNull Context context, boolean z) {
            Intrinsics.checkParameterIsNotNull(context, "context");
            context.getSharedPreferences("code_blue", 0).edit().putBoolean("track_code_blue", z).commit();
        }

        public final boolean getTrackCodeBlue(@NotNull Context context) {
            Intrinsics.checkParameterIsNotNull(context, "context");
            return context.getSharedPreferences("code_blue", 0).getBoolean("track_code_blue", false);
        }
    }
}
