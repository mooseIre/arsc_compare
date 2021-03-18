package com.android.systemui.screenrecord;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.media.projection.IMediaProjection;
import android.media.projection.IMediaProjectionManager;
import android.media.projection.MediaProjection;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.WindowManager;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenMediaRecorder {
    private ScreenInternalAudioRecorder mAudio;
    private ScreenRecordingAudioSource mAudioSource;
    private Context mContext;
    private Surface mInputSurface;
    MediaRecorder.OnInfoListener mListener;
    private MediaProjection mMediaProjection;
    private MediaRecorder mMediaRecorder;
    private ScreenRecordingMuxer mMuxer;
    private File mTempAudioFile;
    private File mTempVideoFile;
    private int mUser;
    private VirtualDisplay mVirtualDisplay;

    public ScreenMediaRecorder(Context context, int i, ScreenRecordingAudioSource screenRecordingAudioSource, MediaRecorder.OnInfoListener onInfoListener) {
        this.mContext = context;
        this.mUser = i;
        this.mListener = onInfoListener;
        this.mAudioSource = screenRecordingAudioSource;
    }

    private void prepare() throws IOException, RemoteException {
        boolean z = false;
        this.mMediaProjection = new MediaProjection(this.mContext, IMediaProjection.Stub.asInterface(IMediaProjectionManager.Stub.asInterface(ServiceManager.getService("media_projection")).createProjection(this.mUser, this.mContext.getPackageName(), 0, false).asBinder()));
        File cacheDir = this.mContext.getCacheDir();
        cacheDir.mkdirs();
        this.mTempVideoFile = File.createTempFile("temp", ".mp4", cacheDir);
        MediaRecorder mediaRecorder = new MediaRecorder();
        this.mMediaRecorder = mediaRecorder;
        if (this.mAudioSource == ScreenRecordingAudioSource.MIC) {
            mediaRecorder.setAudioSource(0);
        }
        this.mMediaRecorder.setVideoSource(2);
        this.mMediaRecorder.setOutputFormat(2);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) this.mContext.getSystemService("window");
        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        int i = displayMetrics.widthPixels;
        int i2 = displayMetrics.heightPixels;
        int refreshRate = (int) windowManager.getDefaultDisplay().getRefreshRate();
        this.mMediaRecorder.setVideoEncoder(2);
        this.mMediaRecorder.setVideoEncodingProfileLevel(8, 8192);
        this.mMediaRecorder.setVideoSize(i, i2);
        this.mMediaRecorder.setVideoFrameRate(refreshRate);
        this.mMediaRecorder.setVideoEncodingBitRate((((i2 * i) * refreshRate) / 30) * 6);
        this.mMediaRecorder.setMaxDuration(3600000);
        this.mMediaRecorder.setMaxFileSize(5000000000L);
        if (this.mAudioSource == ScreenRecordingAudioSource.MIC) {
            this.mMediaRecorder.setAudioEncoder(4);
            this.mMediaRecorder.setAudioChannels(1);
            this.mMediaRecorder.setAudioEncodingBitRate(196000);
            this.mMediaRecorder.setAudioSamplingRate(44100);
        }
        this.mMediaRecorder.setOutputFile(this.mTempVideoFile);
        this.mMediaRecorder.prepare();
        Surface surface = this.mMediaRecorder.getSurface();
        this.mInputSurface = surface;
        this.mVirtualDisplay = this.mMediaProjection.createVirtualDisplay("Recording Display", i, i2, displayMetrics.densityDpi, 16, surface, null, null);
        this.mMediaRecorder.setOnInfoListener(this.mListener);
        ScreenRecordingAudioSource screenRecordingAudioSource = this.mAudioSource;
        if (screenRecordingAudioSource == ScreenRecordingAudioSource.INTERNAL || screenRecordingAudioSource == ScreenRecordingAudioSource.MIC_AND_INTERNAL) {
            this.mTempAudioFile = File.createTempFile("temp", ".aac", this.mContext.getCacheDir());
            String absolutePath = this.mTempAudioFile.getAbsolutePath();
            MediaProjection mediaProjection = this.mMediaProjection;
            if (this.mAudioSource == ScreenRecordingAudioSource.MIC_AND_INTERNAL) {
                z = true;
            }
            this.mAudio = new ScreenInternalAudioRecorder(absolutePath, mediaProjection, z);
        }
    }

    /* access modifiers changed from: package-private */
    public void start() throws IOException, RemoteException, IllegalStateException {
        Log.d("ScreenMediaRecorder", "start recording");
        prepare();
        this.mMediaRecorder.start();
        recordInternalAudio();
    }

    /* access modifiers changed from: package-private */
    public void end() {
        this.mMediaRecorder.stop();
        this.mMediaProjection.stop();
        this.mMediaRecorder.release();
        this.mMediaRecorder = null;
        this.mMediaProjection = null;
        this.mInputSurface.release();
        this.mVirtualDisplay.release();
        stopInternalAudioRecording();
        Log.d("ScreenMediaRecorder", "end recording");
    }

    private void stopInternalAudioRecording() {
        ScreenRecordingAudioSource screenRecordingAudioSource = this.mAudioSource;
        if (screenRecordingAudioSource == ScreenRecordingAudioSource.INTERNAL || screenRecordingAudioSource == ScreenRecordingAudioSource.MIC_AND_INTERNAL) {
            this.mAudio.end();
            this.mAudio = null;
        }
    }

    private void recordInternalAudio() throws IllegalStateException {
        ScreenRecordingAudioSource screenRecordingAudioSource = this.mAudioSource;
        if (screenRecordingAudioSource == ScreenRecordingAudioSource.INTERNAL || screenRecordingAudioSource == ScreenRecordingAudioSource.MIC_AND_INTERNAL) {
            this.mAudio.start();
        }
    }

    /* access modifiers changed from: protected */
    public SavedRecording save() throws IOException {
        String format = new SimpleDateFormat("'screen-'yyyyMMdd-HHmmss'.mp4'").format(new Date());
        ContentValues contentValues = new ContentValues();
        contentValues.put("_display_name", format);
        contentValues.put("mime_type", "video/mp4");
        contentValues.put("date_added", Long.valueOf(System.currentTimeMillis()));
        contentValues.put("datetaken", Long.valueOf(System.currentTimeMillis()));
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Uri insert = contentResolver.insert(MediaStore.Video.Media.getContentUri("external_primary"), contentValues);
        Log.d("ScreenMediaRecorder", insert.toString());
        ScreenRecordingAudioSource screenRecordingAudioSource = this.mAudioSource;
        if (screenRecordingAudioSource == ScreenRecordingAudioSource.MIC_AND_INTERNAL || screenRecordingAudioSource == ScreenRecordingAudioSource.INTERNAL) {
            try {
                Log.d("ScreenMediaRecorder", "muxing recording");
                File createTempFile = File.createTempFile("temp", ".mp4", this.mContext.getCacheDir());
                ScreenRecordingMuxer screenRecordingMuxer = new ScreenRecordingMuxer(0, createTempFile.getAbsolutePath(), this.mTempVideoFile.getAbsolutePath(), this.mTempAudioFile.getAbsolutePath());
                this.mMuxer = screenRecordingMuxer;
                screenRecordingMuxer.mux();
                this.mTempVideoFile.delete();
                this.mTempVideoFile = createTempFile;
            } catch (IOException e) {
                Log.e("ScreenMediaRecorder", "muxing recording " + e.getMessage());
                e.printStackTrace();
            }
        }
        OutputStream openOutputStream = contentResolver.openOutputStream(insert, "w");
        Files.copy(this.mTempVideoFile.toPath(), openOutputStream);
        openOutputStream.close();
        File file = this.mTempAudioFile;
        if (file != null) {
            file.delete();
        }
        DisplayMetrics displayMetrics = this.mContext.getResources().getDisplayMetrics();
        SavedRecording savedRecording = new SavedRecording(this, insert, this.mTempVideoFile, new Size(displayMetrics.widthPixels, displayMetrics.heightPixels));
        this.mTempVideoFile.delete();
        return savedRecording;
    }

    public class SavedRecording {
        private Bitmap mThumbnailBitmap;
        private Uri mUri;

        protected SavedRecording(ScreenMediaRecorder screenMediaRecorder, Uri uri, File file, Size size) {
            this.mUri = uri;
            try {
                this.mThumbnailBitmap = ThumbnailUtils.createVideoThumbnail(file, size, null);
            } catch (IOException e) {
                Log.e("ScreenMediaRecorder", "Error creating thumbnail", e);
            }
        }

        public Uri getUri() {
            return this.mUri;
        }

        public Bitmap getThumbnail() {
            return this.mThumbnailBitmap;
        }
    }
}
