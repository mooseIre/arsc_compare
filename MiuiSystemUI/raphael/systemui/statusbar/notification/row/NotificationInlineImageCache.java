package com.android.systemui.statusbar.notification.row;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import com.android.systemui.statusbar.notification.row.NotificationInlineImageResolver;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

public class NotificationInlineImageCache implements NotificationInlineImageResolver.ImageCache {
    private static final String TAG = "NotificationInlineImageCache";
    private final ConcurrentHashMap<Uri, PreloadImageTask> mCache = new ConcurrentHashMap<>();
    private NotificationInlineImageResolver mResolver;

    @Override // com.android.systemui.statusbar.notification.row.NotificationInlineImageResolver.ImageCache
    public void setImageResolver(NotificationInlineImageResolver notificationInlineImageResolver) {
        this.mResolver = notificationInlineImageResolver;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationInlineImageResolver.ImageCache
    public boolean hasEntry(Uri uri) {
        return this.mCache.containsKey(uri);
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationInlineImageResolver.ImageCache
    public void preload(Uri uri) {
        PreloadImageTask preloadImageTask = new PreloadImageTask(this.mResolver);
        preloadImageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, uri);
        this.mCache.put(uri, preloadImageTask);
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationInlineImageResolver.ImageCache
    public Drawable get(Uri uri) {
        try {
            return (Drawable) this.mCache.get(uri).get();
        } catch (InterruptedException | ExecutionException unused) {
            String str = TAG;
            Log.d(str, "get: Failed get image from " + uri);
            return null;
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationInlineImageResolver.ImageCache
    public void purge() {
        this.mCache.entrySet().removeIf(new Predicate(this.mResolver.getWantedUriSet()) {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotificationInlineImageCache$W1d4bA0jU1G2gSKuFNWjVLFgYyA */
            public final /* synthetic */ Set f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return NotificationInlineImageCache.lambda$purge$0(this.f$0, (Map.Entry) obj);
            }
        });
    }

    static /* synthetic */ boolean lambda$purge$0(Set set, Map.Entry entry) {
        return !set.contains(entry.getKey());
    }

    private static class PreloadImageTask extends AsyncTask<Uri, Void, Drawable> {
        private final NotificationInlineImageResolver mResolver;

        PreloadImageTask(NotificationInlineImageResolver notificationInlineImageResolver) {
            this.mResolver = notificationInlineImageResolver;
        }

        /* access modifiers changed from: protected */
        public Drawable doInBackground(Uri... uriArr) {
            Uri uri = uriArr[0];
            try {
                return this.mResolver.resolveImage(uri);
            } catch (IOException | SecurityException e) {
                String str = NotificationInlineImageCache.TAG;
                Log.d(str, "PreloadImageTask: Resolve failed from " + uri, e);
                return null;
            }
        }
    }
}
