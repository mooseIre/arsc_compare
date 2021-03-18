package com.android.systemui.statusbar.notification.stack;

import android.view.View;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.stack.NotificationSectionsManager;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* access modifiers changed from: package-private */
/* compiled from: NotificationSectionsManager.kt */
public final class NotificationSectionsManager$updateSectionBoundaries$1 extends Lambda implements Function1<View, NotificationSectionsManager.SectionUpdateState<? extends ExpandableView>> {
    final /* synthetic */ NotificationSectionsManager.SectionUpdateState $alertingState;
    final /* synthetic */ NotificationSectionsManager.SectionUpdateState $gentleState;
    final /* synthetic */ NotificationSectionsManager.SectionUpdateState $incomingState;
    final /* synthetic */ NotificationSectionsManager.SectionUpdateState $mediaState;
    final /* synthetic */ NotificationSectionsManager.SectionUpdateState $peopleState;
    final /* synthetic */ NotificationSectionsManager this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    NotificationSectionsManager$updateSectionBoundaries$1(NotificationSectionsManager notificationSectionsManager, NotificationSectionsManager.SectionUpdateState sectionUpdateState, NotificationSectionsManager.SectionUpdateState sectionUpdateState2, NotificationSectionsManager.SectionUpdateState sectionUpdateState3, NotificationSectionsManager.SectionUpdateState sectionUpdateState4, NotificationSectionsManager.SectionUpdateState sectionUpdateState5) {
        super(1);
        this.this$0 = notificationSectionsManager;
        this.$mediaState = sectionUpdateState;
        this.$incomingState = sectionUpdateState2;
        this.$peopleState = sectionUpdateState3;
        this.$alertingState = sectionUpdateState4;
        this.$gentleState = sectionUpdateState5;
    }

    @Nullable
    public final NotificationSectionsManager.SectionUpdateState<ExpandableView> invoke(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "view");
        if (view == this.this$0.getMediaControlsView()) {
            return this.$mediaState;
        }
        if (view == this.this$0.getIncomingHeaderView()) {
            return this.$incomingState;
        }
        if (view == this.this$0.getPeopleHeaderView()) {
            return this.$peopleState;
        }
        if (view == this.this$0.getAlertingHeaderView()) {
            return this.$alertingState;
        }
        if (view == this.this$0.getSilentHeaderView()) {
            return this.$gentleState;
        }
        return null;
    }
}
