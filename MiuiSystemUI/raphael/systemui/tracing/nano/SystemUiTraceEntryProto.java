package com.android.systemui.tracing.nano;

import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.InternalNano;
import com.google.protobuf.nano.MessageNano;
import java.io.IOException;

public final class SystemUiTraceEntryProto extends MessageNano {
    private static volatile SystemUiTraceEntryProto[] _emptyArray;
    public long elapsedRealtimeNanos;
    public SystemUiTraceProto systemUi;

    public static SystemUiTraceEntryProto[] emptyArray() {
        if (_emptyArray == null) {
            synchronized (InternalNano.LAZY_INIT_LOCK) {
                if (_emptyArray == null) {
                    _emptyArray = new SystemUiTraceEntryProto[0];
                }
            }
        }
        return _emptyArray;
    }

    public SystemUiTraceEntryProto() {
        clear();
    }

    public SystemUiTraceEntryProto clear() {
        this.elapsedRealtimeNanos = 0;
        this.systemUi = null;
        this.cachedSize = -1;
        return this;
    }

    public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
        long j = this.elapsedRealtimeNanos;
        if (j != 0) {
            codedOutputByteBufferNano.writeFixed64(1, j);
        }
        SystemUiTraceProto systemUiTraceProto = this.systemUi;
        if (systemUiTraceProto != null) {
            codedOutputByteBufferNano.writeMessage(3, systemUiTraceProto);
        }
        super.writeTo(codedOutputByteBufferNano);
    }

    /* access modifiers changed from: protected */
    public int computeSerializedSize() {
        int computeSerializedSize = super.computeSerializedSize();
        long j = this.elapsedRealtimeNanos;
        if (j != 0) {
            computeSerializedSize += CodedOutputByteBufferNano.computeFixed64Size(1, j);
        }
        SystemUiTraceProto systemUiTraceProto = this.systemUi;
        return systemUiTraceProto != null ? computeSerializedSize + CodedOutputByteBufferNano.computeMessageSize(3, systemUiTraceProto) : computeSerializedSize;
    }
}
