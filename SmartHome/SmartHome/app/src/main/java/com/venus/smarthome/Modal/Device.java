package com.venus.smarthome.Modal;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Device implements Parcelable {
    public String id, name, type;
    public int speed, status;

    public Device() {
    }

    public Device(String id, String name, String type, int speed, int status) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.speed = speed;
        this.status = status;
    }

    protected Device(Parcel in) {
        id = in.readString();
        name = in.readString();
        type = in.readString();
        speed = in.readInt();
        status = in.readInt();
    }

    public static final Creator<Device> CREATOR = new Creator<Device>() {
        @Override
        public Device createFromParcel(Parcel in) {
            return new Device(in);
        }

        @Override
        public Device[] newArray(int size) {
            return new Device[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(type);
        parcel.writeInt(speed);
        parcel.writeInt(status);
    }
}

