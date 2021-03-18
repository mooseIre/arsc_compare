package com.android.systemui.usb;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.storage.DiskInfo;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.os.storage.VolumeRecord;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseArray;
import com.android.systemui.SystemUI;
import com.android.systemui.util.NotificationChannels;
import com.miui.systemui.util.UsbUtils;

public class StorageNotification extends SystemUI {
    private final BroadcastReceiver mFinishReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.usb.StorageNotification.AnonymousClass3 */

        public void onReceive(Context context, Intent intent) {
            StorageNotification.this.mNotificationManager.cancelAsUser(null, 1397575510, UserHandle.ALL);
        }
    };
    private final StorageEventListener mListener = new StorageEventListener() {
        /* class com.android.systemui.usb.StorageNotification.AnonymousClass1 */

        public void onVolumeStateChanged(VolumeInfo volumeInfo, int i, int i2) {
            StorageNotification.this.onVolumeStateChangedInternal(volumeInfo);
        }

        public void onVolumeRecordChanged(VolumeRecord volumeRecord) {
            VolumeInfo findVolumeByUuid = StorageNotification.this.mStorageManager.findVolumeByUuid(volumeRecord.getFsUuid());
            if (findVolumeByUuid != null && findVolumeByUuid.isMountedReadable()) {
                StorageNotification.this.onVolumeStateChangedInternal(findVolumeByUuid);
            }
        }

        public void onVolumeForgotten(String str) {
            StorageNotification.this.mNotificationManager.cancelAsUser(str, 1397772886, UserHandle.ALL);
        }

        public void onDiskScanned(DiskInfo diskInfo, int i) {
            StorageNotification.this.onDiskScannedInternal(diskInfo, i);
        }

        public void onDiskDestroyed(DiskInfo diskInfo) {
            StorageNotification.this.onDiskDestroyedInternal(diskInfo);
        }
    };
    private final PackageManager.MoveCallback mMoveCallback = new PackageManager.MoveCallback() {
        /* class com.android.systemui.usb.StorageNotification.AnonymousClass4 */

        public void onCreated(int i, Bundle bundle) {
            MoveInfo moveInfo = new MoveInfo();
            moveInfo.moveId = i;
            if (bundle != null) {
                moveInfo.packageName = bundle.getString("android.intent.extra.PACKAGE_NAME");
                moveInfo.label = bundle.getString("android.intent.extra.TITLE");
                moveInfo.volumeUuid = bundle.getString("android.os.storage.extra.FS_UUID");
            }
            StorageNotification.this.mMoves.put(i, moveInfo);
        }

        public void onStatusChanged(int i, int i2, long j) {
            MoveInfo moveInfo = (MoveInfo) StorageNotification.this.mMoves.get(i);
            if (moveInfo == null) {
                Log.w("StorageNotification", "Ignoring unknown move " + i);
            } else if (PackageManager.isMoveStatusFinished(i2)) {
                StorageNotification.this.onMoveFinished(moveInfo, i2);
            } else {
                StorageNotification.this.onMoveProgress(moveInfo, i2, j);
            }
        }
    };
    private final SparseArray<MoveInfo> mMoves = new SparseArray<>();
    private NotificationManager mNotificationManager;
    private final BroadcastReceiver mSnoozeReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.usb.StorageNotification.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            StorageNotification.this.mStorageManager.setVolumeSnoozed(intent.getStringExtra("android.os.storage.extra.FS_UUID"), true);
        }
    };
    private StorageManager mStorageManager;

    private Notification onVolumeFormatting(VolumeInfo volumeInfo) {
        return null;
    }

    private Notification onVolumeUnmounted(VolumeInfo volumeInfo) {
        return null;
    }

    public StorageNotification(Context context) {
        super(context);
    }

    /* access modifiers changed from: private */
    public static class MoveInfo {
        public String label;
        public int moveId;
        public String packageName;
        public String volumeUuid;

        private MoveInfo() {
        }
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
        StorageManager storageManager = (StorageManager) this.mContext.getSystemService(StorageManager.class);
        this.mStorageManager = storageManager;
        storageManager.registerListener(this.mListener);
        this.mContext.registerReceiver(this.mSnoozeReceiver, new IntentFilter("com.android.systemui.action.SNOOZE_VOLUME"), "android.permission.MOUNT_UNMOUNT_FILESYSTEMS", null);
        this.mContext.registerReceiver(this.mFinishReceiver, new IntentFilter("com.android.systemui.action.FINISH_WIZARD"), "android.permission.MOUNT_UNMOUNT_FILESYSTEMS", null);
        for (DiskInfo diskInfo : this.mStorageManager.getDisks()) {
            onDiskScannedInternal(diskInfo, diskInfo.volumeCount);
        }
        for (VolumeInfo volumeInfo : this.mStorageManager.getVolumes()) {
            onVolumeStateChangedInternal(volumeInfo);
        }
        this.mContext.getPackageManager().registerMoveCallback(this.mMoveCallback, new Handler());
        updateMissingPrivateVolumes();
    }

    private void updateMissingPrivateVolumes() {
        if (!(isTv() || isAutomotive())) {
            for (VolumeRecord volumeRecord : this.mStorageManager.getVolumeRecords()) {
                if (volumeRecord.getType() == 1) {
                    String fsUuid = volumeRecord.getFsUuid();
                    VolumeInfo findVolumeByUuid = this.mStorageManager.findVolumeByUuid(fsUuid);
                    if ((findVolumeByUuid == null || !findVolumeByUuid.isMountedWritable()) && !volumeRecord.isSnoozed()) {
                        String string = this.mContext.getString(17040166, volumeRecord.getNickname());
                        String string2 = this.mContext.getString(17040165);
                        Notification.Builder extend = new Notification.Builder(this.mContext, NotificationChannels.STORAGE).setSmallIcon(17302831).setColor(this.mContext.getColor(17170460)).setContentTitle(string).setContentText(string2).setContentIntent(buildForgetPendingIntent(volumeRecord)).setStyle(new Notification.BigTextStyle().bigText(string2)).setVisibility(1).setLocalOnly(true).setCategory("sys").setDeleteIntent(buildSnoozeIntent(fsUuid)).extend(new Notification.TvExtender());
                        SystemUI.overrideNotificationAppName(this.mContext, extend, false);
                        this.mNotificationManager.notifyAsUser(fsUuid, 1397772886, extend.build(), UserHandle.ALL);
                    } else {
                        this.mNotificationManager.cancelAsUser(fsUuid, 1397772886, UserHandle.ALL);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDiskScannedInternal(DiskInfo diskInfo, int i) {
        if (i != 0 || diskInfo.size <= 0) {
            this.mNotificationManager.cancelAsUser(diskInfo.getId(), 1396986699, UserHandle.ALL);
            return;
        }
        String string = this.mContext.getString(17040196, diskInfo.getDescription());
        String string2 = this.mContext.getString(17040195, diskInfo.getDescription());
        Notification.Builder extend = new Notification.Builder(this.mContext, NotificationChannels.STORAGE).setSmallIcon(getSmallIcon(diskInfo, 6)).setColor(this.mContext.getColor(17170460)).setContentTitle(string).setContentText(string2).setContentIntent(buildInitPendingIntent(diskInfo)).setStyle(new Notification.BigTextStyle().bigText(string2)).setVisibility(1).setLocalOnly(true).setCategory("err").extend(new Notification.TvExtender());
        SystemUI.overrideNotificationAppName(this.mContext, extend, false);
        this.mNotificationManager.notifyAsUser(diskInfo.getId(), 1396986699, extend.build(), UserHandle.ALL);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDiskDestroyedInternal(DiskInfo diskInfo) {
        this.mNotificationManager.cancelAsUser(diskInfo.getId(), 1396986699, UserHandle.ALL);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onVolumeStateChangedInternal(VolumeInfo volumeInfo) {
        int type = volumeInfo.getType();
        if (type == 0) {
            onPublicVolumeStateChangedInternal(volumeInfo);
        } else if (type == 1) {
            onPrivateVolumeStateChangedInternal(volumeInfo);
        }
    }

    private void onPrivateVolumeStateChangedInternal(VolumeInfo volumeInfo) {
        Log.d("StorageNotification", "Notifying about private volume: " + volumeInfo.toString());
        updateMissingPrivateVolumes();
    }

    private void onPublicVolumeStateChangedInternal(VolumeInfo volumeInfo) {
        Notification notification;
        Log.d("StorageNotification", "Notifying about public volume: " + volumeInfo.toString());
        if (!isAutomotive() || volumeInfo.getMountUserId() != -10000) {
            switch (volumeInfo.getState()) {
                case 0:
                    notification = onVolumeUnmounted(volumeInfo);
                    break;
                case 1:
                    notification = onVolumeChecking(volumeInfo);
                    break;
                case 2:
                case 3:
                    notification = onVolumeMounted(volumeInfo);
                    break;
                case 4:
                    notification = onVolumeFormatting(volumeInfo);
                    break;
                case 5:
                    notification = onVolumeEjecting(volumeInfo);
                    break;
                case 6:
                    notification = onVolumeUnmountable(volumeInfo);
                    break;
                case 7:
                    notification = onVolumeRemoved(volumeInfo);
                    break;
                case 8:
                    notification = onVolumeBadRemoval(volumeInfo);
                    break;
                default:
                    notification = null;
                    break;
            }
            if (notification != null) {
                this.mNotificationManager.notifyAsUser(volumeInfo.getId(), 1397773634, notification, UserHandle.of(volumeInfo.getMountUserId()));
            } else {
                this.mNotificationManager.cancelAsUser(volumeInfo.getId(), 1397773634, UserHandle.of(volumeInfo.getMountUserId()));
            }
        } else {
            Log.d("StorageNotification", "Ignore public volume state change event of removed user");
        }
    }

    private Notification onVolumeChecking(VolumeInfo volumeInfo) {
        DiskInfo disk = volumeInfo.getDisk();
        return buildNotificationBuilder(volumeInfo, this.mContext.getString(17040163, disk.getDescription()), this.mContext.getString(17040162, disk.getDescription())).setCategory("progress").setOngoing(true).build();
    }

    private Notification onVolumeMounted(VolumeInfo volumeInfo) {
        VolumeRecord findRecordByUuid = this.mStorageManager.findRecordByUuid(volumeInfo.getFsUuid());
        DiskInfo disk = volumeInfo.getDisk();
        if (findRecordByUuid.isSnoozed() && disk.isAdoptable()) {
            return null;
        }
        String description = disk.getDescription();
        String string = this.mContext.getString(17040177, disk.getDescription());
        PendingIntent buildBrowsePendingIntent = UsbUtils.buildBrowsePendingIntent(this.mContext, volumeInfo);
        Notification.Builder category = buildNotificationBuilder(volumeInfo, description, string).addAction(new Notification.Action(17302453, this.mContext.getString(17040161), buildBrowsePendingIntent)).addAction(new Notification.Action(17302435, this.mContext.getString(17040190), buildUnmountPendingIntent(volumeInfo))).setContentIntent(buildBrowsePendingIntent).setCategory("sys");
        if (disk.isAdoptable()) {
            category.setDeleteIntent(buildSnoozeIntent(volumeInfo.getFsUuid()));
        }
        return category.build();
    }

    private Notification onVolumeEjecting(VolumeInfo volumeInfo) {
        DiskInfo disk = volumeInfo.getDisk();
        return buildNotificationBuilder(volumeInfo, this.mContext.getString(17040194, disk.getDescription()), this.mContext.getString(17040193, disk.getDescription())).setCategory("progress").setOngoing(true).build();
    }

    private Notification onVolumeUnmountable(VolumeInfo volumeInfo) {
        PendingIntent pendingIntent;
        DiskInfo disk = volumeInfo.getDisk();
        String string = this.mContext.getString(17040192, disk.getDescription());
        String string2 = this.mContext.getString(17040191, disk.getDescription());
        if (isAutomotive()) {
            pendingIntent = buildUnmountPendingIntent(volumeInfo);
        } else {
            pendingIntent = buildInitPendingIntent(volumeInfo);
        }
        return buildNotificationBuilder(volumeInfo, string, string2).setContentIntent(pendingIntent).setCategory("err").build();
    }

    private Notification onVolumeRemoved(VolumeInfo volumeInfo) {
        if (!volumeInfo.isPrimary()) {
            return null;
        }
        DiskInfo disk = volumeInfo.getDisk();
        return buildNotificationBuilder(volumeInfo, this.mContext.getString(17040176, disk.getDescription()), this.mContext.getString(17040175, disk.getDescription())).setCategory("err").build();
    }

    private Notification onVolumeBadRemoval(VolumeInfo volumeInfo) {
        if (!volumeInfo.isPrimary()) {
            return null;
        }
        DiskInfo disk = volumeInfo.getDisk();
        return buildNotificationBuilder(volumeInfo, this.mContext.getString(17040160, disk.getDescription()), this.mContext.getString(17040159, disk.getDescription())).setCategory("err").build();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onMoveProgress(MoveInfo moveInfo, int i, long j) {
        String str;
        CharSequence charSequence;
        PendingIntent pendingIntent;
        if (!TextUtils.isEmpty(moveInfo.label)) {
            str = this.mContext.getString(17040169, moveInfo.label);
        } else {
            str = this.mContext.getString(17040172);
        }
        if (j < 0) {
            charSequence = null;
        } else {
            charSequence = DateUtils.formatDuration(j);
        }
        if (moveInfo.packageName != null) {
            pendingIntent = buildWizardMovePendingIntent(moveInfo);
        } else {
            pendingIntent = buildWizardMigratePendingIntent(moveInfo);
        }
        Notification.Builder ongoing = new Notification.Builder(this.mContext, NotificationChannels.STORAGE).setSmallIcon(17302831).setColor(this.mContext.getColor(17170460)).setContentTitle(str).setContentText(charSequence).setContentIntent(pendingIntent).setStyle(new Notification.BigTextStyle().bigText(charSequence)).setVisibility(1).setLocalOnly(true).setCategory("progress").setProgress(100, i, false).setOngoing(true);
        SystemUI.overrideNotificationAppName(this.mContext, ongoing, false);
        this.mNotificationManager.notifyAsUser(moveInfo.packageName, 1397575510, ongoing.build(), UserHandle.ALL);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onMoveFinished(MoveInfo moveInfo, int i) {
        String str;
        String str2;
        PendingIntent pendingIntent;
        String str3 = moveInfo.packageName;
        if (str3 != null) {
            this.mNotificationManager.cancelAsUser(str3, 1397575510, UserHandle.ALL);
            return;
        }
        VolumeInfo primaryStorageCurrentVolume = this.mContext.getPackageManager().getPrimaryStorageCurrentVolume();
        String bestVolumeDescription = this.mStorageManager.getBestVolumeDescription(primaryStorageCurrentVolume);
        if (i == -100) {
            str = this.mContext.getString(17040171);
            str2 = this.mContext.getString(17040170, bestVolumeDescription);
        } else {
            str = this.mContext.getString(17040168);
            str2 = this.mContext.getString(17040167);
        }
        if (primaryStorageCurrentVolume == null || primaryStorageCurrentVolume.getDisk() == null) {
            pendingIntent = primaryStorageCurrentVolume != null ? buildVolumeSettingsPendingIntent(primaryStorageCurrentVolume) : null;
        } else {
            pendingIntent = buildWizardReadyPendingIntent(primaryStorageCurrentVolume.getDisk());
        }
        Notification.Builder autoCancel = new Notification.Builder(this.mContext, NotificationChannels.STORAGE).setSmallIcon(17302831).setColor(this.mContext.getColor(17170460)).setContentTitle(str).setContentText(str2).setContentIntent(pendingIntent).setStyle(new Notification.BigTextStyle().bigText(str2)).setVisibility(1).setLocalOnly(true).setCategory("sys").setAutoCancel(true);
        SystemUI.overrideNotificationAppName(this.mContext, autoCancel, false);
        this.mNotificationManager.notifyAsUser(moveInfo.packageName, 1397575510, autoCancel.build(), UserHandle.ALL);
    }

    private int getSmallIcon(DiskInfo diskInfo, int i) {
        if (!diskInfo.isSd() && diskInfo.isUsb()) {
            return 17302873;
        }
        return 17302831;
    }

    private Notification.Builder buildNotificationBuilder(VolumeInfo volumeInfo, CharSequence charSequence, CharSequence charSequence2) {
        Notification.Builder extend = new Notification.Builder(this.mContext, NotificationChannels.STORAGE).setSmallIcon(getSmallIcon(volumeInfo.getDisk(), volumeInfo.getState())).setColor(this.mContext.getColor(17170460)).setContentTitle(charSequence).setContentText(charSequence2).setStyle(new Notification.BigTextStyle().bigText(charSequence2)).setVisibility(1).setLocalOnly(true).extend(new Notification.TvExtender());
        SystemUI.overrideNotificationAppName(this.mContext, extend, false);
        return extend;
    }

    private PendingIntent buildInitPendingIntent(DiskInfo diskInfo) {
        Intent intent = new Intent();
        if (isTv()) {
            intent.setPackage("com.android.tv.settings");
            intent.setAction("com.android.tv.settings.action.NEW_STORAGE");
        } else if (isAutomotive()) {
            return null;
        } else {
            intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageWizardInit");
        }
        intent.putExtra("android.os.storage.extra.DISK_ID", diskInfo.getId());
        return PendingIntent.getActivityAsUser(this.mContext, diskInfo.getId().hashCode(), intent, 268435456, null, UserHandle.CURRENT);
    }

    private PendingIntent buildInitPendingIntent(VolumeInfo volumeInfo) {
        Intent intent = new Intent();
        if (isTv()) {
            intent.setPackage("com.android.tv.settings");
            intent.setAction("com.android.tv.settings.action.NEW_STORAGE");
        } else if (isAutomotive()) {
            return null;
        } else {
            intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageWizardInit");
        }
        intent.putExtra("android.os.storage.extra.VOLUME_ID", volumeInfo.getId());
        return PendingIntent.getActivityAsUser(this.mContext, volumeInfo.getId().hashCode(), intent, 268435456, null, UserHandle.CURRENT);
    }

    private PendingIntent buildUnmountPendingIntent(VolumeInfo volumeInfo) {
        Intent intent = new Intent();
        if (isTv()) {
            intent.setPackage("com.android.tv.settings");
            intent.setAction("com.android.tv.settings.action.UNMOUNT_STORAGE");
            intent.putExtra("android.os.storage.extra.VOLUME_ID", volumeInfo.getId());
            return PendingIntent.getActivityAsUser(this.mContext, volumeInfo.getId().hashCode(), intent, 268435456, null, UserHandle.CURRENT);
        } else if (isAutomotive()) {
            intent.setClassName("com.android.car.settings", "com.android.car.settings.storage.StorageUnmountReceiver");
            intent.putExtra("android.os.storage.extra.VOLUME_ID", volumeInfo.getId());
            return PendingIntent.getBroadcastAsUser(this.mContext, volumeInfo.getId().hashCode(), intent, 268435456, UserHandle.CURRENT);
        } else {
            intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageUnmountReceiver");
            intent.putExtra("android.os.storage.extra.VOLUME_ID", volumeInfo.getId());
            return PendingIntent.getBroadcastAsUser(this.mContext, volumeInfo.getId().hashCode(), intent, 268435456, UserHandle.CURRENT);
        }
    }

    private PendingIntent buildVolumeSettingsPendingIntent(VolumeInfo volumeInfo) {
        Intent intent = new Intent();
        if (isTv()) {
            intent.setPackage("com.android.tv.settings");
            intent.setAction("android.settings.INTERNAL_STORAGE_SETTINGS");
        } else if (isAutomotive()) {
            return null;
        } else {
            int type = volumeInfo.getType();
            if (type == 0) {
                intent.setClassName("com.android.settings", "com.android.settings.Settings$PublicVolumeSettingsActivity");
            } else if (type != 1) {
                return null;
            } else {
                intent.setClassName("com.android.settings", "com.android.settings.Settings$PrivateVolumeSettingsActivity");
            }
        }
        intent.putExtra("android.os.storage.extra.VOLUME_ID", volumeInfo.getId());
        return PendingIntent.getActivityAsUser(this.mContext, volumeInfo.getId().hashCode(), intent, 268435456, null, UserHandle.CURRENT);
    }

    private PendingIntent buildSnoozeIntent(String str) {
        Intent intent = new Intent("com.android.systemui.action.SNOOZE_VOLUME");
        intent.putExtra("android.os.storage.extra.FS_UUID", str);
        return PendingIntent.getBroadcastAsUser(this.mContext, str.hashCode(), intent, 268435456, UserHandle.CURRENT);
    }

    private PendingIntent buildForgetPendingIntent(VolumeRecord volumeRecord) {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.Settings$PrivateVolumeForgetActivity");
        intent.putExtra("android.os.storage.extra.FS_UUID", volumeRecord.getFsUuid());
        return PendingIntent.getActivityAsUser(this.mContext, volumeRecord.getFsUuid().hashCode(), intent, 268435456, null, UserHandle.CURRENT);
    }

    private PendingIntent buildWizardMigratePendingIntent(MoveInfo moveInfo) {
        Intent intent = new Intent();
        if (isTv()) {
            intent.setPackage("com.android.tv.settings");
            intent.setAction("com.android.tv.settings.action.MIGRATE_STORAGE");
        } else if (isAutomotive()) {
            return null;
        } else {
            intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageWizardMigrateProgress");
        }
        intent.putExtra("android.content.pm.extra.MOVE_ID", moveInfo.moveId);
        VolumeInfo findVolumeByQualifiedUuid = this.mStorageManager.findVolumeByQualifiedUuid(moveInfo.volumeUuid);
        if (findVolumeByQualifiedUuid != null) {
            intent.putExtra("android.os.storage.extra.VOLUME_ID", findVolumeByQualifiedUuid.getId());
        }
        return PendingIntent.getActivityAsUser(this.mContext, moveInfo.moveId, intent, 268435456, null, UserHandle.CURRENT);
    }

    private PendingIntent buildWizardMovePendingIntent(MoveInfo moveInfo) {
        Intent intent = new Intent();
        if (isTv()) {
            intent.setPackage("com.android.tv.settings");
            intent.setAction("com.android.tv.settings.action.MOVE_APP");
        } else if (isAutomotive()) {
            return null;
        } else {
            intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageWizardMoveProgress");
        }
        intent.putExtra("android.content.pm.extra.MOVE_ID", moveInfo.moveId);
        return PendingIntent.getActivityAsUser(this.mContext, moveInfo.moveId, intent, 268435456, null, UserHandle.CURRENT);
    }

    private PendingIntent buildWizardReadyPendingIntent(DiskInfo diskInfo) {
        Intent intent = new Intent();
        if (isTv()) {
            intent.setPackage("com.android.tv.settings");
            intent.setAction("android.settings.INTERNAL_STORAGE_SETTINGS");
        } else if (isAutomotive()) {
            return null;
        } else {
            intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageWizardReady");
        }
        intent.putExtra("android.os.storage.extra.DISK_ID", diskInfo.getId());
        return PendingIntent.getActivityAsUser(this.mContext, diskInfo.getId().hashCode(), intent, 268435456, null, UserHandle.CURRENT);
    }

    private boolean isAutomotive() {
        return this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.automotive");
    }

    private boolean isTv() {
        return this.mContext.getPackageManager().hasSystemFeature("android.software.leanback");
    }
}
