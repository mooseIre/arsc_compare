package com.android.systemui.statusbar.notification.collection.init;

import android.util.Log;
import com.android.systemui.Dumpable;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.notification.collection.NotifCollection;
import com.android.systemui.statusbar.notification.collection.NotifInflaterImpl;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotifViewManager;
import com.android.systemui.statusbar.notification.collection.ShadeListBuilder;
import com.android.systemui.statusbar.notification.collection.coalescer.GroupCoalescer;
import com.android.systemui.statusbar.notification.collection.coordinator.NotifCoordinators;
import com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinderImpl;
import com.android.systemui.statusbar.notification.stack.NotificationListContainer;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class NotifPipelineInitializer implements Dumpable {
    private final DumpManager mDumpManager;
    private final FeatureFlags mFeatureFlags;
    private final GroupCoalescer mGroupCoalescer;
    private final ShadeListBuilder mListBuilder;
    private final NotifCollection mNotifCollection;
    private final NotifInflaterImpl mNotifInflater;
    private final NotifCoordinators mNotifPluggableCoordinators;
    private final NotifViewManager mNotifViewManager;
    private final NotifPipeline mPipelineWrapper;

    public NotifPipelineInitializer(NotifPipeline notifPipeline, GroupCoalescer groupCoalescer, NotifCollection notifCollection, ShadeListBuilder shadeListBuilder, NotifCoordinators notifCoordinators, NotifInflaterImpl notifInflaterImpl, DumpManager dumpManager, FeatureFlags featureFlags, NotifViewManager notifViewManager) {
        this.mPipelineWrapper = notifPipeline;
        this.mGroupCoalescer = groupCoalescer;
        this.mNotifCollection = notifCollection;
        this.mListBuilder = shadeListBuilder;
        this.mNotifPluggableCoordinators = notifCoordinators;
        this.mDumpManager = dumpManager;
        this.mNotifInflater = notifInflaterImpl;
        this.mFeatureFlags = featureFlags;
        this.mNotifViewManager = notifViewManager;
    }

    public void initialize(NotificationListener notificationListener, NotificationRowBinderImpl notificationRowBinderImpl, NotificationListContainer notificationListContainer) {
        this.mDumpManager.registerDumpable("NotifPipeline", this);
        if (this.mFeatureFlags.isNewNotifPipelineRenderingEnabled()) {
            this.mNotifInflater.setRowBinder(notificationRowBinderImpl);
        }
        this.mNotifPluggableCoordinators.attach(this.mPipelineWrapper);
        this.mNotifViewManager.setViewConsumer(notificationListContainer);
        this.mNotifViewManager.attach(this.mListBuilder);
        this.mListBuilder.attach(this.mNotifCollection);
        this.mNotifCollection.attach(this.mGroupCoalescer);
        this.mGroupCoalescer.attach(notificationListener);
        Log.d("NotifPipeline", "Notif pipeline initialized");
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        this.mNotifViewManager.dump(fileDescriptor, printWriter, strArr);
        this.mNotifPluggableCoordinators.dump(fileDescriptor, printWriter, strArr);
        this.mGroupCoalescer.dump(fileDescriptor, printWriter, strArr);
    }
}
