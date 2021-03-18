package com.android.systemui.statusbar.notification.stack;

import android.view.ViewGroup;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.stack.NotificationSectionsManager;
import org.jetbrains.annotations.Nullable;

/* compiled from: NotificationSectionsManager.kt */
public final class NotificationSectionsManager$expandableViewHeaderState$1 implements NotificationSectionsManager.SectionUpdateState<T> {
    final /* synthetic */ ExpandableView $header;
    @Nullable
    private Integer currentPosition;
    @Nullable
    private Integer targetPosition;
    final /* synthetic */ NotificationSectionsManager this$0;

    NotificationSectionsManager$expandableViewHeaderState$1(NotificationSectionsManager notificationSectionsManager, ExpandableView expandableView) {
        this.this$0 = notificationSectionsManager;
        this.$header = expandableView;
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.SectionUpdateState
    @Nullable
    public Integer getCurrentPosition() {
        return this.currentPosition;
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.SectionUpdateState
    public void setCurrentPosition(@Nullable Integer num) {
        this.currentPosition = num;
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.SectionUpdateState
    @Nullable
    public Integer getTargetPosition() {
        return this.targetPosition;
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.SectionUpdateState
    public void setTargetPosition(@Nullable Integer num) {
        this.targetPosition = num;
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.SectionUpdateState
    public void adjustViewPosition() {
        Integer targetPosition2 = getTargetPosition();
        Integer currentPosition2 = getCurrentPosition();
        if (targetPosition2 == null) {
            if (currentPosition2 != null) {
                this.this$0.getParent().removeView(this.$header);
            }
        } else if (currentPosition2 == null) {
            ViewGroup transientContainer = this.$header.getTransientContainer();
            if (transientContainer != null) {
                transientContainer.removeTransientView(this.$header);
            }
            this.$header.setTransientContainer(null);
            this.this$0.getParent().addView(this.$header, targetPosition2.intValue());
        } else {
            this.this$0.getParent().changeViewPosition(this.$header, targetPosition2.intValue());
        }
    }
}
