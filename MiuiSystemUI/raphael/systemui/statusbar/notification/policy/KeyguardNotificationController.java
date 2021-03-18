package com.android.systemui.statusbar.notification.policy;

import android.app.Notification;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.widget.DateTimeView;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.notification.MiuiNotificationCompat;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import miui.maml.FancyDrawable;
import miui.provider.KeyguardNotification;

public class KeyguardNotificationController {
    private Handler mBgHandler;
    private Context mContext;
    private NotificationEntryManager mEntryManager;
    private NotificationGroupManager mGroupManager;
    private KeyguardStateController mKeyguardStateController;
    private NotificationLockscreenUserManager mLockscreenUserManager;
    private final ArrayList<String> mSortedKeys = new ArrayList<>();

    public KeyguardNotificationController(Context context, NotificationEntryManager notificationEntryManager, NotificationGroupManager notificationGroupManager, KeyguardStateController keyguardStateController, NotificationLockscreenUserManager notificationLockscreenUserManager) {
        this.mContext = context;
        this.mEntryManager = notificationEntryManager;
        this.mGroupManager = notificationGroupManager;
        this.mKeyguardStateController = keyguardStateController;
        this.mLockscreenUserManager = notificationLockscreenUserManager;
        initBgHandler();
    }

    private void initBgHandler() {
        this.mBgHandler = new Handler((Looper) Dependency.get(Dependency.BG_LOOPER)) {
            /* class com.android.systemui.statusbar.notification.policy.KeyguardNotificationController.AnonymousClass1 */

            public void handleMessage(Message message) {
                super.handleMessage(message);
                switch (message.what) {
                    case 3000:
                        KeyguardNotificationController.this.handleInsertDB((ContentValues) message.obj);
                        return;
                    case 3001:
                        KeyguardNotificationController.this.handleUpdateDB((ContentValues) message.obj);
                        return;
                    case 3002:
                        KeyguardNotificationController.this.handleDeleteDB(message.arg1);
                        return;
                    case 3003:
                        KeyguardNotificationController.this.handleClearDB();
                        return;
                    default:
                        return;
                }
            }
        };
    }

    public void addOrUpdate(NotificationEntry notificationEntry, boolean z) {
        if (!needUpdateNotificationProvider(notificationEntry)) {
            return;
        }
        if (z) {
            add(notificationEntry);
        } else {
            update(notificationEntry);
        }
    }

    private boolean needUpdateNotificationProvider(NotificationEntry notificationEntry) {
        if (this.mKeyguardStateController.isShowing() && !NotificationUtil.isMediaNotification(notificationEntry.getSbn()) && !NotificationUtil.isCustomViewNotification(notificationEntry.getSbn())) {
            return this.mLockscreenUserManager.shouldShowOnKeyguard(notificationEntry);
        }
        return false;
    }

    public void add(NotificationEntry notificationEntry) {
        if (!notificationEntry.getSbn().getNotification().isGroupSummary()) {
            NotificationEntry groupSummary = this.mGroupManager.getGroupSummary(notificationEntry.getSbn());
            if (!(groupSummary == null || groupSummary.getRow() == null)) {
                remove(groupSummary.getRow().getEntry().getKey().hashCode());
            }
        } else if (this.mGroupManager.isSummaryOfGroup(notificationEntry.getSbn())) {
            return;
        }
        updateSortedKeys(3000, notificationEntry.getKey());
        ContentValues buildValues = buildValues(notificationEntry);
        if (buildValues != null) {
            this.mBgHandler.obtainMessage(3000, buildValues).sendToTarget();
        }
    }

    public void update(NotificationEntry notificationEntry) {
        updateSortedKeys(3001, notificationEntry.getKey());
        ContentValues buildValues = buildValues(notificationEntry);
        if (buildValues != null) {
            this.mBgHandler.obtainMessage(3001, buildValues).sendToTarget();
        }
    }

    public void remove(String str) {
        if (this.mSortedKeys.contains(str)) {
            updateSortedKeys(3002, str);
            remove(str.hashCode());
        }
    }

    public void remove(int i) {
        this.mBgHandler.obtainMessage(3002, i, 0).sendToTarget();
    }

