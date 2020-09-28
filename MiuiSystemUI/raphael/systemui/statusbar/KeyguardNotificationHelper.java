package com.android.systemui.statusbar;

import android.app.Notification;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.widget.DateTimeView;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUI;
import com.android.systemui.miui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.notification.MiuiNotificationCompat;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.KeyguardNotificationController;
import com.miui.systemui.annotation.Inject;
import com.xiaomi.stat.MiStat;
import com.xiaomi.stat.b;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import miui.maml.FancyDrawable;
import miui.provider.KeyguardNotification;

public class KeyguardNotificationHelper {
    private static final boolean DEBUG = Constants.DEBUG;
    private Handler mBgHandler;
    private Context mContext;
    private final ArrayList<String> mSortedKeys = new ArrayList<>();

    public KeyguardNotificationHelper(@Inject Context context, @Inject(tag = "SysUiBg") Looper looper) {
        this.mContext = context;
        this.mBgHandler = new Handler(looper) {
            public void handleMessage(Message message) {
                super.handleMessage(message);
                switch (message.what) {
                    case 3000:
                        KeyguardNotificationHelper.this.handleInsertDB((ContentValues) message.obj);
                        return;
                    case 3001:
                        KeyguardNotificationHelper.this.handleUpdateDB((ContentValues) message.obj);
                        return;
                    case 3002:
                        KeyguardNotificationHelper keyguardNotificationHelper = KeyguardNotificationHelper.this;
                        int i = message.arg1;
                        Object obj = message.obj;
                        keyguardNotificationHelper.handleDeleteDB(i, obj != null ? (String) obj : null);
                        return;
                    case 3003:
                        KeyguardNotificationHelper.this.handleClearDB();
                        return;
                    default:
                        return;
                }
            }
        };
    }

    public void add(NotificationData.Entry entry) {
        NotificationGroupManager groupManager = ((StatusBar) SystemUI.getComponent(this.mContext, StatusBar.class)).getGroupManager();
        if (!entry.notification.getNotification().isGroupSummary()) {
            ExpandableNotificationRow groupSummary = groupManager.getGroupSummary((StatusBarNotification) entry.notification);
            if (groupSummary != null) {
                remove(groupSummary.getEntry().key.hashCode(), groupSummary.getEntry().notification.getPackageName());
            }
        } else if (groupManager.isSummaryOfGroup(entry.notification)) {
            return;
        }
        updateSortedKeys(3000, entry.key);
        ContentValues buildValues = buildValues(entry);
        if (buildValues != null) {
            this.mBgHandler.obtainMessage(3000, buildValues).sendToTarget();
        }
    }

    public void update(NotificationData.Entry entry) {
        updateSortedKeys(3001, entry.key);
        ContentValues buildValues = buildValues(entry);
        if (buildValues != null) {
            this.mBgHandler.obtainMessage(3001, buildValues).sendToTarget();
        }
    }

    public void remove(String str, String str2) {
        updateSortedKeys(3002, str);
        remove(str.hashCode(), str2);
    }

    public void remove(int i, String str) {
        this.mBgHandler.obtainMessage(3002, i, 0, str).sendToTarget();
    }

    public void clear() {
        ArrayList arrayList = new ArrayList(this.mSortedKeys);
        updateSortedKeys(3003, (String) null);
        this.mBgHandler.obtainMessage(3003).sendToTarget();
        ((StatusBar) SystemUI.getComponent(this.mContext, StatusBar.class)).getNotificationData().getActiveNotifications().forEach(new Consumer(arrayList) {
            private final /* synthetic */ ArrayList f$1;

            {
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                KeyguardNotificationHelper.this.lambda$clear$0$KeyguardNotificationHelper(this.f$1, (NotificationData.Entry) obj);
            }
        });
    }

    public /* synthetic */ void lambda$clear$0$KeyguardNotificationHelper(ArrayList arrayList, NotificationData.Entry entry) {
        if (needReadd(entry) && arrayList.contains(entry.key)) {
            add(entry);
        }
    }

    public boolean needReadd(NotificationData.Entry entry) {
        Notification notification = entry.notification.getNotification();
        if (entry.notification.isClearable() && !MiuiNotificationCompat.isOnlyShowKeyguard(notification) && !MiuiNotificationCompat.isKeptOnKeyguard(notification)) {
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
            ((StatusBar) SystemUI.getComponent(this.mContext, StatusBar.class)).getNotificationData().getActiveNotifications().forEach(new Consumer(hashSet) {
                private final /* synthetic */ Set f$1;

                {
                    this.f$1 = r2;
                }

                public final void accept(Object obj) {
                    KeyguardNotificationHelper.this.lambda$updateSortedKeys$1$KeyguardNotificationHelper(this.f$1, (NotificationData.Entry) obj);
                }
            });
        }
        if (DEBUG) {
            Log.d("KeyguardNotificationHelper", "mSortedKeys " + this.mSortedKeys.toString());
        }
    }

    public /* synthetic */ void lambda$updateSortedKeys$1$KeyguardNotificationHelper(Set set, NotificationData.Entry entry) {
        if (set.contains(entry.key)) {
            this.mSortedKeys.add(entry.key);
        }
    }

    /* access modifiers changed from: private */
    public void handleInsertDB(ContentValues contentValues) {
        String asString = contentValues.getAsString("pkg");
        try {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            contentResolver.insert(KeyguardNotification.URI, contentValues);
            contentResolver.notifyChange(KeyguardNotification.URI, (ContentObserver) null);
            ((KeyguardNotificationController) Dependency.get(KeyguardNotificationController.class)).add(asString);
        } catch (Exception e) {
            Log.e("KeyguardNotifHelper", "handleInsertDB", e);
        }
    }

