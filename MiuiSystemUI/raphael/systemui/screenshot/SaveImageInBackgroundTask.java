package com.android.systemui.screenshot;

import android.app.ActivityTaskManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.DeviceConfig;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.screenshot.GlobalScreenshot;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/* access modifiers changed from: package-private */
public class SaveImageInBackgroundTask extends AsyncTask<Void, Void, Void> {
    private final Context mContext;
    private final GlobalScreenshot.SavedImageData mImageData;
    private final String mImageFileName;
    private final long mImageTime;
    private final GlobalScreenshot.SaveImageInBackgroundData mParams;
    private final Random mRandom = new Random();
    private final String mScreenshotId;
    private final boolean mSmartActionsEnabled;
    private final ScreenshotNotificationSmartActionsProvider mSmartActionsProvider;

    SaveImageInBackgroundTask(Context context, GlobalScreenshot.SaveImageInBackgroundData saveImageInBackgroundData) {
        this.mContext = context;
        this.mImageData = new GlobalScreenshot.SavedImageData();
        this.mParams = saveImageInBackgroundData;
        this.mImageTime = System.currentTimeMillis();
        this.mImageFileName = String.format("Screenshot_%s.png", new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date(this.mImageTime)));
        this.mScreenshotId = String.format("Screenshot_%s", UUID.randomUUID());
        boolean z = DeviceConfig.getBoolean("systemui", "enable_screenshot_notification_smart_actions", true);
        this.mSmartActionsEnabled = z;
        if (z) {
            this.mSmartActionsProvider = SystemUIFactory.getInstance().createScreenshotNotificationSmartActionsProvider(context, AsyncTask.THREAD_POOL_EXECUTOR, new Handler());
        } else {
            this.mSmartActionsProvider = new ScreenshotNotificationSmartActionsProvider();
        }
    }

    /* access modifiers changed from: protected */
    public Void doInBackground(Void... voidArr) {
        if (isCancelled()) {
            return null;
        }
        Thread.currentThread().setPriority(10);
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Bitmap bitmap = this.mParams.image;
        this.mContext.getResources();
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("relative_path", Environment.DIRECTORY_PICTURES + File.separator + Environment.DIRECTORY_SCREENSHOTS);
            contentValues.put("_display_name", this.mImageFileName);
            contentValues.put("mime_type", "image/png");
            contentValues.put("date_added", Long.valueOf(this.mImageTime / 1000));
            contentValues.put("date_modified", Long.valueOf(this.mImageTime / 1000));
            contentValues.put("date_expires", Long.valueOf((this.mImageTime + 86400000) / 1000));
            contentValues.put("is_pending", (Integer) 1);
            Uri insert = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            CompletableFuture<List<Notification.Action>> smartActionsFuture = ScreenshotSmartActions.getSmartActionsFuture(this.mScreenshotId, insert, bitmap, this.mSmartActionsProvider, this.mSmartActionsEnabled, getUserHandle(this.mContext));
            try {
                OutputStream openOutputStream = contentResolver.openOutputStream(insert);
                try {
                    if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, openOutputStream)) {
                        if (openOutputStream != null) {
                            openOutputStream.close();
                        }
                        ParcelFileDescriptor openFile = contentResolver.openFile(insert, "rw", null);
                        try {
                            ExifInterface exifInterface = new ExifInterface(openFile.getFileDescriptor());
                            exifInterface.setAttribute("Software", "Android " + Build.DISPLAY);
                            exifInterface.setAttribute("ImageWidth", Integer.toString(bitmap.getWidth()));
                            exifInterface.setAttribute("ImageLength", Integer.toString(bitmap.getHeight()));
                            ZonedDateTime ofInstant = ZonedDateTime.ofInstant(Instant.ofEpochMilli(this.mImageTime), ZoneId.systemDefault());
                            exifInterface.setAttribute("DateTimeOriginal", DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss").format(ofInstant));
                            exifInterface.setAttribute("SubSecTimeOriginal", DateTimeFormatter.ofPattern("SSS").format(ofInstant));
                            if (Objects.equals(ofInstant.getOffset(), ZoneOffset.UTC)) {
                                exifInterface.setAttribute("OffsetTimeOriginal", "+00:00");
                            } else {
                                exifInterface.setAttribute("OffsetTimeOriginal", DateTimeFormatter.ofPattern("XXX").format(ofInstant));
                            }
                            exifInterface.saveAttributes();
                            if (openFile != null) {
                                openFile.close();
                            }
                            contentValues.clear();
                            contentValues.put("is_pending", (Integer) 0);
                            contentValues.putNull("date_expires");
                            contentResolver.update(insert, contentValues, null, null);
                            ArrayList arrayList = new ArrayList();
                            if (this.mSmartActionsEnabled) {
                                arrayList.addAll(buildSmartActions(ScreenshotSmartActions.getSmartActions(this.mScreenshotId, smartActionsFuture, DeviceConfig.getInt("systemui", "screenshot_notification_smart_actions_timeout_ms", 1000), this.mSmartActionsProvider), this.mContext));
                            }
                            this.mImageData.uri = insert;
                            this.mImageData.smartActions = arrayList;
                            this.mImageData.shareAction = createShareAction(this.mContext, this.mContext.getResources(), insert);
                            this.mImageData.editAction = createEditAction(this.mContext, this.mContext.getResources(), insert);
                            this.mImageData.deleteAction = createDeleteAction(this.mContext, this.mContext.getResources(), insert);
                            this.mParams.mActionsReadyListener.onActionsReady(this.mImageData);
                            this.mParams.finisher.accept(this.mImageData.uri);
                            this.mParams.image = null;
                            this.mParams.errorMsgResId = 0;
                            return null;
                        } catch (Throwable th) {
                            th.addSuppressed(th);
                        }
                    } else {
                        throw new IOException("Failed to compress");
                    }
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            } catch (Exception e) {
                contentResolver.delete(insert, null);
                throw e;
            }
        } catch (Exception e2) {
            Slog.e("SaveImageInBackgroundTask", "unable to save screenshot", e2);
            this.mParams.clearImage();
            this.mParams.errorMsgResId = C0021R$string.screenshot_failed_to_save_text;
            this.mImageData.reset();
            this.mParams.mActionsReadyListener.onActionsReady(this.mImageData);
            this.mParams.finisher.accept(null);
        }
        throw th;
        throw th;
    }

    /* access modifiers changed from: package-private */
    public void setActionsReadyListener(GlobalScreenshot.ActionsReadyListener actionsReadyListener) {
        this.mParams.mActionsReadyListener = actionsReadyListener;
    }

    /* access modifiers changed from: protected */
    public void onCancelled(Void r2) {
        this.mImageData.reset();
        this.mParams.mActionsReadyListener.onActionsReady(this.mImageData);
        this.mParams.finisher.accept(null);
        this.mParams.clearImage();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Notification.Action createShareAction(Context context, Resources resources, Uri uri) {
        String format = String.format("Screenshot (%s)", DateFormat.getDateTimeInstance().format(new Date(this.mImageTime)));
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("image/png");
        intent.putExtra("android.intent.extra.STREAM", uri);
        intent.setClipData(new ClipData(new ClipDescription("content", new String[]{"text/plain"}), new ClipData.Item(uri)));
        intent.putExtra("android.intent.extra.SUBJECT", format);
        intent.addFlags(1);
        int userId = context.getUserId();
        return new Notification.Action.Builder(Icon.createWithResource(resources, C0013R$drawable.ic_screenshot_share), resources.getString(17041327), PendingIntent.getBroadcastAsUser(context, userId, new Intent(context, GlobalScreenshot.ActionProxyReceiver.class).putExtra("android:screenshot_action_intent", PendingIntent.getActivityAsUser(context, 0, Intent.createChooser(intent, null, PendingIntent.getBroadcast(context, userId, new Intent(context, GlobalScreenshot.TargetChosenReceiver.class), 1342177280).getIntentSender()).addFlags(268468224).addFlags(1), 268435456, null, UserHandle.CURRENT)).putExtra("android:screenshot_disallow_enter_pip", true).putExtra("android:screenshot_id", this.mScreenshotId).putExtra("android:smart_actions_enabled", this.mSmartActionsEnabled).setAction("android.intent.action.SEND").addFlags(268435456), 268435456, UserHandle.SYSTEM)).build();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Notification.Action createEditAction(Context context, Resources resources, Uri uri) {
        String string = context.getString(C0021R$string.config_screenshotEditor);
        Intent intent = new Intent("android.intent.action.EDIT");
        if (!TextUtils.isEmpty(string)) {
            intent.setComponent(ComponentName.unflattenFromString(string));
        }
        intent.setType("image/png");
        intent.setData(uri);
        boolean z = true;
        intent.addFlags(1);
        intent.addFlags(2);
        intent.addFlags(268468224);
        PendingIntent activityAsUser = PendingIntent.getActivityAsUser(context, 0, intent, 0, null, UserHandle.CURRENT);
        int userId = this.mContext.getUserId();
        Intent putExtra = new Intent(context, GlobalScreenshot.ActionProxyReceiver.class).putExtra("android:screenshot_action_intent", activityAsUser);
        if (intent.getComponent() == null) {
            z = false;
        }
        return new Notification.Action.Builder(Icon.createWithResource(resources, C0013R$drawable.ic_screenshot_edit), resources.getString(17041288), PendingIntent.getBroadcastAsUser(context, userId, putExtra.putExtra("android:screenshot_cancel_notification", z).putExtra("android:screenshot_id", this.mScreenshotId).putExtra("android:smart_actions_enabled", this.mSmartActionsEnabled).setAction("android.intent.action.EDIT").addFlags(268435456), 268435456, UserHandle.SYSTEM)).build();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Notification.Action createDeleteAction(Context context, Resources resources, Uri uri) {
        return new Notification.Action.Builder(Icon.createWithResource(resources, C0013R$drawable.ic_screenshot_delete), resources.getString(17040076), PendingIntent.getBroadcast(context, this.mContext.getUserId(), new Intent(context, GlobalScreenshot.DeleteScreenshotReceiver.class).putExtra("android:screenshot_uri_id", uri.toString()).putExtra("android:screenshot_id", this.mScreenshotId).putExtra("android:smart_actions_enabled", this.mSmartActionsEnabled).addFlags(268435456), 1342177280)).build();
    }

    private int getUserHandleOfForegroundApplication(Context context) {
        try {
            return ActivityTaskManager.getService().getLastResumedActivityUserId();
        } catch (RemoteException e) {
            Slog.w("SaveImageInBackgroundTask", "getUserHandleOfForegroundApplication: ", e);
            return context.getUserId();
        }
    }

    private UserHandle getUserHandle(Context context) {
        return UserManager.get(context).getUserInfo(getUserHandleOfForegroundApplication(context)).getUserHandle();
    }

    private List<Notification.Action> buildSmartActions(List<Notification.Action> list, Context context) {
        ArrayList arrayList = new ArrayList();
        for (Notification.Action action : list) {
            Bundle extras = action.getExtras();
            String string = extras.getString("action_type", "Smart Action");
            Intent addFlags = new Intent(context, GlobalScreenshot.SmartActionsReceiver.class).putExtra("android:screenshot_action_intent", action.actionIntent).addFlags(268435456);
            addIntentExtras(this.mScreenshotId, addFlags, string, this.mSmartActionsEnabled);
            arrayList.add(new Notification.Action.Builder(action.getIcon(), action.title, PendingIntent.getBroadcast(context, this.mRandom.nextInt(), addFlags, 268435456)).setContextual(true).addExtras(extras).build());
        }
        return arrayList;
    }

    private static void addIntentExtras(String str, Intent intent, String str2, boolean z) {
        intent.putExtra("android:screenshot_action_type", str2).putExtra("android:screenshot_id", str).putExtra("android:smart_actions_enabled", z);
    }
}
