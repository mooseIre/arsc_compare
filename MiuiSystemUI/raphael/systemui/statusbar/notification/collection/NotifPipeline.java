package com.android.systemui.statusbar.notification.collection;

import com.android.systemui.statusbar.notification.collection.listbuilder.OnBeforeFinalizeFilterListener;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifFilter;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifPromoter;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifSection;
import com.android.systemui.statusbar.notification.collection.notifcollection.CommonNotifCollection;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifDismissInterceptor;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender;
import java.util.Collection;
import java.util.List;

public class NotifPipeline implements CommonNotifCollection {
    private final NotifCollection mNotifCollection;
    private final ShadeListBuilder mShadeListBuilder;

    public NotifPipeline(NotifCollection notifCollection, ShadeListBuilder shadeListBuilder) {
        this.mNotifCollection = notifCollection;
        this.mShadeListBuilder = shadeListBuilder;
    }

    @Override // com.android.systemui.statusbar.notification.collection.notifcollection.CommonNotifCollection
    public Collection<NotificationEntry> getAllNotifs() {
        return this.mNotifCollection.getAllNotifs();
    }

    @Override // com.android.systemui.statusbar.notification.collection.notifcollection.CommonNotifCollection
    public void addCollectionListener(NotifCollectionListener notifCollectionListener) {
        this.mNotifCollection.addCollectionListener(notifCollectionListener);
    }

    public void addNotificationLifetimeExtender(NotifLifetimeExtender notifLifetimeExtender) {
        this.mNotifCollection.addNotificationLifetimeExtender(notifLifetimeExtender);
    }

    public void addNotificationDismissInterceptor(NotifDismissInterceptor notifDismissInterceptor) {
        this.mNotifCollection.addNotificationDismissInterceptor(notifDismissInterceptor);
    }

    public void addPreGroupFilter(NotifFilter notifFilter) {
        this.mShadeListBuilder.addPreGroupFilter(notifFilter);
    }

    public void addPromoter(NotifPromoter notifPromoter) {
        this.mShadeListBuilder.addPromoter(notifPromoter);
    }

    public void setSections(List<NotifSection> list) {
        this.mShadeListBuilder.setSections(list);
    }

    public void addOnBeforeFinalizeFilterListener(OnBeforeFinalizeFilterListener onBeforeFinalizeFilterListener) {
        this.mShadeListBuilder.addOnBeforeFinalizeFilterListener(onBeforeFinalizeFilterListener);
    }

    public void addFinalizeFilter(NotifFilter notifFilter) {
        this.mShadeListBuilder.addFinalizeFilter(notifFilter);
    }

    public List<ListEntry> getShadeList() {
        return this.mShadeListBuilder.getShadeList();
    }

    public int getShadeListCount() {
        List<ListEntry> shadeList = getShadeList();
        int i = 0;
        for (int i2 = 0; i2 < shadeList.size(); i2++) {
            ListEntry listEntry = shadeList.get(i2);
            i = listEntry instanceof GroupEntry ? i + 1 + ((GroupEntry) listEntry).getChildren().size() : i + 1;
        }
        return i;
    }
}
