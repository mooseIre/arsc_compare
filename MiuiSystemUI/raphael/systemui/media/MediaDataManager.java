package com.android.systemui.media;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import androidx.palette.graphics.Palette;
import com.android.internal.graphics.ColorUtils;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dumpable;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.statusbar.notification.MediaNotificationProcessor;
import com.android.systemui.statusbar.notification.row.HybridGroupManager;
import com.android.systemui.util.Assert;
import com.android.systemui.util.Utils;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import kotlin.Unit;
import kotlin.collections.ArraysKt___ArraysKt;
import kotlin.collections.CollectionsKt__CollectionsJVMKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref$ObjectRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaDataManager.kt */
public final class MediaDataManager implements Dumpable {
    private final MediaDataManager$appChangeReceiver$1 appChangeReceiver;
    private final Executor backgroundExecutor;
    private final BroadcastDispatcher broadcastDispatcher;
    private final Context context;
    private final Executor foregroundExecutor;
    private final Set<Listener> listeners;
    private final MediaControllerFactory mediaControllerFactory;
    private final LinkedHashMap<String, MediaData> mediaEntries;
    private boolean useMediaResumption;
    private final boolean useQsMediaPlayer;

    /* compiled from: MediaDataManager.kt */
    public interface Listener {

        /* compiled from: MediaDataManager.kt */
        public static final class DefaultImpls {
            public static void onMediaDataRemoved(Listener listener, @NotNull String str) {
                Intrinsics.checkParameterIsNotNull(str, "key");
            }
        }

        void onMediaDataLoaded(@NotNull String str, @Nullable String str2, @NotNull MediaData mediaData);

        void onMediaDataRemoved(@NotNull String str);
    }

