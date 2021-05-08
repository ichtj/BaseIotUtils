//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.eclipse.paho.android.service;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class ParcelableMqttMessage extends MqttMessage implements Parcelable {
    String messageId = null;
    public static final Creator<ParcelableMqttMessage> CREATOR = new Creator<ParcelableMqttMessage>() {
        public ParcelableMqttMessage createFromParcel(Parcel parcel) {
            return new ParcelableMqttMessage(parcel);
        }

        public ParcelableMqttMessage[] newArray(int size) {
            return new ParcelableMqttMessage[size];
        }
    };

    ParcelableMqttMessage(MqttMessage original) {
        super(original.getPayload());
        this.setQos(original.getQos());
        this.setRetained(original.isRetained());
        this.setDuplicate(original.isDuplicate());
    }

    ParcelableMqttMessage(Parcel parcel) {
        super(parcel.createByteArray());
        this.setQos(parcel.readInt());
        boolean[] flags = parcel.createBooleanArray();
        this.setRetained(flags[0]);
        this.setDuplicate(flags[1]);
        this.messageId = parcel.readString();
    }

    public String getMessageId() {
        return this.messageId;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeByteArray(this.getPayload());
        parcel.writeInt(this.getQos());
        parcel.writeBooleanArray(new boolean[]{this.isRetained(), this.isDuplicate()});
        parcel.writeString(this.messageId);
    }
}
