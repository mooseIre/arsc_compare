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
import java.lang.Thread;
import java.util.LinkedList;

public class NotificationPlayer implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    /* access modifiers changed from: private */
    @GuardedBy({"mQueueAudioFocusLock"})
    public AudioManager mAudioManagerWithAudioFocus;
    /* access modifiers changed from: private */
    public final LinkedList<Command> mCmdQueue = new LinkedList<>();
    /* access modifiers changed from: private */
    public final Object mCompletionHandlingLock = new Object();
    @GuardedBy({"mCompletionHandlingLock"})
    private CreationAndCompletionThread mCompletionThread;
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
                monitor-enter(r7)
                com.android.systemui.media.NotificationPlayer$Command r0 = r7.mCmd     // Catch:{ all -> 0x0129 }
                android.content.Context r0 = r0.context     // Catch:{ all -> 0x0129 }
                java.lang.String r1 = "audio"
                java.lang.Object r0 = r0.getSystemService(r1)     // Catch:{ all -> 0x0129 }
                android.media.AudioManager r0 = (android.media.AudioManager) r0     // Catch:{ all -> 0x0129 }
                r1 = 0
                android.media.MediaPlayer r2 = new android.media.MediaPlayer     // Catch:{ Exception -> 0x00d9 }
                r2.<init>()     // Catch:{ Exception -> 0x00d9 }
                com.android.systemui.media.NotificationPlayer$Command r3 = r7.mCmd     // Catch:{ Exception -> 0x00d7 }
                android.media.AudioAttributes r3 = r3.attributes     // Catch:{ Exception -> 0x00d7 }
                if (r3 != 0) goto L_0x003c
                com.android.systemui.media.NotificationPlayer$Command r3 = r7.mCmd     // Catch:{ Exception -> 0x00d7 }
                android.media.AudioAttributes$Builder r4 = new android.media.AudioAttributes$Builder     // Catch:{ Exception -> 0x00d7 }
                r4.<init>()     // Catch:{ Exception -> 0x00d7 }
                r5 = 5
                android.media.AudioAttributes$Builder r4 = r4.setUsage(r5)     // Catch:{ Exception -> 0x00d7 }
                r5 = 4
                android.media.AudioAttributes$Builder r4 = r4.setContentType(r5)     // Catch:{ Exception -> 0x00d7 }
                android.media.AudioAttributes r4 = r4.build()     // Catch:{ Exception -> 0x00d7 }
                r3.attributes = r4     // Catch:{ Exception -> 0x00d7 }
            L_0x003c:
                com.android.systemui.media.NotificationPlayer$Command r3 = r7.mCmd     // Catch:{ Exception -> 0x00d7 }
                android.media.AudioAttributes r3 = r3.attributes     // Catch:{ Exception -> 0x00d7 }
                r2.setAudioAttributes(r3)     // Catch:{ Exception -> 0x00d7 }
                com.android.systemui.media.NotificationPlayer$Command r3 = r7.mCmd     // Catch:{ Exception -> 0x00d7 }
                android.content.Context r3 = r3.context     // Catch:{ Exception -> 0x00d7 }
                com.android.systemui.media.NotificationPlayer$Command r4 = r7.mCmd     // Catch:{ Exception -> 0x00d7 }
                android.net.Uri r4 = r4.uri     // Catch:{ Exception -> 0x00d7 }
                r2.setDataSource(r3, r4)     // Catch:{ Exception -> 0x00d7 }
                com.android.systemui.media.NotificationPlayer$Command r3 = r7.mCmd     // Catch:{ Exception -> 0x00d7 }
                boolean r3 = r3.looping     // Catch:{ Exception -> 0x00d7 }
                r2.setLooping(r3)     // Catch:{ Exception -> 0x00d7 }
                com.android.systemui.media.NotificationPlayer r3 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ Exception -> 0x00d7 }
                r2.setOnCompletionListener(r3)     // Catch:{ Exception -> 0x00d7 }
                com.android.systemui.media.NotificationPlayer r3 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ Exception -> 0x00d7 }
                r2.setOnErrorListener(r3)     // Catch:{ Exception -> 0x00d7 }
                r2.prepare()     // Catch:{ Exception -> 0x00d7 }
                com.android.systemui.media.NotificationPlayer$Command r3 = r7.mCmd     // Catch:{ Exception -> 0x00d7 }
                android.net.Uri r3 = r3.uri     // Catch:{ Exception -> 0x00d7 }
                if (r3 == 0) goto L_0x00bc
                com.android.systemui.media.NotificationPlayer$Command r3 = r7.mCmd     // Catch:{ Exception -> 0x00d7 }
                android.net.Uri r3 = r3.uri     // Catch:{ Exception -> 0x00d7 }
                java.lang.String r3 = r3.getEncodedPath()     // Catch:{ Exception -> 0x00d7 }
                if (r3 == 0) goto L_0x00bc
                com.android.systemui.media.NotificationPlayer$Command r3 = r7.mCmd     // Catch:{ Exception -> 0x00d7 }
                android.net.Uri r3 = r3.uri     // Catch:{ Exception -> 0x00d7 }
                java.lang.String r3 = r3.getEncodedPath()     // Catch:{ Exception -> 0x00d7 }
                int r3 = r3.length()     // Catch:{ Exception -> 0x00d7 }
                if (r3 <= 0) goto L_0x00bc
                boolean r3 = r0.isMusicActiveRemotely()     // Catch:{ Exception -> 0x00d7 }
                if (r3 != 0) goto L_0x00bc
                com.android.systemui.media.NotificationPlayer r3 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ Exception -> 0x00d7 }
                java.lang.Object r3 = r3.mQueueAudioFocusLock     // Catch:{ Exception -> 0x00d7 }
                monitor-enter(r3)     // Catch:{ Exception -> 0x00d7 }
                com.android.systemui.media.NotificationPlayer r4 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ all -> 0x00b9 }
                android.media.AudioManager r4 = r4.mAudioManagerWithAudioFocus     // Catch:{ all -> 0x00b9 }
                if (r4 != 0) goto L_0x00b7
                r4 = 3
                com.android.systemui.media.NotificationPlayer$Command r5 = r7.mCmd     // Catch:{ all -> 0x00b9 }
                boolean r5 = r5.looping     // Catch:{ all -> 0x00b9 }
                if (r5 == 0) goto L_0x009d
                r4 = 1
            L_0x009d:
                com.android.systemui.media.NotificationPlayer r5 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ all -> 0x00b9 }
                com.android.systemui.media.NotificationPlayer$Command r6 = r7.mCmd     // Catch:{ all -> 0x00b9 }
                android.media.AudioAttributes r6 = r6.attributes     // Catch:{ all -> 0x00b9 }
                int r6 = r0.getFocusRampTimeMs(r4, r6)     // Catch:{ all -> 0x00b9 }
                int unused = r5.mNotificationRampTimeMs = r6     // Catch:{ all -> 0x00b9 }
                com.android.systemui.media.NotificationPlayer$Command r5 = r7.mCmd     // Catch:{ all -> 0x00b9 }
                android.media.AudioAttributes r5 = r5.attributes     // Catch:{ all -> 0x00b9 }
                r6 = 0
                r0.requestAudioFocus(r1, r5, r4, r6)     // Catch:{ all -> 0x00b9 }
                com.android.systemui.media.NotificationPlayer r4 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ all -> 0x00b9 }
                android.media.AudioManager unused = r4.mAudioManagerWithAudioFocus = r0     // Catch:{ all -> 0x00b9 }
            L_0x00b7:
                monitor-exit(r3)     // Catch:{ all -> 0x00b9 }
                goto L_0x00bc
            L_0x00b9:
                r0 = move-exception
                monitor-exit(r3)     // Catch:{ all -> 0x00b9 }
                throw r0     // Catch:{ Exception -> 0x00d7 }
            L_0x00bc:
                com.android.systemui.media.NotificationPlayer r0 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ InterruptedException -> 0x00c7 }
                int r0 = r0.mNotificationRampTimeMs     // Catch:{ InterruptedException -> 0x00c7 }
                long r3 = (long) r0     // Catch:{ InterruptedException -> 0x00c7 }
                java.lang.Thread.sleep(r3)     // Catch:{ InterruptedException -> 0x00c7 }
                goto L_0x00d3
            L_0x00c7:
                r0 = move-exception
                com.android.systemui.media.NotificationPlayer r3 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ Exception -> 0x00d7 }
                java.lang.String r3 = r3.mTag     // Catch:{ Exception -> 0x00d7 }
                java.lang.String r4 = "Exception while sleeping to sync notification playback with ducking"
                android.util.Log.e(r3, r4, r0)     // Catch:{ Exception -> 0x00d7 }
            L_0x00d3:
                r2.start()     // Catch:{ Exception -> 0x00d7 }
                goto L_0x0106
            L_0x00d7:
                r0 = move-exception
                goto L_0x00db
            L_0x00d9:
                r0 = move-exception
                r2 = r1
            L_0x00db:
                if (r2 == 0) goto L_0x00e1
                r2.release()     // Catch:{ all -> 0x0129 }
                goto L_0x00e2
            L_0x00e1:
                r1 = r2
            L_0x00e2:
                com.android.systemui.media.NotificationPlayer r2 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ all -> 0x0129 }
                java.lang.String r2 = r2.mTag     // Catch:{ all -> 0x0129 }
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0129 }
                r3.<init>()     // Catch:{ all -> 0x0129 }
                java.lang.String r4 = "error loading sound for "
                r3.append(r4)     // Catch:{ all -> 0x0129 }
                com.android.systemui.media.NotificationPlayer$Command r4 = r7.mCmd     // Catch:{ all -> 0x0129 }
                android.net.Uri r4 = r4.uri     // Catch:{ all -> 0x0129 }
                r3.append(r4)     // Catch:{ all -> 0x0129 }
                java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x0129 }
                android.util.Log.w(r2, r3, r0)     // Catch:{ all -> 0x0129 }
                com.android.systemui.media.NotificationPlayer r0 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ all -> 0x0129 }
                r0.abandonAudioFocusAfterError()     // Catch:{ all -> 0x0129 }
                r2 = r1
            L_0x0106:
                com.android.systemui.media.NotificationPlayer r0 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ all -> 0x0129 }
                java.lang.Object r0 = r0.mPlayerLock     // Catch:{ all -> 0x0129 }
                monitor-enter(r0)     // Catch:{ all -> 0x0129 }
                com.android.systemui.media.NotificationPlayer r1 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ all -> 0x0126 }
                android.media.MediaPlayer r1 = r1.mPlayer     // Catch:{ all -> 0x0126 }
                com.android.systemui.media.NotificationPlayer r3 = com.android.systemui.media.NotificationPlayer.this     // Catch:{ all -> 0x0126 }
                android.media.MediaPlayer unused = r3.mPlayer = r2     // Catch:{ all -> 0x0126 }
                monitor-exit(r0)     // Catch:{ all -> 0x0126 }
                if (r1 == 0) goto L_0x011e
                r1.release()     // Catch:{ all -> 0x0129 }
            L_0x011e:
                r7.notify()     // Catch:{ all -> 0x0129 }
                monitor-exit(r7)     // Catch:{ all -> 0x0129 }
                android.os.Looper.loop()
                return
            L_0x0126:
                r1 = move-exception
                monitor-exit(r0)     // Catch:{ all -> 0x0126 }
                throw r1     // Catch:{ all -> 0x0129 }
            L_0x0129:
                r0 = move-exception
                monitor-exit(r7)     // Catch:{ all -> 0x0129 }
                throw r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.media.NotificationPlayer.CreationAndCompletionThread.run():void");
        }
    }

    /* access modifiers changed from: private */
    public void abandonAudioFocusAfterError() {
        synchronized (this.mQueueAudioFocusLock) {
            if (this.mAudioManagerWithAudioFocus != null) {
                this.mAudioManagerWithAudioFocus.abandonAudioFocus((AudioManager.OnAudioFocusChangeListener) null);
                this.mAudioManagerWithAudioFocus = null;
            }
        }
    }

    /* access modifiers changed from: private */
    public void startSound(Command command) {
        try {
            synchronized (this.mCompletionHandlingLock) {
                if (!(this.mLooper == null || this.mLooper.getThread().getState() == Thread.State.TERMINATED)) {
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
                String str = this.mTag;
                Log.w(str, "Notification sound delayed by " + uptimeMillis + "msecs");
            }
        } catch (Exception e) {
            String str2 = this.mTag;
            Log.w(str2, "error loading sound for " + command.uri, e);
        }
    }

    private final class CmdThread extends Thread {
        CmdThread() {
            super("NotificationPlayer-" + NotificationPlayer.this.mTag);
        }

        public void run() {
            Command command;
            MediaPlayer access$700;
            while (true) {
                synchronized (NotificationPlayer.this.mCmdQueue) {
                    command = (Command) NotificationPlayer.this.mCmdQueue.removeFirst();
                }
                int i = command.code;
                if (i == 1) {
                    NotificationPlayer.this.startSound(command);
                } else if (i == 2) {
                    synchronized (NotificationPlayer.this.mPlayerLock) {
                        access$700 = NotificationPlayer.this.mPlayer;
                        MediaPlayer unused = NotificationPlayer.this.mPlayer = null;
                    }
                    if (access$700 != null) {
                        long uptimeMillis = SystemClock.uptimeMillis() - command.requestTime;
                        if (uptimeMillis > 1000) {
                            String access$400 = NotificationPlayer.this.mTag;
                            Log.w(access$400, "Notification stop delayed by " + uptimeMillis + "msecs");
                        }
                        try {
                            access$700.stop();
                        } catch (Exception unused2) {
                        }
                        access$700.release();
                        synchronized (NotificationPlayer.this.mQueueAudioFocusLock) {
                            if (NotificationPlayer.this.mAudioManagerWithAudioFocus != null) {
                                NotificationPlayer.this.mAudioManagerWithAudioFocus.abandonAudioFocus((AudioManager.OnAudioFocusChangeListener) null);
                                AudioManager unused3 = NotificationPlayer.this.mAudioManagerWithAudioFocus = null;
                            }
                        }
                        synchronized (NotificationPlayer.this.mCompletionHandlingLock) {
                            if (!(NotificationPlayer.this.mLooper == null || NotificationPlayer.this.mLooper.getThread().getState() == Thread.State.TERMINATED)) {
                                NotificationPlayer.this.mLooper.quit();
                            }
                        }
                    } else {
                        Log.w(NotificationPlayer.this.mTag, "STOP command without a player");
                    }
                }
                synchronized (NotificationPlayer.this.mCmdQueue) {
                    if (NotificationPlayer.this.mCmdQueue.size() == 0) {
                        CmdThread unused4 = NotificationPlayer.this.mThread = null;
                        NotificationPlayer.this.releaseWakeLock();
                        return;
                    }
                }
            }
            while (true) {
            }
        }
    }

    public void onCompletion(MediaPlayer mediaPlayer) {
        synchronized (this.mQueueAudioFocusLock) {
            if (this.mAudioManagerWithAudioFocus != null) {
                this.mAudioManagerWithAudioFocus.abandonAudioFocus((AudioManager.OnAudioFocusChangeListener) null);
                this.mAudioManagerWithAudioFocus = null;
            }
        }
        synchronized (this.mCmdQueue) {
            synchronized (this.mCompletionHandlingLock) {
                if (this.mCmdQueue.size() == 0) {
                    if (this.mLooper != null) {
                        this.mLooper.quit();
                    }
                    this.mCompletionThread = null;
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
        if (wakeLock != null) {
            wakeLock.acquire();
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mCmdQueue"})
    public void releaseWakeLock() {
        PowerManager.WakeLock wakeLock = this.mWakeLock;
        if (wakeLock != null) {
            wakeLock.release();
        }
    }
}
