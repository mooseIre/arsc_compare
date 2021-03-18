package com.android.systemui.statusbar.notification.row;

import android.app.ActivityManager;
import android.app.Notification;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.widget.ImageResolver;
import com.android.internal.widget.LocalImageResolver;
import com.android.internal.widget.MessagingMessage;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class NotificationInlineImageResolver implements ImageResolver {
    private static final String TAG = NotificationInlineImageResolver.class.getSimpleName();
    private final Context mContext;
    private final ImageCache mImageCache;
    @VisibleForTesting
    protected int mMaxImageHeight;
    @VisibleForTesting
    protected int mMaxImageWidth;
    private Set<Uri> mWantedUriSet;

    /* access modifiers changed from: package-private */
    public interface ImageCache {
        Drawable get(Uri uri);

        boolean hasEntry(Uri uri);

        void preload(Uri uri);

        void purge();

        void setImageResolver(NotificationInlineImageResolver notificationInlineImageResolver);
    }

    public NotificationInlineImageResolver(Context context, ImageCache imageCache) {
        this.mContext = context.getApplicationContext();
        this.mImageCache = imageCache;
        if (imageCache != null) {
            imageCache.setImageResolver(this);
        }
        updateMaxImageSizes();
    }

    public boolean hasCache() {
        return this.mImageCache != null && !ActivityManager.isLowRamDeviceStatic();
    }

    private boolean isLowRam() {
        return ActivityManager.isLowRamDeviceStatic();
    }

    public void updateMaxImageSizes() {
        this.mMaxImageWidth = getMaxImageWidth();
        this.mMaxImageHeight = getMaxImageHeight();
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public int getMaxImageWidth() {
        return this.mContext.getResources().getDimensionPixelSize(isLowRam() ? 17105362 : 17105361);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public int getMaxImageHeight() {
        return this.mContext.getResources().getDimensionPixelSize(isLowRam() ? 17105360 : 17105359);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public BitmapDrawable resolveImageInternal(Uri uri) throws IOException {
        return (BitmapDrawable) LocalImageResolver.resolveImage(uri, this.mContext);
    }

    /* access modifiers changed from: package-private */
    public Drawable resolveImage(Uri uri) throws IOException {
        BitmapDrawable resolveImageInternal = resolveImageInternal(uri);
        if (resolveImageInternal == null || resolveImageInternal.getBitmap() == null) {
            throw new IOException("resolveImageInternal returned null for uri: " + uri);
        }
        resolveImageInternal.setBitmap(Icon.scaleDownIfNecessary(resolveImageInternal.getBitmap(), this.mMaxImageWidth, this.mMaxImageHeight));
        return resolveImageInternal;
    }

    public Drawable loadImage(Uri uri) {
        try {
            if (!hasCache()) {
                return resolveImage(uri);
            }
            if (!this.mImageCache.hasEntry(uri)) {
                this.mImageCache.preload(uri);
            }
            return this.mImageCache.get(uri);
        } catch (IOException | SecurityException e) {
            String str = TAG;
            Log.d(str, "loadImage: Can't load image from " + uri, e);
            return null;
        }
    }

    public void preloadImages(Notification notification) {
        if (hasCache()) {
            retrieveWantedUriSet(notification);
            getWantedUriSet().forEach(new Consumer() {
                /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotificationInlineImageResolver$9tt2CqLsWBYt2coRCrkS9VmF2EU */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    NotificationInlineImageResolver.this.lambda$preloadImages$0$NotificationInlineImageResolver((Uri) obj);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$preloadImages$0 */
    public /* synthetic */ void lambda$preloadImages$0$NotificationInlineImageResolver(Uri uri) {
        if (!this.mImageCache.hasEntry(uri)) {
            this.mImageCache.preload(uri);
        }
    }

    public void purgeCache() {
        if (hasCache()) {
            this.mImageCache.purge();
        }
    }

    private void retrieveWantedUriSet(Notification notification) {
        HashSet hashSet = new HashSet();
        Bundle bundle = notification.extras;
        if (bundle != null) {
            Parcelable[] parcelableArray = bundle.getParcelableArray("android.messages");
            List<Notification.MessagingStyle.Message> list = null;
            List<Notification.MessagingStyle.Message> messagesFromBundleArray = parcelableArray == null ? null : Notification.MessagingStyle.Message.getMessagesFromBundleArray(parcelableArray);
            if (messagesFromBundleArray != null) {
                for (Notification.MessagingStyle.Message message : messagesFromBundleArray) {
                    if (MessagingMessage.hasImage(message)) {
                        hashSet.add(message.getDataUri());
                    }
                }
            }
            Parcelable[] parcelableArray2 = bundle.getParcelableArray("android.messages.historic");
            if (parcelableArray2 != null) {
                list = Notification.MessagingStyle.Message.getMessagesFromBundleArray(parcelableArray2);
            }
            if (list != null) {
                for (Notification.MessagingStyle.Message message2 : list) {
                    if (MessagingMessage.hasImage(message2)) {
                        hashSet.add(message2.getDataUri());
                    }
                }
            }
            this.mWantedUriSet = hashSet;
        }
    }

    /* access modifiers changed from: package-private */
    public Set<Uri> getWantedUriSet() {
        return this.mWantedUriSet;
    }
}
