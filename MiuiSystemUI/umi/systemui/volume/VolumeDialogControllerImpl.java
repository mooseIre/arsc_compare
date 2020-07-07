package com.android.systemui.volume;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.media.AudioManagerCompat;
import android.media.AudioServiceInjector;
import android.media.AudioSystem;
import android.media.IVolumeController;
import android.media.VolumePolicy;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.Vibrator;
import android.provider.Settings;
import android.service.notification.Condition;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.systemui.Dumpable;
import com.android.systemui.Logger;
import com.android.systemui.miui.volume.VolumeEventTracker;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.VolumeDialogController;
import com.android.systemui.volume.MediaSessions;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class VolumeDialogControllerImpl implements VolumeDialogController, Dumpable {
    private static final ArrayMap<Integer, Integer> STREAMS;
    /* access modifiers changed from: private */
    public static final String TAG = Util.logTag(VolumeDialogControllerImpl.class);
    private AudioManager mAudio;
    /* access modifiers changed from: private */
    public final C mCallbacks = new C();
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public boolean mDestroyed;
    private final boolean mHasVibrator;
    /* access modifiers changed from: private */
    public final MediaSessions mMediaSessions;
    private final MediaSessionsCallbacks mMediaSessionsCallbacksW = new MediaSessionsCallbacks();
    /* access modifiers changed from: private */
    public final NotificationManager mNoMan;
    private final SettingObserver mObserver;
    private final Receiver mReceiver = new Receiver();
    /* access modifiers changed from: private */
    public boolean mShowA11yStream;
    private boolean mShowDndTile;
    /* access modifiers changed from: private */
    public final VolumeDialogController.State mState = new VolumeDialogController.State();
    @GuardedBy({"this"})
    private UserActivityListener mUserActivityListener;
    private final Vibrator mVibrator;
    private int mVoiceAssistStreamType;
    protected final VC mVolumeController;
    private VolumePolicy mVolumePolicy;
    /* access modifiers changed from: private */
    public final W mWorker;
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

    static {
        ArrayMap<Integer, Integer> arrayMap = new ArrayMap<>();
        STREAMS = arrayMap;
        arrayMap.put(4, Integer.valueOf(R.string.stream_alarm));
        STREAMS.put(6, Integer.valueOf(R.string.stream_bluetooth_sco));
        STREAMS.put(8, Integer.valueOf(R.string.stream_dtmf));
        STREAMS.put(3, Integer.valueOf(R.string.stream_music));
        if (Build.VERSION.SDK_INT >= 26) {
            STREAMS.put(10, Integer.valueOf(R.string.stream_accessibility));
        }
        STREAMS.put(5, Integer.valueOf(R.string.stream_notification));
        STREAMS.put(2, Integer.valueOf(R.string.stream_ring));
        STREAMS.put(1, Integer.valueOf(R.string.stream_system));
        STREAMS.put(7, Integer.valueOf(R.string.stream_system_enforced));
        STREAMS.put(9, Integer.valueOf(R.string.stream_tts));
        STREAMS.put(0, Integer.valueOf(R.string.stream_voice_call));
    }

    public VolumeDialogControllerImpl(Context context) {
        boolean z = true;
        this.mShowDndTile = true;
        this.mVoiceAssistStreamType = -2;
        this.mVolumeController = new VC();
        Context applicationContext = context.getApplicationContext();
        this.mContext = applicationContext;
        Events.writeEvent(applicationContext, 5, new Object[0]);
        HandlerThread handlerThread = new HandlerThread(VolumeDialogControllerImpl.class.getSimpleName());
        this.mWorkerThread = handlerThread;
        handlerThread.start();
        this.mWorker = new W(this.mWorkerThread.getLooper());
        this.mMediaSessions = createMediaSessions(this.mContext, this.mWorkerThread.getLooper(), this.mMediaSessionsCallbacksW);
        this.mAudio = (AudioManager) this.mContext.getSystemService("audio");
        this.mNoMan = (NotificationManager) this.mContext.getSystemService("notification");
        SettingObserver settingObserver = new SettingObserver(this.mWorker);
        this.mObserver = settingObserver;
        settingObserver.init();
        this.mReceiver.init();
        Vibrator vibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        this.mVibrator = vibrator;
        this.mHasVibrator = (vibrator == null || !vibrator.hasVibrator()) ? false : z;
        if (getVoiceAssistStreamType() > 0) {
            STREAMS.put(Integer.valueOf(getVoiceAssistStreamType()), Integer.valueOf(R.string.stream_voice_assist));
        }
    }

    public int getVoiceAssistStreamType() {
        if (this.mVoiceAssistStreamType == -2) {
            this.mVoiceAssistStreamType = AudioServiceInjector.getVoiceAssistNum();
        }
        return this.mVoiceAssistStreamType;
    }

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
        return AudioManagerCompat.getStreamMinVolume(this.mAudio, i);
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
        printWriter.print("  mRemoteStreams: ");
        printWriter.println(this.mMediaSessionsCallbacksW.mRemoteStreams.values());
        printWriter.print("  mShowA11yStream: ");
        printWriter.println(this.mShowA11yStream);
        printWriter.println();
        this.mMediaSessions.dump(printWriter);
    }

    public void addCallback(VolumeDialogController.Callbacks callbacks, Handler handler) {
        this.mCallbacks.add(callbacks, handler);
    }

    public void setUserActivityListener(UserActivityListener userActivityListener) {
        if (!this.mDestroyed) {
            synchronized (this) {
                this.mUserActivityListener = userActivityListener;
            }
        }
    }

    public void removeCallback(VolumeDialogController.Callbacks callbacks) {
        this.mCallbacks.remove(callbacks);
    }

    public void getState() {
        if (!this.mDestroyed) {
            this.mWorker.sendEmptyMessage(3);
        }
    }

    public void notifyVisible(boolean z) {
        if (!this.mDestroyed) {
            this.mWorker.obtainMessage(12, z ? 1 : 0, 0).sendToTarget();
        }
    }

    public void userActivity() {
        if (!this.mDestroyed) {
            this.mWorker.removeMessages(13);
            this.mWorker.sendEmptyMessage(13);
        }
    }

    public void setRingerMode(int i, boolean z) {
        if (!this.mDestroyed) {
            this.mWorker.obtainMessage(4, i, z ? 1 : 0).sendToTarget();
        }
    }

    public void setStreamVolume(int i, int i2) {
        if (!this.mDestroyed) {
            this.mWorker.obtainMessage(10, i, i2).sendToTarget();
        }
    }

    public void setActiveStream(int i) {
        if (!this.mDestroyed) {
            this.mWorker.obtainMessage(11, i, 0).sendToTarget();
        }
    }

    public void vibrate() {
        if (this.mHasVibrator) {
            this.mVibrator.vibrate(50);
        }
    }

    public boolean hasVibrator() {
        return this.mHasVibrator;
    }

    /* access modifiers changed from: private */
    public void onNotifyVisibleW(boolean z) {
        if (!this.mDestroyed) {
            this.mAudio.notifyVolumeControllerVisible(this.mVolumeController, z);
            if (!z && updateActiveStreamW(-1)) {
                this.mCallbacks.onStateChanged(this.mState);
            }
        }
    }

    /* access modifiers changed from: private */
    public void onUserActivityW() {
        synchronized (this) {
            if (this.mUserActivityListener != null) {
                this.mUserActivityListener.onUserActivity();
            }
        }
    }

    /* access modifiers changed from: private */
    public void onShowSafetyWarningW(int i) {
        this.mCallbacks.onShowSafetyWarning(i);
    }

    /* access modifiers changed from: private */
    public void onAccessibilityModeChanged(Boolean bool) {
        this.mCallbacks.onAccessibilityModeChanged(bool);
    }

    /* access modifiers changed from: private */
    public boolean checkRoutedToBluetoothW(int i) {
        if (i != 3) {
            return false;
        }
        return false | updateStreamRoutedToBluetoothW(i, (this.mAudio.getDevicesForStream(3) & 896) != 0);
    }

    /* access modifiers changed from: private */
    public boolean onVolumeChangedW(int i, int i2) {
        boolean z = (i2 & 1) != 0;
        boolean z2 = (i2 & 4096) != 0;
        boolean z3 = (i2 & 2048) != 0;
        boolean z4 = (i2 & 128) != 0;
        boolean updateActiveStreamW = z ? updateActiveStreamW(i) | false : false;
        int audioManagerStreamVolume = getAudioManagerStreamVolume(i);
        int i3 = streamStateW(i).level - streamStateW(i).lastLevel;
        boolean updateStreamLevelW = updateActiveStreamW | updateStreamLevelW(i, audioManagerStreamVolume) | checkRoutedToBluetoothW(z ? 3 : i);
        if (D.BUG) {
            String str = TAG;
            Log.d(str, "onVolumeChangedW showUI:" + z + " fromKey:" + z2 + " showVibrateHint:" + z3 + " showSilentHint:" + z4 + " stream:" + i + " lastAudibleStreamVolume:" + audioManagerStreamVolume + " levelUp:" + i3 + " changed:" + updateStreamLevelW);
        }
        if (updateStreamLevelW) {
            this.mCallbacks.onStateChanged(this.mState);
        }
        if (z) {
            String str2 = TAG;
            Logger.i(str2, "onVolumeChangedW showUI activeStream:" + this.mState.activeStream + " fromKey:" + z2);
            VolumeEventTracker.recordAudioCallbackTime();
            this.mCallbacks.onShowRequested(1, z2, i3);
        }
        if (z3) {
            this.mCallbacks.onShowVibrateHint();
        }
        if (z4) {
            this.mCallbacks.onShowSilentHint();
        }
        if (updateStreamLevelW && z2) {
            Events.writeEvent(this.mContext, 4, Integer.valueOf(i), Integer.valueOf(audioManagerStreamVolume));
        }
        this.mCallbacks.onVolumeChanged(i, z2);
        return updateStreamLevelW;
    }

    /* access modifiers changed from: private */
    public boolean updateActiveStreamW(int i) {
        VolumeDialogController.State state = this.mState;
        if (i == state.activeStream) {
            return false;
        }
        state.activeStream = i;
        Events.writeEvent(this.mContext, 2, Integer.valueOf(i));
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
    public VolumeDialogController.StreamState streamStateW(int i) {
        VolumeDialogController.StreamState streamState = this.mState.states.get(i);
        if (streamState != null) {
            return streamState;
        }
        VolumeDialogController.StreamState streamState2 = new VolumeDialogController.StreamState();
        this.mState.states.put(i, streamState2);
        return streamState2;
    }

    /* access modifiers changed from: private */
    public void onGetStateW() {
        if (D.BUG) {
            String str = TAG;
            Log.d(str, "onGetStateW STREAMS" + STREAMS.size());
        }
        for (Integer intValue : STREAMS.keySet()) {
            int intValue2 = intValue.intValue();
            updateStreamLevelW(intValue2, getAudioManagerStreamVolume(intValue2));
            streamStateW(intValue2).levelMin = getAudioManagerStreamMinVolume(intValue2);
            streamStateW(intValue2).levelMax = getAudioManagerStreamMaxVolume(intValue2);
            updateStreamMuteW(intValue2, this.mAudio.isStreamMute(intValue2));
            VolumeDialogController.StreamState streamStateW = streamStateW(intValue2);
            streamStateW.muteSupported = this.mAudio.isStreamAffectedByMute(intValue2);
            streamStateW.nameRes = STREAMS.get(Integer.valueOf(intValue2)).intValue();
            if (D.BUG) {
                String str2 = TAG;
                Log.d(str2, "onGetStateW stream:" + intValue2 + " levelMax:" + streamStateW(intValue2).levelMax + " name:" + this.mContext.getResources().getString(STREAMS.get(Integer.valueOf(intValue2)).intValue()) + " mute:" + streamStateW.muted);
            }
            checkRoutedToBluetoothW(intValue2);
        }
        updateRingerModeExternalW(this.mAudio.getRingerMode());
        updateZenModeW();
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
    public boolean updateStreamLevelW(int i, int i2) {
        VolumeDialogController.StreamState streamStateW = streamStateW(i);
        if (streamStateW.level == i2) {
            return false;
        }
        String str = TAG;
        Logger.i(str, "updateStreamLevelW stream:" + i + " ss.level:" + streamStateW.level + " level:" + i2 + " ss.levelMax:" + streamStateW.levelMax + " mute:" + streamStateW.muted);
        streamStateW.lastLevel = streamStateW.level;
        streamStateW.level = i2;
        if (isLogWorthy(i)) {
            Events.writeEvent(this.mContext, 10, Integer.valueOf(i), Integer.valueOf(i2));
        }
        return true;
    }

    /* access modifiers changed from: private */
    public boolean updateStreamMuteW(int i, boolean z) {
        VolumeDialogController.StreamState streamStateW = streamStateW(i);
        if (streamStateW.muted == z) {
            return false;
        }
        streamStateW.muted = z;
        if (isLogWorthy(i)) {
            Events.writeEvent(this.mContext, 15, Integer.valueOf(i), Boolean.valueOf(z));
        }
        if (z && isRinger(i)) {
            updateRingerModeInternalW(this.mAudio.getRingerModeInternal());
        }
        if (z) {
            streamStateW.level = this.mAudio.getLastAudibleStreamVolume(i);
        }
        return true;
    }

    /* access modifiers changed from: private */
    public boolean updateEffectsSuppressorW(ComponentName componentName) {
        if (Objects.equals(this.mState.effectsSuppressor, componentName)) {
            return false;
        }
        VolumeDialogController.State state = this.mState;
        state.effectsSuppressor = componentName;
        state.effectsSuppressorName = getApplicationName(this.mContext, componentName);
        Context context = this.mContext;
        VolumeDialogController.State state2 = this.mState;
        Events.writeEvent(context, 14, state2.effectsSuppressor, state2.effectsSuppressorName);
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
    public boolean updateZenModeW() {
        int i = Settings.Global.getInt(this.mContext.getContentResolver(), "zen_mode", 0);
        VolumeDialogController.State state = this.mState;
        if (state.zenMode == i) {
            return false;
        }
        state.zenMode = i;
        Events.writeEvent(this.mContext, 13, Integer.valueOf(i));
        return true;
    }

    /* access modifiers changed from: private */
    public boolean updateRingerModeExternalW(int i) {
        VolumeDialogController.State state = this.mState;
        if (i == state.ringerModeExternal) {
            return false;
        }
        state.ringerModeExternal = i;
        Events.writeEvent(this.mContext, 12, Integer.valueOf(i));
        return true;
    }

    /* access modifiers changed from: private */
    public boolean updateRingerModeInternalW(int i) {
        VolumeDialogController.State state = this.mState;
        if (i == state.ringerModeInternal) {
            return false;
        }
        state.ringerModeInternal = i;
        Events.writeEvent(this.mContext, 11, Integer.valueOf(i));
        return true;
    }

    /* access modifiers changed from: private */
    public void onSetRingerModeW(int i, boolean z) {
        if (z) {
            this.mAudio.setRingerMode(i);
        } else {
            this.mAudio.setRingerModeInternal(i);
        }
    }

    /* access modifiers changed from: private */
    public void onSetStreamMuteW(int i, boolean z) {
        this.mAudio.adjustStreamVolume(i, z ? -100 : 100, 0);
    }

    /* access modifiers changed from: private */
    public void onSetStreamVolumeW(int i, int i2) {
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
    public void onSetActiveStreamW(int i) {
        if (updateActiveStreamW(i)) {
            this.mCallbacks.onStateChanged(this.mState);
        }
    }

    /* access modifiers changed from: private */
    public void onSetExitConditionW(Condition condition) {
        this.mNoMan.setZenMode(this.mState.zenMode, condition != null ? condition.id : null, TAG);
    }

    /* access modifiers changed from: private */
    public void onSetZenModeW(int i) {
        if (D.BUG) {
            String str = TAG;
            Log.d(str, "onSetZenModeW " + i);
        }
        this.mNoMan.setZenMode(i, (Uri) null, TAG);
    }

    /* access modifiers changed from: private */
    public void onDismissRequestedW(int i) {
        this.mCallbacks.onDismissRequested(i);
    }

    public void showDndTile(boolean z) {
        if (D.BUG) {
            Log.d(TAG, "showDndTile");
        }
    }

    private final class VC extends IVolumeController.Stub {
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
                    boolean unused = VolumeDialogControllerImpl.this.mShowA11yStream = false;
                } else if (i != 1) {
                    String str2 = this.TAG;
                    Log.e(str2, "Invalid accessibility mode " + i);
                } else {
                    boolean unused2 = VolumeDialogControllerImpl.this.mShowA11yStream = true;
                }
                VolumeDialogControllerImpl.this.mWorker.obtainMessage(15, Boolean.valueOf(VolumeDialogControllerImpl.this.mShowA11yStream)).sendToTarget();
            }
        }
    }

    private final class W extends Handler {
        W(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            boolean z = true;
            switch (message.what) {
                case 1:
                    boolean unused = VolumeDialogControllerImpl.this.onVolumeChangedW(message.arg1, message.arg2);
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
                default:
                    return;
            }
        }
    }

    private final class C implements VolumeDialogController.Callbacks {
        private final ConcurrentHashMap<VolumeDialogController.Callbacks, Handler> mCallbackMap;

        private C() {
            this.mCallbackMap = new ConcurrentHashMap<>();
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

        public void onShowRequested(int i, boolean z, int i2) {
            for (final Map.Entry next : this.mCallbackMap.entrySet()) {
                final int i3 = i;
                final boolean z2 = z;
                final int i4 = i2;
                ((Handler) next.getValue()).post(new Runnable(this) {
                    public void run() {
                        ((VolumeDialogController.Callbacks) next.getKey()).onShowRequested(i3, z2, i4);
                    }
                });
            }
        }

        public void onDismissRequested(final int i) {
            for (final Map.Entry next : this.mCallbackMap.entrySet()) {
                ((Handler) next.getValue()).post(new Runnable(this) {
                    public void run() {
                        ((VolumeDialogController.Callbacks) next.getKey()).onDismissRequested(i);
                    }
                });
            }
        }

        public void onStateChanged(VolumeDialogController.State state) {
            long currentTimeMillis = System.currentTimeMillis();
            final VolumeDialogController.State copy = state.copy();
            for (final Map.Entry next : this.mCallbackMap.entrySet()) {
                ((Handler) next.getValue()).post(new Runnable(this) {
                    public void run() {
                        ((VolumeDialogController.Callbacks) next.getKey()).onStateChanged(copy);
                    }
                });
            }
            Events.writeState(currentTimeMillis, copy);
        }

        public void onLayoutDirectionChanged(final int i) {
            for (final Map.Entry next : this.mCallbackMap.entrySet()) {
                ((Handler) next.getValue()).post(new Runnable(this) {
                    public void run() {
                        ((VolumeDialogController.Callbacks) next.getKey()).onLayoutDirectionChanged(i);
                    }
                });
            }
        }

        public void onConfigurationChanged() {
            VolumeDialogControllerImpl.this.mState.activeStream = -1;
            for (final Map.Entry next : this.mCallbackMap.entrySet()) {
                ((Handler) next.getValue()).post(new Runnable(this) {
                    public void run() {
                        ((VolumeDialogController.Callbacks) next.getKey()).onConfigurationChanged();
                    }
                });
            }
        }

        public void onShowVibrateHint() {
            for (final Map.Entry next : this.mCallbackMap.entrySet()) {
                ((Handler) next.getValue()).post(new Runnable(this) {
                    public void run() {
                        ((VolumeDialogController.Callbacks) next.getKey()).onShowVibrateHint();
                    }
                });
            }
        }

        public void onShowSilentHint() {
            for (final Map.Entry next : this.mCallbackMap.entrySet()) {
                ((Handler) next.getValue()).post(new Runnable(this) {
                    public void run() {
                        ((VolumeDialogController.Callbacks) next.getKey()).onShowSilentHint();
                    }
                });
            }
        }

        public void onScreenOff() {
            for (final Map.Entry next : this.mCallbackMap.entrySet()) {
                ((Handler) next.getValue()).post(new Runnable(this) {
                    public void run() {
                        ((VolumeDialogController.Callbacks) next.getKey()).onScreenOff();
                    }
                });
            }
        }

        public void onShowSafetyWarning(final int i) {
            for (final Map.Entry next : this.mCallbackMap.entrySet()) {
                ((Handler) next.getValue()).post(new Runnable(this) {
                    public void run() {
                        ((VolumeDialogController.Callbacks) next.getKey()).onShowSafetyWarning(i);
                    }
                });
            }
        }

        public void onAccessibilityModeChanged(Boolean bool) {
            final boolean booleanValue = bool == null ? false : bool.booleanValue();
            for (final Map.Entry next : this.mCallbackMap.entrySet()) {
                ((Handler) next.getValue()).post(new Runnable(this) {
                    public void run() {
                        ((VolumeDialogController.Callbacks) next.getKey()).onAccessibilityModeChanged(Boolean.valueOf(booleanValue));
                    }
                });
            }
        }

        public void onVolumeChanged(final int i, final boolean z) {
            for (final Map.Entry next : this.mCallbackMap.entrySet()) {
                ((Handler) next.getValue()).post(new Runnable(this) {
                    public void run() {
                        ((VolumeDialogController.Callbacks) next.getKey()).onVolumeChanged(i, z);
                    }
                });
            }
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
            if (this.ZEN_MODE_URI.equals(uri) ? VolumeDialogControllerImpl.this.updateZenModeW() : false) {
                VolumeDialogControllerImpl.this.mCallbacks.onStateChanged(VolumeDialogControllerImpl.this.mState);
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
            intentFilter.addAction("android.media.RINGER_MODE_CHANGED");
            intentFilter.addAction("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION");
            intentFilter.addAction("android.media.STREAM_MUTE_CHANGED_ACTION");
            intentFilter.addAction("android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED");
            intentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            intentFilter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
            VolumeDialogControllerImpl.this.mContext.registerReceiver(this, intentFilter, (String) null, VolumeDialogControllerImpl.this.mWorker);
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean z = false;
            if (action.equals("android.media.VOLUME_CHANGED_ACTION")) {
                int intExtra = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
                int intExtra2 = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", -1);
                int intExtra3 = intent.getIntExtra("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", -1);
                if (D.BUG) {
                    String access$500 = VolumeDialogControllerImpl.TAG;
                    Log.d(access$500, "onReceive VOLUME_CHANGED_ACTION stream=" + intExtra + " level=" + intExtra2 + " oldLevel=" + intExtra3);
                }
                z = VolumeDialogControllerImpl.this.updateStreamLevelW(intExtra, intExtra2);
            } else if (action.equals("android.media.STREAM_DEVICES_CHANGED_ACTION")) {
                int intExtra4 = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
                int intExtra5 = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_DEVICES", -1);
                int intExtra6 = intent.getIntExtra("android.media.EXTRA_PREV_VOLUME_STREAM_DEVICES", -1);
                if (D.BUG) {
                    String access$5002 = VolumeDialogControllerImpl.TAG;
                    Log.d(access$5002, "onReceive STREAM_DEVICES_CHANGED_ACTION stream=" + intExtra4 + " devices=" + intExtra5 + " oldDevices=" + intExtra6);
                }
                Logger.i(VolumeDialogControllerImpl.TAG, "onReceive STREAM_DEVICES_CHANGED_ACTION");
                z = VolumeDialogControllerImpl.this.checkRoutedToBluetoothW(intExtra4) | VolumeDialogControllerImpl.this.onVolumeChangedW(intExtra4, 0);
            } else if (action.equals("android.media.RINGER_MODE_CHANGED")) {
                int intExtra7 = intent.getIntExtra("android.media.EXTRA_RINGER_MODE", -1);
                if (D.BUG) {
                    String access$5003 = VolumeDialogControllerImpl.TAG;
                    Log.d(access$5003, "onReceive RINGER_MODE_CHANGED_ACTION rm=" + Util.ringerModeToString(intExtra7));
                }
                z = VolumeDialogControllerImpl.this.updateRingerModeExternalW(intExtra7);
            } else if (action.equals("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION")) {
                int intExtra8 = intent.getIntExtra("android.media.EXTRA_RINGER_MODE", -1);
                if (D.BUG) {
                    String access$5004 = VolumeDialogControllerImpl.TAG;
                    Log.d(access$5004, "onReceive INTERNAL_RINGER_MODE_CHANGED_ACTION rm=" + Util.ringerModeToString(intExtra8));
                }
                z = VolumeDialogControllerImpl.this.updateRingerModeInternalW(intExtra8);
            } else if (action.equals("android.media.STREAM_MUTE_CHANGED_ACTION")) {
                int intExtra9 = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
                boolean booleanExtra = intent.getBooleanExtra("android.media.EXTRA_STREAM_VOLUME_MUTED", false);
                if (D.BUG) {
                    String access$5005 = VolumeDialogControllerImpl.TAG;
                    Log.d(access$5005, "onReceive STREAM_MUTE_CHANGED_ACTION stream=" + intExtra9 + " muted=" + booleanExtra);
                }
                z = VolumeDialogControllerImpl.this.updateStreamMuteW(intExtra9, booleanExtra);
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
                VolumeDialogControllerImpl.this.mCallbacks.onStateChanged(VolumeDialogControllerImpl.this.mState);
            }
        }
    }

    private final class MediaSessionsCallbacks implements MediaSessions.Callbacks {
        private int mNextStream;
        /* access modifiers changed from: private */
        public final HashMap<MediaSession.Token, Integer> mRemoteStreams;

        private MediaSessionsCallbacks() {
            this.mRemoteStreams = new HashMap<>();
            this.mNextStream = 100;
        }

        public void onRemoteUpdate(MediaSession.Token token, String str, MediaController.PlaybackInfo playbackInfo) {
            boolean z = true;
            if (!this.mRemoteStreams.containsKey(token)) {
                this.mRemoteStreams.put(token, Integer.valueOf(this.mNextStream));
                if (D.BUG) {
                    Log.d(VolumeDialogControllerImpl.TAG, "onRemoteUpdate: " + str + " is stream " + this.mNextStream);
                }
                this.mNextStream++;
            }
            int intValue = this.mRemoteStreams.get(token).intValue();
            boolean z2 = VolumeDialogControllerImpl.this.mState.states.indexOfKey(intValue) < 0;
            VolumeDialogController.StreamState access$3300 = VolumeDialogControllerImpl.this.streamStateW(intValue);
            access$3300.dynamic = true;
            access$3300.levelMin = 0;
            access$3300.levelMax = playbackInfo.getMaxVolume();
            if (access$3300.level != playbackInfo.getCurrentVolume()) {
                access$3300.lastLevel = access$3300.level;
                access$3300.level = playbackInfo.getCurrentVolume();
                z2 = true;
            }
            if (!Objects.equals(access$3300.remoteLabel, str)) {
                access$3300.nameRes = -1;
                access$3300.remoteLabel = str;
            } else {
                z = z2;
            }
            if (D.BUG) {
                Log.d(VolumeDialogControllerImpl.TAG, "onRemoteUpdate: " + str + " level: " + access$3300.level + " of levelMax:" + access$3300.levelMax);
            }
            if (z) {
                if (D.BUG) {
                    Log.d(VolumeDialogControllerImpl.TAG, "onRemoteUpdate: " + str + ": " + access$3300.level + " of " + access$3300.levelMax);
                }
                VolumeDialogControllerImpl.this.mCallbacks.onStateChanged(VolumeDialogControllerImpl.this.mState);
            }
        }

        public void onRemoteVolumeChanged(MediaSession.Token token, int i) {
            if (this.mRemoteStreams.get(token) != null) {
                int intValue = this.mRemoteStreams.get(token).intValue();
                boolean z = true;
                boolean z2 = (i & 1) != 0;
                if ((i & 4096) == 0) {
                    z = false;
                }
                boolean access$3400 = VolumeDialogControllerImpl.this.updateActiveStreamW(intValue);
                if (z2) {
                    access$3400 |= VolumeDialogControllerImpl.this.checkRoutedToBluetoothW(3);
                }
                if (access$3400) {
                    VolumeDialogControllerImpl.this.mCallbacks.onStateChanged(VolumeDialogControllerImpl.this.mState);
                }
                if (z2) {
                    VolumeDialogControllerImpl.this.mCallbacks.onShowRequested(2, z, 0);
                }
            }
        }

        public void onRemoteRemoved(MediaSession.Token token) {
            int intValue = this.mRemoteStreams.get(token).intValue();
            VolumeDialogControllerImpl.this.mState.states.remove(intValue);
            if (VolumeDialogControllerImpl.this.mState.activeStream == intValue) {
                boolean unused = VolumeDialogControllerImpl.this.updateActiveStreamW(-1);
            }
            VolumeDialogControllerImpl.this.mCallbacks.onStateChanged(VolumeDialogControllerImpl.this.mState);
        }

        public void setStreamVolume(int i, int i2) {
            MediaSession.Token findToken = findToken(i);
            if (findToken == null) {
                String access$500 = VolumeDialogControllerImpl.TAG;
                Log.w(access$500, "setStreamVolume: No token found for stream: " + i);
                return;
            }
            VolumeDialogControllerImpl.this.mMediaSessions.setVolume(findToken, i2);
        }

        private MediaSession.Token findToken(int i) {
            for (Map.Entry next : this.mRemoteStreams.entrySet()) {
                if (((Integer) next.getValue()).equals(Integer.valueOf(i))) {
                    return (MediaSession.Token) next.getKey();
                }
            }
            return null;
        }
    }
}
