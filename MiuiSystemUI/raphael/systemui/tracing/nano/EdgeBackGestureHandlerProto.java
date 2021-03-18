package com.android.systemui.tracing.nano;

import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.MessageNano;
import java.io.IOException;

public final class EdgeBackGestureHandlerProto extends MessageNano {
    public boolean allowGesture;

    public EdgeBackGestureHandlerProto() {
        clear();
    }

    public EdgeBackGestureHandlerProto clear() {
        this.allowGesture = false;
        this.cachedSize = -1;
        return this;
    }

    @Override // com.google.protobuf.nano.MessageNano
    public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
        boolean z = this.allowGesture;
        if (z) {
            codedOutputByteBufferNano.writeBool(1, z);
        }
        super.writeTo(codedOutputByteBufferNano);
    }

    /* access modifiers changed from: protected */
    @Override // com.google.protobuf.nano.MessageNano
    public int computeSerializedSize() {
        int computeSerializedSize = super.computeSerializedSize();
        boolean z = this.allowGesture;
        return z ? computeSerializedSize + CodedOutputByteBufferNano.computeBoolSize(1, z) : computeSerializedSize;
    }
}
