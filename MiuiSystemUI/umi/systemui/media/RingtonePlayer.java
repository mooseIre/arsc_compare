package com.android.systemui.media;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.IAudioService;
import android.media.IRingtonePlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.VolumeShaper;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import com.android.systemui.Constants;
import com.android.systemui.SystemUI;
import com.android.systemui.plugins.R;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;

public class RingtonePlayer extends SystemUI {
    /* access modifiers changed from: private */
    public static final Uri INCALL_NOTIFICATION_URI = Uri.parse("file:///system/media/audio/ui/InCallNotification.ogg");
    /* access modifiers changed from: private */
    public static final boolean LOGD = Constants.DEBUG;
    /* access modifiers changed from: private */
    public static final Uri Q_INCALL_NOTIFICATION_URI = Uri.parse("file:///product/media/audio/ui/InCallNotification.ogg");
    /* access modifiers changed from: private */
    public final NotificationPlayer mAsyncPlayer = new NotificationPlayer("RingtonePlayer");
    private IAudioService mAudioService;
    private IRingtonePlayer mCallback = new IRingtonePlayer.Stub() {
        public void play(IBinder iBinder, Uri uri, AudioAttributes audioAttributes, float f, boolean z) throws RemoteException {
            playWithVolumeShaping(iBinder, uri, audioAttributes, f, z, (VolumeShaper.Configuration) null);
        }

        public void playWithVolumeShaping(IBinder iBinder, Uri uri, AudioAttributes audioAttributes, float f, boolean z, VolumeShaper.Configuration configuration) throws RemoteException {
            Client client;
            if (isInCallNotification(uri)) {
                if (RingtonePlayer.this.mPlayInCallNotification) {
                    uri = RingtonePlayer.INCALL_NOTIFICATION_URI;
                } else {
                    return;
                }
            }
            Uri uri2 = uri;
            if (RingtonePlayer.LOGD) {
                Log.d("RingtonePlayer", "play(token=" + iBinder + ", uri=" + uri2 + ", aa=" + audioAttributes + ", uid=" + Binder.getCallingUid() + ")");
            }
            synchronized (RingtonePlayer.this.mClients) {
                client = (Client) RingtonePlayer.this.mClients.get(iBinder);
                if (client == null) {
                    Client client2 = new Client(iBinder, uri2, Binder.getCallingUserHandle(), audioAttributes, configuration);
                    iBinder.linkToDeath(client2, 0);
                    RingtonePlayer.this.mClients.put(iBinder, client2);
                    client = client2;
                }
            }
            client.mRingtone.setLooping(z);
            client.mRingtone.setVolume(f);
            client.mRingtone.play();
        }

        private boolean isInCallNotification(Uri uri) {
            return (Build.VERSION.SDK_INT >= 29 ? RingtonePlayer.Q_INCALL_NOTIFICATION_URI : RingtonePlayer.INCALL_NOTIFICATION_URI).equals(uri);
        }

        public void stop(IBinder iBinder) {
            Client client;
            if (RingtonePlayer.LOGD) {
                Log.d("RingtonePlayer", "stop(token=" + iBinder + ")");
            }
            synchronized (RingtonePlayer.this.mClients) {
                client = (Client) RingtonePlayer.this.mClients.remove(iBinder);
            }
            if (client != null) {
                client.mToken.unlinkToDeath(client, 0);
                client.mRingtone.stop();
            }
        }

        public boolean isPlaying(IBinder iBinder) {
            Client client;
            if (RingtonePlayer.LOGD) {
                Log.d("RingtonePlayer", "isPlaying(token=" + iBinder + ")");
            }
            synchronized (RingtonePlayer.this.mClients) {
                client = (Client) RingtonePlayer.this.mClients.get(iBinder);
            }
            if (client != null) {
                return client.mRingtone.isPlaying();
            }
            return false;
        }

        public void setPlaybackProperties(IBinder iBinder, float f, boolean z) {
            Client client;
            synchronized (RingtonePlayer.this.mClients) {
                client = (Client) RingtonePlayer.this.mClients.get(iBinder);
            }
            if (client != null) {
                client.mRingtone.setVolume(f);
                client.mRingtone.setLooping(z);
            }
        }

        public void playAsync(Uri uri, UserHandle userHandle, boolean z, AudioAttributes audioAttributes) {
            if (RingtonePlayer.LOGD) {
                Log.d("RingtonePlayer", "playAsync(uri=" + uri + ", user=" + userHandle + ")");
            }
            if (Binder.getCallingUid() == 1000) {
                if (UserHandle.ALL.equals(userHandle)) {
                    userHandle = UserHandle.SYSTEM;
                }
                RingtonePlayer.this.mAsyncPlayer.play(RingtonePlayer.this.getContextForUser(userHandle), RingtonePlayer.this.fallbackNotificationUri(uri, audioAttributes), z, audioAttributes);
                return;
            }
            throw new SecurityException("Async playback only available from system UID.");
        }

        public void stopAsync() {
            if (RingtonePlayer.LOGD) {
                Log.d("RingtonePlayer", "stopAsync()");
            }
            if (Binder.getCallingUid() == 1000) {
                RingtonePlayer.this.mAsyncPlayer.stop();
                return;
            }
            throw new SecurityException("Async playback only available from system UID.");
        }

        public String getTitle(Uri uri) {
            return Ringtone.getTitle(RingtonePlayer.this.getContextForUser(Binder.getCallingUserHandle()), uri, false, false);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:27:0x0067, code lost:
            r7 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:28:0x0068, code lost:
            if (r0 != null) goto L_0x006a;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
            r0.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:31:0x006e, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:32:0x006f, code lost:
            r6.addSuppressed(r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:0x0072, code lost:
            throw r7;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public android.os.ParcelFileDescriptor openRingtone(android.net.Uri r7) {
            /*
                r6 = this;
                android.os.UserHandle r0 = android.os.Binder.getCallingUserHandle()
                com.android.systemui.media.RingtonePlayer r6 = com.android.systemui.media.RingtonePlayer.this
                android.content.Context r6 = r6.getContextForUser(r0)
                android.content.ContentResolver r6 = r6.getContentResolver()
                java.lang.String r0 = r7.toString()
                android.net.Uri r1 = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                java.lang.String r1 = r1.toString()
                boolean r0 = r0.startsWith(r1)
                if (r0 == 0) goto L_0x0073
                java.lang.String r0 = "is_ringtone"
                java.lang.String r1 = "is_alarm"
                java.lang.String r2 = "is_notification"
                java.lang.String[] r2 = new java.lang.String[]{r0, r1, r2}
                r3 = 0
                r4 = 0
                r5 = 0
                r0 = r6
                r1 = r7
                android.database.Cursor r0 = r0.query(r1, r2, r3, r4, r5)
                boolean r1 = r0.moveToFirst()     // Catch:{ all -> 0x0065 }
                if (r1 == 0) goto L_0x005f
                r1 = 0
                int r1 = r0.getInt(r1)     // Catch:{ all -> 0x0065 }
                if (r1 != 0) goto L_0x004c
                r1 = 1
                int r1 = r0.getInt(r1)     // Catch:{ all -> 0x0065 }
                if (r1 != 0) goto L_0x004c
                r1 = 2
                int r1 = r0.getInt(r1)     // Catch:{ all -> 0x0065 }
                if (r1 == 0) goto L_0x005f
            L_0x004c:
                java.lang.String r1 = "r"
                android.os.ParcelFileDescriptor r6 = r6.openFileDescriptor(r7, r1)     // Catch:{ IOException -> 0x0058 }
                if (r0 == 0) goto L_0x0057
                r0.close()
            L_0x0057:
                return r6
            L_0x0058:
                r6 = move-exception
                java.lang.SecurityException r7 = new java.lang.SecurityException     // Catch:{ all -> 0x0065 }
                r7.<init>(r6)     // Catch:{ all -> 0x0065 }
                throw r7     // Catch:{ all -> 0x0065 }
            L_0x005f:
                if (r0 == 0) goto L_0x0073
                r0.close()
                goto L_0x0073
            L_0x0065:
                r6 = move-exception
                throw r6     // Catch:{ all -> 0x0067 }
            L_0x0067:
                r7 = move-exception
                if (r0 == 0) goto L_0x0072
                r0.close()     // Catch:{ all -> 0x006e }
                goto L_0x0072
            L_0x006e:
                r0 = move-exception
                r6.addSuppressed(r0)
            L_0x0072:
                throw r7
            L_0x0073:
                java.lang.SecurityException r6 = new java.lang.SecurityException
                java.lang.StringBuilder r0 = new java.lang.StringBuilder
                r0.<init>()
                java.lang.String r1 = "Uri is not ringtone, alarm, or notification: "
                r0.append(r1)
                r0.append(r7)
                java.lang.String r7 = r0.toString()
                r6.<init>(r7)
                throw r6
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.media.RingtonePlayer.AnonymousClass1.openRingtone(android.net.Uri):android.os.ParcelFileDescriptor");
        }
    };
    /* access modifiers changed from: private */
    public final HashMap<IBinder, Client> mClients = new HashMap<>();
    /* access modifiers changed from: private */
    public boolean mPlayInCallNotification;

    public void start() {
        this.mPlayInCallNotification = this.mContext.getResources().getBoolean(R.bool.play_incall_notification);
        Log.d("RingtonePlayer", "RingtonePlayer mPlayInCallNotification=" + this.mPlayInCallNotification);
        this.mAsyncPlayer.setUsesWakeLock(this.mContext);
        this.mAudioService = IAudioService.Stub.asInterface(ServiceManager.getService("audio"));
        try {
            this.mAudioService.setRingtonePlayer(this.mCallback);
        } catch (RemoteException e) {
            Log.e("RingtonePlayer", "Problem registering RingtonePlayer: " + e);
        }
    }

    private class Client implements IBinder.DeathRecipient {
        /* access modifiers changed from: private */
        public final Ringtone mRingtone;
        /* access modifiers changed from: private */
        public final IBinder mToken;

        Client(IBinder iBinder, Uri uri, UserHandle userHandle, AudioAttributes audioAttributes, VolumeShaper.Configuration configuration) {
            this.mToken = iBinder;
            this.mRingtone = new Ringtone(RingtonePlayer.this.getContextForUser(userHandle), false);
            this.mRingtone.setAudioAttributes(audioAttributes);
            this.mRingtone.setUri(RingtonePlayer.this.fallbackNotificationUri(uri, audioAttributes));
        }

        public void binderDied() {
            if (RingtonePlayer.LOGD) {
                Log.d("RingtonePlayer", "binderDied() token=" + this.mToken);
            }
            synchronized (RingtonePlayer.this.mClients) {
                RingtonePlayer.this.mClients.remove(this.mToken);
            }
            this.mRingtone.stop();
        }
    }

    /* access modifiers changed from: private */
    public Context getContextForUser(UserHandle userHandle) {
        if (999 == userHandle.getIdentifier()) {
            userHandle = UserHandle.SYSTEM;
        }
        try {
            return this.mContext.createPackageContextAsUser(this.mContext.getPackageName(), 0, userHandle);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /* access modifiers changed from: private */
    public Uri fallbackNotificationUri(Uri uri, AudioAttributes audioAttributes) {
        String scheme;
        if (uri != null) {
            if (audioAttributes.getUsage() == 5) {
                String scheme2 = uri.getScheme();
                if ((scheme2 == null || scheme2.equals("file")) && !new File(uri.getPath()).exists()) {
                    return RingtoneManager.getDefaultUri(2);
                }
            } else if (audioAttributes.getUsage() == 6 && (((scheme = uri.getScheme()) == null || scheme.equals("file")) && !new File(uri.getPath()).exists())) {
                return RingtoneManager.getDefaultUri(1);
            }
        }
        return uri;
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("Clients:");
        synchronized (this.mClients) {
            for (Client next : this.mClients.values()) {
                printWriter.print("  mToken=");
                printWriter.print(next.mToken);
                printWriter.print(" mUri=");
                printWriter.println(next.mRingtone.getUri());
            }
        }
    }
}