    public MediaDataManager(@NotNull Context context2, @NotNull Executor executor, @NotNull Executor executor2, @NotNull MediaControllerFactory mediaControllerFactory2, @NotNull BroadcastDispatcher broadcastDispatcher2, @NotNull DumpManager dumpManager, @NotNull MediaTimeoutListener mediaTimeoutListener, @NotNull MediaResumeListener mediaResumeListener, boolean z, boolean z2) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Intrinsics.checkParameterIsNotNull(executor, "backgroundExecutor");
        Intrinsics.checkParameterIsNotNull(executor2, "foregroundExecutor");
        Intrinsics.checkParameterIsNotNull(mediaControllerFactory2, "mediaControllerFactory");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher2, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        Intrinsics.checkParameterIsNotNull(mediaTimeoutListener, "mediaTimeoutListener");
        Intrinsics.checkParameterIsNotNull(mediaResumeListener, "mediaResumeListener");
        this.context = context2;
        this.backgroundExecutor = executor;
        this.foregroundExecutor = executor2;
        this.mediaControllerFactory = mediaControllerFactory2;
        this.broadcastDispatcher = broadcastDispatcher2;
        this.useMediaResumption = z;
        this.useQsMediaPlayer = z2;
        this.listeners = new LinkedHashSet();
        this.mediaEntries = new LinkedHashMap<>();
        this.appChangeReceiver = new MediaDataManager$appChangeReceiver$1(this);
        dumpManager.registerDumpable("MediaDataManager", this);
        mediaTimeoutListener.setTimeoutCallback(new Function2<String, Boolean, Unit>(this) {
            /* class com.android.systemui.media.MediaDataManager.AnonymousClass1 */
            final /* synthetic */ MediaDataManager this$0;

            {
                this.this$0 = r1;
            }

            /* Return type fixed from 'java.lang.Object' to match base method */
            /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object, java.lang.Object] */
            @Override // kotlin.jvm.functions.Function2
            public /* bridge */ /* synthetic */ Unit invoke(String str, Boolean bool) {
                invoke(str, bool.booleanValue());
                return Unit.INSTANCE;
            }

            public final void invoke(@NotNull String str, boolean z) {
                Intrinsics.checkParameterIsNotNull(str, "token");
                this.this$0.setTimedOut$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(str, z);
            }
        });
        addListener(mediaTimeoutListener);
        mediaResumeListener.setManager(this);
        addListener(mediaResumeListener);
        IntentFilter intentFilter = new IntentFilter("android.intent.action.PACKAGES_SUSPENDED");
        BroadcastDispatcher broadcastDispatcher3 = this.broadcastDispatcher;
        MediaDataManager$appChangeReceiver$1 mediaDataManager$appChangeReceiver$1 = this.appChangeReceiver;
        UserHandle userHandle = UserHandle.ALL;
        Intrinsics.checkExpressionValueIsNotNull(userHandle, "UserHandle.ALL");
        broadcastDispatcher3.registerReceiver(mediaDataManager$appChangeReceiver$1, intentFilter, null, userHandle);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter2.addAction("android.intent.action.PACKAGE_RESTARTED");
        intentFilter2.addDataScheme("package");
        this.context.registerReceiver(this.appChangeReceiver, intentFilter2);
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public MediaDataManager(@NotNull Context context2, @NotNull Executor executor, @NotNull Executor executor2, @NotNull MediaControllerFactory mediaControllerFactory2, @NotNull DumpManager dumpManager, @NotNull BroadcastDispatcher broadcastDispatcher2, @NotNull MediaTimeoutListener mediaTimeoutListener, @NotNull MediaResumeListener mediaResumeListener) {
        this(context2, executor, executor2, mediaControllerFactory2, broadcastDispatcher2, dumpManager, mediaTimeoutListener, mediaResumeListener, Utils.useMediaResumption(context2), Utils.useQsMediaPlayer(context2));
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Intrinsics.checkParameterIsNotNull(executor, "backgroundExecutor");
        Intrinsics.checkParameterIsNotNull(executor2, "foregroundExecutor");
        Intrinsics.checkParameterIsNotNull(mediaControllerFactory2, "mediaControllerFactory");
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher2, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(mediaTimeoutListener, "mediaTimeoutListener");
        Intrinsics.checkParameterIsNotNull(mediaResumeListener, "mediaResumeListener");
    }

    public final void onNotificationAdded(@NotNull String str, @NotNull StatusBarNotification statusBarNotification) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        Intrinsics.checkParameterIsNotNull(statusBarNotification, "sbn");
        if (!this.useQsMediaPlayer || !MediaDataManagerKt.isMediaNotification(statusBarNotification)) {
            onNotificationRemoved(str);
            return;
        }
        Assert.isMainThread();
        String packageName = statusBarNotification.getPackageName();
        Intrinsics.checkExpressionValueIsNotNull(packageName, "sbn.packageName");
        String findExistingEntry = findExistingEntry(str, packageName);
        if (findExistingEntry == null) {
            MediaData access$getLOADING$p = MediaDataManagerKt.access$getLOADING$p();
            String packageName2 = statusBarNotification.getPackageName();
            Intrinsics.checkExpressionValueIsNotNull(packageName2, "sbn.packageName");
            this.mediaEntries.put(str, MediaData.copy$default(access$getLOADING$p, 0, false, 0, null, null, null, null, null, null, null, packageName2, null, null, null, false, null, false, null, false, 523263, null));
        } else if (!Intrinsics.areEqual(findExistingEntry, str)) {
            MediaData remove = this.mediaEntries.remove(findExistingEntry);
            if (remove != null) {
                Intrinsics.checkExpressionValueIsNotNull(remove, "mediaEntries.remove(oldKey)!!");
                this.mediaEntries.put(str, remove);
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        }
        loadMediaData(str, statusBarNotification, findExistingEntry);
    }

    /* access modifiers changed from: private */
    public final void removeAllForPackage(String str) {
        Assert.isMainThread();
        Set<Listener> set = CollectionsKt___CollectionsKt.toSet(this.listeners);
        LinkedHashMap<String, MediaData> linkedHashMap = this.mediaEntries;
        LinkedHashMap linkedHashMap2 = new LinkedHashMap();
        for (Map.Entry<String, MediaData> entry : linkedHashMap.entrySet()) {
            if (Intrinsics.areEqual(entry.getValue().getPackageName(), str)) {
                linkedHashMap2.put(entry.getKey(), entry.getValue());
            }
        }
        for (Map.Entry entry2 : linkedHashMap2.entrySet()) {
            this.mediaEntries.remove(entry2.getKey());
            for (Listener listener : set) {
                listener.onMediaDataRemoved((String) entry2.getKey());
            }
        }
    }

    public final void setResumeAction(@NotNull String str, @Nullable Runnable runnable) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        MediaData mediaData = this.mediaEntries.get(str);
        if (mediaData != null) {
            mediaData.setResumeAction(runnable);
            mediaData.setHasCheckedForResume(true);
        }
    }

    public final void addResumptionControls(int i, @NotNull MediaDescription mediaDescription, @NotNull Runnable runnable, @NotNull MediaSession.Token token, @NotNull String str, @NotNull PendingIntent pendingIntent, @NotNull String str2) {
        Intrinsics.checkParameterIsNotNull(mediaDescription, "desc");
        Intrinsics.checkParameterIsNotNull(runnable, "action");
        Intrinsics.checkParameterIsNotNull(token, "token");
        Intrinsics.checkParameterIsNotNull(str, "appName");
        Intrinsics.checkParameterIsNotNull(pendingIntent, "appIntent");
        Intrinsics.checkParameterIsNotNull(str2, "packageName");
        if (!this.mediaEntries.containsKey(str2)) {
            this.mediaEntries.put(str2, MediaData.copy$default(MediaDataManagerKt.access$getLOADING$p(), 0, false, 0, null, null, null, null, null, null, null, str2, null, null, null, false, runnable, false, null, true, 228351, null));
        }
        this.backgroundExecutor.execute(new MediaDataManager$addResumptionControls$1(this, i, mediaDescription, runnable, token, str, pendingIntent, str2));
    }

    private final String findExistingEntry(String str, String str2) {
        if (this.mediaEntries.containsKey(str)) {
            return str;
        }
        if (this.mediaEntries.containsKey(str2)) {
            return str2;
        }
        return null;
    }

    private final void loadMediaData(String str, StatusBarNotification statusBarNotification, String str2) {
        this.backgroundExecutor.execute(new MediaDataManager$loadMediaData$1(this, str, statusBarNotification, str2));
    }

    public final boolean addListener(@NotNull Listener listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        return this.listeners.add(listener);
    }

    public final void setTimedOut$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(@NotNull String str, boolean z) {
        Intrinsics.checkParameterIsNotNull(str, "token");
        MediaData mediaData = this.mediaEntries.get(str);
        if (mediaData != null && mediaData.getActive() != (!z)) {
            mediaData.setActive(!z);
            Intrinsics.checkExpressionValueIsNotNull(mediaData, "it");
            onMediaDataLoaded(str, str, mediaData);
        }
    }

    /* access modifiers changed from: private */
    public final void loadMediaDataInBgForResumption(int i, MediaDescription mediaDescription, Runnable runnable, MediaSession.Token token, String str, PendingIntent pendingIntent, String str2) {
        if (TextUtils.isEmpty(mediaDescription.getTitle())) {
            Log.e("MediaDataManager", "Description incomplete");
            this.mediaEntries.remove(str2);
            return;
        }
        Log.d("MediaDataManager", "adding track for " + i + " from browser: " + mediaDescription);
        Bitmap iconBitmap = mediaDescription.getIconBitmap();
        Icon icon = null;
        if (iconBitmap == null && mediaDescription.getIconUri() != null) {
            Uri iconUri = mediaDescription.getIconUri();
            if (iconUri != null) {
                iconBitmap = loadBitmapFromUri(iconUri);
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        }
        if (iconBitmap != null) {
            icon = Icon.createWithBitmap(iconBitmap);
        }
        this.foregroundExecutor.execute(new MediaDataManager$loadMediaDataInBgForResumption$1(this, str2, i, iconBitmap != null ? computeBackgroundColor(iconBitmap) : -12303292, str, mediaDescription, icon, getResumeMediaAction(runnable), token, pendingIntent, runnable));
    }

    /* access modifiers changed from: private */
    public final void loadMediaDataInBg(String str, StatusBarNotification statusBarNotification, String str2) {
        Icon icon;
        List list;
        Context context2;
        Notification notification;
        Notification.Action[] actionArr;
        MediaSession.Token token = (MediaSession.Token) statusBarNotification.getNotification().extras.getParcelable("android.mediaSession");
        MediaController create = this.mediaControllerFactory.create(token);
        Intrinsics.checkExpressionValueIsNotNull(create, "mediaControllerFactory.create(token)");
        MediaMetadata metadata = create.getMetadata();
        if (metadata != null) {
            Notification notification2 = statusBarNotification.getNotification();
            Intrinsics.checkExpressionValueIsNotNull(notification2, "sbn.notification");
            Bitmap bitmap = metadata.getBitmap("android.media.metadata.ART");
            if (bitmap == null) {
                bitmap = metadata.getBitmap("android.media.metadata.ALBUM_ART");
            }
            if (bitmap == null) {
                bitmap = loadBitmapFromUri(metadata);
            }
            if (bitmap == null) {
                icon = notification2.getLargeIcon();
            } else {
                icon = Icon.createWithBitmap(bitmap);
            }
            int i = 0;
            if (icon != null && bitmap == null) {
                if (icon.getType() == 1 || icon.getType() == 5) {
                    bitmap = icon.getBitmap();
                } else {
                    Drawable loadDrawable = icon.loadDrawable(this.context);
                    Intrinsics.checkExpressionValueIsNotNull(loadDrawable, "artWorkIcon.loadDrawable(context)");
                    Bitmap createBitmap = Bitmap.createBitmap(loadDrawable.getIntrinsicWidth(), loadDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(createBitmap);
                    loadDrawable.setBounds(0, 0, loadDrawable.getIntrinsicWidth(), loadDrawable.getIntrinsicHeight());
                    loadDrawable.draw(canvas);
                    bitmap = createBitmap;
                }
            }
            int computeBackgroundColor = computeBackgroundColor(bitmap);
            String loadHeaderAppName = Notification.Builder.recoverBuilder(this.context, notification2).loadHeaderAppName();
            Notification notification3 = statusBarNotification.getNotification();
            Intrinsics.checkExpressionValueIsNotNull(notification3, "sbn.notification");
            Drawable loadDrawable2 = notification3.getSmallIcon().loadDrawable(this.context);
            Intrinsics.checkExpressionValueIsNotNull(loadDrawable2, "sbn.notification.smallIcon.loadDrawable(context)");
            Ref$ObjectRef ref$ObjectRef = new Ref$ObjectRef();
            T t = (T) metadata.getString("android.media.metadata.DISPLAY_TITLE");
            ref$ObjectRef.element = t;
            if (t == null) {
                ref$ObjectRef.element = (T) metadata.getString("android.media.metadata.TITLE");
            }
            if (ref$ObjectRef.element == null) {
                ref$ObjectRef.element = (T) HybridGroupManager.resolveTitle(notification2);
            }
            Ref$ObjectRef ref$ObjectRef2 = new Ref$ObjectRef();
            T t2 = (T) metadata.getString("android.media.metadata.ARTIST");
            ref$ObjectRef2.element = t2;
            if (t2 == null) {
                ref$ObjectRef2.element = (T) HybridGroupManager.resolveText(notification2);
            }
            ArrayList arrayList = new ArrayList();
            Notification.Action[] actionArr2 = notification2.actions;
            int[] intArray = notification2.extras.getIntArray("android.compactActions");
            if (intArray == null || (list = ArraysKt___ArraysKt.toMutableList(intArray)) == null) {
                list = new ArrayList();
            }
            Context packageContext = statusBarNotification.getPackageContext(this.context);
            Intrinsics.checkExpressionValueIsNotNull(packageContext, "sbn.getPackageContext(context)");
            if (actionArr2 != null) {
                int length = actionArr2.length;
                while (i < length) {
                    Notification.Action action = actionArr2[i];
                    if (action.getIcon() == null) {
                        actionArr = actionArr2;
                        Log.i("MediaDataManager", "No icon for action " + i + ' ' + action.title);
                        list.remove(Integer.valueOf(i));
                        context2 = packageContext;
                        notification = notification2;
                    } else {
                        actionArr = actionArr2;
                        notification = notification2;
                        context2 = packageContext;
                        arrayList.add(new MediaAction(action.getIcon().loadDrawable(packageContext), action.actionIntent != null ? new MediaDataManager$loadMediaDataInBg$runnable$1(action) : null, action.title, action));
                    }
                    i++;
                    length = length;
                    actionArr2 = actionArr;
                    notification2 = notification;
                    packageContext = context2;
                }
            }
            this.foregroundExecutor.execute(new MediaDataManager$loadMediaDataInBg$1(this, str, str2, statusBarNotification, computeBackgroundColor, loadHeaderAppName, loadDrawable2, ref$ObjectRef2, ref$ObjectRef, icon, arrayList, list, token, notification2));
        }
    }

    private final Bitmap loadBitmapFromUri(MediaMetadata mediaMetadata) {
        String[] access$getART_URIS$p = MediaDataManagerKt.access$getART_URIS$p();
        for (String str : access$getART_URIS$p) {
            String string = mediaMetadata.getString(str);
            if (!TextUtils.isEmpty(string)) {
                Uri parse = Uri.parse(string);
                Intrinsics.checkExpressionValueIsNotNull(parse, "Uri.parse(uriString)");
                Bitmap loadBitmapFromUri = loadBitmapFromUri(parse);
                if (loadBitmapFromUri != null) {
                    Log.d("MediaDataManager", "loaded art from " + str);
                    return loadBitmapFromUri;
                }
            }
        }
        return null;
    }

    private final Bitmap loadBitmapFromUri(Uri uri) {
        if (uri.getScheme() == null) {
            return null;
        }
        if (!uri.getScheme().equals("content") && !uri.getScheme().equals("android.resource") && !uri.getScheme().equals("file")) {
            return null;
        }
        try {
            return ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.context.getContentResolver(), uri), MediaDataManager$loadBitmapFromUri$1.INSTANCE);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private final int computeBackgroundColor(Bitmap bitmap) {
        int i;
        if (bitmap != null) {
            Palette generate = MediaNotificationProcessor.generateArtworkPaletteBuilder(bitmap).generate();
            Intrinsics.checkExpressionValueIsNotNull(generate, "MediaNotificationProcess…              .generate()");
            Palette.Swatch findBackgroundSwatch = MediaNotificationProcessor.findBackgroundSwatch(generate);
            Intrinsics.checkExpressionValueIsNotNull(findBackgroundSwatch, "swatch");
            i = findBackgroundSwatch.getRgb();
        } else {
            i = -1;
        }
        float[] fArr = {0.0f, 0.0f, 0.0f};
        ColorUtils.colorToHSL(i, fArr);
        float f = fArr[2];
        if (f < 0.05f || f > 0.95f) {
            fArr[1] = 0.0f;
        }
        fArr[1] = fArr[1] * 0.8f;
        fArr[2] = 0.25f;
        return ColorUtils.HSLToColor(fArr);
    }

    private final MediaAction getResumeMediaAction(Runnable runnable) {
        return new MediaAction(this.context.getDrawable(C0013R$drawable.lb_ic_play), runnable, this.context.getString(C0021R$string.controls_media_resume), null, 8, null);
    }

    public final void onMediaDataLoaded(@NotNull String str, @Nullable String str2, @NotNull MediaData mediaData) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        Intrinsics.checkParameterIsNotNull(mediaData, "data");
        Assert.isMainThread();
        if (this.mediaEntries.containsKey(str)) {
            this.mediaEntries.put(str, mediaData);
            for (Listener listener : CollectionsKt___CollectionsKt.toSet(this.listeners)) {
                listener.onMediaDataLoaded(str, str2, mediaData);
            }
        }
    }

    public final void onNotificationRemoved(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        Assert.isMainThread();
        MediaData remove = this.mediaEntries.remove(str);
        if (this.useMediaResumption) {
            String str2 = null;
            if ((remove != null ? remove.getResumeAction() : null) != null) {
                Log.d("MediaDataManager", "Not removing " + str + " because resumable");
                Runnable resumeAction = remove.getResumeAction();
                if (resumeAction != null) {
                    boolean z = false;
                    MediaData copy$default = MediaData.copy$default(remove, 0, false, 0, null, null, null, null, null, CollectionsKt__CollectionsJVMKt.listOf(getResumeMediaAction(resumeAction)), CollectionsKt__CollectionsJVMKt.listOf(0), null, null, null, null, false, null, true, null, false, 439551, null);
                    if (remove != null) {
                        str2 = remove.getPackageName();
                    }
                    if (this.mediaEntries.put(str2, copy$default) == null) {
                        z = true;
                    }
                    Set<Listener> set = CollectionsKt___CollectionsKt.toSet(this.listeners);
                    if (z) {
                        for (Listener listener : set) {
                            listener.onMediaDataLoaded(str2, str, copy$default);
                        }
                        return;
                    }
                    for (Listener listener2 : set) {
                        listener2.onMediaDataRemoved(str);
                    }
                    for (Listener listener3 : set) {
                        listener3.onMediaDataLoaded(str2, str2, copy$default);
                    }
                    return;
                }
                Intrinsics.throwNpe();
                throw null;
            }
        }
        if (remove != null) {
            for (Listener listener4 : CollectionsKt___CollectionsKt.toSet(this.listeners)) {
                listener4.onMediaDataRemoved(str);
            }
        }
    }

    public final void setMediaResumptionEnabled(boolean z) {
        if (this.useMediaResumption != z) {
            this.useMediaResumption = z;
            if (!z) {
                Set<Listener> set = CollectionsKt___CollectionsKt.toSet(this.listeners);
                LinkedHashMap<String, MediaData> linkedHashMap = this.mediaEntries;
                LinkedHashMap linkedHashMap2 = new LinkedHashMap();
                for (Map.Entry<String, MediaData> entry : linkedHashMap.entrySet()) {
                    if (!entry.getValue().getActive()) {
                        linkedHashMap2.put(entry.getKey(), entry.getValue());
                    }
                }
                for (Map.Entry entry2 : linkedHashMap2.entrySet()) {
                    this.mediaEntries.remove(entry2.getKey());
                    for (Listener listener : set) {
                        listener.onMediaDataRemoved((String) entry2.getKey());
                    }
                }
            }
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(@NotNull FileDescriptor fileDescriptor, @NotNull PrintWriter printWriter, @NotNull String[] strArr) {
        Intrinsics.checkParameterIsNotNull(fileDescriptor, "fd");
        Intrinsics.checkParameterIsNotNull(printWriter, "pw");
        Intrinsics.checkParameterIsNotNull(strArr, "args");
        printWriter.println("listeners: " + this.listeners);
        printWriter.println("mediaEntries: " + this.mediaEntries);
        printWriter.println("useMediaResumption: " + this.useMediaResumption);
    }
}
