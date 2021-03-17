package com.android.systemui.screenrecord;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaMuxer;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

public class ScreenRecordingMuxer {
    private static String TAG = "ScreenRecordingMuxer";
    private ArrayMap<Pair<MediaExtractor, Integer>, Integer> mExtractorIndexToMuxerIndex = new ArrayMap<>();
    private ArrayList<MediaExtractor> mExtractors = new ArrayList<>();
    private String[] mFiles;
    private int mFormat;
    private String mOutFile;

    public ScreenRecordingMuxer(int i, String str, String... strArr) {
        this.mFiles = strArr;
        this.mOutFile = str;
        this.mFormat = i;
        String str2 = TAG;
        Log.d(str2, "out: " + this.mOutFile + " , in: " + this.mFiles[0]);
    }

    public void mux() throws IOException {
        MediaMuxer mediaMuxer = new MediaMuxer(this.mOutFile, this.mFormat);
        for (String str : this.mFiles) {
            MediaExtractor mediaExtractor = new MediaExtractor();
            try {
                mediaExtractor.setDataSource(str);
                Log.d(TAG, str + " track count: " + mediaExtractor.getTrackCount());
                this.mExtractors.add(mediaExtractor);
                for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
                    int addTrack = mediaMuxer.addTrack(mediaExtractor.getTrackFormat(i));
                    Log.d(TAG, "created extractor format" + mediaExtractor.getTrackFormat(i).toString());
                    this.mExtractorIndexToMuxerIndex.put(Pair.create(mediaExtractor, Integer.valueOf(i)), Integer.valueOf(addTrack));
                }
            } catch (IOException e) {
                Log.e(TAG, "error creating extractor: " + str);
                e.printStackTrace();
            }
        }
        mediaMuxer.start();
        for (Pair next : this.mExtractorIndexToMuxerIndex.keySet()) {
            MediaExtractor mediaExtractor2 = (MediaExtractor) next.first;
            mediaExtractor2.selectTrack(((Integer) next.second).intValue());
            int intValue = this.mExtractorIndexToMuxerIndex.get(next).intValue();
            Log.d(TAG, "track format: " + mediaExtractor2.getTrackFormat(((Integer) next.second).intValue()));
            mediaExtractor2.seekTo(0, 2);
            ByteBuffer allocate = ByteBuffer.allocate(4194304);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            while (true) {
                int readSampleData = mediaExtractor2.readSampleData(allocate, allocate.arrayOffset());
                bufferInfo.size = readSampleData;
                if (readSampleData >= 0) {
                    bufferInfo.presentationTimeUs = mediaExtractor2.getSampleTime();
                    bufferInfo.flags = mediaExtractor2.getSampleFlags();
                    mediaMuxer.writeSampleData(intValue, allocate, bufferInfo);
                    mediaExtractor2.advance();
                }
            }
        }
        Iterator<MediaExtractor> it = this.mExtractors.iterator();
        while (it.hasNext()) {
            it.next().release();
        }
        mediaMuxer.stop();
        mediaMuxer.release();
    }
}
