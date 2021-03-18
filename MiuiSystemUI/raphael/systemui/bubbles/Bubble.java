package com.android.systemui.bubbles;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.AsyncTask;
import android.os.UserHandle;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.InstanceId;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.bubbles.BubbleViewInfoTask;
import com.android.systemui.shared.system.SysUiStatsLog;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Objects;

/* access modifiers changed from: package-private */
public class Bubble implements BubbleViewProvider {
    private String mAppName;
    private int mAppUid = -1;
    private Drawable mBadgedAppIcon;
    private Bitmap mBadgedImage;
    private String mChannelId;
    private PendingIntent mDeleteIntent;
    private int mDesiredHeight;
    private int mDesiredHeightResId;
    private int mDotColor;
    private Path mDotPath;
    private BubbleExpandedView mExpandedView;
    private int mFlags;
    private FlyoutMessage mFlyoutMessage;
    private Icon mIcon;
    private BadgedImageView mIconView;
    private boolean mInflateSynchronously;
    private BubbleViewInfoTask mInflationTask;
    private InstanceId mInstanceId;
    private PendingIntent mIntent;
    private boolean mIntentActive;
    private PendingIntent.CancelListener mIntentCancelListener;
    private boolean mIsBubble;
    private boolean mIsClearable;
    private boolean mIsImportantConversation;
    private boolean mIsVisuallyInterruptive;
    private final String mKey;
    private long mLastAccessed;
    private long mLastUpdated;
    private String mMetadataShortcutId;
    private int mNotificationId;
    private String mPackageName;
    private boolean mPendingIntentCanceled;
    private ShortcutInfo mShortcutInfo;
    private boolean mShouldSuppressNotificationDot;
    private boolean mShouldSuppressNotificationList;
    private boolean mShouldSuppressPeek;
    private boolean mShowBubbleUpdateDot = true;
    private boolean mSuppressFlyout;
    private BubbleController.NotificationSuppressionChangedListener mSuppressionListener;
    private String mTitle;
    private UserHandle mUser;

    public static class FlyoutMessage {
        public boolean isGroupChat;
        public CharSequence message;
        public Drawable senderAvatar;
        public Icon senderIcon;
        public CharSequence senderName;
    }

