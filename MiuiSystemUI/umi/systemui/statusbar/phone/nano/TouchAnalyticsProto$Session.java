package com.android.systemui.statusbar.phone.nano;

import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.InternalNano;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.WireFormatNano;
import java.io.IOException;

public final class TouchAnalyticsProto$Session extends MessageNano {
    public String build;
    public String deviceId;
    public long durationMillis;
    public PhoneEvent[] phoneEvents;
    public int result;
    public SensorEvent[] sensorEvents;
    public long startTimestampMillis;
    public int touchAreaHeight;
    public int touchAreaWidth;
    public TouchEvent[] touchEvents;
    public int type;

    public static final class TouchEvent extends MessageNano {
        private static volatile TouchEvent[] _emptyArray;
        public int action;
        public int actionIndex;
        public Pointer[] pointers;
        public BoundingBox removedBoundingBox;
        public boolean removedRedacted;
        public long timeOffsetNanos;

        public static final class BoundingBox extends MessageNano {
        }

        public static final class Pointer extends MessageNano {
            private static volatile Pointer[] _emptyArray;
            public int id;
            public float pressure;
            public BoundingBox removedBoundingBox;
            public float removedLength;
            public float size;
            public float x;
            public float y;

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

            public Pointer() {
                clear();
            }

            public Pointer clear() {
                this.x = 0.0f;
                this.y = 0.0f;
                this.size = 0.0f;
                this.pressure = 0.0f;
                this.id = 0;
                this.removedLength = 0.0f;
                this.removedBoundingBox = null;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.google.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
                if (Float.floatToIntBits(this.x) != Float.floatToIntBits(0.0f)) {
                    codedOutputByteBufferNano.writeFloat(1, this.x);
                }
                if (Float.floatToIntBits(this.y) != Float.floatToIntBits(0.0f)) {
                    codedOutputByteBufferNano.writeFloat(2, this.y);
                }
                if (Float.floatToIntBits(this.size) != Float.floatToIntBits(0.0f)) {
                    codedOutputByteBufferNano.writeFloat(3, this.size);
                }
                if (Float.floatToIntBits(this.pressure) != Float.floatToIntBits(0.0f)) {
                    codedOutputByteBufferNano.writeFloat(4, this.pressure);
                }
                int i = this.id;
                if (i != 0) {
                    codedOutputByteBufferNano.writeInt32(5, i);
                }
                if (Float.floatToIntBits(this.removedLength) != Float.floatToIntBits(0.0f)) {
                    codedOutputByteBufferNano.writeFloat(6, this.removedLength);
                }
                BoundingBox boundingBox = this.removedBoundingBox;
                if (boundingBox != null) {
                    codedOutputByteBufferNano.writeMessage(7, boundingBox);
                }
                super.writeTo(codedOutputByteBufferNano);
            }

