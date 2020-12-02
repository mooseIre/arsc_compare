package com.android.systemui.screenrecord;

import android.media.AudioFormat;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.util.Log;
import android.view.Surface;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ScreenInternalAudioRecorder {
    private static String TAG = "ScreenAudioRecorder";
    private AudioRecord mAudioRecord;
    private AudioRecord mAudioRecordMic;
    private MediaCodec mCodec;
    private Config mConfig = new Config();
    private MediaProjection mMediaProjection;
    private boolean mMic;
    private MediaMuxer mMuxer;
    private long mPresentationTime;
    private Thread mThread;
    private long mTotalBytes;
    private int mTrackId = -1;

    public ScreenInternalAudioRecorder(String str, MediaProjection mediaProjection, boolean z) throws IOException {
        this.mMic = z;
        this.mMuxer = new MediaMuxer(str, 0);
        this.mMediaProjection = mediaProjection;
        String str2 = TAG;
        Log.d(str2, "creating audio file " + str);
        setupSimple();
    }

    public static class Config {
        public int bitRate = 196000;
        public int bufferSizeBytes = 131072;
        public int channelInMask = 16;
        public int channelOutMask = 4;
        public int encoding = 2;
        public boolean legacy_app_looback = false;
        public boolean privileged = true;
        public int sampleRate = 44100;

        public String toString() {
            return "channelMask=" + this.channelOutMask + "\n   encoding=" + this.encoding + "\n sampleRate=" + this.sampleRate + "\n bufferSize=" + this.bufferSizeBytes + "\n privileged=" + this.privileged + "\n legacy app looback=" + this.legacy_app_looback;
        }
    }

    private void setupSimple() throws IOException {
        Config config = this.mConfig;
        int minBufferSize = AudioRecord.getMinBufferSize(config.sampleRate, config.channelInMask, config.encoding) * 2;
        String str = TAG;
        Log.d(str, "audio buffer size: " + minBufferSize);
        AudioFormat build = new AudioFormat.Builder().setEncoding(this.mConfig.encoding).setSampleRate(this.mConfig.sampleRate).setChannelMask(this.mConfig.channelOutMask).build();
        this.mAudioRecord = new AudioRecord.Builder().setAudioFormat(build).setAudioPlaybackCaptureConfig(new AudioPlaybackCaptureConfiguration.Builder(this.mMediaProjection).addMatchingUsage(1).addMatchingUsage(0).addMatchingUsage(14).build()).build();
        if (this.mMic) {
            Config config2 = this.mConfig;
            this.mAudioRecordMic = new AudioRecord(7, config2.sampleRate, 16, config2.encoding, minBufferSize);
        }
        this.mCodec = MediaCodec.createEncoderByType("audio/mp4a-latm");
        MediaFormat createAudioFormat = MediaFormat.createAudioFormat("audio/mp4a-latm", this.mConfig.sampleRate, 1);
        createAudioFormat.setInteger("aac-profile", 2);
        createAudioFormat.setInteger("bitrate", this.mConfig.bitRate);
        createAudioFormat.setInteger("pcm-encoding", this.mConfig.encoding);
        this.mCodec.configure(createAudioFormat, (Surface) null, (MediaCrypto) null, 1);
        this.mThread = new Thread(new Runnable(minBufferSize) {
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                ScreenInternalAudioRecorder.this.lambda$setupSimple$0$ScreenInternalAudioRecorder(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setupSimple$0 */
    public /* synthetic */ void lambda$setupSimple$0$ScreenInternalAudioRecorder(int i) {
        short[] sArr;
        short[] sArr2;
        int i2;
        int i3;
        byte[] bArr = null;
        if (this.mMic) {
            int i4 = i / 2;
            sArr2 = new short[i4];
            sArr = new short[i4];
        } else {
            sArr2 = null;
            bArr = new byte[i];
            sArr = null;
        }
        while (true) {
            int i5 = 0;
            if (this.mMic) {
                int read = this.mAudioRecord.read(sArr2, 0, sArr2.length);
                int read2 = this.mAudioRecordMic.read(sArr, 0, sArr.length);
                scaleValues(sArr, read2, 1.4f);
                i3 = Math.min(read, read2) * 2;
                byte[] addAndConvertBuffers = addAndConvertBuffers(sArr2, read, sArr, read2);
                int i6 = read2;
                i5 = read;
                bArr = addAndConvertBuffers;
                i2 = i6;
            } else {
                i3 = this.mAudioRecord.read(bArr, 0, bArr.length);
                i2 = 0;
            }
            if (i3 < 0) {
                Log.e(TAG, "read error " + i3 + ", shorts internal: " + i5 + ", shorts mic: " + i2);
                endStream();
                return;
            }
            encode(bArr, i3);
        }
    }

    private short[] scaleValues(short[] sArr, int i, float f) {
        for (int i2 = 0; i2 < i; i2++) {
            short s = sArr[i2];
            int i3 = (int) (((float) sArr[i2]) * f);
            if (i3 > 32767) {
                i3 = 32767;
            } else if (i3 < -32768) {
                i3 = -32768;
            }
            sArr[i2] = (short) i3;
        }
        return sArr;
    }

    /* JADX WARNING: type inference failed for: r6v0, types: [short[]] */
    /* JADX WARNING: type inference failed for: r8v0, types: [short[]] */
    /* JADX WARNING: type inference failed for: r2v8, types: [short, int] */
    /* JADX WARNING: type inference failed for: r3v4, types: [short, int] */
    /* JADX WARNING: type inference failed for: r2v10, types: [short] */
    /* JADX WARNING: type inference failed for: r2v11, types: [short] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=short, code=int, for r2v10, types: [short] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=short, code=int, for r2v11, types: [short] */
    /* JADX WARNING: Unknown variable types count: 2 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private byte[] addAndConvertBuffers(short[] r6, int r7, short[] r8, int r9) {
        /*
            r5 = this;
            int r5 = java.lang.Math.max(r7, r9)
            r0 = 0
            if (r5 >= 0) goto L_0x000a
            byte[] r5 = new byte[r0]
            return r5
        L_0x000a:
            int r1 = r5 * 2
            byte[] r1 = new byte[r1]
        L_0x000e:
            if (r0 >= r5) goto L_0x003c
            if (r0 <= r7) goto L_0x0015
            short r2 = r8[r0]
            goto L_0x001f
        L_0x0015:
            if (r0 <= r9) goto L_0x001a
            short r2 = r6[r0]
            goto L_0x001f
        L_0x001a:
            short r2 = r6[r0]
            short r3 = r8[r0]
            int r2 = r2 + r3
        L_0x001f:
            r3 = 32767(0x7fff, float:4.5916E-41)
            if (r2 <= r3) goto L_0x0024
            r2 = r3
        L_0x0024:
            r3 = -32768(0xffffffffffff8000, float:NaN)
            if (r2 >= r3) goto L_0x0029
            r2 = r3
        L_0x0029:
            int r3 = r0 * 2
            r4 = r2 & 255(0xff, float:3.57E-43)
            byte r4 = (byte) r4
            r1[r3] = r4
            int r3 = r3 + 1
            int r2 = r2 >> 8
            r2 = r2 & 255(0xff, float:3.57E-43)
            byte r2 = (byte) r2
            r1[r3] = r2
            int r0 = r0 + 1
            goto L_0x000e
        L_0x003c:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.screenrecord.ScreenInternalAudioRecorder.addAndConvertBuffers(short[], int, short[], int):byte[]");
    }

    private void encode(byte[] bArr, int i) {
        int i2 = 0;
        while (i > 0) {
            int dequeueInputBuffer = this.mCodec.dequeueInputBuffer(500);
            if (dequeueInputBuffer < 0) {
                writeOutput();
                return;
            }
            ByteBuffer inputBuffer = this.mCodec.getInputBuffer(dequeueInputBuffer);
            inputBuffer.clear();
            int capacity = inputBuffer.capacity();
            int i3 = i > capacity ? capacity : i;
            i -= i3;
            inputBuffer.put(bArr, i2, i3);
            i2 += i3;
            this.mCodec.queueInputBuffer(dequeueInputBuffer, 0, i3, this.mPresentationTime, 0);
            long j = this.mTotalBytes + ((long) (i3 + 0));
            this.mTotalBytes = j;
            this.mPresentationTime = ((j / 2) * 1000000) / ((long) this.mConfig.sampleRate);
            writeOutput();
        }
    }

    private void endStream() {
        this.mCodec.queueInputBuffer(this.mCodec.dequeueInputBuffer(500), 0, 0, this.mPresentationTime, 4);
        writeOutput();
    }

    private void writeOutput() {
        while (true) {
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int dequeueOutputBuffer = this.mCodec.dequeueOutputBuffer(bufferInfo, 500);
            if (dequeueOutputBuffer == -2) {
                this.mTrackId = this.mMuxer.addTrack(this.mCodec.getOutputFormat());
                this.mMuxer.start();
            } else if (dequeueOutputBuffer != -1 && this.mTrackId >= 0) {
                ByteBuffer outputBuffer = this.mCodec.getOutputBuffer(dequeueOutputBuffer);
                if ((bufferInfo.flags & 2) == 0 || bufferInfo.size == 0) {
                    this.mMuxer.writeSampleData(this.mTrackId, outputBuffer, bufferInfo);
                }
                this.mCodec.releaseOutputBuffer(dequeueOutputBuffer, false);
            } else {
                return;
            }
        }
    }

    public void start() throws IllegalStateException {
        if (this.mThread != null) {
            Log.e(TAG, "a recording is being done in parallel or stop is not called");
        }
        this.mAudioRecord.startRecording();
        if (this.mMic) {
            this.mAudioRecordMic.startRecording();
        }
        String str = TAG;
        Log.d(str, "channel count " + this.mAudioRecord.getChannelCount());
        this.mCodec.start();
        if (this.mAudioRecord.getRecordingState() == 3) {
            this.mThread.start();
            return;
        }
        throw new IllegalStateException("Audio recording failed to start");
    }

    public void end() {
        this.mAudioRecord.stop();
        if (this.mMic) {
            this.mAudioRecordMic.stop();
        }
        this.mAudioRecord.release();
        if (this.mMic) {
            this.mAudioRecordMic.release();
        }
        try {
            this.mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.mCodec.stop();
        this.mCodec.release();
        this.mMuxer.stop();
        this.mMuxer.release();
        this.mThread = null;
    }
}