    Bubble(String str, ShortcutInfo shortcutInfo, int i, int i2, String str2) {
        Objects.requireNonNull(str);
        Objects.requireNonNull(shortcutInfo);
        this.mMetadataShortcutId = shortcutInfo.getId();
        this.mShortcutInfo = shortcutInfo;
        this.mKey = str;
        this.mFlags = 0;
        this.mUser = shortcutInfo.getUserHandle();
        this.mPackageName = shortcutInfo.getPackage();
        this.mIcon = shortcutInfo.getIcon();
        this.mDesiredHeight = i;
        this.mDesiredHeightResId = i2;
        this.mTitle = str2;
        this.mShowBubbleUpdateDot = false;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    Bubble(NotificationEntry notificationEntry, BubbleController.NotificationSuppressionChangedListener notificationSuppressionChangedListener, BubbleController.PendingIntentCanceledListener pendingIntentCanceledListener) {
        Objects.requireNonNull(notificationEntry);
        this.mKey = notificationEntry.getKey();
        this.mSuppressionListener = notificationSuppressionChangedListener;
        this.mIntentCancelListener = new PendingIntent.CancelListener(pendingIntentCanceledListener) {
            /* class com.android.systemui.bubbles.$$Lambda$Bubble$Ycd3LZAa6VyWLbOckhAm9_pvjnE */
            public final /* synthetic */ BubbleController.PendingIntentCanceledListener f$1;

            {
                this.f$1 = r2;
            }

            public final void onCancelled(PendingIntent pendingIntent) {
                Bubble.this.lambda$new$0$Bubble(this.f$1, pendingIntent);
            }
        };
        setEntry(notificationEntry);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$Bubble(BubbleController.PendingIntentCanceledListener pendingIntentCanceledListener, PendingIntent pendingIntent) {
        PendingIntent pendingIntent2 = this.mIntent;
        if (pendingIntent2 != null) {
            pendingIntent2.unregisterCancelListener(this.mIntentCancelListener);
        }
        pendingIntentCanceledListener.onPendingIntentCanceled(this);
    }

    @Override // com.android.systemui.bubbles.BubbleViewProvider
    public String getKey() {
        return this.mKey;
    }

    public UserHandle getUser() {
        return this.mUser;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    @Override // com.android.systemui.bubbles.BubbleViewProvider
    public Bitmap getBadgedImage() {
        return this.mBadgedImage;
    }

    public Drawable getBadgedAppIcon() {
        return this.mBadgedAppIcon;
    }

    @Override // com.android.systemui.bubbles.BubbleViewProvider
    public int getDotColor() {
        return this.mDotColor;
    }

    @Override // com.android.systemui.bubbles.BubbleViewProvider
    public Path getDotPath() {
        return this.mDotPath;
    }

    public String getAppName() {
        return this.mAppName;
    }

    public ShortcutInfo getShortcutInfo() {
        return this.mShortcutInfo;
    }

    @Override // com.android.systemui.bubbles.BubbleViewProvider
    public BadgedImageView getIconView() {
        return this.mIconView;
    }

    @Override // com.android.systemui.bubbles.BubbleViewProvider
    public BubbleExpandedView getExpandedView() {
        return this.mExpandedView;
    }

    public String getTitle() {
        return this.mTitle;
    }

    /* access modifiers changed from: package-private */
    public String getMetadataShortcutId() {
        return this.mMetadataShortcutId;
    }

    /* access modifiers changed from: package-private */
    public boolean hasMetadataShortcutId() {
        String str = this.mMetadataShortcutId;
        return str != null && !str.isEmpty();
    }

    /* access modifiers changed from: package-private */
    public void cleanupViews() {
        BubbleExpandedView bubbleExpandedView = this.mExpandedView;
        if (bubbleExpandedView != null) {
            bubbleExpandedView.cleanUpExpandedState();
            this.mExpandedView = null;
        }
        this.mIconView = null;
        PendingIntent pendingIntent = this.mIntent;
        if (pendingIntent != null) {
            pendingIntent.unregisterCancelListener(this.mIntentCancelListener);
        }
        this.mIntentActive = false;
    }

    /* access modifiers changed from: package-private */
    public void setPendingIntentCanceled() {
        this.mPendingIntentCanceled = true;
    }

    /* access modifiers changed from: package-private */
    public boolean getPendingIntentCanceled() {
        return this.mPendingIntentCanceled;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setInflateSynchronously(boolean z) {
        this.mInflateSynchronously = z;
    }

    /* access modifiers changed from: package-private */
    public void inflate(BubbleViewInfoTask.Callback callback, Context context, BubbleStackView bubbleStackView, BubbleIconFactory bubbleIconFactory, boolean z) {
        if (isBubbleLoading()) {
            this.mInflationTask.cancel(true);
        }
        BubbleViewInfoTask bubbleViewInfoTask = new BubbleViewInfoTask(this, context, bubbleStackView, bubbleIconFactory, z, callback);
        this.mInflationTask = bubbleViewInfoTask;
        if (this.mInflateSynchronously) {
            bubbleViewInfoTask.onPostExecute(bubbleViewInfoTask.doInBackground(new Void[0]));
        } else {
            bubbleViewInfoTask.execute(new Void[0]);
        }
    }

    private boolean isBubbleLoading() {
        BubbleViewInfoTask bubbleViewInfoTask = this.mInflationTask;
        return (bubbleViewInfoTask == null || bubbleViewInfoTask.getStatus() == AsyncTask.Status.FINISHED) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public boolean isInflated() {
        return (this.mIconView == null || this.mExpandedView == null) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public void stopInflation() {
        BubbleViewInfoTask bubbleViewInfoTask = this.mInflationTask;
        if (bubbleViewInfoTask != null) {
            bubbleViewInfoTask.cancel(true);
            cleanupViews();
        }
    }

    /* access modifiers changed from: package-private */
    public void setViewInfo(BubbleViewInfoTask.BubbleViewInfo bubbleViewInfo) {
        if (!isInflated()) {
            this.mIconView = bubbleViewInfo.imageView;
            this.mExpandedView = bubbleViewInfo.expandedView;
        }
        this.mShortcutInfo = bubbleViewInfo.shortcutInfo;
        this.mAppName = bubbleViewInfo.appName;
        this.mFlyoutMessage = bubbleViewInfo.flyoutMessage;
        this.mBadgedAppIcon = bubbleViewInfo.badgedAppIcon;
        this.mBadgedImage = bubbleViewInfo.badgedBubbleImage;
        this.mDotColor = bubbleViewInfo.dotColor;
        this.mDotPath = bubbleViewInfo.dotPath;
        BubbleExpandedView bubbleExpandedView = this.mExpandedView;
        if (bubbleExpandedView != null) {
            bubbleExpandedView.update(this);
        }
        BadgedImageView badgedImageView = this.mIconView;
        if (badgedImageView != null) {
            badgedImageView.setRenderedBubble(this);
        }
    }

    @Override // com.android.systemui.bubbles.BubbleViewProvider
    public void setContentVisibility(boolean z) {
        BubbleExpandedView bubbleExpandedView = this.mExpandedView;
        if (bubbleExpandedView != null) {
            bubbleExpandedView.setContentVisibility(z);
        }
    }

    /* access modifiers changed from: package-private */
    public void setEntry(NotificationEntry notificationEntry) {
        PendingIntent pendingIntent;
        Objects.requireNonNull(notificationEntry);
        Objects.requireNonNull(notificationEntry.getSbn());
        this.mLastUpdated = notificationEntry.getSbn().getPostTime();
        this.mIsBubble = notificationEntry.getSbn().getNotification().isBubbleNotification();
        this.mPackageName = notificationEntry.getSbn().getPackageName();
        this.mUser = notificationEntry.getSbn().getUser();
        this.mTitle = getTitle(notificationEntry);
        this.mIsClearable = notificationEntry.isClearable();
        this.mShouldSuppressNotificationDot = notificationEntry.shouldSuppressNotificationDot();
        this.mShouldSuppressNotificationList = notificationEntry.shouldSuppressNotificationList();
        this.mShouldSuppressPeek = notificationEntry.shouldSuppressPeek();
        this.mChannelId = notificationEntry.getSbn().getNotification().getChannelId();
        this.mNotificationId = notificationEntry.getSbn().getId();
        this.mAppUid = notificationEntry.getSbn().getUid();
        this.mInstanceId = notificationEntry.getSbn().getInstanceId();
        this.mFlyoutMessage = BubbleViewInfoTask.extractFlyoutMessage(notificationEntry);
        this.mShortcutInfo = notificationEntry.getRanking() != null ? notificationEntry.getRanking().getShortcutInfo() : null;
        this.mMetadataShortcutId = notificationEntry.getBubbleMetadata() != null ? notificationEntry.getBubbleMetadata().getShortcutId() : null;
        if (notificationEntry.getRanking() != null) {
            this.mIsVisuallyInterruptive = notificationEntry.getRanking().visuallyInterruptive();
        }
        if (notificationEntry.getBubbleMetadata() != null) {
            this.mFlags = notificationEntry.getBubbleMetadata().getFlags();
            this.mDesiredHeight = notificationEntry.getBubbleMetadata().getDesiredHeight();
            this.mDesiredHeightResId = notificationEntry.getBubbleMetadata().getDesiredHeightResId();
            this.mIcon = notificationEntry.getBubbleMetadata().getIcon();
            if (!this.mIntentActive || (pendingIntent = this.mIntent) == null) {
                PendingIntent pendingIntent2 = this.mIntent;
                if (pendingIntent2 != null) {
                    pendingIntent2.unregisterCancelListener(this.mIntentCancelListener);
                }
                PendingIntent intent = notificationEntry.getBubbleMetadata().getIntent();
                this.mIntent = intent;
                if (intent != null) {
                    intent.registerCancelListener(this.mIntentCancelListener);
                }
            } else if (pendingIntent != null && notificationEntry.getBubbleMetadata().getIntent() == null) {
                this.mIntent.unregisterCancelListener(this.mIntentCancelListener);
                this.mIntent = null;
            }
            this.mDeleteIntent = notificationEntry.getBubbleMetadata().getDeleteIntent();
        }
        this.mIsImportantConversation = notificationEntry.getChannel() != null && notificationEntry.getChannel().isImportantConversation();
    }

    /* access modifiers changed from: package-private */
    public Icon getIcon() {
        return this.mIcon;
    }

    /* access modifiers changed from: package-private */
    public boolean isVisuallyInterruptive() {
        return this.mIsVisuallyInterruptive;
    }

    /* access modifiers changed from: package-private */
    public long getLastActivity() {
        return Math.max(this.mLastUpdated, this.mLastAccessed);
    }

    /* access modifiers changed from: package-private */
    public void setIntentActive() {
        this.mIntentActive = true;
    }

    /* access modifiers changed from: package-private */
    public boolean isIntentActive() {
        return this.mIntentActive;
    }

    @Override // com.android.systemui.bubbles.BubbleViewProvider
    public int getDisplayId() {
        BubbleExpandedView bubbleExpandedView = this.mExpandedView;
        if (bubbleExpandedView != null) {
            return bubbleExpandedView.getVirtualDisplayId();
        }
        return -1;
    }

    public InstanceId getInstanceId() {
        return this.mInstanceId;
    }

    /* access modifiers changed from: package-private */
    public void markAsAccessedAt(long j) {
        this.mLastAccessed = j;
        setSuppressNotification(true);
        setShowDot(false);
    }

    /* access modifiers changed from: package-private */
    public boolean showInShade() {
        return !shouldSuppressNotification() || !this.mIsClearable;
    }

    /* access modifiers changed from: package-private */
    public boolean isImportantConversation() {
        return this.mIsImportantConversation;
    }

    /* access modifiers changed from: package-private */
    public void setSuppressNotification(boolean z) {
        BubbleController.NotificationSuppressionChangedListener notificationSuppressionChangedListener;
        boolean showInShade = showInShade();
        if (z) {
            this.mFlags |= 2;
        } else {
            this.mFlags &= -3;
        }
        if (showInShade() != showInShade && (notificationSuppressionChangedListener = this.mSuppressionListener) != null) {
            notificationSuppressionChangedListener.onBubbleNotificationSuppressionChange(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void setShowDot(boolean z) {
        this.mShowBubbleUpdateDot = z;
        BadgedImageView badgedImageView = this.mIconView;
        if (badgedImageView != null) {
            badgedImageView.updateDotVisibility(true);
        }
    }

    @Override // com.android.systemui.bubbles.BubbleViewProvider
    public boolean showDot() {
        return this.mShowBubbleUpdateDot && !this.mShouldSuppressNotificationDot && !shouldSuppressNotification();
    }

    /* access modifiers changed from: package-private */
    public boolean showFlyout() {
        return !this.mSuppressFlyout && !this.mShouldSuppressPeek && !shouldSuppressNotification() && !this.mShouldSuppressNotificationList;
    }

    /* access modifiers changed from: package-private */
    public void setSuppressFlyout(boolean z) {
        this.mSuppressFlyout = z;
    }

    /* access modifiers changed from: package-private */
    public FlyoutMessage getFlyoutMessage() {
        return this.mFlyoutMessage;
    }

    /* access modifiers changed from: package-private */
    public int getRawDesiredHeight() {
        return this.mDesiredHeight;
    }

    /* access modifiers changed from: package-private */
    public int getRawDesiredHeightResId() {
        return this.mDesiredHeightResId;
    }

    /* access modifiers changed from: package-private */
    public float getDesiredHeight(Context context) {
        if (this.mDesiredHeightResId != 0) {
            return (float) getDimenForPackageUser(context, this.mDesiredHeightResId, this.mPackageName, this.mUser.getIdentifier());
        }
        return ((float) this.mDesiredHeight) * context.getResources().getDisplayMetrics().density;
    }

    /* access modifiers changed from: package-private */
    public String getDesiredHeightString() {
        if (this.mDesiredHeightResId != 0) {
            return String.valueOf(this.mDesiredHeightResId);
        }
        return String.valueOf(this.mDesiredHeight);
    }

    /* access modifiers changed from: package-private */
    public PendingIntent getBubbleIntent() {
        return this.mIntent;
    }

    /* access modifiers changed from: package-private */
    public PendingIntent getDeleteIntent() {
        return this.mDeleteIntent;
    }

    /* access modifiers changed from: package-private */
    public Intent getSettingsIntent(Context context) {
        Intent intent = new Intent("android.settings.APP_NOTIFICATION_BUBBLE_SETTINGS");
        intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
        int uid = getUid(context);
        if (uid != -1) {
            intent.putExtra("app_uid", uid);
        }
        intent.addFlags(134217728);
        intent.addFlags(268435456);
        intent.addFlags(536870912);
        return intent;
    }

    public int getAppUid() {
        return this.mAppUid;
    }

    private int getUid(Context context) {
        int i = this.mAppUid;
        if (i != -1) {
            return i;
        }
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            return -1;
        }
        try {
            return packageManager.getApplicationInfo(this.mShortcutInfo.getPackage(), 0).uid;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Bubble", "cannot find uid", e);
            return -1;
        }
    }

    private int getDimenForPackageUser(Context context, int i, String str, int i2) {
        PackageManager packageManager = context.getPackageManager();
        if (str != null) {
            if (i2 == -1) {
                i2 = 0;
            }
            try {
                return packageManager.getResourcesForApplicationAsUser(str, i2).getDimensionPixelSize(i);
            } catch (PackageManager.NameNotFoundException unused) {
            } catch (Resources.NotFoundException e) {
                Log.e("Bubble", "Couldn't find desired height res id", e);
            }
        }
        return 0;
    }

    private boolean shouldSuppressNotification() {
        return isEnabled(2);
    }

    public boolean shouldAutoExpand() {
        return isEnabled(1);
    }

    /* access modifiers changed from: package-private */
    public void setShouldAutoExpand(boolean z) {
        if (z) {
            enable(1);
        } else {
            disable(1);
        }
    }

    public void setIsBubble(boolean z) {
        this.mIsBubble = z;
    }

    public boolean isBubble() {
        return this.mIsBubble;
    }

    public void enable(int i) {
        this.mFlags = i | this.mFlags;
    }

    public void disable(int i) {
        this.mFlags = (~i) & this.mFlags;
    }

    public boolean isEnabled(int i) {
        return (this.mFlags & i) != 0;
    }

    public String toString() {
        return "Bubble{" + this.mKey + '}';
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.print("key: ");
        printWriter.println(this.mKey);
        printWriter.print("  showInShade:   ");
        printWriter.println(showInShade());
        printWriter.print("  showDot:       ");
        printWriter.println(showDot());
        printWriter.print("  showFlyout:    ");
        printWriter.println(showFlyout());
        printWriter.print("  desiredHeight: ");
        printWriter.println(getDesiredHeightString());
        printWriter.print("  suppressNotif: ");
        printWriter.println(shouldSuppressNotification());
        printWriter.print("  autoExpand:    ");
        printWriter.println(shouldAutoExpand());
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Bubble)) {
            return false;
        }
        return Objects.equals(this.mKey, ((Bubble) obj).mKey);
    }

    public int hashCode() {
        return Objects.hash(this.mKey);
    }

    @Override // com.android.systemui.bubbles.BubbleViewProvider
    public void logUIEvent(int i, int i2, float f, float f2, int i3) {
        SysUiStatsLog.write(149, this.mPackageName, this.mChannelId, this.mNotificationId, i3, i, i2, f, f2, showInShade(), false, false);
    }

    private static String getTitle(NotificationEntry notificationEntry) {
        CharSequence charSequence = notificationEntry.getSbn().getNotification().extras.getCharSequence("android.title");
        if (charSequence == null) {
            return null;
        }
        return charSequence.toString();
    }
}
