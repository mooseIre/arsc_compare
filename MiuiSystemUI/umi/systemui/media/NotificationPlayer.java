package com.android.systemui.media;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.systemui.Constants;
import java.lang.Thread;
import java.util.LinkedList;

public class NotificationPlayer implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Constants.DEBUG;
    /* access modifiers changed from: private */
    @GuardedBy({"mQueueAudioFocusLock"})
    public AudioManager mAudioManagerWithAudioFocus;
    /* access modifiers changed from: private */
    public final LinkedList<Command> mCmdQueue = new LinkedList<>();
    /* access modifiers changed from: private */
    public final Object mCompletionHandlingLock = new Object();
    @GuardedBy({"mCompletionHandlingLock"})
    private CreationAndCompletionThread mCompletionThread;
    private boolean mLockAcquired = false;
    /* access modifiers changed from: private */
    @GuardedBy({"mCompletionHandlingLock"})
    public Looper mLooper;
    /* access modifiers changed from: private */
    public int mNotificationRampTimeMs = 0;
    /* access modifiers changed from: private */
    @GuardedBy({"mPlayerLock"})
    public MediaPlayer mPlayer;
    /* access modifiers changed from: private */
    public final Object mPlayerLock = new Object();
    /* access modifiers changed from: private */
    public final Object mQueueAudioFocusLock = new Object();
    private int mState = 2;
    /* access modifiers changed from: private */
    public String mTag;
    /* access modifiers changed from: private */
    @GuardedBy({"mCmdQueue"})
    public CmdThread mThread;
    @GuardedBy({"mCmdQueue"})
    private PowerManager.WakeLock mWakeLock;

    private static final class Command {
        AudioAttributes attributes;
        int code;
        Context context;
        boolean looping;
        long requestTime;
        Uri uri;

        private Command() {
        }

        public String toString() {
            return "{ code=" + this.code + " looping=" + this.looping + " attributes=" + this.attributes + " uri=" + this.uri + " }";
        }
    }

    private final class CreationAndCompletionThread extends Thread {
        public Command mCmd;

        public CreationAndCompletionThread(Command command) {
            this.mCmd = command;
        }

        /*  JADX ERROR: IndexOutOfBoundsException in pass: RegionMakerVisitor
            java.lang.IndexOutOfBoundsException: Index 0 out of bounds for length 0
            	at java.base/jdk.internal.util.Preconditions.outOfBounds(Preconditions.java:64)
            	at java.base/jdk.internal.util.Preconditions.outOfBoundsCheckIndex(Preconditions.java:70)
            	at java.base/jdk.internal.util.Preconditions.checkIndex(Preconditions.java:248)
            	at java.base/java.util.Objects.checkIndex(Objects.java:372)
            	at java.base/java.util.ArrayList.get(ArrayList.java:459)
            	at jadx.core.dex.nodes.InsnNode.getArg(InsnNode.java:101)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:611)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:561)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
            	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
            	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:693)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:123)
            	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
            	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:598)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
            	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
            	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:49)
            */
        public void run() {
            /*
                r7 = this;
                android.os.Looper.prepare()
                com.android.systemui.media.NotificationPlayer r0 = com.android.systemui.media.NotificationPlayer.this
                android.os.Looper r1 = android.os.Looper.myLooper()
                android.os.Looper unused = r0.mLooper = r1
                boolean r0 = com.android.systemui.media.NotificationPlayer.DEBUG
                if (r0 == 0) goto L_0x0032
                com.android.systemui.media.NotificationPlayer r0 = com.android.systemui.media.NotificationPlayer.this
                java.lang.String r0 = r0.mTag
                java.lang.StringBuilder r1 = new java.lang.StringBuilder
                r1.<init>()
                java.lang.String r2 = "in run: new looper "
                r1.append(r2)
                com.android.systemui.media.NotificationPlayer r2 = com.android.systemui.media.NotificationPlayer.this
                android.os.Looper r2 = r2.mLooper
                r1.append(r2)
                java.lang.String r1 = r1.toString()
                android.util.Log.d(r0, r1)
            L_0x0032:
                monitor-enter(r7)
                com.android.systemui.media.NotificationPlayer$Command r0 = r7.mCmd     // Catch:{ all -> 0x01bf }
                android.content.Context r0 = r0.context     // Catch:{ all -> 0x01bf }
                java.lang.String r1 = "audio"
                java.lang.Object r0 = r0.getSystemService(r1)     // Catch:{ all -> 0x01bf }
                android.media.AudioManager r0 = (android.media.AudioManager) r0     // Catch:{ all -> 0x01bf }
                r1 = 0
                android.media.MediaPlayer r2 = new android.media.MediaPlayer     // Catch:{ Exception -> 0x015e }
                r2.<init>()     // Catch:{ Exception -> 0x015e }
                com.android.systemui.media.NotificationPlayer$Command r3 = r7.mCmd     // Catch:{ Exception -> 0x015c }
                android.media.AudioAttributes r3 = r3.attributes     // Catch:{ Exception -> 0x015c }
                if (r3 != 0) goto L_0x0062
                com.android.systemui.media.NotificationPlayer$Command r3 = r7.mCmd     // Catch:{ Exception -> 0x015c }
                android.media.AudioAttributes$Builder r4 = new android.media.AudioAttributes$Builder     // Catch:{ Exception -> 0x015c }
                r4.<init>()     // Catch:{ Exception -> 0x015c }
                r5 = 5
                android.media.AudioAttributes$Builder r4 = r4.setUsage(r5)     // Catch:{ Exception -> 0x015c }
                r5 = 4
                android.media.AudioAttributes$Builder r4 = r4.setContentType(r5)     // Catch:{ Exception -> 0x015c }
                android.media.AudioAttributes r4 = r4.build()     // Catch:{ Exception -> 0x015c }
                r3.attributes = r4     // Catch:{ Exception -> 0x015c }
            L_0x0062:
                com.android.systemui.media.NotificationPlayer$Command r3 = r7.mCmd     // Catch:{ Exception -> 0x015c }
                android.media.AudioAttributes r3 = r3.attributes     // Catch:{ Exception -> 0x015c }
                r2.setAudioAttributes(r3)     // Catch:{ Exception -> 0x015c }
                com.android.systemui.media.NotificationPlayer$Command r3 = r7.mCmd     // Catch:{ Exception -> 0x015c }
                android.content.Context r3 = r3.context     // Catch:{ Exception -> 0x015c }
                com.android.systemui.media.NotificationPlayer$Command r4 = r7.mCmd     // Catch:{ Exception -> 0x015c }
                android.net.Uri r4 = r4.uri     // Catch:{ Exception -> 0x015c }
                r2.setDataSource(r3, r4)     // Catch:{ Exception -> 0x015c }
                com.android.systemui.media.NotificationPlayer$Command r3 = r7.mCmd     // Catch:{ Exception -> 0x015c }
                boolean r3 = r3.looping     // Catch:{ Exception -> 0x015c }
                r2.setLooping(r3)     // Catch:{ Exception -> 0x015c }
                com.android.systemui.media.NotificationPlayer r3 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ Exception -> 0x015c }
                r2.setOnCompletionListener(r3)     // Catch:{ Exception -> 0x015c }
                com.android.systemui.media.NotificationPlayer r3 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ Exception -> 0x015c }
                r2.setOnErrorListener(r3)     // Catch:{ Exception -> 0x015c }
                r2.prepare()     // Catch:{ Exception -> 0x015c }
                com.android.systemui.media.NotificationPlayer$Command r3 = r7.mCmd     // Catch:{ Exception -> 0x015c }
                android.net.Uri r3 = r3.uri     // Catch:{ Exception -> 0x015c }
                if (r3 == 0) goto L_0x0105
                com.android.systemui.media.NotificationPlayer$Command r3 = r7.mCmd     // Catch:{ Exception -> 0x015c }
                android.net.Uri r3 = r3.uri     // Catch:{ Exception -> 0x015c }
                java.lang.String r3 = r3.getEncodedPath()     // Catch:{ Exception -> 0x015c }
                if (r3 == 0) goto L_0x0105
                com.android.systemui.media.NotificationPlayer$Command r3 = r7.mCmd     // Catch:{ Exception -> 0x015c }
                android.net.Uri r3 = r3.uri     // Catch:{ Exception -> 0x015c }
                java.lang.String r3 = r3.getEncodedPath()     // Catch:{ Exception -> 0x015c }
                int r3 = r3.length()     // Catch:{ Exception -> 0x015c }
                if (r3 <= 0) goto L_0x0105
                boolean r3 = r0.isMusicActiveRemotely()     // Catch:{ Exception -> 0x015c }
                if (r3 != 0) goto L_0x0105
                com.android.systemui.media.NotificationPlayer r3 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ Exception -> 0x015c }
                java.lang.Object r3 = r3.mQueueAudioFocusLock     // Catch:{ Exception -> 0x015c }
                monitor-enter(r3)     // Catch:{ Exception -> 0x015c }
                com.android.systemui.media.NotificationPlayer r4 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ all -> 0x0102 }
                android.media.AudioManager r4 = r4.mAudioManagerWithAudioFocus     // Catch:{ all -> 0x0102 }
                if (r4 != 0) goto L_0x00ef
                boolean r4 = com.android.systemui.media.NotificationPlayer.DEBUG     // Catch:{ all -> 0x0102 }
                if (r4 == 0) goto L_0x00cc
                com.android.systemui.media.NotificationPlayer r4 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ all -> 0x0102 }
                java.lang.String r4 = r4.mTag     // Catch:{ all -> 0x0102 }
                java.lang.String r5 = "requesting AudioFocus"
                android.util.Log.d(r4, r5)     // Catch:{ all -> 0x0102 }
            L_0x00cc:
                r4 = 3
                com.android.systemui.media.NotificationPlayer$Command r5 = r7.mCmd     // Catch:{ all -> 0x0102 }
                boolean r5 = r5.looping     // Catch:{ all -> 0x0102 }
                if (r5 == 0) goto L_0x00d4
                r4 = 1
            L_0x00d4:
                com.android.systemui.media.NotificationPlayer r5 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ all -> 0x0102 }
                com.android.systemui.media.NotificationPlayer$Command r6 = r7.mCmd     // Catch:{ all -> 0x0102 }
                android.media.AudioAttributes r6 = r6.attributes     // Catch:{ all -> 0x0102 }
                int r6 = r0.getFocusRampTimeMs(r4, r6)     // Catch:{ all -> 0x0102 }
                int unused = r5.mNotificationRampTimeMs = r6     // Catch:{ all -> 0x0102 }
                com.android.systemui.media.NotificationPlayer$Command r5 = r7.mCmd     // Catch:{ all -> 0x0102 }
                android.media.AudioAttributes r5 = r5.attributes     // Catch:{ all -> 0x0102 }
                r6 = 0
                r0.requestAudioFocus(r1, r5, r4, r6)     // Catch:{ all -> 0x0102 }
                com.android.systemui.media.NotificationPlayer r4 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ all -> 0x0102 }
                android.media.AudioManager unused = r4.mAudioManagerWithAudioFocus = r0     // Catch:{ all -> 0x0102 }
                goto L_0x0100
            L_0x00ef:
                boolean r0 = com.android.systemui.media.NotificationPlayer.DEBUG     // Catch:{ all -> 0x0102 }
                if (r0 == 0) goto L_0x0100
                com.android.systemui.media.NotificationPlayer r0 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ all -> 0x0102 }
                java.lang.String r0 = r0.mTag     // Catch:{ all -> 0x0102 }
                java.lang.String r4 = "AudioFocus was previously requested"
                android.util.Log.d(r0, r4)     // Catch:{ all -> 0x0102 }
            L_0x0100:
                monitor-exit(r3)     // Catch:{ all -> 0x0102 }
                goto L_0x0105
            L_0x0102:
                r0 = move-exception
                monitor-exit(r3)     // Catch:{ all -> 0x0102 }
                throw r0     // Catch:{ Exception -> 0x015c }
            L_0x0105:
                boolean r0 = com.android.systemui.media.NotificationPlayer.DEBUG     // Catch:{ Exception -> 0x015c }
                if (r0 == 0) goto L_0x0130
                com.android.systemui.media.NotificationPlayer r0 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ Exception -> 0x015c }
                java.lang.String r0 = r0.mTag     // Catch:{ Exception -> 0x015c }
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x015c }
                r3.<init>()     // Catch:{ Exception -> 0x015c }
                java.lang.String r4 = "notification will be delayed by "
                r3.append(r4)     // Catch:{ Exception -> 0x015c }
                com.android.systemui.media.NotificationPlayer r4 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ Exception -> 0x015c }
                int r4 = r4.mNotificationRampTimeMs     // Catch:{ Exception -> 0x015c }
                r3.append(r4)     // Catch:{ Exception -> 0x015c }
                java.lang.String r4 = "ms"
                r3.append(r4)     // Catch:{ Exception -> 0x015c }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x015c }
                android.util.Log.d(r0, r3)     // Catch:{ Exception -> 0x015c }
            L_0x0130:
                com.android.systemui.media.NotificationPlayer r0 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ InterruptedException -> 0x013b }
                int r0 = r0.mNotificationRampTimeMs     // Catch:{ InterruptedException -> 0x013b }
                long r3 = (long) r0     // Catch:{ InterruptedException -> 0x013b }
                java.lang.Thread.sleep(r3)     // Catch:{ InterruptedException -> 0x013b }
                goto L_0x0147
            L_0x013b:
                r0 = move-exception
                com.android.systemui.media.NotificationPlayer r3 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ Exception -> 0x015c }
                java.lang.String r3 = r3.mTag     // Catch:{ Exception -> 0x015c }
                java.lang.String r4 = "Exception while sleeping to sync notification playback with ducking"
                android.util.Log.e(r3, r4, r0)     // Catch:{ Exception -> 0x015c }
            L_0x0147:
                r2.start()     // Catch:{ Exception -> 0x015c }
                boolean r0 = com.android.systemui.media.NotificationPlayer.DEBUG     // Catch:{ Exception -> 0x015c }
                if (r0 == 0) goto L_0x018b
                com.android.systemui.media.NotificationPlayer r0 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ Exception -> 0x015c }
                java.lang.String r0 = r0.mTag     // Catch:{ Exception -> 0x015c }
                java.lang.String r3 = "player.start"
                android.util.Log.d(r0, r3)     // Catch:{ Exception -> 0x015c }
                goto L_0x018b
            L_0x015c:
                r0 = move-exception
                goto L_0x0160
            L_0x015e:
                r0 = move-exception
                r2 = r1
            L_0x0160:
                if (r2 == 0) goto L_0x0166
                r2.release()     // Catch:{ all -> 0x01bf }
                goto L_0x0167
            L_0x0166:
                r1 = r2
            L_0x0167:
                com.android.systemui.media.NotificationPlayer r2 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ all -> 0x01bf }
                java.lang.String r2 = r2.mTag     // Catch:{ all -> 0x01bf }
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x01bf }
                r3.<init>()     // Catch:{ all -> 0x01bf }
                java.lang.String r4 = "error loading sound for "
                r3.append(r4)     // Catch:{ all -> 0x01bf }
                com.android.systemui.media.NotificationPlayer$Command r4 = r7.mCmd     // Catch:{ all -> 0x01bf }
                android.net.Uri r4 = r4.uri     // Catch:{ all -> 0x01bf }
                r3.append(r4)     // Catch:{ all -> 0x01bf }
                java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x01bf }
                android.util.Log.w(r2, r3, r0)     // Catch:{ all -> 0x01bf }
                com.android.systemui.media.NotificationPlayer r0 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ all -> 0x01bf }
                r0.abandonAudioFocusAfterError()     // Catch:{ all -> 0x01bf }
                r2 = r1
            L_0x018b:
                com.android.systemui.media.NotificationPlayer r0 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ all -> 0x01bf }
                java.lang.Object r0 = r0.mPlayerLock     // Catch:{ all -> 0x01bf }
                monitor-enter(r0)     // Catch:{ all -> 0x01bf }
                com.android.systemui.media.NotificationPlayer r1 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ all -> 0x01bc }
                android.media.MediaPlayer r1 = r1.mPlayer     // Catch:{ all -> 0x01bc }
                com.android.systemui.media.NotificationPlayer r3 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ all -> 0x01bc }
                android.media.MediaPlayer unused = r3.mPlayer = r2     // Catch:{ all -> 0x01bc }
                monitor-exit(r0)     // Catch:{ all -> 0x01bc }
                if (r1 == 0) goto L_0x01b4
                boolean r0 = com.android.systemui.media.NotificationPlayer.DEBUG     // Catch:{ all -> 0x01bf }
                if (r0 == 0) goto L_0x01b1
                com.android.systemui.media.NotificationPlayer r0 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ all -> 0x01bf }
                java.lang.String r0 = r0.mTag     // Catch:{ all -> 0x01bf }
                java.lang.String r2 = "mPlayer.release"
                android.util.Log.d(r0, r2)     // Catch:{ all -> 0x01bf }
            L_0x01b1:
                r1.release()     // Catch:{ all -> 0x01bf }
            L_0x01b4:
                r7.notify()     // Catch:{ all -> 0x01bf }
                monitor-exit(r7)     // Catch:{ all -> 0x01bf }
                android.os.Looper.loop()
                return
            L_0x01bc:
                r1 = move-exception
                monitor-exit(r0)     // Catch:{ all -> 0x01bc }
                throw r1     // Catch:{ all -> 0x01bf }
            L_0x01bf:
                r0 = move-exception
                monitor-exit(r7)     // Catch:{ all -> 0x01bf }
                throw r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.media.NotificationPlayer.CreationAndCompletionThread.run():void");
        }
    }

    /* access modifiers changed from: private */
    public void abandonAudioFocusAfterError() {
        synchronized (this.mQueueAudioFocusLock) {
            if (this.mAudioManagerWithAudioFocus != null) {
                if (DEBUG) {
                    Log.d(this.mTag, "abandoning focus after playback error");
                }
                this.mAudioManagerWithAudioFocus.abandonAudioFocus((AudioManager.OnAudioFocusChangeListener) null);
                this.mAudioManagerWithAudioFocus = null;
            }
        }
    }

    /* access modifiers changed from: private */
    public void startSound(Command command) {
        try {
            if (DEBUG) {
                Log.d(this.mTag, "startSound()");
            }
            synchronized (this.mCompletionHandlingLock) {
                if (!(this.mLooper == null || this.mLooper.getThread().getState() == Thread.State.TERMINATED)) {
                    if (DEBUG) {
                        String str = this.mTag;
                        Log.d(str, "in startSound quitting looper " + this.mLooper);
                    }
                    this.mLooper.quit();
                }
                CreationAndCompletionThread creationAndCompletionThread = new CreationAndCompletionThread(command);
                this.mCompletionThread = creationAndCompletionThread;
                synchronized (creationAndCompletionThread) {
                    this.mCompletionThread.start();
                    this.mCompletionThread.wait();
                }
            }
            long uptimeMillis = SystemClock.uptimeMillis() - command.requestTime;
            if (uptimeMillis > 1000) {
                String str2 = this.mTag;
                Log.w(str2, "Notification sound delayed by " + uptimeMillis + "msecs");
            }
        } catch (Exception e) {
            String str3 = this.mTag;
            Log.w(str3, "error loading sound for " + command.uri, e);
        }
    }

    private final class CmdThread extends Thread {
        CmdThread() {
            super("NotificationPlayer-" + NotificationPlayer.this.mTag);
        }

        public void run() {
            Command command;
            MediaPlayer access$800;
            while (true) {
                synchronized (NotificationPlayer.this.mCmdQueue) {
                    if (NotificationPlayer.DEBUG) {
                        Log.d(NotificationPlayer.this.mTag, "RemoveFirst");
                    }
                    command = (Command) NotificationPlayer.this.mCmdQueue.removeFirst();
                }
                int i = command.code;
                if (i == 1) {
                    if (NotificationPlayer.DEBUG) {
                        Log.d(NotificationPlayer.this.mTag, "PLAY");
                    }
                    NotificationPlayer.this.startSound(command);
                    synchronized (NotificationPlayer.this.mCmdQueue) {
                        if (NotificationPlayer.this.mCmdQueue.size() == 0) {
                            CmdThread unused = NotificationPlayer.this.mThread = null;
                            return;
                        }
                    }
                } else if (i == 2) {
                    if (NotificationPlayer.DEBUG) {
                        Log.d(NotificationPlayer.this.mTag, "STOP");
                    }
                    synchronized (NotificationPlayer.this.mPlayerLock) {
                        access$800 = NotificationPlayer.this.mPlayer;
                        MediaPlayer unused2 = NotificationPlayer.this.mPlayer = null;
                    }
                    if (access$800 != null) {
                        long uptimeMillis = SystemClock.uptimeMillis() - command.requestTime;
                        if (uptimeMillis > 1000) {
                            String access$200 = NotificationPlayer.this.mTag;
                            Log.w(access$200, "Notification stop delayed by " + uptimeMillis + "msecs");
                        }
                        try {
                            access$800.stop();
                        } catch (Exception unused3) {
                        }
                        access$800.release();
                        synchronized (NotificationPlayer.this.mQueueAudioFocusLock) {
                            if (NotificationPlayer.this.mAudioManagerWithAudioFocus != null) {
                                if (NotificationPlayer.DEBUG) {
                                    Log.d(NotificationPlayer.this.mTag, "in STOP: abandonning AudioFocus");
                                }
                                NotificationPlayer.this.mAudioManagerWithAudioFocus.abandonAudioFocus((AudioManager.OnAudioFocusChangeListener) null);
                                AudioManager unused4 = NotificationPlayer.this.mAudioManagerWithAudioFocus = null;
                            }
                        }
                        synchronized (NotificationPlayer.this.mCompletionHandlingLock) {
                            if (!(NotificationPlayer.this.mLooper == null || NotificationPlayer.this.mLooper.getThread().getState() == Thread.State.TERMINATED)) {
                                if (NotificationPlayer.DEBUG) {
                                    String access$2002 = NotificationPlayer.this.mTag;
                                    Log.d(access$2002, "in STOP: quitting looper " + NotificationPlayer.this.mLooper);
                                }
                                NotificationPlayer.this.mLooper.quit();
                            }
                        }
                    } else {
                        Log.w(NotificationPlayer.this.mTag, "STOP command without a player");
                    }
                    synchronized (NotificationPlayer.this.mCmdQueue) {
                        if (NotificationPlayer.this.mCmdQueue.size() == 0) {
                            CmdThread unused5 = NotificationPlayer.this.mThread = null;
                            NotificationPlayer.this.releaseWakeLock();
                            return;
                        }
                    }
                } else {
                    continue;
                }
            }
            while (true) {
            }
        }
    }

    public void onCompletion(MediaPlayer mediaPlayer) {
        synchronized (this.mQueueAudioFocusLock) {
            if (this.mAudioManagerWithAudioFocus != null) {
                if (DEBUG) {
                    Log.d(this.mTag, "onCompletion() abandonning AudioFocus");
                }
                this.mAudioManagerWithAudioFocus.abandonAudioFocus((AudioManager.OnAudioFocusChangeListener) null);
                this.mAudioManagerWithAudioFocus = null;
            } else if (DEBUG) {
                Log.d(this.mTag, "onCompletion() no need to abandon AudioFocus");
            }
        }
        synchronized (this.mCmdQueue) {
            synchronized (this.mCompletionHandlingLock) {
                if (DEBUG) {
                    String str = this.mTag;
                    Log.d(str, "onCompletion queue size=" + this.mCmdQueue.size());
                }
                if (this.mCmdQueue.size() == 0) {
                    if (this.mLooper != null) {
                        if (DEBUG) {
                            String str2 = this.mTag;
                            Log.d(str2, "in onCompletion quitting looper " + this.mLooper);
                        }
                        this.mLooper.quit();
                    }
                    this.mCompletionThread = null;
                    releaseWakeLock();
                }
            }
        }
        synchronized (this.mPlayerLock) {
            if (mediaPlayer == this.mPlayer) {
                this.mPlayer = null;
            }
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
        String str = this.mTag;
        Log.e(str, "error " + i + " (extra=" + i2 + ") playing notification");
        onCompletion(mediaPlayer);
        return true;
    }

    public NotificationPlayer(String str) {
        if (str != null) {
            this.mTag = str;
        } else {
            this.mTag = "NotificationPlayer";
        }
    }

    public void play(Context context, Uri uri, boolean z, AudioAttributes audioAttributes) {
        if (DEBUG) {
            String str = this.mTag;
            Log.d(str, "play uri=" + uri.toString());
        }
        Command command = new Command();
        command.requestTime = SystemClock.uptimeMillis();
        command.code = 1;
        command.context = context;
        command.uri = uri;
        command.looping = z;
        command.attributes = audioAttributes;
        synchronized (this.mCmdQueue) {
            enqueueLocked(command);
            this.mState = 1;
        }
    }

    public void stop() {
        if (DEBUG) {
            Log.d(this.mTag, "stop");
        }
        synchronized (this.mCmdQueue) {
            if (this.mState != 2) {
                Command command = new Command();
                command.requestTime = SystemClock.uptimeMillis();
                command.code = 2;
                enqueueLocked(command);
                this.mState = 2;
            }
        }
    }

    @GuardedBy({"mCmdQueue"})
    private void enqueueLocked(Command command) {
        this.mCmdQueue.add(command);
        if (this.mThread == null) {
            acquireWakeLock();
            CmdThread cmdThread = new CmdThread();
            this.mThread = cmdThread;
            cmdThread.start();
        }
    }

    public void setUsesWakeLock(Context context) {
        synchronized (this.mCmdQueue) {
            if (this.mWakeLock == null && this.mThread == null) {
                this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, this.mTag);
            } else {
                throw new RuntimeException("assertion failed mWakeLock=" + this.mWakeLock + " mThread=" + this.mThread);
            }
        }
    }

    @GuardedBy({"mCmdQueue"})
    private void acquireWakeLock() {
        PowerManager.WakeLock wakeLock = this.mWakeLock;
        if (wakeLock != null && !this.mLockAcquired) {
            this.mLockAcquired = true;
            wakeLock.acquire();
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mCmdQueue"})
    public void releaseWakeLock() {
        PowerManager.WakeLock wakeLock = this.mWakeLock;
        if (wakeLock != null && this.mLockAcquired) {
            wakeLock.release();
            this.mLockAcquired = false;
        }
    }
}
