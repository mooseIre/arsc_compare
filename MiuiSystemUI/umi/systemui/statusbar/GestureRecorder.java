package com.android.systemui.statusbar;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import java.io.BufferedWriter;
import java.io.FileDescriptor;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

public class GestureRecorder {
    public static final String TAG = "GestureRecorder";
    private Gesture mCurrentGesture;
    private LinkedList<Gesture> mGestures;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            if (message.what == 6351) {
                GestureRecorder.this.save();
            }
        }
    };
    private int mLastSaveLen = -1;
    private String mLogfile;

    public class Gesture {
        boolean mComplete = false;
        long mDownTime = -1;
        private LinkedList<Record> mRecords = new LinkedList<>();
        private HashSet<String> mTags = new HashSet<>();

        public abstract class Record {
            long time;

            public abstract String toJson();

            public Record(Gesture gesture) {
            }
        }

        public Gesture(GestureRecorder gestureRecorder) {
        }

        public class MotionEventRecord extends Record {
            public MotionEvent event;

            public MotionEventRecord(Gesture gesture, long j, MotionEvent motionEvent) {
                super(gesture);
                this.time = j;
                this.event = MotionEvent.obtain(motionEvent);
            }

            /* access modifiers changed from: package-private */
            public String actionName(int i) {
                if (i == 0) {
                    return "down";
                }
                if (i == 1) {
                    return "up";
                }
                if (i != 2) {
                    return i != 3 ? String.valueOf(i) : "cancel";
                }
                return "move";
            }

            public String toJson() {
                return String.format("{\"type\":\"motion\", \"time\":%d, \"action\":\"%s\", \"x\":%.2f, \"y\":%.2f, \"s\":%.2f, \"p\":%.2f}", new Object[]{Long.valueOf(this.time), actionName(this.event.getAction()), Float.valueOf(this.event.getRawX()), Float.valueOf(this.event.getRawY()), Float.valueOf(this.event.getSize()), Float.valueOf(this.event.getPressure())});
            }
        }

        public class TagRecord extends Record {
            public String info;
            public String tag;

            public TagRecord(Gesture gesture, long j, String str, String str2) {
                super(gesture);
                this.time = j;
                this.tag = str;
                this.info = str2;
            }

            public String toJson() {
                return String.format("{\"type\":\"tag\", \"time\":%d, \"tag\":\"%s\", \"info\":\"%s\"}", new Object[]{Long.valueOf(this.time), this.tag, this.info});
            }
        }

        public void add(MotionEvent motionEvent) {
            this.mRecords.add(new MotionEventRecord(this, motionEvent.getEventTime(), motionEvent));
            long j = this.mDownTime;
            if (j < 0) {
                this.mDownTime = motionEvent.getDownTime();
            } else if (j != motionEvent.getDownTime()) {
                String str = GestureRecorder.TAG;
                Log.w(str, "Assertion failure in GestureRecorder: event downTime (" + motionEvent.getDownTime() + ") does not match gesture downTime (" + this.mDownTime + ")");
            }
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked == 1 || actionMasked == 3) {
                this.mComplete = true;
            }
        }

        public void tag(long j, String str, String str2) {
            this.mRecords.add(new TagRecord(this, j, str, str2));
            this.mTags.add(str);
        }

        public boolean isComplete() {
            return this.mComplete;
        }

        public String toJson() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            Iterator it = this.mRecords.iterator();
            boolean z = true;
            while (it.hasNext()) {
                Record record = (Record) it.next();
                if (!z) {
                    sb.append(", ");
                }
                z = false;
                sb.append(record.toJson());
            }
            sb.append("]");
            return sb.toString();
        }
    }

    public GestureRecorder(String str) {
        this.mLogfile = str;
        this.mGestures = new LinkedList<>();
        this.mCurrentGesture = null;
    }

    public void add(MotionEvent motionEvent) {
        synchronized (this.mGestures) {
            if (this.mCurrentGesture == null || this.mCurrentGesture.isComplete()) {
                Gesture gesture = new Gesture(this);
                this.mCurrentGesture = gesture;
                this.mGestures.add(gesture);
            }
            this.mCurrentGesture.add(motionEvent);
        }
        saveLater();
    }

    public void tag(long j, String str, String str2) {
        synchronized (this.mGestures) {
            if (this.mCurrentGesture == null) {
                Gesture gesture = new Gesture(this);
                this.mCurrentGesture = gesture;
                this.mGestures.add(gesture);
            }
            this.mCurrentGesture.tag(j, str, str2);
        }
        saveLater();
    }

    public void tag(String str, String str2) {
        tag(SystemClock.uptimeMillis(), str, str2);
    }

    public String toJsonLocked() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        Iterator it = this.mGestures.iterator();
        boolean z = true;
        int i = 0;
        while (it.hasNext()) {
            Gesture gesture = (Gesture) it.next();
            if (gesture.isComplete()) {
                if (!z) {
                    sb.append(",");
                }
                sb.append(gesture.toJson());
                i++;
                z = false;
            }
        }
        this.mLastSaveLen = i;
        sb.append("]");
        return sb.toString();
    }

    public void saveLater() {
        this.mHandler.removeMessages(6351);
        this.mHandler.sendEmptyMessageDelayed(6351, 5000);
    }

    public void save() {
        synchronized (this.mGestures) {
            try {
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(this.mLogfile, true));
                bufferedWriter.append(toJsonLocked() + "\n");
                bufferedWriter.close();
                this.mGestures.clear();
                if (this.mCurrentGesture != null && !this.mCurrentGesture.isComplete()) {
                    this.mGestures.add(this.mCurrentGesture);
                }
                Log.v(TAG, String.format("Wrote %d complete gestures to %s", new Object[]{Integer.valueOf(this.mLastSaveLen), this.mLogfile}));
            } catch (IOException e) {
                Log.e(TAG, String.format("Couldn't write gestures to %s", new Object[]{this.mLogfile}), e);
                this.mLastSaveLen = -1;
            }
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        save();
        if (this.mLastSaveLen >= 0) {
            printWriter.println(String.valueOf(this.mLastSaveLen) + " gestures written to " + this.mLogfile);
            return;
        }
        printWriter.println("error writing gestures");
    }
}
