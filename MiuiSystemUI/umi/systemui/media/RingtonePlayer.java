package com.android.systemui.media;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.util.Log;
import com.android.systemui.Constants;
import com.android.systemui.SystemUI;
import com.android.systemui.plugins.R;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
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

        public ParcelFileDescriptor openRingtone(Uri uri) {
            ContentResolver contentResolver = RingtonePlayer.this.getContextForUser(Binder.getCallingUserHandle()).getContentResolver();
            if (uri.toString().startsWith(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString())) {
                Cursor query = contentResolver.query(uri, new String[]{"is_ringtone", "is_alarm", "is_notification"}, (String) null, (String[]) null, (String) null);
                try {
                    if (query.moveToFirst() && (query.getInt(0) != 0 || query.getInt(1) != 0 || query.getInt(2) != 0)) {
                        ParcelFileDescriptor openFileDescriptor = contentResolver.openFileDescriptor(uri, "r");
                        if (query != null) {
                            query.close();
                        }
                        return openFileDescriptor;
                    } else if (query != null) {
                        query.close();
                    }
                } catch (IOException e) {
                    throw new SecurityException(e);
                } catch (Throwable th) {
                    if (query != null) {
                        try {
                            query.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    }
                    throw th;
                }
            }
            throw new SecurityException("Uri is not ringtone, alarm, or notification: " + uri);
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
        IAudioService asInterface = IAudioService.Stub.asInterface(ServiceManager.getService("audio"));
        this.mAudioService = asInterface;
        try {
            asInterface.setRingtonePlayer(this.mCallback);
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
            Ringtone ringtone = new Ringtone(RingtonePlayer.this.getContextForUser(userHandle), false);
            this.mRingtone = ringtone;
            ringtone.setAudioAttributes(audioAttributes);
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
