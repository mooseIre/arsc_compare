package com.android.systemui.statusbar.notification.logging.nano;

import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.MessageNano;
import java.io.IOException;

public final class Notifications$NotificationList extends MessageNano {
    public Notifications$Notification[] notifications;

    public Notifications$NotificationList() {
        clear();
    }

    public Notifications$NotificationList clear() {
        this.notifications = Notifications$Notification.emptyArray();
        this.cachedSize = -1;
        return this;
    }

    public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
        Notifications$Notification[] notifications$NotificationArr = this.notifications;
        if (notifications$NotificationArr != null && notifications$NotificationArr.length > 0) {
            int i = 0;
            while (true) {
                Notifications$Notification[] notifications$NotificationArr2 = this.notifications;
                if (i >= notifications$NotificationArr2.length) {
                    break;
                }
                Notifications$Notification notifications$Notification = notifications$NotificationArr2[i];
                if (notifications$Notification != null) {
                    codedOutputByteBufferNano.writeMessage(1, notifications$Notification);
                }
                i++;
            }
        }
        super.writeTo(codedOutputByteBufferNano);
    }

    /* access modifiers changed from: protected */
    public int computeSerializedSize() {
        int computeSerializedSize = super.computeSerializedSize();
        Notifications$Notification[] notifications$NotificationArr = this.notifications;
        if (notifications$NotificationArr != null && notifications$NotificationArr.length > 0) {
            int i = 0;
            while (true) {
                Notifications$Notification[] notifications$NotificationArr2 = this.notifications;
                if (i >= notifications$NotificationArr2.length) {
                    break;
                }
                Notifications$Notification notifications$Notification = notifications$NotificationArr2[i];
                if (notifications$Notification != null) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(1, notifications$Notification);
                }
                i++;
            }
        }
        return computeSerializedSize;
    }
}
