package com.android.systemui.statusbar.notification.stack;

import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import com.miui.systemui.animation.PhysicBasedInterpolator;
import java.util.List;
import kotlin.collections.CollectionsKt;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiNotificationAnimationExtensions.kt */
public final class SpringAnimationEvent extends NotificationStackScrollLayout.AnimationEvent {
    public static final Companion Companion = new Companion(null);
    private static final List<Companion.DummyFolmeData> NOTIFICATION_SPRING_TABLE = CollectionsKt__CollectionsKt.listOf((Object[]) new Companion.DummyFolmeData[]{new Companion.DummyFolmeData(0.7f, 0.625f, 800), new Companion.DummyFolmeData(0.74f, 0.743f, 700), new Companion.DummyFolmeData(0.78f, 0.72f, 750), new Companion.DummyFolmeData(0.82f, 0.7467f, 750), new Companion.DummyFolmeData(0.86f, 0.725f, 800), new Companion.DummyFolmeData(0.9f, 0.8f, 750), new Companion.DummyFolmeData(0.94f, 0.8857f, 700), new Companion.DummyFolmeData(0.98f, 0.7529f, 850), new Companion.DummyFolmeData(0.99f, 0.7529f, 900)});

    public SpringAnimationEvent(int i) {
        super(null, 16, 450, MiuiNotificationAnimations.INSTANCE.getRELEASE_SPRING_FILTER());
    }

    /* compiled from: MiuiNotificationAnimationExtensions.kt */
    public static final class Companion {

        /* access modifiers changed from: private */
        /* compiled from: MiuiNotificationAnimationExtensions.kt */
        public static final class DummyFolmeData {
            private final float damping;
            private final long duration;
            private final float response;

            public boolean equals(@Nullable Object obj) {
                if (this == obj) {
                    return true;
                }
                if (!(obj instanceof DummyFolmeData)) {
                    return false;
                }
                DummyFolmeData dummyFolmeData = (DummyFolmeData) obj;
                return Float.compare(this.damping, dummyFolmeData.damping) == 0 && Float.compare(this.response, dummyFolmeData.response) == 0 && this.duration == dummyFolmeData.duration;
            }

            public int hashCode() {
                return (((Float.hashCode(this.damping) * 31) + Float.hashCode(this.response)) * 31) + Long.hashCode(this.duration);
            }

            @NotNull
            public String toString() {
                return "DummyFolmeData(damping=" + this.damping + ", response=" + this.response + ", duration=" + this.duration + ")";
            }

            public DummyFolmeData(float f, float f2, long j) {
                this.damping = f;
                this.response = f2;
                this.duration = j;
            }

            public final long getDuration() {
                return this.duration;
            }

            @NotNull
            public final PhysicBasedInterpolator toInterpolator() {
                PhysicBasedInterpolator.Builder builder = new PhysicBasedInterpolator.Builder();
                builder.setDamping(this.damping);
                builder.setResponse(this.response);
                return builder.build();
            }
        }

        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private final DummyFolmeData getFolmeDataForIndex(int i) {
            List list = SpringAnimationEvent.NOTIFICATION_SPRING_TABLE;
            return (DummyFolmeData) ((i < 0 || i > CollectionsKt__CollectionsKt.getLastIndex(list)) ? (DummyFolmeData) CollectionsKt.last(SpringAnimationEvent.NOTIFICATION_SPRING_TABLE) : list.get(i));
        }

        public final long getDurationForIndex$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(int i) {
            return getFolmeDataForIndex(i).getDuration();
        }

        @NotNull
        public final PhysicBasedInterpolator getInterpolatorForIndex$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(int i) {
            return getFolmeDataForIndex(i).toInterpolator();
        }
    }
}