    public void clear() {
        ArrayList arrayList = new ArrayList(this.mSortedKeys);
        updateSortedKeys(3003, null);
        this.mBgHandler.obtainMessage(3003).sendToTarget();
        this.mEntryManager.getVisibleNotifications().forEach(new Consumer(arrayList) {
            /* class com.android.systemui.statusbar.notification.policy.$$Lambda$KeyguardNotificationController$Na2b_4U1h0L_f2rlRPW0xKjNbIY */
            public final /* synthetic */ ArrayList f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                KeyguardNotificationController.this.lambda$clear$0$KeyguardNotificationController(this.f$1, (NotificationEntry) obj);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$clear$0 */
    public /* synthetic */ void lambda$clear$0$KeyguardNotificationController(ArrayList arrayList, NotificationEntry notificationEntry) {
        if (needReadd(notificationEntry) && arrayList.contains(notificationEntry.getKey())) {
            add(notificationEntry);
        }
    }

    private boolean needReadd(NotificationEntry notificationEntry) {
        Notification notification = notificationEntry.getSbn().getNotification();
        if (notificationEntry.getSbn().isClearable() && !MiuiNotificationCompat.isOnlyShowKeyguard(notification) && !MiuiNotificationCompat.isKeptOnKeyguard(notification)) {
            return false;
        }
        return true;
    }

    public List<String> getSortedKeys() {
        return this.mSortedKeys;
    }

    private void updateSortedKeys(int i, String str) {
        HashSet hashSet = new HashSet(this.mSortedKeys);
        switch (i) {
            case 3000:
            case 3001:
                hashSet.add(str);
                break;
            case 3002:
                hashSet.remove(str);
                break;
            case 3003:
                hashSet.clear();
                break;
        }
        this.mSortedKeys.clear();
        if (!hashSet.isEmpty()) {
            this.mEntryManager.getVisibleNotifications().forEach(new Consumer(hashSet) {
                /* class com.android.systemui.statusbar.notification.policy.$$Lambda$KeyguardNotificationController$GuZXS6bJBjs3ceHXMFgWBJ2pts */
                public final /* synthetic */ Set f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    KeyguardNotificationController.this.lambda$updateSortedKeys$1$KeyguardNotificationController(this.f$1, (NotificationEntry) obj);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateSortedKeys$1 */
    public /* synthetic */ void lambda$updateSortedKeys$1$KeyguardNotificationController(Set set, NotificationEntry notificationEntry) {
        if (set.contains(notificationEntry.getKey())) {
            this.mSortedKeys.add(notificationEntry.getKey());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleInsertDB(ContentValues contentValues) {
        try {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            contentResolver.insert(KeyguardNotification.URI, contentValues);
            contentResolver.notifyChange(KeyguardNotification.URI, null);
        } catch (Exception e) {
            Log.e("KeyguardNotifHelper", "handleInsertDB", e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUpdateDB(ContentValues contentValues) {
        int intValue = contentValues.getAsInteger("key").intValue();
        try {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            if (contentResolver.update(KeyguardNotification.URI, contentValues, "key" + "=" + intValue, null) > 0) {
                contentResolver.notifyChange(KeyguardNotification.URI, null);
            } else {
                handleInsertDB(contentValues);
            }
        } catch (Exception e) {
            Log.e("KeyguardNotifHelper", "handleUpdateDB", e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDeleteDB(int i) {
        try {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            if (contentResolver.delete(KeyguardNotification.URI, "key" + "=" + i, null) > 0) {
                contentResolver.notifyChange(KeyguardNotification.URI, null);
            }
        } catch (Exception e) {
            Log.e("KeyguardNotifHelper", "handleDeleteDB", e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleClearDB() {
        try {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            contentResolver.delete(KeyguardNotification.URI, null, null);
            contentResolver.notifyChange(KeyguardNotification.URI, null);
        } catch (Exception e) {
            Log.e("KeyguardNotifHelper", "handleClearDB", e);
        }
    }

    private ContentValues buildValues(NotificationEntry notificationEntry) {
        CharSequence charSequence;
        byte[] drawableToByte = drawableToByte(notificationEntry.getSbn().getAppIcon());
        if (drawableToByte == null) {
            return null;
        }
        boolean isSensitive = notificationEntry.isSensitive();
        Notification notification = notificationEntry.getSbn().getNotification();
        CharSequence appName = isSensitive ? notificationEntry.getSbn().getAppName() : NotificationUtil.resolveTitle(notification);
        CharSequence hiddenText = isSensitive ? NotificationUtil.getHiddenText() : NotificationUtil.resolveText(notification);
        String str = "";
        if (isSensitive) {
            charSequence = str;
        } else {
            charSequence = NotificationUtil.resolveSubText(notification);
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("icon", drawableToByte);
        contentValues.put("title", TextUtils.isEmpty(appName) ? str : appName.toString());
        contentValues.put("content", TextUtils.isEmpty(hiddenText) ? str : hiddenText.toString());
        contentValues.put("time", getTimeText(notificationEntry));
        contentValues.put("info", this.mSortedKeys.toString());
        if (!TextUtils.isEmpty(charSequence)) {
            str = charSequence.toString();
        }
        contentValues.put("subtext", str);
        contentValues.put("key", Integer.valueOf(notificationEntry.getKey().hashCode()));
        contentValues.put("pkg", notificationEntry.getSbn().getPackageName());
        contentValues.put("user_id", Integer.valueOf(notificationEntry.getSbn().getUserId()));
        return contentValues;
    }

    private byte[] drawableToByte(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof FancyDrawable) {
            FancyDrawable fancyDrawable = (FancyDrawable) drawable;
            Drawable quietDrawable = fancyDrawable.getQuietDrawable();
            if (quietDrawable == null) {
                fancyDrawable.getRoot().tick(SystemClock.elapsedRealtime());
            } else {
                drawable = quietDrawable;
            }
        }
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
            return null;
        }
        Bitmap createBitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        drawable.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
        drawable.draw(canvas);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        createBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private String getTimeText(NotificationEntry notificationEntry) {
        DateTimeView dateTimeView = new DateTimeView(this.mContext);
        long j = notificationEntry.getSbn().getNotification().when;
        if (j != 0 && j < System.currentTimeMillis() + 31449600000L) {
            dateTimeView.setTime(notificationEntry.getSbn().getNotification().when);
        }
        return dateTimeView.getText().toString();
    }
}