            /* access modifiers changed from: protected */
            @Override // com.google.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int computeSerializedSize = super.computeSerializedSize();
                if (Float.floatToIntBits(this.x) != Float.floatToIntBits(0.0f)) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeFloatSize(1, this.x);
                }
                if (Float.floatToIntBits(this.y) != Float.floatToIntBits(0.0f)) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeFloatSize(2, this.y);
                }
                if (Float.floatToIntBits(this.size) != Float.floatToIntBits(0.0f)) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeFloatSize(3, this.size);
                }
                if (Float.floatToIntBits(this.pressure) != Float.floatToIntBits(0.0f)) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeFloatSize(4, this.pressure);
                }
                int i = this.id;
                if (i != 0) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(5, i);
                }
                if (Float.floatToIntBits(this.removedLength) != Float.floatToIntBits(0.0f)) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeFloatSize(6, this.removedLength);
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

        public TouchEvent() {
            clear();
        }

        public TouchEvent clear() {
            this.timeOffsetNanos = 0;
            this.action = 0;
            this.actionIndex = 0;
            this.pointers = Pointer.emptyArray();
            this.removedRedacted = false;
            this.removedBoundingBox = null;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.google.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            long j = this.timeOffsetNanos;
            if (j != 0) {
                codedOutputByteBufferNano.writeUInt64(1, j);
            }
            int i = this.action;
            if (i != 0) {
                codedOutputByteBufferNano.writeInt32(2, i);
            }
            int i2 = this.actionIndex;
            if (i2 != 0) {
                codedOutputByteBufferNano.writeInt32(3, i2);
            }
            Pointer[] pointerArr = this.pointers;
            if (pointerArr != null && pointerArr.length > 0) {
                int i3 = 0;
                while (true) {
                    Pointer[] pointerArr2 = this.pointers;
                    if (i3 >= pointerArr2.length) {
                        break;
                    }
                    Pointer pointer = pointerArr2[i3];
                    if (pointer != null) {
                        codedOutputByteBufferNano.writeMessage(4, pointer);
                    }
                    i3++;
                }
            }
            boolean z = this.removedRedacted;
            if (z) {
                codedOutputByteBufferNano.writeBool(5, z);
            }
            BoundingBox boundingBox = this.removedBoundingBox;
            if (boundingBox != null) {
                codedOutputByteBufferNano.writeMessage(6, boundingBox);
            }
            super.writeTo(codedOutputByteBufferNano);
        }

        /* access modifiers changed from: protected */
        @Override // com.google.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int computeSerializedSize = super.computeSerializedSize();
            long j = this.timeOffsetNanos;
            if (j != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeUInt64Size(1, j);
            }
            int i = this.action;
            if (i != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(2, i);
            }
            int i2 = this.actionIndex;
            if (i2 != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(3, i2);
            }
            Pointer[] pointerArr = this.pointers;
            if (pointerArr != null && pointerArr.length > 0) {
                int i3 = 0;
                while (true) {
                    Pointer[] pointerArr2 = this.pointers;
                    if (i3 >= pointerArr2.length) {
                        break;
                    }
                    Pointer pointer = pointerArr2[i3];
                    if (pointer != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(4, pointer);
                    }
                    i3++;
                }
            }
            boolean z = this.removedRedacted;
            if (z) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(5, z);
            }
            BoundingBox boundingBox = this.removedBoundingBox;
            return boundingBox != null ? computeSerializedSize + CodedOutputByteBufferNano.computeMessageSize(6, boundingBox) : computeSerializedSize;
        }
    }

    public static final class SensorEvent extends MessageNano {
        private static volatile SensorEvent[] _emptyArray;
        public long timeOffsetNanos;
        public long timestamp;
        public int type;
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

        public SensorEvent() {
            clear();
        }

        public SensorEvent clear() {
            this.type = 1;
            this.timeOffsetNanos = 0;
            this.values = WireFormatNano.EMPTY_FLOAT_ARRAY;
            this.timestamp = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.google.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            int i = this.type;
            if (i != 1) {
                codedOutputByteBufferNano.writeInt32(1, i);
            }
            long j = this.timeOffsetNanos;
            if (j != 0) {
                codedOutputByteBufferNano.writeUInt64(2, j);
            }
            float[] fArr = this.values;
            if (fArr != null && fArr.length > 0) {
                int i2 = 0;
                while (true) {
                    float[] fArr2 = this.values;
                    if (i2 >= fArr2.length) {
                        break;
                    }
                    codedOutputByteBufferNano.writeFloat(3, fArr2[i2]);
                    i2++;
                }
            }
            long j2 = this.timestamp;
            if (j2 != 0) {
                codedOutputByteBufferNano.writeUInt64(4, j2);
            }
            super.writeTo(codedOutputByteBufferNano);
        }

        /* access modifiers changed from: protected */
        @Override // com.google.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int computeSerializedSize = super.computeSerializedSize();
            int i = this.type;
            if (i != 1) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            long j = this.timeOffsetNanos;
            if (j != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeUInt64Size(2, j);
            }
            float[] fArr = this.values;
            if (fArr != null && fArr.length > 0) {
                computeSerializedSize = computeSerializedSize + (fArr.length * 4) + (fArr.length * 1);
            }
            long j2 = this.timestamp;
            return j2 != 0 ? computeSerializedSize + CodedOutputByteBufferNano.computeUInt64Size(4, j2) : computeSerializedSize;
        }
    }

    public static final class PhoneEvent extends MessageNano {
        private static volatile PhoneEvent[] _emptyArray;
        public long timeOffsetNanos;
        public int type;

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

        public PhoneEvent() {
            clear();
        }

        public PhoneEvent clear() {
            this.type = 0;
            this.timeOffsetNanos = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.google.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            int i = this.type;
            if (i != 0) {
                codedOutputByteBufferNano.writeInt32(1, i);
            }
            long j = this.timeOffsetNanos;
            if (j != 0) {
                codedOutputByteBufferNano.writeUInt64(2, j);
            }
            super.writeTo(codedOutputByteBufferNano);
        }

        /* access modifiers changed from: protected */
        @Override // com.google.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int computeSerializedSize = super.computeSerializedSize();
            int i = this.type;
            if (i != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            long j = this.timeOffsetNanos;
            return j != 0 ? computeSerializedSize + CodedOutputByteBufferNano.computeUInt64Size(2, j) : computeSerializedSize;
        }
    }

    public TouchAnalyticsProto$Session() {
        clear();
    }

    public TouchAnalyticsProto$Session clear() {
        this.startTimestampMillis = 0;
        this.durationMillis = 0;
        this.build = "";
        this.result = 0;
        this.touchEvents = TouchEvent.emptyArray();
        this.sensorEvents = SensorEvent.emptyArray();
        this.touchAreaWidth = 0;
        this.touchAreaHeight = 0;
        this.type = 0;
        this.phoneEvents = PhoneEvent.emptyArray();
        this.deviceId = "";
        this.cachedSize = -1;
        return this;
    }

    @Override // com.google.protobuf.nano.MessageNano
    public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
        long j = this.startTimestampMillis;
        if (j != 0) {
            codedOutputByteBufferNano.writeUInt64(1, j);
        }
        long j2 = this.durationMillis;
        if (j2 != 0) {
            codedOutputByteBufferNano.writeUInt64(2, j2);
        }
        if (!this.build.equals("")) {
            codedOutputByteBufferNano.writeString(3, this.build);
        }
        int i = this.result;
        if (i != 0) {
            codedOutputByteBufferNano.writeInt32(4, i);
        }
        TouchEvent[] touchEventArr = this.touchEvents;
        int i2 = 0;
        if (touchEventArr != null && touchEventArr.length > 0) {
            int i3 = 0;
            while (true) {
                TouchEvent[] touchEventArr2 = this.touchEvents;
                if (i3 >= touchEventArr2.length) {
                    break;
                }
                TouchEvent touchEvent = touchEventArr2[i3];
                if (touchEvent != null) {
                    codedOutputByteBufferNano.writeMessage(5, touchEvent);
                }
                i3++;
            }
        }
        SensorEvent[] sensorEventArr = this.sensorEvents;
        if (sensorEventArr != null && sensorEventArr.length > 0) {
            int i4 = 0;
            while (true) {
                SensorEvent[] sensorEventArr2 = this.sensorEvents;
                if (i4 >= sensorEventArr2.length) {
                    break;
                }
                SensorEvent sensorEvent = sensorEventArr2[i4];
                if (sensorEvent != null) {
                    codedOutputByteBufferNano.writeMessage(6, sensorEvent);
                }
                i4++;
            }
        }
        int i5 = this.touchAreaWidth;
        if (i5 != 0) {
            codedOutputByteBufferNano.writeInt32(9, i5);
        }
        int i6 = this.touchAreaHeight;
        if (i6 != 0) {
            codedOutputByteBufferNano.writeInt32(10, i6);
        }
        int i7 = this.type;
        if (i7 != 0) {
            codedOutputByteBufferNano.writeInt32(11, i7);
        }
        PhoneEvent[] phoneEventArr = this.phoneEvents;
        if (phoneEventArr != null && phoneEventArr.length > 0) {
            while (true) {
                PhoneEvent[] phoneEventArr2 = this.phoneEvents;
                if (i2 >= phoneEventArr2.length) {
                    break;
                }
                PhoneEvent phoneEvent = phoneEventArr2[i2];
                if (phoneEvent != null) {
                    codedOutputByteBufferNano.writeMessage(12, phoneEvent);
                }
                i2++;
            }
        }
        if (!this.deviceId.equals("")) {
            codedOutputByteBufferNano.writeString(13, this.deviceId);
        }
        super.writeTo(codedOutputByteBufferNano);
    }

    /* access modifiers changed from: protected */
    @Override // com.google.protobuf.nano.MessageNano
    public int computeSerializedSize() {
        int computeSerializedSize = super.computeSerializedSize();
        long j = this.startTimestampMillis;
        if (j != 0) {
            computeSerializedSize += CodedOutputByteBufferNano.computeUInt64Size(1, j);
        }
        long j2 = this.durationMillis;
        if (j2 != 0) {
            computeSerializedSize += CodedOutputByteBufferNano.computeUInt64Size(2, j2);
        }
        if (!this.build.equals("")) {
            computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(3, this.build);
        }
        int i = this.result;
        if (i != 0) {
            computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(4, i);
        }
        TouchEvent[] touchEventArr = this.touchEvents;
        int i2 = 0;
        if (touchEventArr != null && touchEventArr.length > 0) {
            int i3 = 0;
            while (true) {
                TouchEvent[] touchEventArr2 = this.touchEvents;
                if (i3 >= touchEventArr2.length) {
                    break;
                }
                TouchEvent touchEvent = touchEventArr2[i3];
                if (touchEvent != null) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(5, touchEvent);
                }
                i3++;
            }
        }
        SensorEvent[] sensorEventArr = this.sensorEvents;
        if (sensorEventArr != null && sensorEventArr.length > 0) {
            int i4 = 0;
            while (true) {
                SensorEvent[] sensorEventArr2 = this.sensorEvents;
                if (i4 >= sensorEventArr2.length) {
                    break;
                }
                SensorEvent sensorEvent = sensorEventArr2[i4];
                if (sensorEvent != null) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(6, sensorEvent);
                }
                i4++;
            }
        }
        int i5 = this.touchAreaWidth;
        if (i5 != 0) {
            computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(9, i5);
        }
        int i6 = this.touchAreaHeight;
        if (i6 != 0) {
            computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(10, i6);
        }
        int i7 = this.type;
        if (i7 != 0) {
            computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(11, i7);
        }
        PhoneEvent[] phoneEventArr = this.phoneEvents;
        if (phoneEventArr != null && phoneEventArr.length > 0) {
            while (true) {
                PhoneEvent[] phoneEventArr2 = this.phoneEvents;
                if (i2 >= phoneEventArr2.length) {
                    break;
                }
                PhoneEvent phoneEvent = phoneEventArr2[i2];
                if (phoneEvent != null) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(12, phoneEvent);
                }
                i2++;
            }
        }
        return !this.deviceId.equals("") ? computeSerializedSize + CodedOutputByteBufferNano.computeStringSize(13, this.deviceId) : computeSerializedSize;
    }
}
