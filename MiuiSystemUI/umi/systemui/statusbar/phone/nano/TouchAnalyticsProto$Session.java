package com.android.systemui.statusbar.phone.nano;

import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.InternalNano;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.WireFormatNano;
import java.io.IOException;

public final class TouchAnalyticsProto$Session extends MessageNano {
    private int bitField0_;
    private String build_;
    private long durationMillis_;
    public PhoneEvent[] phoneEvents;
    private int result_;
    public SensorEvent[] sensorEvents;
    private long startTimestampMillis_;
    private int touchAreaHeight_;
    private int touchAreaWidth_;
    public TouchEvent[] touchEvents;
    private int type_;

    public static final class TouchEvent extends MessageNano {
        private static volatile TouchEvent[] _emptyArray;
        private int actionIndex_;
        private int action_;
        private int bitField0_;
        public Pointer[] pointers;
        public BoundingBox removedBoundingBox;
        private boolean removedRedacted_;
        private long timeOffsetNanos_;

        public static final class BoundingBox extends MessageNano {
        }

        public static final class Pointer extends MessageNano {
            private static volatile Pointer[] _emptyArray;
            private int bitField0_;
            private int id_;
            private float pressure_;
            public BoundingBox removedBoundingBox;
            private float removedLength_;
            private float size_;
            private float x_;
            private float y_;

