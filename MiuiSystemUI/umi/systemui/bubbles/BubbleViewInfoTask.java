package com.android.systemui.bubbles;

import android.app.Notification;
import android.app.Person;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.util.PathParser;
import android.view.LayoutInflater;
import com.android.internal.graphics.ColorUtils;
import com.android.launcher3.icons.BitmapInfo;
import com.android.systemui.C0014R$layout;
import com.android.systemui.bubbles.Bubble;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.lang.ref.WeakReference;
import java.util.Objects;

public class BubbleViewInfoTask extends AsyncTask<Void, Void, BubbleViewInfo> {
    private Bubble mBubble;
    private Callback mCallback;
    private WeakReference<Context> mContext;
    private BubbleIconFactory mIconFactory;
    private boolean mSkipInflation;
    private WeakReference<BubbleStackView> mStackView;

    public interface Callback {
        void onBubbleViewsReady(Bubble bubble);
    }

    BubbleViewInfoTask(Bubble bubble, Context context, BubbleStackView bubbleStackView, BubbleIconFactory bubbleIconFactory, boolean z, Callback callback) {
        this.mBubble = bubble;
        this.mContext = new WeakReference<>(context);
        this.mStackView = new WeakReference<>(bubbleStackView);
        this.mIconFactory = bubbleIconFactory;
        this.mSkipInflation = z;
        this.mCallback = callback;
    }

