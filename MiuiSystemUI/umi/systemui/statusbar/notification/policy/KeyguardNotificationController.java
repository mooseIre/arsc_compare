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
import codeinjection.CodeInjection;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.notification.MiuiNotificationCompat;
import com.android.systemui.statusbar.notification.MiuiNotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import miui.maml.FancyDrawable;
import miui.provider.KeyguardNotification;

public class KeyguardNotificationController {
    private Handler mBgHandler;
    private Context mContext;
    private MiuiNotificationEntryManager mEntryManager;
    private NotificationGroupManager mGroupManager;
    private KeyguardStateController mKeyguardStateController;
    private NotificationLockscreenUserManager mLockscreenUserManager;
    private final ArrayList<String> mSortedKeys = new ArrayList<>();

    public KeyguardNotificationController(Context context, NotificationEntryManager notificationEntryManager, NotificationGroupManager notificationGroupManager, KeyguardStateController keyguardStateController, NotificationLockscreenUserManager notificationLockscreenUserManager) {
        this.mContext = context;
        this.mEntryManager = (MiuiNotificationEntryManager) notificationEntryManager;
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
                        ContentValues buildValues = KeyguardNotificationController.this.buildValues((NotificationEntry) message.obj);
                        if (buildValues != null) {
                            KeyguardNotificationController.this.handleInsertDB(buildValues);
                            return;
                        }
                        return;
                    case 3001:
                        ContentValues buildValues2 = KeyguardNotificationController.this.buildValues((NotificationEntry) message.obj);
                        if (buildValues2 != null) {
                            KeyguardNotificationController.this.handleUpdateDB(buildValues2);
                            return;
                        }
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
        this.mBgHandler.obtainMessage(3000, notificationEntry).sendToTarget();
    }

    public void update(NotificationEntry notificationEntry) {
        updateSortedKeys(3001, notificationEntry.getKey());
        this.mBgHandler.obtainMessage(3001, notificationEntry).sendToTarget();
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
        this.mEntryManager.getFinalVisibleNotifications().stream().filter(new Predicate() {
            /* class com.android.systemui.statusbar.notification.policy.$$Lambda$KeyguardNotificationController$wQZ3W3KW5nGOmIU_stL2x4YFouM */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return KeyguardNotificationController.this.needReadd((NotificationEntry) obj);
            }
        }).filter(new Predicate(arrayList) {
            /* class com.android.systemui.statusbar.notification.policy.$$Lambda$KeyguardNotificationController$KoW3_XHXMSpczuC6npEAlR0JVS0 */
            public final /* synthetic */ ArrayList f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return this.f$0.contains(((NotificationEntry) obj).getKey());
            }
        }).forEach(new Consumer() {
            /* class com.android.systemui.statusbar.notification.policy.$$Lambda$ieMWCO1N96wbjpbwHZTfl2QngQ */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                KeyguardNotificationController.this.add((NotificationEntry) obj);
            }
        });
    }

    /* access modifiers changed from: private */
    public boolean needReadd(NotificationEntry notificationEntry) {
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
            Stream<R> filter = this.mEntryManager.getFinalVisibleNotifications().stream().map($$Lambda$kpoFpoZoiVX8t3fHvYFACoEdWls.INSTANCE).filter(new Predicate(hashSet) {
                /* class com.android.systemui.statusbar.notification.policy.$$Lambda$X3mL3VweBXQxL21HEcoksSGlEm4 */
                public final /* synthetic */ Set f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return this.f$0.contains((String) obj);
                }
            });
            ArrayList<String> arrayList = this.mSortedKeys;
            Objects.requireNonNull(arrayList);
            filter.forEach(new Consumer(arrayList) {
                /* class com.android.systemui.statusbar.notification.policy.$$Lambda$I1A8dniBuRdoMGKE4fgVMPvl9YM */
                public final /* synthetic */ ArrayList f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    this.f$0.add((String) obj);
                }
            });
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
        String str = CodeInjection.MD5;
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
        contentValues.put("info", new ArrayList(this.mSortedKeys).toString());
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