            public static Pointer[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new Pointer[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public Pointer setX(float f) {
                this.x_ = f;
                this.bitField0_ |= 1;
                return this;
            }

            public Pointer setY(float f) {
                this.y_ = f;
                this.bitField0_ |= 2;
                return this;
            }

            public Pointer setSize(float f) {
                this.size_ = f;
                this.bitField0_ |= 4;
                return this;
            }

            public Pointer setPressure(float f) {
                this.pressure_ = f;
                this.bitField0_ |= 8;
                return this;
            }

            public Pointer setId(int i) {
                this.id_ = i;
                this.bitField0_ |= 16;
                return this;
            }

            public Pointer() {
                clear();
            }

            public Pointer clear() {
                this.bitField0_ = 0;
                this.x_ = 0.0f;
                this.y_ = 0.0f;
                this.size_ = 0.0f;
                this.pressure_ = 0.0f;
                this.id_ = 0;
                this.removedLength_ = 0.0f;
                this.removedBoundingBox = null;
                this.cachedSize = -1;
                return this;
            }

            public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
                if ((this.bitField0_ & 1) != 0) {
                    codedOutputByteBufferNano.writeFloat(1, this.x_);
                }
                if ((this.bitField0_ & 2) != 0) {
                    codedOutputByteBufferNano.writeFloat(2, this.y_);
                }
                if ((this.bitField0_ & 4) != 0) {
                    codedOutputByteBufferNano.writeFloat(3, this.size_);
                }
                if ((this.bitField0_ & 8) != 0) {
                    codedOutputByteBufferNano.writeFloat(4, this.pressure_);
                }
                if ((this.bitField0_ & 16) != 0) {
                    codedOutputByteBufferNano.writeInt32(5, this.id_);
                }
                if ((this.bitField0_ & 32) != 0) {
                    codedOutputByteBufferNano.writeFloat(6, this.removedLength_);
                }
                BoundingBox boundingBox = this.removedBoundingBox;
                if (boundingBox != null) {
                    codedOutputByteBufferNano.writeMessage(7, boundingBox);
                }
                super.writeTo(codedOutputByteBufferNano);
            }

            /* access modifiers changed from: protected */
            public int computeSerializedSize() {
                int computeSerializedSize = super.computeSerializedSize();
                if ((this.bitField0_ & 1) != 0) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeFloatSize(1, this.x_);
                }
                if ((this.bitField0_ & 2) != 0) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeFloatSize(2, this.y_);
                }
                if ((this.bitField0_ & 4) != 0) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeFloatSize(3, this.size_);
                }
                if ((this.bitField0_ & 8) != 0) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeFloatSize(4, this.pressure_);
                }
                if ((this.bitField0_ & 16) != 0) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(5, this.id_);
                }
                if ((this.bitField0_ & 32) != 0) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeFloatSize(6, this.removedLength_);
                }
                BoundingBox boundingBox = this.removedBoundingBox;
                return boundingBox != null ? computeSerializedSize + CodedOutputByteBufferNano.computeMessageSize(7, boundingBox) : computeSerializedSize;
            }
        }

        public static TouchEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new TouchEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public TouchEvent setTimeOffsetNanos(long j) {
            this.timeOffsetNanos_ = j;
            this.bitField0_ |= 1;
            return this;
        }

        public TouchEvent setAction(int i) {
            this.action_ = i;
            this.bitField0_ |= 2;
            return this;
        }

        public TouchEvent setActionIndex(int i) {
            this.actionIndex_ = i;
            this.bitField0_ |= 4;
            return this;
        }

        public TouchEvent() {
            clear();
        }

        public TouchEvent clear() {
            this.bitField0_ = 0;
            this.timeOffsetNanos_ = 0;
            this.action_ = 0;
            this.actionIndex_ = 0;
            this.pointers = Pointer.emptyArray();
            this.removedRedacted_ = false;
            this.removedBoundingBox = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if ((this.bitField0_ & 1) != 0) {
                codedOutputByteBufferNano.writeUInt64(1, this.timeOffsetNanos_);
            }
            if ((this.bitField0_ & 2) != 0) {
                codedOutputByteBufferNano.writeInt32(2, this.action_);
            }
            if ((this.bitField0_ & 4) != 0) {
                codedOutputByteBufferNano.writeInt32(3, this.actionIndex_);
            }
            Pointer[] pointerArr = this.pointers;
            if (pointerArr != null && pointerArr.length > 0) {
                int i = 0;
                while (true) {
                    Pointer[] pointerArr2 = this.pointers;
                    if (i >= pointerArr2.length) {
                        break;
                    }
                    Pointer pointer = pointerArr2[i];
                    if (pointer != null) {
                        codedOutputByteBufferNano.writeMessage(4, pointer);
                    }
                    i++;
                }
            }
            if ((this.bitField0_ & 8) != 0) {
                codedOutputByteBufferNano.writeBool(5, this.removedRedacted_);
            }
            BoundingBox boundingBox = this.removedBoundingBox;
            if (boundingBox != null) {
                codedOutputByteBufferNano.writeMessage(6, boundingBox);
            }
            super.writeTo(codedOutputByteBufferNano);
        }

        /* access modifiers changed from: protected */
        public int computeSerializedSize() {
            int computeSerializedSize = super.computeSerializedSize();
            if ((this.bitField0_ & 1) != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeUInt64Size(1, this.timeOffsetNanos_);
            }
            if ((this.bitField0_ & 2) != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(2, this.action_);
            }
            if ((this.bitField0_ & 4) != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(3, this.actionIndex_);
            }
            Pointer[] pointerArr = this.pointers;
            if (pointerArr != null && pointerArr.length > 0) {
                int i = 0;
                while (true) {
                    Pointer[] pointerArr2 = this.pointers;
                    if (i >= pointerArr2.length) {
                        break;
                    }
                    Pointer pointer = pointerArr2[i];
                    if (pointer != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(4, pointer);
                    }
                    i++;
                }
            }
            if ((this.bitField0_ & 8) != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(5, this.removedRedacted_);
            }
            BoundingBox boundingBox = this.removedBoundingBox;
            return boundingBox != null ? computeSerializedSize + CodedOutputByteBufferNano.computeMessageSize(6, boundingBox) : computeSerializedSize;
        }
    }

    public static final class SensorEvent extends MessageNano {
        private static volatile SensorEvent[] _emptyArray;
        private int bitField0_;
        private long timeOffsetNanos_;
        private long timestamp_;
        private int type_;
        public float[] values;

        public static SensorEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new SensorEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public SensorEvent setType(int i) {
            this.type_ = i;
            this.bitField0_ |= 1;
            return this;
        }

        public SensorEvent setTimeOffsetNanos(long j) {
            this.timeOffsetNanos_ = j;
            this.bitField0_ |= 2;
            return this;
        }

        public SensorEvent setTimestamp(long j) {
            this.timestamp_ = j;
            this.bitField0_ |= 4;
            return this;
        }

        public SensorEvent() {
            clear();
        }

        public SensorEvent clear() {
            this.bitField0_ = 0;
            this.type_ = 1;
            this.timeOffsetNanos_ = 0;
            this.values = WireFormatNano.EMPTY_FLOAT_ARRAY;
            this.timestamp_ = 0;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if ((this.bitField0_ & 1) != 0) {
                codedOutputByteBufferNano.writeInt32(1, this.type_);
            }
            if ((this.bitField0_ & 2) != 0) {
                codedOutputByteBufferNano.writeUInt64(2, this.timeOffsetNanos_);
            }
            float[] fArr = this.values;
            if (fArr != null && fArr.length > 0) {
                int i = 0;
                while (true) {
                    float[] fArr2 = this.values;
                    if (i >= fArr2.length) {
                        break;
                    }
                    codedOutputByteBufferNano.writeFloat(3, fArr2[i]);
                    i++;
                }
            }
            if ((this.bitField0_ & 4) != 0) {
                codedOutputByteBufferNano.writeUInt64(4, this.timestamp_);
            }
            super.writeTo(codedOutputByteBufferNano);
        }

        /* access modifiers changed from: protected */
        public int computeSerializedSize() {
            int computeSerializedSize = super.computeSerializedSize();
            if ((this.bitField0_ & 1) != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(1, this.type_);
            }
            if ((this.bitField0_ & 2) != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeUInt64Size(2, this.timeOffsetNanos_);
            }
            float[] fArr = this.values;
            if (fArr != null && fArr.length > 0) {
                computeSerializedSize = computeSerializedSize + (fArr.length * 4) + (fArr.length * 1);
            }
            return (this.bitField0_ & 4) != 0 ? computeSerializedSize + CodedOutputByteBufferNano.computeUInt64Size(4, this.timestamp_) : computeSerializedSize;
        }
    }

    public static final class PhoneEvent extends MessageNano {
        private static volatile PhoneEvent[] _emptyArray;
        private int bitField0_;
        private long timeOffsetNanos_;
        private int type_;

        public static PhoneEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new PhoneEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public PhoneEvent setType(int i) {
            this.type_ = i;
            this.bitField0_ |= 1;
            return this;
        }

        public PhoneEvent setTimeOffsetNanos(long j) {
            this.timeOffsetNanos_ = j;
            this.bitField0_ |= 2;
            return this;
        }

        public PhoneEvent() {
            clear();
        }

        public PhoneEvent clear() {
            this.bitField0_ = 0;
            this.type_ = 0;
            this.timeOffsetNanos_ = 0;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if ((this.bitField0_ & 1) != 0) {
                codedOutputByteBufferNano.writeInt32(1, this.type_);
            }
            if ((this.bitField0_ & 2) != 0) {
                codedOutputByteBufferNano.writeUInt64(2, this.timeOffsetNanos_);
            }
            super.writeTo(codedOutputByteBufferNano);
        }

        /* access modifiers changed from: protected */
        public int computeSerializedSize() {
            int computeSerializedSize = super.computeSerializedSize();
            if ((this.bitField0_ & 1) != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(1, this.type_);
            }
            return (this.bitField0_ & 2) != 0 ? computeSerializedSize + CodedOutputByteBufferNano.computeUInt64Size(2, this.timeOffsetNanos_) : computeSerializedSize;
        }
    }

    public TouchAnalyticsProto$Session setStartTimestampMillis(long j) {
        this.startTimestampMillis_ = j;
        this.bitField0_ |= 1;
        return this;
    }

    public TouchAnalyticsProto$Session setDurationMillis(long j) {
        this.durationMillis_ = j;
        this.bitField0_ |= 2;
        return this;
    }

    public TouchAnalyticsProto$Session setBuild(String str) {
        if (str != null) {
            this.build_ = str;
            this.bitField0_ |= 4;
            return this;
        }
        throw new NullPointerException();
    }

    public TouchAnalyticsProto$Session setResult(int i) {
        this.result_ = i;
        this.bitField0_ |= 8;
        return this;
    }

    public TouchAnalyticsProto$Session setTouchAreaWidth(int i) {
        this.touchAreaWidth_ = i;
        this.bitField0_ |= 16;
        return this;
    }

    public TouchAnalyticsProto$Session setTouchAreaHeight(int i) {
        this.touchAreaHeight_ = i;
        this.bitField0_ |= 32;
        return this;
    }

    public TouchAnalyticsProto$Session setType(int i) {
        this.type_ = i;
        this.bitField0_ |= 64;
        return this;
    }

    public TouchAnalyticsProto$Session() {
        clear();
    }

    public TouchAnalyticsProto$Session clear() {
        this.bitField0_ = 0;
        this.startTimestampMillis_ = 0;
        this.durationMillis_ = 0;
        this.build_ = "";
        this.result_ = 0;
        this.touchEvents = TouchEvent.emptyArray();
        this.sensorEvents = SensorEvent.emptyArray();
        this.touchAreaWidth_ = 0;
        this.touchAreaHeight_ = 0;
        this.type_ = 0;
        this.phoneEvents = PhoneEvent.emptyArray();
        this.cachedSize = -1;
        return this;
    }

    public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
        if ((this.bitField0_ & 1) != 0) {
            codedOutputByteBufferNano.writeUInt64(1, this.startTimestampMillis_);
        }
        if ((this.bitField0_ & 2) != 0) {
            codedOutputByteBufferNano.writeUInt64(2, this.durationMillis_);
        }
        if ((this.bitField0_ & 4) != 0) {
            codedOutputByteBufferNano.writeString(3, this.build_);
        }
        if ((this.bitField0_ & 8) != 0) {
            codedOutputByteBufferNano.writeInt32(4, this.result_);
        }
        TouchEvent[] touchEventArr = this.touchEvents;
        int i = 0;
        if (touchEventArr != null && touchEventArr.length > 0) {
            int i2 = 0;
            while (true) {
                TouchEvent[] touchEventArr2 = this.touchEvents;
                if (i2 >= touchEventArr2.length) {
                    break;
                }
                TouchEvent touchEvent = touchEventArr2[i2];
                if (touchEvent != null) {
                    codedOutputByteBufferNano.writeMessage(5, touchEvent);
                }
                i2++;
            }
        }
        SensorEvent[] sensorEventArr = this.sensorEvents;
        if (sensorEventArr != null && sensorEventArr.length > 0) {
            int i3 = 0;
            while (true) {
                SensorEvent[] sensorEventArr2 = this.sensorEvents;
                if (i3 >= sensorEventArr2.length) {
                    break;
                }
                SensorEvent sensorEvent = sensorEventArr2[i3];
                if (sensorEvent != null) {
                    codedOutputByteBufferNano.writeMessage(6, sensorEvent);
                }
                i3++;
            }
        }
        if ((this.bitField0_ & 16) != 0) {
            codedOutputByteBufferNano.writeInt32(9, this.touchAreaWidth_);
        }
        if ((this.bitField0_ & 32) != 0) {
            codedOutputByteBufferNano.writeInt32(10, this.touchAreaHeight_);
        }
        if ((this.bitField0_ & 64) != 0) {
            codedOutputByteBufferNano.writeInt32(11, this.type_);
        }
        PhoneEvent[] phoneEventArr = this.phoneEvents;
        if (phoneEventArr != null && phoneEventArr.length > 0) {
            while (true) {
                PhoneEvent[] phoneEventArr2 = this.phoneEvents;
                if (i >= phoneEventArr2.length) {
                    break;
                }
                PhoneEvent phoneEvent = phoneEventArr2[i];
                if (phoneEvent != null) {
                    codedOutputByteBufferNano.writeMessage(12, phoneEvent);
                }
                i++;
            }
        }
        super.writeTo(codedOutputByteBufferNano);
    }

    /* access modifiers changed from: protected */
    public int computeSerializedSize() {
        int computeSerializedSize = super.computeSerializedSize();
        if ((this.bitField0_ & 1) != 0) {
            computeSerializedSize += CodedOutputByteBufferNano.computeUInt64Size(1, this.startTimestampMillis_);
        }
        if ((this.bitField0_ & 2) != 0) {
            computeSerializedSize += CodedOutputByteBufferNano.computeUInt64Size(2, this.durationMillis_);
        }
        if ((this.bitField0_ & 4) != 0) {
            computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(3, this.build_);
        }
        if ((this.bitField0_ & 8) != 0) {
            computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(4, this.result_);
        }
        TouchEvent[] touchEventArr = this.touchEvents;
        int i = 0;
        if (touchEventArr != null && touchEventArr.length > 0) {
            int i2 = computeSerializedSize;
            int i3 = 0;
            while (true) {
                TouchEvent[] touchEventArr2 = this.touchEvents;
                if (i3 >= touchEventArr2.length) {
                    break;
                }
                TouchEvent touchEvent = touchEventArr2[i3];
                if (touchEvent != null) {
                    i2 += CodedOutputByteBufferNano.computeMessageSize(5, touchEvent);
                }
                i3++;
            }
            computeSerializedSize = i2;
        }
        SensorEvent[] sensorEventArr = this.sensorEvents;
        if (sensorEventArr != null && sensorEventArr.length > 0) {
            int i4 = computeSerializedSize;
            int i5 = 0;
            while (true) {
                SensorEvent[] sensorEventArr2 = this.sensorEvents;
                if (i5 >= sensorEventArr2.length) {
                    break;
                }
                SensorEvent sensorEvent = sensorEventArr2[i5];
                if (sensorEvent != null) {
                    i4 += CodedOutputByteBufferNano.computeMessageSize(6, sensorEvent);
                }
                i5++;
            }
            computeSerializedSize = i4;
        }
        if ((this.bitField0_ & 16) != 0) {
            computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(9, this.touchAreaWidth_);
        }
        if ((this.bitField0_ & 32) != 0) {
            computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(10, this.touchAreaHeight_);
        }
        if ((this.bitField0_ & 64) != 0) {
            computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(11, this.type_);
        }
        PhoneEvent[] phoneEventArr = this.phoneEvents;
        if (phoneEventArr != null && phoneEventArr.length > 0) {
            while (true) {
                PhoneEvent[] phoneEventArr2 = this.phoneEvents;
                if (i >= phoneEventArr2.length) {
                    break;
                }
                PhoneEvent phoneEvent = phoneEventArr2[i];
                if (phoneEvent != null) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(12, phoneEvent);
                }
                i++;
            }
        }
        return computeSerializedSize;
    }
}
