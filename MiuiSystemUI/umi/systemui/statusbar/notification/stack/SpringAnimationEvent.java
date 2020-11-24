package com.android.systemui.statusbar.notification.stack;

import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import com.miui.systemui.animation.PhysicBasedInterpolator;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiNotificationAnimationExtensions.kt */
public final class SpringAnimationEvent extends NotificationStackScrollLayout.AnimationEvent {
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    /* access modifiers changed from: private */
    public static final List<Companion.DummyFolmeData> NOTIFICATION_SPRING_TABLE = CollectionsKt__CollectionsKt.listOf(new Companion.DummyFolmeData(0.7f, 0.625f, 800), new Companion.DummyFolmeData(0.74f, 0.743f, 700), new Companion.DummyFolmeData(0.78f, 0.72f, 750), new Companion.DummyFolmeData(0.82f, 0.7467f, 750), new Companion.DummyFolmeData(0.86f, 0.725f, 800), new Companion.DummyFolmeData(0.9f, 0.8f, 750), new Companion.DummyFolmeData(0.94f, 0.8857f, 700), new Companion.DummyFolmeData(0.98f, 0.7529f, 850), new Companion.DummyFolmeData(1.02f, 0.66f, 1000), new Companion.DummyFolmeData(1.06f, 0.59f, 1150));

    public SpringAnimationEvent(int i) {
        super((ExpandableView) null, 16, Companion.getMaxDuration(i), MiuiNotificationAnimations.INSTANCE.getRELEASE_SPRING_FILTER());
    }

    /* compiled from: MiuiNotificationAnimationExtensions.kt */
    public static final class Companion {

        /* compiled from: MiuiNotificationAnimationExtensions.kt */
        private static final class DummyFolmeData {
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

        /* access modifiers changed from: private */
        public final long getMaxDuration(int i) {
            List<DummyFolmeData> subList = SpringAnimationEvent.NOTIFICATION_SPRING_TABLE.subList(0, RangesKt___RangesKt.coerceAtMost(i, CollectionsKt__CollectionsKt.getLastIndex(SpringAnimationEvent.NOTIFICATION_SPRING_TABLE)));
            ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(subList, 10));
            for (DummyFolmeData duration : subList) {
                arrayList.add(Long.valueOf(duration.getDuration()));
            }
            Long l = (Long) CollectionsKt___CollectionsKt.max(arrayList);
            if (l != null) {
                return l.longValue();
            }
            return 750;
        }

        private final DummyFolmeData getFolmeDataForIndex(int i) {
            List access$getNOTIFICATION_SPRING_TABLE$cp = SpringAnimationEvent.NOTIFICATION_SPRING_TABLE;
            return (DummyFolmeData) ((i < 0 || i > CollectionsKt__CollectionsKt.getLastIndex(access$getNOTIFICATION_SPRING_TABLE$cp)) ? (DummyFolmeData) CollectionsKt___CollectionsKt.last(SpringAnimationEvent.NOTIFICATION_SPRING_TABLE) : access$getNOTIFICATION_SPRING_TABLE$cp.get(i));
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
