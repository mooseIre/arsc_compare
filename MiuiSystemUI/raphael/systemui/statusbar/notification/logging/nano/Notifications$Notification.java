package com.android.systemui.statusbar.notification.logging.nano;

import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.InternalNano;
import com.google.protobuf.nano.MessageNano;
import java.io.IOException;

public final class Notifications$Notification extends MessageNano {
    private static volatile Notifications$Notification[] _emptyArray;
    public int groupInstanceId;
    public int instanceId;
    public boolean isGroupSummary;
    public String packageName;
    public int section;
    public int uid;

    public static Notifications$Notification[] emptyArray() {
        if (_emptyArray == null) {
            synchronized (InternalNano.LAZY_INIT_LOCK) {
                if (_emptyArray == null) {
                    _emptyArray = new Notifications$Notification[0];
                }
            }
        }
        return _emptyArray;
    }

    public Notifications$Notification() {
        clear();
    }

    public Notifications$Notification clear() {
        this.uid = 0;
        this.packageName = "";
        this.instanceId = 0;
        this.groupInstanceId = 0;
        this.isGroupSummary = false;
        this.section = 0;
        this.cachedSize = -1;
        return this;
    }

    public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
        int i = this.uid;
        if (i != 0) {
            codedOutputByteBufferNano.writeInt32(1, i);
        }
        if (!this.packageName.equals("")) {
            codedOutputByteBufferNano.writeString(2, this.packageName);
        }
        int i2 = this.instanceId;
        if (i2 != 0) {
            codedOutputByteBufferNano.writeInt32(3, i2);
        }
        int i3 = this.groupInstanceId;
        if (i3 != 0) {
            codedOutputByteBufferNano.writeInt32(4, i3);
        }
        boolean z = this.isGroupSummary;
        if (z) {
            codedOutputByteBufferNano.writeBool(5, z);
        }
        int i4 = this.section;
        if (i4 != 0) {
            codedOutputByteBufferNano.writeInt32(6, i4);
        }
        super.writeTo(codedOutputByteBufferNano);
    }

    /* access modifiers changed from: protected */
    public int computeSerializedSize() {
        int computeSerializedSize = super.computeSerializedSize();
        int i = this.uid;
        if (i != 0) {
            computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(1, i);
        }
        if (!this.packageName.equals("")) {
            computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.packageName);
        }
        int i2 = this.instanceId;
        if (i2 != 0) {
            computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(3, i2);
        }
        int i3 = this.groupInstanceId;
        if (i3 != 0) {
            computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(4, i3);
        }
        boolean z = this.isGroupSummary;
        if (z) {
            computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(5, z);
        }
        int i4 = this.section;
        return i4 != 0 ? computeSerializedSize + CodedOutputByteBufferNano.computeInt32Size(6, i4) : computeSerializedSize;
    }
}