    /* access modifiers changed from: private */
    public void handleUpdateDB(ContentValues contentValues) {
        int intValue = contentValues.getAsInteger("key").intValue();
        String asString = contentValues.getAsString("pkg");
        try {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            if (contentResolver.update(KeyguardNotification.URI, contentValues, "key" + "=" + intValue, (String[]) null) > 0) {
                contentResolver.notifyChange(KeyguardNotification.URI, (ContentObserver) null);
                ((KeyguardNotificationController) Dependency.get(KeyguardNotificationController.class)).update(asString);
                return;
            }
            handleInsertDB(contentValues);
        } catch (Exception e) {
            Log.e("KeyguardNotifHelper", "handleUpdateDB", e);
        }
    }

    /* access modifiers changed from: private */
    public void handleDeleteDB(int i, String str) {
        try {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            if (contentResolver.delete(KeyguardNotification.URI, "key" + "=" + i, (String[]) null) > 0) {
                contentResolver.notifyChange(KeyguardNotification.URI, (ContentObserver) null);
                if (!TextUtils.isEmpty(str)) {
                    ((KeyguardNotificationController) Dependency.get(KeyguardNotificationController.class)).delete(str);
                }
            }
        } catch (Exception e) {
            Log.e("KeyguardNotifHelper", "handleDeleteDB", e);
        }
    }

    /* access modifiers changed from: private */
    public void handleClearDB() {
        try {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            contentResolver.delete(KeyguardNotification.URI, (String) null, (String[]) null);
            contentResolver.notifyChange(KeyguardNotification.URI, (ContentObserver) null);
            ((KeyguardNotificationController) Dependency.get(KeyguardNotificationController.class)).clearAll();
        } catch (Exception e) {
            Log.e("KeyguardNotifHelper", "handleClearDB", e);
        }
    }

    private ContentValues buildValues(NotificationData.Entry entry) {
        CharSequence charSequence;
        byte[] byteIcon = getByteIcon(entry);
        if (byteIcon == null) {
            return null;
        }
        boolean z = entry.hideSensitive || entry.hideSensitiveByAppLock;
        Notification notification = entry.notification.getNotification();
        CharSequence appName = z ? entry.notification.getAppName() : NotificationUtil.resolveTitle(notification);
        CharSequence hiddenText = z ? NotificationUtil.getHiddenText(this.mContext) : NotificationUtil.resolveText(notification);
        String str = "";
        if (z) {
            charSequence = str;
        } else {
            charSequence = NotificationUtil.resolveSubText(notification);
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("icon", byteIcon);
        contentValues.put("title", TextUtils.isEmpty(appName) ? str : appName.toString());
        contentValues.put(MiStat.Param.CONTENT, TextUtils.isEmpty(hiddenText) ? str : hiddenText.toString());
        contentValues.put(b.j, getTimeText(entry));
        contentValues.put("info", this.mSortedKeys.toString());
        if (!TextUtils.isEmpty(charSequence)) {
            str = charSequence.toString();
        }
        contentValues.put("subtext", str);
        contentValues.put("key", Integer.valueOf(entry.key.hashCode()));
        contentValues.put("pkg", entry.notification.getPackageName());
        contentValues.put("user_id", Integer.valueOf(entry.notification.getUserId()));
        return contentValues;
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0032  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0038  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private byte[] getByteIcon(com.android.systemui.statusbar.NotificationData.Entry r4) {
        /*
            r3 = this;
            boolean r0 = r4.hideSensitive
            r1 = 16909313(0x1020401, float:2.3880102E-38)
            r2 = 0
            if (r0 != 0) goto L_0x0024
            boolean r0 = r4.hideSensitiveByAppLock
            if (r0 == 0) goto L_0x000d
            goto L_0x0024
        L_0x000d:
            android.view.View r0 = r4.getPrivateView()
            com.android.systemui.statusbar.NotificationContentView r0 = (com.android.systemui.statusbar.NotificationContentView) r0
            if (r0 == 0) goto L_0x0022
            android.view.View r0 = r0.getContractedChild()
            if (r0 == 0) goto L_0x0022
            android.view.View r0 = r0.findViewById(r1)
            android.widget.ImageView r0 = (android.widget.ImageView) r0
            goto L_0x0030
        L_0x0022:
            r0 = r2
            goto L_0x0030
        L_0x0024:
            android.view.View r0 = r4.getPublicContentView()
            if (r0 == 0) goto L_0x0022
            android.view.View r0 = r0.findViewById(r1)
            android.widget.ImageView r0 = (android.widget.ImageView) r0
        L_0x0030:
            if (r0 == 0) goto L_0x0036
            android.graphics.drawable.Drawable r2 = r0.getDrawable()
        L_0x0036:
            if (r2 != 0) goto L_0x003e
            com.android.systemui.miui.statusbar.ExpandedNotification r4 = r4.notification
            android.graphics.drawable.Drawable r2 = r4.getAppIcon()
        L_0x003e:
            byte[] r3 = r3.drawableToByte(r2)
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.KeyguardNotificationHelper.getByteIcon(com.android.systemui.statusbar.NotificationData$Entry):byte[]");
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

    private String getTimeText(NotificationData.Entry entry) {
        DateTimeView dateTimeView = new DateTimeView(this.mContext);
        if (entry.notification.getNotification().when != 0) {
            dateTimeView.setTime(entry.notification.getNotification().when);
        }
        return dateTimeView.getText().toString();
    }
}
