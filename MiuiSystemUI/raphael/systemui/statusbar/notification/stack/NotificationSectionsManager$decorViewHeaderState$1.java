package com.android.systemui.statusbar.notification.stack;

import com.android.systemui.statusbar.notification.row.StackScrollerDecorView;
import com.android.systemui.statusbar.notification.stack.NotificationSectionsManager;
import org.jetbrains.annotations.Nullable;

/* compiled from: NotificationSectionsManager.kt */
public final class NotificationSectionsManager$decorViewHeaderState$1 implements NotificationSectionsManager.SectionUpdateState<T> {
    private final /* synthetic */ NotificationSectionsManager.SectionUpdateState $$delegate_0;
    final /* synthetic */ StackScrollerDecorView $header;
    final /* synthetic */ NotificationSectionsManager.SectionUpdateState $inner;

    @Nullable
    public Integer getCurrentPosition() {
        return this.$$delegate_0.getCurrentPosition();
    }

    @Nullable
    public Integer getTargetPosition() {
        return this.$$delegate_0.getTargetPosition();
    }

    public void setCurrentPosition(@Nullable Integer num) {
        this.$$delegate_0.setCurrentPosition(num);
    }

    public void setTargetPosition(@Nullable Integer num) {
        this.$$delegate_0.setTargetPosition(num);
    }

    NotificationSectionsManager$decorViewHeaderState$1(NotificationSectionsManager.SectionUpdateState sectionUpdateState, StackScrollerDecorView stackScrollerDecorView) {
        this.$inner = sectionUpdateState;
        this.$header = stackScrollerDecorView;
        this.$$delegate_0 = sectionUpdateState;
    }

    public void adjustViewPosition() {
        this.$inner.adjustViewPosition();
        if (getTargetPosition() != null && getCurrentPosition() == null) {
            this.$header.setContentVisible(true);
        }
    }
}