    /* access modifiers changed from: protected */
    public BubbleViewInfo doInBackground(Void... voidArr) {
        return BubbleViewInfo.populate((Context) this.mContext.get(), (BubbleStackView) this.mStackView.get(), this.mIconFactory, this.mBubble, this.mSkipInflation);
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(BubbleViewInfo bubbleViewInfo) {
        if (bubbleViewInfo != null) {
            this.mBubble.setViewInfo(bubbleViewInfo);
            if (this.mCallback != null && !isCancelled()) {
                this.mCallback.onBubbleViewsReady(this.mBubble);
            }
        }
    }

    static class BubbleViewInfo {
        String appName;
        Drawable badgedAppIcon;
        Bitmap badgedBubbleImage;
        int dotColor;
        Path dotPath;
        BubbleExpandedView expandedView;
        Bubble.FlyoutMessage flyoutMessage;
        BadgedImageView imageView;
        ShortcutInfo shortcutInfo;

        BubbleViewInfo() {
        }

        static BubbleViewInfo populate(Context context, BubbleStackView bubbleStackView, BubbleIconFactory bubbleIconFactory, Bubble bubble, boolean z) {
            BubbleViewInfo bubbleViewInfo = new BubbleViewInfo();
            if (!z && !bubble.isInflated()) {
                LayoutInflater from = LayoutInflater.from(context);
                bubbleViewInfo.imageView = (BadgedImageView) from.inflate(C0014R$layout.bubble_view, bubbleStackView, false);
                BubbleExpandedView bubbleExpandedView = (BubbleExpandedView) from.inflate(C0014R$layout.bubble_expanded_view, bubbleStackView, false);
                bubbleViewInfo.expandedView = bubbleExpandedView;
                bubbleExpandedView.setStackView(bubbleStackView);
            }
            if (bubble.getShortcutInfo() != null) {
                bubbleViewInfo.shortcutInfo = bubble.getShortcutInfo();
            }
            PackageManager packageManager = context.getPackageManager();
            try {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(bubble.getPackageName(), 795136);
                if (applicationInfo != null) {
                    bubbleViewInfo.appName = String.valueOf(packageManager.getApplicationLabel(applicationInfo));
                }
                Drawable applicationIcon = packageManager.getApplicationIcon(bubble.getPackageName());
                Drawable userBadgedIcon = packageManager.getUserBadgedIcon(applicationIcon, bubble.getUser());
                Drawable bubbleDrawable = bubbleIconFactory.getBubbleDrawable(context, bubbleViewInfo.shortcutInfo, bubble.getIcon());
                if (bubbleDrawable != null) {
                    applicationIcon = bubbleDrawable;
                }
                BitmapInfo badgeBitmap = bubbleIconFactory.getBadgeBitmap(userBadgedIcon, bubble.isImportantConversation());
                bubbleViewInfo.badgedAppIcon = userBadgedIcon;
                bubbleViewInfo.badgedBubbleImage = bubbleIconFactory.getBubbleBitmap(applicationIcon, badgeBitmap).icon;
                Path createPathFromPathData = PathParser.createPathFromPathData(context.getResources().getString(17039929));
                Matrix matrix = new Matrix();
                float scale = bubbleIconFactory.getNormalizer().getScale(applicationIcon, (RectF) null, (Path) null, (boolean[]) null);
                matrix.setScale(scale, scale, 50.0f, 50.0f);
                createPathFromPathData.transform(matrix);
                bubbleViewInfo.dotPath = createPathFromPathData;
                bubbleViewInfo.dotColor = ColorUtils.blendARGB(badgeBitmap.color, -1, 0.54f);
                Bubble.FlyoutMessage flyoutMessage2 = bubble.getFlyoutMessage();
                bubbleViewInfo.flyoutMessage = flyoutMessage2;
                if (flyoutMessage2 != null) {
                    flyoutMessage2.senderAvatar = BubbleViewInfoTask.loadSenderAvatar(context, flyoutMessage2.senderIcon);
                }
                return bubbleViewInfo;
            } catch (PackageManager.NameNotFoundException unused) {
                Log.w("Bubbles", "Unable to find package: " + bubble.getPackageName());
                return null;
            }
        }
    }

    static Bubble.FlyoutMessage extractFlyoutMessage(NotificationEntry notificationEntry) {
        Objects.requireNonNull(notificationEntry);
        Notification notification = notificationEntry.getSbn().getNotification();
        Class notificationStyle = notification.getNotificationStyle();
        Bubble.FlyoutMessage flyoutMessage = new Bubble.FlyoutMessage();
        flyoutMessage.isGroupChat = notification.extras.getBoolean("android.isGroupConversation");
        try {
            if (Notification.BigTextStyle.class.equals(notificationStyle)) {
                CharSequence charSequence = notification.extras.getCharSequence("android.bigText");
                if (TextUtils.isEmpty(charSequence)) {
                    charSequence = notification.extras.getCharSequence("android.text");
                }
                flyoutMessage.message = charSequence;
                return flyoutMessage;
            }
            if (Notification.MessagingStyle.class.equals(notificationStyle)) {
                Notification.MessagingStyle.Message findLatestIncomingMessage = Notification.MessagingStyle.findLatestIncomingMessage(Notification.MessagingStyle.Message.getMessagesFromBundleArray((Parcelable[]) notification.extras.get("android.messages")));
                if (findLatestIncomingMessage != null) {
                    flyoutMessage.message = findLatestIncomingMessage.getText();
                    Person senderPerson = findLatestIncomingMessage.getSenderPerson();
                    Icon icon = null;
                    flyoutMessage.senderName = senderPerson != null ? senderPerson.getName() : null;
                    flyoutMessage.senderAvatar = null;
                    if (senderPerson != null) {
                        icon = senderPerson.getIcon();
                    }
                    flyoutMessage.senderIcon = icon;
                    return flyoutMessage;
                }
            } else if (Notification.InboxStyle.class.equals(notificationStyle)) {
                CharSequence[] charSequenceArray = notification.extras.getCharSequenceArray("android.textLines");
                if (charSequenceArray != null && charSequenceArray.length > 0) {
                    flyoutMessage.message = charSequenceArray[charSequenceArray.length - 1];
                    return flyoutMessage;
                }
            } else if (Notification.MediaStyle.class.equals(notificationStyle)) {
                return flyoutMessage;
            } else {
                flyoutMessage.message = notification.extras.getCharSequence("android.text");
                return flyoutMessage;
            }
            return flyoutMessage;
        } catch (ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    static Drawable loadSenderAvatar(Context context, Icon icon) {
        Objects.requireNonNull(context);
        if (icon == null) {
            return null;
        }
        if (icon.getType() == 4 || icon.getType() == 6) {
            context.grantUriPermission(context.getPackageName(), icon.getUri(), 1);
        }
        return icon.loadDrawable(context);
    }
}
