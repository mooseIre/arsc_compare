package com.android.systemui.tuner;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.internal.util.ArrayUtils;
import com.android.systemui.DejankUtils;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.leak.LeakDetector;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class TunerServiceImpl extends TunerService {
    private static final String[] RESET_BLACKLIST = {"sysui_qs_tiles", "doze_always_on", "qs_media_resumption"};
    private ContentResolver mContentResolver;
    private final Context mContext;
    private int mCurrentUser;
    private final LeakDetector mLeakDetector;
    private final ArrayMap<Uri, String> mListeningUris = new ArrayMap<>();
    private final Observer mObserver = new Observer();
    private final ConcurrentHashMap<String, Set<TunerService.Tunable>> mTunableLookup = new ConcurrentHashMap<>();
    private final HashSet<TunerService.Tunable> mTunables;
    private CurrentUserTracker mUserTracker;

    public TunerServiceImpl(Context context, Handler handler, LeakDetector leakDetector, BroadcastDispatcher broadcastDispatcher) {
        this.mTunables = LeakDetector.ENABLED ? new HashSet<>() : null;
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
        this.mLeakDetector = leakDetector;
        for (UserInfo userInfo : UserManager.get(this.mContext).getUsers()) {
            this.mCurrentUser = userInfo.getUserHandle().getIdentifier();
            if (getValue("sysui_tuner_version", 0) != 4) {
                upgradeTuner(getValue("sysui_tuner_version", 0), 4, handler);
            }
        }
        this.mCurrentUser = ActivityManager.getCurrentUser();
        AnonymousClass1 r4 = new CurrentUserTracker(broadcastDispatcher) {
            /* class com.android.systemui.tuner.TunerServiceImpl.AnonymousClass1 */

            @Override // com.android.systemui.settings.CurrentUserTracker
            public void onUserSwitched(int i) {
                TunerServiceImpl.this.mCurrentUser = i;
                TunerServiceImpl.this.reloadAll();
                TunerServiceImpl.this.reregisterAll();
            }
        };
        this.mUserTracker = r4;
        r4.startTracking();
    }

    private void upgradeTuner(int i, int i2, Handler handler) {
        String value;
        if (i < 1 && (value = getValue("icon_blacklist")) != null) {
            ArraySet<String> iconBlacklist = StatusBarIconController.getIconBlacklist(this.mContext, value);
            iconBlacklist.add("rotate");
            iconBlacklist.add("headset");
            Settings.Secure.putStringForUser(this.mContentResolver, "icon_blacklist", TextUtils.join(",", iconBlacklist), this.mCurrentUser);
        }
        if (i < 2) {
            TunerService.setTunerEnabled(this.mContext, false);
        }
        if (i < 4) {
            handler.postDelayed(new Runnable(this.mCurrentUser) {
                /* class com.android.systemui.tuner.$$Lambda$TunerServiceImpl$SZ83wU1GcnmYXjZCH1hfw7pCVvY */
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    TunerServiceImpl.this.lambda$upgradeTuner$0$TunerServiceImpl(this.f$1);
                }
            }, 5000);
        }
        setValue("sysui_tuner_version", i2);
    }

    @Override // com.android.systemui.tuner.TunerService
    public String getValue(String str) {
        return Settings.Secure.getStringForUser(this.mContentResolver, str, this.mCurrentUser);
    }

    @Override // com.android.systemui.tuner.TunerService
    public void setValue(String str, String str2) {
        Settings.Secure.putStringForUser(this.mContentResolver, str, str2, this.mCurrentUser);
    }

    @Override // com.android.systemui.tuner.TunerService
    public int getValue(String str, int i) {
        return Settings.Secure.getIntForUser(this.mContentResolver, str, i, this.mCurrentUser);
    }

    @Override // com.android.systemui.tuner.TunerService
    public void setValue(String str, int i) {
        Settings.Secure.putIntForUser(this.mContentResolver, str, i, this.mCurrentUser);
    }

    @Override // com.android.systemui.tuner.TunerService
    public void addTunable(TunerService.Tunable tunable, String... strArr) {
        for (String str : strArr) {
            addTunable(tunable, str);
        }
    }

    private void addTunable(TunerService.Tunable tunable, String str) {
        if (!this.mTunableLookup.containsKey(str)) {
            this.mTunableLookup.put(str, new ArraySet());
        }
        this.mTunableLookup.get(str).add(tunable);
        if (LeakDetector.ENABLED) {
            this.mTunables.add(tunable);
            this.mLeakDetector.trackCollection(this.mTunables, "TunerService.mTunables");
        }
        Uri uriFor = Settings.Secure.getUriFor(str);
        if (!this.mListeningUris.containsKey(uriFor)) {
            this.mListeningUris.put(uriFor, str);
            this.mContentResolver.registerContentObserver(uriFor, false, this.mObserver, this.mCurrentUser);
        }
        tunable.onTuningChanged(str, (String) DejankUtils.whitelistIpcs(new Supplier(str) {
            /* class com.android.systemui.tuner.$$Lambda$TunerServiceImpl$rxKdnA_ESs9ir91j7kkfizLlu6E */
            public final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Supplier
            public final Object get() {
                return TunerServiceImpl.this.lambda$addTunable$1$TunerServiceImpl(this.f$1);
            }
        }));
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$addTunable$1 */
    public /* synthetic */ String lambda$addTunable$1$TunerServiceImpl(String str) {
        return Settings.Secure.getStringForUser(this.mContentResolver, str, this.mCurrentUser);
    }

    @Override // com.android.systemui.tuner.TunerService
    public void removeTunable(TunerService.Tunable tunable) {
        for (Set<TunerService.Tunable> set : this.mTunableLookup.values()) {
            set.remove(tunable);
        }
        if (LeakDetector.ENABLED) {
            this.mTunables.remove(tunable);
        }
    }

    /* access modifiers changed from: protected */
    public void reregisterAll() {
        if (this.mListeningUris.size() != 0) {
            this.mContentResolver.unregisterContentObserver(this.mObserver);
            for (Uri uri : this.mListeningUris.keySet()) {
                this.mContentResolver.registerContentObserver(uri, false, this.mObserver, this.mCurrentUser);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reloadSetting(Uri uri) {
        String str = this.mListeningUris.get(uri);
        Set<TunerService.Tunable> set = this.mTunableLookup.get(str);
        if (set != null) {
            String stringForUser = Settings.Secure.getStringForUser(this.mContentResolver, str, this.mCurrentUser);
            for (TunerService.Tunable tunable : set) {
                tunable.onTuningChanged(str, stringForUser);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reloadAll() {
        for (String str : this.mTunableLookup.keySet()) {
            String stringForUser = Settings.Secure.getStringForUser(this.mContentResolver, str, this.mCurrentUser);
            for (TunerService.Tunable tunable : this.mTunableLookup.get(str)) {
                tunable.onTuningChanged(str, stringForUser);
            }
        }
    }

    @Override // com.android.systemui.tuner.TunerService
    public void clearAll() {
        lambda$upgradeTuner$0(this.mCurrentUser);
    }

    /* renamed from: clearAllFromUser */
    public void lambda$upgradeTuner$0(int i) {
        Settings.Global.putString(this.mContentResolver, "sysui_demo_allowed", null);
        Intent intent = new Intent("com.android.systemui.demo");
        intent.putExtra("command", "exit");
        this.mContext.sendBroadcast(intent);
        for (String str : this.mTunableLookup.keySet()) {
            if (!ArrayUtils.contains(RESET_BLACKLIST, str)) {
                Settings.Secure.putStringForUser(this.mContentResolver, str, null, i);
            }
        }
    }

    /* access modifiers changed from: private */
    public class Observer extends ContentObserver {
        public Observer() {
            super(new Handler(Looper.getMainLooper()));
        }

        public void onChange(boolean z, Collection<Uri> collection, int i, int i2) {
            if (i2 == ActivityManager.getCurrentUser()) {
                for (Uri uri : collection) {
                    TunerServiceImpl.this.reloadSetting(uri);
                }
            }
        }
    }
}
