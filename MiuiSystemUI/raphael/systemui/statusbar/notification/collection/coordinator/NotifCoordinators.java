package com.android.systemui.statusbar.notification.collection.coordinator;

import com.android.systemui.Dumpable;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifSection;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class NotifCoordinators implements Dumpable {
    private final List<Coordinator> mCoordinators = new ArrayList();
    private final List<NotifSection> mOrderedSections = new ArrayList();

    public NotifCoordinators(DumpManager dumpManager, FeatureFlags featureFlags, HideNotifsForOtherUsersCoordinator hideNotifsForOtherUsersCoordinator, KeyguardCoordinator keyguardCoordinator, RankingCoordinator rankingCoordinator, AppOpsCoordinator appOpsCoordinator, DeviceProvisionedCoordinator deviceProvisionedCoordinator, BubbleCoordinator bubbleCoordinator, HeadsUpCoordinator headsUpCoordinator, ConversationCoordinator conversationCoordinator, PreparationCoordinator preparationCoordinator, MediaCoordinator mediaCoordinator) {
        dumpManager.registerDumpable("NotifCoordinators", this);
        this.mCoordinators.add(new HideLocallyDismissedNotifsCoordinator());
        this.mCoordinators.add(hideNotifsForOtherUsersCoordinator);
        this.mCoordinators.add(keyguardCoordinator);
        this.mCoordinators.add(rankingCoordinator);
        this.mCoordinators.add(appOpsCoordinator);
        this.mCoordinators.add(deviceProvisionedCoordinator);
        this.mCoordinators.add(bubbleCoordinator);
        if (featureFlags.isNewNotifPipelineRenderingEnabled()) {
            this.mCoordinators.add(conversationCoordinator);
            this.mCoordinators.add(headsUpCoordinator);
            this.mCoordinators.add(preparationCoordinator);
        }
        this.mCoordinators.add(mediaCoordinator);
        for (Coordinator next : this.mCoordinators) {
            if (next.getSection() != null) {
                this.mOrderedSections.add(next.getSection());
            }
        }
    }

    public void attach(NotifPipeline notifPipeline) {
        for (Coordinator attach : this.mCoordinators) {
            attach.attach(notifPipeline);
        }
        notifPipeline.setSections(this.mOrderedSections);
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println();
        printWriter.println("NotifCoordinators:");
        for (Coordinator coordinator : this.mCoordinators) {
            printWriter.println("\t" + coordinator.getClass());
        }
        for (NotifSection name : this.mOrderedSections) {
            printWriter.println("\t" + name.getName());
        }
    }
}
