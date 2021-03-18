package com.android.systemui.volume;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.media.IAudioService;
import android.media.IVolumeController;
import android.media.VolumePolicy;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.service.notification.Condition;
import android.service.notification.ZenModeConfig;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import android.view.accessibility.AccessibilityManager;
import androidx.lifecycle.Observer;
import com.android.internal.annotations.GuardedBy;
import com.android.settingslib.volume.MediaSessions;
import com.android.settingslib.volume.Util;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dumpable;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.plugins.VolumeDialogController;
import com.android.systemui.qs.tiles.DndTile;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.util.RingerModeLiveData;
import com.android.systemui.util.RingerModeTracker;
import com.android.systemui.volume.VolumeDialogControllerImpl;
import com.miui.systemui.volume.VolumeDialogTransformHelper;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class VolumeDialogControllerImpl implements VolumeDialogController, Dumpable {
    private static final AudioAttributes SONIFICIATION_VIBRATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(13).build();
    static final ArrayMap<Integer, Integer> STREAMS;
    private static final String TAG = Util.logTag(VolumeDialogControllerImpl.class);
    private AudioManager mAudio;
    private IAudioService mAudioService;
    protected final BroadcastDispatcher mBroadcastDispatcher;
    protected C mCallbacks = new C(this);
    private final Context mContext;
    private boolean mDestroyed;
    private final boolean mHasVibrator;
    private long mLastToggledRingerOn;
    private final MediaSessions mMediaSessions;
    protected final MediaSessionsCallbacks mMediaSessionsCallbacksW = new MediaSessionsCallbacks();
    private final NotificationManager mNoMan;
    private final NotificationManager mNotificationManager;
    private final SettingObserver mObserver;
    private final Receiver mReceiver = new Receiver();
    private final RingerModeObservers mRingerModeObservers;
    private boolean mShowA11yStream;
    private boolean mShowDndTile;
    private boolean mShowSafetyWarning;
    private boolean mShowVolumeDialog;
    private final VolumeDialogController.State mState = new VolumeDialogController.State();
    private final Optional<Lazy<StatusBar>> mStatusBarOptionalLazy;
    @GuardedBy({"this"})
    private UserActivityListener mUserActivityListener;
    private final Vibrator mVibrator;
    protected final VC mVolumeController;
    private VolumePolicy mVolumePolicy;
    private final W mWorker;
    private final HandlerThread mWorkerThread;

    public interface UserActivityListener {
        void onUserActivity();
    }

    private static boolean isLogWorthy(int i) {
        return i == 0 || i == 1 || i == 2 || i == 3 || i == 4 || i == 6;
    }

    private static boolean isRinger(int i) {
        return i == 2 || i == 5;
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public boolean isCaptionStreamOptedOut() {
        return false;
    }

    static {
        ArrayMap<Integer, Integer> arrayMap = new ArrayMap<>();
        STREAMS = arrayMap;
        arrayMap.put(4, Integer.valueOf(C0021R$string.stream_alarm));
        STREAMS.put(6, Integer.valueOf(C0021R$string.stream_bluetooth_sco));
        STREAMS.put(8, Integer.valueOf(C0021R$string.stream_dtmf));
        STREAMS.put(3, Integer.valueOf(C0021R$string.stream_music));
        STREAMS.put(10, Integer.valueOf(C0021R$string.stream_accessibility));
        STREAMS.put(5, Integer.valueOf(C0021R$string.stream_notification));
        STREAMS.put(2, Integer.valueOf(C0021R$string.stream_ring));
        STREAMS.put(1, Integer.valueOf(C0021R$string.stream_system));
        STREAMS.put(7, Integer.valueOf(C0021R$string.stream_system_enforced));
        STREAMS.put(9, Integer.valueOf(C0021R$string.stream_tts));
        STREAMS.put(0, Integer.valueOf(C0021R$string.stream_voice_call));
    }

    public VolumeDialogControllerImpl(Context context, BroadcastDispatcher broadcastDispatcher, Optional<Lazy<StatusBar>> optional, RingerModeTracker ringerModeTracker) {
        boolean z = true;
        this.mShowDndTile = true;
        this.mVolumeController = new VC();
        Context applicationContext = context.getApplicationContext();
        this.mContext = applicationContext;
        if (applicationContext.getPackageManager().hasSystemFeature("android.software.leanback")) {
            this.mStatusBarOptionalLazy = Optional.empty();
        } else {
            this.mStatusBarOptionalLazy = optional;
        }
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        Events.writeEvent(5, new Object[0]);
        HandlerThread handlerThread = new HandlerThread(VolumeDialogControllerImpl.class.getSimpleName());
        this.mWorkerThread = handlerThread;
        handlerThread.start();
        this.mWorker = new W(this.mWorkerThread.getLooper());
        this.mMediaSessions = createMediaSessions(this.mContext, this.mWorkerThread.getLooper(), this.mMediaSessionsCallbacksW);
        this.mAudio = (AudioManager) this.mContext.getSystemService("audio");
        this.mNoMan = (NotificationManager) this.mContext.getSystemService("notification");
        this.mObserver = new SettingObserver(this.mWorker);
        RingerModeObservers ringerModeObservers = new RingerModeObservers((RingerModeLiveData) ringerModeTracker.getRingerMode(), (RingerModeLiveData) ringerModeTracker.getRingerModeInternal());
        this.mRingerModeObservers = ringerModeObservers;
        ringerModeObservers.init();
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mObserver.init();
        this.mReceiver.init();
        Vibrator vibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        this.mVibrator = vibrator;
        this.mHasVibrator = (vibrator == null || !vibrator.hasVibrator()) ? false : z;
        this.mAudioService = IAudioService.Stub.asInterface(ServiceManager.getService("audio"));
        this.mVolumeController.setA11yMode(((AccessibilityManager) context.getSystemService(AccessibilityManager.class)).isAccessibilityVolumeStreamActive() ? 1 : 0);
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public AudioManager getAudioManager() {
        return this.mAudio;
    }

    public void dismiss() {
        this.mCallbacks.onDismissRequested(2);
    }

    /* access modifiers changed from: protected */
    public void setVolumeController() {
        try {
            this.mAudio.setVolumeController(this.mVolumeController);
        } catch (SecurityException e) {
            Log.w(TAG, "Unable to set the volume controller", e);
        }
    }

    /* access modifiers changed from: protected */
    public void setAudioManagerStreamVolume(int i, int i2, int i3) {
        this.mAudio.setStreamVolume(i, i2, i3);
    }

    /* access modifiers changed from: protected */
    public int getAudioManagerStreamVolume(int i) {
        return this.mAudio.getLastAudibleStreamVolume(i);
    }

    /* access modifiers changed from: protected */
    public int getAudioManagerStreamMaxVolume(int i) {
        return this.mAudio.getStreamMaxVolume(i);
    }

    /* access modifiers changed from: protected */
    public int getAudioManagerStreamMinVolume(int i) {
        return this.mAudio.getStreamMinVolumeInt(i);
    }

    public void register() {
        setVolumeController();
        setVolumePolicy(this.mVolumePolicy);
        showDndTile(this.mShowDndTile);
        try {
            this.mMediaSessions.init();
        } catch (SecurityException e) {
            Log.w(TAG, "No access to media sessions", e);
        }
    }

    public void setVolumePolicy(VolumePolicy volumePolicy) {
        this.mVolumePolicy = volumePolicy;
        if (volumePolicy != null) {
            try {
                this.mAudio.setVolumePolicy(volumePolicy);
            } catch (NoSuchMethodError unused) {
                Log.w(TAG, "No volume policy api");
            }
        }
    }

    /* access modifiers changed from: protected */
    public MediaSessions createMediaSessions(Context context, Looper looper, MediaSessions.Callbacks callbacks) {
        return new MediaSessions(context, looper, callbacks);
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println(VolumeDialogControllerImpl.class.getSimpleName() + " state:");
        printWriter.print("  mDestroyed: ");
        printWriter.println(this.mDestroyed);
        printWriter.print("  mVolumePolicy: ");
        printWriter.println(this.mVolumePolicy);
        printWriter.print("  mState: ");
        printWriter.println(this.mState.toString(4));
        printWriter.print("  mShowDndTile: ");
        printWriter.println(this.mShowDndTile);
        printWriter.print("  mHasVibrator: ");
        printWriter.println(this.mHasVibrator);
        synchronized (this.mMediaSessionsCallbacksW.mRemoteStreams) {
            printWriter.print("  mRemoteStreams: ");
            printWriter.println(this.mMediaSessionsCallbacksW.mRemoteStreams.values());
        }
        printWriter.print("  mShowA11yStream: ");
        printWriter.println(this.mShowA11yStream);
        printWriter.println();
        this.mMediaSessions.dump(printWriter);
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public void addCallback(VolumeDialogController.Callbacks callbacks, Handler handler) {
        this.mCallbacks.add(callbacks, handler);
        callbacks.onAccessibilityModeChanged(Boolean.valueOf(this.mShowA11yStream));
    }

    public void setUserActivityListener(UserActivityListener userActivityListener) {
        if (!this.mDestroyed) {
            synchronized (this) {
                this.mUserActivityListener = userActivityListener;
            }
        }
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public void removeCallback(VolumeDialogController.Callbacks callbacks) {
        this.mCallbacks.remove(callbacks);
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public void getState() {
        if (!this.mDestroyed) {
            this.mWorker.sendEmptyMessage(3);
        }
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public boolean areCaptionsEnabled() {
        return Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "odi_captions_enabled", 0, -2) == 1;
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public void setCaptionsEnabled(boolean z) {
        Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "odi_captions_enabled", z ? 1 : 0, -2);
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public void getCaptionsComponentState(boolean z) {
        if (!this.mDestroyed) {
            this.mWorker.obtainMessage(16, Boolean.valueOf(z)).sendToTarget();
        }
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public void notifyVisible(boolean z) {
        if (!this.mDestroyed) {
            this.mWorker.obtainMessage(12, z ? 1 : 0, 0).sendToTarget();
        }
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public void userActivity() {
        if (!this.mDestroyed) {
            this.mWorker.removeMessages(13);
            this.mWorker.sendEmptyMessage(13);
        }
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public void setRingerMode(int i, boolean z) {
        if (!this.mDestroyed) {
            this.mWorker.obtainMessage(4, i, z ? 1 : 0).sendToTarget();
        }
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public void setStreamVolume(int i, int i2) {
        if (!this.mDestroyed) {
            this.mWorker.obtainMessage(10, i, i2).sendToTarget();
        }
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public void setActiveStream(int i) {
        if (!this.mDestroyed) {
            this.mWorker.obtainMessage(11, i, 0).sendToTarget();
        }
    }

    public void setEnableDialogs(boolean z, boolean z2) {
        this.mShowVolumeDialog = z;
        this.mShowSafetyWarning = z2;
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public void scheduleTouchFeedback() {
        this.mLastToggledRingerOn = System.currentTimeMillis();
    }

    private void playTouchFeedback() {
        if (System.currentTimeMillis() - this.mLastToggledRingerOn < 1000) {
            try {
                this.mAudioService.playSoundEffect(5);
            } catch (RemoteException unused) {
            }
        }
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public void vibrate(VibrationEffect vibrationEffect) {
        if (this.mHasVibrator) {
            this.mVibrator.vibrate(vibrationEffect, SONIFICIATION_VIBRATION_ATTRIBUTES);
        }
    }

    @Override // com.android.systemui.plugins.VolumeDialogController
    public boolean hasVibrator() {
        return this.mHasVibrator;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onNotifyVisibleW(boolean z) {
        if (!this.mDestroyed) {
            this.mAudio.notifyVolumeControllerVisible(this.mVolumeController, z);
            if (!z && updateActiveStreamW(-1)) {
                this.mCallbacks.onStateChanged(this.mState);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUserActivityW() {
        synchronized (this) {
            if (this.mUserActivityListener != null) {
                this.mUserActivityListener.onUserActivity();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onShowSafetyWarningW(int i) {
        if (this.mShowSafetyWarning) {
            this.mCallbacks.onShowSafetyWarning(i);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onGetCaptionsComponentStateW(boolean z) {
        Boolean bool = Boolean.FALSE;
        try {
            String string = this.mContext.getString(17039897);
            if (TextUtils.isEmpty(string)) {
                this.mCallbacks.onCaptionComponentStateChanged(bool, Boolean.valueOf(z));
                return;
            }
            boolean z2 = false;
            if (D.BUG) {
                Log.i(TAG, String.format("isCaptionsServiceEnabled componentNameString=%s", string));
            }
            ComponentName unflattenFromString = ComponentName.unflattenFromString(string);
            if (unflattenFromString == null) {
                this.mCallbacks.onCaptionComponentStateChanged(bool, Boolean.valueOf(z));
                return;
            }
            PackageManager packageManager = this.mContext.getPackageManager();
            C c = this.mCallbacks;
            if (packageManager.getComponentEnabledSetting(unflattenFromString) == 1) {
                z2 = true;
            }
            c.onCaptionComponentStateChanged(Boolean.valueOf(z2), Boolean.valueOf(z));
        } catch (Exception e) {
            Log.e(TAG, "isCaptionsServiceEnabled failed to check for captions component", e);
            this.mCallbacks.onCaptionComponentStateChanged(bool, Boolean.valueOf(z));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onAccessibilityModeChanged(Boolean bool) {
        this.mCallbacks.onAccessibilityModeChanged(bool);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkRoutedToBluetoothW(int i) {
        if (i != 3) {
            return false;
        }
        return false | updateStreamRoutedToBluetoothW(i, (this.mAudio.getDevicesForStream(3) & 896) != 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean shouldShowUI(int i) {
        Optional<U> map = this.mStatusBarOptionalLazy.map(new Function(i) {
            /* class com.android.systemui.volume.$$Lambda$VolumeDialogControllerImpl$ZC0tNHMtDXeMl4n7hjZubonHuc */
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return VolumeDialogControllerImpl.this.lambda$shouldShowUI$0$VolumeDialogControllerImpl(this.f$1, (Lazy) obj);
            }
        });
        boolean z = true;
        if (!this.mShowVolumeDialog || (i & 1) == 0) {
            z = false;
        }
        return map.orElse((U) Boolean.valueOf(z)).booleanValue();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$shouldShowUI$0 */
    public /* synthetic */ Boolean lambda$shouldShowUI$0$VolumeDialogControllerImpl(int i, Lazy lazy) {
        StatusBar statusBar = (StatusBar) lazy.get();
        boolean z = true;
        if (statusBar.getWakefulnessState() == 0 || statusBar.getWakefulnessState() == 3 || !statusBar.isDeviceInteractive() || (i & 1) == 0 || !this.mShowVolumeDialog) {
            z = false;
        }
        return Boolean.valueOf(z);
    }

    /* access modifiers changed from: package-private */
    public boolean onVolumeChangedW(int i, int i2) {
        boolean shouldShowUI = shouldShowUI(i2);
        boolean z = (i2 & 4096) != 0;
        boolean z2 = (i2 & 2048) != 0;
        boolean z3 = (i2 & 128) != 0;
        boolean updateActiveStreamW = shouldShowUI ? updateActiveStreamW(i) | false : false;
        int audioManagerStreamVolume = getAudioManagerStreamVolume(i);
        boolean updateStreamLevelW = updateActiveStreamW | updateStreamLevelW(i, audioManagerStreamVolume) | checkRoutedToBluetoothW(shouldShowUI ? 3 : i);
        if (updateStreamLevelW) {
            this.mCallbacks.onStateChanged(this.mState);
        }
        if (shouldShowUI) {
            this.mCallbacks.onPerformHapticFeedback(VolumeDialogTransformHelper.calculateHapticFeedbackState(streamStateW(i), audioManagerStreamVolume, i2, updateStreamLevelW));
            this.mCallbacks.onShowRequested(1);
        }
        if (z2) {
            this.mCallbacks.onShowVibrateHint();
        }
        if (z3) {
            this.mCallbacks.onShowSilentHint();
        }
        if (updateStreamLevelW && z) {
            Events.writeEvent(4, Integer.valueOf(i), Integer.valueOf(audioManagerStreamVolume));
        }
        return updateStreamLevelW;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean updateActiveStreamW(int i) {
        VolumeDialogController.State state = this.mState;
        if (i == state.activeStream) {
            return false;
        }
        state.activeStream = i;
        Events.writeEvent(2, Integer.valueOf(i));
        if (D.BUG) {
            String str = TAG;
            Log.d(str, "updateActiveStreamW " + i);
        }
        if (i >= 100) {
            i = -1;
        }
        if (D.BUG) {
            String str2 = TAG;
            Log.d(str2, "forceVolumeControlStream " + i);
        }
        this.mAudio.forceVolumeControlStream(i);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private VolumeDialogController.StreamState streamStateW(int i) {
        VolumeDialogController.StreamState streamState = this.mState.states.get(i);
        if (streamState != null) {
            return streamState;
        }
        VolumeDialogController.StreamState streamState2 = new VolumeDialogController.StreamState();
        this.mState.states.put(i, streamState2);
        return streamState2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onGetStateW() {
        for (Integer num : STREAMS.keySet()) {
            int intValue = num.intValue();
            updateStreamLevelW(intValue, getAudioManagerStreamVolume(intValue));
            streamStateW(intValue).levelMin = getAudioManagerStreamMinVolume(intValue);
            streamStateW(intValue).levelMax = Math.max(1, getAudioManagerStreamMaxVolume(intValue));
            updateStreamMuteW(intValue, this.mAudio.isStreamMute(intValue));
            VolumeDialogController.StreamState streamStateW = streamStateW(intValue);
            streamStateW.muteSupported = this.mAudio.isStreamAffectedByMute(intValue);
            streamStateW.name = STREAMS.get(Integer.valueOf(intValue)).intValue();
            checkRoutedToBluetoothW(intValue);
        }
        updateRingerModeExternalW(this.mRingerModeObservers.mRingerMode.getValue().intValue());
        updateZenModeW();
        updateZenConfig();
        updateEffectsSuppressorW(this.mNoMan.getEffectsSuppressor());
        this.mCallbacks.onStateChanged(this.mState);
    }

    private boolean updateStreamRoutedToBluetoothW(int i, boolean z) {
        VolumeDialogController.StreamState streamStateW = streamStateW(i);
        if (streamStateW.routedToBluetooth == z) {
            return false;
        }
        streamStateW.routedToBluetooth = z;
        if (!D.BUG) {
            return true;
        }
        String str = TAG;
        Log.d(str, "updateStreamRoutedToBluetoothW stream=" + i + " routedToBluetooth=" + z);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean updateStreamLevelW(int i, int i2) {
        VolumeDialogController.StreamState streamStateW = streamStateW(i);
        if (streamStateW.level == i2) {
            return false;
        }
        streamStateW.level = i2;
        if (isLogWorthy(i)) {
            Events.writeEvent(10, Integer.valueOf(i), Integer.valueOf(i2));
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean updateStreamMuteW(int i, boolean z) {
        VolumeDialogController.StreamState streamStateW = streamStateW(i);
        if (streamStateW.muted == z) {
            return false;
        }
        streamStateW.muted = z;
        if (isLogWorthy(i)) {
            Events.writeEvent(15, Integer.valueOf(i), Boolean.valueOf(z));
        }
        if (z && isRinger(i)) {
            updateRingerModeInternalW(this.mRingerModeObservers.mRingerModeInternal.getValue().intValue());
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean updateEffectsSuppressorW(ComponentName componentName) {
        if (Objects.equals(this.mState.effectsSuppressor, componentName)) {
            return false;
        }
        VolumeDialogController.State state = this.mState;
        state.effectsSuppressor = componentName;
        state.effectsSuppressorName = getApplicationName(this.mContext, componentName);
        VolumeDialogController.State state2 = this.mState;
        Events.writeEvent(14, state2.effectsSuppressor, state2.effectsSuppressorName);
        return true;
    }

    private static String getApplicationName(Context context, ComponentName componentName) {
        if (componentName == null) {
            return null;
        }
        PackageManager packageManager = context.getPackageManager();
        String packageName = componentName.getPackageName();
        try {
            String trim = Objects.toString(packageManager.getApplicationInfo(packageName, 0).loadLabel(packageManager), "").trim();
            return trim.length() > 0 ? trim : packageName;
        } catch (PackageManager.NameNotFoundException unused) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean updateZenModeW() {
        int i = Settings.Global.getInt(this.mContext.getContentResolver(), "zen_mode", 0);
        VolumeDialogController.State state = this.mState;
        if (state.zenMode == i) {
            return false;
        }
        state.zenMode = i;
        Events.writeEvent(13, Integer.valueOf(i));
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean updateZenConfig() {
        NotificationManager.Policy consolidatedNotificationPolicy = this.mNotificationManager.getConsolidatedNotificationPolicy();
        boolean z = (consolidatedNotificationPolicy.priorityCategories & 32) == 0;
        boolean z2 = (consolidatedNotificationPolicy.priorityCategories & 64) == 0;
        boolean z3 = (consolidatedNotificationPolicy.priorityCategories & 128) == 0;
        boolean areAllPriorityOnlyRingerSoundsMuted = ZenModeConfig.areAllPriorityOnlyRingerSoundsMuted(consolidatedNotificationPolicy);
        VolumeDialogController.State state = this.mState;
        if (state.disallowAlarms == z && state.disallowMedia == z2 && state.disallowRinger == areAllPriorityOnlyRingerSoundsMuted && state.disallowSystem == z3) {
            return false;
        }
        VolumeDialogController.State state2 = this.mState;
        state2.disallowAlarms = z;
        state2.disallowMedia = z2;
        state2.disallowSystem = z3;
        state2.disallowRinger = areAllPriorityOnlyRingerSoundsMuted;
        Events.writeEvent(17, "disallowAlarms=" + z + " disallowMedia=" + z2 + " disallowSystem=" + z3 + " disallowRinger=" + areAllPriorityOnlyRingerSoundsMuted);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean updateRingerModeExternalW(int i) {
        VolumeDialogController.State state = this.mState;
        if (i == state.ringerModeExternal) {
            return false;
        }
        state.ringerModeExternal = i;
        Events.writeEvent(12, Integer.valueOf(i));
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean updateRingerModeInternalW(int i) {
        VolumeDialogController.State state = this.mState;
        if (i == state.ringerModeInternal) {
            return false;
        }
        state.ringerModeInternal = i;
        Events.writeEvent(11, Integer.valueOf(i));
        if (this.mState.ringerModeInternal == 2) {
            playTouchFeedback();
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSetRingerModeW(int i, boolean z) {
        if (z) {
            this.mAudio.setRingerMode(i);
        } else {
            this.mAudio.setRingerModeInternal(i);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSetStreamMuteW(int i, boolean z) {
        this.mAudio.adjustStreamVolume(i, z ? -100 : 100, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSetStreamVolumeW(int i, int i2) {
        if (D.BUG) {
            String str = TAG;
            Log.d(str, "onSetStreamVolume " + i + " level=" + i2);
        }
        if (i >= 100) {
            this.mMediaSessionsCallbacksW.setStreamVolume(i, i2);
        } else {
            setAudioManagerStreamVolume(i, i2, 0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSetActiveStreamW(int i) {
        if (updateActiveStreamW(i)) {
            this.mCallbacks.onStateChanged(this.mState);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSetExitConditionW(Condition condition) {
        this.mNoMan.setZenMode(this.mState.zenMode, condition != null ? condition.id : null, TAG);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSetZenModeW(int i) {
        if (D.BUG) {
            String str = TAG;
            Log.d(str, "onSetZenModeW " + i);
        }
        this.mNoMan.setZenMode(i, null, TAG);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDismissRequestedW(int i) {
        this.mCallbacks.onDismissRequested(i);
    }

    public void showDndTile(boolean z) {
        if (D.BUG) {
            Log.d(TAG, "showDndTile");
        }
        DndTile.setVisible(this.mContext, z);
    }

    /* access modifiers changed from: private */
    public final class VC extends IVolumeController.Stub {
        private final String TAG;

        private VC() {
            this.TAG = VolumeDialogControllerImpl.TAG + ".VC";
        }

        public void displaySafeVolumeWarning(int i) throws RemoteException {
            if (D.BUG) {
                String str = this.TAG;
                Log.d(str, "displaySafeVolumeWarning " + Util.audioManagerFlagsToString(i));
            }
            if (!VolumeDialogControllerImpl.this.mDestroyed) {
                VolumeDialogControllerImpl.this.mWorker.obtainMessage(14, i, 0).sendToTarget();
            }
        }

        public void volumeChanged(int i, int i2) throws RemoteException {
            if (D.BUG) {
                String str = this.TAG;
                Log.d(str, "volumeChanged " + AudioSystem.streamToString(i) + " " + Util.audioManagerFlagsToString(i2));
            }
            if (!VolumeDialogControllerImpl.this.mDestroyed) {
                VolumeDialogControllerImpl.this.mWorker.obtainMessage(1, i, i2).sendToTarget();
            }
        }

        public void masterMuteChanged(int i) throws RemoteException {
            if (D.BUG) {
                Log.d(this.TAG, "masterMuteChanged");
            }
        }

        public void setLayoutDirection(int i) throws RemoteException {
            if (D.BUG) {
                Log.d(this.TAG, "setLayoutDirection");
            }
            if (!VolumeDialogControllerImpl.this.mDestroyed) {
                VolumeDialogControllerImpl.this.mWorker.obtainMessage(8, i, 0).sendToTarget();
            }
        }

        public void dismiss() throws RemoteException {
            if (D.BUG) {
                Log.d(this.TAG, "dismiss requested");
            }
            if (!VolumeDialogControllerImpl.this.mDestroyed) {
                VolumeDialogControllerImpl.this.mWorker.obtainMessage(2, 2, 0).sendToTarget();
                VolumeDialogControllerImpl.this.mWorker.sendEmptyMessage(2);
            }
        }

        public void setA11yMode(int i) {
            if (D.BUG) {
                String str = this.TAG;
                Log.d(str, "setA11yMode to " + i);
            }
            if (!VolumeDialogControllerImpl.this.mDestroyed) {
                if (i == 0) {
                    VolumeDialogControllerImpl.this.mShowA11yStream = false;
                } else if (i != 1) {
                    String str2 = this.TAG;
                    Log.e(str2, "Invalid accessibility mode " + i);
                } else {
                    VolumeDialogControllerImpl.this.mShowA11yStream = true;
                }
                VolumeDialogControllerImpl.this.mWorker.obtainMessage(15, Boolean.valueOf(VolumeDialogControllerImpl.this.mShowA11yStream)).sendToTarget();
            }
        }
    }

    /* access modifiers changed from: private */
    public final class W extends Handler {
        W(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            boolean z = true;
            switch (message.what) {
                case 1:
                    VolumeDialogControllerImpl.this.onVolumeChangedW(message.arg1, message.arg2);
                    return;
                case 2:
                    VolumeDialogControllerImpl.this.onDismissRequestedW(message.arg1);
                    return;
                case 3:
                    VolumeDialogControllerImpl.this.onGetStateW();
                    return;
                case 4:
                    VolumeDialogControllerImpl volumeDialogControllerImpl = VolumeDialogControllerImpl.this;
                    int i = message.arg1;
                    if (message.arg2 == 0) {
                        z = false;
                    }
                    volumeDialogControllerImpl.onSetRingerModeW(i, z);
                    return;
                case 5:
                    VolumeDialogControllerImpl.this.onSetZenModeW(message.arg1);
                    return;
                case 6:
                    VolumeDialogControllerImpl.this.onSetExitConditionW((Condition) message.obj);
                    return;
                case 7:
                    VolumeDialogControllerImpl volumeDialogControllerImpl2 = VolumeDialogControllerImpl.this;
                    int i2 = message.arg1;
                    if (message.arg2 == 0) {
                        z = false;
                    }
                    volumeDialogControllerImpl2.onSetStreamMuteW(i2, z);
                    return;
                case 8:
                    VolumeDialogControllerImpl.this.mCallbacks.onLayoutDirectionChanged(message.arg1);
                    return;
                case 9:
                    VolumeDialogControllerImpl.this.mCallbacks.onConfigurationChanged();
                    return;
                case 10:
                    VolumeDialogControllerImpl.this.onSetStreamVolumeW(message.arg1, message.arg2);
                    return;
                case 11:
                    VolumeDialogControllerImpl.this.onSetActiveStreamW(message.arg1);
                    return;
                case 12:
                    VolumeDialogControllerImpl volumeDialogControllerImpl3 = VolumeDialogControllerImpl.this;
                    if (message.arg1 == 0) {
                        z = false;
                    }
                    volumeDialogControllerImpl3.onNotifyVisibleW(z);
                    return;
                case 13:
                    VolumeDialogControllerImpl.this.onUserActivityW();
                    return;
                case 14:
                    VolumeDialogControllerImpl.this.onShowSafetyWarningW(message.arg1);
                    return;
                case 15:
                    VolumeDialogControllerImpl.this.onAccessibilityModeChanged((Boolean) message.obj);
                    return;
                case 16:
                    VolumeDialogControllerImpl.this.onGetCaptionsComponentStateW(((Boolean) message.obj).booleanValue());
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class C implements VolumeDialogController.Callbacks {
        private final Map<VolumeDialogController.Callbacks, Handler> mCallbackMap = new ConcurrentHashMap();

        C(VolumeDialogControllerImpl volumeDialogControllerImpl) {
        }

        public void add(VolumeDialogController.Callbacks callbacks, Handler handler) {
            if (callbacks == null || handler == null) {
                throw new IllegalArgumentException();
            }
            this.mCallbackMap.put(callbacks, handler);
        }

        public void remove(VolumeDialogController.Callbacks callbacks) {
            this.mCallbackMap.remove(callbacks);
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onShowRequested(final int i) {
            for (final Map.Entry<VolumeDialogController.Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                entry.getValue().post(new Runnable(this) {
                    /* class com.android.systemui.volume.VolumeDialogControllerImpl.C.AnonymousClass1 */

                    public void run() {
                        ((VolumeDialogController.Callbacks) entry.getKey()).onShowRequested(i);
                    }
                });
            }
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onDismissRequested(final int i) {
            for (final Map.Entry<VolumeDialogController.Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                entry.getValue().post(new Runnable(this) {
                    /* class com.android.systemui.volume.VolumeDialogControllerImpl.C.AnonymousClass2 */

                    public void run() {
                        ((VolumeDialogController.Callbacks) entry.getKey()).onDismissRequested(i);
                    }
                });
            }
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onStateChanged(VolumeDialogController.State state) {
            long currentTimeMillis = System.currentTimeMillis();
            final VolumeDialogController.State copy = state.copy();
            for (final Map.Entry<VolumeDialogController.Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                entry.getValue().post(new Runnable(this) {
                    /* class com.android.systemui.volume.VolumeDialogControllerImpl.C.AnonymousClass3 */

                    public void run() {
                        ((VolumeDialogController.Callbacks) entry.getKey()).onStateChanged(copy);
                    }
                });
            }
            Events.writeState(currentTimeMillis, copy);
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onLayoutDirectionChanged(final int i) {
            for (final Map.Entry<VolumeDialogController.Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                entry.getValue().post(new Runnable(this) {
                    /* class com.android.systemui.volume.VolumeDialogControllerImpl.C.AnonymousClass4 */

                    public void run() {
                        ((VolumeDialogController.Callbacks) entry.getKey()).onLayoutDirectionChanged(i);
                    }
                });
            }
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onConfigurationChanged() {
            for (final Map.Entry<VolumeDialogController.Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                entry.getValue().post(new Runnable(this) {
                    /* class com.android.systemui.volume.VolumeDialogControllerImpl.C.AnonymousClass5 */

                    public void run() {
                        ((VolumeDialogController.Callbacks) entry.getKey()).onConfigurationChanged();
                    }
                });
            }
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onShowVibrateHint() {
            for (final Map.Entry<VolumeDialogController.Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                entry.getValue().post(new Runnable(this) {
                    /* class com.android.systemui.volume.VolumeDialogControllerImpl.C.AnonymousClass6 */

                    public void run() {
                        ((VolumeDialogController.Callbacks) entry.getKey()).onShowVibrateHint();
                    }
                });
            }
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onShowSilentHint() {
            for (final Map.Entry<VolumeDialogController.Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                entry.getValue().post(new Runnable(this) {
                    /* class com.android.systemui.volume.VolumeDialogControllerImpl.C.AnonymousClass7 */

                    public void run() {
                        ((VolumeDialogController.Callbacks) entry.getKey()).onShowSilentHint();
                    }
                });
            }
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onScreenOff() {
            for (final Map.Entry<VolumeDialogController.Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                entry.getValue().post(new Runnable(this) {
                    /* class com.android.systemui.volume.VolumeDialogControllerImpl.C.AnonymousClass8 */

                    public void run() {
                        ((VolumeDialogController.Callbacks) entry.getKey()).onScreenOff();
                    }
                });
            }
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onShowSafetyWarning(final int i) {
            for (final Map.Entry<VolumeDialogController.Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                entry.getValue().post(new Runnable(this) {
                    /* class com.android.systemui.volume.VolumeDialogControllerImpl.C.AnonymousClass9 */

                    public void run() {
                        ((VolumeDialogController.Callbacks) entry.getKey()).onShowSafetyWarning(i);
                    }
                });
            }
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onAccessibilityModeChanged(Boolean bool) {
            final boolean booleanValue = bool == null ? false : bool.booleanValue();
            for (final Map.Entry<VolumeDialogController.Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                entry.getValue().post(new Runnable(this) {
                    /* class com.android.systemui.volume.VolumeDialogControllerImpl.C.AnonymousClass10 */

                    public void run() {
                        ((VolumeDialogController.Callbacks) entry.getKey()).onAccessibilityModeChanged(Boolean.valueOf(booleanValue));
                    }
                });
            }
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onCaptionComponentStateChanged(Boolean bool, Boolean bool2) {
            boolean booleanValue = bool == null ? false : bool.booleanValue();
            for (Map.Entry<VolumeDialogController.Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                entry.getValue().post(new Runnable(entry, booleanValue, bool2) {
                    /* class com.android.systemui.volume.$$Lambda$VolumeDialogControllerImpl$C$Q4oXmUMuqtOXvcXaIdydaXsm_80 */
                    public final /* synthetic */ Map.Entry f$0;
                    public final /* synthetic */ boolean f$1;
                    public final /* synthetic */ Boolean f$2;

                    {
                        this.f$0 = r1;
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        ((VolumeDialogController.Callbacks) this.f$0.getKey()).onCaptionComponentStateChanged(Boolean.valueOf(this.f$1), this.f$2);
                    }
                });
            }
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onPerformHapticFeedback(final int i) {
            for (final Map.Entry<VolumeDialogController.Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                entry.getValue().post(new Runnable(this) {
                    /* class com.android.systemui.volume.VolumeDialogControllerImpl.C.AnonymousClass11 */

                    public void run() {
                        ((VolumeDialogController.Callbacks) entry.getKey()).onPerformHapticFeedback(i);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    public final class RingerModeObservers {
        private final RingerModeLiveData mRingerMode;
        private final RingerModeLiveData mRingerModeInternal;
        private final Observer<Integer> mRingerModeInternalObserver = new Observer<Integer>() {
            /* class com.android.systemui.volume.VolumeDialogControllerImpl.RingerModeObservers.AnonymousClass2 */

            public void onChanged(Integer num) {
                VolumeDialogControllerImpl.this.mWorker.post(new Runnable(num) {
                    /* class com.android.systemui.volume.$$Lambda$VolumeDialogControllerImpl$RingerModeObservers$2$2YUQzphxT5pK7JvSmIyMJfBaPo */
                    public final /* synthetic */ Integer f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        VolumeDialogControllerImpl.RingerModeObservers.AnonymousClass2.this.lambda$onChanged$0$VolumeDialogControllerImpl$RingerModeObservers$2(this.f$1);
                    }
                });
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onChanged$0 */
            public /* synthetic */ void lambda$onChanged$0$VolumeDialogControllerImpl$RingerModeObservers$2(Integer num) {
                int intValue = num.intValue();
                if (RingerModeObservers.this.mRingerModeInternal.getInitialSticky()) {
                    VolumeDialogControllerImpl.this.mState.ringerModeInternal = intValue;
                }
                if (D.BUG) {
                    String str = VolumeDialogControllerImpl.TAG;
                    Log.d(str, "onChange internal_ringer_mode rm=" + Util.ringerModeToString(intValue));
                }
                if (VolumeDialogControllerImpl.this.updateRingerModeInternalW(intValue)) {
                    VolumeDialogControllerImpl volumeDialogControllerImpl = VolumeDialogControllerImpl.this;
                    volumeDialogControllerImpl.mCallbacks.onStateChanged(volumeDialogControllerImpl.mState);
                }
            }
        };
        private final Observer<Integer> mRingerModeObserver = new Observer<Integer>() {
            /* class com.android.systemui.volume.VolumeDialogControllerImpl.RingerModeObservers.AnonymousClass1 */

            public void onChanged(Integer num) {
                VolumeDialogControllerImpl.this.mWorker.post(new Runnable(num) {
                    /* class com.android.systemui.volume.$$Lambda$VolumeDialogControllerImpl$RingerModeObservers$1$CmgEGLTOVVQV5yIb7ebHF_uhtJc */
                    public final /* synthetic */ Integer f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        VolumeDialogControllerImpl.RingerModeObservers.AnonymousClass1.this.lambda$onChanged$0$VolumeDialogControllerImpl$RingerModeObservers$1(this.f$1);
                    }
                });
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onChanged$0 */
            public /* synthetic */ void lambda$onChanged$0$VolumeDialogControllerImpl$RingerModeObservers$1(Integer num) {
                int intValue = num.intValue();
                if (RingerModeObservers.this.mRingerMode.getInitialSticky()) {
                    VolumeDialogControllerImpl.this.mState.ringerModeExternal = intValue;
                }
                if (D.BUG) {
                    String str = VolumeDialogControllerImpl.TAG;
                    Log.d(str, "onChange ringer_mode rm=" + Util.ringerModeToString(intValue));
                }
                if (VolumeDialogControllerImpl.this.updateRingerModeExternalW(intValue)) {
                    VolumeDialogControllerImpl volumeDialogControllerImpl = VolumeDialogControllerImpl.this;
                    volumeDialogControllerImpl.mCallbacks.onStateChanged(volumeDialogControllerImpl.mState);
                }
            }
        };

        RingerModeObservers(RingerModeLiveData ringerModeLiveData, RingerModeLiveData ringerModeLiveData2) {
            this.mRingerMode = ringerModeLiveData;
            this.mRingerModeInternal = ringerModeLiveData2;
        }

        public void init() {
            int intValue = this.mRingerMode.getValue().intValue();
            if (intValue != -1) {
                VolumeDialogControllerImpl.this.mState.ringerModeExternal = intValue;
            }
            this.mRingerMode.observeForever(this.mRingerModeObserver);
            int intValue2 = this.mRingerModeInternal.getValue().intValue();
            if (intValue2 != -1) {
                VolumeDialogControllerImpl.this.mState.ringerModeInternal = intValue2;
            }
            this.mRingerModeInternal.observeForever(this.mRingerModeInternalObserver);
        }
    }

    private final class SettingObserver extends ContentObserver {
        private final Uri ZEN_MODE_CONFIG_URI = Settings.Global.getUriFor("zen_mode_config_etag");
        private final Uri ZEN_MODE_URI = Settings.Global.getUriFor("zen_mode");

        public SettingObserver(Handler handler) {
            super(handler);
        }

        public void init() {
            VolumeDialogControllerImpl.this.mContext.getContentResolver().registerContentObserver(this.ZEN_MODE_URI, false, this);
            VolumeDialogControllerImpl.this.mContext.getContentResolver().registerContentObserver(this.ZEN_MODE_CONFIG_URI, false, this);
        }

        public void onChange(boolean z, Uri uri) {
            boolean updateZenModeW = this.ZEN_MODE_URI.equals(uri) ? VolumeDialogControllerImpl.this.updateZenModeW() : false;
            if (this.ZEN_MODE_CONFIG_URI.equals(uri)) {
                updateZenModeW |= VolumeDialogControllerImpl.this.updateZenConfig();
            }
            if (updateZenModeW) {
                VolumeDialogControllerImpl volumeDialogControllerImpl = VolumeDialogControllerImpl.this;
                volumeDialogControllerImpl.mCallbacks.onStateChanged(volumeDialogControllerImpl.mState);
            }
        }
    }

    private final class Receiver extends BroadcastReceiver {
        private Receiver() {
        }

        public void init() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
            intentFilter.addAction("android.media.STREAM_DEVICES_CHANGED_ACTION");
            intentFilter.addAction("android.media.STREAM_MUTE_CHANGED_ACTION");
            intentFilter.addAction("android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED");
            intentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            intentFilter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
            VolumeDialogControllerImpl volumeDialogControllerImpl = VolumeDialogControllerImpl.this;
            volumeDialogControllerImpl.mBroadcastDispatcher.registerReceiverWithHandler(this, intentFilter, volumeDialogControllerImpl.mWorker);
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean z = false;
            if (action.equals("android.media.VOLUME_CHANGED_ACTION")) {
                int intExtra = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
                int intExtra2 = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", -1);
                int intExtra3 = intent.getIntExtra("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", -1);
                if (D.BUG) {
                    String str = VolumeDialogControllerImpl.TAG;
                    Log.d(str, "onReceive VOLUME_CHANGED_ACTION stream=" + intExtra + " level=" + intExtra2 + " oldLevel=" + intExtra3);
                }
                z = VolumeDialogControllerImpl.this.updateStreamLevelW(intExtra, intExtra2);
            } else if (action.equals("android.media.STREAM_DEVICES_CHANGED_ACTION")) {
                int intExtra4 = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
                int intExtra5 = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_DEVICES", -1);
                int intExtra6 = intent.getIntExtra("android.media.EXTRA_PREV_VOLUME_STREAM_DEVICES", -1);
                if (D.BUG) {
                    String str2 = VolumeDialogControllerImpl.TAG;
                    Log.d(str2, "onReceive STREAM_DEVICES_CHANGED_ACTION stream=" + intExtra4 + " devices=" + intExtra5 + " oldDevices=" + intExtra6);
                }
                z = VolumeDialogControllerImpl.this.checkRoutedToBluetoothW(intExtra4) | VolumeDialogControllerImpl.this.onVolumeChangedW(intExtra4, 0);
            } else if (action.equals("android.media.STREAM_MUTE_CHANGED_ACTION")) {
                int intExtra7 = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
                boolean booleanExtra = intent.getBooleanExtra("android.media.EXTRA_STREAM_VOLUME_MUTED", false);
                if (D.BUG) {
                    String str3 = VolumeDialogControllerImpl.TAG;
                    Log.d(str3, "onReceive STREAM_MUTE_CHANGED_ACTION stream=" + intExtra7 + " muted=" + booleanExtra);
                }
                z = VolumeDialogControllerImpl.this.updateStreamMuteW(intExtra7, booleanExtra);
            } else if (action.equals("android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED")) {
                if (D.BUG) {
                    Log.d(VolumeDialogControllerImpl.TAG, "onReceive ACTION_EFFECTS_SUPPRESSOR_CHANGED");
                }
                VolumeDialogControllerImpl volumeDialogControllerImpl = VolumeDialogControllerImpl.this;
                z = volumeDialogControllerImpl.updateEffectsSuppressorW(volumeDialogControllerImpl.mNoMan.getEffectsSuppressor());
            } else if (action.equals("android.intent.action.CONFIGURATION_CHANGED")) {
                if (D.BUG) {
                    Log.d(VolumeDialogControllerImpl.TAG, "onReceive ACTION_CONFIGURATION_CHANGED");
                }
                VolumeDialogControllerImpl.this.mCallbacks.onConfigurationChanged();
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                if (D.BUG) {
                    Log.d(VolumeDialogControllerImpl.TAG, "onReceive ACTION_SCREEN_OFF");
                }
                VolumeDialogControllerImpl.this.mCallbacks.onScreenOff();
            } else if (action.equals("android.intent.action.CLOSE_SYSTEM_DIALOGS")) {
                if (D.BUG) {
                    Log.d(VolumeDialogControllerImpl.TAG, "onReceive ACTION_CLOSE_SYSTEM_DIALOGS");
                }
                VolumeDialogControllerImpl.this.dismiss();
            }
            if (z) {
                VolumeDialogControllerImpl volumeDialogControllerImpl2 = VolumeDialogControllerImpl.this;
                volumeDialogControllerImpl2.mCallbacks.onStateChanged(volumeDialogControllerImpl2.mState);
            }
        }
    }

    /* access modifiers changed from: protected */
    public final class MediaSessionsCallbacks implements MediaSessions.Callbacks {
        private int mNextStream = 100;
        private final HashMap<MediaSession.Token, Integer> mRemoteStreams = new HashMap<>();

        protected MediaSessionsCallbacks() {
        }

        @Override // com.android.settingslib.volume.MediaSessions.Callbacks
        public void onRemoteUpdate(MediaSession.Token token, String str, MediaController.PlaybackInfo playbackInfo) {
            int intValue;
            addStream(token, "onRemoteUpdate");
            synchronized (this.mRemoteStreams) {
                intValue = this.mRemoteStreams.get(token).intValue();
            }
            Slog.d(VolumeDialogControllerImpl.TAG, "onRemoteUpdate: stream: " + intValue + " volume: " + playbackInfo.getCurrentVolume());
            boolean z = true;
            boolean z2 = VolumeDialogControllerImpl.this.mState.states.indexOfKey(intValue) < 0;
            VolumeDialogController.StreamState streamStateW = VolumeDialogControllerImpl.this.streamStateW(intValue);
            streamStateW.dynamic = true;
            streamStateW.levelMin = 0;
            streamStateW.levelMax = playbackInfo.getMaxVolume();
            if (streamStateW.level != playbackInfo.getCurrentVolume()) {
                streamStateW.level = playbackInfo.getCurrentVolume();
                z2 = true;
            }
            if (!Objects.equals(streamStateW.remoteLabel, str)) {
                streamStateW.name = -1;
                streamStateW.remoteLabel = str;
            } else {
                z = z2;
            }
            if (z) {
                Log.d(VolumeDialogControllerImpl.TAG, "onRemoteUpdate: " + str + ": " + streamStateW.level + " of " + streamStateW.levelMax);
                VolumeDialogControllerImpl volumeDialogControllerImpl = VolumeDialogControllerImpl.this;
                volumeDialogControllerImpl.mCallbacks.onStateChanged(volumeDialogControllerImpl.mState);
            }
        }

        @Override // com.android.settingslib.volume.MediaSessions.Callbacks
        public void onRemoteVolumeChanged(MediaSession.Token token, int i) {
            int intValue;
            addStream(token, "onRemoteVolumeChanged");
            synchronized (this.mRemoteStreams) {
                intValue = this.mRemoteStreams.get(token).intValue();
            }
            boolean shouldShowUI = VolumeDialogControllerImpl.this.shouldShowUI(i);
            String str = VolumeDialogControllerImpl.TAG;
            Slog.d(str, "onRemoteVolumeChanged: stream: " + intValue + " showui? " + shouldShowUI);
            boolean updateActiveStreamW = VolumeDialogControllerImpl.this.updateActiveStreamW(intValue);
            if (shouldShowUI) {
                updateActiveStreamW |= VolumeDialogControllerImpl.this.checkRoutedToBluetoothW(3);
            }
            if (updateActiveStreamW) {
                Slog.d(VolumeDialogControllerImpl.TAG, "onRemoteChanged: updatingState");
                VolumeDialogControllerImpl volumeDialogControllerImpl = VolumeDialogControllerImpl.this;
                volumeDialogControllerImpl.mCallbacks.onStateChanged(volumeDialogControllerImpl.mState);
            }
            if (shouldShowUI) {
                VolumeDialogControllerImpl.this.mCallbacks.onShowRequested(2);
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x0036, code lost:
            r3.this$0.mState.states.remove(r4);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0049, code lost:
            if (r3.this$0.mState.activeStream != r4) goto L_0x0051;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x004b, code lost:
            r0 = r3.this$0.updateActiveStreamW(-1);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0051, code lost:
            r3 = r3.this$0;
            r3.mCallbacks.onStateChanged(r3.mState);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x005c, code lost:
            return;
         */
        @Override // com.android.settingslib.volume.MediaSessions.Callbacks
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onRemoteRemoved(android.media.session.MediaSession.Token r4) {
            /*
                r3 = this;
                java.util.HashMap<android.media.session.MediaSession$Token, java.lang.Integer> r0 = r3.mRemoteStreams
                monitor-enter(r0)
                java.util.HashMap<android.media.session.MediaSession$Token, java.lang.Integer> r1 = r3.mRemoteStreams     // Catch:{ all -> 0x005d }
                boolean r1 = r1.containsKey(r4)     // Catch:{ all -> 0x005d }
                if (r1 != 0) goto L_0x0029
                java.lang.String r3 = com.android.systemui.volume.VolumeDialogControllerImpl.access$500()     // Catch:{ all -> 0x005d }
                java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x005d }
                r1.<init>()     // Catch:{ all -> 0x005d }
                java.lang.String r2 = "onRemoteRemoved: stream doesn't exist, aborting remote removed for token:"
                r1.append(r2)     // Catch:{ all -> 0x005d }
                java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x005d }
                r1.append(r4)     // Catch:{ all -> 0x005d }
                java.lang.String r4 = r1.toString()     // Catch:{ all -> 0x005d }
                android.util.Log.d(r3, r4)     // Catch:{ all -> 0x005d }
                monitor-exit(r0)     // Catch:{ all -> 0x005d }
                return
            L_0x0029:
                java.util.HashMap<android.media.session.MediaSession$Token, java.lang.Integer> r1 = r3.mRemoteStreams     // Catch:{ all -> 0x005d }
                java.lang.Object r4 = r1.get(r4)     // Catch:{ all -> 0x005d }
                java.lang.Integer r4 = (java.lang.Integer) r4     // Catch:{ all -> 0x005d }
                int r4 = r4.intValue()     // Catch:{ all -> 0x005d }
                monitor-exit(r0)     // Catch:{ all -> 0x005d }
                com.android.systemui.volume.VolumeDialogControllerImpl r0 = com.android.systemui.volume.VolumeDialogControllerImpl.this
                com.android.systemui.plugins.VolumeDialogController$State r0 = com.android.systemui.volume.VolumeDialogControllerImpl.access$2200(r0)
                android.util.SparseArray<com.android.systemui.plugins.VolumeDialogController$StreamState> r0 = r0.states
                r0.remove(r4)
                com.android.systemui.volume.VolumeDialogControllerImpl r0 = com.android.systemui.volume.VolumeDialogControllerImpl.this
                com.android.systemui.plugins.VolumeDialogController$State r0 = com.android.systemui.volume.VolumeDialogControllerImpl.access$2200(r0)
                int r0 = r0.activeStream
                if (r0 != r4) goto L_0x0051
                com.android.systemui.volume.VolumeDialogControllerImpl r4 = com.android.systemui.volume.VolumeDialogControllerImpl.this
                r0 = -1
                com.android.systemui.volume.VolumeDialogControllerImpl.access$3500(r4, r0)
            L_0x0051:
                com.android.systemui.volume.VolumeDialogControllerImpl r3 = com.android.systemui.volume.VolumeDialogControllerImpl.this
                com.android.systemui.volume.VolumeDialogControllerImpl$C r4 = r3.mCallbacks
                com.android.systemui.plugins.VolumeDialogController$State r3 = com.android.systemui.volume.VolumeDialogControllerImpl.access$2200(r3)
                r4.onStateChanged(r3)
                return
            L_0x005d:
                r3 = move-exception
                monitor-exit(r0)
                throw r3
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.volume.VolumeDialogControllerImpl.MediaSessionsCallbacks.onRemoteRemoved(android.media.session.MediaSession$Token):void");
        }

        public void setStreamVolume(int i, int i2) {
            MediaSession.Token findToken = findToken(i);
            if (findToken == null) {
                String str = VolumeDialogControllerImpl.TAG;
                Log.w(str, "setStreamVolume: No token found for stream: " + i);
                return;
            }
            VolumeDialogControllerImpl.this.mMediaSessions.setVolume(findToken, i2);
        }

        private MediaSession.Token findToken(int i) {
            synchronized (this.mRemoteStreams) {
                for (Map.Entry<MediaSession.Token, Integer> entry : this.mRemoteStreams.entrySet()) {
                    if (entry.getValue().equals(Integer.valueOf(i))) {
                        return entry.getKey();
                    }
                }
                return null;
            }
        }

        private void addStream(MediaSession.Token token, String str) {
            synchronized (this.mRemoteStreams) {
                if (!this.mRemoteStreams.containsKey(token)) {
                    this.mRemoteStreams.put(token, Integer.valueOf(this.mNextStream));
                    String str2 = VolumeDialogControllerImpl.TAG;
                    Log.d(str2, str + ": added stream " + this.mNextStream + " from token + " + token.toString());
                    this.mNextStream = this.mNextStream + 1;
                }
            }
        }
    }
}
